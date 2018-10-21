package com.ensoftcorp.open.commons.startup;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.ui.IStartup;

import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.ICFG;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.subsystems.Subsystem;
import com.ensoftcorp.open.commons.subsystems.Subsystems;

public class RegisterHierarchyStartup implements IStartup {

	@Override
	public void earlyStartup() {
		// create subsystem tag hierarchy
		try {
			Subsystems.loadSubsystemContributions();
			Set<Subsystem> subsystems = Subsystems.getRegisteredSubsystems();
			buildSubsystemTagHierarchy(subsystems);
		} catch (Exception e){
			Log.error("Unable to build subsystem tag hierarchy.", e);
		}
		
		// create ICFG tag hierarchy
		try {
			XCSG.HIERARCHY.registerTag(ICFG.ICFGEdge); // intentionally NOT making this a child of XCSG.ControlFlow_Edge so we don't break fundamental schema assertions
			XCSG.HIERARCHY.registerTag(ICFG.ICFGEntryEdge, ICFG.ICFGEdge);
			XCSG.HIERARCHY.registerTag(ICFG.ICFGExitEdge, ICFG.ICFGEdge);
		} catch (Exception e) {
			Log.error("Unable to build ICFG tag hierarchy.", e);
		}
	}
	
	private void buildSubsystemTagHierarchy(Set<Subsystem> subsystems) {
		// register root tag
		try {
			XCSG.HIERARCHY.registerTag(Subsystem.ROOT_SUBSYSTEM_TAG, new String[] {});
		} catch (IllegalStateException e){
			// its ok if the tag was already registered, then we are
			// successful for the purposes of building the hierarchy
			if(!e.getMessage().contains("Tag is already registered!")){
				throw e;
			}
		}

		// create a worklist of subsystems to register
		LinkedList<Subsystem> registrationWorklist = new LinkedList<Subsystem>();
		
		for(Subsystem subsystem : subsystems){
			registrationWorklist.add(subsystem);
		}
		
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
				// check if all of the parent tags in the subsystem have been registered
				if (registeredTags.containsAll(parents)) {
					// register the subsystem
					try {
						XCSG.HIERARCHY.registerTag(subsystem.getTag(), subsystem.getParentTags());
					} catch (IllegalStateException e){
						// its ok if the tag was already registered, then we are
						// successful for the purposes of building the hierarchy
						if(!e.getMessage().contains("Tag is already registered!")){
							throw e;
						}
					}
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
	
}
