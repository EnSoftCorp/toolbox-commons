package com.ensoftcorp.open.commons.filters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.commons.Activator;

public class Filters {

	private static Set<Filter> FILTERS = Collections.synchronizedSet(new HashSet<Filter>());

	/**
	 * Returns a copy of the currently registered filters
	 * 
	 * @return
	 */
	public static Set<Filter> getRegisteredFilters() {
		HashSet<Filter> filters = new HashSet<Filter>();
		for (Filter filter : FILTERS) {
			filters.add(filter);
		}
		return filters;
	}
	
	public static ArrayList<Filter> getApplicableFilters(Q input) {
		ArrayList<Filter> applicableFilters = new ArrayList<Filter>();
		// find the applicable filters
		for(Filter filter : Filters.getRegisteredFilters()){
			if(filter.isApplicableTo(input)){
				applicableFilters.add(filter);
			}
		}
		// sort the filters alphabetically
		Collections.sort(applicableFilters, new Comparator<Filter>(){
			@Override
			public int compare(Filter f1, Filter f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		return applicableFilters;
	}

	/**
	 * Registers the contributed plugin filter definitions
	 */
	public static void loadFilterContributions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_FILTER_EXTENSION_ID);
		try {
			for (IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof Filter) {
					Filter filter = (Filter) o;
					registerFilter(filter);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading filters.", e);
		}
	}

	/**
	 * Registers a new filter
	 * 
	 * @param filter
	 */
	private static synchronized void registerFilter(Filter filter) {
		FILTERS.add(filter);
	}

	/**
	 * Unregisters a filter
	 * 
	 * @param filter
	 */
	@SuppressWarnings("unused")
	private static synchronized void unregisterFilter(Filter filter) {
		FILTERS.remove(filter);
	}
	
}
