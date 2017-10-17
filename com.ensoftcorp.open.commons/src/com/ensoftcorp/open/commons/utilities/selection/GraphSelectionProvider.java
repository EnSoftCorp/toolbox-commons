package com.ensoftcorp.open.commons.utilities.selection;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;

import com.ensoftcorp.atlas.core.query.Q;

public class GraphSelectionProvider implements ISelectionProvider {

	private ISelection selection;
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
		this.selection = selection;
	}
	
	public void setSelection(Q selection){
		setSelection(new GraphSelectionEvent(selection));
	}

}
