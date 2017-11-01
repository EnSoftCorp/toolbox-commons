package com.ensoftcorp.open.commons.ui.views.codepainter;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IExplorableScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.scripts.util.SimpleScriptUtil;
import com.ensoftcorp.atlas.ui.selection.event.FrontierEdgeExploreEvent;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.codepainter.CodePainter;

public class CodePainterSmartView extends FilteringAtlasSmartViewScript implements IResizableScript, IExplorableScript {

	private static CodePainter codePainter = null;
	
	public static void setCodePainter(CodePainter codePainter){
		CodePainterSmartView.codePainter = codePainter;
	}
	
	@Override
	public String getTitle() {
		return "Code Painter";
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return EVERYTHING;
	}
	
	@Override
	protected String[] getSupportedEdgeTags() {
		return EVERYTHING;
	}

	@Override
	public int getDefaultStepTop() {
		if(codePainter != null){
			return codePainter.getDefaultStepTop();
		}
		return 1;
	}

	@Override
	public int getDefaultStepBottom() {
		if(codePainter != null){
			return codePainter.getDefaultStepBottom();
		}
		return 1;
	}
	
	@Override
	public FrontierStyledResult explore(FrontierEdgeExploreEvent event, FrontierStyledResult oldResult) {
		if(codePainter != null){
			return codePainter.explore(event, oldResult);
		}
		return SimpleScriptUtil.explore(this, event, oldResult);
	}

	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		if(codePainter != null){
			return codePainter.evaluate(event, reverse, forward);
		}
		return null;
	}

	@Override
	protected StyledResult selectionChanged(IAtlasSelectionEvent event, Q filteredSelection) {
		// this is going to be dead code, so just returning null to preserve the selected graph
//		if(codePainter != null){
//			return codePainter.selectionChanged(event, filteredSelection);
//		}
		return null;
	}

}
