package com.ensoftcorp.open.commons.utilities.selection;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import com.ensoftcorp.atlas.core.query.Q;

public class GraphSelectionProvider implements ISelectionProvider {

	private boolean enabled = true;
	private ISelection selection = new StructuredSelection();
	private Set<ISelectionChangedListener> changeListeners = new HashSet<ISelectionChangedListener>();

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
		if(enabled){
			this.selection = selection;
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
