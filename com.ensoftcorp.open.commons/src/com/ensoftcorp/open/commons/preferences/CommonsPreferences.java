package com.ensoftcorp.open.commons.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.subsystems.Subsystem;
import com.ensoftcorp.open.commons.subsystems.Subsystems;

public class CommonsPreferences extends AbstractPreferenceInitializer {

	@SuppressWarnings("unused")
	private static boolean initialized = false;
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		// let each registered subystem tagging instruction be enabled by default
		Subsystems.loadSubsystemContributions();
		for(Subsystem subsystem : Subsystems.getRegisteredSubsystems()){
			preferences.setDefault(subsystem.getTag(), false);
		}
	}
	
	/**
	 * Returns true if the subsystem category is enabled for tagging
	 * 
	 * @param subsystemCategory
	 * @return
	 */
	public static boolean isSubsystemCategoryEnabled(String subsystemCategory){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		Boolean result = preferences.getBoolean(subsystemCategory);
		return result.booleanValue();
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			@SuppressWarnings("unused")
			IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		} catch (Exception e){
			Log.warning("Error accessing commons preferences, using defaults...", e);
		}
		initialized = true;
	}

}