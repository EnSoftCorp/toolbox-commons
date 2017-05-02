package com.ensoftcorp.open.commons.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.analyzers.Analyzer;
import com.ensoftcorp.open.commons.analyzers.Analyzers;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.subsystems.Subsystem;
import com.ensoftcorp.open.commons.subsystems.Subsystems;

public class CommonsPreferences extends AbstractPreferenceInitializer {

	@SuppressWarnings("unused")
	private static boolean initialized = false;
	
	/**
	 * Enables or disables the subsystem category
	 * @param subsystemCategory
	 * @param enabled
	 */
	public static void enableSubsystemCategory(String subsystemCategory, boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(subsystemCategory, enabled);
	}
	
	/**
	 * Returns true if the subsystem category is enabled for tagging
	 * 
	 * @param subsystemCategory
	 * @return
	 */
	public static boolean isSubsystemCategoryEnabled(String subsystemCategory){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		Boolean result = preferences.getBoolean(subsystemCategory);
		return result.booleanValue();
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
		// disable subsystem tagging categories by default
		Subsystems.loadSubsystemContributions();
		for(Subsystem subsystem : Subsystems.getRegisteredSubsystems()){
			preferences.setDefault(subsystem.getCategory(), false);
		}
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
		// disable subsystem tagging categories by default
		Subsystems.loadSubsystemContributions();
		for(Subsystem subsystem : Subsystems.getRegisteredSubsystems()){
			preferences.setValue(subsystem.getCategory(), false);
		}
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
			Log.warning("Error accessing commons preferences, using defaults...", e);
		}
		initialized = true;
	}

}