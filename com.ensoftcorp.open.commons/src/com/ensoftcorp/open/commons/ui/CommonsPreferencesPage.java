package com.ensoftcorp.open.commons.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;
import com.ensoftcorp.open.commons.subsystems.Subsystem;
import com.ensoftcorp.open.commons.subsystems.Subsystems;

/**
 * UI for setting toolbox commons analysis preferences
 * 
 * @author Ben Holland
 */
public class CommonsPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String SUBSYSTEM_TAGGING_DESCRIPTION = "Tag Subsystems";
	
	private static boolean changeListenerAdded = false;

	public CommonsPreferencesPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(preferences);
		setDescription("Configure preferences for the Toolbox Commons plugins.");
		// use to update cached values if user edits a preference
		if(!changeListenerAdded){
			getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
					// reload the preference variable cache
					CommonsPreferences.loadPreferences();
				}
			});
			changeListenerAdded = true;
		}
	}

	@Override
	protected void createFieldEditors() {
		// add option to enable/disable each category of subystem
		Subsystems.loadSubsystemContributions();
		HashMap<String,String> taggingCategories = new HashMap<String,String>();
		for(Subsystem subsystem : Subsystems.getRegisteredSubsystems()){
			taggingCategories.put(subsystem.getCategory(), subsystem.getCategoryDescription());
		}
		for(Entry<String,String> taggingCategory : taggingCategories.entrySet()){
			addField(new BooleanFieldEditor(taggingCategory.getKey(), "&" + ("Tag: " + taggingCategory.getValue()), getFieldEditorParent()));
		}
	}

}

