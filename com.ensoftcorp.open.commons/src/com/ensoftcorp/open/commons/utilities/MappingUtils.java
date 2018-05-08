package com.ensoftcorp.open.commons.utilities;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;

import com.ensoftcorp.atlas.core.index.ProjectPropertiesUtil;
import com.ensoftcorp.atlas.core.indexing.IIndexListener;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.atlas.core.licensing.AtlasLicenseException;

/**
 * A wrapper around Atlas indexing utils with added listeners for error handling.
 * 
 * @author Ben Holland
 */
public class MappingUtils {

	private MappingUtils() {}
	
	private static class IndexerError extends Exception {
		private static final long serialVersionUID = 1L;

		public IndexerError(Throwable t) {
			super(t);
		}
	}
	
	private static class IndexerErrorListener implements IIndexListener {

		private Throwable t = null;

		public boolean hasCaughtThrowable() {
			return t != null;
		}

		public Throwable getCaughtThrowable() {
			return t;
		}

		@Override
		public void indexOperationCancelled(IndexOperation io) {}

		@Override
		public void indexOperationError(IndexOperation io, Throwable t) {
			this.t = t;
		}

		@Override
		public void indexOperationStarted(IndexOperation io) {}

		@Override
		public void indexOperationComplete(IndexOperation io) {}

		@Override
		public void indexOperationScheduled(IndexOperation io) {}
	};

	/**
	 * Index the workspace (blocking mode and throws index errors)
	 * @throws AtlasLicenseException 
	 * 
	 * @throws Throwable
	 */
	public static void indexWorkspace() throws IndexerError, AtlasLicenseException {
		IndexerErrorListener errorListener = new IndexerErrorListener();
		IndexingUtil.addListener(errorListener);
		IndexingUtil.indexWorkspace(true);
		IndexingUtil.removeListener(errorListener);
		if (errorListener.hasCaughtThrowable()) {
			try {
				throw errorListener.getCaughtThrowable();
			} catch (Throwable t) {
				throw new IndexerError(t);
			}
		}
	}

	/**
	 * Configures a project for indexing
	 * @param project
	 * @throws AtlasLicenseException
	 * @throws IndexerError 
	 */
	public static void mapProject(IProject project) throws AtlasLicenseException, IndexerError {
		// disable indexing for all projects
		List<IProject> allEnabledProjects = ProjectPropertiesUtil.getAllEnabledProjects();
		ProjectPropertiesUtil.setIndexingEnabledAndDisabled(Collections.<IProject>emptySet(), allEnabledProjects);
		
		// enable indexing for this project
		List<IProject> ourProjects = Collections.singletonList(project);
		ProjectPropertiesUtil.setIndexingEnabledAndDisabled(ourProjects, Collections.<IProject>emptySet());
	
		// index in a blocking mode
		indexWorkspace();
		
		// alternatively use the Atlas apis directly
//		// TODO: set jar indexing mode to: used only (same as default)
//		IndexingUtil.indexWithSettings(/*saveIndex*/true, /*indexingSettings*/Collections.<IMappingSettings>emptySet(), ourProjects.toArray(new IProject[1]));
	}
}
