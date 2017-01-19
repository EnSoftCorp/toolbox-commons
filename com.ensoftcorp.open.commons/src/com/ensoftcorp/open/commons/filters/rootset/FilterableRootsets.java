package com.ensoftcorp.open.commons.filters.rootset;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class FilterableRootsets {

	private static Set<FilterableRootset> FILTERABLE_ROOT_SETS = Collections.synchronizedSet(new HashSet<FilterableRootset>());

	/**
	 * Returns a copy of the currently registered root sets
	 * 
	 * @return
	 */
	public static Set<FilterableRootset> getRegisteredRootSets() {
		HashSet<FilterableRootset> sets = new HashSet<FilterableRootset>();
		for (FilterableRootset set : FILTERABLE_ROOT_SETS) {
			sets.add(set);
		}
		return sets;
	}
	
	/**
	 * Registers the contributed plugin filterable root set definitions
	 */
	public static void loadFilterContributions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_FILTERABLE_ROOT_SET_EXTENSION_ID);
		try {
			for (IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof FilterableRootset) {
					FilterableRootset set = (FilterableRootset) o;
					registerFilterableRootset(set);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading filterable root sets.", e);
		}
	}

	/**
	 * Registers a new filter
	 * 
	 * @param filter
	 */
	private static synchronized void registerFilterableRootset(FilterableRootset set) {
		FILTERABLE_ROOT_SETS.add(set);
	}

	/**
	 * Unregisters a filter
	 * 
	 * @param filter
	 */
	@SuppressWarnings("unused")
	private static synchronized void unregisterFilterableRootset(FilterableRootset set) {
		FILTERABLE_ROOT_SETS.remove(set);
	}
	
}
