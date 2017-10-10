package com.ensoftcorp.open.commons.ui.smart.replacements;

import java.awt.Color;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.markup.MarkupFromH;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IExplorableScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.scripts.util.SimpleScriptUtil;
import com.ensoftcorp.atlas.ui.selection.event.FrontierEdgeExploreEvent;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/**
 * A replacement Control Flow Smart view for Atlas default that allows users to select functions to view the entire function CFG
 * @author Ben Holland
 */
public class ReplacementControlFlowSmartView extends FilteringAtlasSmartViewScript implements IResizableScript, IExplorableScript {

	@Override
	public String getTitle() {
//		// after lots of poking around...the cleanest way to replace a smart view is to make a name collision
//		// reflection is too hacky and tends to be defeated by class loader issues
//		// Atlas canned (default) smart views are not registered under the Atlas smart view extension points
//		// so they cannot be programmatically removed from Eclipse registry
//		// Atlas adds canned scripts to a map then adds any custom scripts to the map, so making a collision
//		// will replace the entry in the map... this behavior is not officially defined, but works for our purposes
		
		// TODO: I thought this trick worked, but looks like its not now...or it did and then views got re-registered...
		
//		return SelectionScripts.TYPE.CONTROL_FLOW.getScript().getTitle();
		
		return "Control Flow Replacement";
	}
	
	@Override
	protected String[] getSupportedNodeTags() {
		return new String[]{ XCSG.DataFlow_Node, XCSG.ControlFlow_Node, XCSG.Function };
	}
	
	@Override
	protected String[] getSupportedEdgeTags() {
		return NOTHING;
	}

	@Override
	public int getDefaultStepTop() {
		return 1;
	}

	@Override
	public int getDefaultStepBottom() {
		return 1;
	}
	
	@Override
	public FrontierStyledResult explore(FrontierEdgeExploreEvent event, FrontierStyledResult oldResult) {
		return SimpleScriptUtil.explore(this, event, oldResult);
	}

	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		Q filteredSelection = filter(event.getSelection());

		if(filteredSelection.eval().nodes().isEmpty()){
			return null;
		}
		
		AtlasSet<Node> dataFlowNodes = filteredSelection.nodes(XCSG.DataFlow_Node).eval().nodes();
		AtlasSet<Node> correspondingDataFlowStatements = Common.toQ(dataFlowNodes).parent().nodes(XCSG.ControlFlow_Node).eval().nodes();
		AtlasSet<Node> functions = filteredSelection.nodes(XCSG.Function).eval().nodes();
		Q selectedStatements = filteredSelection.difference(Common.toQ(functions), Common.toQ(dataFlowNodes)).union(Common.toQ(correspondingDataFlowStatements));

		if(functions.isEmpty()){
			// just cfg nodes were selected
			Q containingFunctions = CommonQueries.getContainingFunctions(selectedStatements);
			Q cfgs = CommonQueries.cfg(containingFunctions);
			
			// highlight the origin
			Highlighter h = new Highlighter();
			h.highlight(selectedStatements, Color.CYAN);
			return computeFrontierResult(selectedStatements, cfgs, forward, reverse, h);
		} else {
			// a function was selected possibly along with cfg nodes
			Q containingFunctions = CommonQueries.getContainingFunctions(selectedStatements);
			Q cfgs = CommonQueries.cfg(containingFunctions);
			Q selectedFunctions = Common.toQ(functions);
			
			// remove any functions that are selected because callsites were selected
			Q selectedCallsites = selectedStatements.children().nodes(XCSG.CallSite);
			Q selectedCallsiteFunctions = CallSiteAnalysis.getTargets(selectedCallsites);
			selectedFunctions = selectedFunctions.difference(selectedCallsiteFunctions);
			
			// get the complete CFGs for any intentionally selected function
			Q selectedFunctionCFGs = CommonQueries.cfg(selectedFunctions);
			
			// highlight the selected functions
			// highlight the actually selected origin
			Highlighter h = new Highlighter();
			h.highlight(Common.toQ(functions), Color.CYAN); 
			h.highlight(selectedStatements, Color.CYAN);
			
			// just pretend the entire cfg was selected for selected functions
			selectedStatements = selectedStatements.union(selectedFunctionCFGs);
			return computeFrontierResult(selectedStatements, cfgs, forward, reverse, h);
		}
	}
	
	public FrontierStyledResult computeFrontierResult(Q origin, Q graph, int forward, int reverse, Highlighter h){
		// calculate the complete result
		Q fullForward = graph.forward(origin);
		Q fullReverse = graph.reverse(origin);
		Q completeResult = fullForward.union(fullReverse);
		
		// compute what to show for current steps
		Q f = origin.forwardStepOn(completeResult, forward);
		Q r = origin.reverseStepOn(completeResult, reverse);
		Q result = f.union(r).union(origin);
		
		// compute what is on the frontier
		Q frontierForward = origin.forwardStepOn(completeResult, forward+1);
		frontierForward = frontierForward.retainEdges().differenceEdges(result);
		Q frontierReverse = origin.reverseStepOn(completeResult, reverse+1);
		frontierReverse = frontierReverse.retainEdges().differenceEdges(result);

		// show the result
		return new com.ensoftcorp.atlas.core.script.FrontierStyledResult(result, frontierReverse, frontierForward, new MarkupFromH(h));
	}

	@Override
	protected StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
		return null;
	}
	
}