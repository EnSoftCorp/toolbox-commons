package com.ensoftcorp.open.commons.ui.views.codepainter.codepainters;

import java.util.LinkedList;
import java.util.List;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.codepainter.CodePainter;
import com.ensoftcorp.open.commons.codepainter.ColorPalette;

/**
 * A code painter that responds to all node selections and traverses all edges
 * in the universe
 * 
 * @author Ben Holland
 */
public class UniversalGraphExplorerCodePainter extends CodePainter {

	@Override
	public String getTitle() {
		return "Universal Graph Explorer";
	}
	
	@Override
	public String getCategory() {
		return "Basic";
	}
	
	@Override
	protected String[] getSupportedNodeTags() {
		return EVERYTHING;
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
	public UnstyledFrontierResult computeFrontierResult(Q filteredSelections, int reverse, int forward) {
		// graph is the entire universe
		Q graph = Common.universe();
		
		Q origin = filteredSelections;
		
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