package com.ensoftcorp.open.commons.utilities.selection;

import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.ViewPart;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.commons.log.Log;

public abstract class GraphSelectionProviderView extends ViewPart {

	private GraphSelectionProvider graphSelectionProvider = new GraphSelectionProvider();

	public void registerGraphSelectionProvider(){
		IWorkbenchPartSite site = getSite();
		if(site != null){
			site.setSelectionProvider(graphSelectionProvider);
		} else {
			String message = "Unable to register graph selection provider";
			Log.warning(message, new RuntimeException(message));
		}
	}
	
	public void enableGraphSelectionProvider(){
		graphSelectionProvider.enable();
	}
	
	public void disableGraphSelectionProvider(){
		graphSelectionProvider.disable();
	}
	
	public void setSelection(Q selection){
		graphSelectionProvider.setSelection(selection);
	}
}
