package com.ensoftcorp.open.commons.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A menu handler for configuring the toolbox commons analysis preferences
 * 
 * @author Ben Holland
 */
public class CommonsPreferencesHandler extends AbstractHandler {
	public CommonsPreferencesHandler() {}

	/**
	 * Runs the immutability analysis
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String id = "com.ensoftcorp.open.commons.ui.preferences";
		return PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), id, new String[] {id}, null).open();
	}
	
}