package com.ensoftcorp.open.commons.utilities.selection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.indexing.IIndexListener;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.ui.selection.IAtlasSelectionListener;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.log.Log;

public abstract class GraphSelectionListenerView extends ViewPart {

	private boolean indexExists = IndexingUtil.indexExists();
	private Graph selection = null;
	private IIndexListener indexListener = null;
	private IAtlasSelectionListener selectionListener = null;
	private Shell shell = null;
	private boolean selectionListenerEnabled = true;
	private IAtlasSelectionEvent pendingEvent = null;
	
	private SelectionJob selectionJob = new SelectionJob("Graph Selection Listener");
	
	/**
	 * Handles delayed processing of the selection event
	 */
	private class SelectionJob extends UIJob {

		public SelectionJob(String name) {
			super(name);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			IAtlasSelectionEvent event = pendingEvent;
			pendingEvent = null;
			if (event == null) 
				return Status.OK_STATUS;

			try {
				selection = event.getSelection().eval();
			} catch (Exception e){
				selection = null;
			}
			// ignore selections originating from the same part
			if (event.getContributingPart() == GraphSelectionListenerView.this) {
				return Status.OK_STATUS;
			}
			selectionChangedHandler();

			return Status.OK_STATUS;
		}
		
	}
	
	/**
	 * This method should be invoked at the end of the ViewPart's
	 * createPartControl(Composite parent) method. Listener's will 
	 * automatically be cleaned up when the view is disposed.
	 */
	public void registerGraphHandlers(){
		shell = getSite().getShell();
		
		// index listener disables selection listener on index change
		indexListener = new IIndexListener(){
			@Override
			public void indexOperationCancelled(IndexOperation op) {}

			@Override
			public void indexOperationComplete(IndexOperation op) {
				indexExists = true;
				indexBecameAccessibleHandler();
			}

			@Override
			public void indexOperationError(IndexOperation op, Throwable error) {}

			@Override
			public void indexOperationScheduled(IndexOperation op) {}

			@Override
			public void indexOperationStarted(IndexOperation op) {
				indexExists = false;
				selection = null;
				indexBecameUnaccessibleHandler();
			}
		};
		
		// add the index listener
		IndexingUtil.addListener(indexListener);
				
		// setup the Atlas selection event listener
		selectionListener = new IAtlasSelectionListener(){
			@Override
			public void selectionChanged(IAtlasSelectionEvent atlasSelection) {
				pendingEvent = atlasSelection;
				// Graph selections currently fire for every intermediate selection, 
				// e.g. when everything is selected and then deselected, events are
				// fired for n, n-1, n-2, ..., 0 nodes.  This can overwhelm a listener
				// and it isn't strictly necessary to respond to all events, just the
				// last one.
				// This delays for another 0.1 seconds before trying to process the last event. 
				selectionJob.schedule(100);
			}			
		};
		
		// add the selection listener
		SelectionUtil.addSelectionListener(selectionListener);
	}
	
	/**
	 * Returns true if the graph selection listener is enabled, false otherwise
	 * @return
	 */
	public boolean isGraphSelectionListenerEnabled(){
		return selectionListenerEnabled;
	}
	
	/**
	 * Enables graph selection listener
	 */
	public void enableGraphSelectionListener(){
		selectionListenerEnabled = true;
	}
	
	/**
	 * Disables graph selection listener
	 */
	public void disableGraphSelectionListener(){
		selectionListenerEnabled = false;
	}
	
	/**
	 * Toggles the graph selection listener state
	 * from enabled to disabled or vice versa.
	 */
	public void toggleGraphSelectionListener(){
		selectionListenerEnabled = !selectionListenerEnabled;	
	}
	
	private void selectionChangedHandler(){
		if(indexExists && selection != null){
			if(selectionListenerEnabled){
				try {
					if(shell != null && !shell.isDisposed()){
						shell.getDisplay().syncExec(new Runnable(){
							@Override
							public void run() {
								selectionChanged(selection);
							}
						});
					}
				} catch (Throwable t){
					Log.error("Error handling selection changed event.", t);
				}
			}
		}
	}
	
	/**
	 * This method can be overridden to handle selection changed events
	 * Note: This event is synchronized with the view's UI thread.
	 */
	public abstract void selectionChanged(Graph selection);
	
	private void indexBecameUnaccessibleHandler(){
		try {
			if(shell != null && !shell.isDisposed()){
				shell.getDisplay().syncExec(new Runnable(){
					@Override
					public void run() {
						indexBecameUnaccessible();
					}
				});
			}
		} catch (Throwable t){
			Log.error("Error handling index became unaccessible event.", t);
		}
	}
	
	/**
	 * This method can be overridden to handle index changed events.
	 * Note: This event is synchronized with the view's UI thread.
	 */
	public abstract void indexBecameUnaccessible();
	
	private void indexBecameAccessibleHandler(){
		try {
			if(shell != null && !shell.isDisposed()){
				shell.getDisplay().syncExec(new Runnable(){
					@Override
					public void run() {
						indexBecameAccessible();
					}
				});
			}
		} catch (Throwable t){
			Log.error("Error handling index became accessible event.", t);
		}
	}
	
	/**
	 * This method can be overridden to handle index changed events
	 * Note: This event is synchronized with the view's UI thread.
	 */
	public abstract void indexBecameAccessible();
	
	/**
	 * Returns true if an index currently exists
	 * @return
	 */
	public boolean indexExists(){
		return indexExists;
	}
	
	/**
	 * Returns the current selection or null if the index does not exist
	 * @return
	 */
	public Q getSelection(){
		if(indexExists && selection != null){
			return Common.toQ(selection);
		} else {
			return null;
		}
	}
	
	@Override
	public void dispose(){
		// need to clean up listeners
		if(selectionListener != null){
			SelectionUtil.removeSelectionListener(selectionListener);
		}
		if(indexListener != null){
			IndexingUtil.removeListener(indexListener);
		}
		super.dispose();
	}

}
