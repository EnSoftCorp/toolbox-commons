package com.ensoftcorp.open.commons.ui;

import java.util.HashMap;
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

public class AtlasToolboxPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public AtlasToolboxPreferencesPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription("Expand the preferences tree to configure settings for a specific Atlas toolbox feature.");
	}

	@Override
	protected void createFieldEditors() {}

}