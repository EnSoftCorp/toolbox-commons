package com.ensoftcorp.open.commons.utilities.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ensoftcorp.open.commons.log.Log;

public class ProjectAnalysisProperties {

	private static final String PROPERTIES_PATH = "/analysis.properties.xml";
	
	public static final String ROOT_ELEMENT = "properties";
	
	private static void initializeDefaultProjectProperties(IProject project) throws Exception {
		Log.info("Initializing default analysis project properties for " + project.getName() + ".");
		AnalysisPropertiesInitializers.loadAnalysisPropertiesInitializerContributions();
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document properties = builder.newDocument();
		
		Element rootElement = properties.createElement(ROOT_ELEMENT);
		properties.appendChild(rootElement);
		
		for(AnalysisPropertiesInitializer initializer : AnalysisPropertiesInitializers.getRegisteredAnalysisPropertiesInitializers()){
			if(initializer.supportsProject(project)){
				try {
					initializer.initialize(project, properties);
				} catch (Exception e){
					Log.error("Error initializing properties", e);
				}
			}
		}
		setAnalysisProperties(project, properties);
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (Throwable t) {
			// just a best effort, we don't really care if the UI is updated
		}
	}
	
	public static Document getAnalysisProperties(IProject project) throws Exception {
		File propertiesFile = new File(project.getFile(PROPERTIES_PATH).getLocation().toOSString());
		if(!propertiesFile.exists()){
			initializeDefaultProjectProperties(project);
		}
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(new FileInputStream(propertiesFile));
	}
	
	public static void setAnalysisProperties(IProject project, Document properties) throws Exception {
		// remove blank lines
		// reference: https://stackoverflow.com/a/12670194/475329
		XPath xp = XPathFactory.newInstance().newXPath();
		NodeList nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", properties, XPathConstants.NODESET);
		for (int i=0; i < nl.getLength(); ++i) {
		    Node node = nl.item(i);
		    node.getParentNode().removeChild(node);
		}
		// save the indented xml file
		File propertiesFile = new File(project.getFile(PROPERTIES_PATH).getLocation().toOSString());
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		transformer.transform(new DOMSource(properties), new StreamResult(new FileOutputStream(propertiesFile)));
	}
	
}
