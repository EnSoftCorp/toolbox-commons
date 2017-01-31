package com.ensoftcorp.open.commons.analysis;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

public class CFG {

	/**
	 * 
	 * @param method
	 * @return the control flow graph under the method
	 */
	public static Q cfg(Q method) {
		return method.contained().nodesTaggedWithAll(XCSG.ControlFlow_Node).induce(Common.edges(XCSG.ControlFlow_Edge));
	}
	
	/**
	 * 
	 * @param method
	 * @return the control flow graph (including exceptional control flow) under the method
	 */
	public static Q excfg(Q method) {
		return method.contained().nodesTaggedWithAll(XCSG.ControlFlow_Node).induce(Common.edges(XCSG.ControlFlow_Edge, XCSG.ExceptionalControlFlow_Edge));
	}
}
