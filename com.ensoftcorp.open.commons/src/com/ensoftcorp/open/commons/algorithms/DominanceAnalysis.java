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
import com.ensoftcorp.open.commons.sandbox.Sandbox;
import com.ensoftcorp.open.commons.sandbox.SandboxEdge;
import com.ensoftcorp.open.commons.sandbox.SandboxGraph;
import com.ensoftcorp.open.commons.sandbox.SandboxHashSet;
import com.ensoftcorp.open.commons.sandbox.SandboxNode;
import com.ensoftcorp.open.commons.xcsg.XCSG_Extension;

public class DominanceAnalysis extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the PDG codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.dominance";
	
	/**
	 * The immediate dominator or idom of a node n is the unique node that strictly
	 * dominates n but does not strictly dominate any other node that strictly
	 * dominates n. Every node, except the entry node, has an immediate dominator.
	 */
	@XCSG_Extension
	public static final String IMMEDIATE_DOMINANCE_EDGE = "idom";
	
	/**
	 * Used to tag the edges from a node that post-dominate a node.
	 * 
	 * Wikipedia:  A node z is said to post-dominate a node n if all 
	 * paths to the exit node of the graph starting at n must go through z.
	 */
	@XCSG_Extension
	public static final String POST_DOMINANCE_EDGE = "postdom";
	
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
	
	public static Q getPostDominanceEdges(){
		return Common.universe().edges(POST_DOMINANCE_EDGE).retainEdges();
	}
	
	public static Q getDominanceFrontierEdges(){
		return Common.universe().edges(DOMINANCE_FRONTIER_EDGE).retainEdges();
	}

	public static Q getImmediateDominanceEdges(){
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
		if(CommonsPreferences.isComputeControlFlowGraphDominanceEnabled() || CommonsPreferences.isComputeExceptionalControlFlowGraphDominanceEnabled()){
			Log.info("Computing Control Flow Graph Dominator Trees");
			AtlasSet<Node> functions = Query.resolve(null, Query.universe().nodes(XCSG.Function).eval().nodes());
			SubMonitor task = SubMonitor.convert(monitor, (int) functions.size());
			int functionsCompleted = 0;
			for(Node function : functions){
				Q cfg;
				if(CommonsPreferences.isComputeControlFlowGraphDominanceEnabled()){
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
	public static SandboxGraph computeSandboxedForwardDominatorTree(Sandbox sandbox, UniqueEntryExitGraph ucfg){
		SandboxHashSet<SandboxEdge> treeEdges = new SandboxHashSet<SandboxEdge>(sandbox);
		SandboxGraph dominance = computeSandboxedDominance(sandbox, ucfg);
		for(SandboxEdge edge : dominance.edges()){
			if(edge.taggedWith(POST_DOMINANCE_EDGE)){
				treeEdges.add(edge);
			}
		}
		return sandbox.toGraph(treeEdges);
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
	public static SandboxGraph computeSandboxedDominanceFrontier(Sandbox sandbox, UniqueEntryExitGraph ucfg){
		SandboxHashSet<SandboxEdge> frontierEdges = new SandboxHashSet<SandboxEdge>(sandbox);
		SandboxGraph dominance = computeSandboxedDominance(sandbox, ucfg);
		for(SandboxEdge edge : dominance.edges()){
			if(edge.taggedWith(DOMINANCE_FRONTIER_EDGE)){
				frontierEdges.add(edge);
			}
		}
		return sandbox.toGraph(frontierEdges);
	}

	/**
	 * Returns a graph of ifdom and domfrontier edges
	 * @param ucfg
	 * @return
	 */
	public static SandboxGraph computeSandboxedDominance(Sandbox sandbox, UniqueEntryExitGraph ucfg) {
		DominanceAnalysisHelper dominanceAnalysis = new DominanceAnalysisHelper(ucfg, true);
		// compute the dominator tree
		Multimap<Node> dominanceTree = dominanceAnalysis.getDominatorTree();
		SandboxHashSet<SandboxEdge> dominanceEdges = new SandboxHashSet<SandboxEdge>(sandbox);
		for(Entry<Node, Set<Node>> entry : dominanceTree.entrySet()){
			Node fromNode = entry.getKey();
			SandboxNode sandboxFromNode = sandbox.addNode(fromNode);
			for(Node toNode : entry.getValue()){
				SandboxNode sandboxToNode = sandbox.addNode(toNode);
				SandboxGraph forwardDominanceEdges = sandbox.toGraph(sandbox.universe().edges(POST_DOMINANCE_EDGE));
				SandboxEdge forwardDominanceEdge = forwardDominanceEdges.betweenStep(sandboxFromNode, sandboxToNode).edges().one();
				if(forwardDominanceEdge == null){
					forwardDominanceEdge = sandbox.createEdge(sandboxFromNode, sandboxToNode);
					forwardDominanceEdge.tag(POST_DOMINANCE_EDGE);
					forwardDominanceEdge.putAttr(XCSG.name, POST_DOMINANCE_EDGE);
				}
				dominanceEdges.add(forwardDominanceEdge);
			}
		}
		// compute the dominance frontier
		Multimap<Node> dominanceFrontier = dominanceAnalysis.getDominanceFrontiers();
		for(Entry<Node, Set<Node>> entry : dominanceFrontier.entrySet()){
			Node fromNode = entry.getKey();
			SandboxNode sandboxFromNode = sandbox.addNode(fromNode);
			for(Node toNode : entry.getValue()){
				SandboxNode sandboxToNode = sandbox.addNode(toNode);
				SandboxGraph dominanceFrontierEdges = sandbox.toGraph(sandbox.universe().edges(DOMINANCE_FRONTIER_EDGE));
				SandboxEdge dominanceFrontierEdge = dominanceFrontierEdges.betweenStep(sandboxFromNode, sandboxToNode).edges().one();
				if(dominanceFrontierEdge == null){
					dominanceFrontierEdge = sandbox.createEdge(sandboxFromNode, sandboxToNode);
					dominanceFrontierEdge.tag(DOMINANCE_FRONTIER_EDGE);
					dominanceFrontierEdge.putAttr(XCSG.name, DOMINANCE_FRONTIER_EDGE);
				}
				dominanceEdges.add(dominanceFrontierEdge);
			}
		}
		return sandbox.toGraph(dominanceEdges);
	}
	
	/**
	 * Returns the post dominance graph
	 * 
	 * Note this method will compute both the dominance frontier and the
	 * dominator tree. If you want both edges, call the
	 * computeDominance(UniqueEntryExitGraph ucfg) method directly.
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computePostDominance(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> treeEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(POST_DOMINANCE_EDGE)){
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
		// compute the post-dominators
		Multimap<Node> dominanceTree = dominanceAnalysis.getDominatorTree();
		AtlasSet<Edge> dominanceEdges = new AtlasHashSet<Edge>();
		for(Entry<Node, Set<Node>> entry : dominanceTree.entrySet()){
			Node fromNode = entry.getKey();
			for(Node toNode : entry.getValue()){
				Q forwardDominanceEdges = Common.universe().edges(POST_DOMINANCE_EDGE);
				Edge forwardDominanceEdge = forwardDominanceEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
				if(forwardDominanceEdge == null){
					forwardDominanceEdge = Graph.U.createEdge(fromNode, toNode);
					forwardDominanceEdge.tag(POST_DOMINANCE_EDGE);
					forwardDominanceEdge.putAttr(XCSG.name, POST_DOMINANCE_EDGE);
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
		
		// compute the immediate dominators (idoms)
		for(Entry<Node,Node> entry : dominanceAnalysis.getIdoms().entrySet()) {
			Node fromNode = entry.getKey();
			Node toNode = entry.getValue();
			Q idomEdges = Common.universe().edges(IMMEDIATE_DOMINANCE_EDGE);
			Edge idomEdge = idomEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
			if(idomEdge == null){
				idomEdge = Graph.U.createEdge(fromNode, toNode);
				idomEdge.tag(IMMEDIATE_DOMINANCE_EDGE);
				idomEdge.putAttr(XCSG.name, IMMEDIATE_DOMINANCE_EDGE);
			}
			dominanceEdges.add(idomEdge);
		}
		
		return Common.toQ(dominanceEdges).eval();
	}

}
