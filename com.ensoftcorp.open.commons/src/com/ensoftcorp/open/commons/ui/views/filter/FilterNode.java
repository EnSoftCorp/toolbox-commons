package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.filters.Filter;
import com.ensoftcorp.open.commons.filters.Filters;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;

public class FilterNode implements FilterTreeNode {

	public LinkedList<FilterTreeNode> children = new LinkedList<FilterTreeNode>();
	private LinkedList<Filter> applicableFilters = new LinkedList<Filter>();
	
	private FilterNode parent;
	private Graph inputGraph;
	private Graph outputGraph;
	private Filter filter;
	private Map<String,Object> filterParameters;
	private String name;
	private boolean expanded;
	
	public FilterNode(FilterTreeNode parent, Q input, Filter filter, Map<String,Object> parameters, boolean expanded) throws InvalidFilterParameterException {
		this.inputGraph = input.eval();
		if(inputGraph.nodes().isEmpty()){
			throw new IllegalArgumentException("Input is empty.");
		}
		if(!filter.isApplicableTo(input)){
			throw new IllegalArgumentException("Filter is not applicable to input.");
		}
		this.filter = filter;
		this.name = filter.toString();
		this.expanded = expanded;
		this.filterParameters = parameters;
		this.outputGraph = filter.filter(input, parameters).eval();
		this.applicableFilters.addAll(Filters.getApplicableFilters(Common.toQ(outputGraph)));
		// remove filters that have been already applied
		applicableFilters.remove(filter);
		FilterTreeNode current = parent;
		while(current != null){
			if(current instanceof FilterNode){
				applicableFilters.remove(((FilterNode) current).getFilter());
			}
			current = current.getParent();
		}
	}
	
	@Override
	public FilterNode getParent(){
		return parent;
	}
	
	public Filter getFilter(){
		return filter;
	}
	
	public Map<String,Object> getFilterParameters(){
		return filterParameters;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public boolean isExpanded(){
		return expanded;
	}
	
	@Override
	public void setExpanded(boolean expanded){
		this.expanded = expanded;
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
		FilterNode other = (FilterNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public List<FilterTreeNode> getChildren() {
		return new LinkedList<FilterTreeNode>(children);
	}

	public Graph getInput(){
		return inputGraph;
	}
	
	@Override
	public Graph getOutput() {
		return outputGraph;
	}

	@Override
	public void addChild(Filter filter, Map<String, Object> filterParameters) throws InvalidFilterParameterException {
		children.add(new FilterNode(this, Common.toQ(getOutput()), filter, filterParameters, true));
		// newly added child should be expanded along with the parents to show the path to it
		FilterTreeNode parent = getParent();
		while(parent != null){
			parent.setExpanded(true);
			parent = parent.getParent();
		}
	}
	
}
