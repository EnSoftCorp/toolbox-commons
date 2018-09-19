package com.ensoftcorp.open.commons.algorithms;
import java.util.HashMap;
import java.util.Map;
import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.UniqueEntryExitControlFlowGraph;
import com.ensoftcorp.open.commons.algorithms.UniqueEntryExitGraph;
import com.ensoftcorp.open.commons.algorithms.internal.DominatorTree;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;

/**
 * It uses immediate dominance (idom) analysis to detect Goto loop and add tag as Goto_Back_Edge to the edge which cause loop. 
 * It also gives the count of Goto loops.
 * 
 * @author Sharwan Ram
 */

public class GotoLoopDetection {
	public static String recoverGotoLoops()
	{
		return recoverGotoLoops(Common.universe());
	}
	public static String recoverGotoLoops(Q app)
	{
		String message = "Total number of Goto Loops = " ;
		long totalLoops = 0l;
		Q functions = app.contained().nodes(XCSG.Function);
		for(Node function: functions.eval().nodes())
		{
			AtlasSet<Edge> allEdges = new AtlasHashSet<Edge>();
			Q cfg = CommonQueries.cfg(Common.toQ(function));
			allEdges.addAll(cfg.eval().edges());
			Q labelNodesQ = cfg.contained().nodesTaggedWithAny("isLabel");
			AtlasSet<Node> labelNodes = labelNodesQ.eval().nodes();
		
			if(!CommonQueries.isEmpty(labelNodesQ))
			{
				Map<Node,Node> idom = getIDOM(Common.toQ(function));		
				for(Node label: labelNodes)
				{   
					Node idomOfLabel = idom.get(label); 
					Node gotoNode = null;
					Q labelQ = Common.toQ(label);
					AtlasSet<Node> labelPredecessors = cfg.predecessors(labelQ).eval().nodes();
				    for(Node predecessorNode:labelPredecessors) {
					     if( predecessorNode.getAttr(XCSG.name).toString().contains("goto"))
						   {
							 gotoNode = predecessorNode;
							  Q gotoNodeQ = Common.toQ(gotoNode);
							  Edge e = cfg.betweenStep(gotoNodeQ, labelQ).edges(XCSG.ControlFlow_Edge).eval().edges().one();
							  if(!idomOfLabel.getAttr(XCSG.name).toString().contains("goto"))
								{
								    e.tag("Goto_Back_Edge");
								    totalLoops++;
								}
							 break;
					       }
							   
						  }
				 
				}
				
			}
		}
		
        
		return message + totalLoops;
	
	}

	/**
	 * Returns an immediate dominator (idom) mapping in the control flow graph of the given function
	 * @param function
	 * @return Map which contains immediate dominator (idom) mapping, like:- (node, idom of node) 
	 */
	
	public static Map<Node,Node> getIDOM(Q function) {
		Q cfg;
		Map<Node,Node> idom = new HashMap<Node,Node>();
		if(CommonsPreferences.isComputeControlFlowGraphDominanceEnabled()){
			cfg = CommonQueries.cfg(function);
		} else {
			cfg = CommonQueries.excfg(function);
		}
		Graph g = cfg.eval();
		AtlasSet<Node> roots = cfg.nodes(XCSG.controlFlowRoot).eval().nodes();
		AtlasSet<Node> exits = cfg.nodes(XCSG.controlFlowExitPoint).eval().nodes();
		if(g.nodes().isEmpty() || roots.isEmpty() || exits.isEmpty()){
			// nothing to compute
			
		} else {
			try {
				UniqueEntryExitGraph uexg = new UniqueEntryExitControlFlowGraph(g, roots, exits, CommonsPreferences.isMasterEntryExitContainmentRelationshipsEnabled());
				DominatorTree dominatorTree = new DominatorTree(uexg);
				idom =dominatorTree.getIdoms();
			} catch (Exception e){
				Log.error("Error computing control flow graph dominance tree", e);
			}
		}
		return idom;
	}

}
