package com.ensoftcorp.open.commons.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Filters nodes based on how whether or not they are children of loops
 * 
 * @author Ben Holland
 */
public class LoopChildFilter extends NodeFilter {

	private static final String EXCLUDE_MATCHES = "EXCLUDE_MATCHES";

	public LoopChildFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only nodes that are not loop children.");
	}
	
	@Override
	public String getName() {
		return "Loop Children";
	}

	@Override
	public String getDescription() {
		return "Filters nodes based on how whether or not they are children of loops.";
	}

	@Override
	protected Q filterInput(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filterInput(input, parameters);
		
		// get the corresponding control flow nodes for the input
		Q inputCFNodes = input.nodes(XCSG.ControlFlow_Node);
		inputCFNodes = inputCFNodes.union(input.nodes(XCSG.DataFlow_Node).parent().nodes(XCSG.ControlFlow_Node));
		
		// loop children are connected by a loop child edge from the loop header to the control flow node
		Q loopChildEdges = Common.universe().edges(XCSG.LoopChild);
		Q children = loopChildEdges.reverseStep(inputCFNodes).retainEdges().leaves();
		
		// include data flow nodes
		children = children.union(children.children().nodes(XCSG.DataFlow_Node));
		
		// restrict the children to a subset of the original input
		children = children.intersection(input);
		
		if(isFlagSet(EXCLUDE_MATCHES, parameters)){
			return input.difference(children);
		} else {
			return children;
		}
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[]{ XCSG.ControlFlow_Node, XCSG.DataFlow_Node };
	}

}
