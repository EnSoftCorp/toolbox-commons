package com.ensoftcorp.open.commons.analysis;

import com.ensoftcorp.atlas.core.query.Attr.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.script.CommonQueries.TraversalDirection;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Common set definitions which are useful for program analysis
 * 
 * @author Tom Deering, Ben Holland
 */
public final class SetDefinitions {

private SetDefinitions() {}
	
	private static Q index() {
		return Common.codemap();
	}

	/**
	 * Types which represent arrays of other types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q arrayTypes() {
		return index().nodesTaggedWithAny(XCSG.ArrayType);
	}

	/**
	 * Types which represent language primitive types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q primitiveTypes() {
		return index().nodesTaggedWithAny(XCSG.Primitive);
	}

	/**
	 * Summary invoke nodes, representing invocations on methods.
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q invokeNodes() {
		return index().nodesTaggedWithAny(Node.INVOKE);
	}

	/**
	 * Everything declared under any of the known API projects, if they are in
	 * the index.
	 */
	public static Q apis() {
		return CommonQueries.declarations(index().nodesTaggedWithAny(XCSG.Library), TraversalDirection.FORWARD).difference(arrayTypes(),
				primitiveTypes(), invokeNodes());
	}

	/**
	 * Methods defined in java.lang.Object, and all methods which override them
	 */
	public static Q objectMethodOverrides() {
		return Common.edges(XCSG.Overrides).reverse(
				CommonQueries.declarations(Common.typeSelect("java.lang", "Object"), TraversalDirection.FORWARD).nodesTaggedWithAny(XCSG.Method));
	}

	/**
	 * Everything in the universe which is part of the app (not part of the
	 * apis, or any "floating" nodes).
	 */
	public static Q app() {
		return index().difference(apis(), invokeNodes(), arrayTypes(), primitiveTypes());
	}

	/**
	 * All method nodes declared by the APIs.
	 */
	public static Q apiMethods() {
		return apis().nodesTaggedWithAny(XCSG.Method);
	}

	/**
	 * All variable nodes declared by the APIs.
	 */
	public static Q apiVariables() {
		return apis().nodesTaggedWithAny(XCSG.Variable);
	}

	/**
	 * All data flow nodes declared by the APIs.
	 */
	public static Q apiDFN() {
		return apis().nodesTaggedWithAny(XCSG.DataFlow_Node);
	}

	/**
	 * All edges for which both endpoints lay within the APIs.
	 */
	public static Q apiEdges() {
		return apis().induce(index());
	}
}