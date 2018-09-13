package com.ensoftcorp.open.commons.xcsg;

/**
 * Toolbox-defined extensions to XCSG
 *
 */
public class Toolbox {
	
	/**
	 * Integer attribute indicating loop depth.
	 * A loop header shares the same depth as it's immediate children 
	 * (except for nested loop headers, which are +1 deeper).
	 */
	@XCSG_Extension
	public static final String loopDepth = "Toolbox.loopDepth"; //$NON-NLS-1$

}
