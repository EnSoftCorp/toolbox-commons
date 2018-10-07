package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.filters.Filter;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;

public class SelectedFilterState extends FilterState {

	private Graph rootset;
	private Graph filteredRootset;
	private long nodeImpact = 0;
	private long edgeImpact = 0;
	private boolean isEnabled;
	
	public Map<String,Object> filterParameters = new HashMap<String,Object>();
	
	public SelectedFilterState(Filter filter, boolean isExpanded, Graph rootset, boolean isEnabled) {
		super(filter, isExpanded);
		this.rootset = rootset;
		this.isEnabled = isEnabled;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public long getNodeImpact() {
		return nodeImpact;
	}

	public long getEdgeImpact() {
		return edgeImpact;
	}
	
	public Graph getFilteredRootset(){
		return filteredRootset;
	}

	public void updateFilterResult() throws InvalidFilterParameterException {
		try {
			filteredRootset = filter.filter(Common.toQ(rootset), filterParameters).eval();
			nodeImpact = rootset.nodes().size() - filteredRootset.nodes().size();
			edgeImpact = rootset.edges().size() - filteredRootset.edges().size();
		} catch (InvalidFilterParameterException e) {
			nodeImpact = 0;
			edgeImpact = 0;
			throw e;
		}
	}
	
}
