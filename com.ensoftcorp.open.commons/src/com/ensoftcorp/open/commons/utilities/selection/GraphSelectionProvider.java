package com.ensoftcorp.open.commons.utilities.selection;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import com.ensoftcorp.atlas.core.log.Log;
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
		if(selectionChangedListener != null){
			changeListeners.add(selectionChangedListener);
		}
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener selectionChangedListener) {
		if(selectionChangedListener != null){
			changeListeners.remove(selectionChangedListener);
		}
	}

	@Override
	public void setSelection(ISelection selection) {
		this.selection = selection;
		SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this, getSelection());
		if(enabled){
			for(ISelectionChangedListener changeListener : changeListeners){
				try {
					changeListener.selectionChanged(selectionChangedEvent);
				} catch (Throwable t){
					Log.error("Error notifying selection change listener.", t);
				}
			}
		}
	}
	
	public void setSelection(Q selection){
		setSelection(new GraphSelectionEvent(selection));
	}
	
	public boolean isEnabled(){
		return enabled;
	}

	public void enable() {
		enabled = true;
	}
	
	public void disable() {
		enabled = false;
	}

}
