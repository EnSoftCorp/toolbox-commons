package com.ensoftcorp.open.commons.analyzers;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.StronglyConnectedComponents;

/**
 * An analyzer for recursive functions
 * 
 * @author Ben Holland
 * @author Jon Mathews
 */
public class RecursiveFunctions extends Property {

	public static final String RESULT_PREFIX = "Recursive Functions: ";
	
	@Override
	public String getName() {
		return "Recursive Functions";
	}

	@Override
	public String getDescription() {
		return "Detects direct and mutually recursive function calls.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"The CHA based call graph is assumed to be accurate."};
	}

	private static Q resolve(Q q) {
		return Query.resolve(null, q);
	}
	
	@Override
	public List<Result> getResults(Q context) {
		// TODO: make removal of Object member overrides optional
		// removing Object methods can improve the usability of these results for the general case
		Q overridesEdges = Query.universe().edges(XCSG.Overrides);
		Q equalsMethods = resolve(overridesEdges.reverse(Common.methodSelect("java.lang", "Object", "equals")).nodes(XCSG.Function));
		Q toStringMethods = resolve(overridesEdges.reverse(Common.methodSelect("java.lang", "Object", "toString")).nodes(XCSG.Function));
		Q hashCodeMethods = resolve(overridesEdges.reverse(Common.methodSelect("java.lang", "Object", "hashCode")).nodes(XCSG.Function));
		Q functions = resolve(context.nodes(XCSG.Function).difference(equalsMethods, toStringMethods, hashCodeMethods));
		
		Q callgraph = resolve(functions.induce(context.edges(XCSG.Call)));
		
		StronglyConnectedComponents adapter = new StronglyConnectedComponents(callgraph);

		LinkedList<Result> results = new LinkedList<Result>();
		for (AtlasSet<Node> scc : adapter.findSCCs()) {
			Q recursion = Common.toQ(scc).induce(callgraph);
			Graph recursionGraph = recursion.eval();
			if (recursionGraph.edges().size() > 0) {
				// SCC must have at least one edge to be recursive
				Result result = new Result((RESULT_PREFIX + recursionGraph.nodes().size()), Common.toQ(recursionGraph));
				result.setData(recursionGraph.nodes().size());
				results.add(result);
			}
		}
		
		return results;
	}

	// jgrapht library version
	public static Q getRecursiveMethods() {
		Q callgraph = resolve(Query.universe().nodes(XCSG.Function).induce(Query.universe().edges(XCSG.Call)));
		StronglyConnectedComponents adapter = new StronglyConnectedComponents(callgraph);
		AtlasSet<Node> recursionNodes = new AtlasHashSet<Node>();
		AtlasSet<Edge> recursionEdges = new AtlasHashSet<Edge>();
		for (AtlasSet<Node> scc : adapter.findSCCs()) {
			Q recursion = Common.toQ(scc).induce(callgraph);
			Graph recursionGraph = recursion.eval();
			if (recursionGraph.edges().size() > 0) {
				// SCC must have at least one edge to be recursive
				recursionNodes.addAll(recursionGraph.nodes());
				recursionEdges.addAll(recursionGraph.edges());
			}
		}
		Q recursiveMethods = Common.toQ(new UncheckedGraph(recursionNodes, recursionEdges));
		return recursiveMethods;
	}
		
		// native atlas version
//		public static Q getRecursiveMethods(){
//			Q callEdges = Query.universe().edges(XCSG.Call);
//			Q[] sccs = stronglyConnectedComponents(callEdges);
//			
////			JGraphTAdapter jgraphtAdapter = new JGraphTAdapter(methods.eval().nodes(), callEdges.eval().edges());
//			AtlasSet<Node> recursionNodes = new AtlasHashSet<Node>();
//			AtlasSet<Edge> recursionEdges = new AtlasHashSet<Edge>();
//			for(Q scc : sccs){
//				Q recursion = scc.retainEdges();
//				Graph recursionGraph = recursion.eval();
//				if(recursionGraph.edges().size() > 0){
//					recursionNodes.addAll(recursionGraph.nodes());
//					recursionEdges.addAll(recursionGraph.edges());
//				}
//			}
//			Q recursiveMethods = Common.toQ(recursionNodes).induce(Common.toQ(recursionEdges));
//			return recursiveMethods;
//		}
	
	/**
	 * Sort results based on complexity
	 * @return
	 */
	public Comparator<Result> getResultOrder(){
		return new Comparator<Result>(){
			@Override
			public int compare(Result o1, Result o2) {
				long r1 = (long) o1.getData();
				long r2 = (long) o2.getData();
				return Long.compare(r1, r2);
			}
		};
	}

}
