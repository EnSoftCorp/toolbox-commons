package com.ensoftcorp.open.commons.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.utilities.OSUtils;

public class CommonsPreferences extends AbstractPreferenceInitializer {

	/**
	 * Returns the preference store used for these preferences
	 * @return
	 */
	public static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
	
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
	 * Returns true if debug logging is enabled
	 * @return
	 */
	public static boolean isDebugLoggingEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return debugLoggingValue;
	}

	/**
	 * Configure path to cloc
	 */
	public static final String CLOC_PATH = "CLOC_PATH";
	public static final String CLOC_PATH_DEFAULT = OSUtils.isWindows() ? "cloc.exe" : "cloc"; // assume clock is on the environment path
	private static String clocPathValue = CLOC_PATH_DEFAULT;
	
	/**
	 * Configures path to cloc
	 */
	public static void configureClocPath(File clocPath){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(CLOC_PATH, clocPath.getAbsolutePath());
		loadPreferences();
	}
	
	/**
	 * Returns the path to cloc
	 * @return
	 */
	public static File getClocPath(){
		if(!initialized){
			loadPreferences();
		}
		if(clocPathValue != null) {
			return new File(clocPathValue);
		} else {
			return null;
		}
	}
	
//	/**
//	 * Enable/disable initializing analysis properties
//	 */
//	public static final String INITIALIZE_ANALYSIS_PROPERTIES = "INITIALIZE_ANALYSIS_PROPERTIES";
//	public static final Boolean INITIALIZE_ANALYSIS_PROPERTIES_DEFAULT = true;
//	private static boolean initializeAnalysisPropertiesValue = INITIALIZE_ANALYSIS_PROPERTIES_DEFAULT;
//	
//	/**
//	 * Configures initializing analysis properties
//	 */
//	public static void enableInitializingAnalysisProperties(boolean enabled){
//		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
//		preferences.setValue(INITIALIZE_ANALYSIS_PROPERTIES, enabled);
//		loadPreferences();
//	}
//	
//	/**
//	 * Returns true if loop cataloging is enabled
//	 * @return
//	 */
//	public static boolean isInitializingAnalysisPropertiesEnabled(){
//		if(!initialized){
//			loadPreferences();
//		}
//		return initializeAnalysisPropertiesValue;
//	}
	
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
	 * Enable/disable ICFG construction
	 */
	public static final String CONSTRUCT_ICFG = "CONSTRUCT_ICFG";
	public static final Boolean CONSTRUCT_ICFG_DEFAULT = false;
	private static boolean constructICFGValue = CONSTRUCT_ICFG_DEFAULT;
	
	/**
	 * Configures ICFG construction
	 */
	public static void enableConstructICFG(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(CONSTRUCT_ICFG, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if ICFG construction is enabled
	 * @return
	 */
	public static boolean isConstructICFGEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return constructICFGValue;
	}
	
	/**
	 * Enable/disable computing control flow graph dominance trees
	 */
	public static final String COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE = "COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE";
	public static final Boolean COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_DEFAULT = true;
	private static boolean computeControlFlowGraphDominanceValue = COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_DEFAULT;
	
	/**
	 * Configures dominance analysis
	 */
	public static void enableComputeControlFlowGraphDominance(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if dominance analysis is enabled
	 * @return
	 */
	public static boolean isComputeControlFlowGraphDominanceEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return computeControlFlowGraphDominanceValue;
	}
	
	/**
	 * Enable/disable computing exceptional control flow graph dominance trees
	 */
	public static final String COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES = "COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES";
	public static final Boolean COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT = false;
	private static boolean computeExceptionalControlFlowGraphDominanceValue = COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DEFAULT;
	
	/**
	 * Configures inference rule logging
	 */
	public static void enableComputeExceptionalControlFlowGraphDominance(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, enabled);
		loadPreferences();
	}
	
	public static boolean isComputeExceptionalControlFlowGraphDominanceEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return computeExceptionalControlFlowGraphDominanceValue;
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
//		preferences.setDefault(INITIALIZE_ANALYSIS_PROPERTIES, INITIALIZE_ANALYSIS_PROPERTIES_DEFAULT);
		preferences.setDefault(NORMALIZE_GRAPH_ELEMENT_ADDRESSES, NORMALIZE_GRAPH_ELEMENT_ADDRESSES_DEFAULT);
		preferences.setDefault(CLOC_PATH, CLOC_PATH_DEFAULT);
		preferences.setDefault(CONSTRUCT_ICFG, CONSTRUCT_ICFG_DEFAULT);
		preferences.setDefault(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE, COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_DEFAULT);
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
//		preferences.setValue(INITIALIZE_ANALYSIS_PROPERTIES, INITIALIZE_ANALYSIS_PROPERTIES_DEFAULT);
		preferences.setValue(NORMALIZE_GRAPH_ELEMENT_ADDRESSES, NORMALIZE_GRAPH_ELEMENT_ADDRESSES_DEFAULT);
		preferences.setValue(CLOC_PATH, CLOC_PATH_DEFAULT);
		preferences.setValue(CONSTRUCT_ICFG, CONSTRUCT_ICFG_DEFAULT);
		preferences.setValue(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE, COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_DEFAULT);
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
			clocPathValue = preferences.getString(CLOC_PATH);
//			initializeAnalysisPropertiesValue = preferences.getBoolean(INITIALIZE_ANALYSIS_PROPERTIES);
			constructICFGValue = preferences.getBoolean(CONSTRUCT_ICFG);
			computeControlFlowGraphDominanceValue = preferences.getBoolean(COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE);
			computeExceptionalControlFlowGraphDominanceValue = preferences.getBoolean(COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES);
			addMasterEntryExitContainmentRelationships = preferences.getBoolean(ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS);
			displayFilterViewResultContainersValue = preferences.getBoolean(DISPLAY_FILTER_VIEW_RESULT_CONTAINERS);
		} catch (Exception e){
			Log.warning("Error accessing commons preferences, using defaults...", e);
		}
		initialized = true;
	}
}