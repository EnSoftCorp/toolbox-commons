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
	private long PreID = 0L;
	private long Counter = 0L;
	private Q rootFunction;
	private AtlasSet<GraphElement> icfgElements;
	private List<Pair<Node, Node>> icfgNodePairs;
	private Q predecessorsToConnect;
	private Q successorsToConnect;
	private Queue<Node> nodesToProcess;
	private List<Node> processedNodes;
	private Q expandables;
	private AtlasSet<Edge> backedges;
	private Q dag;
	static public AtlasSet<Node> functionTargets = new AtlasHashSet<Node>();
	static private AtlasSet<Node> processedFunctions = new AtlasHashSet<Node>();
	static public AtlasSet<Node> connectedComponents;
	static public Q rtfunction;
	static public AtlasSet<Node> excludeRecursive;

	public ICFG(Q function, Q functionsToExpand) {
		rootFunction = function;
		backedges = CommonQueries.cfg(function).edgesTaggedWithAny(XCSG.ControlFlowBackEdge).eval().edges();
		functionTargets.add(function.eval().nodes().one());
		icfgElements = new AtlasHashSet<GraphElement>();
		icfgNodePairs = new ArrayList<Pair<Node, Node>>();
		predecessorsToConnect = Common.empty();
		successorsToConnect = Common.empty();
		nodesToProcess = new LinkedList<Node>();
		processedNodes = new ArrayList<Node>();
		expandables = functionsToExpand;
		dag = CommonQueries.cfg(rootFunction).differenceEdges(Common.toQ(backedges));

	}

	private void computeICFGElements() {
		Q rootCfg = CommonQueries.cfg(rootFunction);
		rootCfg = rootCfg.differenceEdges(Common.toQ(backedges));
		if (CommonQueries.isEmpty(rootCfg)) {
			return;
		}
		Q root = rootCfg.nodes(XCSG.controlFlowRoot);
		nodesToProcess.add(root.eval().nodes().one());
		while (nodesToProcess.peek() != null) {
			Node currentNode = nodesToProcess.poll();
			Q currentNodeQ = Common.toQ(currentNode);
			Q successors = rootCfg.successors(currentNodeQ);
			if (!CommonQueries.isCallSite(currentNodeQ, this.expandables)) {
				successorsToConnect = successors;
				findPredecessors(currentNode);
				connectPredecessors(currentNode);
				connectSuccessors(currentNode);
			} else {

				Q callsites = CommonQueries.getContainingCallSites(currentNodeQ);
				if (callsites.eval().nodes().size() == 1) {
					if (excludeRecursive.contains(currentNode)) {
						successorsToConnect = successors;
					} else {
						AtlasSet<Node> targets = CallSiteAnalysis.getTargets(callsites).eval().nodes();
						for (Node target : targets) {
							if (processedFunctions.contains(target)) {
								successorsToConnect = CommonQueries.cfg(target).roots();

							} else {
								Q callsiteicfg = processCallSite(target);
								successorsToConnect = callsiteicfg.roots();

							}

						}
					}
					findPredecessors(currentNode);
					connectPredecessors(currentNode);
					connectSuccessors(currentNode);

				}

			}
			processedNodes.add(currentNode);
			for (Node successor : successors.eval().nodes()) {
				Q predessorOfSuccessor = rootCfg.predecessors(Common.toQ(successor));
				// processing constraints ensure that the child nodes are processed in the
				// correct order
				boolean correctOrder = true;
				for (Node n : predessorOfSuccessor.eval().nodes()) {
					if (!processedNodes.contains(n)) {
						correctOrder = false;
						break;
					}
				}
				if (correctOrder && !nodesToProcess.contains(successor))
					nodesToProcess.add(successor);
			}
		}

	}

	private void findPredecessors(Node currentNode) {
		// TODO Auto-generated method stub
		predecessorsToConnect = Common.empty();
		AtlasSet<Node> pd = new AtlasHashSet<Node>();
		pd = dag.predecessors(Common.toQ(currentNode)).eval().nodes();
		for (Node n : pd) {
			Q nQ = Common.toQ(n);
			if (!CommonQueries.isCallSite(nQ, this.expandables)) {
				predecessorsToConnect = predecessorsToConnect.union(nQ);
			} else {

				Q callsites = CommonQueries.getContainingCallSites(nQ);
				if (callsites.eval().nodes().size() == 1) {
					if (excludeRecursive.contains(n)) {
						predecessorsToConnect = predecessorsToConnect.union(nQ);
						;
					} else {
						AtlasSet<Node> targets = CallSiteAnalysis.getTargets(callsites).eval().nodes();
						for (Node target : targets) {
							Q targetCFG = CommonQueries.cfg(target);
							predecessorsToConnect = predecessorsToConnect.union(targetCFG.leaves());

						}

					}
				}

			}
		}

	}

	private void connectEdges() {
		for (Pair<Node, Node> nodePair : icfgNodePairs) {
			Node u = nodePair.getLeft();
			Node v = nodePair.getRight();
			Q betweenControlFlow = Common.universe().edges(XCSG.ControlFlow_Edge).betweenStep(Common.toQ(u),
					Common.toQ(v));
			AtlasSet<Edge> betweenEdges = betweenControlFlow.eval().edges();
			if (betweenEdges.size() == 0) {
				Edge e = Graph.U.createEdge(u, v);
				e.tag(ICFGEdge);
				e.tag(XCSG.ControlFlow_Edge);
				icfgElements.add(e);
				Log.info("Edge added : " + u.getAttr(XCSG.name) + " -> " + v.getAttr(XCSG.name));
			} else {
				for (Edge betweenEdge : betweenEdges) {
					icfgElements.add(betweenEdge);
				}
			}
			icfgElements.add(u);
			icfgElements.add(v);
		}
	}

	private void connectEdges(Node u, Node v, boolean temp) {
		Q betweenControlFlow = Common.universe().edges(XCSG.ControlFlow_Edge).betweenStep(Common.toQ(u), Common.toQ(v));
		AtlasSet<Edge> betweenEdges = betweenControlFlow.eval().edges();
		if (betweenEdges.size() == 0) {
			Edge e = Graph.U.createEdge(u, v);
			if (temp) {
				if (u.taggedWith(XCSG.controlFlowExitPoint)) {
					e.putAttr(XCSG.name, "CallID " + this.PreID);
					e.tag("CallID");
					e.tag("CallID" + this.PreID);

				}

			} else {
				if (CommonQueries.isCallSite(Common.toQ(u))) {
					this.PreID = Counter++;
					e.putAttr(XCSG.name, "CallID " + this.PreID);
					e.tag("CallID");
					e.tag("CallID" + this.PreID);

				}

			}
			e.tag(ICFGEdge);
			e.tag(XCSG.ControlFlow_Edge);
			icfgElements.add(e);
			Log.info("Edge added : " + u.getAttr(XCSG.name) + " -> " + v.getAttr(XCSG.name));
		} else {
			for (Edge betweenEdge : betweenEdges) {
				icfgElements.add(betweenEdge);
			}
		}
		icfgElements.add(u);
		icfgElements.add(v);

	}

	private void connectPredecessors(Node currentNode) {
		if (!CommonQueries.isEmpty(predecessorsToConnect)) {
			for (Node predecessor : predecessorsToConnect.eval().nodes()) {
				this.icfgNodePairs.add(new ImmutablePair<Node, Node>(predecessor, currentNode));
				connectEdges(predecessor, currentNode, true);
			}
		}
	}

	private void connectSuccessors(Node currentNode) {
		if (!CommonQueries.isEmpty(successorsToConnect)) {
			for (Node successor : successorsToConnect.eval().nodes()) {
				this.icfgNodePairs.add(new ImmutablePair<Node, Node>(currentNode, successor));
				connectEdges(currentNode, successor, false);
			}
		}
	}

	private Q processCallSite(Node target) {
		Q targetIcfg = Common.empty();
		Q targetQ = Common.toQ(target);
		ICFG icfg = new ICFG(targetQ, this.expandables);
		targetIcfg = targetIcfg.union(icfg.getICFG());
		this.icfgNodePairs.addAll(icfg.getIcfgNodePairs());

		return targetIcfg;
	}

	public Q getICFG() {
		computeICFGElements();
		connectEdges();
		icfgElements.addAll(backedges);
		return Common.toQ(icfgElements);
	}

	public List<Pair<Node, Node>> getIcfgNodePairs() {
		return icfgNodePairs;
	}

}