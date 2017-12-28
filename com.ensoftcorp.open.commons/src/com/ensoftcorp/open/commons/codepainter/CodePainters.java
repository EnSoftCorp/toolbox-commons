package com.ensoftcorp.open.commons.codepainter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.atlas.core.indexing.IIndexListener;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.atlas.core.indexing.IIndexListener.IndexOperation;
import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class CodePainters {

	private static Set<CodePainter> CODE_PAINTERS = Collections.synchronizedSet(new HashSet<CodePainter>());
	private static IIndexListener indexListener = null;
	
	public CodePainters(){
		// index listener clears registered code painter color palette canvases on index change
		if(indexListener == null){
			indexListener = new IIndexListener(){
				@Override
				public void indexOperationCancelled(IndexOperation op) {}
	
				@Override
				public void indexOperationComplete(IndexOperation op) {}
	
				@Override
				public void indexOperationError(IndexOperation op, Throwable error) {}
	
				@Override
				public void indexOperationScheduled(IndexOperation op) {}
	
				@Override
				public void indexOperationStarted(IndexOperation op) {
					for(CodePainter codePainter : CODE_PAINTERS){
						for(ColorPalette colorPalette : codePainter.getColorPalettes()){
							// TODO: clear canvas
//							colorPalette.clearCanvas();
						}
					}
				}
			};
			
			// add the index listener
			IndexingUtil.addListener(indexListener);
		}
	}

	/**
	 * Returns a copy of the currently registered codePainters
	 * 
	 * @return
	 */
	public static Set<CodePainter> getRegisteredCodePainters() {
		HashSet<CodePainter> codePainters = new HashSet<CodePainter>();
		for (CodePainter codePainter : CODE_PAINTERS) {
			codePainters.add(codePainter);
		}
		return codePainters;
	}

	/**	
	 * Registers the contributed plugin codePainter definitions
	 */
	public static void loadCodePainterContributions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_CODE_PAINTER_EXTENSION_ID);
		try {
			for (IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof CodePainter) {
					CodePainter codePainter = (CodePainter) o;
					registerCodePainter(codePainter);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading codePainters.", e);
		}
	}

	/**
	 * Registers a new codePainter
	 * 
	 * @param codePainter
	 */
	private static synchronized void registerCodePainter(CodePainter codePainter) {
		CODE_PAINTERS.add(codePainter);
	}

	/**
	 * Unregisters a codePainter
	 * 
	 * @param codePainter
	 */
	@SuppressWarnings("unused")
	private static synchronized void unregisterCodePainter(CodePainter codePainter) {
		CODE_PAINTERS.remove(codePainter);
	}
	
}
