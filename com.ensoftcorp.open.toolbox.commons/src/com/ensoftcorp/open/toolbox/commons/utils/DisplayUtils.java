package com.ensoftcorp.open.toolbox.commons.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.text.WordUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.ensoftcorp.atlas.java.core.highlight.Highlighter;
import com.ensoftcorp.atlas.java.core.query.Attr.Edge;
import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;
import com.ensoftcorp.atlas.ui.viewer.graph.DisplayUtil;
import com.ensoftcorp.atlas.ui.viewer.graph.SaveUtil;

/**
 * A set of helper utilities for some common display related methods
 * 
 * @author Ben Holland
 */
public class DisplayUtils {

	private final static long LARGE_GRAPH_WARNING = 100;

	public static void centerWindow(Shell shell) {
		// center window on primary display to start
		Display display = Display.getDefault();
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
	}

	/**
	 * Opens a display prompt alerting the user of the error and offers the
	 * ability to copy a stack trace to the clipboard
	 * 
	 * @param t the throwable to grab stack trace from
	 * @param message the message to display
	 */
	public static void showError(final Throwable t, final String message) {
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR | SWT.NO | SWT.YES);
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
	 * Helper method for logging stack traces to a file
	 * @param t
	 * @param log
	 */
	public static void logErrorToFile(Throwable t, File log, boolean append){
		try {
			FileWriter fw = new FileWriter(log, append);
			StringWriter errors = new StringWriter();
			t.printStackTrace(new PrintWriter(errors));
			String stackTrace = errors.toString();
			fw.write("" + System.currentTimeMillis() + "\n");
			fw.write(stackTrace + "\n\n");
			fw.close();
		} catch (Exception e){
			// what do you do when the back up to the back up fails?
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens a display prompt showing a message
	 * @param message
	 */
	public static void showMessage(final String message){
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_INFORMATION | SWT.OK);
				mb.setText("Message");
				mb.setMessage(message);
				mb.open();
			}
		});
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
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			public void run() {
				long graphSize = CommonQueries.nodeSize(q);
				boolean showGraph = false;

				if (graphSize > LARGE_GRAPH_WARNING) {
					MessageBox mb = new MessageBox(new Shell(display), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
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
					Q displayExpr = extend ? Common.extend(q, Edge.DECLARES) : q;
					DisplayUtil.displayGraph(displayExpr.eval(), (h != null ? h : new Highlighter()), title);
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
	public static void save(Q q, Highlighter h, boolean extend, String title, File directory) throws InterruptedException {
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("File must be a directory");
		}
		if (h == null){
			h = new Highlighter();
		}
		File outputFile = new File(directory.getAbsolutePath() + File.separatorChar + title.toLowerCase().replaceAll("\\s+", " ").replaceAll(" ", "_") + ".png");
		if (extend){
			q = Common.extend(q, Edge.DECLARES); // FIXME: [jdm] context should be from toolbox, not ##index
		}
		org.eclipse.core.runtime.jobs.Job job = SaveUtil.saveGraph(outputFile, q.eval(), h);
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
	 * Creates a qualified class name (stripping out dollar signs found in inner java class and Scala class representations)
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String formatClassName(Object object) {
		if(object instanceof Class){
			return ((Class) object).getName().replace("$", "");
		}
		return object.getClass().getPackage().toString().replace("package ", "") + "." + object.getClass().getSimpleName().replace("$", "");
	}

	/**
	 * Resizes an SWT image
	 * Modified from original source at http://stackoverflow.com/questions/4752748/swt-how-to-do-high-quality-image-resize
	 * TODO: Fix known issue, turns transparent backgrounds black...not the best for App icons
	 * @param image
	 * @param w
	 * @param h
	 * @return
	 */
	public static org.eclipse.swt.graphics.Image resize(org.eclipse.swt.graphics.Image image, int w, int h) {
		// convert to buffered image
		BufferedImage img = convertToAWT(image.getImageData());

		// determine scaling mode for best result: if downsizing, use area
		// averaging, if upsizing, use smooth scaling
		// (usually bilinear).
		int mode = (image.getBounds().width > w) || (image.getBounds().height > h) ? BufferedImage.SCALE_AREA_AVERAGING : BufferedImage.SCALE_SMOOTH;
		java.awt.Image scaledImage = img.getScaledInstance(w, h, mode);

		// convert the scaled image back to a buffered image
		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		img.getGraphics().drawImage(scaledImage, 0, 0, null);

		// reconstruct swt image
		ImageData imageData = convertToSWT(img);
		return new org.eclipse.swt.graphics.Image(Display.getDefault(), imageData);
	}

	/**
	 * Resizes an image, using the given scaling factor. Constructs a new image
	 * resource, please take care of resource disposal if you no longer need the
	 * original one. This method is optimized for quality, not for speed.
	 * 
	 * @param image
	 *            source image
	 * @param scale
	 *            scale factor (<1 = downscaling, >1 = upscaling)
	 * @return scaled image
	 */
	public static org.eclipse.swt.graphics.Image resize(org.eclipse.swt.graphics.Image image, float scale) {
		int w = image.getBounds().width;
		int h = image.getBounds().height;

		// convert to buffered image
		BufferedImage img = convertToAWT(image.getImageData());

		// resize buffered image
		int newWidth = Math.round(scale * w);
		int newHeight = Math.round(scale * h);

		// determine scaling mode for best result: if downsizing, use area
		// averaging, if upsizing, use smooth scaling
		// (usually bilinear).
		int mode = scale < 1 ? BufferedImage.SCALE_AREA_AVERAGING : BufferedImage.SCALE_SMOOTH;
		java.awt.Image scaledImage = img.getScaledInstance(newWidth, newHeight, mode);

		// convert the scaled image back to a buffered image
		img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		img.getGraphics().drawImage(scaledImage, 0, 0, null);

		// reconstruct swt image
		ImageData imageData = convertToSWT(img);
		return new org.eclipse.swt.graphics.Image(Display.getDefault(), imageData);
	}

	/**
	 * Converts an SWT image to an AWT image
	 * @param data
	 * @return
	 */
	public static BufferedImage convertToAWT(ImageData data) {
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					pixelArray[0] = rgb.red;
					pixelArray[1] = rgb.green;
					pixelArray[2] = rgb.blue;
					raster.setPixels(x, y, 1, 1, pixelArray);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = palette.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte) rgb.red;
				green[i] = (byte) rgb.green;
				blue[i] = (byte) rgb.blue;
			}
			if (data.transparentPixel != -1) {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
			} else {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
			}
			BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					pixelArray[0] = pixel;
					raster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}

	/**
	 * Converts an AWT image to an SWT image
	 * @param bufferedImage
	 * @return
	 */
	public static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
	}

	public static void openFileInEclipseEditor(File file) {
		if (file.exists() && file.isFile()) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				IDE.openEditorOnFileStore(page, fileStore);
			} catch (PartInitException e) {
				showError(e, "Could not display file: " + file.getAbsolutePath());
			}
		} else {
			MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.OK);
			mb.setText("Alert");
			mb.setMessage("Could not find file: " + file.getAbsolutePath());
			mb.open();
		}
	}

}