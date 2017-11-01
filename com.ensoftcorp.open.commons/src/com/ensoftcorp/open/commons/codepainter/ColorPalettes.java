package com.ensoftcorp.open.commons.codepainter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ensoftcorp.open.commons.Activator;
import com.ensoftcorp.open.commons.log.Log;

public class ColorPalettes {

	private static Set<ColorPalette> COLOR_PALETTES = Collections.synchronizedSet(new HashSet<ColorPalette>());

	/**
	 * Returns a copy of the currently registered colorPalettes
	 * 
	 * @return
	 */
	public static Set<ColorPalette> getRegisteredColorPalettes() {
		HashSet<ColorPalette> colorPalettes = new HashSet<ColorPalette>();
		for (ColorPalette colorPalette : COLOR_PALETTES) {
			colorPalettes.add(colorPalette);
		}
		return colorPalettes;
	}

	/**	
	 * Registers the contributed plugin colorPalette definitions
	 */
	public static void loadColorPaletteContributions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] config = registry.getConfigurationElementsFor(Activator.PLUGIN_COLOR_PALETTE_EXTENSION_ID);
		try {
			for (IConfigurationElement element : config) {
				final Object o = element.createExecutableExtension("class");
				if (o instanceof ColorPalette) {
					ColorPalette colorPalette = (ColorPalette) o;
					registerColorPalette(colorPalette);
				}
			}
		} catch (CoreException e) {
			Log.error("Error loading colorPalettes.", e);
		}
	}

	/**
	 * Registers a new colorPalette
	 * 
	 * @param colorPalette
	 */
	private static synchronized void registerColorPalette(ColorPalette colorPalette) {
		COLOR_PALETTES.add(colorPalette);
	}

	/**
	 * Unregisters a colorPalette
	 * 
	 * @param colorPalette
	 */
	@SuppressWarnings("unused")
	private static synchronized void unregisterColorPalette(ColorPalette colorPalette) {
		COLOR_PALETTES.remove(colorPalette);
	}
	
}
