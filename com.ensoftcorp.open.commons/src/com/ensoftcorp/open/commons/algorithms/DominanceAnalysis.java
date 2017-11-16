package com.ensoftcorp.open.commons.algorithms;

import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.internal.DominanceAnalysisHelper;
import com.ensoftcorp.open.commons.algorithms.internal.DominanceAnalysisHelper.Multimap;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;
import com.ensoftcorp.open.commons.xcsg.XCSG_Extension;

public class DominanceAnalysis extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the PDG codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.dominance";
	
	/**
	 * Used to tag the edges from a node that immediately forward dominate
	 * (post-dominate) a node. The set of ifdom edges forms the dominator tree.
	 * 
	 * Wikipedia: The immediate dominator or idom of a node n is the unique node
	 * that strictly dominates n but does not strictly dominate any other node
	 * that strictly dominates n. Every node, except the entry node, has an
	 * immediate dominator. Analogous to the definition of dominance above, a
	 * node z is said to post-dominate a node n if all paths to the exit node of
	 * the graph starting at n must go through z. Similarly, the immediate
	 * post-dominator of a node n is the postdominator of n that doesn't
	 * strictly postdominate any other strict postdominators of n. A dominator
	 * tree is a tree where each node's children are those nodes it immediately
	 * dominates. Because the immediate dominator is unique, it is a tree. The
	 * start node is the root of the tree.
	 */
	@XCSG_Extension
	public static final String IMMEDIATE_FORWARD_DOMINANCE_EDGE = "ifdom";
	
	/**
	 * Used to tag the edges from a node the identify the node's dominance
	 * frontier.
	 * 
	 * Wikipedia: The dominance frontier of a node d is the set of all nodes n
	 * such that d dominates an immediate predecessor of n, but d does not
	 * strictly dominate n. It is the set of nodes where d's dominance stops.
	 */
	@XCSG_Extension
	public static final String DOMINANCE_FRONTIER_EDGE = "domfrontier";
	
	public DominanceAnalysis() {}
	
	public static Q getForwardDominanceTrees(){
		return Common.universe().edges(IMMEDIATE_FORWARD_DOMINANCE_EDGE).retainEdges();
	}
	
	public static Q getDominanceFrontiers(){
		return Common.universe().edges(DOMINANCE_FRONTIER_EDGE).retainEdges();
	}

	@Override
	public String getDisplayName() {
		return "Computing Control Flow Graph Dominator Trees";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{};
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		if(CommonsPreferences.isComputeControlFlowGraphDominanceTreesEnabled() || CommonsPreferences.isComputeExceptionalControlFlowGraphDominanceTreesEnabled()){
			Log.info("Computing Control Flow Graph Dominator Trees");
			AtlasSet<Node> functions = Query.resolve(null, Query.universe().nodes(XCSG.Function).eval().nodes());
			SubMonitor task = SubMonitor.convert(monitor, (int) functions.size());
			int functionsCompleted = 0;
			for(Node function : functions){
				Q cfg;
				if(CommonsPreferences.isComputeControlFlowGraphDominanceTreesEnabled()){
					cfg = CommonQueries.cfg(function);
				} else {
					cfg = CommonQueries.excfg(function);
				}
				Graph g = cfg.eval();
				AtlasSet<Node> roots = cfg.nodes(XCSG.controlFlowRoot).eval().nodes();
				AtlasSet<Node> exits = cfg.nodes(XCSG.controlFlowExitPoint).eval().nodes();
				if(g.nodes().isEmpty() || roots.isEmpty() || exits.isEmpty()){
					// nothing to compute
					task.setWorkRemaining(((int) functions.size())-(functionsCompleted++));
					continue;
				} else {
					try {
						UniqueEntryExitGraph uexg = new UniqueEntryExitControlFlowGraph(g, roots, exits, CommonsPreferences.isMasterEntryExitContainmentRelationshipsEnabled());
						computeDominance(uexg);
					} catch (Exception e){
						Log.error("Error computing control flow graph dominance tree", e);
					}
					if(monitor.isCanceled()){
						Log.warning("Cancelled: Computing Control Flow Graph Dominator Trees");
						break;
					}
					task.setWorkRemaining(((int) functions.size())-(functionsCompleted++));
				}
			}
		}
	}
	
	/**
	 * Returns the dominator tree
	 * 
	 * Note this method will compute both the dominance frontier and the
	 * dominator tree. If you want both edges, call the
	 * computeDominance(UniqueEntryExitGraph ucfg) method directly.
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computeForwardDominatorTree(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> treeEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(IMMEDIATE_FORWARD_DOMINANCE_EDGE)){
				treeEdges.add(edge);
			}
		}
		return Common.toQ(treeEdges).eval();
	}
	
	/**
	 * Returns the dominance frontier (each edge represents a frontier node for
	 * a given from node)
	 * 
	 * Note this method will compute both the dominance frontier and the
	 * dominator tree. If you want both edges, call the
	 * computeDominance(UniqueEntryExitGraph ucfg) method directly.
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computeDominanceFrontier(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> frontierEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(DOMINANCE_FRONTIER_EDGE)){
				frontierEdges.add(edge);
			}
		}
		return Common.toQ(frontierEdges).eval();
	}

	/**
	 * Returns a graph of ifdom and domfrontier edges
	 * @param ucfg
	 * @return
	 */
	public static Graph computeDominance(UniqueEntryExitGraph ucfg) {
		DominanceAnalysisHelper dominanceAnalysis = new DominanceAnalysisHelper(ucfg, true);
		// compute the dominator tree
		Multimap<Node> dominanceTree = dominanceAnalysis.getDominatorTree();
		AtlasSet<Edge> dominanceEdges = new AtlasHashSet<Edge>();
		for(Entry<Node, Set<Node>> entry : dominanceTree.entrySet()){
			Node fromNode = entry.getKey();
			for(Node toNode : entry.getValue()){
				Q forwardDominanceEdges = Common.universe().edges(IMMEDIATE_FORWARD_DOMINANCE_EDGE);
				Edge forwardDominanceEdge = forwardDominanceEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
				if(forwardDominanceEdge == null){
					forwardDominanceEdge = Graph.U.createEdge(fromNode, toNode);
					forwardDominanceEdge.tag(IMMEDIATE_FORWARD_DOMINANCE_EDGE);
					forwardDominanceEdge.putAttr(XCSG.name, IMMEDIATE_FORWARD_DOMINANCE_EDGE);
				}
				dominanceEdges.add(forwardDominanceEdge);
			}
		}
		// compute the dominance frontier
		Multimap<Node> dominanceFrontier = dominanceAnalysis.getDominanceFrontiers();
		for(Entry<Node, Set<Node>> entry : dominanceFrontier.entrySet()){
			Node fromNode = entry.getKey();
			for(Node toNode : entry.getValue()){
				Q dominanceFrontierEdges = Common.universe().edges(DOMINANCE_FRONTIER_EDGE);
				Edge dominanceFrontierEdge = dominanceFrontierEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
				if(dominanceFrontierEdge == null){
					dominanceFrontierEdge = Graph.U.createEdge(fromNode, toNode);
					dominanceFrontierEdge.tag(DOMINANCE_FRONTIER_EDGE);
					dominanceFrontierEdge.putAttr(XCSG.name, DOMINANCE_FRONTIER_EDGE);
				}
				dominanceEdges.add(dominanceFrontierEdge);
			}
		}
		return Common.toQ(dominanceEdges).eval();
	}

}
