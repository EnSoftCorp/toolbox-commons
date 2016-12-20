package com.ensoftcorp.open.commons.filters;

import com.ensoftcorp.atlas.core.query.Q;

public abstract class NodeFilter extends Filter {

	protected NodeFilter(Q input) {
		super(input);
	}

	/**
	 * Node filters do not operate on edges
	 * @return
	 */
	protected String[] getSupportedEdgeTags() {
		return NOTHING;
	}
	
	/**
	 * Returns the filtered result. Note that this method enforces
	 * that the result must be a subset of the original input and retains 
	 * only nodes.
	 * @return
	 */
	public Q getFilteredResult(){
		return filter(getSupportedInput(input)).intersection(input).retainNodes();
	}
	
	@Override
	public String toString(){
		Long n1 = input.eval().nodes().size();
		String n1s = n1 > 1 ? "s" : "";
		Long n2 = getFilteredResult().eval().nodes().size();
		String n2s = n2 > 1 ? "s" : "";
		return "[" + n1 + " node" + n1s + "] -> " + getName() + " -> [" + n2 + " node" + n2s + "]";
	}
}
