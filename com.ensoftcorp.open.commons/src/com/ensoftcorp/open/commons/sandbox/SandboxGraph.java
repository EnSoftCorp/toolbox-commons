package com.ensoftcorp.open.commons.sandbox;

import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;

public class SandboxGraph {

	private final int sandboxInstanceID;
	private Set<SandboxNode> nodes;
	private Set<SandboxEdge> edges;
	
	/**
	 * Creates an empty sandbox graph
	 */
	protected SandboxGraph(int sandboxInstanceID) {
		this(sandboxInstanceID, new SandboxHashSet<SandboxNode>(sandboxInstanceID), new SandboxHashSet<SandboxEdge>(sandboxInstanceID));
	}
	
	/**
	 * Constructs a new graph from a set of sandbox nodes and edges
	 * @param edges
	 */
	protected SandboxGraph(int sandboxInstanceID, Set<SandboxNode> nodes, Set<SandboxEdge> edges) {
		this.sandboxInstanceID = sandboxInstanceID;
		this.nodes = new SandboxHashSet<SandboxNode>(sandboxInstanceID);
		this.edges = new SandboxHashSet<SandboxEdge>(sandboxInstanceID);
		this.nodes.addAll(nodes);
		for(SandboxEdge edge : edges){
			this.nodes.add(edge.from());
			this.nodes.add(edge.to());
			this.edges.add(edge);
		}
	}

	/**
	 * Returns the sandbox instance this graph belongs to
	 * @return
	 */
	public int getSandboxInstanceID(){
		return sandboxInstanceID;
	}
	
	/**
	 * Returns the nodes of this graph
	 * @return
	 */
	public Set<SandboxNode> nodes() {
		return nodes;
	}

	/**
	 * Returns the edges of this graph
	 * @return
	 */
	public Set<SandboxEdge> edges() {
		return edges;
	}
	
	/**
	 * Gets the node's predecessor or successor edges in this graph
	 * @param node
	 * @param direction
	 * @return
	 */
	public Set<SandboxEdge> edges(SandboxNode node, NodeDirection direction){
		Set<SandboxEdge> result = new SandboxHashSet<SandboxEdge>(sandboxInstanceID);
		for(SandboxEdge edge : edges){
			if(direction == NodeDirection.IN){
				if(edge.to().equals(node)){
					result.add(edge);
				}
			} else {
				if(edge.from().equals(node)){
					result.add(edge);
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the nodes in the graph without edges from the given direction
	 * @param direction
	 * @return
	 */
	public Set<SandboxNode> limit(NodeDirection direction){
		Set<SandboxNode> result = new SandboxHashSet<SandboxNode>(sandboxInstanceID);
		for(SandboxNode node : nodes()){
			Set<SandboxEdge> connections = edges(node, direction);
			if(connections.isEmpty()){
				result.add(node);
			}
		}
		return result;
	}
	
	/**
	 * Selects the nodes of this graph that have no successors
	 * 
	 * Convenience for limit(NodeDirection.OUT)
	 * @return
	 */
	public Set<SandboxNode> leaves(){
		return limit(NodeDirection.OUT);
	}
	
	/**
	 * Selects the nodes of this graph that have no predecessors
	 * 
	 * Convenience for limit(NodeDirection.IN)
	 * @return
	 */
	public Set<SandboxNode> roots(){
		return limit(NodeDirection.IN);
	}
}
