package com.ensoftcorp.open.commons.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class CommonsPreferences extends AbstractPreferenceInitializer {

	private static boolean initialized = false;
	
	/**
	 * Enable/disable debug logging
	 */
	public static final String DEBUG_LOGGING = "DEBUG_LOGGING";
	public static final Boolean DEBUG_LOGGING_DEFAULT = false;
	private static boolean debugLoggingValue = DEBUG_LOGGING_DEFAULT;
	
	/**
	 * Configures debug logging
	 */
	public static void enableDebugLogging(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(DEBUG_LOGGING, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if loop cataloging is enabled
	 * @return
	 */
	public static boolean isDebugLoggingEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return debugLoggingValue;
	}
	
	/**
	 * Enable/disable node address normalization
	 */
	public static final String NORMALIZE_GRAPH_ELEMENT_ADDRESSES = "NORMALIZE_GRAPH_ELEMENT_ADDRESSES";
	public static final Boolean NORMALIZE_GRAPH_ELEMENT_ADDRESSES_DEFAULT = true;
	private static boolean normalizeGraphElementAddressesValue = NORMALIZE_GRAPH_ELEMENT_ADDRESSES_DEFAULT;
	
	/**
	 * Configures address normalization
	 */
	public static void enableAddressNormalization(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(NORMALIZE_GRAPH_ELEMENT_ADDRESSES, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if address normalization is enabled
	 * @return
	 */
	public static boolean isAddressNormalizationEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return normalizeGraphElementAddressesValue;
	}
	
	/**
	 * Enable/disable computing control flow graph dominance trees
	 */
	public static final String COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES = "COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES";
	public static final Boolean COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT = false;
	private static boolean computeControlFlowGraphDominanceTreesValue = COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT;
	
	/**
	 * Configures dominance analysis
	 */
	public static void enableComputeControlFlowGraphDominanceTrees(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if dominance analysis is enabled
	 * @return
	 */
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
	
	/**
	 * Enable/disable adding master entry/exit containment relationships
	 */
	public static final String ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS = "ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS";
	public static final Boolean ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS_DEFAULT = true;
	private static boolean addMasterEntryExitContainmentRelationships = ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS_DEFAULT;
	
	/**
	 * Configures inference rule logging
	 */
	public static void enableMasterEntryExitContainmentRelationships(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS, enabled);
		loadPreferences();
	}
	
	public static boolean isMasterEntryExitContainmentRelationshipsEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return addMasterEntryExitContainmentRelationships;
	}
	
	/**
	 * Enable/disable displaying filter view result containers
	 */
	public static final String DISPLAY_FILTER_VIEW_RESULT_CONTAINERS = "DISPLAY_FILTER_VIEW_RESULT_CONTAINERS";
	public static final Boolean DISPLAY_FILTER_VIEW_RESULT_CONTAINERS_DEFAULT = false;
	private static boolean displayFilterViewResultContainersValue = DISPLAY_FILTER_VIEW_RESULT_CONTAINERS_DEFAULT;

	/**
	 * Configures displaying filter view result containers
	 */
	public static void enableDisplayFilterViewResultContainers(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(DISPLAY_FILTER_VIEW_RESULT_CONTAINERS, enabled);
		loadPreferences();
	}

	public static boolean isDisplayFilterViewResultContainersEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return displayFilterViewResultContainersValue;
	}
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setDefault(DEBUG_LOGGING, DEBUG_LOGGING_DEFAULT);
		preferences.setDefault(NORMALIZE_GRAPH_ELEMENT_ADDRESSES, NORMALIZE_GRAPH_ELEMENT_ADDRESSES_DEFAULT);
		preferences.setDefault(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT);
		preferences.setDefault(COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT);
		preferences.setDefault(ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS, ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS_DEFAULT);
		preferences.setDefault(DISPLAY_FILTER_VIEW_RESULT_CONTAINERS, DISPLAY_FILTER_VIEW_RESULT_CONTAINERS_DEFAULT);
	}
	
	/**
	 * Restores the default preferences
	 */
	public static void restoreDefaults(){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(DEBUG_LOGGING, DEBUG_LOGGING_DEFAULT);
		preferences.setValue(NORMALIZE_GRAPH_ELEMENT_ADDRESSES, NORMALIZE_GRAPH_ELEMENT_ADDRESSES_DEFAULT);
		preferences.setValue(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT);
		preferences.setValue(COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT);
		preferences.setValue(ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS, ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS_DEFAULT);
		preferences.setValue(DISPLAY_FILTER_VIEW_RESULT_CONTAINERS, DISPLAY_FILTER_VIEW_RESULT_CONTAINERS_DEFAULT);
		loadPreferences();
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
			debugLoggingValue = preferences.getBoolean(DEBUG_LOGGING);
			normalizeGraphElementAddressesValue = preferences.getBoolean(NORMALIZE_GRAPH_ELEMENT_ADDRESSES);
			computeControlFlowGraphDominanceTreesValue = preferences.getBoolean(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES);
			computeExceptionalControlFlowGraphDominanceTreesValue = preferences.getBoolean(COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES);
			addMasterEntryExitContainmentRelationships = preferences.getBoolean(ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS);
			displayFilterViewResultContainersValue = preferences.getBoolean(DISPLAY_FILTER_VIEW_RESULT_CONTAINERS);
		} catch (Exception e){
			Log.warning("Error accessing commons preferences, using defaults...", e);
		}
		initialized = true;
	}
}