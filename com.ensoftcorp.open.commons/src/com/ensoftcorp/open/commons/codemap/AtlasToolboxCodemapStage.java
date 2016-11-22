package com.ensoftcorp.open.commons.codemap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.atlas.core.indexing.providers.ToolboxIndexingStage;
import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class AtlasToolboxCodemapStage implements ToolboxIndexingStage {

	@Override
	public String displayName() {
		return "Prioritized Codemap Stages";
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		try {
			HashSet<String> completedCodemapStages = new HashSet<String>();
			Set<PrioritizedCodemapStage> codemapStages = loadCodemapContributions();
			
			String message = "Loaded " + codemapStages.size() + " Prioritized Codemap Stages";
			for(PrioritizedCodemapStage codemapStage : codemapStages){
				String[] dependencies = codemapStage.getCodemapStageDependencies();
				message += ("\n" + codemapStage.getDisplayName() 
						+ ", dependencies: " 
						+ (dependencies.length==0 ? "none" : Arrays.toString(dependencies)));
			}
			Log.info(message);
					
			while(!codemapStages.isEmpty()){
				PrioritizedCodemapStage codemapStage = getDependencySatisfiedCodemapStage(codemapStages, completedCodemapStages);
				monitor.setTaskName(codemapStage.getDisplayName());
				try {
					codemapStage.performIndexing(monitor);
				} catch (Throwable t){
					Log.error(codemapStage.getDisplayName() + " failed.", t);
				}
				codemapStages.remove(codemapStage);
				completedCodemapStages.add(codemapStage.getIdentifier());
			}
		} catch (Exception e) {
			Log.error("Error running toolbox codemap stages", e);
		}
	}
	
	/**
	 * Returns a codemap stage that has all dependencies satisfied
	 * @param codemapStages
	 * @param completedCodemapStages
	 * @return
	 */
	private PrioritizedCodemapStage getDependencySatisfiedCodemapStage(Set<PrioritizedCodemapStage> codemapStages, HashSet<String> completedCodemapStages){
		for(PrioritizedCodemapStage codemapStage : codemapStages){
			HashSet<String> dependencies = new HashSet<String>();
			for(String dependency : codemapStage.getCodemapStageDependencies()){
				dependencies.add(dependency);
			}
			dependencies.removeAll(completedCodemapStages);
			if(dependencies.isEmpty()){
				return codemapStage;
			}
		}
		throw new RuntimeException("Unsatisfiable codemap dependencies!");
	}
	
	/**
	 * Returns the contributed plugin codemap stages
	 */
	public static Set<PrioritizedCodemapStage> loadCodemapContributions() {
		HashSet<PrioritizedCodemapStage> codemapStages = new HashSet<PrioritizedCodemapStage>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_CODEMAP_EXTENSION_ID);
		try {
			for (IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof PrioritizedCodemapStage) {
					PrioritizedCodemapStage codemapStage = (PrioritizedCodemapStage) o;
					codemapStages.add(codemapStage);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading prioritized codemap.", e);
		}
		return codemapStages;
	}

}
