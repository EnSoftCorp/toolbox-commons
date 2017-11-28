package com.ensoftcorp.open.commons.analyzers;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/**
 * An analyzer for cyclomatic complexity
 * 
 * @author Ben Holland
 */
public class CyclomaticComplexity extends Property {

	public static final String RESULT_PREFIX = "Complexity: ";
	
	@Override
	public String getName() {
		return "Cyclomatic Complexity";
	}

	@Override
	public String getDescription() {
		return "Calculates the cyclomatic complexity of methods.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"Complexity is defined as [Cyclomatic Complexity](https://en.wikipedia.org/wiki/Cyclomatic_complexity)."};
	}

	@Override
	public List<Result> getResults(Q context) {
		Q functions = context.nodesTaggedWithAny(XCSG.Function);
		LinkedList<Result> results = new LinkedList<Result>();
		for(Node function : functions.eval().nodes()){
			Integer metric = cyclomaticComplexity(function, false);
			Result result = new Result((RESULT_PREFIX + metric), Common.toQ(function));
			result.setData(metric);
			results.add(result);
		}
		return results;
	}

	/**
	 * Computes the cyclomatic complexity of a function as defined by
	 * https://en.wikipedia.org/wiki/Cyclomatic_complexity
	 * 
	 * @param function
	 * @param includeExceptionControlFlows If true exception control flow graph edges will be included in the result
	 * @return
	 */
	public static int cyclomaticComplexity(Node function, boolean includeExceptionControlFlows) {
		Graph cfg;
		if(includeExceptionControlFlows){
			cfg = CommonQueries.excfg(function).eval();
		} else {
			cfg = CommonQueries.cfg(function).eval();
		}
		long edgesCount = cfg.edges().size();
		long nodesConut = cfg.nodes().size();
		long numExitPoints = cfg.nodes().taggedWithAny(XCSG.controlFlowExitPoint).size();
		return (int) (edgesCount - nodesConut + 2 * numExitPoints);
	}
	
	/**
	 * Sort results based on complexity
	 * @return
	 */
	public Comparator<Result> getResultOrder(){
		return new Comparator<Result>(){
			@Override
			public int compare(Result o1, Result o2) {
				int c1 = (int) o1.getData();
				int c2 = (int) o2.getData();
				return Integer.compare(c1, c2);
			}
		};
	}

}
