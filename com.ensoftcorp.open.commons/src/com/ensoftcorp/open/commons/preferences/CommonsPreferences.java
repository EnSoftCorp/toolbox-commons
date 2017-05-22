package com.ensoftcorp.open.commons.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class CommonsPreferences extends AbstractPreferenceInitializer {

	private static boolean initialized = false;
	
	/**
	 * Enable/disable computing control flow graph dominance trees
	 */
	public static final String COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES = "COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES";
	public static final Boolean COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT = false;
	private static boolean computeControlFlowGraphDominanceTreesValue = COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT;
	
	/**
	 * Configures inference rule logging
	 */
	public static void enableComputeControlFlowGraphDominanceTrees(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, enabled);
		loadPreferences();
	}
	
	public static boolean isComputeControlFlowGraphDominanceTreesEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return computeControlFlowGraphDominanceTreesValue;
	}
	
	/**
	 * Enable/disable computing exceptional control flow graph dominance trees
	 */
	public static final String COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES = "COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES";
	public static final Boolean COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT = false;
	private static boolean computeExceptionalControlFlowGraphDominanceTreesValue = COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT;
	
	/**
	 * Configures inference rule logging
	 */
	public static void enableComputeExceptionalControlFlowGraphDominanceTrees(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, enabled);
		loadPreferences();
	}
	
	public static boolean isComputeExceptionalControlFlowGraphDominanceTreesEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return computeExceptionalControlFlowGraphDominanceTreesValue;
	}
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setDefault(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT);
		preferences.setDefault(COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT);
	}
	
	/**
	 * Restores the default preferences
	 */
	public static void restoreDefaults(){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT);
		loadPreferences();
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
			computeControlFlowGraphDominanceTreesValue = preferences.getBoolean(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES);
			computeExceptionalControlFlowGraphDominanceTreesValue = preferences.getBoolean(COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES);
		} catch (Exception e){
			Log.warning("Error accessing commons preferences, using defaults...", e);
		}
		initialized = true;
	}
}