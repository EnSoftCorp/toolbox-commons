package com.ensoftcorp.open.commons.ui.views.filter;

import com.ensoftcorp.open.commons.filters.Filter;

public class FilterState {

	protected Filter filter;
	protected boolean isExpanded;

	public FilterState(Filter filter, boolean isExpanded) {
		this.filter = filter;
		this.isExpanded = isExpanded;
	}
	
	public Filter getFilter() {
		return filter;
	}
	
	public boolean isExpanded() {
		return isExpanded;
	}
	
	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filter.getName() == null) ? 0 : filter.getName().hashCode());
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
		FilterState other = (FilterState) obj;
		if (filter.getName() == null) {
			if (other.filter.getName() != null)
				return false;
		} else if (!filter.getName().equals(other.filter.getName()))
			return false;
		return true;
	}
	
}
