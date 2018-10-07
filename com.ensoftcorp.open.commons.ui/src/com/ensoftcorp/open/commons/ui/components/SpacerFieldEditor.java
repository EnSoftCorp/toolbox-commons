package com.ensoftcorp.open.commons.ui.components;

import org.eclipse.swt.widgets.Composite;

/**
 * A field editor for adding space to a preference page
 * 
 * Original source: https://www.eclipse.org/articles/Article-Field-Editors/field_editors.html#conclusion
 */
public class SpacerFieldEditor extends LabelFieldEditor {
	// Implemented as an empty label field editor.
	public SpacerFieldEditor(Composite parent) {
		super("", parent);
	}
}