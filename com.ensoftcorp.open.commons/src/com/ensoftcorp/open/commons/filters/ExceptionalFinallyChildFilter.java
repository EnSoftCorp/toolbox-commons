package com.ensoftcorp.open.commons.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/**
 * Filters nodes based on how whether or not they are children of exceptional finally blocks
 * 
 * @author Ben Holland
 */
public class ExceptionalFinallyChildFilter extends NodeFilter {

	private static final String EXCLUDE_MATCHES = "EXCLUDE_MATCHES";

	public ExceptionalFinallyChildFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only nodes that are not exceptional finally children blocks.");
	}
	
	@Override
	public String getName() {
		return "Finally Block Children";
	}

	@Override
	public String getDescription() {
		return "Filters nodes based on how whether or not they are children of exceptional finally blocks.";
	}

	@Override
	public Q filter(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filter(input, parameters);

		Q finallyBlockContents = CommonQueries.localDeclarations(Common.universe().nodes(XCSG.FinallyBlock));
		
		// find exceptional finally children
		Q children = finallyBlockContents.intersection(input);
		
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
