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
		return new String[]{"Dead code is detectable by identifying control flow graph roots that are not the control flow root."};
	}

	private static class ResultData {
		String name;
		long deadCodeRoots;
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
				ResultData data = new ResultData();
				data.deadCodeRoots = deadCodeRoots;
				data.name = CommonQueries.getQualifiedFunctionName(function);
				String title = (String.format(RESULT_PREFIX, ("" + deadCodeRoots), data.name) + deadCodeRoots);
				Result result = new Result(title, deadCode);
				result.setData(data);
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
			public int compare(Result a, Result b) {
				ResultData aData = (ResultData) a.getData();
				ResultData bData = (ResultData) b.getData();
				int order = Long.compare(aData.deadCodeRoots, bData.deadCodeRoots);
				return order != 0 ? -order : aData.name.compareTo(bData.name); // reverse integer sort, secondary sort alphabetical
			}
		};
	}

}
