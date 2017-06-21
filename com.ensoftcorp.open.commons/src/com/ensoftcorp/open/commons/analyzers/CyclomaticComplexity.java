package com.ensoftcorp.open.commons.analyzers;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

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
			Integer metric = cyclomaticComplexity(function);
			Result result = new Result((RESULT_PREFIX + metric), Common.toQ(function));
			result.setData(metric);
			results.add(result);
		}
		return results;
	}

	private int cyclomaticComplexity(Node method) {
		Q controlFlowEdges = Common.universe().edgesTaggedWithAny(XCSG.ControlFlow_Edge);
		Q declarations = Common.toQ(method).contained();
		Q controlFlowRoot = declarations.nodesTaggedWithAny(XCSG.controlFlowRoot);
		Graph methodCFG = controlFlowEdges.forward(controlFlowRoot).eval();
		long edgesCount = methodCFG.edges().size();
		long nodesConut = methodCFG.nodes().size();
		long numExitPoints = methodCFG.nodes().taggedWithAny(XCSG.controlFlowExitPoint).size();
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
