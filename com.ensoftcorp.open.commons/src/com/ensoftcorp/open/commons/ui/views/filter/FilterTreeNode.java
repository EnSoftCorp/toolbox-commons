package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.LinkedList;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.filters.Filter;
import com.ensoftcorp.open.commons.filters.Filters;

public class FilterTreeNode {

	public LinkedList<FilterTreeNode> nodes = new LinkedList<FilterTreeNode>();
	private LinkedList<Filter> applicableFilters = new LinkedList<Filter>();
	
	private Graph graph;
	private Filter filter;
	private String name;
	private boolean expanded;
	
	public FilterTreeNode(Q input, Filter filter, boolean expanded){
		this.graph = input.eval();
		if(graph.nodes().isEmpty()){
			throw new IllegalArgumentException("Input is empty.");
		}
		if(!filter.isApplicableTo(input)){
			throw new IllegalArgumentException("Filter is not applicable to imput.");
		}
		this.filter = filter;
		this.name = filter.toString();
		this.expanded = expanded;
		this.applicableFilters.addAll(Filters.getApplicableFilters(input));
	}
	
	public Filter getFilter(){
		return filter;
	}
	
	public String getName(){
		return name;
	}
	
	public Q getInput(){
		return Common.toQ(graph);
	}
	
	public boolean isExpanded(){
		return expanded;
	}
	
	public LinkedList<Filter> getApplicableFilters(){
		return new LinkedList<Filter>(applicableFilters);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterTreeNode other = (FilterTreeNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
