package com.ensoftcorp.open.commons.ui.utilities;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Helper class for dealing with Eclipse workspaces
 * 
 * @author Ben Holland
 */
public class WorkspaceUIUtils {

	private WorkspaceUIUtils() {}
	
	public static void openFileInEclipseEditor(File file) {
		if (file.exists() && file.isFile()) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				IDE.openEditorOnFileStore(page, fileStore);
			} catch (PartInitException e) {
				DisplayUtils.showError(e, "Could not display file: " + file.getAbsolutePath());
			}
		} else {
			MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.OK);
			mb.setText("Alert");
			mb.setMessage("Could not find file: " + file.getAbsolutePath());
			mb.open();
		}
	}

}