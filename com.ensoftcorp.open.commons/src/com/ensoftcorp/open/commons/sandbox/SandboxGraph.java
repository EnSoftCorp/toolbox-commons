package com.ensoftcorp.open.commons.sandbox;

import java.util.HashSet;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;

public class SandboxGraph {

	private Set<SandboxNode> nodes;
	private Set<SandboxEdge> edges;
	
	/**
	 * Creates an empty sandbox graph
	 */
	public SandboxGraph() {
		this(new HashSet<SandboxNode>(), new HashSet<SandboxEdge>());
	}
	
	/**
	 * Creates a sandbox mirror from an Atlas graph
	 * @param graph
	 */
	public SandboxGraph(Graph graph){
		this(graph.nodes(), graph.edges());
	}
	
	/**
	 * Creates a sandbox mirror from a set of Atlas nodes and edges
	 * Use this constructor if the graph has disconnected nodes
	 * @param edges
	 */
	public SandboxGraph(AtlasSet<Node> nodes, AtlasSet<Edge> edges){
		for(Node node : nodes){
			this.nodes.add(new SandboxNode(node));
		}
		for(Edge edge : edges){
			this.nodes.add(new SandboxNode(edge.from()));
			this.nodes.add(new SandboxNode(edge.to()));
			this.edges.add(new SandboxEdge(edge));
		}
	}
	
	/**
	 * Constructs a new graph from a set of sandbox nodes and edges
	 * @param edges
	 */
	public SandboxGraph(Set<SandboxNode> nodes, Set<SandboxEdge> edges) {
		for(SandboxNode node : nodes){
			this.nodes.add(node);
		}
		for(SandboxEdge edge : edges){
			this.nodes.add(edge.from());
			this.nodes.add(edge.to());
			this.edges.add(edge);
		}
	}

	/**
	 * Returns the nodes of this graph
	 * @return
	 */
	public Set<SandboxNode> nodes() {
		return new HashSet<SandboxNode>(nodes);
	}

	/**
	 * Returns the edges of this graph
	 * @return
	 */
	public Set<SandboxEdge> edges() {
		return new HashSet<SandboxEdge>(edges);
	}
	
	/**
	 * Gets the node's predecessor or successor edges in this graph
	 * @param node
	 * @param direction
	 * @return
	 */
	public Set<SandboxEdge> edges(SandboxNode node, NodeDirection direction){
		Set<SandboxEdge> result = new HashSet<SandboxEdge>();
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
		Set<SandboxNode> result = new HashSet<SandboxNode>();
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
