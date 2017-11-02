package com.ensoftcorp.open.commons.ui.views.codepainter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;

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
		
		Composite searchCodePaintersComposite = new Composite(parent, SWT.NONE);
		searchCodePaintersComposite.setLayout(new GridLayout(2, false));
		searchCodePaintersComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button searchCodePaintersCheckbox = new Button(searchCodePaintersComposite, SWT.CHECK);
		searchCodePaintersCheckbox.setText("Search Code Painters: ");
		
		Combo selectedCodePainterCombo = new Combo(searchCodePaintersComposite, SWT.NONE);
		selectedCodePainterCombo.setEnabled(false);
		selectedCodePainterCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group browseCodePaintersGroup = new Group(parent, SWT.NONE);
		browseCodePaintersGroup.setLayout(new GridLayout(1, false));
		browseCodePaintersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browseCodePaintersGroup.setText("Browse Code Painters:");
		
		Tree categorizedCodePaintersTree = new Tree(browseCodePaintersGroup, SWT.BORDER);
		categorizedCodePaintersTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		// populate the combo with the registered code painters
		selectedCodePainterCombo.removeAll();
		ArrayList<CodePainter> codePainters = new ArrayList<CodePainter>(CodePainters.getRegisteredCodePainters());
		Collections.sort(codePainters, new Comparator<CodePainter>(){
			@Override
			public int compare(CodePainter cp1, CodePainter cp2) {
				int categoryComparison = cp1.getCategory().compareTo(cp2.getCategory());
				if(categoryComparison == 0){
					return cp1.getTitle().compareTo(cp2.getTitle());
				} else {
					return categoryComparison;
				}
			}
		});
		for(CodePainter codePainter : codePainters){
			selectedCodePainterCombo.add(codePainter.getTitle());
			selectedCodePainterCombo.setData(codePainter.getTitle(), codePainter);
		}
		
		// add code painters to categorized tree
		categorizedCodePaintersTree.removeAll();
		for(CodePainter codePainter : codePainters){
			boolean categoryExists = false;
			for(TreeItem category : categorizedCodePaintersTree.getItems()){
				if(category.getText().equals(codePainter.getCategory())){
					TreeItem item = new TreeItem(category, SWT.NONE);
					item.setText(codePainter.getTitle());
					item.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/brush.gif"));
					item.setData(codePainter);
					categoryExists = true;
					break;
				}
			}
			if(!categoryExists){
				TreeItem category = new TreeItem(categorizedCodePaintersTree, SWT.NONE);
				category.setText(codePainter.getCategory());
				category.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/folder.gif"));
				TreeItem item = new TreeItem(category, SWT.NONE);
				item.setText(codePainter.getTitle());
				item.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/brush.gif"));
				item.setData(codePainter);
			}
		}
		
		categorizedCodePaintersTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(categorizedCodePaintersTree.getSelectionCount() == 1){
					CodePainter codePainter = (CodePainter) categorizedCodePaintersTree.getSelection()[0].getData();
					if(codePainter != null){
						CodePainterSmartView.setCodePainter(codePainter);
						selectedCodePainterCombo.setText(codePainter.getTitle());
					}
				}
			}
		});
		
		searchCodePaintersCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = searchCodePaintersCheckbox.getSelection();
				categorizedCodePaintersTree.setEnabled(!enabled);
				selectedCodePainterCombo.setEnabled(enabled);
			}
		});
		
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
