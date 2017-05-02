package com.ensoftcorp.open.commons.analyzers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.analyzers.Analyzer.Result;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.log.Log;

public class Analyzers extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the Subsystem codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.analyzers";
	
	private static Set<Analyzer> ANALYZERS = Collections.synchronizedSet(new HashSet<Analyzer>());
	private static Map<String, List<Result>> ANALYZER_RESULTS = new HashMap<String, List<Result>>();
	private static Map<String, Graph> ANALYZER_ALL_RESULTS = new HashMap<String, Graph>();
	
	@Override
	public String getDisplayName() {
		return "Analyzers";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{}; // no dependencies
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		Log.info("Running analyzers...");
		Analyzers.loadAnalyzerContributions();
		Set<Analyzer> analyzers = Analyzers.getRegisteredAnalyzers();
		for(Analyzer analyzer : analyzers){
			monitor.subTask("Analyzing " + analyzer.getName());
			// TODO: set context via preferences
			List<Result> results = analyzer.getResults(Common.universe());
			ANALYZER_RESULTS.put(analyzer.getName(), results);
			AtlasSet<Node> nodes = new AtlasHashSet<Node>();
			AtlasSet<Edge> edges = new AtlasHashSet<Edge>();
			for(Result result : results){
				Graph g = result.getQ().eval();
				nodes.addAll(g.nodes());
				edges.addAll(g.edges());
			}
			ANALYZER_ALL_RESULTS.put(analyzer.getName(), Common.toQ(edges).union(Common.toQ(nodes)).eval());
		}
	}
	
	public static boolean hasCachedResult(Analyzer analyzer){
		return hasCachedResult(analyzer.getName());
	}
	
	public static boolean hasCachedResult(String analyzerName) {
		return ANALYZER_ALL_RESULTS.containsKey(analyzerName);
	}

	public static Graph getAllAnalyzerResults(Analyzer analyzer){
		return getAllAnalyzerResults(analyzer.getName()); 
	}
	
	public static Graph getAllAnalyzerResults(String analyzerName){
		return ANALYZER_ALL_RESULTS.get(analyzerName); 
	}
	
	public static List<Result> getAnalyzerResults(Analyzer analyzer){
		return getAnalyzerResults(analyzer.getName()); 
	}
	
	public static List<Result> getAnalyzerResults(String analyzerName){
		return ANALYZER_RESULTS.get(analyzerName); 
	}

	/**
	 * Returns a copy of the currently registered analyzers
	 * 
	 * @return
	 */
	public static Set<Analyzer> getRegisteredAnalyzers() {
		HashSet<Analyzer> analyzers = new HashSet<Analyzer>();
		for (Analyzer analyzer : ANALYZERS) {
			analyzers.add(analyzer);
		}
		return analyzers;
	}

	/**
	 * Registers the contributed plugin analyzer definitions
	 */
	public static void loadAnalyzerContributions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_ANALYZER_EXTENSION_ID);
		try {
			for (IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof Analyzer) {
					Analyzer analyzer = (Analyzer) o;
					registerAnalyzer(analyzer);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading analyzers.", e);
		}
	}

	/**
	 * Registers a new analyzer
	 * 
	 * @param analyzer
	 */
	private static synchronized void registerAnalyzer(Analyzer analyzer) {
		ANALYZERS.add(analyzer);
	}

	/**
	 * Unregisters a analyzer
	 * 
	 * @param analyzer
	 */
	@SuppressWarnings("unused")
	private static synchronized void unregisterAnalyzer(Analyzer analyzer) {
		ANALYZERS.remove(analyzer);
	}
	
}
