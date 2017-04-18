package com.ensoftcorp.open.commons.algorithms;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

public class UniqueEntryExitControlFlowGraph implements UniqueEntryExitGraph {

	/**
	 * The set of nodes in the current graph
	 */
	private AtlasSet<Node> nodes;
	
	/**
	 * The set of edges in the current graph
	 */
	private AtlasSet<Edge> edges;

	private String[] entryNodeTags = { XCSG.controlFlowRoot };

	private String[] exitNodeTags = { XCSG.controlFlowExitPoint }; // may have multiple exit points...
	
	/** 
	 * @param cfg a ControlFlowGraph (may include ExceptionalControlFlow_Edges)
	 */
	public UniqueEntryExitControlFlowGraph(Graph cfg) {
		this.nodes = new AtlasHashSet<Node>();
		this.nodes().addAll(cfg.nodes());
		this.edges = new AtlasHashSet<Edge>();
		this.edges().addAll(cfg.edges());
	}
	
	public UniqueEntryExitControlFlowGraph(Graph cfg, String[] entryNodeTags, String[] exitNodeTags) {
		this.nodes = new AtlasHashSet<Node>();
		this.nodes().addAll(cfg.nodes());
		this.edges = new AtlasHashSet<Edge>();
		this.edges().addAll(cfg.edges());
		
		this.entryNodeTags = entryNodeTags;
		this.exitNodeTags = exitNodeTags;
	}
	
	/**
	 * Gets the predecessors of a given node
	 * @param node
	 * @return Predecessors of node
	 */
	@Override
	public AtlasSet<Node> getPredecessors(Node node){
		AtlasSet<Node> predecessors = new AtlasHashSet<Node>();
		for(Edge edge : this.edges()){
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
	public AtlasSet<Node> getSuccessors(Node node){		
		AtlasSet<Node> successors = new AtlasHashSet<Node>();
		for(Edge edge : this.edges()){
			if(edge.getNode(EdgeDirection.FROM).equals(node)){
				Node child = edge.getNode(EdgeDirection.TO);
				successors.add(child);
			}
		}
		return successors;
	}

	@Override
	public Node getEntryNode() {
		return this.nodes().taggedWithAny(entryNodeTags).getFirst();
	}

	@Override
	public Node getExitNode() {
		return this.nodes().taggedWithAll(exitNodeTags).getFirst();
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
