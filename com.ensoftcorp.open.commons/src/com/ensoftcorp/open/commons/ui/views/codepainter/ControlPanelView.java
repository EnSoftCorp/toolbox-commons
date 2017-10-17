package com.ensoftcorp.open.commons.ui.views.codepainter;

import org.eclipse.swt.widgets.Composite;

import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.utilities.selection.GraphSelectionProviderView;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ControlPanelView extends GraphSelectionProviderView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.codepainter.controlpanel";
	
	public ControlPanelView(){}
	
	@Override
	public void createPartControl(Composite arg0) {

		Button btnShow = new Button(arg0, SWT.NONE);
		btnShow.setText("Show");
		
		btnShow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setSelection(Common.types("File"));
			}
		});
		
		registerGraphSelectionProvider();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
