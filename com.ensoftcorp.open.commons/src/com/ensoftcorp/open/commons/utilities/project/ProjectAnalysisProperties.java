package com.ensoftcorp.open.commons.utilities.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;

import com.ensoftcorp.atlas.core.indexing.IndexingUtil;

public class ProjectAnalysisProperties {

	private static final String PROPERTIES_PATH = "/analysis.properties";

	public static void initializeDefaultProjectProperties(IProject project) throws IOException {
		if(!IndexingUtil.indexExists()){
			throw new RuntimeException("Index does not exist.");
		} else {
			File propertiesFile = new File(project.getFile(PROPERTIES_PATH).getLocation().toOSString());
			Properties properties = new Properties();
			for(ProjectPropertyInitializer initializer : ProjectPropertyInitializers.getProjectPropertyInitializers()){
				if(initializer.supportsProject(project)){
					initializer.initialize(project, properties);
				}
			}
			properties.store(new FileWriter(propertiesFile), "Project Analysis Properties");
		}
	}
	
	public static Properties getAnalysisProperties(IProject project) throws Exception {
		File propertiesFile = new File(project.getFile(PROPERTIES_PATH).getLocation().toOSString());
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertiesFile));
		} catch (FileNotFoundException e) {
			initializeDefaultProjectProperties(project);
		} catch (Exception e) {
			throw e;
		}
		return properties;
	}
	
	public static void setAnalysisProperties(IProject project, Properties properties) throws IOException {
		File propertiesFile = new File(project.getFile(PROPERTIES_PATH).getLocation().toOSString());
		properties.store(new FileWriter(propertiesFile), "Project Analysis Properties");
	}
	
}
