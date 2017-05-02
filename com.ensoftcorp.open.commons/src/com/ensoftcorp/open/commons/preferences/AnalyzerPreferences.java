package com.ensoftcorp.open.commons.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.analyzers.Analyzer;
import com.ensoftcorp.open.commons.analyzers.Analyzers;
import com.ensoftcorp.open.commons.log.Log;

public class AnalyzerPreferences extends AbstractPreferenceInitializer {

	@SuppressWarnings("unused")
	private static boolean initialized = false;

	/**
	 * Enables or disables the caching for analyzer
	 * @param subsystemCategory
	 * @param enabled
	 */
	public static void enableAnalyzerCaching(Analyzer analyzer, boolean enabled){
		enableAnalyzerCaching(analyzer.getName(), enabled);
	}
	
	/**
	 * Enables or disables the caching for analyzer
	 * @param subsystemCategory
	 * @param enabled
	 */
	public static void enableAnalyzerCaching(String analyzerName, boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(analyzerName, enabled);
	}
	
	/**
	 * Returns true if caching for the given analyzer is enabled
	 * 
	 * @param subsystemCategory
	 * @return
	 */
	public static boolean isAnalyzerCachingEnabled(String analyzerName){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		Boolean result = preferences.getBoolean(analyzerName);
		return result.booleanValue();
	}
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		// disable analyzer result caching by default
		Analyzers.loadAnalyzerContributions();
		for(Analyzer analyzer : Analyzers.getRegisteredAnalyzers()){
			preferences.setDefault(analyzer.getName(), false);
		}
	}
	
	/**
	 * Restores the default preferences
	 */
	public static void restoreDefaults(){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		// disable analyzer result caching by default
		Analyzers.loadAnalyzerContributions();
		for(Analyzer analyzer : Analyzers.getRegisteredAnalyzers()){
			preferences.setValue(analyzer.getName(), false);
		}
		loadPreferences();
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			Activator.getDefault().getPreferenceStore();
		} catch (Exception e){
			Log.warning("Error accessing commons analyzer preferences, using defaults...", e);
		}
		initialized = true;
	}

}