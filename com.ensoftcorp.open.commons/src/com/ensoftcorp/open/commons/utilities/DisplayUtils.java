package com.ensoftcorp.open.commons.utilities;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.text.WordUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;
import com.ensoftcorp.atlas.ui.viewer.graph.SaveUtil;
import com.ensoftcorp.open.commons.ui.components.InputDialog;

/**
 * A set of helper utilities for some common display related methods
 * 
 * @author Ben Holland
 */
public class DisplayUtils {

	private final static long LARGE_GRAPH_WARNING = 100;

	/**
	 * Returns a string entered by the user, returns null if the user canceled the prompt
	 * @param title
	 * @param message
	 * @return
	 */
	public static String promptString(String title, String message) {
		return promptString(title, message, true);
	}
	
	/**
	 * Returns a string entered by the user, returns null if the user canceled the prompt
	 * @param title
	 * @param message
	 * @param blocking set to false if the dialog should be non-blocking
	 * @return
	 */
	public static String promptString(String title, String message, boolean blocking) {
	    InputDialog prompt = new InputDialog(Display.getCurrent().getActiveShell(), blocking, title, message);
	    return prompt.open();
	}

	/**
	 * Opens a display prompt alerting the user of the error 
	 * 
	 * @param message the message to display
	 */
	public static void showError(final String message) {
		final Display display = Display.getCurrent();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox mb = new MessageBox(display.getActiveShell(), SWT.ICON_ERROR | SWT.OK);
				mb.setText("Alert");
				mb.setMessage(message);
				mb.open();
			}
		});
	}
	
	/**
	 * Opens a display prompt alerting the user of the error and offers the
	 * ability to copy a stack trace to the clipboard
	 * 
	 * @param t the throwable to grab stack trace from
	 * @param message the message to display
	 */
	public static void showError(final Throwable t, final String message) {
		final Display display = Display.getCurrent();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox mb = new MessageBox(display.getActiveShell(), SWT.ICON_ERROR | SWT.NO | SWT.YES);
				mb.setText("Alert");
				StringWriter errors = new StringWriter();
				t.printStackTrace(new PrintWriter(errors));
				String stackTrace = errors.toString();
				mb.setMessage(message + "\n\nWould you like to copy the stack trace?");
				int response = mb.open();
				if (response == SWT.YES) {
					StringSelection stringSelection = new StringSelection(stackTrace);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, stringSelection);
				}
			}
		});
	}
	
	/**
	 * Opens a display prompt showing a message
	 * @param message
	 */
	public static void showMessage(final String message){
		final Display display = Display.getCurrent();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox mb = new MessageBox(display.getActiveShell(), SWT.ICON_INFORMATION | SWT.OK);
				mb.setText("Message");
				mb.setMessage(message);
				mb.open();
			}
		});
	}

	/**
	 * A show method for a single graph element
	 * Defaults to extending and no highlighting
	 * @param ge The GraphElement to show
	 * @param title A title to indicate the graph content
	 */
	public static void show(final GraphElement ge, final String title) {
		show(Common.toQ(ge), title);
	}
	
	/**
	 * A show method for a single graph element
	 * @param ge The GraphElement to show
	 * @param h An optional highlighter, set to null otherwise
	 * @param extend A boolean to define if the graph should be extended (typical use is true)
	 * @param title A title to indicate the graph content
	 */
	public static void show(final GraphElement ge, final Highlighter h, final boolean extend, final String title) {
		show(Common.toQ(ge), h, extend, title);
	}
	
	/**
	 * Shows a graph inside Atlas
	 * Defaults to extending and no highlighting
	 * @param ge The GraphElement to show
	 * @param title A title to indicate the graph content
	 */
	public static void show(final Q q, final String title) {
		show(q, null, true, title);
	}
	
	/**
	 * Shows a graph inside Atlas
	 * 
	 * @param q The query to show
	 * @param h An optional highlighter, set to null otherwise
	 * @param extend A boolean to define if the graph should be extended (typical use is true)
	 * @param title A title to indicate the graph content
	 */
	public static void show(final Q q, final Highlighter h, final boolean extend, final String title) {
		final Display display = Display.getCurrent();
		display.syncExec(new Runnable() {
			public void run() {
				try {
					long graphSize = CommonQueries.nodeSize(q);
					boolean showGraph = false;
					if (graphSize > LARGE_GRAPH_WARNING) {
						MessageBox mb = new MessageBox(display.getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
						mb.setText("Warning");
						mb.setMessage("The graph you are about to display has " + graphSize + " nodes.  " 
								+ "Displaying large graphs may cause Eclipse to become unresponsive." 
								+ "\n\nDo you want to continue?");
						int response = mb.open();
						if (response == SWT.YES) {
							showGraph = true; // user says let's do it!!
						}
					} else {
						// graph is small enough to display
						showGraph = true;
					}
					if (showGraph) {
						Q extended = Common.universe().edgesTaggedWithAny(XCSG.Contains).reverse(q).union(q);
						Q displayExpr = extend ? extended : q;
						DisplayUtil.displayGraph(displayExpr.eval(), (h != null ? h : new Highlighter()), title);
					}
				} catch (Exception e){
					DisplayUtils.showError(e, "Could not display graph.");
				}
			}
		});
	}

	/**
	 * Saves the given Q to a file as an image
	 * 
	 * @param q
	 * @param h
	 * @param extend
	 * @param title
	 * @throws InterruptedException
	 */
	public static void save(Q q, IMarkup markup, boolean extend, String title, File directory) throws InterruptedException {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("File must be a directory");
		}
		if (markup == null){
			markup = new Markup();
		}
		File outputFile = new File(directory.getAbsolutePath() + File.separatorChar + title.toLowerCase().replaceAll("\\s+", " ").replaceAll(" ", "_") + ".png");
		if (extend){
//			q = Common.extend(q, XCSG.Contains); // extends only in ##index
			q = Common.universe().edgesTaggedWithAny(XCSG.Contains).reverse(q).union(q);
		}
		org.eclipse.core.runtime.jobs.Job job = SaveUtil.saveGraph(outputFile, q.eval(), markup);
		job.join(); // block until save is complete
	}

	/**
	 * Given a string of text, inserts new lines at the given length boundary
	 * 
	 * @param text
	 *            The text to wrap, pre-existing newlines reset the counter
	 * @return A string with new lines inserted for line wrap lengths
	 */
	public static String wrapText(String text, int boundary) {
		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			lines[i] = WordUtils.wrap(lines[i], boundary, "\n", false);
		}
		String result = "";
		for (String line : lines) {
			result += line + "\n";
		}
		return result.trim();
	}

	/**
	 * Creates a qualified class name
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getQualifiedClassName(Object object) {
		if(object instanceof Class){
			return ((Class) object).getName();
		}
		return object.getClass().getPackage().toString().replace("package ", "") + "." + object.getClass().getSimpleName();
	}

}
