package com.ensoftcorp.open.commons.subsystems;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;

/**
 * Runs the subsystem tagging as a post indexing process
 * 
 * @author Ben Holland
 */
public class SubsystemCodemapStage extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the Subsystem codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.subsystems";
	
	@Override
	public String getDisplayName() {
		return "Subsystem Tagging";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{}; // no dependencies
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		Log.info("Tagging subsystems...");
		Subsystems.loadSubsystemContributions();

		Set<Subsystem> subsystems = Subsystems.getRegisteredSubsystems();
		
		boolean hasEnabledSubystemCategory = false;
		for(Subsystem subsystem : subsystems){
			if(CommonsPreferences.isSubsystemCategoryEnabled(subsystem.getCategory())){
				hasEnabledSubystemCategory = true;
				break;
			}
		}
		
		if(hasEnabledSubystemCategory){
			buildSubsystemTagHierarchy(subsystems);
			
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
			if(CommonsPreferences.isSubsystemCategoryEnabled(subsystem.getCategory())){
				registrationWorklist.add(subsystem);
			}
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
