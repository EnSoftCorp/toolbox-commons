package com.ensoftcorp.open.commons.utilities.project;

import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;

public abstract class AnalysisPropertiesInitializer {

	public abstract boolean supportsProject(IProject project);

	public abstract void initialize(IProject project, Document properties);

}
