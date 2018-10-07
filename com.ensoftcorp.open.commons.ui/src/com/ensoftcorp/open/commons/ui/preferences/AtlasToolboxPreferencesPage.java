package com.ensoftcorp.open.commons.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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