package com.ensoftcorp.open.commons.subsystems;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class Subsystems {

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
	 * Registers the contributed plugin subsystems definitions
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
			Log.error("Error loading subsystems.", e);
		}
	}

	/**
	 * Registers a new subsystem
	 * 
	 * @param subsystem
	 */
	private static synchronized void registerSubsystem(Subsystem subsystem) {
		SUBSYSTEMS.add(subsystem);
	}

	/**
	 * Unregisters a subsystem
	 * 
	 * @param subsystem
	 */
	@SuppressWarnings("unused")
	private static synchronized void unregisterSubsystem(Subsystem subsystem) {
		SUBSYSTEMS.remove(subsystem);
	}

	/**
	 * Removes all registered subsystem tags
	 */
	@SuppressWarnings("unused")
	private static synchronized void clearSubsystemTags() {
		Set<Subsystem> subsystems = getRegisteredSubsystems();
		for (Subsystem subsystem : subsystems) {
			subsystem.untagSubsystem();
		}

		// TODO: how to destroy the tag hierarchy? 
		// is this done automatically if there are no tags of a kind?
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
		Q subsystems = Common.universe().nodesTaggedWithAny(subsystemTags);
		Q subsystemContents = subsystems.contained();
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
