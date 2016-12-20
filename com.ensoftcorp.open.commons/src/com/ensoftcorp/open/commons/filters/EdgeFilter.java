package com.ensoftcorp.open.commons.filters;

import com.ensoftcorp.atlas.core.query.Q;

public abstract class EdgeFilter extends Filter {

	protected EdgeFilter(Q input) {
		super(input);
	}

	/**
	 * Node filters do not operate on nodes
	 * @return
	 */
	protected String[] getSupportedNodeTags() {
		return NOTHING;
	}
	
	/**
	 * Returns the filtered result. Note that this method enforces
	 * that the result must be a subset of the original input and retains 
	 * only edges.
	 * @return
	 */
	public Q getFilteredResult(){
		return filter(getSupportedInput(input)).intersection(input).retainEdges();
	}
	
	@Override
	public String toString(){
		Long e1 = input.eval().edges().size();
		String e1s = e1 > 1 ? "s" : "";
		Long e2 = getFilteredResult().eval().edges().size();
		String e2s = e2 > 1 ? "s" : "";
		return "[" + e1 + " edge" + e1s + "] -> " + getName() + " -> [" + e2 + " edge" + e2s + "]";
	}
}
