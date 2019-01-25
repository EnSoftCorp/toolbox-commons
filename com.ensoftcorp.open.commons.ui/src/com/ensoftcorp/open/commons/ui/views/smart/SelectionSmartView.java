package com.ensoftcorp.open.commons.ui.views.smart;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;

public class SelectionSmartView extends FilteringAtlasSmartViewScript {

	@Override
	public String getTitle() {
		return "Selection"; // calling it the "Identity" transformation would be confusing w.r.t XCSG.Identity 
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
	protected StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
		StyledResult result = new StyledResult(filteredSelection);
		result.includeContainers(false);
		return result;
	}
}