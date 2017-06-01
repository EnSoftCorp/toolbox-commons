package com.ensoftcorp.open.commons.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.language.LanguageSpecificAnalysis;

public class CallSiteAnalysis {
	
	public static abstract class LanguageSpecificCallSiteAnalysis extends LanguageSpecificAnalysis {
		/**
		 * Given a call site, return the functions which may have been invoked.
		 * 
		 * @param callsite
		 * @return
		 */
		public abstract AtlasSet<Node> getTargets(Node callSite);
		
		/**
		 * Given a function return the possible call sites
		 * @param function
		 * @return
		 */
		public abstract AtlasSet<Node> getCallSites(Node function);
	}
	
	private static Map<String,LanguageSpecificCallSiteAnalysis> analysisMap = new HashMap<String,LanguageSpecificCallSiteAnalysis>();
	
	/**
	 * Returns the set of target functions that the given call sites could resolve to
	 * @param callsites
	 * @return
	 */
	public static Q getTargets(Q callSites){
		return Common.toQ(getTargets(callSites.nodes(XCSG.CallSite).eval().nodes()));
	}
	
	/**
	 * Returns the set of target functions that the given call sites could resolve to
	 * @param callsites
	 * @return
	 */
	public static AtlasSet<Node> getTargets(AtlasSet<Node> callSites){
		AtlasSet<Node> targets = new AtlasHashSet<Node>();
		for(Node callsite : callSites){
			targets.addAll(getTargets(callsite));
		}
		return targets;
	}
	
	/**
	 * Given a call site, return the functions which may have been invoked.
	 * 
	 * @param callsite
	 * @return
	 */
	public static AtlasSet<Node> getTargets(Node callSite){
		AtlasSet<Node> targets = new AtlasHashSet<Node>();
		String language = getRequestedLanguage(callSite);
		LanguageSpecificCallSiteAnalysis analysis = getLanguageSpecificCallSiteAnalysis(language);
		return analysis.getTargets(callSite);
	}
	
	/**
	 * Given functions, return possible call sites
	 * @param methods
	 * @return
	 */
	public static AtlasSet<Node> getCallSites(AtlasSet<Node> functions) {
		AtlasSet<Node> callSites = new AtlasHashSet<Node>();
		for (Node function : functions) {
			callSites.addAll(getCallSites(function));
		}
		return callSites;
	}
	
	/**
	 * Given functions, return possible call sites
	 * @param methods
	 * @return
	 */
	public static Q getCallSites(Q functions) {
		return Common.toQ(getCallSites(functions.nodes(XCSG.Function).eval().nodes()));
	}

	/**
	 * Given a function return the possible call sites
	 * @param function
	 * @return
	 */
	public static AtlasSet<Node> getCallSites(Node function){
		AtlasSet<Node> callsites = new AtlasHashSet<Node>();
		String language = getRequestedLanguage(function);
		LanguageSpecificCallSiteAnalysis analysis = getLanguageSpecificCallSiteAnalysis(language);
		return analysis.getCallSites(function);
	}
	
	/**
	 * Clears the cached language specific call site analysis implementation
	 */
	public static void clearLanguageSpecificCallSiteAnalyzerCache(){
		analysisMap.clear();
	}
	
	/**
	 * Loads and caches the language specific call site analysis implementation for the given language
	 * @param language
	 * @return
	 */
	private static LanguageSpecificCallSiteAnalysis getLanguageSpecificCallSiteAnalysis(String language) {
		if(analysisMap.containsKey(language)){
			return analysisMap.get(language);
		}
		Set<LanguageSpecificCallSiteAnalysis> callSiteAnalyses = (Set<LanguageSpecificCallSiteAnalysis>) 
				LanguageSpecificAnalysis.getRegisteredAnalyses(language, LanguageSpecificCallSiteAnalysis.class);
		if(callSiteAnalyses.isEmpty()){
			throw new RuntimeException("A language specific call site analysis for " + language + " has not been registered.");
		}
		if(callSiteAnalyses.size() > 1){
			throw new RuntimeException("Multiple language specific call site analyses for " + language + " have been registered.");
		}
		LanguageSpecificCallSiteAnalysis analysis = callSiteAnalyses.iterator().next();
		analysisMap.put(language, analysis);
		return analysis;
	}
	
	/**
	 * Returns the language of the given node (a call site or function)
	 * @param node
	 * @return
	 */
	private static String getRequestedLanguage(Node node){
		if(node.taggedWith(XCSG.Language.Java)){
			return XCSG.Language.Java;
		} 
		if(node.taggedWith(XCSG.Language.Jimple)){
			return XCSG.Language.Jimple;
		}
		if(node.taggedWith(XCSG.Language.C)){
			return XCSG.Language.C;
		}
		if(node.taggedWith(XCSG.Language.Cpp)){
			return XCSG.Language.Cpp;
		}
		throw new RuntimeException("Unknown language type");
	}
	
}
