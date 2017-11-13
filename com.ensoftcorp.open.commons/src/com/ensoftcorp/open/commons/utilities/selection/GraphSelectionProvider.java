package com.ensoftcorp.open.commons.utilities.selection;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import com.ensoftcorp.atlas.core.indexing.IIndexListener;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.atlas.core.indexing.IIndexListener.IndexOperation;
import com.ensoftcorp.atlas.core.query.Q;

public class GraphSelectionProvider implements ISelectionProvider {

	private boolean enabled = true;
	private ISelection selection = new StructuredSelection();
	private CopyOnWriteArrayList<ISelectionChangedListener> changeListeners = new CopyOnWriteArrayList<ISelectionChangedListener>();

	@Override
	public ISelection getSelection() {
		return selection;
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener selectionChangedListener) {
		changeListeners.add(selectionChangedListener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener selectionChangedListener) {
		changeListeners.remove(selectionChangedListener);
	}

	@Override
	public void setSelection(ISelection selection) {
		this.selection = selection;
		SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this, getSelection());
		if(enabled){
			for(ISelectionChangedListener changeListener : changeListeners){
				changeListener.selectionChanged(selectionChangedEvent);
			}
		}
	}
	
	public void setSelection(Q selection){
		setSelection(new GraphSelectionEvent(selection));
	}

	public void enable() {
		enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}

}
