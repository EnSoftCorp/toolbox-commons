package com.ensoftcorp.open.commons.sandbox;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;

public class Sandbox {
	
	private static int sandboxInstanceCounter = 0;
	
	private static final String SANDBOX_ADDRESS_PREFIX = "SANDBOX_";
	private static int sandboxAddressCounter = 0;

	private String getUniqueSandboxGraphElementAddress(){
		return SANDBOX_ADDRESS_PREFIX + (sandboxAddressCounter++);
	}
	
	private int sandboxInstanceID;
	private Map<String,SandboxGraphElement> addresses;
	
	public SandboxGraph U;
	
	public Sandbox(){
		sandboxInstanceID = (sandboxInstanceCounter++);
		addresses = new HashMap<String,SandboxGraphElement>();
		U = new SandboxGraph();
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
	public static SandboxGraph empty(){
		return new SandboxGraph();
	}
	
	/**
	 * Returns the current sandbox universe graph
	 * @return
	 */
	public SandboxGraph universe(){
		return U;
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
			U.nodes().add(new SandboxNode(node));
		}
	}

	/**
	 * Adds the current state of the given edges in the Atlas graph to the sandbox
	 * @param edges
	 */
	public void addEdges(AtlasSet<Edge> edges) {
		for(Edge edge : edges){
			U.nodes().add(new SandboxNode(edge.from()));
			U.nodes().add(new SandboxNode(edge.to()));
			U.edges().add(new SandboxEdge(edge));
		}
	}
	
	/**
	 * Creates a new sandbox node
	 * This node does not affect the Atlas universe
	 * @return
	 */
	public SandboxNode createNode(){
		SandboxNode node = new SandboxNode(getUniqueSandboxGraphElementAddress());
		U.nodes().add(node);
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
		SandboxEdge edge = new SandboxEdge(getUniqueSandboxGraphElementAddress(), fromNode, toNode);
		U.edges().add(edge);
		return edge;
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
