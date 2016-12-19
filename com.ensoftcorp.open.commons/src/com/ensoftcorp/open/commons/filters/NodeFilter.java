package com.ensoftcorp.open.commons.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;

public abstract class NodeFilter extends Filter {

	protected NodeFilter(Q input, Map<String,Object> parameters) {
		super(input, parameters);
	}
	
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
	
}
