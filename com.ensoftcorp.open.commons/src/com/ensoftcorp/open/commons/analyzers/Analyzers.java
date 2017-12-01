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

import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.analyzers.Analyzer.Result;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.preferences.AnalyzerPreferences;
import com.ensoftcorp.open.commons.ui.views.dashboard.DashboardView;

public class Analyzers extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the Subsystem codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.analyzers";
	
	private static Set<Analyzer> ANALYZERS = Collections.synchronizedSet(new HashSet<Analyzer>());
	private static Map<String, List<Result>> ANALYZER_RESULTS = new HashMap<String, List<Result>>();
	
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
		loadAnalyzerContributions();
		HashSet<String> dependencies = new HashSet<String>();
		for(Analyzer analyzer : getRegisteredAnalyzers()){
			if(AnalyzerPreferences.isAnalyzerCachingEnabled(analyzer.getName())){
				for(String dependency : analyzer.getCodemapStageDependencies()){
					dependencies.add(dependency);
				}
			}
		}
		String[] result = new String[dependencies.size()];
		dependencies.toArray(result);
		return result;
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		Analyzers.loadAnalyzerContributions();
		Set<Analyzer> analyzers = Analyzers.getRegisteredAnalyzers();
		
		boolean logged = false;
		
		for(Analyzer analyzer : analyzers){
			if(!logged){
				Log.info("Running analyzers...");
				logged = true;
			}
			if(AnalyzerPreferences.isAnalyzerCachingEnabled(analyzer.getName())){
				monitor.subTask("Analyzing " + analyzer.getName());
				Log.info("Analyzing " + analyzer.getName());
				// TODO: how to set analyzer context? via preferences maybe?
				List<Result> results = analyzer.getResults(Common.universe());
				ANALYZER_RESULTS.put(analyzer.getName(), results);
				DashboardView.refreshRequired();
			}
		}
	}
	
	public static boolean hasCachedResult(Analyzer analyzer){
		return hasCachedResult(analyzer.getName());
	}
	
	public static boolean hasCachedResult(String analyzerName) {
		return ANALYZER_RESULTS.containsKey(analyzerName);
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
