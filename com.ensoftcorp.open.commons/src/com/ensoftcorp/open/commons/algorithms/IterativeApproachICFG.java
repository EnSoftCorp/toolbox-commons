package com.ensoftcorp.open.commons.algorithms;

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
 * @author Payas Awadhutkar, Sharwan Ram
 */

public class IterativeApproachICFG {
	private AtlasSet<Node> cg;
	private AtlasSet<Node> expendableCFNodes;
	private String ICFGEdge = "InterproceduralControlFlow_Edge";
	private long PreID = 0L;
	private long Counter = 0L;
	private AtlasSet<GraphElement> icfgElements;
	private Q successorsToConnect;
	private AtlasSet<Edge> edgesToRemove;

	public IterativeApproachICFG() {
		cg =null;
		successorsToConnect = Common.empty();
		icfgElements = new AtlasHashSet<GraphElement>();
		edgesToRemove = new AtlasHashSet<Edge>();
	}

	// store all the control flow nodes in expendableCFNodes which have callsites
	// and are expendable
	public void getExpendableCFNode(Q functions, Q functionsToExpand) {
		expendableCFNodes = new AtlasHashSet<Node>();
		if (cg == null) {
			cg = Query.universe().edges(XCSG.Call).forward(functions).eval().nodes();
		}
		if (cg.size() == 1)
			return;
		AtlasSet<Node> as = new AtlasHashSet<Node>();
		if (!CommonQueries.isEmpty(functionsToExpand))
			as.addAll(functionsToExpand.eval().nodes());
		else {
			as.addAll(cg);
		}
		for (Node n : as) {
			AtlasSet<Node> callSites = Query.universe().edges(XCSG.InvokedFunction).predecessors(Common.toQ(n)).eval()
					.nodes();
			for (Node m : callSites) {
				if (as.contains(CommonQueries.getContainingFunction(m))) {
					Node cfgNode = Common.toQ(m).parent().eval().nodes().one();
					expendableCFNodes.add(cfgNode);
				}
			}
		}
	}

	// Find successors of the expendableCFNodes and connect it's callsite's to the
	// root of it's cfg and
	// connect it's cfg's leaves to the successors of the expendableCFNodes
	public Q getICFG() {
		if (expendableCFNodes == null)
			return CommonQueries.cfg(cg.getFirst());
		for (Node n : expendableCFNodes) {
			Q currentNodeQ = Common.toQ(n);
			Q callsites = getContainingCallSites(currentNodeQ);
			if (callsites.eval().nodes().size() == 1) {
				AtlasSet<Node> targets = CallSiteAnalysis.getTargets(callsites).eval().nodes();
				if (targets.size() == 1) {
					successorsToConnect = CommonQueries.cfg(targets.getFirst()).roots();
				}
				connectSuccessors(Common.toQ(n));
			}

		}

		icfgElements.addAll(Query.universe().edges(XCSG.ControlFlowBackEdge).eval().edges());
		for (Node n : cg) {
			icfgElements.addAll(CommonQueries.cfg(n).contained().nodes(XCSG.ControlFlow_Node).eval().nodes());
			for (Edge e : CommonQueries.cfg(n).edges(XCSG.ControlFlow_Edge).eval().edges()) {
				if (!icfgElements.contains(e))
					icfgElements.add(e);
			}
		}
		for (Edge e : edgesToRemove) {
			icfgElements.remove(e);
		}
		return Common.toQ(icfgElements);

	}

	private void connectSuccessors(Q currentNodeQ) {
		//cfg of the expendable node
		Q cfg = CommonQueries.cfg(CommonQueries.getContainingFunctions(successorsToConnect));
		//cfg of the functions which contain expendable node
		Q cfgRoot = CommonQueries.cfg(CommonQueries.getContainingFunctions(currentNodeQ));
		//connect expendable node to it's cfg's root
		connectEdges(currentNodeQ.eval().nodes().one(), successorsToConnect.eval().nodes().one(), false);
		//connect leaves of the expendable node's cfg to successors of it
		AtlasSet<Node> successorsNodes = cfgRoot.successors(currentNodeQ).eval().nodes();
		Q subgraph = cfgRoot.between(currentNodeQ, Common.toQ(successorsNodes));
		edgesToRemove.addAll(subgraph.eval().edges());
		for (Node l : cfg.leaves().eval().nodes()) {
			for (Node successor : successorsNodes) {
				connectEdges(l, successor, true);
			}
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
				this.PreID = Counter++;
				e.putAttr(XCSG.name, "CallID " + this.PreID);
				e.tag("CallID");
				e.tag("CallID" + this.PreID);
			}
			e.tag(ICFGEdge);
			e.tag(XCSG.ControlFlow_Edge);
			icfgElements.add(e);
			// Log.info("Edge added : " + u.getAttr(XCSG.name) + " -> " +
			// v.getAttr(XCSG.name));
		} else {
			for (Edge betweenEdge : betweenEdges) {
				icfgElements.add(betweenEdge);
			}
		}

	}

	public static Q geticfg(Q functions) {
		IterativeApproachICFG icfg = new IterativeApproachICFG();
		//icfg.getRecursionCFNodes();
		icfg.getExpendableCFNode(functions, Common.empty());
		return icfg.getICFG();
	}

	public static Q geticfg(Node function) {
		return geticfg(Common.toQ(function));
	}

	public static Q geticfg(Q functions, Q functionsToExpand) {
		IterativeApproachICFG icfg = new IterativeApproachICFG();
		//icfg.getRecursionCFNodes();
		icfg.getExpendableCFNode(functions, functionsToExpand);
		return icfg.getICFG();
	}

	public static boolean isCallSite(Q cfNode, Q functionContext) {
		Q callsites = getContainingCallSites(cfNode);
		if (CommonQueries.isEmpty(callsites)) {
			return false;
		}
		if (!CommonQueries.isEmpty(functionContext)) {
			Q targets = CallSiteAnalysis.getTargets(callsites);
			if (CommonQueries.isEmpty(targets.intersection(functionContext))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isCallSite(Q cfNode) {
		return isCallSite(cfNode, Common.empty());
	}

	public static Q getContainingCallSites(Q cfNode) {
		return cfNode.children().nodes(XCSG.CallSite);
	}

}