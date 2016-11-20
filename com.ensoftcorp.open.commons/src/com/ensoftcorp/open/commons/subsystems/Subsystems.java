package com.ensoftcorp.open.commons.subsystems;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
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
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;

public class Subsystems implements ToolboxIndexingStage {

	private static Set<Subsystem> SUBSYSTEMS = Collections.synchronizedSet(new HashSet<Subsystem>());

	/**
	 * Returns a copy of the currently registered subsystems
	 * 
	 * @return
	 */
	public static Set<Subsystem> getRegisteredSubsystems() {
		HashSet<Subsystem> subsystems = new HashSet<Subsystem>();
		for (Subsystem subsystem : SUBSYSTEMS) {
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
			for (IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof Subsystem) {
					Subsystem subsystem = (Subsystem) o;
					registerSubsystem(subsystem);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading work items.", e);
		}
	}

	/**
	 * Registers a new subsystem
	 * 
	 * @param subsystem
	 */
	public static synchronized void registerSubsystem(Subsystem subsystem) {
		SUBSYSTEMS.add(subsystem);
	}

	/**
	 * Unregisters a subsystem
	 * 
	 * @param subsystem
	 */
	public static synchronized void unregisterSubsystem(Subsystem subsystem) {
		SUBSYSTEMS.remove(subsystem);
	}

	/**
	 * Removes all registered subsystem tags
	 */
	public static synchronized void clearSubsystemTags() {
		Set<Subsystem> subsystems = getRegisteredSubsystems();
		for (Subsystem subsystem : subsystems) {
			subsystem.untagSubsystem();
		}

		// TODO: how to destroy the tag hierarchy? 
		// is this done automatically if there are no tags of a kind?
	}

	@Override
	public String displayName() {
		return "Subsystem Tagging";
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		loadSubsystemContributions();

		Set<Subsystem> subsystems = getRegisteredSubsystems();
		
		try {
			buildSubsystemTagHierarchy(subsystems);
		} catch (IllegalStateException e){
			if(e.getMessage().contains("Tag is already registered!")){
				Log.info("The subystem hierarchy already exists.", e);
			} else {
				throw e;
			}
		}

		for (Subsystem subsystem : subsystems) {
			if (monitor.isCanceled()) {
				break;
			} else {
				if(CommonsPreferences.isSubsystemCategoryEnabled(subsystem.getCategory())){
					subsystem.tagSubsystem();
				}
			}
		}
	}

	private void buildSubsystemTagHierarchy(Set<Subsystem> subsystems) {
		// register root tag
		XCSG.HIERARCHY.registerTag(Subsystem.ROOT_SUBSYSTEM_TAG, new String[] {});

		// create a worklist of subsystems to register
		LinkedList<Subsystem> registrationWorklist = new LinkedList<Subsystem>();
		registrationWorklist.addAll(subsystems);

		// get the registered tags
		HashSet<String> registeredTags = new HashSet<String>();
		for (String registeredTag : XCSG.HIERARCHY.registeredTags()) {
			registeredTags.add(registeredTag);
		}
		
		// register each tag adding the parents
		// parents tags must be registered first
		while (!registrationWorklist.isEmpty()) {
			// for each subsystem to register
			LinkedList<Subsystem> completedRegistrations = new LinkedList<Subsystem>();
			for (Subsystem subsystem : registrationWorklist) {
				// get the subsystems parent tags
				HashSet<String> parents = new HashSet<String>();
				for (String parent : subsystem.getParentTags()) {
					parents.add(parent);
				}
				// check if all of the parent tags in the subsystem have been
				// registered
				if (registeredTags.containsAll(parents)) {
					// register the subsystem
					XCSG.HIERARCHY.registerTag(subsystem.getTag(), subsystem.getParentTags());
					registeredTags.add(subsystem.getTag());
					completedRegistrations.add(subsystem);
				}
			}
			// if we couldn't register any subsystems and the worklist is not empty,
			// then we must have a cycle in the hierarchy :(
			if (!registrationWorklist.removeAll(completedRegistrations)) {
				Log.warning("An issue was detected in the subsystem hierarchy.\n"
						+ "There may be a dependency cycle or required parent subsystems may not have been contributed properly and are missing."
						+ "The following subsystems were not registered successfully.\n"
						+ registrationWorklist.toString());
				break;
			}
		}
	}

	/**
	 * Returns the set of nodes contained in the given subsystem tags
	 * 
	 * @param subsystemTags
	 * @return
	 */
	public static Q getSubsystemContents(Subsystem... subsystems) {
		String[] subsystemTags = new String[subsystems.length];
		for (int i = 0; i < subsystems.length; i++) {
			subsystemTags[i] = subsystems[i].getTag();
		}
		return getSubsystemContents(subsystemTags);
	}

	/**
	 * Returns the set of nodes contained in the given subsystem tags
	 * 
	 * @param subsystemTags
	 * @return
	 */
	public static Q getSubsystemContents(String... subsystemTags) {
		AtlasSet<Node> subsystemNodes = new AtlasHashSet<Node>();
		Q containsEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains);
		Q subsystems = Common.universe().nodesTaggedWithAny(subsystemTags);
		Q subsystemContents = containsEdges.forward(subsystems);
		subsystemNodes.addAll(subsystemContents.eval().nodes());
		return Common.toQ(subsystemNodes);
	}

	/**
	 * Returns the interactions along the given types of edges of the context
	 * set and the set defined by the contents of the subsystem
	 * 
	 * @param context
	 * @param subsystem
	 * @param edgeTags
	 * @return
	 */
	public static Q getSubsystemInteractions(Q context, Subsystem subsystem, String... edgeTags) {
		Q subsystemContents = getSubsystemContents(subsystem.getTag());
		return CommonQueries.interactions(context, subsystemContents, edgeTags);
	}

	/**
	 * Returns the interactions along the given types of edges of the context
	 * set and the set defined by the contents of the subsystem
	 * 
	 * @param context
	 * @param subsystemTag
	 * @param edgeTags
	 * @return
	 */
	public static Q getSubsystemInteractions(Q context, String subsystemTag, String... edgeTags) {
		Q subsystemContents = getSubsystemContents(subsystemTag);
		return CommonQueries.interactions(context, subsystemContents, edgeTags);
	}
}
