package com.ensoftcorp.open.commons.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;

/**
 * A helper for computing SourceCorrespondence objects from an XML file
 * Creates an annotated DOM tree, adding start and end line numbers to each XML element
 * 
 * @author Ben Holland
 */
public class XMLUtils {

	private XMLUtils() {}
	
	// Supported DOM annotations
	public static final String START_LINE_NUMBER_USER_DATA = "start-line-number";
	public static final String END_LINE_NUMBER_USER_DATA = "end-line-number";

	public static final String START_COLUMN_NUMBER_USER_DATA = "start-column-number";
	public static final String END_COLUMN_NUMBER_USER_DATA = "end-column-number";

	public static final String START_CHARACTER_OFFSET_USER_DATA = "start-character-offset";
	public static final String END_CHARACTER_OFFSET_USER_DATA = "end-character-offset";

	public static final String START_LINE_NUMBER_CHARACTER_OFFSET_USER_DATA = "start-line-number-character-offset";
	public static final String END_LINE_NUMBER_CHARACTER_OFFSET_USER_DATA = "end-line-number-character-offset";

	/**
	 * Returns a SourceCorrespondence from an element in an annotated DOM
	 * 
	 * @param file
	 * @param element
	 * @return
	 */
	public static SourceCorrespondence xmlElementToSourceCorrespondence(File file, Element element) {
		try {
			IFile iFile = WorkspaceUtils.getFile(file);
			int startOffset = Integer.parseInt(element.getUserData(XMLUtils.START_CHARACTER_OFFSET_USER_DATA).toString());
			int endOffset = Integer.parseInt(element.getUserData(XMLUtils.END_CHARACTER_OFFSET_USER_DATA).toString());
			SourceCorrespondence sc = SourceCorrespondence.fromString(String.valueOf(startOffset), String.valueOf(endOffset - startOffset), iFile);
			return sc;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns a SourceCorrespondence set to the first line of the XML file from
	 * an element in an annotated DOM
	 * 
	 * @param file
	 * @param leftElement
	 * @return
	 */
	public static SourceCorrespondence xmlFileToSourceCorrespondence(File file) {
		try {
			IFile iFile = WorkspaceUtils.getFile(file);
			int startOffset = 0;
			int endOffset = 0;
			SourceCorrespondence sc = SourceCorrespondence.fromString(String.valueOf(startOffset), String.valueOf(endOffset), iFile);
			return sc;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Prints the DOM Tree to the console
	 * 
	 * @param doc
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
	public static void printDOM(Document doc) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer transformer = tranFactory.newTransformer();
		Source src = new DOMSource(doc);
		Result dest = new StreamResult(System.out);
		transformer.transform(src, dest);
	}

	/**
	 * Just a helper wrapper around the getAnnotatedDOM(InputStream inputStream) method
	 */
	public static Document getAnnotatedDOM(File file) throws IOException, SAXException {
		Document document = null;
		InputStream is = null;
		try {
			String xmlString = FileUtils.readFileToString(file);
			is = new ByteArrayInputStream(xmlString.getBytes());
			document = getAnnotatedDOM(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return document;
	}

	/**
	 * Just a helper wrapper around the getAnnotatedDOM(InputStream inputStream) method
	 */
	public static Document getAnnotatedDOM(String xmlString) throws IOException, SAXException {
		InputStream is = new ByteArrayInputStream(xmlString.getBytes());
		return getAnnotatedDOM(is);
	}

	/**
	 * Returns an XML DOM Document object that has been annotated with User Data
	 * representing the line numbers and byte offsets of elements.
	 * 
	 * User data attribute names are provided as public final strings in this
	 * class. Supported: START_LINE_NUMBER_USER_DATA,
	 * START_COLUMN_NUMBER_USER_DATA, START_CHARACTER_OFFSET_USER_DATA,
	 * END_LINE_NUMBER_USER_DATA, END_COLUMN_NUMBER_USER_DATA,
	 * END_CHARACTER_OFFSET_USER_DATA
	 * 
	 * Example: Node node = doc.getElementsByTagName("foo").item(0);
	 * System.out.println("Line number: " + node.getUserData(START_LINE_NUMBER_USER_DATA));
	 * 
	 * Note: This method will close the input stream!
	 * 
	 * This method was inspired by:
	 * http://stackoverflow.com/questions/4915422/get-line-number-from-xml-node-java and
	 * http://eyalsch.wordpress.com/2010/11/30/xml-dom-2/ and
	 * http://stackoverflow.com/questions/1077865/how-do-i-get-the-correct-starting-ending-locations-of-a-xml-tag-with-sax
	 */
	public static Document getAnnotatedDOM(InputStream inputStream) throws IOException, SAXException {
		final Document document;
		try {
			// first go through the stream and find all the line breaks
			final HashMap<Integer, Integer> lineNumberOffsets = new HashMap<Integer, Integer>();
			int lineNumber = 1;
			int totalOffset = 0;

			InputStreamReader streamReader = new InputStreamReader(inputStream);
			BufferedReader reader = new BufferedReader(streamReader);
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (lineNumber == 1) {
					lineNumberOffsets.put(lineNumber, totalOffset);
				} else {
					// not the first line so add a character offset for the new
					// line
					totalOffset++;
					lineNumberOffsets.put(lineNumber, totalOffset);
				}
				lineNumber++;
				totalOffset = totalOffset + line.length();
			}

			// debug code to print line number offsets
			// for(Entry<Integer,Integer> entry : lineNumberOffsets.entrySet()){
			// System.out.println(entry.getKey() + " : " + entry.getValue());
			// }

			// reset the input stream for the SAX XML parser
			inputStream.reset();

			// start building up the DOM tree adding the SAX parser annotations
			SAXParser parser;
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				parser = factory.newSAXParser();
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				document = docBuilder.newDocument();
			} catch (ParserConfigurationException e) {
				throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
			}

			final Stack<Element> elementStack = new Stack<Element>();
			final StringBuilder accumulator = new StringBuilder();
			DefaultHandler handler = new DefaultHandler() {
				private Locator locator;

				@Override
				public void setDocumentLocator(Locator locator) {
					// save the locator, so that it can be used later for line
					// tracking when traversing nodes
					this.locator = locator;
				}

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					writeElementTextBuffer();
					Element element = document.createElement(qName);
					for (int i = 0; i < attributes.getLength(); i++) {
						element.setAttribute(attributes.getQName(i), attributes.getValue(i));
					}
					element.setUserData(START_LINE_NUMBER_USER_DATA, String.valueOf(locator.getLineNumber()), null);
					element.setUserData(START_COLUMN_NUMBER_USER_DATA, String.valueOf(locator.getColumnNumber()), null);
					element.setUserData(START_CHARACTER_OFFSET_USER_DATA, String.valueOf(lineNumberOffsets.get(locator.getLineNumber()) + locator.getColumnNumber()), null);
					element.setUserData(START_LINE_NUMBER_CHARACTER_OFFSET_USER_DATA, String.valueOf(lineNumberOffsets.get(locator.getLineNumber())), null);
					elementStack.push(element);
				}

				@Override
				public void endElement(String uri, String localName, String qName) {
					writeElementTextBuffer();
					Element element = elementStack.pop();
					if (elementStack.isEmpty()) {
						document.appendChild(element);
					} else {
						Element parentEl = elementStack.peek();
						parentEl.appendChild(element);
					}
					element.setUserData(END_LINE_NUMBER_USER_DATA, String.valueOf(locator.getLineNumber()), null);
					element.setUserData(END_COLUMN_NUMBER_USER_DATA, String.valueOf(locator.getColumnNumber()), null);
					element.setUserData(END_CHARACTER_OFFSET_USER_DATA, String.valueOf(lineNumberOffsets.get(locator.getLineNumber()) + locator.getColumnNumber()), null);
					element.setUserData(END_LINE_NUMBER_CHARACTER_OFFSET_USER_DATA, String.valueOf(lineNumberOffsets.get(locator.getLineNumber())), null);
				}

				@Override
				public void characters(char ch[], int start, int length) throws SAXException {
					accumulator.append(ch, start, length);
				}

				// outputs text accumulated under the current element
				private void writeElementTextBuffer() {
					if (accumulator.length() > 0) {
						Element element = elementStack.peek();
						Node textNode = document.createTextNode(accumulator.toString());
						element.appendChild(textNode);
						accumulator.delete(0, accumulator.length());
					}
				}
			};
			parser.parse(inputStream, handler); // start parsing the XML
		} finally {
			inputStream.close();
		}
		return document;
	}

}