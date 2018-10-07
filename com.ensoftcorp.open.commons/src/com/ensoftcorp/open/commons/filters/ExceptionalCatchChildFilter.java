package com.ensoftcorp.open.commons.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/**
 * Filters nodes based on how whether or not they are children of exceptional catch blocks
 * 
 * @author Ben Holland
 */
public class ExceptionalCatchChildFilter extends NodeFilter {

	private static final String EXCLUDE_MATCHES = "EXCLUDE_MATCHES";

	public ExceptionalCatchChildFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only nodes that are not exceptional catch block children.");
	}
	
	@Override
	public String getName() {
		return "Catch Block Children";
	}

	@Override
	public String getDescription() {
		return "Filters nodes based on how whether or not they are children of exceptional catch blocks.";
	}

	@Override
	protected Q filterInput(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filterInput(input, parameters);

		Q catchBlockContents = CommonQueries.localDeclarations(Query.universe().nodes(XCSG.CatchBlock));
		
		// find exceptional catch children
		Q children = catchBlockContents.intersection(input);
		
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
