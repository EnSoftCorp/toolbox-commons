package com.ensoftcorp.open.commons.subsystems;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.preferences.SubsystemPreferences;

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
	public boolean performIndexing(IProgressMonitor monitor) {
		Subsystems.loadSubsystemContributions();

		Set<Subsystem> subsystems = Subsystems.getRegisteredSubsystems();
		
		boolean hasEnabledSubystemCategory = false;
		for(Subsystem subsystem : subsystems){
			if(SubsystemPreferences.isSubsystemCategoryEnabled(subsystem.getCategory())){
				hasEnabledSubystemCategory = true;
				break;
			}
		}
		
		boolean logged = false;
		boolean ranIndexer = false;
		if(hasEnabledSubystemCategory){
			for (Subsystem subsystem : subsystems) {
				if(!logged){
					Log.info("Tagging subsystems...");
					logged = true;
				}
				if (monitor.isCanceled()) {
					break;
				} else {
					if(SubsystemPreferences.isSubsystemCategoryEnabled(subsystem.getCategory())){
						subsystem.tagSubsystem();
						ranIndexer = true;
					}
				}
			}
		}
		return ranIndexer;
	}

}
