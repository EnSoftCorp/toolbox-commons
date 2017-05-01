package com.ensoftcorp.open.commons.dashboard.properties;

import java.util.List;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.commons.analyzers.CyclomaticComplexity;

/**
 * An property work item wrapper for cyclomatic complexity
 * 
 * @author Ben Holland
 */
public class CyclomaticComplexityProperty extends Property {

	private CyclomaticComplexity analyzer;
	
	public CyclomaticComplexityProperty(){
		analyzer = new CyclomaticComplexity();
	}
	
	@Override
	public String getDescription() {
		return analyzer.getDescription();
	}

	@Override
	public List<Result> getResults(Q context) {
		return analyzer.getResults(context);
	}
	
}
