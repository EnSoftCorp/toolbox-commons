package com.ensoftcorp.open.commons.sandbox;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;

public class Sandbox {
	
	private static volatile int sandboxInstanceCounter = 0;
	
	public static final String SANDBOX_ADDRESS_PREFIX = "SANDBOX_"; //$NON-NLS-1$
	private static volatile int sandboxAddressCounter = 0;

	private String getUniqueSandboxGraphElementAddress(){
		return SANDBOX_ADDRESS_PREFIX + (sandboxAddressCounter++);
	}
	
	private int sandboxInstanceID;
	private FlushProvider flushProvider = new DefaultFlushProvider();
	
	/**
	 * Sets the flush provider implementation
	 * @param flushProvider
	 */
	public void setFlushProvider(FlushProvider flushProvider) {
		this.flushProvider = flushProvider;
	}

	/**
	 * The sandbox universe graph
	 */
	public SandboxGraph U;
	
	/**
	 * The address map of the universe graph
	 */
	private Map<String,SandboxGraphElement> addresses;
	
	/**
	 * Constructs a new sandbox where changes are isolated from the Atlas graph
	 */
	public Sandbox(){
		sandboxInstanceID = (sandboxInstanceCounter++);
		addresses = new HashMap<String,SandboxGraphElement>();
		U = new SandboxGraph(sandboxInstanceID);
	}
	
	/**
	 * Returns the instance id of this sandbox
	 * @return
	 */
	public int getInstanceID(){
		return sandboxInstanceID;
	}
	
	/**
	 * Returns an empty sandbox graph
	 * @return
	 */
	public SandboxGraph empty(){
		return new SandboxGraph(sandboxInstanceID);
	}
	
	/**
	 * Returns an empty sandbox node set
	 * @return
	 */
	public SandboxHashSet<SandboxNode> emptyNodeSet(){
		return new SandboxHashSet<SandboxNode>(sandboxInstanceID);
	}
	
	/**
	 * Returns an empty sandbox edge set
	 * @return
	 */
	public SandboxHashSet<SandboxEdge> emptyEdgeSet(){
		return new SandboxHashSet<SandboxEdge>(sandboxInstanceID);
	}
	
	/**
	 * Returns the current sandbox universe graph
	 * @return
	 */
	public SandboxGraph universe(){
		return U;
	}
	
	/**
	 * Converts the given graph elements into a sandbox graph
	 * @param graphElements
	 * @return
	 */
	public SandboxGraph toGraph(SandboxHashSet<SandboxNode> nodes, SandboxHashSet<SandboxEdge> edges){
		SandboxGraph result = new SandboxGraph(sandboxInstanceID);
		for(SandboxNode node : nodes){
			result.nodes().add(node);
		}
		for(SandboxEdge edge : edges){
			result.nodes().add(edge.from());
			result.nodes().add(edge.to());
			result.edges().add(edge);
		}
		return result;
	}
	
	/**
	 * Converts the given graph elements into a sandbox graph
	 * @param graphElements
	 * @return
	 */
	public SandboxGraph toGraph(SandboxHashSet<? extends SandboxGraphElement> graphElements){
		SandboxGraph graph = empty();
		for(SandboxGraphElement graphElement : graphElements){
			if(graphElement instanceof SandboxNode){
				graph.nodes().add((SandboxNode) graphElement);
			} else if(graphElement instanceof SandboxEdge){
				graph.nodes().add(((SandboxEdge) graphElement).from());
				graph.nodes().add(((SandboxEdge) graphElement).to());
				graph.edges().add(((SandboxEdge) graphElement));
			}
		}
		return graph;
	}
	
	/**
	 * Converts the given graph elements into a sandbox graph
	 * @param graphElements
	 * @return
	 */
	public SandboxGraph toGraph(SandboxGraphElement... graphElements){
		SandboxGraph graph = empty();
		for(SandboxGraphElement graphElement : graphElements){
			if(graphElement instanceof SandboxNode){
				graph.nodes().add((SandboxNode) graphElement);
			} else if(graphElement instanceof SandboxEdge){
				graph.nodes().add(((SandboxEdge) graphElement).from());
				graph.nodes().add(((SandboxEdge) graphElement).to());
				graph.edges().add(((SandboxEdge) graphElement));
			}
		}
		return graph;
	}
	
	/**
	 * Returns a sandbox graph contained in the sandbox universe referenced in
	 * the given set of the Atlas graph
	 * 
	 * @param nodes
	 * @return
	 */
	public SandboxGraph graph(Graph graph){
		SandboxGraph result = new SandboxGraph(sandboxInstanceID);
		result.nodes().addAll(nodes(graph.nodes()));
		result.edges().addAll(edges(graph.edges()));
		return result;
	}
	
	/**
	 * Returns a set of sandbox nodes contained in the sandbox universe
	 * corresponding to the given set Atlas graph nodes.
	 * Does not instantiate nodes in the sandbox.
	 * 
	 * @param tags
	 * @return the sandbox nodes
	 */
	public SandboxHashSet<SandboxNode> nodes(AtlasSet<Node> nodes){
		SandboxHashSet<SandboxNode> result = new SandboxHashSet<SandboxNode>(sandboxInstanceID);
		for(Node node : nodes){
			SandboxGraphElement ge = node(node);
			if(ge != null && ge instanceof SandboxNode){
				result.add((SandboxNode) ge);
			}
		}
		return result;
	}

	/**
	 * Returns a node from the sandbox corresponding to the Atlas node,
	 * if it already exists in the sandbox.
	 * @param node
	 * @return the sandbox node, else null
	 */
	public SandboxNode node(Node node) {
		SandboxNode ge = (SandboxNode) this.getAt(addrStr(node));
		return ge;
	}
	
	/**
	 * Returns a sandbox set of edges contained in the sandbox universe
	 * referenced in the given set of the Atlas graph edges
	 * 
	 * @param edges
	 * @return
	 */
	public SandboxHashSet<SandboxEdge> edges(AtlasSet<Edge> edges){
		SandboxHashSet<SandboxEdge> result = new SandboxHashSet<SandboxEdge>(sandboxInstanceID);
		for(Edge edge : edges){
			SandboxEdge ge = edge(edge);
			if(ge != null && ge instanceof SandboxEdge){
				result.add((SandboxEdge) ge);
			}
		}
		return result;
	}

	/**
	 * Returns an edge from the sandbox corresponding to the Atlas edge,
	 * if it already exists in the sandbox.
	 * @param edge
	 * @return the sandbox edge, else null
	 */
	private SandboxEdge edge(Edge edge) {
		SandboxEdge ge = (SandboxEdge) this.getAt(addrStr(edge));
		return ge;
	}
	
	/**
	 * Adds the current state of the Atlas nodes and edges from the given Atlas graph to the sandbox
	 * @param graph
	 */
	public SandboxGraph addGraph(Graph graph) {
		SandboxGraph result = new SandboxGraph(sandboxInstanceID, 
				addNodes(graph.nodes()), 
				addEdges(graph.edges()));
		return result;
	}

	/**
	 * Adds the current state of the given nodes in the Atlas graph to the sandbox
	 * @param nodes
	 */
	public SandboxHashSet<SandboxNode> addNodes(AtlasSet<Node> nodes) {
		SandboxHashSet<SandboxNode> result = new SandboxHashSet<SandboxNode>(sandboxInstanceID);
		for(Node node : nodes){
			result.add(addNode(node));
		}
		return result;
	}

	private SandboxNode addNode(Node node) {
		SandboxNode sandboxNode = new SandboxNode(sandboxInstanceID, node);
		U.nodes().add(sandboxNode);
		addresses.put(addrStr(node), sandboxNode);
		return sandboxNode;
	}

	/**
	 * Adds the current state of the given edges in the Atlas graph to the sandbox
	 * @param edges
	 */
	public SandboxHashSet<SandboxEdge> addEdges(AtlasSet<Edge> edges) {
		SandboxHashSet<SandboxEdge> result = new SandboxHashSet<SandboxEdge>(sandboxInstanceID);
		for(Edge edge : edges){
			result.add(addEdge(edge));
		}
		return result;
	}

	private SandboxEdge addEdge(Edge edge) {
		SandboxNode from = node(edge.from()); 
		if (from == null) {
			from = addNode(edge.from());
		}
		
		SandboxNode to = node(edge.to()); 
		if (to == null) {
			to = addNode(edge.to());
		}
		
		SandboxEdge sandboxEdge = new SandboxEdge(sandboxInstanceID, edge, from, to);
		U.edges().add(sandboxEdge);
		addresses.put(addrStr(edge), sandboxEdge);
		return sandboxEdge;
	}

	/*package*/static String addrStr(GraphElement ge) {
		return ge.address().toAddressString();
	}
	
	/**
	 * Creates a new sandbox node
	 * This node does not affect the Atlas universe
	 * @return
	 */
	public SandboxNode createNode(){
		SandboxNode node = new SandboxNode(sandboxInstanceID, getUniqueSandboxGraphElementAddress());
		U.nodes().add(node);
		addresses.put(node.getAddress(), node);
		return node;
	}

	/**
	 * Creates a new sandbox edge
	 * This edge does not affect the Atlas universe
	 * @param fromNode
	 * @param toNode
	 * @return
	 */
	public SandboxEdge createEdge(SandboxNode fromNode, SandboxNode toNode){
		SandboxEdge edge = new SandboxEdge(sandboxInstanceID, getUniqueSandboxGraphElementAddress(), fromNode, toNode);
		U.edges().add(edge);
		addresses.put(edge.getAddress(), edge);
		return edge;
	}
	
	/**
	 * Removes the given sandbox graph element from the sandbox universe
	 * @param graphElement
	 */
	public void delete(SandboxGraphElement graphElement){
		if(graphElement instanceof SandboxNode){
			SandboxNode node = (SandboxNode) graphElement;
			U.nodes().remove(node);
		} else if(graphElement instanceof SandboxEdge){
			SandboxEdge edge = (SandboxEdge) graphElement;
			U.edges().remove(edge);
		}
		addresses.remove(graphElement.getAddress());
	}

	/**
	 * Returns the sandbox graph element associated with this address or null if
	 * one does not exist
	 * 
	 * @param address
	 * @return
	 */
	private SandboxGraphElement getAt(String address){
		return addresses.get(address);
	}
	
	/**
	 * Flushes the changes made in the sandbox universe to the Atlas graph
	 * 
	 * @return The serialized Atlas graph version of the sandbox
	 */
	public Graph flush(){
		// flushing the universe ;)
		return flushProvider.flush(U, addresses);
	}
	
	/**
	 * Flushes the changes made in the sandbox that are restricted to the nodes
	 * and edges in the given graph to the Atlas graph
	 * 
	 * This methods does the following:
	 * 
	 * 1) Adds nodes/edges that are not mirrored to the Atlas graph with the
	 * current tags/attributes
	 * 
	 * 2) Updates (adds/removes) tags and attributes from the corresponding
	 * Atlas graph element's to match the current sandbox tags/attributes for
	 * each node/edge in the sandbox.
	 * 
	 * @param graph the graph containing the set of nodes and edges to flush
	 * 
	 * @return The serialized Atlas graph version of the sandbox
	 */
	public Graph flush(SandboxGraph graph){
		return flushProvider.flush(graph, addresses);
	}
	
	/**
	 * Sandboxes are equal if they have the same instance id
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sandboxInstanceID;
		return result;
	}
	
	/**
	 * Sandboxes are equal if they have the same instance id
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sandbox other = (Sandbox) obj;
		if (sandboxInstanceID != other.sandboxInstanceID)
			return false;
		return true;
	}

}
