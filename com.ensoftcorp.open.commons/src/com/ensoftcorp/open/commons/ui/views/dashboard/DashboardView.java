package com.ensoftcorp.open.commons.ui.views.dashboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.open.commons.dashboard.WorkItem;
import com.ensoftcorp.open.commons.dashboard.WorkItems;

public class DashboardView extends ViewPart {
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.dashboard.DashboardView";
	
	public DashboardView() {}
	
	private Collection<WorkItemViewComponent> workItems = new LinkedList<WorkItemViewComponent>();
	private HashMap<String, Collection<WorkItemViewComponent>> workItemTypes;
	private LinkedList<Button> filterCheckboxes = new LinkedList<Button>();
	private Button searchCheckbox;
	private Combo searchBar;
	private Button emptyFilterCheckbox;
	private Button nonEmptyFilterCheckbox;
	private Button reviewedFilterCheckbox;
	private Button unreviewedFilterCheckbox;
	private ScrolledComposite workQueueScrolledComposite;

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
		
		searchCheckbox = new Button(searchBarComposite, SWT.CHECK);
		searchCheckbox.setText("Search: ");
		
		searchBar = new Combo(searchBarComposite, SWT.NONE);
		searchBar.setEnabled(false);
		searchBar.setToolTipText("Search for a work item by typing part of the name and pressing return or selecting an autocomplete suggestion.");
		searchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		workQueueScrolledComposite = new ScrolledComposite(workComposite, SWT.BORDER | SWT.V_SCROLL);
		workQueueScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		workQueueScrolledComposite.setExpandHorizontal(true);
		workQueueScrolledComposite.setExpandVertical(true);
		
		// load the contributed work items
		for(WorkItem workItem : WorkItems.getContributions(Platform.getExtensionRegistry())){
			workItems.add(new WorkItemViewComponent(workItem));
		}
		
		// sort the work items by type
		workItemTypes = new HashMap<String,Collection<WorkItemViewComponent>>();
		for(WorkItemViewComponent workItem : workItems){
			String type = workItem.getWorkItem().getType();
			Collection<WorkItemViewComponent> types = workItemTypes.remove(type);
			if(types == null){
				types = new LinkedList<WorkItemViewComponent>();
			}
			types.add(workItem);
			workItemTypes.put(type, types);
		}
		
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
		
		
		// add the filter checkboxes
		for(String workItemType : workItemTypes.keySet()){
			final Button filterCheckbox = new Button(workItemTypeGroup, SWT.CHECK);
			filterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			filterCheckbox.setText(workItemType);
			filterCheckbox.setSelection(true);
			filterCheckboxes.add(filterCheckbox);
		}
		
		final Group workItemContentsGroup = new Group(controlPanelComposite, SWT.NONE);
		workItemContentsGroup.setLayout(new GridLayout(1, false));
		workItemContentsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		workItemContentsGroup.setText("Contents");
		workItemContentsGroup.setToolTipText("This group filters the work items by the work item contents. " 
				+ "Toggling a checkbox includes the work item for consideration in the final work item result set.");
		
		emptyFilterCheckbox = new Button(workItemContentsGroup, SWT.CHECK);
		emptyFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		emptyFilterCheckbox.setText("Empty");
		emptyFilterCheckbox.setSelection(false);
		
		nonEmptyFilterCheckbox = new Button(workItemContentsGroup, SWT.CHECK);
		nonEmptyFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		nonEmptyFilterCheckbox.setText("Non-Empty");
		nonEmptyFilterCheckbox.setSelection(true);
		
		final Group workItemStateGroup = new Group(controlPanelComposite, SWT.NONE);
		workItemStateGroup.setLayout(new GridLayout(1, false));
		workItemStateGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		workItemStateGroup.setText("State");
		workItemStateGroup.setToolTipText("This group filters the work items by the work item state. " 
				+ "Toggling a checkbox includes the work item for consideration in the final work item result set.");
		
		reviewedFilterCheckbox = new Button(workItemStateGroup, SWT.CHECK);
		reviewedFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		reviewedFilterCheckbox.setText("Reviewed");
		reviewedFilterCheckbox.setSelection(false);
		
		unreviewedFilterCheckbox = new Button(workItemStateGroup, SWT.CHECK);
		unreviewedFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		unreviewedFilterCheckbox.setText("Unreviewed");
		unreviewedFilterCheckbox.setSelection(true);
		
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
		
		refreshWorkItems();
	}

	private void refreshWorkItems() {
		Composite workQueueComposite = new Composite(workQueueScrolledComposite, SWT.NONE);
		workQueueComposite.setLayout(new GridLayout(1, false));
		
		for(Button filterCheckbox : filterCheckboxes){
			if(filterCheckbox.getSelection()){
				String type = filterCheckbox.getText();
				Collection<WorkItemViewComponent> workItems = workItemTypes.get(type);
				if(workItems != null){
					for(WorkItemViewComponent workItem : workItems){
						addWorkItem(workItem, workQueueComposite);
					}
				}
			}
		}
		
		workQueueScrolledComposite.setContent(workQueueComposite);
		workQueueScrolledComposite.setMinSize(workQueueComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void addWorkItem(WorkItemViewComponent workItem, Composite workQueueComposite) {
		ExpandBar workItemExpandBar = new ExpandBar(workQueueComposite, SWT.NONE);
		workItemExpandBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		ExpandItem workItemExpandBarItem = new ExpandItem(workItemExpandBar, SWT.NONE);
		workItemExpandBarItem.setExpanded(true);
		workItemExpandBarItem.setText(workItem.getWorkItem().getName());
		
		Composite workItemComposite = new Composite(workItemExpandBar, SWT.NONE);
		workItemExpandBarItem.setControl(workItemComposite);
		workItemComposite.setLayout(new GridLayout(1, false));
		
		Label descriptionLabel = new Label(workItemComposite, SWT.NONE);
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		descriptionLabel.setText("Description:");
		
		StyledText workItemDescription = new StyledText(workItemComposite, SWT.BORDER);
		workItemDescription.setText(workItem.getWorkItem().getDescription());
		workItemDescription.setEditable(false);
		workItemDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Label assumptionsLabel = new Label(workItemComposite, SWT.NONE);
		assumptionsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		assumptionsLabel.setText("Assumptions:");
		
		StyledText workItemAssumptions = new StyledText(workItemComposite, SWT.BORDER);
		workItemAssumptions.setText(workItem.getAssumptionsText());
		workItemAssumptions.setEditable(false);
		workItemAssumptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Button workItemReviewedCheckbox = new Button(workItemComposite, SWT.CHECK);
		workItemReviewedCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
		workItemReviewedCheckbox.setText("Reviewed");
		
		Composite workItemResultsComposite = new Composite(workItemComposite, SWT.NONE);
		workItemResultsComposite.setLayout(new GridLayout(1, false));
		workItemResultsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Button showResultsButton = new Button(workItemResultsComposite, SWT.NONE);
		showResultsButton.setText("Show Results");
		
		workItemExpandBarItem.setHeight(workItemExpandBarItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	}

	@Override
	public void setFocus() {}
}
