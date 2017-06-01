package com.ensoftcorp.open.commons.language;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public abstract class LanguageSpecificAnalysis {

	public abstract String getName();
	
	public abstract String getDescription();
	
	public abstract Set<String> getSupportedLanguages();
	
	private static Set<LanguageSpecificAnalysis> ANALYSES = Collections.synchronizedSet(new HashSet<LanguageSpecificAnalysis>());
	
	static {
		loadAnalysisContributions();
	}
	
	/**
	 * Returns a copy of the currently registered analyses
	 * 
	 * @return
	 */
	public static Set<LanguageSpecificAnalysis> getRegisteredAnalyses() {
		HashSet<LanguageSpecificAnalysis> analyses = new HashSet<LanguageSpecificAnalysis>();
		for (LanguageSpecificAnalysis analysis : ANALYSES) {
			analyses.add(analysis);
		}
		return analyses;
	}
	
	/**
	 * Returns a copy of the currently registered analyses that apply to the given language
	 * @param language
	 * @return
	 */
	public static Set<LanguageSpecificAnalysis> getRegisteredAnalyses(String language){
		HashSet<LanguageSpecificAnalysis> analyses = new HashSet<LanguageSpecificAnalysis>();
		for(LanguageSpecificAnalysis analysis : getRegisteredAnalyses()){
			if(analysis.getSupportedLanguages().contains(language)){
				analyses.add(analysis);
			}
		}
		return analyses;
	}
	
	/**
	 * Returns a copy of the currently registered analyses that apply to the given analysis type
	 * 
	 * @param analysisType
	 * @return
	 */
	public static Set<? extends LanguageSpecificAnalysis> getRegisteredAnalyses(Class<? extends LanguageSpecificAnalysis> analysisType){
		HashSet<LanguageSpecificAnalysis> analyses = new HashSet<LanguageSpecificAnalysis>();
		for(LanguageSpecificAnalysis analysis : getRegisteredAnalyses()){
			if(analysisType.isInstance(analysis)){
				analyses.add(analysis);
			}
		}
		return analyses;
	}
	
	/**
	 * Returns a copy of the currently registered analyses that apply to the given language and analysis type
	 * @param language
	 * @param analysisType
	 * @return
	 */
	public static Set<? extends LanguageSpecificAnalysis> getRegisteredAnalyses(String language, Class<? extends LanguageSpecificAnalysis> analysisType){
		Set<LanguageSpecificAnalysis> analyses = new HashSet<LanguageSpecificAnalysis>();
		for(LanguageSpecificAnalysis analysis : getRegisteredAnalyses(analysisType)){
			if(analysis.getSupportedLanguages().contains(language)){
				analyses.add(analysis);
			}
		}
		return analyses;
	}

	/**
	 * Registers the contributed plugin analyses definitions
	 */
	public static void loadAnalysisContributions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_LANGUAGE_SPECIFIC_ANALYSIS_EXTENSION_ID);
		try {
			for (IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof LanguageSpecificAnalysis) {
					LanguageSpecificAnalysis analysis = (LanguageSpecificAnalysis) o;
					registerAnalysis(analysis);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading language specific analyses.", e);
		}
	}

	/**
	 * Registers a new analysis
	 * 
	 * @param analysis
	 */
	private static synchronized void registerAnalysis(LanguageSpecificAnalysis analysis) {
		ANALYSES.add(analysis);
	}

	/**
	 * Unregisters an analysis
	 * 
	 * @param analysis
	 */
	@SuppressWarnings("unused")
	private static synchronized void unregisterAnalysis(LanguageSpecificAnalysis analysis) {
		ANALYSES.remove(analysis);
	}

	/**
	 * LanguageSpecificAnalysis objects are equal if they share the same name and support the same languages
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getSupportedLanguages() == null) ? 0 : getSupportedLanguages().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	/**
	 * LanguageSpecificAnalysis objects are equal if they share the same name and support the same languages
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanguageSpecificAnalysis other = (LanguageSpecificAnalysis) obj;
		if (getSupportedLanguages() == null) {
			if (other.getSupportedLanguages() != null)
				return false;
		} else if (!getSupportedLanguages().equals(other.getSupportedLanguages()))
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}
	
}
