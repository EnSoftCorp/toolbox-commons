package com.ensoftcorp.open.commons.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DirectedPseudograph;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/**
 * Utilities for enumerating paths through a CFG
 * 
 * @author Ben Holland
 */
public class PathEnumerator {
	
	/**
	 * Returns a string representing the path through the CFG
	 * @param path
	 */
	public static String printPath(List<Node> path) {
		StringBuilder result = new StringBuilder();
		String prefix = "";
		for (Node n : path) {
			result.append(prefix);
			result.append(n.getAttr(XCSG.name));
			prefix = " -> ";
		}
		return result.toString();
	}
	
	/**
	 * Returns the set of paths through the CFG of the given function from the control flow root to the control flow exits 
	 * @param function
	 * @return
	 */
	public static List<List<Node>> getPaths(Node function){
		return getPaths(CommonQueries.cfg(function));
	}
	
	/**
	 * Returns the set of paths through the CFG from the control flow root to the control flow exits
	 * @param cfg
	 * @return
	 */
	public static List<List<Node>> getPaths(Q cfg){
		return getPaths(cfg, cfg.nodes(XCSG.controlFlowRoot).eval().nodes(), cfg.nodes(XCSG.controlFlowExitPoint).eval().nodes());
	}
	
	/**
	 * Returns the set of paths through the CFG from a set of source nodes to the set of target nodes
	 * @param cfg
	 * @param sources
	 * @param targets
	 * @return
	 */
	public static List<List<Node>> getPaths(Q cfg, AtlasSet<Node> sources, AtlasSet<Node> targets){
		Set<Node> sourceNodes = new HashSet<Node>();
		for(Node node : cfg.intersection(Common.toQ(sources)).eval().nodes()){
			sourceNodes.add(node);
		}
		
		Set<Node> targetNodes = new HashSet<Node>();
		for(Node node : cfg.intersection(Common.toQ(targets)).eval().nodes()){
			targetNodes.add(node);
		}
		
		DirectedGraph<Node, Edge> jGraph = getJgraphTDirectedGraph(cfg);
		AllDirectedPaths<Node, Edge> allDirectedPaths = new AllDirectedPaths<Node, Edge>(jGraph);
		List<GraphPath<Node,Edge>> paths = allDirectedPaths.getAllPaths(sourceNodes, targetNodes, true, null);
	
		List<List<Node>> result = new ArrayList<List<Node>>();
		for(GraphPath<Node, Edge> path : paths) {
			result.add(path.getVertexList());
		}
		
		return result;
	}
	
	/**
	 * Convenience method to make a JGraphT DirectedGraph from a query 
	 * 
	 * @param q
	 * @return
	 */
	private static DirectedGraph<Node, Edge> getJgraphTDirectedGraph(Q q) {
		DirectedPseudograph<Node, Edge> jGraph = new DirectedPseudograph<Node, Edge>(Edge.class);
		
		for(Node node : q.eval().nodes()){
			jGraph.addVertex(node);
		}
		
		for(Edge edge : q.eval().edges()){
			jGraph.addEdge(edge.getNode(EdgeDirection.FROM), edge.getNode(EdgeDirection.TO), edge);
		}
		
		return jGraph;
	}
}