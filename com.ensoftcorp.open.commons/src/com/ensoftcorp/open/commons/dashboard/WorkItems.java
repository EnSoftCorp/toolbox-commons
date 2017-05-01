package com.ensoftcorp.open.commons.dashboard;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class WorkItems {

	public static Collection<WorkItem> getContributions(IExtensionRegistry registry) {
		LinkedList<WorkItem> workItems = new LinkedList<WorkItem>();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_WORKITEM_EXTENSION_ID);
		try {
			for(IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if(o instanceof WorkItem) {
					WorkItem workItem = (WorkItem) o;
					workItems.add(workItem);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading work items.", e);
		}
		Log.info("Dashboard loaded " + workItems.size() + " work items.");
		return workItems;
	}

}