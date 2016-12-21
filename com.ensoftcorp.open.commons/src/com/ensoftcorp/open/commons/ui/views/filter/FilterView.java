package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.ui.selection.IAtlasSelectionListener;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.filters.Filter;
import com.ensoftcorp.open.commons.filters.Filters;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.ui.components.DropdownSelectionListener;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class FilterView extends ViewPart {

	private static LinkedList<FilterTreeRoot> treeRoots = new LinkedList<FilterTreeRoot>();
	
	private Tree filterTree;
	private Combo filterSearchBar;

	// the current Atlas selection
	private Graph selection = Common.empty().eval();

	public FilterView() {
		setPartName("Filter View");
		setTitleImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/toolbox.gif"));
		
		// load plugin filter contributions
		Filters.loadFilterContributions();
	}

	@Override
	public void createPartControl(Composite parent) {
		
		SashForm sashForm = new SashForm(parent, SWT.NONE);
		
		Composite filterTreeComposite = new Composite(sashForm, SWT.NONE);
		filterTreeComposite.setLayout(new GridLayout(1, false));
		
		Label filterTreeLabel = new Label(filterTreeComposite, SWT.NONE);
		filterTreeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		filterTreeLabel.setText("Filter Tree (0 roots)");

		filterTree = new Tree(filterTreeComposite, /* SWT.CHECK | */ SWT.BORDER);
		filterTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		filterTree.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		refreshFilterTree();

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

		filterSearchBar = new Combo(controlPanelComposite, SWT.NONE);
		filterSearchBar.setEnabled(false);
		filterSearchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite filterParametersComposite = new Composite(controlPanelComposite, SWT.NONE);
		filterParametersComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		filterParametersComposite.setLayout(new GridLayout(1, false));
		filterParametersComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Group filterParametersGroup = new Group(filterParametersComposite, SWT.NONE);
		filterParametersGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		filterParametersGroup.setText("Filter Parameters");
		filterParametersGroup.setLayout(new GridLayout(1, false));
		filterParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		ScrolledComposite filterParametersScrolledComposite = new ScrolledComposite(filterParametersGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		filterParametersScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		filterParametersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		filterParametersScrolledComposite.setExpandHorizontal(true);
		filterParametersScrolledComposite.setExpandVertical(true);

		Label noParamsLabel = new Label(filterParametersScrolledComposite, SWT.NONE);
		noParamsLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		noParamsLabel.setAlignment(SWT.CENTER);
		noParamsLabel.setText("No parameters available for this filter.");
		filterParametersScrolledComposite.setContent(noParamsLabel);
		filterParametersScrolledComposite.setMinSize(noParamsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite applyFilterComposite = new Composite(filterParametersComposite, SWT.NONE);
		applyFilterComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		applyFilterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		applyFilterComposite.setLayout(new GridLayout(2, false));

		Label errorLabel = new Label(applyFilterComposite, SWT.NONE);
		errorLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		Button applyFilterButton = new Button(applyFilterComposite, SWT.NONE);
		applyFilterButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		applyFilterButton.setText("Apply Filter");
		sashForm.setWeights(new int[] { 1, 1 });
		
		// handle key release on search bar
		filterSearchBar.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				if(key.character == '\r'){
					// selection made
					Filter filter = (Filter) filterSearchBar.getData(filterSearchBar.getItem(filterSearchBar.getSelectionIndex()));
					if(filter != null){
						Log.info("SELECTED FILTER: " + filter.getName());
						// TODO: implement
					}
				} else {
					if(Character.isLetter(key.character)){
						filterSearchBar.setListVisible(false); // hide the list, we are going to modify the values
						String searchText = filterSearchBar.getText();

						// remove all items
						// note: doing this the hard way because removeAll method also clears the text
						for(String item : filterSearchBar.getItems()){
							filterSearchBar.remove(item);
						}

						// add the autocomplete suggestions for each matching permission
						for(Filter filter : getApplicableFilters()){
							if(filter.getName().toLowerCase().contains(searchText.toLowerCase())){
								filterSearchBar.add(filter.getName());
								filterSearchBar.setData(filter.getName(), filter);
							}
						}
					}
				}
			}
		});
		
		// handle apply filter button pressed
		applyFilterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// selection made
				Filter filter = (Filter) filterSearchBar.getData(filterSearchBar.getItem(filterSearchBar.getSelectionIndex()));
				if(filter != null){
					Log.info("SELECTED FILTER: " + filter.getName());
					// TODO: implement
				}
			}
		});
		
		// handle filter tree item selection
		filterTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(filterTree.getSelectionCount() == 1){
					ArrayList<Filter> applicableFilters = getApplicableFilters();
					// update the search bar with the applicable filters
					filterSearchBar.removeAll();
					for(Filter filter : applicableFilters){
						filterSearchBar.add(filter.getName());
						filterSearchBar.setData(filter.getName(), filter);
					}
					// TODO: fill out filter parameters if any
				}
			}
		});
		
		// add toolbar menu handlers
		addFileMenuItems(fileMenuDropDownItem);
		addOptionMenuItems(optionMenuDropDownItem);
		
		// setup the Atlas selection event listener
		IAtlasSelectionListener selectionListener = new IAtlasSelectionListener(){
			@Override
			public void selectionChanged(IAtlasSelectionEvent atlasSelection) {
				try {
					selection = atlasSelection.getSelection().eval();
				} catch (Exception e){
					selection = Common.empty().eval();
				}
			}				
		};
		
		// add the selection listener
		SelectionUtil.addSelectionListener(selectionListener);
	}
	
	private ArrayList<Filter> getApplicableFilters() {
		ArrayList<Filter> applicableFilters = new ArrayList<Filter>();
		if(filterTree.getSelectionCount() == 1){
			// get the input set of the selected tree item
			TreeItem treeItem = filterTree.getSelection()[0];
			Q input;
			if(treeItem.getData() instanceof FilterTreeRoot){
				FilterTreeRoot root = (FilterTreeRoot) treeItem.getData();
				input = root.getRootInput();
			} else {
				FilterTreeNode node = (FilterTreeNode) treeItem.getData();
				input = node.getInput();
			}
			// find the applicable filters
			for(Filter filter : Filters.getRegisteredFilters()){
				if(filter.isApplicableTo(input)){
					applicableFilters.add(filter);
				}
			}
			// sort the filters alphabetically
			Collections.sort(applicableFilters, new Comparator<Filter>(){
				@Override
				public int compare(Filter f1, Filter f2) {
					return f1.getName().compareTo(f2.getName());
				}
			});
		}
		return applicableFilters;
	}

	private void addFileMenuItems(ToolItem fileMenuDropDownItem) {
		DropdownSelectionListener fileListener = new DropdownSelectionListener(fileMenuDropDownItem);
		fileListener.add("Save Filter Chain", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// TODO: implement
				DisplayUtils.showError("Not Implemented!");
			}
		});
		fileListener.add("Load Filter Chain", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// TODO: implement
				DisplayUtils.showError("Not Implemented!");
			}
		});
		fileMenuDropDownItem.addSelectionListener(fileListener);
	}

	private void addOptionMenuItems(ToolItem optionMenuDropDownItem) {
		DropdownSelectionListener optionListener = new DropdownSelectionListener(optionMenuDropDownItem);
		optionListener.add("Add Selected as Root Set", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Q currentSelection = Common.toQ(selection);
				String name = DisplayUtils.promptString("Add Root Set", "Root Set Name:", false);
				if(name != null){
					try {
						treeRoots.add(new FilterTreeRoot(currentSelection, name, true));
						refreshFilterTree();
					} catch (IllegalArgumentException e){
						DisplayUtils.showError("Could not add root set. " + e.getMessage());
					} catch (Throwable t){
						DisplayUtils.showError(t, "Could not add root set.");
					}
				}
			}
		});
		optionListener.add("Rename Root Set", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(filterTree.getSelectionCount() == 1){
					TreeItem treeItem = filterTree.getSelection()[0];
					FilterTreeRoot root = (FilterTreeRoot) treeItem.getData();
					String name = DisplayUtils.promptString("Rename Root Set", "Root Set Name:", false);
					if(name != null){
						try {
							root.rename(name);
							refreshFilterTree();
						} catch (Exception e){
							DisplayUtils.showError("Could not rename root set. " + e.getMessage());
						}
					}
				} else {
					DisplayUtils.showError("Please select a root set in the filter tree to rename.");
				}
			}
		});
		optionMenuDropDownItem.addSelectionListener(optionListener);
	}

	private void refreshFilterTree() {
		filterTree.removeAll();
		for(FilterTreeRoot treeRoot : treeRoots){
			addTreeRootItem(treeRoot);
		}
	}

	public void addTreeRootItem(FilterTreeRoot root){
		TreeItem treeItem = new TreeItem(filterTree, SWT.NONE);
		treeItem.setData(root);
		treeItem.setText(root.getName());
		treeItem.setExpanded(root.isExpanded());
		for(FilterTreeNode node : root.nodes){
			addFilterTreeItem(node, treeItem);
		}
	}
	
	public void addFilterTreeItem(FilterTreeNode node, TreeItem treeItem){
		TreeItem subTreeItem = new TreeItem(treeItem, SWT.NONE);
		subTreeItem.setData(node);
		subTreeItem.setText(node.getName());
		subTreeItem.setExpanded(node.isExpanded());
		for(FilterTreeNode child : node.nodes){
			addFilterTreeItem(child, subTreeItem);
		}
	}
	
	@Override
	public void setFocus() {}
}
