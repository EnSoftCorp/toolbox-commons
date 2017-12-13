package com.ensoftcorp.open.commons.utilities.project;

import java.util.Properties;

import org.eclipse.core.resources.IProject;

public abstract class AnalysisPropertiesInitializer {

	public abstract boolean supportsProject(IProject project);

	public abstract void initialize(IProject project, Properties properties);

}
