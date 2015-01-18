package com.ensoftcorp.open.toolbox.commons.analysis.utils;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.db.set.UnionSet;
import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.Common;

/**
 * A set of traversals for performing undirected graph traversals
 * 
 * @author Tom Deering
 */
public class UndirectedTraversals {
	
	private UndirectedTraversals() {}
	
	/**
	 * Traverse all edges in the context from the origin
	 * in an undirected manner.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q undirectedTraverse(Q context, Q origin){
		return nodeWalk(context, origin, Integer.MAX_VALUE);
	}
	
	/**
	 * Traverse all edges in the context from the origin
	 * in an undirected manner. Limited traversal to 1 edge step.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q undirectedTraverseStep(Q context, Q origin){
		return nodeWalk(context, origin, 1);
	}
	
	/**
	 * Traverse all edges in the context between from and to
	 * in an undirected manner.
	 * 
	 * @param context
	 * @param from
	 * @param to
	 * @return
	 */
	public static Q undirectedBetween(Q context, Q from, Q to){
		return undirectedTraverse(context, from).intersection(undirectedTraverse(context, to));
	}
	
	/**
	 * Traverse all edges in the context between from and to
	 * in an undirected manner. Limited traversal to 1 edge step.
	 * 
	 * @param context
	 * @param from
	 * @param to
	 * @return
	 */
	public static Q undirectedBetweenStep(Q context, Q from, Q to){
		return undirectedTraverseStep(context, from).intersection(undirectedTraverseStep(context, to));
	}
	
	private static Q nodeWalk(Q context, Q origin, int steps){
		AtlasSet<GraphElement> fromNodes = origin.eval().nodes();
		AtlasSet<GraphElement> reachedNodes = new AtlasHashSet<GraphElement>(fromNodes);
		AtlasSet<GraphElement> reachedEdges = new AtlasHashSet<GraphElement>(origin.eval().edges());
		
		nodeWalk(context.eval(), fromNodes, reachedNodes, reachedEdges, steps);
		
		return Common.toQ(new UncheckedGraph(reachedNodes, reachedEdges));
	}

	private static void nodeWalk(Graph context,
			AtlasSet<GraphElement> fromNodes,
			AtlasSet<GraphElement> reachedNodes,
			AtlasSet<GraphElement> reachedEdges,
			int steps) {
		for (GraphElement fromNode : fromNodes) nodeWalk(context, fromNode, reachedNodes, reachedEdges, steps);
	}

	private static void nodeWalk(Graph context, GraphElement fromNode, AtlasSet<GraphElement> reachedNodes,
			AtlasSet<GraphElement> reachedEdges, int steps) {
		reachedNodes.add(fromNode);
		
		if(steps == 0) return;

		AtlasSet<GraphElement> nextEdges = new UnionSet<GraphElement>(
				context.edges(fromNode, NodeDirection.IN),
				context.edges(fromNode, NodeDirection.OUT));
		
		for (GraphElement edge : nextEdges) {
			reachedEdges.add(edge);
			
			GraphElement n = edge.getNode(EdgeDirection.FROM);
			if (!reachedNodes.contains(n)) nodeWalk(context, n, reachedNodes, reachedEdges, steps-1);
			n = edge.getNode(EdgeDirection.TO);
			if (!reachedNodes.contains(n)) nodeWalk(context, n, reachedNodes, reachedEdges, steps-1);
		}
	}
}