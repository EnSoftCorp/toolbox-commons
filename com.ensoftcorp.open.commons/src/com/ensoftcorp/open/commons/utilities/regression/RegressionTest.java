/**
 * 
 */
package com.ensoftcorp.open.commons.utilities.regression;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.open.commons.utilities.MappingUtils;
import com.ensoftcorp.open.commons.utilities.WorkspaceUtils;

/**
 * A helper class for importing, mapping, and removing known projects before and after tests
 * 
 * @author Ben Holland
 */
public class RegressionTest {
	
	public static void setUpBeforeClass(Bundle bundle, String relativeArchivedProjectPath, String projectName) throws Exception {
		// delete the project if it exists already
		IProject project = WorkspaceUtils.getProject(projectName);
		if(project != null && project.exists()) {
			WorkspaceUtils.deleteProject(project);
		}
		
		// import a fresh copy of the project
		try {
			Job job = WorkspaceUtils.importProjectFromArchivedResource(bundle, relativeArchivedProjectPath, projectName);
			job.join(); // block and wait until import is complete
			project = WorkspaceUtils.getProject(projectName);
			if(project != null && project.exists()) {
				MappingUtils.mapProject(project);
			} else {
				throw new RuntimeException("Error importing test project: " + relativeArchivedProjectPath);
			}			
		} catch (Exception e) {
			Log.error("Error searching for project:" + relativeArchivedProjectPath, e);
		}
	}

	public static void tearDownAfterClass(String projectName) throws Exception {
		// clean up and delete the project
		IProject project = WorkspaceUtils.getProject(projectName);
		if(project != null && project.exists()) {
			WorkspaceUtils.deleteProject(project);
		}
	}

}
