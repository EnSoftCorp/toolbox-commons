package com.ensoftcorp.open.commons.algorithms;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.analyzers.RecursiveFunctions;

/**
 * 
 * Interprocedural Control Flow Graph
 * 
 * @author Payas Awadhutkar, Sharwan Ram
 */

public class InterproceduralControlFlowGraph {
    static boolean flag = true;
	public static void getRecursionCFNodes() {
		//find strongly connected component to detect recursion
		Q SCC = RecursiveFunctions.getRecursiveMethods();
		Map<Node, Node> recursiveFunction = new HashMap<Node, Node>();
		AtlasSet<Node> excludeRecursive = new AtlasHashSet<Node>();
		AtlasSet<Edge> edges = SCC.eval().edges();
		for (Edge e : edges) {
			if (!e.taggedWith(XCSG.C.Provisional.NominalCall)) {
				recursiveFunction.put(e.from(), e.to());
			}
		}

		for (Map.Entry<Node, Node> entry : recursiveFunction.entrySet()) {
			Node from = entry.getKey();

			Node to = entry.getValue();
			Q allCallSite = CommonQueries.cfg(from).contained().nodes(XCSG.CallSite);
			for (Node n : allCallSite.eval().nodes()) {
				Node target = Common.toQ(n).parent().eval().nodes().one();
				AtlasSet<Node> targetFunction = Query.universe().edges(XCSG.InvokedFunction).successors(Common.toQ(n))
						.eval().nodes();
				for (Node nd : targetFunction) {
					if (to.equals(nd)) {
						excludeRecursive.add(target);
					}
				}

			}
		}
		ICFG.excludeRecursive = excludeRecursive;
	}

	public static Q icfg(Q functions) {
		getRecursionCFNodes();
		flag=false;
		return icfg(functions, Common.empty());
	}

	public static Q icfg(Node function) {
		if(flag) {
		getRecursionCFNodes();
		flag =false;
		}
		return icfg(Common.toQ(function));
	}

	public static Q icfg(Q functions, Q functionsToExpand) {
		if(flag) {
			getRecursionCFNodes();
			flag =false;
		}
		ICFG icfg = new ICFG(functions, functionsToExpand);
		return icfg.getICFG();
	}

}