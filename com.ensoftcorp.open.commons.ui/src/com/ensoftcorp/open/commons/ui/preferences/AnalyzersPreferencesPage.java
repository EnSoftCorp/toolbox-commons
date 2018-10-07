package com.ensoftcorp.open.commons.ui.preferences;

import java.util.Set;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ensoftcorp.open.commons.analyzers.Analyzer;
import com.ensoftcorp.open.commons.analyzers.Analyzers;
import com.ensoftcorp.open.commons.preferences.AnalyzerPreferences;
import com.ensoftcorp.open.commons.ui.Activator;
import com.ensoftcorp.open.commons.ui.components.LabelFieldEditor;

/**
 * UI for setting toolbox commons analysis preferences
 * 
 * @author Ben Holland
 */
public class AnalyzersPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private static boolean changeListenerAdded = false;

	public AnalyzersPreferencesPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(preferences);
		setDescription("Configure analyzer preferences for the Toolbox Commons plugins.");
		// use to update cached values if user edits a preference
		if(!changeListenerAdded){
			getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
					// reload the preference variable cache
					AnalyzerPreferences.loadPreferences();
				}
			});
			changeListenerAdded = true;
		}
	}

	@Override
	protected void createFieldEditors() {
		// add option to enable/disable each category of subystem
		Analyzers.loadAnalyzerContributions();
		Set<Analyzer> registeredAnalyzers = Analyzers.getRegisteredAnalyzers();
		if(!registeredAnalyzers.isEmpty()){
			addField(new LabelFieldEditor("Analyzer Result Caching", getFieldEditorParent()));
			for(Analyzer Analyzer : registeredAnalyzers){
				addField(new BooleanFieldEditor(Analyzer.getName(), "&" + (Analyzer.getName()), getFieldEditorParent()));
			}
		}
	}

}

