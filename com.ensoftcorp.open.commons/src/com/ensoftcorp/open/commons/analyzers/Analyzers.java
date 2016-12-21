package com.ensoftcorp.open.commons.analyzers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.analyzers.Analyzer;

public class Analyzers {

	private static Set<Analyzer> ANALYZERS = Collections.synchronizedSet(new HashSet<Analyzer>());

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
