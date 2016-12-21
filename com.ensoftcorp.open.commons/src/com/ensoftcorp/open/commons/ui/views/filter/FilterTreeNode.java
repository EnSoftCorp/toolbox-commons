package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.open.commons.filters.Filter;

public interface FilterTreeNode {

	public String getName();
	
	public FilterTreeNode getParent();
	
	public List<FilterTreeNode> getChildren();
	
	public Graph getOutput();
	
	public List<Filter> getApplicableFilters();
	
	public boolean isExpanded();

}
