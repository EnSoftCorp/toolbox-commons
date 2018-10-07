package com.ensoftcorp.open.commons.codemap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.utilities.WorkspaceUtils;
import com.ensoftcorp.open.commons.utilities.address.NormalizedAddress;
import com.ensoftcorp.open.commons.utilities.project.ProjectAnalysisProperties;

public class ProjectAnalysisDefaultPropertiesInitializer extends PrioritizedCodemapStage {

	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.utilities.project.properties.initializer";
	
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
		// just in case toolboxes decide to take advantage of it (and normalization is enabled)
		// then make sure the initializers run after normalization
		return new String[]{ NormalizedAddress.IDENTIFIER };
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
//		if(CommonsPreferences.isInitializingAnalysisPropertiesEnabled()) {
			for(Node projectNode : Query.universe().nodes(XCSG.Project).eval().nodes()){
				IProject project = WorkspaceUtils.getProject(projectNode.getAttr(XCSG.name).toString());
				if(project.exists() && project.isOpen() && project.isAccessible()){
					try {
						// initialize project properties if they are not there already
						ProjectAnalysisProperties.getAnalysisProperties(project);
					} catch (Exception e) {
						Log.error("Error initializing " + project.getName() + " analysis properties.", e);
					}
				}
			}
//		}
	}

}
