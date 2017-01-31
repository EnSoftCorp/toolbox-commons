package com.ensoftcorp.open.commons.analysis;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.open.commons.log.Log;

public class G {

	private static final boolean SANITY = true;
	private static final boolean FAIL_FAST = false;
	
	private static boolean FORWARD = true;
	private static boolean REVERSE = false;
	
	private static boolean ONE_EXPECTED = true;
	private static boolean ONE_MAYBE = false;
	
	/**
	 * Gets outgoing edges having tag.
	 * @param g
	 * @param node
	 * @param tag
	 * @return 
	 */
	public static AtlasSet<Edge> outEdges(Graph g, Node node, String tag) {
		return stepToAllEdges(g,node,tag,NodeDirection.OUT);
	}
	
	/**
	 * Get successor, expected to have 0 or 1.
	 * 
	 * @param g
	 * @param node
	 * @param tag
	 * @return null if out degree 0
	 */
	public static Node outMaybe(Graph g, Node node, String tag) {
		return stepToMaybe(g,node,tag,FORWARD,ONE_MAYBE);
	}

	/**
	 * Get successor, expected to have exactly 1.
	 * 
	 * @param g
	 * @param node
	 * @param tag
	 * @return
	 */
	public static Node out(Graph g, Node node, String tag) {
		return stepToMaybe(g,node,tag,FORWARD,ONE_EXPECTED);
	}
	
	/**
	 * Gets successor nodes along edges having tag.
	 * @param g
	 * @param node
	 * @param tag
	 * @return 
	 */
	public static AtlasSet<Node> outs(Graph g, Node node, String tag) {
		return stepToNodes(g,node,tag,FORWARD);
	}
	
	/**
	 * Gets incoming edges having tag.
	 * @param g
	 * @param node
	 * @param tag
	 * @return 
	 */
	public static AtlasSet<Edge> inEdges(Graph g, Node node, String tag) {
		return stepToAllEdges(g,node,tag,NodeDirection.IN);
	}
	
	/**
	 * Get predecessor, expected to have 0 or 1
	 * 
	 * @param g
	 * @param node
	 * @param tag
	 * @return null if out degree 0
	 */
	public static GraphElement inMaybe(Graph g, Node node, String tag) {
		return stepToMaybe(g,node,tag,REVERSE,ONE_MAYBE);
	}

	/**
	 * Get predecessor, expected to have exactly 1
	 * 
	 * @param g
	 * @param node
	 * @param tag
	 * @return
	 */
	public static Node in(Graph g, Node node, String tag) {
		return stepToMaybe(g,node,tag,REVERSE,ONE_EXPECTED);
	}
	
	/**
	 * Gets predecessor nodes along edges having tag.
	 * @param g
	 * @param node
	 * @param tag
	 * @return 
	 */
	public static AtlasSet<Node> ins(Graph g, Node node, String tag) {
		return stepToNodes(g,node,tag,REVERSE);
	}
	
	/**
	 * Get next node in given direction, along edge having tag.
	 * @param g
	 * @param node
	 * @param tag
	 * @param d
	 * @return
	 */
	private static AtlasSet<Edge> stepToAllEdges(Graph g, Node node, String tag, NodeDirection d) {
		return g.edges(node, d).taggedWithAny(tag);
	}
	
	/**
	 * Get next node, expected to have 0 or 1
	 * 
	 * @param g
	 * @param node
	 * @param tag
	 * @param expected true: 1, false: 0 or 1
	 * @return
	 */
	private static Node stepToMaybe(Graph g, Node node, String tag, boolean forward, boolean expected) {
		NodeDirection nd;
		EdgeDirection ed;
		if (forward) {
			nd = NodeDirection.OUT;
			ed = EdgeDirection.TO;
		} else {
			nd = NodeDirection.IN;
			ed = EdgeDirection.FROM;
		}
		
		AtlasSet<Edge> edges = stepToAllEdges(g, node, tag, nd);
		if (SANITY && edges.size() > 1) {
			String message = nd + " degree > 1 for (tag, node): " + tag + ", " + node;
			Log.warning(message);
			if (FAIL_FAST)
				throw new IllegalArgumentException(message);
		}
		
		GraphElement edge = edges.getFirst();
		if (edge == null) {
			if (expected)
				Log.warning(nd + " degree == 0 for (tag, node): " + tag + ", " + node);
			return null;
		}
		
		return edge.getNode(ed);		
	}
	
	/**
	 * Get next nodes
	 * 
	 * @param g
	 * @param node
	 * @param tag
	 * @param forward
	 * @return
	 */
	private static AtlasSet<Node> stepToNodes(Graph g, Node node, String tag, boolean forward) {
		NodeDirection nd;
		EdgeDirection ed;
		if (forward) {
			nd = NodeDirection.OUT;
			ed = EdgeDirection.TO;
		} else {
			nd = NodeDirection.IN;
			ed = EdgeDirection.FROM;
		}
		
		AtlasSet<Node> nodes = new AtlasHashSet<Node>();
		AtlasSet<Edge> edges = stepToAllEdges(g, node, tag, nd);
		for (GraphElement edge : edges) {
			nodes.add(edge.getNode(ed));
		}
		
		return nodes;
	}

}
