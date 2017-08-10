package com.ensoftcorp.open.commons.utilities;

import com.ensoftcorp.atlas.core.indexing.IIndexListener;

public class CodeMapChangeListener implements IIndexListener {

	private boolean indexHasChanged = false;
	
	public boolean hasIndexChanged(){
		return indexHasChanged;
	}
	
	public void reset(){
		indexHasChanged = false;
	}
	
	@Override
	public void indexOperationCancelled(IndexOperation io) {
		indexHasChanged = true;
	}

	@Override
	public void indexOperationError(IndexOperation io, Throwable t) {
		indexHasChanged = true;
	}

	@Override
	public void indexOperationStarted(IndexOperation io) {
		indexHasChanged = true;
	}

	@Override
	public void indexOperationComplete(IndexOperation io) {
		indexHasChanged = true;
	}

	@Override
	public void indexOperationScheduled(IndexOperation io) {}
};