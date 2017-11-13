package com.ensoftcorp.open.commons.utilities.selection;

import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.ViewPart;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.indexing.IIndexListener;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.atlas.core.indexing.IIndexListener.IndexOperation;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.ui.selection.IAtlasSelectionListener;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.log.Log;

public abstract class GraphSelectionProviderView extends ViewPart {

	private GraphSelectionProvider graphSelectionProvider = new GraphSelectionProvider();
	private Graph selection = null;
	
	public void registerGraphSelectionProvider(){
		IWorkbenchPartSite site = getSite();
		if(site != null){
			site.setSelectionProvider(graphSelectionProvider);
		} else {
			String message = "Unable to register graph selection provider";
			Log.warning(message, new RuntimeException(message));
		}
		
		// add index listeners to disable selection provider on index change
		IndexingUtil.addListener(new IIndexListener(){
			@Override
			public void indexOperationCancelled(IndexOperation op) {}

			@Override
			public void indexOperationComplete(IndexOperation op) {
				graphSelectionProvider.enable();
			}

			@Override
			public void indexOperationError(IndexOperation op, Throwable error) {}

			@Override
			public void indexOperationScheduled(IndexOperation op) {}

			@Override
			public void indexOperationStarted(IndexOperation op) {
				selection = null;
				graphSelectionProvider.disable();
			}
		});
		
		// setup the Atlas selection event listener
		IAtlasSelectionListener selectionListener = new IAtlasSelectionListener(){
			@Override
			public void selectionChanged(IAtlasSelectionEvent atlasSelection) {
				try {
					selection = atlasSelection.getSelection().eval();
				} catch (Exception e){
					selection = null;
				}
				selectionChangedHandler();
			}			
		};
		
		// add the selection listener
		SelectionUtil.addSelectionListener(selectionListener);
	}
	
	private void selectionChangedHandler(){
		selectionChanged();
	}
	
	/**
	 * This method can be overridden to handle selection changed events
	 */
	public void selectionChanged(){}
	
	public void enableGraphSelectionProvider(){
		graphSelectionProvider.enable();
	}
	
	public void disableGraphSelectionProvider(){
		graphSelectionProvider.disable();
	}
	
	public Q getSelection(){
		if(selection != null){
			return Common.toQ(selection);
		} else {
			return Common.empty();
		}
	}
	
	public void refreshSelection(){
		if(selection != null){
			setSelection(Common.toQ(selection));
		} else {
			setSelection(Common.empty());
		}
	}
	
	public void setSelection(Q selection){
		graphSelectionProvider.setSelection(selection);
	}
}
