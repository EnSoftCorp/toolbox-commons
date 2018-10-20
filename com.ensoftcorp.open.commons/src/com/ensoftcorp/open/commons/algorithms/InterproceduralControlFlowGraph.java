package com.ensoftcorp.open.commons.algorithms;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;

/**
 * 
 * Interprocedural Control Flow Graph
 * 
 * @author Payas Awadhutkar, Sharwan Ram
 */
public class InterproceduralControlFlowGraph {
	
	
	public static Q icfg(Q functions) {
		return icfg(functions,Common.empty());
	}
	
	public static Q icfg(Node function) {
		return icfg(Common.toQ(function));
	}
	
	public static Q icfg(Q functions, Q functionsToExpand) {
		ICFG icfg = new ICFG(functions,functionsToExpand);
		return icfg.getICFG();
	}
	
}