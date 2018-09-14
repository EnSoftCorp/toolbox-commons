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
import com.ensoftcorp.open.commons.algorithms.internal.DominatorTree;
import com.ensoftcorp.open.commons.algorithms.internal.PostDominatorTree;
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
	 * Because the immediate dominator is unique, it is a tree. The start node is
	 * the root of the tree.
	 */
	@XCSG_Extension
	public static final String DOMINATOR_TREE_EDGE = "idom";
	
	/**
	 * Used to tag the edges from a node that post-dominate a node.
	 * 
	 * Wikipedia: Analogous to the definition of dominance above, a node z is said
	 * to post-dominate a node n if all paths to the exit node of the graph starting
	 * at n must go through z. Similarly, the immediate post-dominator of a node n
	 * is the postdominator of n that doesn't strictly postdominate any other strict
	 * postdominators of n.
	 */
	@XCSG_Extension
	public static final String POST_DOMINATOR_TREE_EDGE = "ipdom";
	
	/**
	 * Used to tag the edges from a node that identify the node's dominance
	 * frontier.
	 * 
	 * Wikipedia: The dominance frontier of a node d is the set of all nodes n
	 * such that d dominates an immediate predecessor of n, but d does not
	 * strictly dominate n. It is the set of nodes where d's dominance stops.
	 */
	@XCSG_Extension
	public static final String DOMINANCE_FRONTIER_EDGE = "dom-frontier";
	
	/**
	 * Used to tag the edges from a node that identify the node's post-dominance
	 * frontier.
	 * 
	 * Wikipedia: The dominance frontier of a node d is the set of all nodes n
	 * such that d dominates an immediate predecessor of n, but d does not
	 * strictly dominate n. It is the set of nodes where d's dominance stops.
	 */
	@XCSG_Extension
	public static final String POST_DOMINANCE_FRONTIER_EDGE = "pdom-frontier";
	
	public DominanceAnalysis() {}
	
	public static Q getDominatorTreeEdges(){
		return Common.universe().edges(DOMINATOR_TREE_EDGE).retainEdges();
	}
	
	public static Q getPostDominatorTreeEdges(){
		return Common.universe().edges(POST_DOMINATOR_TREE_EDGE).retainEdges();
	}
	
	public static Q getDominanceFrontierEdges(){
		return Common.universe().edges(DOMINANCE_FRONTIER_EDGE).retainEdges();
	}
	
	public static Q getPostDominanceFrontierEdges(){
		return Common.universe().edges(POST_DOMINANCE_FRONTIER_EDGE).retainEdges();
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
	 * Returns the immediate dominator tree
	 * 
	 * @param ucfg
	 * @return
	 */
	public static SandboxGraph computeSandboxedDominatorTree(Sandbox sandbox, UniqueEntryExitGraph ucfg){
		SandboxHashSet<SandboxEdge> treeEdges = new SandboxHashSet<SandboxEdge>(sandbox);
		SandboxGraph dominance = computeSandboxedDominance(sandbox, ucfg);
		for(SandboxEdge edge : dominance.edges()){
			if(edge.taggedWith(DOMINATOR_TREE_EDGE)){
				treeEdges.add(edge);
			}
		}
		return sandbox.toGraph(treeEdges);
	}
	
	/**
	 * Returns the post dominator tree
	 * 
	 * @param ucfg
	 * @return
	 */
	public static SandboxGraph computeSandboxedPostDominatorTree(Sandbox sandbox, UniqueEntryExitGraph ucfg){
		SandboxHashSet<SandboxEdge> treeEdges = new SandboxHashSet<SandboxEdge>(sandbox);
		SandboxGraph dominance = computeSandboxedDominance(sandbox, ucfg);
		for(SandboxEdge edge : dominance.edges()){
			if(edge.taggedWith(POST_DOMINATOR_TREE_EDGE)){
				treeEdges.add(edge);
			}
		}
		return sandbox.toGraph(treeEdges);
	}
	
	/**
	 * Returns the dominance frontier (each edge represents a frontier node for
	 * a given from node)
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
	 * Returns the post-dominance frontier (each edge represents a frontier node for
	 * a given from node)
	 * 
	 * @param ucfg
	 * @return
	 */
	public static SandboxGraph computeSandboxedPostDominanceFrontier(Sandbox sandbox, UniqueEntryExitGraph ucfg){
		SandboxHashSet<SandboxEdge> frontierEdges = new SandboxHashSet<SandboxEdge>(sandbox);
		SandboxGraph dominance = computeSandboxedDominance(sandbox, ucfg);
		for(SandboxEdge edge : dominance.edges()){
			if(edge.taggedWith(POST_DOMINANCE_FRONTIER_EDGE)){
				frontierEdges.add(edge);
			}
		}
		return sandbox.toGraph(frontierEdges);
	}

	/**
	 * Returns a graph of all dominance relationship edges
	 * @param ucfg
	 * @return
	 */
	public static SandboxGraph computeSandboxedDominance(Sandbox sandbox, UniqueEntryExitGraph ucfg) {
		SandboxHashSet<SandboxEdge> dominanceEdges = new SandboxHashSet<SandboxEdge>(sandbox);
		
		// compute the immediate dominator tree (idom)
		DominatorTree dominatorTree = new DominatorTree(ucfg);
		for(Entry<Node,Node> entry : dominatorTree.getIdoms().entrySet()){
			Node fromNode = entry.getKey();
			SandboxNode sandboxFromNode = sandbox.addNode(fromNode);
			Node toNode = entry.getValue();
			SandboxNode sandboxToNode = sandbox.addNode(toNode);
			SandboxGraph forwardDominanceEdges = sandbox.toGraph(sandbox.universe().edges(DOMINATOR_TREE_EDGE));
			SandboxEdge forwardDominanceEdge = forwardDominanceEdges.betweenStep(sandboxFromNode, sandboxToNode).edges().one();
			if(forwardDominanceEdge == null){
				forwardDominanceEdge = sandbox.createEdge(sandboxFromNode, sandboxToNode);
				forwardDominanceEdge.tag(DOMINATOR_TREE_EDGE);
				forwardDominanceEdge.putAttr(XCSG.name, DOMINATOR_TREE_EDGE);
			}
			dominanceEdges.add(forwardDominanceEdge);
		}
		
		// compute the dominance frontier
		DominatorTree.Multimap<Node> dominanceFrontier = dominatorTree.getDominanceFrontiers();
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
		
		// compute the post-dominator tree (postdom)
		PostDominatorTree postDominatorTree = new PostDominatorTree(ucfg);
		for(Entry<Node,Node> entry : postDominatorTree.getIdoms().entrySet()){
			Node fromNode = entry.getValue();
			SandboxNode sandboxFromNode = sandbox.addNode(fromNode);
			Node toNode = entry.getKey();
			SandboxNode sandboxToNode = sandbox.addNode(toNode);
			SandboxGraph forwardDominanceEdges = sandbox.toGraph(sandbox.universe().edges(POST_DOMINATOR_TREE_EDGE));
			SandboxEdge postdomEdge = forwardDominanceEdges.betweenStep(sandboxFromNode, sandboxToNode).edges().one();
			if(postdomEdge == null){
				postdomEdge = sandbox.createEdge(sandboxFromNode, sandboxToNode);
				postdomEdge.tag(POST_DOMINATOR_TREE_EDGE);
				postdomEdge.putAttr(XCSG.name, POST_DOMINATOR_TREE_EDGE);
			}
			dominanceEdges.add(postdomEdge);
		}
		
		// compute the post-dominance frontier
		PostDominatorTree.Multimap<Node> postDominanceFrontier = postDominatorTree.getDominanceFrontiers();
		for(Entry<Node, Set<Node>> entry : postDominanceFrontier.entrySet()){
			Node toNode = entry.getKey();
			SandboxNode sandboxToNode = sandbox.addNode(toNode);
			for(Node fromNode : entry.getValue()){
				SandboxNode sandboxFromNode = sandbox.addNode(fromNode);
				SandboxGraph dominanceFrontierEdges = sandbox.toGraph(sandbox.universe().edges(POST_DOMINANCE_FRONTIER_EDGE));
				SandboxEdge dominanceFrontierEdge = dominanceFrontierEdges.betweenStep(sandboxFromNode, sandboxToNode).edges().one();
				if(dominanceFrontierEdge == null){
					dominanceFrontierEdge = sandbox.createEdge(sandboxFromNode, sandboxToNode);
					dominanceFrontierEdge.tag(POST_DOMINANCE_FRONTIER_EDGE);
					dominanceFrontierEdge.putAttr(XCSG.name, POST_DOMINANCE_FRONTIER_EDGE);
				}
				dominanceEdges.add(dominanceFrontierEdge);
			}
		}
		
		return sandbox.toGraph(dominanceEdges);
	}
	
	/**
	 * Returns the immediate dominance tree
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computeDominanceTree(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> treeEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(DOMINATOR_TREE_EDGE)){
				treeEdges.add(edge);
			}
		}
		return Common.toQ(treeEdges).eval();
	}
	
	/**
	 * Returns the dominance frontier
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
	 * Returns the post dominance graph
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computePostDominanceTree(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> treeEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(POST_DOMINATOR_TREE_EDGE)){
				treeEdges.add(edge);
			}
		}
		return Common.toQ(treeEdges).eval();
	}
	
	/**
	 * Returns the post-dominance frontier
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computePostDominanceFrontier(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> frontierEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(POST_DOMINANCE_FRONTIER_EDGE)){
				frontierEdges.add(edge);
			}
		}
		return Common.toQ(frontierEdges).eval();
	}

	/**
	 * Returns a graph of all dominance relationship edges
	 * @param ucfg
	 * @return
	 */
	public static Graph computeDominance(UniqueEntryExitGraph ucfg) {
		AtlasSet<Edge> dominanceEdges = new AtlasHashSet<Edge>();
		
		// compute the immediate dominator tree (idom)
		DominatorTree dominatorTree = new DominatorTree(ucfg);
		for(Entry<Node,Node> entry : dominatorTree.getIdoms().entrySet()) {
			Node fromNode = entry.getKey();
			Node toNode = entry.getValue();
			Q idomEdges = Common.universe().edges(DOMINATOR_TREE_EDGE);
			Edge idomEdge = idomEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
			if(idomEdge == null){
				idomEdge = Graph.U.createEdge(fromNode, toNode);
				idomEdge.tag(DOMINATOR_TREE_EDGE);
				idomEdge.putAttr(XCSG.name, DOMINATOR_TREE_EDGE);
			}
			dominanceEdges.add(idomEdge);
		}
		
		// compute the dominance frontier
		DominatorTree.Multimap<Node> dominanceFrontier = dominatorTree.getDominanceFrontiers();
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
		
		// compute the post-dominator tree (postdom)
		PostDominatorTree postDominatorTree = new PostDominatorTree(ucfg);
		for(Entry<Node,Node> entry : postDominatorTree.getIdoms().entrySet()) {
			Node fromNode = entry.getValue();
			Node toNode = entry.getKey();
			Q postdomEdges = Common.universe().edges(POST_DOMINATOR_TREE_EDGE);
			Edge postdomEdge = postdomEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
			if(postdomEdge == null){
				postdomEdge = Graph.U.createEdge(fromNode, toNode);
				postdomEdge.tag(POST_DOMINATOR_TREE_EDGE);
				postdomEdge.putAttr(XCSG.name, POST_DOMINATOR_TREE_EDGE);
			}
			dominanceEdges.add(postdomEdge);
		}
		
		// compute the post-dominance frontier
		PostDominatorTree.Multimap<Node> postDominanceFrontier = postDominatorTree.getDominanceFrontiers();
		for(Entry<Node, Set<Node>> entry : postDominanceFrontier.entrySet()){
			Node toNode = entry.getKey();
			for(Node fromNode : entry.getValue()){
				Q dominanceFrontierEdges = Common.universe().edges(POST_DOMINANCE_FRONTIER_EDGE);
				Edge dominanceFrontierEdge = dominanceFrontierEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
				if(dominanceFrontierEdge == null){
					dominanceFrontierEdge = Graph.U.createEdge(fromNode, toNode);
					dominanceFrontierEdge.tag(POST_DOMINANCE_FRONTIER_EDGE);
					dominanceFrontierEdge.putAttr(XCSG.name, POST_DOMINANCE_FRONTIER_EDGE);
				}
				dominanceEdges.add(dominanceFrontierEdge);
			}
		}
		
		return Common.toQ(dominanceEdges).eval();
	}

}
