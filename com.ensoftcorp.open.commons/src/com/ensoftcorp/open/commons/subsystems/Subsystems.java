package com.ensoftcorp.open.commons.subsystems;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.indexing.providers.ToolboxIndexingStage;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.Activator;

public class Subsystems implements ToolboxIndexingStage {

	private static Set<Subsystem> SUBSYSTEMS = Collections.synchronizedSet(new HashSet<Subsystem>());
	
	/**
	 * Returns a copy of the currently registered subsystems
	 * @return
	 */
	public static Set<Subsystem> getRegisteredSubsystems(){
		HashSet<Subsystem> subsystems = new HashSet<Subsystem>();
		for(Subsystem subsystem : SUBSYSTEMS){
			subsystems.add(subsystem);
		}
		return subsystems;
	}
	
	/**
	 * Registers the contributed plugin susbsystem definitions
	 */
	public static void loadSubsystemContributions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_SUBSYSTEM_EXTENSION_ID);
		try {
			for(IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if(o instanceof Subsystem) {
					Subsystem subsystem = (Subsystem) o;
					registerSubsystem(subsystem);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading work items.", e);
		}
		Log.info("Loaded " + SUBSYSTEMS.size() + " unique subsystem definitions.");
	}
	
	/**
	 * Registers a new subsystem 
	 * @param subsystem
	 */
	public static synchronized void registerSubsystem(Subsystem subsystem){
		SUBSYSTEMS.add(subsystem);
	}
	
	/**
	 * Unregisters a subsystem
	 * @param subsystem
	 */
	public static synchronized void unregisterSubsystem(Subsystem subsystem){
		SUBSYSTEMS.remove(subsystem);
	}
	
	/**
	 * Applies all registered subsystem tags
	 */
	public static synchronized void tagSubsystems(){
		for(Subsystem subsystem : SUBSYSTEMS){
			subsystem.tagSubsystem();
		}
	}
	
	/**
	 * Removes all registered subsystem tags
	 */
	public static synchronized void untagSubsystems(){
		for(Subsystem subsystem : SUBSYSTEMS){
			subsystem.untagSubsystem();
		}
	}

	@Override
	public String displayName() {
		return "Subsystem Tagging";
	}
	
	@Override
	public void performIndexing(IProgressMonitor monitor) {
		loadSubsystemContributions();
		
		Set<Subsystem> subsystems = getRegisteredSubsystems();
		buildSubsystemTagHierarchy(subsystems);
		
		for(Subsystem subsystem : subsystems){
			if(monitor.isCanceled()){
				break;
			} else {
				subsystem.tagSubsystem();
			}
		}
	}

	private void buildSubsystemTagHierarchy(Set<Subsystem> subsystems) {
		
	}
	
	/**
	 * Returns the set of nodes contained in the given subsystem tags
	 * @param subsystemTags
	 * @return
	 */
	public static Q getSubsystemContents(Subsystem... subsystems){
		String[] subsystemTags = new String[subsystems.length];
		for(int i=0; i<subsystems.length; i++){
			subsystemTags[i] = subsystems[i].getTag();
		}
		return getSubsystemContents(subsystemTags);
	}
	
	/**
	 * Returns the set of nodes contained in the given subsystem tags
	 * @param subsystemTags
	 * @return
	 */
	public static Q getSubsystemContents(String... subsystemTags){
		AtlasSet<Node> subsystemNodes = new AtlasHashSet<Node>();
		Q containsEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains);
		Q subsystems = Common.universe().nodesTaggedWithAny(subsystemTags);
		Q subsystemContents = containsEdges.forward(subsystems);
		subsystemNodes.addAll(subsystemContents.eval().nodes());
		return Common.toQ(subsystemNodes);
	}
}
