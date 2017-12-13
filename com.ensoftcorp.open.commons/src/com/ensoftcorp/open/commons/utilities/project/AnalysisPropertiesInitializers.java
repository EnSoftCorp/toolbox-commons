package com.ensoftcorp.open.commons.utilities.project;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class AnalysisPropertiesInitializers {

	private static Set<AnalysisPropertiesInitializer> INITIALIZERS = Collections.synchronizedSet(new HashSet<AnalysisPropertiesInitializer>());

	/**
	 * Returns a copy of the currently registered analysis properties initializers
	 * 
	 * @return
	 */
	public static Set<AnalysisPropertiesInitializer> getRegisteredAnalysisPropertiesInitializers() {
		HashSet<AnalysisPropertiesInitializer> initializers = new HashSet<AnalysisPropertiesInitializer>();
		for (AnalysisPropertiesInitializer initializer : INITIALIZERS) {
			initializers.add(initializer);
		}
		return initializers;
	}

	/**
	 * Registers the contributed plugin initializer definitions
	 */
	public static void loadAnalysisPropertiesInitializerContributions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_INITIALIZER_EXTENSION_ID);
		try {
			for (IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof AnalysisPropertiesInitializer) {
					AnalysisPropertiesInitializer initializer = (AnalysisPropertiesInitializer) o;
					registerAnalysisPropertiesInitializer(initializer);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading anlaysis properties initializers.", e);
		}
	}

	/**
	 * Registers a new analysis properties initializer
	 * 
	 * @param initializer
	 */
	private static synchronized void registerAnalysisPropertiesInitializer(AnalysisPropertiesInitializer initializer) {
		INITIALIZERS.add(initializer);
	}

	/**
	 * Unregisters an analysis properties initializer
	 * 
	 * @param initializer
	 */
	@SuppressWarnings("unused")
	private static synchronized void unregisterAnalysisPropertiesInitializer(AnalysisPropertiesInitializer initializer) {
		INITIALIZERS.remove(initializer);
	}
	
}
