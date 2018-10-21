package com.ensoftcorp.open.commons.algorithms;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.xcsg.XCSG_Extension;

public class UniqueEntryExitInterproceduralControlFlowGraph implements UniqueEntryExitGraph {

	/**
	 * Tag applied to the newly created master entry node
	 */
	@XCSG_Extension
	public static final String UniqueEntryExitICFG_Master_Entry = "UniqueEntryExitICFG_Master_Entry";
	
	/**
	 * Tag applied to the newly create master exit node
	 */
	@XCSG_Extension
	public static final String UniqueEntryExitICFG_Master_Exit = "UniqueEntryExitICFG_Master_Exit";
	
	/**
	 * The name attribute applied to the EventFlow_Master_Entry of the PCG
	 */
	@XCSG_Extension
	public static final String UniqueEntryExitICFG_Master_Entry_Name = "\u22A4";
	
	/**
	 * The name attribute applied to the EventFlow_Master_Exit of the PCG
	 */
	@XCSG_Extension
	public static final String UniqueEntryExitICFG_Master_Exit_Name = "\u22A5";
	
	/**
	 * Tag applied to the newly created edges between the master entry and the cfg roots 
	 * and the cfg exits and the master exit
	 */
	@XCSG_Extension
	public static final String UniqueEntryExitICFG_Edge = "UniqueEntryExitCFG_Edge";
	
	/**
	 * The interprocedural control flow graph
	 */
	private Graph icfg;
	
	/**
	 * The set of CFG roots
	 */
	private AtlasSet<Node> roots;
	
	/**
	 * The set of CFG exits
	 */
	private AtlasSet<Node> exits;
	
	/**
	 * The set of nodes in the current graph
	 */
	private AtlasSet<Node> nodes;
	
	/**
	 * The set of edges in the current graph
	 */
	private AtlasSet<Edge> edges;

	/**
	 * The master entry node
	 */
	private Node masterEntry;
	
	/**
	 * The master exit node
	 */
	private Node masterExit;
	
	/**
	 * Entry point function of the interprocedural control flow graph
	 */
	private Node function;
	
	/**
	 * Functions captured by the interprocedural control flow graph	
	 */
	private AtlasSet<Node> functions;
	
	/**
	 * Constructs a new unique entry/exit interprocedural control flow graph. 
	 *  
	 * @param icfg
	 *            a control flow graph
	 */
	public UniqueEntryExitInterproceduralControlFlowGraph(Graph icfg) {
		this(icfg, Common.toQ(icfg).roots().eval().nodes(), Common.toQ(icfg).leaves().eval().nodes(), false);
	}
	
	/**
	 * Constructs a new unique entry/exit interprocedural control flow graph.
	 * 
	 * Optionally, containment edges can be added for display purposes
	 * 
	 * @param icfg
	 * @param addContains
	 */
	public UniqueEntryExitInterproceduralControlFlowGraph(Graph icfg, boolean addContains) {
		this(icfg, Common.toQ(icfg).roots().eval().nodes(), Common.toQ(icfg).leaves().eval().nodes(), addContains);
	}
	
	/**
	 * Constructs a new unique entry/exit interprocedural control flow graph with the specified
	 * entry and exit points.
	 * 
	 * @param icfg
	 *            an interprocedural control flow graph
	 * @param roots
	 * @param exits
	 */
	public UniqueEntryExitInterproceduralControlFlowGraph(Graph icfg, AtlasSet<Node> roots, AtlasSet<Node> exits) {
		this(icfg, roots, exits, false);
	}
	
	/**
	 * Constructs a new unique entry/exit interprocedural control flow graph with the specified
	 * entry and exit points.
	 * 
	 * @param icfg
	 *            a control flow graph
	 * @param roots
	 * @param exits
	 * @param addContains
	 */
	public UniqueEntryExitInterproceduralControlFlowGraph(Graph icfg, AtlasSet<Node> roots, AtlasSet<Node> exits, boolean addContains) {
		this(icfg, roots, false, exits, false, false);
	}
	
	/**
	 * Constructs a new unique entry/exit interprocedural control flow graph with the specified
	 * entry and exit points.
	 * 
	 * @param icfg
	 * @param roots
	 * @param relaxNonEmptyRootsRequirement Relaxes the requirement that roots must be a non-empty set
	 * @param exits
	 * @param relaxNonEmptyExitsRequirement Relaxes the requirement that exist must be a non-empty set
	 * @param addContains
	 */
	public UniqueEntryExitInterproceduralControlFlowGraph(Graph icfg, AtlasSet<Node> roots, boolean relaxNonEmptyRootsRequirement, AtlasSet<Node> exits, boolean relaxNonEmptyExitsRequirement, boolean addContains) {
		AtlasSet<Node> capturedfunctions = CommonQueries.getContainingFunctions(Common.toQ(icfg)).eval().nodes();
		AtlasSet<Node> functions = CommonQueries.getContainingFunctions(Common.toQ(roots)).eval().nodes();
		if(functions.isEmpty()){
			String message = "CFG is empty or is not contained within a function!";
			IllegalArgumentException e = new IllegalArgumentException(message);
			Log.error(message, e);
			throw e;
		} else if(functions.size() > 1){
			String message = "ICFG must have a unique entry point function";
			IllegalArgumentException e = new IllegalArgumentException(message);
			Log.error(message, e);
			throw e;
		} else {
			this.function = functions.one();
			this.functions = capturedfunctions;
			this.icfg = icfg;
			this.nodes = new AtlasHashSet<Node>(icfg.nodes());
			this.edges = new AtlasHashSet<Edge>(icfg.edges());
			
			this.roots = Common.toQ(roots).intersection(Common.toQ(icfg)).eval().nodes();

			if(!relaxNonEmptyRootsRequirement && this.roots.isEmpty()){
				String message = "ICFG roots must be a non-empty set contained within the ICFG!";
				IllegalArgumentException e = new IllegalArgumentException(message);
				Log.error(message, e);
				throw e;
			}
			
			this.exits = Common.toQ(exits).intersection(Common.toQ(icfg)).eval().nodes();
			if(!relaxNonEmptyExitsRequirement && this.exits.isEmpty()){
				String message = "ICFG exits must be a non-empty set contained within the ICFG!";
				IllegalArgumentException e = new IllegalArgumentException(message);
				Log.error(message, e);
				throw e;
			}
			
			this.masterEntry = setupMasterEntryNode(this.roots, addContains);
			this.masterExit = setupMasterExitNode(this.exits, addContains);
		}
	}
	
	/**
	 * Creates the nodes and edges for setting up the master entry node
	 * @param roots nodes to consider as control flow roots (entry points) in the graph
	 */
	private Node setupMasterEntryNode(AtlasSet<Node> roots, boolean addContains){
		// search if the function has a master entry node created previously
		Node masterEntryNode;
		if(addContains){
			// note we are reusing master entry nodes so the search should be from
			// as a child of the entry point function
			masterEntryNode = Common.toQ(function).children()
					.nodes(UniqueEntryExitICFG_Master_Entry)
					.eval().nodes().one();
		} else {
			// note we are reusing master entry nodes so the search should be from
			// icfg not just the specified roots
			masterEntryNode = Common.universe()
					.predecessors(new ICFG(function).getICFG())
					.nodes(UniqueEntryExitICFG_Master_Entry)
					.eval().nodes().one();
		}
		
		// if master entry node has not been created previously, then we need to
		// create one now
		if (masterEntryNode == null) {
			masterEntryNode = Graph.U.createNode();
			masterEntryNode.attr().put(XCSG.name, UniqueEntryExitICFG_Master_Entry_Name);
			masterEntryNode.tag(UniqueEntryExitICFG_Master_Entry);
			if(addContains){
				Edge containsEdge = Graph.U.createEdge(function, masterEntryNode);
				containsEdge.tag(XCSG.Contains);
			}
		}
		
		// add the master entry node
		this.nodes.add(masterEntryNode);
		
		// create entry edges from the master entry to the root nodes
		// and check if the entry edges exist before creating new ones
		for(Node root : roots){
			Edge entryEdge = this.getOrCreateEdge(masterEntryNode, root);
			this.edges.add(entryEdge);
		}
		return masterEntryNode;
	}
	
	/**
	 * Creates the nodes and edges for setting up the master exit node
	 * @param exits nodes to consider as control flow exits (exit points) in the graph
	 * @return
	 */
	private Node setupMasterExitNode(AtlasSet<Node> exits, boolean addContains) {
		// search if the function has a master entry node created previously
		Node masterExitNode;
		if(addContains){
			// note we are reusing master entry nodes so the search should be from
			// as a child of the function
			masterExitNode = Common.toQ(function).children()
					.nodes(UniqueEntryExitICFG_Master_Exit)
					.eval().nodes().one();
		} else {
			// note we are reusing master entry nodes so the search should be from
			// the entire function cfg not just the specified roots
			masterExitNode = Common.universe()
					.successors(new ICFG(function).getICFG())
					.nodes(UniqueEntryExitICFG_Master_Exit)
					.eval().nodes().one();
		}
		
		// if master exit node has not been created previously, then we need to
		// create one now
		if (masterExitNode == null) {
			masterExitNode = Graph.U.createNode();
			masterExitNode.attr().put(XCSG.name, UniqueEntryExitICFG_Master_Exit_Name);
			masterExitNode.tag(UniqueEntryExitICFG_Master_Exit);
			if(addContains){
				Edge containsEdge = Graph.U.createEdge(function, masterExitNode);
				containsEdge.tag(XCSG.Contains);
			}
		}
		
		// add the master exit node to the pcg
		this.nodes.add(masterExitNode);

		// create exit edges from the exits to the master exit node
		// and check if the exit edges exist before creating new ones
		for (Node exit : exits) {
			Edge exitEdge = this.getOrCreateEdge(exit, masterExitNode);
			this.edges.add(exitEdge);
		}
		return masterExitNode;
	}
	
	private Edge getOrCreateEdge(Node masterEntryNode, Node root) {
		AtlasSet<Edge> betweenEdges = Common.universe().edges(UniqueEntryExitICFG_Edge).betweenStep(Common.toQ(masterEntryNode), Common.toQ(root)).eval().edges();
		if (!betweenEdges.isEmpty()) {
			return betweenEdges.one();
		} else {
			Edge edge = Graph.U.createEdge(masterEntryNode, root);
			edge.tag(XCSG.Edge);
			edge.tag(UniqueEntryExitICFG_Edge);
			return edge;
		}
	}
	
	public Graph getICFG(){
		return icfg;
	}
	
	public AtlasSet<Node> getRoots(){
		return roots;
	}
	
	public AtlasSet<Node> getExits(){
		return exits;
	}
	
	public Node getEntryFunction(){
		return function;
	}
	
	public AtlasSet<Node> getCapturedFunctions() {
		return functions;
	}
	
	public Graph getGraph(){
		return Common.toQ(nodes).induce(Common.toQ(edges)).eval();
	}
	
	/**
	 * Gets the predecessors of a given node 
	 * @param node
	 * @return Predecessors of node
	 */
	@Override
	public AtlasSet<Node> getPredecessors(Node node) {
		AtlasSet<Node> predecessors = new AtlasHashSet<Node>();
		for(Edge edge : edges()){
			if(edge.getNode(EdgeDirection.TO).equals(node)){
				Node parent = edge.getNode(EdgeDirection.FROM);
				predecessors.add(parent);
			}
		}
		return predecessors;
	}

	/**
	 * Gets the successors of a given node 
	 * @param node
	 * @return Successors of node
	 */
	@Override
	public AtlasSet<Node> getSuccessors(Node node) {
		AtlasSet<Node> successors = new AtlasHashSet<Node>();
		for(Edge edge : edges()){
			if(edge.getNode(EdgeDirection.FROM).equals(node)){
				Node child = edge.getNode(EdgeDirection.TO);
				successors.add(child);
			}
		}
		return successors;
	}

	@Override
	public Node getEntryNode() {
		return masterEntry;
	}

	@Override
	public Node getExitNode() {
		return masterExit;
	}

	@Override
	public AtlasSet<Node> nodes() {
		return nodes;
	}

	@Override
	public AtlasSet<Edge> edges() {
		return edges;
	}
	
}
