package com.ensoftcorp.open.commons.codemap;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.utilities.WorkspaceUtils;
import com.ensoftcorp.open.commons.utilities.project.ProjectAnalysisProperties;

public class ProjectAnalysisDefaultPropertiesInitializer extends PrioritizedCodemapStage {

	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.utilities.project.propertiesinitializer";
	
	@Override
	public String getDisplayName() {
		return "Project Analysis Properties Initializer";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{}; // no dependencies
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		for(Node projectNode : Common.universe().nodes(XCSG.Project).eval().nodes()){
			IProject project = WorkspaceUtils.getProject(projectNode.getAttr(XCSG.name).toString());
			if(project.exists() && project.isOpen() && project.isAccessible()){
				try {
					ProjectAnalysisProperties.initializeDefaultProjectProperties(project);
				} catch (IOException e) {
					Log.error("Error initializing " + project.getName() + " analysis properties.", e);
				}
			}
		}
	}

}
