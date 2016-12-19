package com.ensoftcorp.open.commons.filters;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;

public abstract class EdgeFilter extends Filter {

	public EdgeFilter(Q input) {
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
	 * Returns the evaluated filtered result. Note that this method enforces
	 * that the result must be a subset of the original input and retains 
	 * only edges.
	 * @return
	 */
	public Graph getFilteredResult(){
		return filter(getSupportedInput(input)).intersection(input).retainEdges().eval();
	}
	
}
