package com.ensoftcorp.open.commons.analysis;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.NodeGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Common queries which are useful for writing larger language agnostic analysis
 * programs, and for using on the shell. This also acts as a wrapper around
 * relevant <code>com.ensoftcorp.atlas.core.script.CommonQueries</code> API functions.
 * 
 * @author Ben Holland, Tom Deering, Jon Mathews
 */
public final class CommonQueries {	
	
	// hide constructor
	private CommonQueries() {}
	
	// begin wrapper queries
	
	/**
	 * Returns all references to class literals (Type.class) for the given
	 * types. 
	 * 
	 * Equivalent to classLiterals(index(), types).
	 * 
	 * @param types
	 * @return the query expression
	 */
	public static Q classLiterals(Q types){
		return com.ensoftcorp.atlas.core.script.CommonQueries.classLiterals(types);
	}
	
	/**
	 * Returns all references to class literals (Type.class) for the given
	 * types.
	 * 
	 * @param types
	 * @return the query expression
	 */
	public static Q classLiterals(Q context, Q types){
		return com.ensoftcorp.atlas.core.script.CommonQueries.classLiterals(context, types);
	}
	
	/**
	 * Produces a declarations (contains) graph. 
	 * 
	 * Equivalent to declarations(index(), origin).
	 * 
	 * @param origin
	 * @return the query expression
	 */
	public static Q declarations(Q origin){
		return com.ensoftcorp.atlas.core.script.CommonQueries.declarations(origin);
	}
	
	
	/**
	 * Produces a declarations (contains) graph. Uses only the given context for
	 * the traversal.
	 * 
	 * @param context
	 * @param origin
	 * @return the query expression
	 */
	public static Q declarations(Q context, Q origin){
		return com.ensoftcorp.atlas.core.script.CommonQueries.declarations(context, origin);
	}
	
	/**
	 * Returns direct edges of the given kinds which lay immediately between the
	 * first group and second group of nodes. In the case that the selected edge
	 * kinds have multiple levels of granularity, only the function level of
	 * granularity is used.
	 * 
	 * @param first
	 * @param second
	 * @param edgeTags
	 * @return the query expression
	 */
	public static Q interactions(Q first, Q second, String... edgeTags){
		return com.ensoftcorp.atlas.core.script.CommonQueries.interactions(first, second, edgeTags);
	}
	
	/**
	 * Returns direct edges of the given kinds which lay immediately between the
	 * first group and second group of nodes. In the case that the selected edge
	 * kinds have multiple levels of granularity, only the function level of
	 * granularity is used. Uses only the given context for the traversal.
	 * 
	 * @param context
	 * @param first
	 * @param second
	 * @param edgeTags
	 * @return the query expression
	 */
	public static Q interactions(Q context, Q first, Q second, String... edgeTags){
		return com.ensoftcorp.atlas.core.script.CommonQueries.interactions(context, first, second, edgeTags);
	}
	
	/**
	 * Returns those nodes which are declared by a library.
	 * 
	 * @return the query expression
	 */
	public static Q libraryDeclarations(){
		return com.ensoftcorp.atlas.core.script.CommonQueries.libraryDeclarations(); 
	}
	
	/**
	 * Returns those nodes which are declared by a library. Results are only
	 * returned if they are within the given context.
	 * 
	 * Equivalent to libraryDeclarations(index())
	 * 
	 * @param context
	 * @return the query expression
	 */
	public static Q libraryDeclarations(Q context){
		return com.ensoftcorp.atlas.core.script.CommonQueries.libraryDeclarations(context); 
	}
	
	/**
	 * Returns those nodes which are declared by a library with the given name.
	 * 
	 * @param name
	 * @return the query expression
	 */
	public static Q libraryDeclarations(String name){
		return com.ensoftcorp.atlas.core.script.CommonQueries.libraryDeclarations(name); 
	}
	
	/**
	 * Returns those nodes which are declared by a library with the given name.
	 * Results are only returned if they are within the given context.
	 * 
	 * Equivalent to libraryDeclarations(index(), name)
	 * 
	 * @param context
	 * @param name
	 * @return the query expression
	 */
	public static Q libraryDeclarations(Q context, String name){
		return com.ensoftcorp.atlas.core.script.CommonQueries.libraryDeclarations(context, name); 
	}
	
	/**
	 * Returns the nodes whose names contain the given string.
	 * 
	 * Equivalent to nodesContaining(index(), substring).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesContaining(String substring){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodesContaining(substring);
	}
	
	/**
	 * Returns the nodes whose names contain the given string within the given
	 * context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesContaining(Q context, String substring){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodesContaining(context, substring);
	}
	
	/**
	 * Returns the nodes whose names end with the given string.
	 * 
	 * Equivalent to nodesEndingWith(index(), suffix).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesEndingWith(String suffix){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodesEndingWith(suffix);
	}
	
	/**
	 * Returns the nodes whose names end with the given string within the given
	 * context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesEndingWith(Q context, String suffix){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodesEndingWith(context, suffix);
	}
	
	/**
	 * Returns the nodes whose names match the given regular expression.
	 * 
	 * Equivalent to nodesMatchingRegex(index(), regex).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesMatchingRegex(String regex){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodesMatchingRegex(regex);
	}
	
	/**
	 * Returns the nodes whose names match the given regular expression within
	 * the given context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesMatchingRegex(Q context, String regex){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodesMatchingRegex(context, regex);
	}
	
	/**
	 * Returns the nodes whose names start with the given string.
	 * 
	 * Equivalent to nodesStartingWith(index(), prefix).
	 * 
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesStartingWith(String prefix){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodesStartingWith(prefix);
	}
	
	/**
	 * Returns the nodes whose names start with the given string within the
	 * given context.
	 * 
	 * @param context
	 * @param substring
	 * @return the query expression
	 */
	public static Q nodesStartingWith(Q context, String prefix){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodesStartingWith(context, prefix);
	}
	
	/**
	 * Returns the number of edges contained.
	 * @param toCount
	 * @return
	 */
	public static long edgeSize(Q toCount){
		return com.ensoftcorp.atlas.core.script.CommonQueries.edgeSize(toCount);
	}
	
	/**
	 * Returns the number of nodes contained.
	 * @param toCount
	 * @return
	 */
	public static long nodeSize(Q toCount){
		return com.ensoftcorp.atlas.core.script.CommonQueries.nodeSize(toCount);
	}
	
	/**
	 * Returns whether the given Q is empty.
	 * 
	 * @param test
	 * @return
	 */
	public static boolean isEmpty(Q test){
		return com.ensoftcorp.atlas.core.script.CommonQueries.isEmpty(test);
	}
	
	// begin toolbox commons queries
	
	/**
	 * Returns the set of functions where their names matches the any of the
	 * names given (functionNames) list. A (*) in (functionNames) represents a
	 * wildcard that matches any string.
	 * 
	 * @param functionNames:
	 *            A list of function names as Strings
	 * @return A set of functions
	 */
	public static Q functions(String... functionNames){
		return find(XCSG.Function, functionNames);
	}

	/**
	 * Returns the nodes representing the global variable(s) given by the
	 * parameter list (names). A (*) in any string in the list (names)
	 * represents a wildcard that matches any string.
	 * 
	 * @param names:
	 *            A list of global variable names as Strings
	 * @return A set of global variable nodes
	 */
	public static Q globals(String... names){
		return find(XCSG.GlobalVariable, names);
	}

	/**
	 * Returns the nodes representing the types given by the names. 
	 * A (*) is a wildcard that matches any string.
	 * @param names A list of type names
	 * @return A set of global variable nodes
	 */
	public static Q types(String... names){
		return find(XCSG.Type, names);
	}
	
	/**
	 * Selects the Atlas graph element given a serialized graph
	 * element address
	 * 
	 * Returns null if the address does not correspond to a graph element
	 * 
	 * @param address
	 * @return
	 */
	public static GraphElement getGraphElementByAddress(String address){
		int hexAddress = Integer.parseInt(address, 16);
		GraphElement ge = Graph.U.getAt(hexAddress);
		return ge;
	}
	
	/**
	 * Selects the Atlas node graph element given a serialized graph
	 * element address
	 * 
	 * Returns null if the address does not correspond to a node
	 * 
	 * @param address
	 * @return
	 */
	public static Node getNodeByAddress(String address){
		GraphElement ge = getGraphElementByAddress(address);
		if(ge != null && ge instanceof Node){
			return (Node) ge;
		}
		return null;
	}
	
	/**
	 * Selects the Atlas edge graph element given a serialized graph
	 * element address
	 * 
	 * Returns null if the address does not correspond to a edge
	 * 
	 * @param address
	 * @return
	 */
	public static Edge getEdgeByAddress(String address){
		GraphElement ge = getGraphElementByAddress(address);
		if(ge != null && ge instanceof Edge){
			return (Edge) ge;
		}
		return null;
	}
	
	/**
	 * Returns the parameters of the given functions. 
	 * 
	 * Equivalent to functionParameter(index(), functions)
	 * 
	 * @param functions
	 * @return the query expression
	 */
	public static Q functionParameter(Q functions){
		return functionParameter(Common.index(), functions);
	}
	
	/**
	 * Returns the parameters of the given functions at the given indices. 
	 * 
	 * Equivalent to functionParameter(index(), functions, index)
	 * 
	 * @param functions
	 * @param index
	 * @return the query expression
	 */
	public static Q functionParameter(Q functions, Integer... index){
		return functionParameter(Common.index(), functions, index);
	}
	
	/**
	 * Returns the parameters of the given functions. Results are only returned if
	 * they are within the given context.
	 * 
	 * @param context
	 * @param functions
	 * @return the query expression
	 */
	public static Q functionParameter(Q context, Q functions){
		return functions.children().intersection(context).nodes(XCSG.Parameter);
	}
	
	/**
	 * Returns the parameters of the given functions at the given indices. Results
	 * are only returned if they are within the given context.
	 * 
	 * @param context
	 * @param functions
	 * @param index
	 * @return the query expression
	 */
	public static Q functionParameter(Q context, Q functions, Integer... index){
		return functionParameter(context, functions).selectNode(XCSG.parameterIndex, (Object[]) index);
	}
	
	/**
	 * Returns the return nodes for the given functions.
	 * 
	 * Equivalent to functionReturn(index(), functions).
	 * 
	 * @param functions
	 * @return the query expression
	 */
	public static Q functionReturn(Q functions){
		return functionReturn(Common.index(), functions);
	}
	
	/**
	 * Returns the return nodes for the given functions.
	 * @param context
	 * @param functions
	 * @return the query expression
	 */
	public static Q functionReturn(Q context, Q functions){
		return context.edgesTaggedWithAny(XCSG.Contains).successors(functions).nodes(XCSG.ReturnValue);
	}
	
	/**
	 * Returns the functions declared by the given types. 
	 * 
	 * Equivalent to functionsOf(index(), types).
	 * 
	 * @param params
	 * @return the query expression
	 */
	public static Q functionsOf(Q types){
		return functionsOf(Common.index(), types);
	}
	
	/**
	 * Returns the functions declared by the given types.
	 * 
	 * @param context
	 * @param types
	 * @return the query expression
	 */
	public static Q functionsOf(Q context, Q types){
		return types.nodes(XCSG.Type).children().intersection(context).nodes(XCSG.Function);
	}
	
	/**
	 * 
	 * @param functions
	 * @return the data flow graph under the function
	 */
	public static Q dfg(Q functions) {
		return localDeclarations(functions).nodes(XCSG.DataFlow_Node).induce(Common.edges(XCSG.DataFlow_Edge));
	}
	
	/**
	 * 
	 * @param functions
	 * @return the control flow graph under the function
	 */
	public static Q cfg(Q functions) {
		return localDeclarations(functions).nodes(XCSG.ControlFlow_Node).induce(Common.edges(XCSG.ControlFlow_Edge));
	}
	
	/**
	 * 
	 * @param function
	 * @return the control flow graph under the function
	 */
	public static Q cfg(Node function) {
		return cfg(Common.toQ(function));
	}
	
	/**
	 * 
	 * @param functions
	 * @return the control flow graph (including exceptional control flow) under the function
	 */
	public static Q excfg(Q functions) {
		return localDeclarations(functions).nodes(XCSG.ControlFlow_Node).induce(Common.edges(XCSG.ControlFlow_Edge, XCSG.ExceptionalControlFlow_Edge));
	}
	
	/**
	 * 
	 * @param function
	 * @return the control flow graph (including exceptional control flow) under the function
	 */
	public static Q excfg(Node function) {
		return excfg(Common.toQ(function));
	}
	
	/**
	 * Everything declared under the given functions, but NOT declared under
	 * additional functions or types. Retrieves declarations of only this function.
	 * Results are only returned if they are within the given context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q localDeclarations(Q origin) {
		return localDeclarations(Common.index(), origin);
	}

	/**
	 * Everything declared under the given functions, but NOT declared under
	 * additional functions or types. Retrieves declarations of only this function.
	 * Results are only returned if they are within the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q localDeclarations(Q context, Q origin) {
		Q declarations = context.edges(XCSG.Contains);
		declarations = declarations.differenceEdges(declarations.reverseStep(declarations.nodes(XCSG.Type)));
		return declarations.forward(origin);
	}

	/**
	 * Returns the direct callers of the given functions.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q callers(Q origin) {
		return callers(Common.index(), origin);
	}

	/**
	 * Returns the direct callers of the given functions.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q callers(Q context, Q origin) {
		return context.edges(XCSG.Call).predecessors(origin);
	}

	/**
	 * Returns the subset of the given functions which are called.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q called(Q origin) {
		return called(Common.index(), origin);
	}

	/**
	 * Returns the subset of the given functions which are called. Results are
	 * only returned if they are within the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q called(Q context, Q origin) {
		return context.edges(XCSG.Call).successors(origin);
	}

	/**
	 * Returns the given functions which were called by the given callers.
	 * 
	 * Operates in the index context.
	 * 
	 * @param callers
	 * @param called
	 * @return
	 */
	public static Q calledBy(Q callers, Q called) {
		return calledBy(Common.index(), callers, called);
	}

	/**
	 * Returns the given functions which were called by the given callers. Results
	 * are only returned if they are within the given context.
	 * 
	 * @param context
	 * @param callers
	 * @param called
	 * @return
	 */
	public static Q calledBy(Q context, Q callers, Q called) {
		return context.edgesTaggedWithAny(XCSG.Call).betweenStep(callers, called).retainEdges().leaves();
	}

	/**
	 * Returns the first declaring node of the given Q which is tagged with one
	 * of the given types.
	 * 
	 * Operates in the index context.
	 * 
	 * @param declared
	 * @param declaratorTypes
	 * @return
	 */
	public static Q firstDeclarator(Q declared, String... declaratorTypes) {
		return firstDeclarator(Common.index(), declared, declaratorTypes);
	}

	/**
	 * Returns the first declaring node of the given Q which is tagged with one
	 * of the given types. Results are only returned if they are within the
	 * given context.
	 * 
	 * @param context
	 * @param declared
	 * @param declaratorTypes
	 * @return
	 */
	public static Q firstDeclarator(Q context, Q declared, String... declaratorTypes) {
		Q subContext = declared.containers().intersection(context);
		subContext = subContext.differenceEdges(subContext.reverseStep(subContext.nodes(declaratorTypes)));
		return subContext.reverse(declared).nodes(declaratorTypes);
	}

	/**
	 * Given two query expressions, intersects the given node and edge kinds to
	 * produce a new expression.
	 * 
	 * @param first
	 * @param second
	 * @param nodeTags
	 * @param edgeTags
	 * @return
	 */
	public static Q advancedIntersection(Q first, Q second, String[] nodeTags, String[] edgeTags) {
		Q plainIntersection = first.intersection(second);
		return plainIntersection.nodes(nodeTags).induce(plainIntersection.edgesTaggedWithAny(edgeTags));
	}

	/**
	 * Returns the nodes which directly read from nodes in origin.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q readersOf(Q origin) {
		return readersOf(Common.index(), origin);
	}

	/**
	 * Returns the nodes which directly read from nodes in origin.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q readersOf(Q context, Q origin) {
		return context.edgesTaggedWithAny(XCSG.DataFlow_Edge).successors(origin);
	}

	/**
	 * Returns the nodes which directly write to nodes in origin.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q writersOf(Q origin) {
		return writersOf(Common.index(), origin);
	}

	/**
	 * Returns the nodes which directly write to nodes in origin.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q writersOf(Q context, Q origin) {
		return context.edgesTaggedWithAny(XCSG.DataFlow_Edge).predecessors(origin);
	}

	/**
	 * Returns the nodes from which nodes in the origin read.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q readBy(Q origin) {
		return readBy(Common.index(), origin);
	}

	/**
	 * Returns the nodes from which nodes in the origin read.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q readBy(Q context, Q origin) {
		return writersOf(context, origin);
	}

	/**
	 * Returns the nodes to which nodes in origin write.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q writtenBy(Q origin) {
		return writtenBy(Common.index(), origin);
	}

	/**
	 * Returns the nodes to which nodes in origin write.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q writtenBy(Q context, Q origin) {
		return readersOf(context, origin);
	}
	
	/**
	 * Returns the least common ancestor of both child1 and child2 within the given graph
	 * @param child1
	 * @param child2
	 * @param graph
	 * @return
	 */
	public static Node leastCommonAncestor(Node child1, Node child2, Graph graph){
		return leastCommonAncestor(child1, child2, Common.toQ(graph));
	}
	
	/**
	 * Returns the least common ancestor of both child1 and child2 within the given graph
	 * @param child1
	 * @param child2
	 * @param graph
	 * @return
	 */
	public static Node leastCommonAncestor(Node child1, Node child2, Q graph){
		Q ancestors = graph.reverse(Common.toQ(child1)).intersection(graph.reverse(Common.toQ(child2)));
		return ancestors.leaves().eval().nodes().getFirst();
	}

	/**
	 * Returns the containing function of a given Q or empty if one is not found
	 * @param nodes
	 * @return
	 */
	public static Q getContainingFunctions(Q nodes) {
		AtlasSet<Node> nodeSet = nodes.eval().nodes();
		AtlasSet<Node> containingMethods = new AtlasHashSet<Node>();
		for (Node currentNode : nodeSet) {
			Node function = getContainingFunction(currentNode);
			if (function != null){
				containingMethods.add(function);
			}
		}
		return Common.toQ(Common.toGraph(containingMethods));
	}
	
	/**
	 * Returns the nearest parent that is a control flow node
	 * @param node
	 * @return
	 */
	public static Node getContainingControlFlowNode(Node node) {
		// NOTE: this logic considers that the enclosing control flow node may be two steps or more above
		return getContainingNode(node, XCSG.ControlFlow_Node);
	}

	/**
	 * Returns the containing function of a given graph element or null if one is not found
	 * @param node
	 * @return
	 */
	public static Node getContainingFunction(Node node) {
		// NOTE: this logic considers that the enclosing function may be two steps or more above
		return getContainingNode(node, XCSG.Function);
	}
	
	/**
	 * Find the next immediate containing node with the given tag.
	 * 
	 * @param node 
	 * @param containingTag
	 * @return the next immediate containing node, or null if none exists; never returns the given node
	 */
	public static Node getContainingNode(Node node, String containingTag) {
		if(node == null){
			return null;
		}
		while(true) {
			GraphElement containsEdge = Graph.U.edges(node, NodeDirection.IN).taggedWithAll(XCSG.Contains).getFirst();
			if(containsEdge == null){
				return null;
			}
			Node parent = containsEdge.getNode(EdgeDirection.FROM);
			if(parent.taggedWith(containingTag)){
				return parent;
			}
			node = parent;
		}
	}

	/**
	 * Returns the control flow graph between conditional nodes and the given
	 * origin.
	 * 
	 * Operates within the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q conditionsAbove(Q origin) {
		return conditionsAbove(Common.index(), origin);
	}

	/**
	 * Returns the control flow graph between conditional nodes and the given
	 * origin.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q conditionsAbove(Q context, Q origin) {
		Q conditionNodes = context.nodes(XCSG.ControlFlowCondition);
		return context.edgesTaggedWithAny(XCSG.ControlFlow_Edge).between(conditionNodes, origin);
	}

	/**
	 * Given a Q containing functions or data flow nodes, returns a Q of things
	 * which write to or call things in the Q.
	 * 
	 * Operates within the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q mutators(Q origin) {
		return mutators(Common.index(), origin);
	}

	/**
	 * Returns those nodes in the context which have self edges.
	 * 
	 * @param context
	 * @return
	 */
	public static Q nodesWithSelfEdges(Q context) {
		AtlasSet<GraphElement> result = new AtlasHashSet<GraphElement>();
		for (Edge edge : context.eval().edges()) {
			Node to = edge.getNode(EdgeDirection.TO);
			Node from = edge.getNode(EdgeDirection.FROM);
			if (to == from){
				result.add(to);
			}
		}
		return Common.toQ(new NodeGraph(result));
	}

	/**
	 * Given a Q containing functions or data flow nodes, returns a Q of things
	 * which write to or call things in the Q.
	 * 
	 * Operates within the index context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q mutators(Q context, Q origin) {
		return writersOf(context, origin).union(callers(context, origin));
	}

	/**
	 * Returns those elements in the origin which were called by or written by
	 * elements in the mutators set.
	 * 
	 * Operates within the index context.
	 * 
	 * @param mutators
	 * @param origin
	 * @return
	 */
	public static Q mutatedBy(Q mutators, Q origin) {
		return mutatedBy(Common.index(), mutators, origin);
	}

	/**
	 * Returns those elements in the origin which were called by or written by
	 * elements in the mutators set.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param mutators
	 * @param origin
	 * @return
	 */
	public static Q mutatedBy(Q context, Q mutators, Q origin) {
		return writtenBy(context, origin).union(calledBy(context, origin, mutators)).intersection(origin);
	}
	
	/**
	 * Helper function to get the stringified qualified name of the class
	 * @param type
	 * @return
	 */
	public static String getQualifiedTypeName(Node type) {
		if(type == null){
			throw new IllegalArgumentException("Type is null!");
		}
		if(!type.taggedWith(XCSG.Type)){
			throw new IllegalArgumentException("Type parameter is not a type!");
		}
		return getQualifiedName(type, XCSG.Package);
	}
	
	/**
	 * Helper function to get the stringified qualified name of the function
	 * @param function
	 * @return
	 */
	public static String getQualifiedFunctionName(Node function) {
		if(function == null){
			throw new IllegalArgumentException("Function is null!");
		}
		if(!function.taggedWith(XCSG.Function)){
			throw new IllegalArgumentException("Function parameter is not a function!");
		}
		return getQualifiedName(function, XCSG.Package);
	}
	
	/**
	 * Helper function to get the stringified qualified name of the function
	 * @param function
	 * @return
	 */
	public static String getQualifiedName(Node node) {
		return getQualifiedName(node, XCSG.Package);
	}
	
	/**
	 * Helper function to get the stringified qualified name of the class
	 * Stop after tags specify parent containers to stop qualifying at (example packages or jars)
	 * @param node
	 * @return
	 */
	public static String getQualifiedName(Node node, String...stopAfterTags) {
		if(node == null){
			throw new IllegalArgumentException("Node is null!");
		}
		String result = node.attr().get(XCSG.name).toString();
		Node parent = getDeclarativeParent(node);
		boolean qualified = false;
		while (parent != null && !qualified) {
			for(String stopAfterTag : stopAfterTags){
				if(parent.taggedWith(stopAfterTag)){
					qualified = true;
				}
			}
			String prefix = parent.attr().get(XCSG.name).toString();
			if(!prefix.equals("")){
				result = parent.attr().get(XCSG.name) + "." + result;
			}
			parent = getDeclarativeParent(parent);
		}
		return result;
	}
	
	/**
	 * Returns the single delcarative parent
	 * Returns null if there is no parent
	 * Throws an IllegalArgumentException if there is more than one parent
	 * @param function
	 * @return
	 */
	private static Node getDeclarativeParent(Node node) {
		AtlasSet<Node> parentNodes = Common.toQ(node).parent().eval().nodes();
		if(parentNodes.size() > 1){
			throw new IllegalArgumentException("Multiple declarative parents!");
		}
		return parentNodes.one();
	}
	
	// begin utility functions
	
	private static Q find(String tag, String... names) {
		Q ts = Common.empty();
		for(String n : names){
			ts = ts.union(findByName(n, tag));
		}
		return ts;
	}

	private static Q findByName(String functionName, String tag) {
		if(functionName.indexOf("*") >= 0){
			Q nodes = Common.universe().nodes(tag);
			Q result = getMatches(functionName, nodes);
			return result;
		}
		// Atlas has an index over literal attribute values, so it's faster to query directly
		return Common.universe().nodes(tag).selectNode(XCSG.name, functionName);
	}
	
	/**
	 * Returns all nodes with a name matching the wildcard expression
	 * 
	 * @param name
	 * @param nodes
	 * @return
	 */
	private static Q getMatches(String name, Q nodes){
		name = name.replace("*", ".*");
		AtlasSet<Node> allNodes = nodes.eval().nodes();
		AtlasSet<GraphElement> result = new AtlasHashSet<GraphElement>();
		
		for(GraphElement node : allNodes){
			String thisName = (String) node.getAttr(XCSG.name);
			if(thisName.matches(name)){
				result.add(node);
			}
		}
		return Common.toQ(Common.toGraph(result));
	}
	
}