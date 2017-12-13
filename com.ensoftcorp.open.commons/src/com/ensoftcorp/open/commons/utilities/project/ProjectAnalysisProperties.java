package com.ensoftcorp.open.commons.utilities.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.open.commons.log.Log;

public class ProjectAnalysisProperties {

	private static final String PROPERTIES_PATH = "/analysis.properties";
	
	private static void initializeDefaultProjectProperties(IProject project) throws IOException {
		if(!IndexingUtil.indexExists()){
			throw new RuntimeException("Index does not exist.");
		} else {
			Log.info("Initializing default analysis project properties for " + project.getName() + ".");
			AnalysisPropertiesInitializers.loadAnalysisPropertiesInitializerContributions();
			File propertiesFile = new File(project.getFile(PROPERTIES_PATH).getLocation().toOSString());
			Properties properties = new Properties();
			for(AnalysisPropertiesInitializer initializer : AnalysisPropertiesInitializers.getRegisteredAnalysisPropertiesInitializers()){
				if(initializer.supportsProject(project)){
					try {
						initializer.initialize(project, properties);
					} catch (Exception e){
						Log.error("Error initializing properties", e);
					}
				}
			}
			properties.store(new FileWriter(propertiesFile), "Project Analysis Properties");
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (Throwable t) {
				// just a best effort, we don't really care if the UI is updated
			}
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
