package com.ensoftcorp.open.commons.ui.views.codepainter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.ensoftcorp.open.commons.codepainter.CodePainter;
import com.ensoftcorp.open.commons.codepainter.CodePainters;
import com.ensoftcorp.open.commons.codepainter.ColorPalettes;
import com.ensoftcorp.open.commons.utilities.selection.GraphSelectionProviderView;

public class ControlPanelView extends GraphSelectionProviderView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.codepainter.controlpanel";
	
	public ControlPanelView(){
		CodePainters.loadCodePainterContributions();
		ColorPalettes.loadColorPaletteContributions();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Combo selectedCodePainterCombo = new Combo(composite, SWT.NONE);
		selectedCodePainterCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// populate the combo with the registered code painters
		for(CodePainter codePainter : CodePainters.getRegisteredCodePainters()){
			selectedCodePainterCombo.add(codePainter.getTitle());
			selectedCodePainterCombo.setData(codePainter.getTitle(), codePainter);
		}
		
		// update the selected code painter upon combo selection
		selectedCodePainterCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CodePainter codePainter = (CodePainter) selectedCodePainterCombo.getData(selectedCodePainterCombo.getText());
				if(codePainter != null){
					CodePainterSmartView.setCodePainter(codePainter);
				}
			}
		});
		
		registerGraphSelectionProvider();
	}

	@Override
	public void setFocus() {}

}
