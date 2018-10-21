package com.ensoftcorp.open.commons.algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.xcsg.XCSG_Extension;

public class ICFG {

	@XCSG_Extension
	public static String ICFGEdge = "ICFG.InterproceduralControlFlow_Edge";

	@XCSG_Extension
	public static String ICFGEntryEdge = "ICFG.InterproceduralControlFlowEntry_Edge";
	
	@XCSG_Extension
	public static String ICFGExitEdge = "ICFG.InterproceduralControlFlowExit_Edge";
	
	@XCSG_Extension
	public static String ICFGCallsiteAttribute = "ICFG.CallSite";
	
	private Node entryPointFunction;
	private AtlasSet<GraphElement> icfgElements;
	private List<CallSitePair> icfgNodePairs;
	private AtlasSet<Node> predecessorsToConnect;
	private AtlasSet<Node> successorsToConnect;
	private Queue<Node> nodesToProcess;
	private List<Node> processedNodes;
	private AtlasSet<Node> functionsToExpand;
	private AtlasSet<Edge> backEdges;
	private Q dag;
	private Node callsite;
	
	private long callsiteCounter = 0;
	
	private abstract static class CallSitePair {
		private Node predecessor;
		private Node successor;
		private Node callsite;
		private long callsiteCounter;
		
		public CallSitePair(Node predecessor, Node successor, Node callsite, long callsiteCounter) {
			this.predecessor = predecessor;
			this.successor = successor;
			this.callsite = callsite;
			this.callsiteCounter = callsiteCounter;
		}

		public Node getPredecessor() {
			return predecessor;
		}

		public Node getSuccessor() {
			return successor;
		}

		public Node getCallsite() {
			return callsite;
		}
		
		public long getCallsiteCounter() {
			return callsiteCounter;
		}
		
		public abstract boolean isEntry();
		
		public boolean isExit() {
			return !isEntry();
		}
	}
	
	private static class CallSiteEntryPair extends CallSitePair {
		public CallSiteEntryPair(Node predecessor, Node successor, Node callsite, long callsiteCounter) {
			super(predecessor, successor, callsite, callsiteCounter);
		}

		@Override
		public boolean isEntry() {
			return true;
		}
	}
	
	private static class CallSiteExitPair extends CallSitePair {
		public CallSiteExitPair(Node predecessor, Node successor, Node callsite, long callsiteCounter) {
			super(predecessor, successor, callsite, callsiteCounter);
		}

		@Override
		public boolean isEntry() {
			return false;
		}
	}

	private static boolean isCallSite(Node cfNode, Q functionsToExpand) {
		AtlasSet<Node> callsites = getContainingCallSites(cfNode);
		if (callsites.isEmpty()) {
			return false;
		}
		if (!CommonQueries.isEmpty(functionsToExpand)) {
			AtlasSet<Node> targets = CallSiteAnalysis.getTargets(callsites);
			if (CommonQueries.isEmpty(Common.toQ(targets).intersection(functionsToExpand))) {
				return false;
			}
		}
		return true;
	}

	private static AtlasSet<Node> getContainingCallSites(Node cfNode) {
		return Common.toQ(cfNode).children().nodes(XCSG.CallSite).eval().nodes();
	}
	
	public ICFG(Node entryPointFunction, AtlasSet<Node> functionsToExpand, long callsiteCounter) {
		this(entryPointFunction, functionsToExpand);
		this.callsiteCounter = callsiteCounter;
	}

	public ICFG(Node entryPointFunction, AtlasSet<Node> functionsToExpand) {
		this.entryPointFunction = entryPointFunction;
		this.backEdges = CommonQueries.cfg(entryPointFunction).edges(XCSG.ControlFlowBackEdge).eval().edges();
		this.icfgElements = new AtlasHashSet<GraphElement>();
		this.icfgNodePairs = new ArrayList<CallSitePair>();
		this.predecessorsToConnect = new AtlasHashSet<Node>();
		this.successorsToConnect = new AtlasHashSet<Node>();
		this.nodesToProcess = new LinkedList<Node>();
		this.processedNodes = new ArrayList<Node>();
		this.functionsToExpand = functionsToExpand;
		this.dag = CommonQueries.cfg(entryPointFunction).differenceEdges(Common.toQ(backEdges));
	}

	private void computeICFGElements() {
		Q rootCFG = CommonQueries.cfg(entryPointFunction);
		rootCFG = rootCFG.differenceEdges(Common.toQ(backEdges));
		if (CommonQueries.isEmpty(rootCFG)) {
			return;
		}
		
		Q root = rootCFG.nodes(XCSG.controlFlowRoot);
		nodesToProcess.add(root.eval().nodes().one());
		while (!nodesToProcess.isEmpty()) {
			Node currentNode = nodesToProcess.poll();
			if(!processedNodes.contains(currentNode)) { // why don't we do this or use a sorted set? ~BH we should only need to visit each node once, otherwise its an exponential cost
				Q successors = rootCFG.successors(Common.toQ(currentNode));
				boolean isCallsite = isCallSite(currentNode, Common.toQ(this.functionsToExpand));
				if (!isCallsite) {
					successorsToConnect = new AtlasHashSet<Node>(successors.eval().nodes());
					findPredecessors(currentNode);
					connectPredecessors(currentNode, callsiteCounter);
					connectSuccessors(currentNode, callsiteCounter-1);
				} else {
					callsiteCounter++;
					Node callsiteCFNode = currentNode;
					Q callsiteICFG = processCallSite(callsiteCFNode);
					successorsToConnect = new AtlasHashSet<Node>(callsiteICFG.roots().eval().nodes());
					findPredecessors(callsiteCFNode);
					connectPredecessors(callsiteCFNode, callsiteCounter-1);
					connectSuccessors(callsiteCFNode, callsiteCounter);
				}
				processedNodes.add(currentNode);
				for (Node successor : successors.eval().nodes()) {
					Q predessorOfSuccessor = rootCFG.predecessors(Common.toQ(successor));
					// processing constraints ensure that the child nodes are processed in the
					// correct order
					boolean correctOrder = true;
					for (Node n : predessorOfSuccessor.eval().nodes()) {
						if (!processedNodes.contains(n)) {
							correctOrder = false;
							break;
						}
					}
					if (correctOrder) {
						nodesToProcess.add(successor);
					}
				}
			}
		}
	}

	private void findPredecessors(Node currentNode) {
		predecessorsToConnect = new AtlasHashSet<Node>();
		AtlasSet<Node> pd = new AtlasHashSet<Node>();
		pd = dag.predecessors(Common.toQ(currentNode)).eval().nodes();
		for (Node n : pd) {
			if (!isCallSite(n, Common.toQ(this.functionsToExpand))) {
				predecessorsToConnect.add(n);
			} else {
				AtlasSet<Node> callsites = getContainingCallSites(n);
				if (callsites.size() == 1) {
					AtlasSet<Node> targets = CallSiteAnalysis.getTargets(callsites);
					for (Node target : targets) {
						Q targetCFG = CommonQueries.cfg(target);
						predecessorsToConnect.addAll(targetCFG.leaves().eval().nodes());
					}
				}
			}
		}
	}

	private void connectNodes() {
		for (CallSitePair callsitePair : icfgNodePairs) {
			Node u = callsitePair.getPredecessor();
			Node v = callsitePair.getSuccessor();
			Q betweenControlFlow = Query.universe().edges(XCSG.ControlFlow_Edge, ICFGEdge).betweenStep(Common.toQ(u), Common.toQ(v));
			AtlasSet<Edge> betweenEdges = betweenControlFlow.eval().edges();
			if (betweenEdges.size() == 0) {
				Edge e = Graph.U.createEdge(u, v);
				e.tag(ICFGEdge);
				e.tag(XCSG.ControlFlow_Edge);
				if(callsitePair.isEntry()) {
					e.tag(ICFGEntryEdge);
				}
				if(callsitePair.isExit()) {
					e.tag(ICFGExitEdge);
				}
				if(callsitePair.getCallsite() != null) {
					// we could make a correspondence to the atlas dataflow callsite id, but it appears to be deprecated
//					String callsiteID = callsitePair.getCallsite().getAttr(Attr.Node.CALL_SITE_ID).toString();
					e.putAttr(ICFGCallsiteAttribute, callsitePair.getCallsiteCounter());
					e.putAttr(XCSG.name, "CID_" + callsitePair.getCallsiteCounter());
				}
				icfgElements.add(e);
				// Log.info("Edge added : " + u.getAttr(XCSG.name) + " -> " + v.getAttr(XCSG.name));
			} else {
				for (Edge betweenEdge : betweenEdges) {
					icfgElements.add(betweenEdge);
				}
			}
			icfgElements.add(u);
			icfgElements.add(v);
		}
	}

	private void connectPredecessors(Node currentNode, long callsiteCounter) {
		if (!predecessorsToConnect.isEmpty()) {
			for (Node predecessor : predecessorsToConnect) {
				if(predecessor.taggedWith(XCSG.controlFlowExitPoint)) {
					this.icfgNodePairs.add(new CallSiteExitPair(predecessor, currentNode, callsite, callsiteCounter));
				} else {
					this.icfgNodePairs.add(new CallSiteEntryPair(predecessor, currentNode, callsite, callsiteCounter));
				}
			}
		}
	}

	private void connectSuccessors(Node currentNode, long callsiteCounter) {
		if (!successorsToConnect.isEmpty()) {
			for (Node successor : successorsToConnect) {
				if(currentNode.taggedWith(XCSG.controlFlowExitPoint)) {
					this.icfgNodePairs.add(new CallSiteExitPair(currentNode, successor, callsite, callsiteCounter));
				} else {
					this.icfgNodePairs.add(new CallSiteEntryPair(currentNode, successor, callsite, callsiteCounter));
				}
			}
		}
	}

	private Q processCallSite(Node callsiteCFNode) {
		Q targetICFG = Common.empty();
		AtlasSet<Node> callsites = getContainingCallSites(callsiteCFNode);
		if (callsites.size() == 1) {
			callsite = callsites.one();
			AtlasSet<Node> targets = CallSiteAnalysis.getTargets(callsite);
			for (Node target : targets) {
				ICFG icfg = new ICFG(target, this.functionsToExpand, this.callsiteCounter);
				targetICFG = targetICFG.union(icfg.getICFG());
				this.icfgNodePairs.addAll(icfg.getICFGNodePairs());
			}
		} else {
			// TODO: handle multiple callsites
			Log.warning("Multiple callsites are not handled at this time.");
		}
		return targetICFG;
	}

	public Q getICFG() {
		computeICFGElements();
		connectNodes();
		icfgElements.addAll(backEdges);
		return Common.toQ(icfgElements);
	}

	public List<CallSitePair> getICFGNodePairs() {
		return icfgNodePairs;
	}

}