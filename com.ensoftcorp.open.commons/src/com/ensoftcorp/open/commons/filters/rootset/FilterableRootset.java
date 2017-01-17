package com.ensoftcorp.open.commons.filters.rootset;

import com.ensoftcorp.atlas.core.query.Q;

/**
 * Filterable root sets are predefined sets that are commonly used as a starting
 * point for filtering experiments
 * 
 * @author Ben Holland
 */
public abstract class FilterableRootset {

	private Class<? extends FilterableRootset> filterableRootSetType = this.getClass();

	/**
	 * The display name of the filter
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * A short description of the filter
	 * 
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Returns the root set
	 * @return
	 */
	public abstract Q getRootSet();
	
	/**
	 * Returns the filter name
	 */
	@Override
	public String toString(){
		return getName();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filterableRootSetType == null) ? 0 : filterableRootSetType.hashCode());
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
		FilterableRootset other = (FilterableRootset) obj;
		if (filterableRootSetType == null) {
			if (other.filterableRootSetType != null)
				return false;
		} else if (!filterableRootSetType.equals(other.filterableRootSetType))
			return false;
		return true;
	}
}
