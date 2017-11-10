package com.ensoftcorp.open.commons.ui.views.smart;

import java.util.HashSet;
import java.util.Set;

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
	private static Set<CodePainterSmartViewEventListener> listeners = new HashSet<CodePainterSmartViewEventListener>();
	
	/**
	 * A listener class to handle callbacks for smart view events
	 */
	public static interface CodePainterSmartViewEventListener {
		public void selectionChanged(IAtlasSelectionEvent event, int reverse, int forward);
		public void codePainterChanged(CodePainter codePainter);
	}
	
	public static void addListener(CodePainterSmartViewEventListener listener){
		listeners.add(listener);
	}
	
	public static void removeListener(CodePainterSmartViewEventListener listener){
		listeners.remove(listener);
	}
	
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
				notifyListenersCodePainterChanged();
				return true;
			} else if(CodePainterSmartView.codePainter == null){
				// first non-null assignment
				CodePainterSmartView.codePainter = codePainter;
				notifyListenersCodePainterChanged();
				return true;
			} else if(!CodePainterSmartView.codePainter.equals(codePainter)){
				// new non-equal assignment (code painter change)
				CodePainterSmartView.codePainter = codePainter;
				notifyListenersCodePainterChanged();
				return true;
			}
			
			// no change
			return false;
		}
	}

	private static void notifyListenersCodePainterChanged() {
		for(CodePainterSmartViewEventListener listener : listeners){
			listener.codePainterChanged(CodePainterSmartView.codePainter);
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
				return codePainter.getDefaultStepReverse();
			}
		}
		return 1;
	}

	@Override
	public final synchronized int getDefaultStepBottom() {
		synchronized (CodePainter.class){
			if(codePainter != null){
				return codePainter.getDefaultStepForward();
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
		FrontierStyledResult result = null;
		synchronized (CodePainter.class){
			if(codePainter != null){
				result = codePainter.evaluate(event, reverse, forward);
			}
		}
		for(CodePainterSmartViewEventListener listener : listeners){
			listener.selectionChanged(event, reverse, forward);
		}
		return result;
	}

	@Override
	protected final StyledResult selectionChanged(IAtlasSelectionEvent event, Q filteredSelection) {
		// this is dead code, just here to satisfy interfaces
		return null;
	}

}