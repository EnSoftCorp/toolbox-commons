package com.ensoftcorp.open.commons;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.ensoftcorp.open.commons"; //$NON-NLS-1$
	
	// plugin extension point IDs
	public static final String PLUGIN_ANALYZER_EXTENSION_ID = "com.ensoftcorp.open.commons.analyzer"; //$NON-NLS-1$
	public static final String PLUGIN_SUBSYSTEM_EXTENSION_ID = "com.ensoftcorp.open.commons.subsystem"; //$NON-NLS-1$
	public static final String PLUGIN_FILTER_EXTENSION_ID = "com.ensoftcorp.open.commons.filter"; //$NON-NLS-1$
	public static final String PLUGIN_FILTERABLE_ROOT_SET_EXTENSION_ID = "com.ensoftcorp.open.commons.filter.rootset"; //$NON-NLS-1$
	public static final String PLUGIN_CODEMAP_EXTENSION_ID = "com.ensoftcorp.open.commons.codemap"; //$NON-NLS-1$
	public static final String PLUGIN_WORKITEM_EXTENSION_ID = "com.ensoftcorp.open.commons.dashboard.workitem"; //$NON-NLS-1$
	public static final String PLUGIN_LANGUAGE_SPECIFIC_ANALYSIS_EXTENSION_ID = "com.ensoftcorp.open.commons.language.analysis"; //$NON-NLS-1$
	public static final String PLUGIN_CODE_PAINTER_EXTENSION_ID = "com.ensoftcorp.open.commons.codepainter"; //$NON-NLS-1$
	public static final String PLUGIN_COLOR_PALETTE_EXTENSION_ID = "com.ensoftcorp.open.commons.colorpalette"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
