package com.ensoftcorp.open.commons.algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/**
 * 
 * Interprocedural Control Flow Graph
 * 
 * @author Payas Awadhutkar
 */
public class InterproceduralControlFlowGraph {
	
	private static final String ICFGEdge = "InterproceduralControlFlow_Edge";
	private static Q controlFlow = Query.universe().edges(XCSG.ControlFlow_Edge,ICFGEdge);
	
	/**
	 * 
	 * @param functions
	 * @return the interprocedural control flow graph under the function
	 */	
	public static Q icfg(Q functions) {
		Q cfg = CommonQueries.cfg(functions);
		Q icfg = Common.empty();
		if(CommonQueries.isEmpty(cfg)) {
			return icfg;
		}
		AtlasSet<Node> icfgNodes = new AtlasHashSet<Node>();
		AtlasSet<Edge> icfgEdges = new AtlasHashSet<Edge>();
		Queue<Node> nodesToProcess = new LinkedList<Node>();
		ArrayList<Node> processedNodes = new ArrayList<Node>();
		nodesToProcess.add(cfg.roots().eval().nodes().one());
		Q predecessorToConnect = Common.empty();
		while(nodesToProcess.peek() != null) {
			Node currentNode = nodesToProcess.poll();
			Q currentNodeQ = Common.toQ(currentNode);
			Q successors = cfg.successors(currentNodeQ);
			if(!CommonQueries.isCallSite(currentNodeQ)) {
				icfgNodes.add(currentNode);
				if(!CommonQueries.isEmpty(predecessorToConnect)) {
					for(Node predecessorNodeToConnect : predecessorToConnect.eval().nodes()) {
						if(!icfgEdgeExists(predecessorNodeToConnect,currentNode)) {
							Edge e = Graph.U.createEdge(predecessorNodeToConnect, currentNode);
							icfgEdges.add(e);
							e.tag(ICFGEdge);
							e.tag(XCSG.ControlFlow_Edge);
						}
					}
					predecessorToConnect = Common.empty();
				}
				for(Node successorNode : successors.eval().nodes()) {
					Q successorNodeQ = Common.toQ(successorNode);
					if(!CommonQueries.isCallSite(successorNodeQ)) {
						Edge e = cfg.betweenStep(currentNodeQ, successorNodeQ).eval().edges().one();
						icfgEdges.add(e);
						icfgNodes.add(successorNode);
						if(!processedNodes.contains(successorNode)) {
							nodesToProcess.add(successorNode);
						}
					} else {
						Q successoricfg = processCallSite(successorNodeQ);
						icfgNodes.addAll(successoricfg.eval().nodes());
						icfgEdges.addAll(successoricfg.eval().edges());
						predecessorToConnect = successoricfg.leaves();
						Node successoricfgrootNode = successoricfg.roots().eval().nodes().one();
						if(!icfgEdgeExists(currentNode,successoricfgrootNode)) {
							Edge e = Graph.U.createEdge(currentNode, successoricfgrootNode);
							icfgEdges.add(e);
							e.tag(ICFGEdge);
							e.tag(XCSG.ControlFlow_Edge);
						}
						Q nextSuccessors = cfg.successors(successorNodeQ);
						for(Node nextSuccessorNode : nextSuccessors.eval().nodes()) {
							if(!processedNodes.contains(nextSuccessorNode)) {
								nodesToProcess.add(nextSuccessorNode);
							}
						}
					}
				}
			}
			else {
				Q callsiteicfg = processCallSite(currentNodeQ);
				icfgNodes.addAll(callsiteicfg.eval().nodes());
				icfgEdges.addAll(callsiteicfg.eval().edges());
				Q callsiteicfgroots = callsiteicfg.roots();
				AtlasSet<Node> callsiteicfgleaves = callsiteicfg.leaves().eval().nodes();
				if(!CommonQueries.isEmpty(predecessorToConnect)) {
					for(Node predecessorNodeToConnect : predecessorToConnect.eval().nodes()) {
						for(Node root : callsiteicfgroots.eval().nodes()) {
							if(!icfgEdgeExists(predecessorNodeToConnect, root)) {
								Edge e = Graph.U.createEdge(predecessorNodeToConnect, root);
								icfgEdges.add(e);
								e.tag(ICFGEdge);
								e.tag(XCSG.ControlFlow_Edge);
							}
						}
					}
					predecessorToConnect = Common.empty();
				}
				for(Node successorNode : successors.eval().nodes()) {
					Q successorNodeQ = Common.toQ(successorNode);
					if(!CommonQueries.isCallSite(successorNodeQ)) {
						for(Node leaf : callsiteicfgleaves) {
							if(!icfgEdgeExists(leaf,successorNode)) {
								Edge e = Graph.U.createEdge(leaf, successorNode);
								icfgEdges.add(e);
								e.tag(ICFGEdge);
								e.tag(XCSG.ControlFlow_Edge);
							}
						} 
						if(!processedNodes.contains(successorNode)) {
							nodesToProcess.add(successorNode);
						}
					} else {
						Q successoricfg = processCallSite(successorNodeQ);
						icfgNodes.addAll(successoricfg.eval().nodes());
						icfgEdges.addAll(successoricfg.eval().edges());
						predecessorToConnect = successoricfg.leaves();
						Node successoricfgrootNode = successoricfg.roots().eval().nodes().one();
						for(Node leaf : callsiteicfgleaves) {
							if(!icfgEdgeExists(leaf,successoricfgrootNode)) {
								Edge e = Graph.U.createEdge(leaf, successoricfgrootNode);
								icfgEdges.add(e);
								e.tag(ICFGEdge);
								e.tag(XCSG.ControlFlow_Edge);
							}
						}
						Q nextSuccessors = cfg.successors(successorNodeQ);
						for(Node nextSuccessorNode : nextSuccessors.eval().nodes()) {
							if(!processedNodes.contains(nextSuccessorNode)) {
								nodesToProcess.add(nextSuccessorNode);
							}
						}
					}
				}
			}
			processedNodes.add(currentNode);
		}
		AtlasSet<GraphElement> icfgElements = new AtlasHashSet<GraphElement>();
		icfgElements.addAll(icfgNodes);
		icfgElements.addAll(icfgEdges);
		icfg = Common.toQ(icfgElements);
		return icfg.induce(controlFlow);
	}
	
	/**
	 * 
	 * @param function
	 * @return the control flow graph under the function
	 */	
	public static Q icfg(Node function) {
		return icfg(Common.toQ(function));
	}
	
	private static Q processCallSite(Q callsite) {
		Q callsiteicfg = Common.empty();
		Q callsites = CommonQueries.getContainingCallSites(callsite);
		if(callsites.eval().nodes().size() == 1){
			AtlasSet<Node> targets = CallSiteAnalysis.getTargets(callsites).eval().nodes();
			for(Node target : targets) {
				Q targetQ = Common.toQ(target);
				Q targeticfg = icfg(targetQ);
				callsiteicfg = callsiteicfg.union(targeticfg);
			}
		} else {
			// handle multiple callsites
		}
		return callsiteicfg;			
	}
	
	private static boolean icfgEdgeExists(Node a, Node b) {
		Q aQ = Common.toQ(a);
		Q bQ = Common.toQ(b);
		Q betweenGraph = Query.universe().edges(XCSG.ControlFlow_Edge).betweenStep(aQ, bQ);
		if(betweenGraph.eval().edges().size() > 0) {
			return true;
		}
		return false;
	}
	
}
