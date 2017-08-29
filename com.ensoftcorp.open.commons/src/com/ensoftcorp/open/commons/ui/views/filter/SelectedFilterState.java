package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.commons.filters.Filter;

public class SelectedFilterState extends FilterState {

	private Q rootset;
	private boolean isEnabled;
	
	public Map<String,Object> filterParameters;
	
	public SelectedFilterState(Filter filter, boolean isExpanded, Q rootset, boolean isEnabled) {
		super(filter, isExpanded);
		this.rootset = rootset;
		this.filterParameters = new HashMap<String,Object>();
		this.isEnabled = isEnabled;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
}
