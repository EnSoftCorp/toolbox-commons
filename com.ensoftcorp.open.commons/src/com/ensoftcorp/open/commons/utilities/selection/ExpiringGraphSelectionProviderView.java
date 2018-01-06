package com.ensoftcorp.open.commons.utilities.selection;

import org.eclipse.ui.IWorkbenchPartSite;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.open.commons.log.Log;

public abstract class ExpiringGraphSelectionProviderView extends GraphSelectionListenerView {

	private ExpiringGraphSelectionProvider graphSelectionProvider = new ExpiringGraphSelectionProvider();
	
	/**
	 * This method should be invoked at the end of the ViewPart's
	 * createPartControl(Composite parent) method. Listener's will 
	 * automatically be cleaned up when the view is disposed.
	 */
	@Override
	public void registerGraphHandlers(){
		super.registerGraphHandlers();
		
		IWorkbenchPartSite site = getSite();
		if(site != null){
			site.setSelectionProvider(graphSelectionProvider);
		} else {
			String message = "Unable to register graph selection provider";
			Log.warning(message, new RuntimeException(message));
		}
	}
	
	/**
	 * Enables graph selection providers
	 */
	public void enableGraphSelectionProvider(){
		graphSelectionProvider.enable();
	}
	
	/**
	 * Disables graph selection providers
	 */
	public void disableGraphSelectionProvider(){
		graphSelectionProvider.disable();
	}
	
	/**
	 * Returns true if the graph selection provider is enabled, false otherwise
	 */
	public boolean isGraphSelectionProviderEnabled(){
		return graphSelectionProvider.isEnabled();		
	}
	
	/**
	 * Toggles the graph selection provider state
	 * from enabled to disabled or vice versa.
	 */
	public void toggleGraphSelectionProvider(){
		if(graphSelectionProvider.isEnabled()){
			graphSelectionProvider.disable();
		} else {
			graphSelectionProvider.enable();
		}
	}
	
	/**
	 * If an index exists and a selection has previously been made this replays the last selection event
	 */
	public void refreshSelection(){
		if(indexExists()){
			Q selection = getSelection();
			if(selection != null){
				setSelection(selection);
			} else {
				setSelection(Query.empty());
			}
		}
	}
	
	/**
	 * If an index exists then the given selection event will be fired
	 * @param selection
	 */
	public void setSelection(Q selection){
		if(indexExists()){
			if(selection != null){
				graphSelectionProvider.setSelection(selection);
			}
		}
	}
}
