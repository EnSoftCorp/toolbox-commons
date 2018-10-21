package com.ensoftcorp.open.commons.algorithms;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Interprocedural Control Flow Graph
 * 
 * @author Payas Awadhutkar, Sharwan Ram
 */
public class InterproceduralControlFlowGraph {
	
	/**
	 * Creates an ICFG starting from the given function and expanding every each callsite that could result
	 * @param entryPointFunction
	 * @return
	 */
	public static ICFG icfg(Q entryPointFunction) {
		return icfg(entryPointFunction, Common.empty());
	}
	
	/**
	 * Creates an ICFG starting from the given function and expanding each callsite that could result in the given set of functions to expand
	 * @param entryPointFunction
	 * @param functionsToExpand
	 * @return
	 */
	public static ICFG icfg(Q entryPointFunction, Q functionsToExpand) {
		AtlasSet<Node> entryPointFunctionSet = entryPointFunction.eval().nodes();
		if(entryPointFunctionSet.size() != 1) {
			throw new IllegalArgumentException("Expected a single entry point function");
		}
		return icfg(entryPointFunctionSet.one(), functionsToExpand.nodes(XCSG.Function).eval().nodes());
	}
	
	/**
	 * Creates an ICFG starting from the given function and expanding each callsite that could result in the given set of functions to expand
	 * @param entryPointFunction
	 * @param functionsToExpand
	 * @return
	 */
	public static ICFG icfg(Node entryPointFunction, AtlasSet<Node> functionsToExpand) {
		return new ICFG(entryPointFunction, functionsToExpand);
	}
	
	/**
	 * Creates an ICFG starting from the given function and expanding every each callsite that could result
	 * @param entryPointFunction
	 * @return
	 */
	public static ICFG icfg(Node entryPointFunction) {
		return new ICFG(entryPointFunction, Common.empty().eval().nodes());
	}
}