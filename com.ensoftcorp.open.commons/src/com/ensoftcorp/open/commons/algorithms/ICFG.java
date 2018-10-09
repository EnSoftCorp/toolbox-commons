package com.ensoftcorp.open.commons.algorithms;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.log.Log;

public class ICFG {
	
	private static String ICFGEdge = "InterproceduralControlFlow_Edge";
	
	private Q rootFunction;
	private AtlasSet<GraphElement> icfgElements;
	private List<Pair<Node,Node>> icfgNodePairs;
	private Q predecessorsToConnect;
	private Q successorsToConnect;
	private Queue<Node> nodesToProcess;
	private List<Node> processedNodes;
	private Q expandables;
	
	public ICFG(Q function, Q functionsToExpand) {
		rootFunction = function;
		icfgElements = new AtlasHashSet<GraphElement>();
		icfgNodePairs = new ArrayList<Pair<Node,Node>>();
		predecessorsToConnect = Common.empty();
		successorsToConnect = Common.empty();
		nodesToProcess = new LinkedList<Node>();
		processedNodes = new ArrayList<Node>();
		expandables = functionsToExpand;		
	}
	
	private void computeICFGElements() {
		Q rootCfg = CommonQueries.cfg(rootFunction);
		if(CommonQueries.isEmpty(rootCfg)) {
			return;
		}
		Q root = rootCfg.nodes(XCSG.controlFlowRoot);
		nodesToProcess.add(root.eval().nodes().one());
		while(nodesToProcess.peek() != null) {
			Node currentNode = nodesToProcess.poll();
			Q currentNodeQ = Common.toQ(currentNode);
			Q successors = rootCfg.successors(currentNodeQ);
			if(!CommonQueries.isCallSite(currentNodeQ,this.expandables)) {
				successorsToConnect = successors;
				connectPredecessors(currentNode);
				connectSuccessors(currentNode);
				predecessorsToConnect = currentNodeQ;
			} else {
				Q callsiteicfg = processCallSite(currentNodeQ);
				successorsToConnect = callsiteicfg.roots();
				connectPredecessors(currentNode);
				connectSuccessors(currentNode);
				predecessorsToConnect = callsiteicfg.leaves();
			}
			processedNodes.add(currentNode);
			for(Node successor : successors.eval().nodes()) {
				nodesToProcess.add(successor);
			}
		}
		
	}
	
	private void connectEdges() {
		for(Pair<Node,Node> nodePair : icfgNodePairs) {
			Node u = nodePair.getKey();
			Node v = nodePair.getValue();
			Q betweenControlFlow = Common.universe().edges(XCSG.ControlFlow_Edge).betweenStep(Common.toQ(u), Common.toQ(v));
			AtlasSet<Edge> betweenEdges = betweenControlFlow.eval().edges();
			if(betweenEdges.size() == 0) {
				Edge e = Graph.U.createEdge(u, v);
				e.tag(ICFGEdge);
				e.tag(XCSG.ControlFlow_Edge);
				icfgElements.add(e);
				Log.info("Edge added : " + u.getAttr(XCSG.name) + " -> " + v.getAttr(XCSG.name));
			} else {
				for(Edge betweenEdge : betweenEdges) {
					icfgElements.add(betweenEdge);
				}
			}
			icfgElements.add(u);
			icfgElements.add(v);
		}
	}
	
	private void connectPredecessors(Node currentNode) {
		if(!CommonQueries.isEmpty(predecessorsToConnect)) {
			for(Node predecessor : predecessorsToConnect.eval().nodes()) {
				this.icfgNodePairs.add(new ImmutablePair<Node,Node>(predecessor,currentNode));
			}
		}
	}
	
	private void connectSuccessors(Node currentNode) {
		if(!CommonQueries.isEmpty(successorsToConnect)) {
			for(Node successor : successorsToConnect.eval().nodes()) {
				this.icfgNodePairs.add(new ImmutablePair<Node,Node>(currentNode,successor));
			}
		}
	}
	
	private Q processCallSite(Q callsite) {
		Q targetIcfg = Common.empty();
		Q callsites = CommonQueries.getContainingCallSites(callsite);
		if (callsites.eval().nodes().size() == 1) {
			AtlasSet<Node> targets = CallSiteAnalysis.getTargets(callsites).eval().nodes();
			for (Node target : targets) {
				Q targetQ = Common.toQ(target);
				ICFG icfg = new ICFG(targetQ, this.expandables);
				targetIcfg = targetIcfg.union(icfg.getICFG());
				this.icfgNodePairs.addAll(icfg.getIcfgNodePairs());
			}
		} else {
			// Handle multiple callsites
		}
		return targetIcfg;
	}

	public Q getICFG() {
		computeICFGElements();
		connectEdges();
		return Common.toQ(icfgElements);
	}
	
	public List<Pair<Node,Node>> getIcfgNodePairs() {
		return icfgNodePairs;
	}
}