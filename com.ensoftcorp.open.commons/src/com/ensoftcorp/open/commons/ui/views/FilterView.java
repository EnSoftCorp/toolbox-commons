package com.ensoftcorp.open.commons.ui.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.SWT;

public class FilterView extends ViewPart {
	
	public FilterView() {}

	@Override
	public void createPartControl(Composite arg0) {
		
		Tree filterTree = new Tree(arg0, SWT.BORDER);
		
	}

	@Override
	public void setFocus() {}

}
