package com.ensoftcorp.open.commons.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;

/**
 * UI for setting toolbox commons analysis preferences
 * 
 * @author Ben Holland
 */
public class CommonsPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DESCRIPTION = "Compute control flow graph dominance trees";
	private static final String COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DESCRIPTION = "Compute exceptional control flow graph dominance trees";
	
	private static boolean changeListenerAdded = false;
	
	public CommonsPreferencesPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
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
		addField(new BooleanFieldEditor(CommonsPreferences.COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, "&" + COMPUTE_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CommonsPreferences.COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES, "&" + COMPUTE_EXCEPTIONAL_CONTROL_FLOW_GRAPH_DOMINANCE_TREES_DESCRIPTION, getFieldEditorParent()));
	}
	
}

