package com.ensoftcorp.open.commons.utilities.selection;

import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.ViewPart;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.ui.selection.IAtlasSelectionListener;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.log.Log;

public abstract class GraphSelectionProviderView extends ViewPart {

	private GraphSelectionProvider graphSelectionProvider = new GraphSelectionProvider();
	private Graph selection;
	
	public void registerGraphSelectionProvider(){
		IWorkbenchPartSite site = getSite();
		if(site != null){
			site.setSelectionProvider(graphSelectionProvider);
		} else {
			String message = "Unable to register graph selection provider";
			Log.warning(message, new RuntimeException(message));
		}
		
		// setup the Atlas selection event listener
		IAtlasSelectionListener selectionListener = new IAtlasSelectionListener(){
			@Override
			public void selectionChanged(IAtlasSelectionEvent atlasSelection) {
				try {
					selection = atlasSelection.getSelection().eval();
				} catch (Exception e){
					selection = Common.empty().eval();
				}
			}				
		};
		
		// add the selection listener
		SelectionUtil.addSelectionListener(selectionListener);
	}
	
	public void enableGraphSelectionProvider(){
		graphSelectionProvider.enable();
	}
	
	public void disableGraphSelectionProvider(){
		graphSelectionProvider.disable();
	}
	
	public Q getSelection(){
		return Common.toQ(selection);
	}
	
	public void refreshSelection(){
		setSelection(Common.toQ(selection));
	}
	
	public void setSelection(Q selection){
		graphSelectionProvider.setSelection(selection);
	}
}
