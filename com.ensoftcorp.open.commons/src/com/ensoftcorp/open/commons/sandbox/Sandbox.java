package com.ensoftcorp.open.commons.sandbox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;

public class Sandbox {
	
	private static int sandboxInstanceCounter = 0;
	
	public static final String SANDBOX_ADDRESS_PREFIX = "SANDBOX_";
	private static int sandboxAddressCounter = 0;

	private String getUniqueSandboxGraphElementAddress(){
		return SANDBOX_ADDRESS_PREFIX + (sandboxAddressCounter++);
	}
	
	private int sandboxInstanceID;
	private Map<String,SandboxGraphElement> addresses;
	
	/**
	 * The sandbox universe graph
	 */
	public SandboxGraph U;
	
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
	 * Returns a sandbox set of nodes contained in the sandbox universe
	 * referenced in the given set of the Atlas graph nodes
	 * 
	 * @param tags
	 * @return
	 */
	public SandboxHashSet<SandboxNode> nodes(AtlasSet<Node> nodes){
		SandboxHashSet<SandboxNode> result = new SandboxHashSet<SandboxNode>(sandboxInstanceID);
		for(Node node : nodes){
			SandboxGraphElement ge = this.getAt(node.address().toAddressString());
			if(ge != null && ge instanceof SandboxNode){
				result.add((SandboxNode) ge);
			}
		}
		return result;
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
			SandboxGraphElement ge = this.getAt(edge.address().toAddressString());
			if(ge != null && ge instanceof SandboxEdge){
				result.add((SandboxEdge) ge);
			}
		}
		return result;
	}
	
	/**
	 * Adds the current state of the Atlas nodes and edges from the given Atlas graph to the sandbox
	 * @param graph
	 */
	public void addGraph(Graph graph) {
		addNodes(graph.nodes());
		addEdges(graph.edges());
	}

	/**
	 * Adds the current state of the given nodes in the Atlas graph to the sandbox
	 * @param nodes
	 */
	public void addNodes(AtlasSet<Node> nodes) {
		for(Node node : nodes){
			SandboxNode sandboxNode = new SandboxNode(sandboxInstanceID, node);
			U.nodes().add(sandboxNode);
			addresses.put(node.address().toAddressString(), sandboxNode);
		}
	}

	/**
	 * Adds the current state of the given edges in the Atlas graph to the sandbox
	 * @param edges
	 */
	public void addEdges(AtlasSet<Edge> edges) {
		for(Edge edge : edges){
			SandboxNode fromSandboxNode = new SandboxNode(sandboxInstanceID, edge.from());
			U.nodes().add(fromSandboxNode);
			addresses.put(edge.from().address().toAddressString(), fromSandboxNode);
			
			SandboxNode toSandboxNode = new SandboxNode(sandboxInstanceID, edge.to());
			U.nodes().add(toSandboxNode);
			addresses.put(edge.to().address().toAddressString(), toSandboxNode);
			
			SandboxEdge sandboxEdge = new SandboxEdge(sandboxInstanceID, edge);
			U.edges().add(sandboxEdge);
			addresses.put(edge.address().toAddressString(), sandboxEdge);
		}
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
			U.nodes().remove(edge.from());
			U.nodes().remove(edge.to());
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
	public SandboxGraphElement getAt(String address){
		return addresses.get(address);
	}
	
	/**
	 * Flushes the changes made in the sandbox universe to the Atlas graph
	 * 
	 * @return The serialized Atlas graph version of the sandbox
	 */
	public Graph flush(){
		// flushing the universe ;)
		return flush(U);
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
		AtlasSet<Node> nodes = new AtlasHashSet<Node>();
		for(SandboxNode node : graph.nodes()){
			nodes.add((Node) flush(node));
		}
		AtlasSet<Edge> edges = new AtlasHashSet<Edge>();
		for(SandboxEdge edge : graph.edges()){
			edges.add((Edge) flush(edge));
		}
		return new UncheckedGraph(nodes, edges);
	}
	
	/**
	 * Flushes the changes made or creation of a sandbox graph element to the
	 * Atlas graph
	 * 
	 * @param ge
	 * @return
	 */
	private GraphElement flush(SandboxGraphElement ge) {
		if(ge.isMirror()){
			if(ge instanceof SandboxNode){
				Node node = Graph.U.createNode();
				// add all the sandbox tags
				for(String tag : ge.tags()){
					node.tag(tag);
				}
				// add all new sandbox attributes
				for(String key : ge.attr().keySet()){
					node.putAttr(key, ge.attr().get(key));
				}
				addresses.remove(ge.getAddress());
				ge.flush(node.address().toAddressString());
				addresses.put(ge.getAddress(), ge);
				return node;
			} else if(ge instanceof SandboxEdge){
				SandboxEdge sandboxEdge = (SandboxEdge) ge;
				// assert: nodes will all have been flushed by the time we are flushing edges
				Node from = (Node) getGraphElementByAddress(sandboxEdge.from().getAddress());
				Node to = (Node) getGraphElementByAddress(sandboxEdge.to().getAddress());
				Edge edge = Graph.U.createEdge(from, to);
				// add all the sandbox tags
				for(String tag : ge.tags()){
					edge.tag(tag);
				}
				// add all new sandbox attributes
				for(String key : ge.attr().keySet()){
					edge.putAttr(key, ge.attr().get(key));
				}
				addresses.remove(ge.getAddress());
				ge.flush(edge.address().toAddressString());
				addresses.put(ge.getAddress(), ge);
				return edge;
			} else {
				throw new RuntimeException("Unknown sandbox graph element type.");
			}
		} else {
			GraphElement age = getGraphElementByAddress(ge.getAddress());
			
			// purge all old tags
			Set<String> tagsToRemove = new HashSet<String>();
			for(String tag : age.tags()){
				tagsToRemove.add(tag);
			}
			for(String tag : tagsToRemove){
				age.tags().remove(tag);
			}
			
			// add all the sandbox tags
			for(String tag : ge.tags()){
				age.tag(tag);
			}
			
			// purge all old attributes
			Set<String> keysToRemove = new HashSet<String>();
			for(String key : age.attr().keys()){
				keysToRemove.add(key);
			}
			for(String key : keysToRemove){
				age.attr().remove(key);
			}
			
			// add all new sandbox attributes
			for(String key : ge.attr().keySet()){
				age.putAttr(key, ge.attr().get(key));
			}
			
			return age;
		}
	}

	/**
	 * Helper method to select the Atlas graph element given a serialized graph element address
	 * @param address
	 * @return
	 */
	private GraphElement getGraphElementByAddress(String address){
		int hexAddress = Integer.parseInt(address, 16);
		GraphElement ge = Graph.U.getAt(hexAddress);
		return ge;
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
