package com.ensoftcorp.open.commons.dashboard.ui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.open.commons.dashboard.work.WorkItem;

public class DashboardView extends ViewPart {
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.dashboard.ui.DashboardView";
	
	public DashboardView() {}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		final SashForm sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setToolTipText("Click and drag to resize.");
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite controlPanelComposite = new Composite(sashForm, SWT.BORDER);
		controlPanelComposite.setLayout(new GridLayout(1, false));
		controlPanelComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		
		Composite workComposite = new Composite(sashForm, SWT.BORDER);
		workComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		workComposite.setLayout(new GridLayout(1, false));
		
		Composite searchBarComposite = new Composite(workComposite, SWT.BORDER);
		searchBarComposite.setLayout(new GridLayout(2, false));
		searchBarComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button searchCheckbox = new Button(searchBarComposite, SWT.CHECK);
		searchCheckbox.setText("Search: ");
		
		final Combo searchBar = new Combo(searchBarComposite, SWT.NONE);
		searchBar.setEnabled(false);
		searchBar.setToolTipText("Search for a work item by typing part of the name and pressing return or selecting an autocomplete suggestion.");
		searchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite workQueueComposite = new Composite(workComposite, SWT.BORDER | SWT.V_SCROLL);
		workQueueComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		ArrayList<WorkItem> workItems = new ArrayList<WorkItem>();
		
		
		Label workItemFiltersLabel = new Label(controlPanelComposite, SWT.NONE);
		workItemFiltersLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.BOLD));
		workItemFiltersLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		workItemFiltersLabel.setText("Work Item Filters");
		
		final Group workItemTypeGroup = new Group(controlPanelComposite, SWT.NONE);
		workItemTypeGroup.setLayout(new GridLayout(1, false));
		workItemTypeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		workItemTypeGroup.setText("Type");
		workItemTypeGroup.setToolTipText("This group filters the work items by the work item type. " 
				+ "Toggling a checkbox includes the work item for consideration in the final work item result set.");
		
		final Button propertiesFilterCheckbox = new Button(workItemTypeGroup, SWT.CHECK);
		propertiesFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		propertiesFilterCheckbox.setText("Properties");
		
		final Button smellsFilterCheckbox = new Button(workItemTypeGroup, SWT.CHECK);
		smellsFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		smellsFilterCheckbox.setText("Smells");
		
		final Group workItemContentsGroup = new Group(controlPanelComposite, SWT.NONE);
		workItemContentsGroup.setLayout(new GridLayout(1, false));
		workItemContentsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		workItemContentsGroup.setText("Contents");
		workItemContentsGroup.setToolTipText("This group filters the work items by the work item contents. " 
				+ "Toggling a checkbox includes the work item for consideration in the final work item result set.");
		
		final Button emptyFilterCheckbox = new Button(workItemContentsGroup, SWT.CHECK);
		emptyFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		emptyFilterCheckbox.setText("Empty");
		
		final Button nonEmptyFilterCheckbox = new Button(workItemContentsGroup, SWT.CHECK);
		nonEmptyFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		nonEmptyFilterCheckbox.setText("Non-Empty");
		
		final Group workItemStateGroup = new Group(controlPanelComposite, SWT.NONE);
		workItemStateGroup.setLayout(new GridLayout(1, false));
		workItemStateGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		workItemStateGroup.setText("State");
		workItemStateGroup.setToolTipText("This group filters the work items by the work item state. " 
				+ "Toggling a checkbox includes the work item for consideration in the final work item result set.");
		
		final Button reviewedFilterCheckbox = new Button(workItemStateGroup, SWT.CHECK);
		reviewedFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		reviewedFilterCheckbox.setText("Reviewed");
		
		final Button unreviewedFilterCheckbox = new Button(workItemStateGroup, SWT.CHECK);
		unreviewedFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		unreviewedFilterCheckbox.setText("Unreviewed");
		
		searchCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(searchCheckbox.getSelection()){
					for(Control control : workItemTypeGroup.getChildren()){
						control.setEnabled(false);
					}
					for(Control control : workItemContentsGroup.getChildren()){
						control.setEnabled(false);
					}
					for(Control control : workItemStateGroup.getChildren()){
						control.setEnabled(false);
					}
					searchBar.setEnabled(true);
				} else {
					for(Control control : workItemTypeGroup.getChildren()){
						control.setEnabled(true);
					}
					for(Control control : workItemContentsGroup.getChildren()){
						control.setEnabled(true);
					}
					for(Control control : workItemStateGroup.getChildren()){
						control.setEnabled(true);
					}
					searchBar.clearSelection();
					searchBar.setText("");
					searchBar.setEnabled(false);
				}
			}
		});
		
		sashForm.setWeights(new int[] {125, 450});
		new Label(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {}
}
