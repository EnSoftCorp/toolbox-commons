package com.ensoftcorp.open.commons.filters.examples;

import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.NodeFilter;

public class LoopNestingDepthFilter extends NodeFilter {

	private static final String DEPTH_GREATER_THAN = "DEPTH_GREATER_THAN";
	private static final String DEPTH_GREATER_THAN_EQUAL_TO = "DEPTH_GREATER_THAN_EQUAL_TO";
	private static final String DEPTH_LESS_THAN = "DEPTH_LESS_THAN";
	private static final String DEPTH_LESS_THAN_EQUAL_TO = "DEPTH_LESS_THAN_EQUAL_TO";

	protected LoopNestingDepthFilter() {
		this.addPossibleParameter(DEPTH_GREATER_THAN, Integer.class);
		this.addPossibleParameter(DEPTH_GREATER_THAN_EQUAL_TO, Integer.class);
		this.addPossibleParameter(DEPTH_LESS_THAN, Integer.class);
		this.addPossibleParameter(DEPTH_LESS_THAN_EQUAL_TO, Integer.class);
	}

	@Override
	public String getName() {
		return "Loop Nesting Depth";
	}

	@Override
	public String getDescription() {
		return "Filters loop headers based on thier local nesting depth.";
	}

	@Override
	public Q filter(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		Q loopChildEdges = Common.universe().edgesTaggedWithAny(XCSG.LoopChild).retainEdges();
		Q loopRoots = loopChildEdges.roots();
		Q loopHeaders = loopChildEdges.nodesTaggedWithAny("LOOP_HEADER" /*LoopAnalyzer.CFGNode.LOOP_HEADER*/);

		for (Node header : loopHeaders.eval().nodes()) {
			Q path = loopChildEdges.between(loopRoots, Common.toQ(header));
			long depth = path.eval().edges().size();
			
			boolean add = true;
			
			if(isParameterSet(DEPTH_GREATER_THAN, parameters)){
				int min = (Integer) getParameterValue(DEPTH_GREATER_THAN, parameters);
				if(depth <= min){
					add = false;
				}
			}
			
			if(isParameterSet(DEPTH_GREATER_THAN_EQUAL_TO, parameters)){
				int minEq = (Integer) getParameterValue(DEPTH_GREATER_THAN_EQUAL_TO, parameters);
				if(depth < minEq){
					add = false;
				}
			}
			
			if(isParameterSet(DEPTH_LESS_THAN, parameters)){
				int max = (Integer) getParameterValue(DEPTH_LESS_THAN, parameters);
				if(depth >= max){
					add = false;
				}
			}
			
			if(isParameterSet(DEPTH_LESS_THAN_EQUAL_TO, parameters)){
				int maxEq = (Integer) getParameterValue(DEPTH_LESS_THAN_EQUAL_TO, parameters);
				if(depth > maxEq){
					add = false;
				}
			}
			
			if(add){
				result.add(header);
			}
		}
		
		return Common.toQ(result);
	}

	@Override
	protected String[] getSupportedNodeTags() {
//		return new String[]{ "LOOP_HEADER" };
		return EVERYTHING;
	}

}
