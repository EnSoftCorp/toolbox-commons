package com.ensoftcorp.open.commons.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ensoftcorp.open.commons.preferences.CommonsPreferences;

/**
 * UI for setting toolbox commons analysis preferences
 * 
 * @author Ben Holland
 */
public class CommonsPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String DEBUG_LOGGING_DESCRIPTION = "Debug logging";
	private static final String NORMALIZE_GRAPH_ELEMENT_ADDRESSES_DESCRIPTION = "Compute normalized graph element addresses";
	private static final String CLOC_PATH_DESCRIPTION = "Path to CLOC logical line of code counter";
	private static final String CONSTRUCT_ICFG_DESCRIPTION = "Construct inter-procedural control flow graphs (ICFGs)";
	private static final String COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_DESCRIPTION = "Compute control flow graph dominance";
	private static final String COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DESCRIPTION = "Compute exceptional control flow graph dominance";
	private static final String ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS_DESCRIPTION = "Add master entry/exit containment relationships";
	private static final String DISPLAY_FILTER_VIEW_RESULT_CONTAINERS_DESCRIPTION = "Display container relationships in filter view results";
	
	private static boolean changeListenerAdded = false;
	
	public CommonsPreferencesPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferences = CommonsPreferences.getPreferenceStore();
		setPreferenceStore(preferences);
		setDescription("Configure preferences for the Toolbox Commons plugin. Expand the preferences tree to configure settings for additional Toolbox Commons plugin features.");
		
		// use to update cached values if user edits a preference
		if(!changeListenerAdded){
			getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
					CommonsPreferences.loadPreferences();
				}
			});
			changeListenerAdded = true;
		}
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(CommonsPreferences.DEBUG_LOGGING, "&" + DEBUG_LOGGING_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CommonsPreferences.NORMALIZE_GRAPH_ELEMENT_ADDRESSES, "&" + NORMALIZE_GRAPH_ELEMENT_ADDRESSES_DESCRIPTION, getFieldEditorParent()));
		addField(new FileFieldEditor(CommonsPreferences.CLOC_PATH, "&" + CLOC_PATH_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CommonsPreferences.CONSTRUCT_ICFG, "&" + CONSTRUCT_ICFG_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CommonsPreferences.COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE, "&" + COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CommonsPreferences.COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, "&" + COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CommonsPreferences.ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS, "&" + ADD_MASTER_ENTRY_EXIT_CONTAINMENT_RELATIONSHIPS_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CommonsPreferences.DISPLAY_FILTER_VIEW_RESULT_CONTAINERS, "&" + DISPLAY_FILTER_VIEW_RESULT_CONTAINERS_DESCRIPTION, getFieldEditorParent()));
	}
	
}

