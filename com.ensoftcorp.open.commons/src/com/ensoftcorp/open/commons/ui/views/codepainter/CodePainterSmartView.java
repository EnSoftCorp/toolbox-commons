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

public final class CodePainterSmartView extends FilteringAtlasSmartViewScript implements IResizableScript, IExplorableScript {

	private static CodePainter codePainter = null;
	
	/**
	 * Sets the active code painter. Setting code painter to null effectively
	 * disables code painter smart view.
	 * 
	 * @param codePainter
	 */
	public static final synchronized boolean setCodePainter(CodePainter codePainter){
		synchronized (CodePainter.class){
			if(codePainter == null){
				// assigning null again
				CodePainterSmartView.codePainter = null;
				return true;
			} else if(CodePainterSmartView.codePainter == null){
				// first non-null assignment
				CodePainterSmartView.codePainter = codePainter;
				return true;
			} else if(!CodePainterSmartView.codePainter.equals(codePainter)){
				// new non-equal assignment (code painter change)
				CodePainterSmartView.codePainter = codePainter;
				return true;
			}
			
			// no change
			return false;
		}
	}
	
	/**
	 * Returns the active code painter
	 * @return
	 */
	public static final synchronized CodePainter getCodePainter(){
		synchronized (CodePainter.class){
			return codePainter;
		}
	}
	
	@Override
	public final String getTitle() {
		return "Code Painter";
	}

	@Override
	protected final String[] getSupportedNodeTags() {
		return EVERYTHING;
	}
	
	@Override
	protected final String[] getSupportedEdgeTags() {
		return EVERYTHING;
	}

	@Override
	public final synchronized int getDefaultStepTop() {
		synchronized (CodePainter.class){
			if(codePainter != null){
				return codePainter.getDefaultStepTop();
			}
		}
		return 1;
	}

	@Override
	public final synchronized int getDefaultStepBottom() {
		synchronized (CodePainter.class){
			if(codePainter != null){
				return codePainter.getDefaultStepBottom();
			}
		}
		return 1;
	}
	
	@Override
	public final synchronized FrontierStyledResult explore(FrontierEdgeExploreEvent event, FrontierStyledResult oldResult) {
		synchronized (CodePainter.class){
			if(codePainter != null){
				return codePainter.explore(event, oldResult);
			}
		}
		return SimpleScriptUtil.explore(this, event, oldResult);
	}

	@Override
	public final synchronized FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		synchronized (CodePainter.class){
			if(codePainter != null){
				return codePainter.evaluate(event, reverse, forward);
			}
		}
		return null;
	}

	@Override
	protected final StyledResult selectionChanged(IAtlasSelectionEvent event, Q filteredSelection) {
		// this is dead code, just here to satisfy interfaces
		return null;
	}

}
