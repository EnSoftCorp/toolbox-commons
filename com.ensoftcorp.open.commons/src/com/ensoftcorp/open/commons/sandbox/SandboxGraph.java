package com.ensoftcorp.open.commons.sandbox;

import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;

public class SandboxGraph {

	private final int sandboxInstanceID;
	private SandboxHashSet<SandboxNode> nodes;
	private SandboxHashSet<SandboxEdge> edges;
	
	/**
	 * Creates an empty sandbox graph
	 */
	// clients should not instantiate, use Sandbox to get an instance
	/*package*/ SandboxGraph(int sandboxInstanceID) {
		this(sandboxInstanceID, new SandboxHashSet<SandboxNode>(sandboxInstanceID), new SandboxHashSet<SandboxEdge>(sandboxInstanceID));
	}
	
	/**
	 * Constructs a new graph from a set of sandbox nodes and edges
	 * @param edges
	 */
	// clients should not instantiate, use Sandbox to get an instance
	/*package*/ SandboxGraph(int sandboxInstanceID, Set<SandboxNode> nodes, Set<SandboxEdge> edges) {
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

	public SandboxGraph(SandboxGraph graph) {
		this(graph.sandboxInstanceID);
	}
	
	public SandboxGraph(Sandbox sandbox){
		this(sandbox.getInstanceID());
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
	public SandboxHashSet<SandboxNode> nodes() {
		return nodes;
	}

	/**
	 * Returns the edges of this graph
	 * @return
	 */
	public SandboxHashSet<SandboxEdge> edges() {
		return edges;
	}
	
	/**
	 * Gets the node's predecessor or successor edges in this graph
	 * @param node
	 * @param direction
	 * @return
	 */
	public SandboxHashSet<SandboxEdge> edges(SandboxNode node, NodeDirection direction){
		SandboxHashSet<SandboxEdge> result = new SandboxHashSet<SandboxEdge>(this);
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
	public SandboxHashSet<SandboxNode> limit(NodeDirection direction){
		SandboxHashSet<SandboxNode> result = new SandboxHashSet<SandboxNode>(this);
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
	public SandboxHashSet<SandboxNode> leaves(){
		return limit(NodeDirection.OUT);
	}
	
	/**
	 * Selects the nodes of this graph that have no predecessors
	 * 
	 * Convenience for limit(NodeDirection.IN)
	 * @return
	 */
	public SandboxHashSet<SandboxNode> roots(){
		return limit(NodeDirection.IN);
	}
	
	/**
	 * A convenience method for nodesTaggedWithAny(String... tags)
	 * 
	 * @param tags
	 * @return
	 */
	public SandboxHashSet<SandboxNode> nodes(String... tags){
		return nodesTaggedWithAny(tags);
	}
	
	/**
	 * Returns the set of nodes from this graph that are tagged with all of the
	 * given tags
	 * 
	 * @param tags
	 * @return
	 */
	public SandboxHashSet<SandboxNode> nodesTaggedWithAny(String... tags){
		SandboxHashSet<SandboxNode> result = new SandboxHashSet<SandboxNode>(this);
		for(SandboxNode node : nodes){
			for(String tag : tags){
				if(node.tags().contains(tag)){
					result.add(node);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the set of nodes from this graph that are tagged with any of the
	 * given tags
	 * 
	 * @param tags
	 * @return
	 */
	public SandboxHashSet<SandboxNode> nodesTaggedWithAll(String... tags){
		SandboxHashSet<SandboxNode> result = new SandboxHashSet<SandboxNode>(this);
		for(SandboxNode node : nodes){
			boolean add = true;
			for(String tag : tags){
				if(!node.tags().contains(tag)){
					add = false;
					break;
				}
			}
			if(add){
				result.add(node);
			}
		}
		return result;
	}
	
	/**
	 * A convenience method for edgesTaggedWithAny(String... tags)
	 * @param tags
	 * @return
	 */
	public SandboxHashSet<SandboxEdge> edges(String... tags){
		return edgesTaggedWithAny(tags);
	}
	
	/**
	 * Returns the set of edges from this graph that are tagged with any of the
	 * given tags
	 * 
	 * @param tags
	 * @return
	 */
	public SandboxHashSet<SandboxEdge> edgesTaggedWithAny(String... tags){
		SandboxHashSet<SandboxEdge> result = new SandboxHashSet<SandboxEdge>(this);
		for(SandboxEdge edge : edges){
			for(String tag : tags){
				if(edge.tags().contains(tag)){
					result.add(edge);
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the set of edges from this graph that are tagged with all of the
	 * given tags
	 * 
	 * @param tags
	 * @return
	 */
	public SandboxHashSet<SandboxEdge> edgesTaggedWithAll(String... tags){
		SandboxHashSet<SandboxEdge> result = new SandboxHashSet<SandboxEdge>(this);
		for(SandboxEdge edge : edges){
			boolean add = true;
			for(String tag : tags){
				if(!edge.tags().contains(tag)){
					add = false;
					break;
				}
			}
			if(add){
				result.add(edge);
			}
		}
		return result;
	}
	
	/**
	 * Gets the predecessor nodes of the given node for this graph's edges
	 * @param node
	 * @return The set of incoming edges to the given node
	 */
	public SandboxHashSet<SandboxNode> predecessors(SandboxNode node){
		SandboxHashSet<SandboxNode> result = new SandboxHashSet<SandboxNode>(this);
		for(SandboxEdge edge : edges){
			if(edge.getNode(EdgeDirection.TO).equals(node)){
				result.add(edge.getNode(EdgeDirection.FROM));
			}
		}
		return result;
	}
	
	/**
	 * Gets the successor nodes of the given node for this graph's edges
	 * @param node
	 * @return The set of out-coming edges from the given node
	 */
	public SandboxHashSet<SandboxNode> successors(SandboxNode node){
		SandboxHashSet<SandboxNode> result = new SandboxHashSet<SandboxNode>(this);
		for(SandboxEdge edge : edges){
			if(edge.getNode(EdgeDirection.FROM).equals(node)){
				result.add(edge.getNode(EdgeDirection.TO));
			}
		}
		return result;
	}
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the forward direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 * 
	 * @param origin
	 * @return
	 */
	public SandboxGraph forwardStep(SandboxNode origin){
		SandboxHashSet<SandboxNode> origins = new SandboxHashSet<SandboxNode>(this);
		origins.add(origin);
		return forwardStep(origins);
	}
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the forward direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 * 
	 * @param origin
	 * @return
	 */
	public SandboxGraph forwardStep(SandboxHashSet<SandboxNode> origin){
		SandboxGraph result = new SandboxGraph(this);
		for(SandboxNode node : origin){
			SandboxHashSet<SandboxEdge> outEdges = getOutEdgesFromNode(node);
			for(SandboxEdge edge : outEdges){
				result.nodes().add(edge.from());
				result.nodes().add(edge.to());
				result.edges().add(edge);
			}
		}
		return result;
	}
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the reverse direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 * 
	 * @param origin
	 * @return
	 */
	public SandboxGraph reverseStep(SandboxNode origin){
		SandboxHashSet<SandboxNode> origins = new SandboxHashSet<SandboxNode>(this);
		origins.add(origin);
		return reverseStep(origins);
	}
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * along a path length of 1 in the reverse direction.
	 * 
	 * The final result includes the given nodes, the traversed edges, and the
	 * reachable nodes.
	 * 
	 * @param origin
	 * @return
	 */
	public SandboxGraph reverseStep(SandboxHashSet<SandboxNode> origin){
		SandboxGraph result = new SandboxGraph(this);
		for(SandboxNode node : origin){
			SandboxHashSet<SandboxEdge> inEdges = getInEdgesToNode(node);
			for(SandboxEdge edge : inEdges){
				result.nodes().add(edge.from());
				result.nodes().add(edge.to());
				result.edges().add(edge);
			}
		}
		return result;
	}
	
	/**
	 * Yields the union of this graph and the given graphs. That is, the
	 * resulting graph's nodes are the union of all nodes, and likewise for
	 * edges.
	 * 
	 * @param a
	 * @param graphs
	 * @return
	 */
	public SandboxGraph union(SandboxGraph... graphs){
		SandboxGraph result = new SandboxGraph(this);
		SandboxHashSet<SandboxNode> nodes = new SandboxHashSet<SandboxNode>(this);
		nodes.addAll(nodes());
		SandboxHashSet<SandboxEdge> edges = new SandboxHashSet<SandboxEdge>(this);
		edges.addAll(edges());
		for(SandboxGraph graph : graphs){
			nodes.addAll(graph.nodes());
			edges.addAll(graph.edges());
		}
		result.nodes().addAll(nodes);
		result.edges().addAll(edges);
		return result;
	}
	
	/**
	 * Select this graph, excluding the graphs g. Note that, because
	 * an edge is only in a graph if it's nodes are in a graph, removing an edge
	 * will necessarily remove the nodes it connects as well. Removing either
	 * node would remove the edge as well.
	 * 
	 * This behavior may seem counter-intuitive if one is thinking in terms of
	 * removing a single edge from a graph. Consider the graphs: - g1: a -> b ->
	 * c - g2: a -> b g1.remove(g2) yields the graph containing only node c:
	 * because b is removed, so b -> c is also removed. In general, this
	 * operation is useful for removing nodes from a graph, but may not be as
	 * useful for operating on edges.
	 * 
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public SandboxGraph difference(SandboxGraph... graphs){
		SandboxGraph result = new SandboxGraph(this);
		SandboxHashSet<SandboxNode> nodes = new SandboxHashSet<SandboxNode>(this);
		nodes.addAll(nodes());
		SandboxHashSet<SandboxEdge> edges = new SandboxHashSet<SandboxEdge>(this);
		edges.addAll(edges());
		for(SandboxGraph graph : graphs){
			nodes.removeAll(graph.nodes());
			edges.removeAll(graph.edges());
		}
		result.nodes().addAll(nodes);
		result.edges().addAll(edges);
		return result;
	}
	
	/**
	 * Select this graph, excluding the edges from the given graphs. 
	 * @param a
	 * @param b
	 * @return
	 */
	public SandboxGraph differenceEdges(SandboxGraph... graphs){
		SandboxGraph result = new SandboxGraph(this);
		SandboxHashSet<SandboxNode> nodes = new SandboxHashSet<SandboxNode>(this);
		nodes.addAll(nodes());
		SandboxHashSet<SandboxEdge> edges = new SandboxHashSet<SandboxEdge>(this);
		edges.addAll(edges());
		for(SandboxGraph graph : graphs){
			edges.retainAll(graph.edges());
		}
		result.nodes().addAll(nodes);
		result.edges().addAll(edges);
		return result;
	}
	
	/**
	 * Yields the intersection of this graph and the given graphs. That is, the
	 * resulting graph's nodes are the intersection of all node sets, and
	 * likewise for edges.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public SandboxGraph intersection(SandboxGraph... graphs){
		SandboxGraph result = new SandboxGraph(this);
		SandboxHashSet<SandboxNode> nodes = new SandboxHashSet<SandboxNode>(this);
		nodes.addAll(nodes());
		SandboxHashSet<SandboxEdge> edges = new SandboxHashSet<SandboxEdge>(this);
		edges.addAll(edges());
		for(SandboxGraph graph : graphs){
			nodes.retainAll(graph.nodes());
			edges.retainAll(graph.edges());
		}
		result.nodes().addAll(nodes);
		result.edges().addAll(edges);
		return result;
	}
	
	/**
	 * From this graph, selects the subgraph such that the given nodes in to are
	 * reachable from the nodes in from in a single step
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public SandboxGraph betweenStep(SandboxNode from, SandboxNode to){
		SandboxHashSet<SandboxNode> fromOrigins = new SandboxHashSet<SandboxNode>(this);
		fromOrigins.add(from);
		SandboxHashSet<SandboxNode> toOrigins = new SandboxHashSet<SandboxNode>(this);
		toOrigins.add(to);
		return forwardStep(fromOrigins).intersection(reverseStep(toOrigins));
	}
	
	/**
	 * From this graph, selects the subgraph such that the given nodes in to are
	 * reachable from the nodes in from in a single step
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public SandboxGraph betweenStep(SandboxHashSet<SandboxNode> from, SandboxHashSet<SandboxNode> to){
		return forwardStep(from).intersection(reverseStep(to));
	}
	
	/**
	 * From this graph, selects the subgraph such that the given nodes in to are
	 * reachable from the nodes in from using forward traversal.
	 * 
	 * Logically equivalent to
	 * graph.forward(from).intersection(graph.reverse(to)) .
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public SandboxGraph between(SandboxNode from, SandboxNode to) {
		SandboxHashSet<SandboxNode> fromOrigins = new SandboxHashSet<SandboxNode>(this);
		fromOrigins.add(from);
		SandboxHashSet<SandboxNode> toOrigins = new SandboxHashSet<SandboxNode>(this);
		toOrigins.add(to);
		return forward(fromOrigins).intersection(reverse(toOrigins));
	}
	
	/**
	 * From this graph, selects the subgraph such that the given nodes in to are
	 * reachable from the nodes in from using forward traversal.
	 * 
	 * Logically equivalent to
	 * graph.forward(from).intersection(graph.reverse(to)) .
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public SandboxGraph between(SandboxHashSet<SandboxNode> from, SandboxHashSet<SandboxNode> to) {
		return forward(from).intersection(reverse(to));
	}
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * using forward transitive traversal.
	 * 
	 * @param origin
	 * @return
	 */
	public SandboxGraph forward(SandboxNode origin){
		SandboxHashSet<SandboxNode> origins = new SandboxHashSet<SandboxNode>(this);
		origins.add(origin);
		return forward(origins);
	}
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * using forward transitive traversal.
	 * 
	 * @param origin
	 * @return
	 */
	public SandboxGraph forward(SandboxHashSet<SandboxNode> origin){
		SandboxGraph result = new SandboxGraph(this);
		result.nodes().addAll(origin);
		SandboxHashSet<SandboxNode> frontier = new SandboxHashSet<SandboxNode>(this);
		frontier.addAll(origin);
		while(!frontier.isEmpty()){
			SandboxNode next = frontier.one();
			frontier.remove(next);
			for(SandboxEdge edge : forwardStep(next).edges()){
				if(result.nodes().add(edge.to())){
					frontier.add(edge.to());
				}
				result.edges().add(edge);
			}
		}
		return result;
	}
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * using reverse transitive traversal.
	 * 
	 * @param origin
	 * @return
	 */
	public SandboxGraph reverse(SandboxNode origin){
		SandboxHashSet<SandboxNode> origins = new SandboxHashSet<SandboxNode>(this);
		origins.add(origin);
		return reverse(origins);
	}
	
	/**
	 * From this graph, selects the subgraph reachable from the given nodes
	 * using reverse transitive traversal.
	 * 
	 * @param origin
	 * @return
	 */
	public SandboxGraph reverse(SandboxHashSet<SandboxNode> origin){
		SandboxGraph result = new SandboxGraph(this);
		result.nodes().addAll(origin);
		SandboxHashSet<SandboxNode> frontier = new SandboxHashSet<SandboxNode>(this);
		frontier.addAll(origin);
		while(!frontier.isEmpty()){
			SandboxNode next = frontier.one();
			frontier.remove(next);
			for(SandboxEdge edge : reverseStep(next).edges()){
				if(result.nodes().add(edge.to())){
					frontier.add(edge.to());
				}
				result.edges().add(edge);
			}
		}
		return result;
	}
	
	/**
	 * Gets incoming edges to node
	 * @param node
	 * @return The set of incoming edges to the given node
	 */
	private SandboxHashSet<SandboxEdge> getInEdgesToNode(SandboxNode node){
		SandboxHashSet<SandboxEdge> inEdges = new SandboxHashSet<SandboxEdge>(this);
		for(SandboxEdge edge : edges){
			if(edge.getNode(EdgeDirection.TO).equals(node)){
				inEdges.add(edge);
			}
		}
		return inEdges;
	}
	
	/**
	 * Gets out-coming edges from node
	 * @param node
	 * @return The set of out-coming edges from the given node
	 */
	private SandboxHashSet<SandboxEdge> getOutEdgesFromNode(SandboxNode node){
		SandboxHashSet<SandboxEdge> outEdges = new SandboxHashSet<SandboxEdge>(this);
		for(SandboxEdge edge : edges){
			if(edge.getNode(EdgeDirection.FROM).equals(node)){
				outEdges.add(edge);
			}
		}
		return outEdges;
	}

}
