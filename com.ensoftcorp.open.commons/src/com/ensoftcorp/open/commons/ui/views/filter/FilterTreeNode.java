package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.List;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.open.commons.filters.Filter;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;

public interface FilterTreeNode {

	public String getName();
	
	public FilterTreeNode getParent();
	
	public List<FilterTreeNode> getChildren();
	
	public void addChild(Filter filter, Map<String,Object> filterParameters) throws InvalidFilterParameterException;
	
	public Graph getOutput();
	
	public List<Filter> getApplicableFilters();
	
	public boolean isExpanded();
	
	public void setExpanded(boolean expanded);

}
