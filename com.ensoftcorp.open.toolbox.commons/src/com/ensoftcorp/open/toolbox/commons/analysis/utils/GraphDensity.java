package com.ensoftcorp.open.toolbox.commons.analysis.utils;

import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;

/**
 * A set of utilities for calculating graph density
 * 
 * @author Ben Holland
 */
public class GraphDensity {

	private GraphDensity() {}
	
	/**
	 * Calculates Graph Density of a Q, considering all nodes and edges
	 * 
	 * @param graph
	 *            A Q representing the graph to calculate density for
	 * @param directed
	 *            A boolean to indicate if the edges should be treated as
	 *            directed or undirected edges
	 * @return graph density calculated as: An undirected graph has no loops and
	 *         can have at most |N| * (|N| - 1) / 2 edges, so the density of an
	 *         undirected graph is 2 * |E| / (|N| * (|N| - 1)).
	 * 
	 *         A directed graph has no loops and can have at most |N| * (|N| -
	 *         1) edges, so the density of a directed graph is |E| / (|N| * (|N|
	 *         - 1)).
	 * 
	 *         Note: N is the number of nodes and E is the number of edges in
	 *         the graph. Note: A value of 0 would be a sparse graph and a value
	 *         of 1 is a dense graph. Note: Because of the way the way the Atlas
	 *         schema is constructed the above assumptions are likely to be
	 *         violated based on the nodes/edges contained in the graph.
	 *         Therefore the result of this calculation will likely not be
	 *         between 0 and 1 as expected, and should be taken with a grain of
	 *         salt.
	 * 
	 *         Reference: http://webwhompers.com/graph-theory.html
	 */
	public static double getDensity(Q graph, boolean directed) {
		double N = new Double(CommonQueries.nodeSize(graph));
		double E = new Double(CommonQueries.edgeSize(graph));

		// no nodes means we don't even have a graph
		// check this first
		if (N <= 0) {
			return -1;
		}

		// can't have any edges with just one node
		if (N == 1) {
			return -1;
		}

		// no edges is the sparsest you can get
		if ((E <= 0)) {
			return 0;
		}

		if (directed) {
			return E / (N * (N - 1));
		} else {
			return 2 * E / (N * (N - 1));
		}
	}

	/**
	 * Returns the Graph Density of a Q, considering specified nodes and edges
	 * 
	 * @param graph
	 * @param directed
	 * @param nodeTypes
	 *            An array of node type tags, or null for all nodes
	 * @param edgeTypes
	 *            An array of edge type tags, or null for all edges
	 * @return See getDensity method for details
	 */
	public static double getDensity(Q graph, boolean directed, String[] nodeTypes, String[] edgeTypes) {
		Q nodes = nodeTypes != null ? graph.nodesTaggedWithAny(nodeTypes).retainNodes() : graph.retainNodes();
		Q edges = edgeTypes != null ? graph.edgesTaggedWithAll(edgeTypes).retainEdges() : graph.retainEdges();
		graph = nodes.union(edges);
		return getDensity(graph, directed);
	}
	
}
