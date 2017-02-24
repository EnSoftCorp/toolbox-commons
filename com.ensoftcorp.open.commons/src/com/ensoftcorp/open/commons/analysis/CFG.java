package com.ensoftcorp.open.commons.analysis;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

public class CFG {

	/**
	 * 
	 * @param functions
	 * @return the control flow graph under the function
	 */
	public static Q cfg(Q functions) {
		return functions.contained().nodesTaggedWithAll(XCSG.ControlFlow_Node).induce(Common.edges(XCSG.ControlFlow_Edge));
	}
	
	/**
	 * 
	 * @param function
	 * @return the control flow graph under the function
	 */
	public static Q cfg(Node function) {
		return Common.toQ(function).contained().nodesTaggedWithAll(XCSG.ControlFlow_Node).induce(Common.edges(XCSG.ControlFlow_Edge));
	}
	
	/**
	 * 
	 * @param functions
	 * @return the control flow graph (including exceptional control flow) under the function
	 */
	public static Q excfg(Q functions) {
		return functions.contained().nodesTaggedWithAll(XCSG.ControlFlow_Node).induce(Common.edges(XCSG.ControlFlow_Edge, XCSG.ExceptionalControlFlow_Edge));
	}
	
	/**
	 * 
	 * @param function
	 * @return the control flow graph (including exceptional control flow) under the function
	 */
	public static Q excfg(Node function) {
		return Common.toQ(function).contained().nodesTaggedWithAll(XCSG.ControlFlow_Node).induce(Common.edges(XCSG.ControlFlow_Edge, XCSG.ExceptionalControlFlow_Edge));
	}
}
