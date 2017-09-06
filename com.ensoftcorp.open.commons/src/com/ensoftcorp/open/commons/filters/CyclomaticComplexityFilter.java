package com.ensoftcorp.open.commons.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analyzers.Analyzer;
import com.ensoftcorp.open.commons.analyzers.Analyzer.Result;
import com.ensoftcorp.open.commons.analyzers.CyclomaticComplexity;

/**
 * Filters functions based on cyclomatic complexity
 * 
 * @author Ben Holland
 */
public class CyclomaticComplexityFilter extends NodeFilter {

	private static final String COMPLEXITY_GREATER_THAN = "COMPLEXITY_GREATER_THAN";
	private static final String COMPLEXITY_GREATER_THAN_EQUAL_TO = "COMPLEXITY_GREATER_THAN_EQUAL_TO";
	private static final String COMPLEXITY_LESS_THAN = "COMPLEXITY_LESS_THAN";
	private static final String COMPLEXITY_LESS_THAN_EQUAL_TO = "COMPLEXITY_LESS_THAN_EQUAL_TO";

	public CyclomaticComplexityFilter() {
		this.addPossibleParameter(COMPLEXITY_GREATER_THAN, Integer.class, false, "Filters functions with cyclomatic complexity less than or equal to the specified value");
		this.addPossibleParameter(COMPLEXITY_GREATER_THAN_EQUAL_TO, Integer.class, false, "Filters functions with cyclomatic complexity less than the specified value");
		this.addPossibleParameter(COMPLEXITY_LESS_THAN, Integer.class, false, "Filters functions with cyclomatic complexity greater than or equal to the specified value");
		this.addPossibleParameter(COMPLEXITY_LESS_THAN_EQUAL_TO, Integer.class, false, "Filters functions with cyclomatic complexity greater than the specified value");
		this.setMinimumNumberParametersRequired(1);
	}

	@Override
	public String getName() {
		return "Cyclomatic Complexity";
	}

	@Override
	public String getDescription() {
		return "Filters functions based on cyclomatic complexity.";
	}

	@Override
	protected Q filterInput(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filterInput(input, parameters);
		
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		
		Analyzer cyclomaticComplexity = new CyclomaticComplexity();
		List<Result> cyclomaticComplexityResults = cyclomaticComplexity.getResults(input);

		List<Result> complexities = new ArrayList<Result>(cyclomaticComplexityResults);
		Collections.sort(complexities, cyclomaticComplexity.getResultOrder());
		
		for (Result complexity : complexities) {
			Integer metric = (Integer) complexity.getData();
			
			boolean add = true;
			
			if(isParameterSet(COMPLEXITY_GREATER_THAN, parameters)){
				int min = (Integer) getParameterValue(COMPLEXITY_GREATER_THAN, parameters);
				if(metric <= min){
					add = false;
				}
			}
			
			if(isParameterSet(COMPLEXITY_GREATER_THAN_EQUAL_TO, parameters)){
				int minEq = (Integer) getParameterValue(COMPLEXITY_GREATER_THAN_EQUAL_TO, parameters);
				if(metric < minEq){
					add = false;
				}
			}
			
			if(isParameterSet(COMPLEXITY_LESS_THAN, parameters)){
				int max = (Integer) getParameterValue(COMPLEXITY_LESS_THAN, parameters);
				if(metric >= max){
					add = false;
				}
			}
			
			if(isParameterSet(COMPLEXITY_LESS_THAN_EQUAL_TO, parameters)){
				int maxEq = (Integer) getParameterValue(COMPLEXITY_LESS_THAN_EQUAL_TO, parameters);
				if(metric > maxEq){
					add = false;
				}
			}
			
			if(add){
				result.addAll(complexity.getQ().eval().nodes());
			}
		}
		
		return Common.toQ(result);
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[]{ XCSG.Function };
	}

}
