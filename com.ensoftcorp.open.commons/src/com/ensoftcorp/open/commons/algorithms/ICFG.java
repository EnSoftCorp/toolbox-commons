package com.ensoftcorp.open.commons.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CallSiteAnalysis;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.utilities.NodeSourceCorrespondenceSorter;
import com.ensoftcorp.open.commons.xcsg.XCSG_Extension;

public class ICFG {

	@XCSG_Extension
	public static String ICFGEdge = "ICFG.InterproceduralControlFlow_Edge";

	@XCSG_Extension
	public static String ICFGEntryEdge = "ICFG.InterproceduralControlFlowEntry_Edge";
	
	@XCSG_Extension
	public static String ICFGExitEdge = "ICFG.InterproceduralControlFlowExit_Edge";
	
	@XCSG_Extension
	public static String ICFGCallsiteAttribute = "ICFG.CallSite";
	
	private Node entryPointFunction;
	private AtlasSet<Node> icfgNodes = new AtlasHashSet<Node>();
	private AtlasSet<Edge> icfgEdges = new AtlasHashSet<Edge>();
	
	/**
	 * Returns the entry point function
	 * @return
	 */
	public Node getEntryPointFunction() {
		return entryPointFunction;
	}
	
	/**
	 * Returns the entry point function roots
	 * @return
	 */
	public Node getEntryPointFunctionRoots() {
		return CommonQueries.cfg(entryPointFunction).nodes(XCSG.controlFlowRoot).eval().nodes().one();
	}
	
	/**
	 * Returns the entry point function exits
	 * @return
	 */
	public AtlasSet<Node> getEntryPointFunctionExits() {
		return CommonQueries.cfg(entryPointFunction).nodes(XCSG.controlFlowExitPoint).eval().nodes();
	}
	
	/**
	 * Returns the ICFG
	 * @return
	 */
	public Q getICFG() {
		return Common.resolve(new NullProgressMonitor(), Common.toQ(new UncheckedGraph(icfgNodes, icfgEdges)));
	}
	
	/**
	 * Compute the ICFG given an entry point function and all functions expandable using the default call resolution strategy
	 * @param entryPointFunction
	 * @param functionsToExpand
	 */
	public ICFG(Node entryPointFunction) {
		this(entryPointFunction, Common.empty().eval().nodes());
	}
	
	/**
	 * Compute the ICFG given an entry point function and a set of functions to expand using the default call resolution strategy
	 * @param entryPointFunction
	 * @param functionsToExpand
	 */
	public ICFG(Node entryPointFunction, AtlasSet<Node> functionsToExpand) {
		this(entryPointFunction, functionsToExpand, new DefaultCallResolutionStrategy());
	}
	
	/**
	 * Compute the ICFG given an entry point function and a set of functions to expand as well as a call resolution strategy
	 * @param entryPointFunction The function to start the ICFG at
	 * @param functionsToExpand If this is empty then all functions with non-empty CFGs will be expanded
	 * @param callResolutionStrategy The call resolution strategy to use
	 */
	public ICFG(Node entryPointFunction, AtlasSet<Node> functionsToExpand, CallResolutionStrategy callResolutionStrategy) {
		// check argument
		if(entryPointFunction == null || !entryPointFunction.taggedWith(XCSG.Function)) {
			throw new IllegalArgumentException("Entry point function is invalid.");
		} 
		if(CommonQueries.isEmpty(Common.toQ(entryPointFunction).children().nodes(XCSG.ControlFlow_Node))) {
			throw new IllegalArgumentException("Entry point function cannot have an emtpy CFG.");
		}
		this.entryPointFunction = entryPointFunction;
		
		// check argument
		if(functionsToExpand == null) {
			throw new IllegalArgumentException("Function to expand is invalid.");
		} else {
			for(Node function : functionsToExpand) {
				if(!function.taggedWith(XCSG.Function)) {
					throw new IllegalArgumentException("Function to expand is invalid.");
				}
			}
		}
		
		// check argument
		if(callResolutionStrategy == null) {
			throw new IllegalArgumentException("Call resolution strategy is invalid.");
		}
		
		// step 1) use call summaries (i.e. call graph) to scan ahead and find all functions in the ICFG
		//         we can do this because if a call summary edge exists from one function to another
		//         it implies there must be a callsite that could resolve to that target inside the 
		//         calling functions CFG
		AtlasSet<Node> icfgFunctions = new AtlasHashSet<Node>();
		icfgFunctions.add(entryPointFunction);
		AtlasSet<Node> lastSuccessors = icfgFunctions;
		final boolean expandAll = functionsToExpand.isEmpty();
		while(true) {
			AtlasSet<Node> successors = new AtlasHashSet<Node>();
			for(Node lastSuccessor : lastSuccessors) {
				for(Node successor : callResolutionStrategy.getCallSuccessors(lastSuccessor)) {
					if(expandAll) {
						successors.add(successor);
					} else {
						if(functionsToExpand.contains(successor)) {
							successors.add(successor);
						}
					}
				}
			}
			if(!icfgFunctions.addAll(successors)) {
				// we've reached a fixed point and have no identified all relevant ICFG functions
				break;
			} else {
				// new ICFG functions were discovered, there may still be more relevant ICFG functions
				lastSuccessors = successors;
			}
		}
		
		// step 2) remove functions that empty CFGs (such as library functions that are not indexed)
		AtlasSet<Node> emptyCFGFunctions = new AtlasHashSet<Node>();
		for(Node icfgFunction : icfgFunctions) {
			if(CommonQueries.isEmpty(Common.toQ(icfgFunction).children().nodes(XCSG.ControlFlow_Node))) {
				emptyCFGFunctions.add(icfgFunction);
			}
		}
		for(Node emptyCFGFunction : emptyCFGFunctions) {
			icfgFunctions.remove(emptyCFGFunction);
		}
		
		// step 3) for each function compute the CFG and add the CFG to the ICFG
		Map<Node,CFG> functionCFGs = new HashMap<Node,CFG>();
		for(Node icfgFunction : icfgFunctions) {
			CFG cfg = new CFG(icfgFunction, callResolutionStrategy);
			functionCFGs.put(icfgFunction, cfg);
			icfgNodes.addAll(cfg.getCFG().nodes());
			icfgEdges.addAll(cfg.getCFG().edges());
		}
		
		// step 4) for each callsite that resolves to an expandable target in each CFG
		//         create the ICFG edges and then remove the old successor edges
		for(Node icfgFunction : icfgFunctions) {
			CFG cfg = functionCFGs.get(icfgFunction);
			
			// technically it is not necessary to process the callsites in order
			// but it is a nice property to have the callsites ids created in order of source correspondence
			ArrayList<Node> sortedCallsites = new ArrayList<Node>();
			for(Node callsite : cfg.getCallsiteTargets().keySet()) {
				sortedCallsites.add(callsite);
			}
			Collections.sort(sortedCallsites, new NodeSourceCorrespondenceSorter());
			for(Node callsite : sortedCallsites) {
				for(Node callsiteTarget : cfg.getCallsiteTargets().get(callsite)) {
					if(icfgFunctions.contains(callsiteTarget)) {
						// we use a globally accepted callsite id that is unique to the df callsite node
						// because it will coordinate with existing Atlas schema and will not clobber
						// callsite ids on successive ICFG constructions
						String callsiteID = callsite.getAttr(Attr.Node.CALL_SITE_ID).toString();
						
						Node callsiteControlFlowNode = cfg.getCallsiteControlFlowNodes().get(callsite);
						CFG targetCFG = functionCFGs.get(callsiteTarget);
	
						// link up the callsite cf node to the CFG root and the CFG leaves to the callsite cf node successor
						Edge icfgEntryEdge = getOrCreateICFGEntryEdge(callsiteControlFlowNode, targetCFG.getControlFlowRoot(), callResolutionStrategy);
						icfgEntryEdge.putAttr(ICFGCallsiteAttribute, callsiteID);
						icfgEdges.add(icfgEntryEdge);
						
						// remove the old successor edges
						AtlasSet<Node> callsiteControlFlowNodeSuccessors = new AtlasHashSet<Node>();
						AtlasSet<Edge> successorEdges = Common.toQ(cfg.getCFG()).forwardStep(Common.toQ(callsiteControlFlowNode)).eval().edges();
						for(Edge successorEdge : successorEdges) {
							icfgEdges.remove(successorEdge);
							callsiteControlFlowNodeSuccessors.add(successorEdge.to());
						}
						
						// link of the cfg exits to each of the cfg node successor
						for(Node targetCFGExit : targetCFG.getControlFlowExits()) {
							for(Node callsiteControlFlowNodeSuccessor : callsiteControlFlowNodeSuccessors) {
								Edge icfgExitEdge = getOrCreateICFGExitEdge(targetCFGExit, callsiteControlFlowNodeSuccessor, callResolutionStrategy);
								icfgExitEdge.putAttr(ICFGCallsiteAttribute, callsiteID);
								icfgEdges.add(icfgExitEdge);
							}
						}
					}
				}
			}
		}
	}
	
	private Edge getOrCreateICFGEntryEdge(Node callsiteControlFlowNode, Node cfgRoot, CallResolutionStrategy callResolutionStrategy) {
		Q icfgEntryEdges = Query.universe().edges(ICFGEntryEdge).edges(callResolutionStrategy.getStrategyTagName());
		Edge icfgEntryEdge = icfgEntryEdges.between(Common.toQ(callsiteControlFlowNode), Common.toQ(cfgRoot)).eval().edges().one();
		if(icfgEntryEdge == null) {
			icfgEntryEdge = Graph.U.createEdge(callsiteControlFlowNode, cfgRoot);
			icfgEntryEdge.tag(ICFGEntryEdge);
		}
		return icfgEntryEdge;
	}
	
	private Edge getOrCreateICFGExitEdge(Node cfgExit, Node callsiteControlFlowNodeSuccessor, CallResolutionStrategy callResolutionStrategy) {
		Q icfgExitEdges = Query.universe().edges(ICFGExitEdge).edges(callResolutionStrategy.getStrategyTagName());
		Edge icfgExitEdge = icfgExitEdges.between(Common.toQ(cfgExit), Common.toQ(callsiteControlFlowNodeSuccessor)).eval().edges().one();
		if(icfgExitEdge == null) {
			icfgExitEdge = Graph.U.createEdge(cfgExit, callsiteControlFlowNodeSuccessor);
			icfgExitEdge.tag(ICFGExitEdge);
		}
		return icfgExitEdge;
	}
	
	private static class CFG {
		private Node function;
		private Graph cfg;
		private Node controlFlowRoot;
		private AtlasSet<Node> controlFlowExits;
		private Map<Node,Node> callsiteControlFlowNodes;
		private Map<Node,AtlasSet<Node>> callsiteTargets;
		
		public CFG(Node function, CallResolutionStrategy callResolutionStrategy) {
			this.function = function;
			cfg = CommonQueries.cfg(function).eval();
			this.controlFlowRoot = Common.toQ(cfg).nodes(XCSG.controlFlowRoot).eval().nodes().one();
			this.controlFlowExits = Common.toQ(cfg).nodes(XCSG.controlFlowExitPoint).eval().nodes();
			this.callsiteControlFlowNodes = new HashMap<Node,Node>();
			this.callsiteTargets = new HashMap<Node,AtlasSet<Node>>();
			AtlasSet<Node> callsites = CommonQueries.localDeclarations(Common.toQ(cfg)).nodes(XCSG.CallSite).eval().nodes();
			for(Node callsite : callsites) {
				Node cfNode = Common.toQ(callsite).parent().eval().nodes().one();
				this.callsiteControlFlowNodes.put(callsite, cfNode);
				this.callsiteTargets.put(callsite, callResolutionStrategy.getCallsiteTargets(callsite));
			}
		}

		@SuppressWarnings("unused")
		public Node getFunction() {
			return function;
		}
		
		public Graph getCFG() {
			return cfg;
		}
		
		public Node getControlFlowRoot() {
			return controlFlowRoot;
		}

		public AtlasSet<Node> getControlFlowExits() {
			return controlFlowExits;
		}

		public Map<Node, Node> getCallsiteControlFlowNodes() {
			return callsiteControlFlowNodes;
		}
		
		public Map<Node, AtlasSet<Node>> getCallsiteTargets() {
			return callsiteTargets;
		}
	}
	
	/**
	 * An interface to define alternate strategies for call summary and callsite resolution
	 * @author Ben Holland
	 */
	public static abstract class CallResolutionStrategy {
		
		/**
		 * Returns the call resolution strategy name This name will be used as a tag
		 * name for differentiating ICFG edges created by different call resolution
		 * strategies
		 * 
		 * @return
		 */
		public abstract String getStrategyTagName();
		
		/**
		 * Returns the potential callsite target functions
		 * @param callsite
		 * @return
		 */
		public abstract AtlasSet<Node> getCallsiteTargets(Node callsite);
		
		/**
		 * Returns the potential call successors
		 * @param function
		 * @return
		 */
		public abstract AtlasSet<Node> getCallSuccessors(Node function);
		
		/**
		 * Returns the call graph roots
		 * @param function
		 * @return
		 */
		public abstract AtlasSet<Node> getCallGraphRoots();
	}
	
	/**
	 * This class uses the default call resolution strategy provided by each toolbox provider
	 * For C/C++ and Java this is a class hierarchy analysis
	 * @author Ben Holland
	 */
	public static class DefaultCallResolutionStrategy extends CallResolutionStrategy {

		public static final String CALL_RESOLUTION_STRATEGY_TAG = "ICFG.AtlasCHA";
		
		@Override
		public String getStrategyTagName() {
			return CALL_RESOLUTION_STRATEGY_TAG;
		}
		
		@Override
		public AtlasSet<Node> getCallsiteTargets(Node callsite) {
			return CallSiteAnalysis.getTargets(callsite);
		}

		@Override
		public AtlasSet<Node> getCallSuccessors(Node function) {
			return Query.universe().edges(XCSG.Call).successors(Common.toQ(function)).eval().nodes();
		}

		@Override
		public AtlasSet<Node> getCallGraphRoots() {
			return Query.universe().edges(XCSG.Call).roots().eval().nodes();
		}
		
	}
	
}
