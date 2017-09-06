package com.ensoftcorp.open.commons.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/**
 * Filters nodes based on how whether or not they are children of exceptional try blocks
 * 
 * @author Ben Holland
 */
public class ExceptionalTryChildFilter extends NodeFilter {

	private static final String EXCLUDE_MATCHES = "EXCLUDE_MATCHES";

	public ExceptionalTryChildFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only nodes that are not exceptional try children blocks.");
	}
	
	@Override
	public String getName() {
		return "Try Block Children";
	}

	@Override
	public String getDescription() {
		return "Filters nodes based on how whether or not they are children of exceptional try blocks.";
	}

	@Override
	protected Q filterInput(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filterInput(input, parameters);

		Q tryBlockContents = CommonQueries.localDeclarations(Common.universe().nodes(XCSG.TryBlock));
		
		// find exceptional try children
		Q children = tryBlockContents.intersection(input);
		
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
