package com.ensoftcorp.open.commons.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;

import com.ensoftcorp.atlas.core.query.Q;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class FilterView extends ViewPart {

//	private static LinkedList<>
	
	private Tree filterTree;
	private Combo searchFilterBar;
	
	public FilterView() {
		setPartName("Filter View");
		setTitleImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/toolbox.gif"));
	}

	@Override
	public void createPartControl(Composite parent) {
		
		SashForm sashForm = new SashForm(parent, SWT.NONE);

		filterTree = new Tree(sashForm, /* SWT.CHECK | */ SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		TreeItem treeItem = new TreeItem(filterTree, SWT.NONE);
		treeItem.setText("New TreeItem");
		
		TreeItem trtmNewTreeitem = new TreeItem(treeItem, SWT.NONE);
		trtmNewTreeitem.setText("New TreeItem");
		treeItem.setExpanded(true);

		Composite controlPanelComposite = new Composite(sashForm, SWT.NONE);
		controlPanelComposite.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar = new ToolBar(controlPanelComposite, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		ToolItem fileMenuDropDownItem = new ToolItem(toolBar, SWT.DROP_DOWN);
		fileMenuDropDownItem.setText("File");
		
		ToolItem optionMenuDropDownItem = new ToolItem(toolBar, SWT.DROP_DOWN);
		optionMenuDropDownItem.setText("Option");
		
		Label label = new Label(controlPanelComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label applicableFiltersLabel = new Label(controlPanelComposite, SWT.NONE);
		applicableFiltersLabel.setText("(0/0) Filters Applicable");

		searchFilterBar = new Combo(controlPanelComposite, SWT.NONE);
		searchFilterBar.setEnabled(false);
		searchFilterBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite filterParametersComposite = new Composite(controlPanelComposite, SWT.NONE);
		filterParametersComposite.setLayout(new GridLayout(1, false));
		filterParametersComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
				Group filterParametersGroup = new Group(filterParametersComposite, SWT.NONE);
				filterParametersGroup.setText("Filter Parameters");
				filterParametersGroup.setLayout(new GridLayout(1, false));
				filterParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				
						ScrolledComposite filterParametersScrolledComposite = new ScrolledComposite(filterParametersGroup,
								SWT.H_SCROLL | SWT.V_SCROLL);
						filterParametersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
						filterParametersScrolledComposite.setExpandHorizontal(true);
						filterParametersScrolledComposite.setExpandVertical(true);
						
						Label noParamsLabel = new Label(filterParametersScrolledComposite, SWT.NONE);
						noParamsLabel.setAlignment(SWT.CENTER);
						noParamsLabel.setText("No parameters available for this filter.");
						filterParametersScrolledComposite.setContent(noParamsLabel);
						filterParametersScrolledComposite.setMinSize(noParamsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Composite applyFilterComposite = new Composite(filterParametersComposite, SWT.NONE);
		applyFilterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		applyFilterComposite.setLayout(new GridLayout(2, false));
				
				Label errorLabel = new Label(applyFilterComposite, SWT.NONE);
				errorLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
				errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
				Button applyFilterButton = new Button(applyFilterComposite, SWT.NONE);
				applyFilterButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				applyFilterButton.setText("Apply Filter");
		sashForm.setWeights(new int[] { 1, 1 });

	}

	public void addRootTreeItem(String name, Q input){
		
	}
	
	public void addFilterToTreeItem(String name, Q input){
		
	}
	
	@Override
	public void setFocus() {}
}
