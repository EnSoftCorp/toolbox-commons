package com.ensoftcorp.open.commons.algorithms;

import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.DominanceAnalysis.Multimap;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;
import com.ensoftcorp.open.commons.xcsg.XCSG_Extension;

public class ForwardDominanceTree extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the PDG codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.dominance";
	
	/**
	 * Used to tag the edges that immediately forward dominate (post-dominate) a node
	 */
	@XCSG_Extension
	public static final String IMMEDIATE_FORWARD_DOMINANCE_EDGE = "ifdom";
	
	public ForwardDominanceTree() {}
	
	public Graph getForwardDominanceTree(){
		Q forwardDominanceEdges = Common.universe().edges(IMMEDIATE_FORWARD_DOMINANCE_EDGE).retainEdges();
		return forwardDominanceEdges.eval();
	}

	@Override
	public String getDisplayName() {
		return "Computing Control Flow Graph Dominantor Trees";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{};
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		if(CommonsPreferences.isComputeControlFlowGraphDominanceTreesEnabled() || CommonsPreferences.isComputeExceptionalControlFlowGraphDominanceTreesEnabled()){
			Log.info("Computing Control Flow Graph Dominantor Trees");
			for(Node function : Common.universe().nodes(XCSG.Function).eval().nodes()){
				Q cfg;
				if(CommonsPreferences.isComputeControlFlowGraphDominanceTreesEnabled()){
					cfg = CommonQueries.cfg(function);
				} else {
					cfg = CommonQueries.excfg(function);
				}
				UniqueEntryExitGraph uexg = new UniqueEntryExitControlFlowGraph(cfg.eval(), cfg.nodes(XCSG.controlFlowRoot).eval().nodes(), cfg.nodes(XCSG.controlFlowExitPoint).eval().nodes());
				computeDominatorTree(uexg);
				if(monitor.isCanceled()){
					Log.warning("Cancelled: Computing Control Flow Graph Dominantor Trees");
					break;
				}
			}
		}
	}

	public static Graph computeDominatorTree(UniqueEntryExitGraph ucfg) {
		DominanceAnalysis dominanceAnalysis = new DominanceAnalysis(ucfg, true);
		Multimap<Node> dominanceTree = dominanceAnalysis.getDominatorTree();
		AtlasSet<Edge> dominanceEdges = new AtlasHashSet<Edge>();
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
