package com.ensoftcorp.open.commons.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class CommonsPreferences extends AbstractPreferenceInitializer {

	private static boolean initialized = false;
	
	/**
	 * Enable/disable subystem tagging
	 * If enabled all registered subystem tagging instructions will be applied during indexing
	 * If disabled no subsystem tagging instructions will be applied during indexing
	 */
	public static final String SUBSYSTEM_TAGGING = "SUBSYSTEM_TAGGING";
	public static final Boolean SUBSYSTEM_TAGGING_DEFAULT = false;
	private static boolean subsystemTaggingValue = SUBSYSTEM_TAGGING_DEFAULT;
	
	public static boolean isSubsystemTaggingEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return subsystemTaggingValue;
	}
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setDefault(SUBSYSTEM_TAGGING, SUBSYSTEM_TAGGING_DEFAULT);
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
			subsystemTaggingValue = preferences.getBoolean(SUBSYSTEM_TAGGING);
		} catch (Exception e){
			Log.warning("Error accessing commons preferences, using defaults...", e);
		}
		initialized = true;
	}

}