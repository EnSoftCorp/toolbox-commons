package com.ensoftcorp.open.commons.utilities;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
public class WorkspaceUtils {

	private WorkspaceUtils() {}
	
	/**
	 * Returns a project in the workspace for the given project name
	 * @param projectName
	 * @return
	 */
	public static IProject getProject(String projectName){
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}
	
	/**
	 * Converts a File to an Eclipse IFile Source:
	 * http://stackoverflow.com/questions/960746/how-to-convert-from-file-to-ifile-in-java-for-files-outside-the-project
	 * 
	 * @param file
	 * @return
	 */
	public static IFile getFile(File file) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath location = Path.fromOSString(file.getAbsolutePath());
		IFile iFile = workspace.getRoot().getFileForLocation(location);
		return iFile;
	}
	
	/**
	 * Converts an IFile to an Java File Source:
	 * 
	 * @param file
	 * @return
	 */
	public static File getFile(IFile iFile) {
		File file = getFile(iFile, true);
		if(file == null){
			// file does not exist, but we should at least return a file
			file = getFile(iFile, false);
		}
		return file;
	}
	
	/**
	 * Converts an IFile to an Java File Source:
	 * 
	 * @param file
	 * @return
	 */
	private static File getFile(IFile iFile, boolean checkExists) {
		File file = null;
		
		// generally this is all we need
		if(iFile.getLocation() != null){
			file = iFile.getLocation().toFile();
			if(!file.exists()){
				file = null;
			}
		}
		
		// however Eclispe is weird so we have some fallbacks
		if(file == null && iFile.getRawLocation() != null){
			file = iFile.getRawLocation().toFile();
			if(!file.exists()){
				file = null;
			}
		}
		
		// in the worst case this method should work
		if(file == null){
			file = new File(iFile.getFullPath().toOSString());
			if(!file.exists()){
				file = null;
			}
		}
		
		return file;
	}
	
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