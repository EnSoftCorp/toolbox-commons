package com.ensoftcorp.open.commons.analyzers;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/**
 * An analyzer for dead code
 * 
 * @author Ben Holland
 */
public class DeadCode extends Property {

	public static final String RESULT_PREFIX = "Dead Code (%s statements in %s): ";
	
	@Override
	public String getName() {
		return "Dead Code";
	}

	@Override
	public String getDescription() {
		return "Detects dead code statements in function control flow graphs.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"Dead code is detectable by identifying control flow graph roots that are not the expected control flow roots."};
	}

	@Override
	public List<Result> getResults(Q context) {
		Q functions = context.nodesTaggedWithAny(XCSG.Function);
		LinkedList<Result> results = new LinkedList<Result>();
		for(Node function : functions.eval().nodes()){
			Q cfg = CommonQueries.excfg(function);
			Q deadCode = cfg.roots().difference(cfg.nodes(XCSG.controlFlowRoot));
			long deadCodeRoots = deadCode.eval().nodes().size();
			if(deadCodeRoots > 0) {
				String name = (String.format(RESULT_PREFIX, ("" + deadCodeRoots), CommonQueries.getQualifiedFunctionName(function)) + deadCodeRoots);
				Result result = new Result(name, deadCode);
				result.setData(deadCodeRoots);
				results.add(result);
			}
		}
		return results;
	}
	
	/**
	 * Sort results based on number dead code statements (more statements first)
	 * @return
	 */
	public Comparator<Result> getResultOrder(){
		return new Comparator<Result>(){
			@Override
			public int compare(Result o1, Result o2) {
				long c1 = (long) o1.getData();
				long c2 = (long) o2.getData();
				int order = Long.compare(c1, c2);
				return order != 0 ? -order : order; // reverse sort
			}
		};
	}

}
