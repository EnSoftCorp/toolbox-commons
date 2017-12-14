package com.ensoftcorp.open.commons.ui.views.dashboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ExpandAdapter;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.analyzers.Analyzer;
import com.ensoftcorp.open.commons.analyzers.Analyzer.Result;
import com.ensoftcorp.open.commons.analyzers.Analyzers;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;
import com.ensoftcorp.open.commons.utilities.selection.GraphSelectionListenerView;

public class DashboardView extends GraphSelectionListenerView {
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.dashboard.DashboardView";
	
	public DashboardView() {}
	
	private Set<WorkItem> workItems = new HashSet<WorkItem>();
	private HashMap<String, Set<WorkItem>> workItemCategories;
	private LinkedList<Button> filterCheckboxes = new LinkedList<Button>();
	private Button searchCheckbox;
	private Combo searchBar;
	private Button emptyFilterCheckbox;
	private Button nonEmptyFilterCheckbox;
	private Button reviewedFilterCheckbox;
	private Button unreviewedFilterCheckbox;
	private Button initializedFilterCheckbox;
	private Button uninitializedFilterCheckbox;
	private ScrolledComposite workQueueScrolledComposite;
	
	private static boolean needsRefresh = false;
	
	public static void refreshRequired(){
		needsRefresh = true;
	}

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
		loadWorkItems();
		
		// categorize work items
		workItemCategories = new HashMap<String,Set<WorkItem>>();
		categorizeWorkItems();
		
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
		for(String workItemType : workItemCategories.keySet()){
			final Button filterCheckbox = new Button(workItemTypeGroup, SWT.CHECK);
			filterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			filterCheckbox.setText(workItemType);
			filterCheckbox.setSelection(true);
			filterCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshWorkItems();
				}
			});
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
		emptyFilterCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshWorkItems();
			}
		});
		
		nonEmptyFilterCheckbox = new Button(workItemContentsGroup, SWT.CHECK);
		nonEmptyFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		nonEmptyFilterCheckbox.setText("Non-Empty");
		nonEmptyFilterCheckbox.setSelection(true);
		nonEmptyFilterCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshWorkItems();
			}
		});
		
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
		reviewedFilterCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshWorkItems();
			}
		});
		
		unreviewedFilterCheckbox = new Button(workItemStateGroup, SWT.CHECK);
		unreviewedFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		unreviewedFilterCheckbox.setText("Unreviewed");
		unreviewedFilterCheckbox.setSelection(true);
		unreviewedFilterCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshWorkItems();
			}
		});
		
		initializedFilterCheckbox = new Button(workItemStateGroup, SWT.CHECK);
		initializedFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		initializedFilterCheckbox.setText("Initialized");
		initializedFilterCheckbox.setSelection(true);
		initializedFilterCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshWorkItems();
			}
		});
		
		uninitializedFilterCheckbox = new Button(workItemStateGroup, SWT.CHECK);
		uninitializedFilterCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		uninitializedFilterCheckbox.setText("Uninitialized");
		uninitializedFilterCheckbox.setSelection(false);
		uninitializedFilterCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshWorkItems();
			}
		});
		
		searchBar.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				// don't update results for ctrl-keys
				// such as ctrl-a for select all, consider command key for macs
				if((key.stateMask & SWT.CTRL) == SWT.CTRL || (key.stateMask & SWT.COMMAND) == SWT.COMMAND){
					return;
				}
				
		        // refresh on returns
		        if(key.character == '\r' || key.character == '\n'){
					refreshWorkItems();
				} 
		        
		        // if its a alphabetic character then update the search results
		        // consider backspace or delete as updates as well
		        else if(key.character == '\u0008' || key.character == '\u007F' || Character.isAlphabetic(key.character)){
					// hide the list we are going to modify the values
					searchBar.setListVisible(false); 
					
					// save the search text
					String searchText = searchBar.getText();

					// remove all items
					// note: doing this the hard way because removeAll method also clears the text
					for(String item : searchBar.getItems()){
						searchBar.remove(item);
					}
					
					for(WorkItem workItem : workItems){
						if(workItem.getAnalyzer().getName().toLowerCase().contains(searchBar.getText().toLowerCase().trim())){
							searchBar.add(workItem.getAnalyzer().getName());
						}
					}
					
					// for some reason the previous actions are clearing the search text on some OS's so restoring it now
					searchBar.setText(searchText);
					
					// make sure the cursor selection is at the end
					searchBar.setSelection(new Point(searchText.length(), searchText.length()));
					
					// refresh the work items with the search results
					refreshWorkItems();
				}
			}
		});
		
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
				refreshWorkItems();
			}
		});
		
		sashForm.setWeights(new int[] {125, 450});
		new Label(parent, SWT.NONE);
		
		// add the work items
		refreshWorkItems();
		
		this.registerGraphHandlers();
	}

	private void loadWorkItems() {
		workItems.clear();
		Analyzers.loadAnalyzerContributions();
		for(Analyzer analyzer : Analyzers.getRegisteredAnalyzers()){
			workItems.add(new WorkItem(analyzer));
		}
	}

	private void categorizeWorkItems() {
		// sort the work items by type
		workItemCategories.clear();
		for(WorkItem workItem : workItems){
			String category = workItem.getAnalyzer().getCategory();
			Set<WorkItem> categories = workItemCategories.remove(category);
			if(categories == null){
				categories = new HashSet<WorkItem>();
			}
			categories.add(workItem);
			workItemCategories.put(category, categories);
		}
	}

	private void refreshWorkItems() {
		loadWorkItems();
		
		// update the category sorting
		for(WorkItem workItem : workItems){
			String category = workItem.getAnalyzer().getCategory();
			Set<WorkItem> categories = workItemCategories.remove(category);
			if(categories == null){
				categories = new HashSet<WorkItem>();
			}
			categories.add(workItem);
			workItemCategories.put(category, categories);
		}
		
		// mark any work items that are completed as initialized
		for(WorkItem workItem : workItems){
			if(!workItem.isInitialized()){
				Analyzer analyzer = workItem.getAnalyzer();
				if(Analyzers.hasCachedResult(analyzer)){
					List<Result> results = Analyzers.getAnalyzerResults(analyzer);
					workItem.initialize(results);
				}
			}
		}
		
		// save the old scroll position and content origin
		int scrollPosition = workQueueScrolledComposite.getVerticalBar().getSelection();
		org.eclipse.swt.graphics.Point origin = workQueueScrolledComposite.getOrigin();
		
		// create a new composite to store the work items on
		Composite workQueueComposite = new Composite(workQueueScrolledComposite, SWT.NONE);
		workQueueComposite.setLayout(new GridLayout(1, false));

		if(searchCheckbox.getSelection()){
			// show work items by search name
			// but add them by categories to be consistent
			for(Button filterCheckbox : filterCheckboxes){
				String category = filterCheckbox.getText();
				Set<WorkItem> workItems = workItemCategories.get(category);
				if(workItems != null){
					for(WorkItem workItem : workItems){
						Analyzer analyzer = workItem.getAnalyzer();
						if(analyzer.getName().toLowerCase().contains(searchBar.getText().toLowerCase().trim())){
							addWorkItem(workItem, workQueueComposite);
						}
					}
				}
			}
		} else {
			// show filtered search items
			for(Button filterCheckbox : filterCheckboxes){
				if(filterCheckbox.getSelection()){
					String category = filterCheckbox.getText();
					Collection<WorkItem> workItems = workItemCategories.get(category);
					if(workItems != null){
						for(WorkItem workItem : workItems){
							// consider reviewed state
							if((initializedFilterCheckbox.getSelection() && workItem.isInitialized()) || (uninitializedFilterCheckbox.getSelection() && !workItem.isInitialized())){
								if((reviewedFilterCheckbox.getSelection() && workItem.isReviewed()) || (unreviewedFilterCheckbox.getSelection() && !workItem.isReviewed())){
									if((emptyFilterCheckbox.getSelection() && workItem.isEmpty()) || (nonEmptyFilterCheckbox.getSelection() && !workItem.isEmpty())){
										addWorkItem(workItem, workQueueComposite);
									}
								}
							}
						}
					}
				}
			}
		}
		
		workQueueScrolledComposite.setContent(workQueueComposite);
		workQueueScrolledComposite.setMinSize(workQueueComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		// set the scroll position on redraw
		workQueueScrolledComposite.getVerticalBar().setSelection(scrollPosition);
		workQueueScrolledComposite.setOrigin(origin);
	}

	private void addWorkItem(WorkItem workItem, Composite workQueueComposite) {
		ExpandBar workItemExpandBar = new ExpandBar(workQueueComposite, SWT.NONE);
		workItemExpandBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		ExpandItem workItemExpandBarItem = new ExpandItem(workItemExpandBar, SWT.NONE);
		workItemExpandBarItem.setExpanded(workItem.isExpanded());
		String initializedState = "";
		if(!workItem.isInitialized()){
			initializedState = " [RESULTS PENDING]";
		}
		workItemExpandBarItem.setText(workItem.getAnalyzer().getCategory() + ": " + workItem.getAnalyzer().getName() + initializedState);
		
		Composite workItemComposite = new Composite(workItemExpandBar, SWT.NONE);
		workItemExpandBarItem.setControl(workItemComposite);
		workItemComposite.setLayout(new GridLayout(1, false));
		
		Label descriptionLabel = new Label(workItemComposite, SWT.NONE);
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		descriptionLabel.setText("Description:");
		
		StyledText workItemDescription = new StyledText(workItemComposite, SWT.BORDER);
		workItemDescription.setText(workItem.getAnalyzer().getDescription());
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
		workItemReviewedCheckbox.setSelection(workItem.isReviewed());
		workItemReviewedCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				workItem.setReviewed(workItemReviewedCheckbox.getSelection());
				// since checking the reviewed box will end up hiding the
				// content, lets go ahead and collapse the expand content
				workItem.setContentExpanded(false);
				refreshWorkItems();
			}
		});
		
		Composite workItemResultsComposite = new Composite(workItemComposite, SWT.NONE);
		workItemResultsComposite.setLayout(new GridLayout(1, false));
		workItemResultsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Button showResultsButton = new Button(workItemResultsComposite, SWT.NONE);
		showResultsButton.setText("Show All Results");
		
		showResultsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Analyzer analyzer = workItem.getAnalyzer();
				if(Analyzers.hasCachedResult(analyzer)){
					List<Result> results = Analyzers.getAnalyzerResults(analyzer);
					DisplayUtils.show(Analyzer.getAllResults(results), analyzer.getName());
				} else {
					Boolean answer = DisplayUtils.promptBoolean("Result has not been computed.", "Would you like to compute it now?");
					if(answer != null && answer.booleanValue() == true){
						Job job = new Job("Analyzing: " + analyzer.getName()){
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								Log.info("Analyzing: " + analyzer.getName());
								List<Result> results = analyzer.getResults(Common.universe()); // TODO: update context
								
								// initailize the work item and cache the results
								workItem.initialize(results);
								Analyzers.cacheResult(analyzer, results);
								
								// show the newly compute results
								DisplayUtils.show(Analyzer.getAllResults(results), analyzer.getName());
								
								// update the work items
								Display.getDefault().syncExec(new Runnable(){
									@Override
									public void run() {
										refreshWorkItems();
									}
								});
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
				}
			}
		});
		
		workItemExpandBarItem.setHeight(workItemExpandBarItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		// tracking the expand state of the work items
		final ExpandAdapter expandAdapter = new ExpandAdapter() {
			@Override
			public void itemCollapsed(ExpandEvent e) {
				workItem.setContentExpanded(false);
				refreshWorkItems();
			}

			@Override
			public void itemExpanded(ExpandEvent e) {
				workItem.setContentExpanded(true);
				refreshWorkItems();
			}
		};
		workItemExpandBar.addExpandListener(expandAdapter);
	}

	@Override
	public void setFocus() {
		if(needsRefresh){
			refreshWorkItems();
			needsRefresh = false;
		}
	}

	@Override
	public void selectionChanged(Graph selection) {}

	@Override
	public void indexBecameUnaccessible() {
		Analyzers.clearCachedResults();
		loadWorkItems();
		categorizeWorkItems();
		refreshWorkItems();
	}

	@Override
	public void indexBecameAccessible() {}
}
