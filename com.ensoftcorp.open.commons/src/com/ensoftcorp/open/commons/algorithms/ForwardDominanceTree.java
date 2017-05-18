package com.ensoftcorp.open.commons.algorithms;

import java.util.Map.Entry;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasEdgeHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.DominanceAnalysis.Multimap;

public class ForwardDominanceTree {

	/**
	 * Used to tag the edges that immediately forward dominate (post-dominate) a node
	 */
	public static final String IMMEDIATE_FORWARD_DOMINANCE_EDGE = "ifdom";
	
	private UniqueEntryExitGraph graph;
	
	/** 
	 * @param cfg a ControlFlowGraph (may include ExceptionalControlFlow_Edges)
	 */
	public ForwardDominanceTree(UniqueEntryExitGraph graph) {
		this.graph = graph;
	}
	
	public Graph getForwardDominanceTree(){
		DominanceAnalysis dominanceAnalysis = new DominanceAnalysis(graph, true);
		Multimap<Node> dominanceTree = dominanceAnalysis.getDominatorTree();
		AtlasSet<Edge> dominanceEdges = new AtlasEdgeHashSet();
		for(Entry<Node, Set<Node>> entry : dominanceTree.entrySet()){
			Node fromNode = entry.getKey();
			for(Node toNode : entry.getValue()){
				Q forwardDominanceEdges = Common.universe().edgesTaggedWithAny(IMMEDIATE_FORWARD_DOMINANCE_EDGE);
				Edge forwardDominanceEdge = forwardDominanceEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
				if(forwardDominanceEdge == null){
					forwardDominanceEdge = Graph.U.createEdge(fromNode, toNode);
					forwardDominanceEdge.tag(IMMEDIATE_FORWARD_DOMINANCE_EDGE);
					forwardDominanceEdge.putAttr(XCSG.name, IMMEDIATE_FORWARD_DOMINANCE_EDGE);
				}
				dominanceEdges.add(forwardDominanceEdge);
			}
		}
		return Common.toQ(dominanceEdges).eval();
	}

}
