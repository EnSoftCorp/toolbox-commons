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

import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.analyzers.Analyzer.Result;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.preferences.AnalyzerPreferences;

public class Analyzers extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for analyzer definitions
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
	public boolean performIndexing(IProgressMonitor monitor) {
		boolean logged = false;
		ANALYZER_RESULTS.clear();
		ANALYZERS.clear();
		Analyzers.loadAnalyzerContributions();
		Set<Analyzer> analyzers = Analyzers.getRegisteredAnalyzers();
		boolean ranIndexer = false;
		for(Analyzer analyzer : analyzers){
			ranIndexer = true;
			if(AnalyzerPreferences.isAnalyzerCachingEnabled(analyzer.getName())){
				if(!logged){
					Log.info("Running analyzers...");
					logged = true;
				}
				monitor.subTask("Analyzing " + analyzer.getName());
				Log.info("Analyzing " + analyzer.getName());
				
				long elapsed = System.currentTimeMillis();
				
				// TODO: how to set analyzer context? via preferences maybe?
				List<Result> results = analyzer.getResults(Query.universe());
				
				elapsed = System.currentTimeMillis() - elapsed;
				Log.debug("Analyzer time: " + analyzer.getName() + " " + elapsed + "ms");
				
				cacheResults(analyzer, results);
				
				for(AnalyzerResultChangedCallback callback : CALLBACKS) {
					callback.callback();
				}
			}
		}
		return ranIndexer;
	}
	
	public static abstract class AnalyzerResultChangedCallback {
		private String name;
		
		public AnalyzerResultChangedCallback(String name) {
			this.name = name;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnalyzerResultChangedCallback other = (AnalyzerResultChangedCallback) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
		
		public abstract void callback();
	}
	
	private static final Set<AnalyzerResultChangedCallback> CALLBACKS = new HashSet<AnalyzerResultChangedCallback>();
	
	public static void registerAnalyzerResultChangedCallback(AnalyzerResultChangedCallback callback) {
		CALLBACKS.add(callback);
	}
	
	public static void unregisterAnalyzerResultChangedCallback(AnalyzerResultChangedCallback callback) {
		CALLBACKS.remove(callback);
	}
	
	public static void clearCachedResults(){
		ANALYZER_RESULTS.clear();
	}
	
	public static boolean hasCachedResult(Analyzer analyzer){
		return hasCachedResult(analyzer.getName());
	}
	
	public static void cacheResults(Analyzer analyzer, List<Result> results){
		ANALYZER_RESULTS.put(analyzer.getName(), results);
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