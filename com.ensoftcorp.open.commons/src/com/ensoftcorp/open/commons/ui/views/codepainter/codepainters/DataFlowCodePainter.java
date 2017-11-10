package com.ensoftcorp.open.commons.ui.views.codepainter.codepainters;

import java.util.LinkedList;
import java.util.List;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.codepainter.CodePainter;
import com.ensoftcorp.open.commons.codepainter.ColorPalette;

/**
 * A Data Flow code painter
 * 
 * @author Ben Holland
 */
public class DataFlowCodePainter extends CodePainter {

	public DataFlowCodePainter(){}
	
	@Override
	public String getTitle() {
		return "Data Flow";
	}
	
	@Override
	public String getDescription() {
		return "Explores data flow graphs.";
	}
	
	@Override
	public String getCategory() {
		return "Basic";
	}
	
	@Override
	public String[] getSupportedNodeTags() {
		return new String[]{ XCSG.DataFlow_Node, XCSG.Function };
	}
	
	@Override
	public String[] getSupportedEdgeTags() {
		return NOTHING;
	}

	@Override
	public int getDefaultStepReverse() {
		return 1;
	}

	@Override
	public int getDefaultStepForward() {
		return 1;
	}

	@Override
	public UnstyledFrontierResult computeFrontierResult(Q filteredSelections, int reverse, int forward) {
		if(filteredSelections.eval().nodes().isEmpty()){
			return null;
		}
		
		Q selectedDataFlowNodes = filteredSelections.nodes(XCSG.DataFlow_Node);
		Q selectedFunctions = filteredSelections.nodes(XCSG.Function);
		Q containingFunctions = CommonQueries.getContainingFunctions(selectedDataFlowNodes);
		
		if(CommonQueries.isEmpty(selectedFunctions)){
			// just data nodes were selected
			Q dfgs = CommonQueries.dfg(containingFunctions);

			UnstyledFrontierResult frontier = computeFrontierResult(selectedDataFlowNodes, dfgs, reverse, forward);
			
			// a selection could include a function, so explicitly include it in the result to be highlighted
			Q result = frontier.getResult().union(filteredSelections.nodes(XCSG.Function));
			return new UnstyledFrontierResult(result, frontier.getFrontierReverse(), frontier.getFrontierForward());
		} else {
			// a function was selected possibly along with dfg nodes
			Q dfgs = CommonQueries.dfg(containingFunctions);
			
			// remove any functions that are selected because callsites were selected
			Q selectedCallsites = selectedDataFlowNodes.nodes(XCSG.CallSite);
			Q selectedCallsiteFunctions = CallSiteAnalysis.getTargets(selectedCallsites);
			selectedFunctions = selectedFunctions.difference(selectedCallsiteFunctions);
			
			// get the complete DFGs for any intentionally selected function
			Q selectedFunctionDFGs = CommonQueries.dfg(selectedFunctions);
			
			// just pretend the entire cfg was selected for selected functions
			selectedDataFlowNodes = selectedDataFlowNodes.union(selectedFunctionDFGs);
			
			Q allDFGs = dfgs.union(selectedFunctionDFGs);
			
			UnstyledFrontierResult frontier = computeFrontierResult(selectedDataFlowNodes, allDFGs, reverse, forward);
			
			// a selection could include a function, so explicitly include it in the result to be highlighted
			Q result = frontier.getResult().union(filteredSelections.nodes(XCSG.Function));
			return new UnstyledFrontierResult(result, frontier.getFrontierReverse(), frontier.getFrontierForward());
		}
	}
	
	private UnstyledFrontierResult computeFrontierResult(Q origin, Q graph, int reverse, int forward){
		// compute what to show for current steps
		Q f = origin.forwardStepOn(graph, forward);
		Q r = origin.reverseStepOn(graph, reverse);
		Q result = f.union(r);
		
		// compute what is on the frontier
		Q frontierReverse = origin.reverseStepOn(graph, reverse+1);
		frontierReverse = frontierReverse.differenceEdges(result).retainEdges();
		Q frontierForward = origin.forwardStepOn(graph, forward+1);
		frontierForward = frontierForward.differenceEdges(result).retainEdges();
		
		// show the result
		return new UnstyledFrontierResult(result, frontierReverse, frontierForward);
	}

	@Override
	public List<ColorPalette> getDefaultColorPalettes() {
		return new LinkedList<ColorPalette>();
	}
	
}
