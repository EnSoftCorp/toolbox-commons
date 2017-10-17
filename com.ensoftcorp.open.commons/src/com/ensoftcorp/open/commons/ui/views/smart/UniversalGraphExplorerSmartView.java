package com.ensoftcorp.open.commons.ui.views.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.markup.MarkupFromH;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IExplorableScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.scripts.util.SimpleScriptUtil;
import com.ensoftcorp.atlas.ui.selection.event.FrontierEdgeExploreEvent;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;

public class UniversalGraphExplorerSmartView extends FilteringAtlasSmartViewScript implements IResizableScript, IExplorableScript {

	@Override
	public String getTitle() {
		return "Universal Graph Explorer";
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
	public FrontierStyledResult explore(FrontierEdgeExploreEvent event, FrontierStyledResult oldResult) {
		return SimpleScriptUtil.explore(this, event, oldResult);
	}

	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		Q filteredSelection = filter(event.getSelection());

		if(filteredSelection.eval().nodes().isEmpty()){
			return null;
		}
		
		Q origin = filteredSelection;
		
		// highlight the origin
		Highlighter h = new Highlighter();
		h.highlight(origin, Color.CYAN);
		
		// calculate the complete result
		Q fullForward = Common.universe().forward(origin);
		Q fullReverse = Common.universe().reverse(origin);
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
	protected StyledResult selectionChanged(IAtlasSelectionEvent event, Q filteredSelection) {
		return null;
	}
}