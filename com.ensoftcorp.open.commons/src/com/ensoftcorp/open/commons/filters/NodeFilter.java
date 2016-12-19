package com.ensoftcorp.open.commons.filters;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;

public abstract class NodeFilter extends Filter {

	public NodeFilter(Q input) {
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
	 * Returns the evaluated filtered result. Note that this method enforces
	 * that the result must be a subset of the original input and retains 
	 * only nodes.
	 * @return
	 */
	public Graph getFilteredResult(){
		return filter(getSupportedInput(input)).intersection(input).retainNodes().eval();
	}
	
}
