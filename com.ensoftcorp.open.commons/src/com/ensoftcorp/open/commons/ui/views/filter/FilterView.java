package com.ensoftcorp.open.commons.ui.views.filter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.ui.selection.IAtlasSelectionListener;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.filters.Filter;
import com.ensoftcorp.open.commons.filters.Filters;
import com.ensoftcorp.open.commons.filters.rootset.FilterableRootset;
import com.ensoftcorp.open.commons.filters.rootset.FilterableRootsets;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;
import com.ensoftcorp.open.commons.ui.components.DropdownSelectionListener;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;

public class FilterView extends ViewPart {

	private static final int FONT_SIZE = 11;
	
	private static LinkedList<FilterRootNode> treeRoots = new LinkedList<FilterRootNode>();
	
	private Tree filterTree;
	private Label filterTreeLabel;
	private Label applicableFiltersLabel;
	private Combo filterSearchBar;
	private Label filterDescriptionText;
	private ScrolledComposite filterParametersScrolledComposite;
	private Button applyFilterButton;
	private Label errorLabel;
	
	// the filter parameters to be used when applying a filter
	private Map<String,Object> filterParameters = new HashMap<String,Object>();
	
	// the current Atlas selection
	private Graph selection = Common.empty().eval();

	public FilterView() {
		setPartName("Filter View");
		setTitleImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/toolbox.gif"));
		
		// load plugin filter contributions
		Filters.loadFilterContributions();
		FilterableRootsets.loadFilterContributions();
	}

	@Override
	public void createPartControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.NONE);
		
		Composite filterTreeComposite = new Composite(sashForm, SWT.NONE);
		filterTreeComposite.setLayout(new GridLayout(1, false));
		
		filterTreeLabel = new Label(filterTreeComposite, SWT.NONE);
		filterTreeLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		filterTreeLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		filterTreeLabel.setText("Filter Tree (0 roots)");

		filterTree = new Tree(filterTreeComposite, SWT.SINGLE | SWT.VIRTUAL | /* SWT.CHECK | */ SWT.BORDER);
		filterTree.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
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

		applicableFiltersLabel = new Label(controlPanelComposite, SWT.NONE);
		applicableFiltersLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		applicableFiltersLabel.setText("(0/" + Filters.getRegisteredFilters().size() + ") Applicable Filters");

		filterSearchBar = new Combo(controlPanelComposite, SWT.NONE);
		filterSearchBar.setEnabled(false);
		filterSearchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite filterComposite = new Composite(controlPanelComposite, SWT.NONE);
		filterComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		filterComposite.setLayout(new GridLayout(1, false));
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group grpFilterDescription = new Group(filterComposite, SWT.NONE);
		grpFilterDescription.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		grpFilterDescription.setLayout(new GridLayout(1, false));
		grpFilterDescription.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		grpFilterDescription.setText("Filter Description");
		
		filterDescriptionText = new Label(grpFilterDescription, SWT.WRAP);
		filterDescriptionText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		filterDescriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		filterDescriptionText.setText("No filter selected.");

		Group filterParametersGroup = new Group(filterComposite, SWT.NONE);
		filterParametersGroup.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		filterParametersGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		filterParametersGroup.setText("Filter Parameters");
		filterParametersGroup.setLayout(new GridLayout(1, false));
		filterParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		filterParametersScrolledComposite = new ScrolledComposite(filterParametersGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		filterParametersScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		filterParametersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		filterParametersScrolledComposite.setExpandHorizontal(true);
		filterParametersScrolledComposite.setExpandVertical(true);

		Label noSelectedFilterLabel = new Label(filterParametersScrolledComposite, SWT.NONE);
		noSelectedFilterLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		noSelectedFilterLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		noSelectedFilterLabel.setText("No filter selected.");
		filterParametersScrolledComposite.setContent(noSelectedFilterLabel);
		filterParametersScrolledComposite.setMinSize(noSelectedFilterLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite applyFilterComposite = new Composite(filterComposite, SWT.NONE);
		applyFilterComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		applyFilterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		applyFilterComposite.setLayout(new GridLayout(2, false));

		errorLabel = new Label(applyFilterComposite, SWT.NONE);
		errorLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		applyFilterButton = new Button(applyFilterComposite, SWT.NONE);
		applyFilterButton.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
		applyFilterButton.setEnabled(false);
		applyFilterButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		applyFilterButton.setText("Apply Filter");
		sashForm.setWeights(new int[] { 1, 1 });
		
		// record expanded state in the data structure
		filterTree.addTreeListener(new TreeAdapter() {
			@Override
			public void treeExpanded(TreeEvent e) {
				FilterTreeNode node = (FilterTreeNode) e.item.getData();
				node.setExpanded(true);
			}
			
			@Override
			public void treeCollapsed(TreeEvent e) {
				FilterTreeNode node = (FilterTreeNode) e.item.getData();
				node.setExpanded(false);
			}
		});
		
		// handle double click on tree item
		filterTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if(filterTree.getSelectionCount() == 1){
					TreeItem treeItem = filterTree.getSelection()[0];
					FilterTreeNode node = (FilterTreeNode) treeItem.getData();
					boolean extend = CommonsPreferences.isDisplayFilterViewResultContainersEnabled();
					DisplayUtils.show(Common.toQ(node.getOutput()), extend, node.getName());
				}
			}
		});
		
		// handle key release on search bar
		filterSearchBar.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				
				if(filterSearchBar.getText().trim().equals("") || filterTree.getSelectionCount() == 0){
					// nothing to search
					return;
				}
				
				// tree item has to be selected for this event to occur
				// wrapped in a try/catch just to be safe
				try {
					// get the input set of the selected tree item
					TreeItem treeItem = filterTree.getSelection()[0];
					FilterTreeNode node = (FilterTreeNode) treeItem.getData();
					String searchText = filterSearchBar.getText();
					if(searchText.trim().equals("")){
						filterSearchBar.setListVisible(false); // hide the list, we are going to modify the values
						
						// remove all items
						filterSearchBar.removeAll();

						// there is no search term, so add all applicable filters
						for(Filter filter : node.getApplicableFilters()){
							filterSearchBar.add(filter.getName());
							filterSearchBar.setData(filter.getName(), filter);
						}
					} else if(key.character == '\r'){
						// make selection of first match
						for(String item : filterSearchBar.getItems()){
							if(item.toLowerCase().contains(searchText.toLowerCase())){
								filterSearchBar.select(filterSearchBar.indexOf(item));
								populateFilterSelection();
								break;
							}
						}
					} else if(key.keyCode == SWT.ARROW_DOWN){
						filterSearchBar.setListVisible(true); // show the drop down list
					} else if(Character.isLetter(key.character)){
						filterSearchBar.setListVisible(false); // hide the list, we are going to modify the values
						
						// remove all items
						// note: doing this the hard way because removeAll method also clears the text
						for(String item : filterSearchBar.getItems()){
							filterSearchBar.remove(item);
						}

						// add the autocomplete suggestions for each matching permission
						for(Filter filter : node.getApplicableFilters()){
							if(filter.getName().toLowerCase().contains(searchText.toLowerCase())){
								filterSearchBar.add(filter.getName());
								filterSearchBar.setData(filter.getName(), filter);
							}
						}
						
						// for some reason the previous actions are clearing the search text on some OS's so restoring it now
						filterSearchBar.setText(searchText);
						// make sure the cursor selection is at the end
						filterSearchBar.setSelection(new Point(searchText.length(), searchText.length()));
					}
				} catch (Throwable t){
					DisplayUtils.showError(t, "An unexpected error searching filters occurred.");
				}
			}
		});
		
		// filter selected
		filterSearchBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				populateFilterSelection();
			}
		});
		
		// handle apply filter button pressed
		applyFilterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// filter and node have to be selected for this event to occur
				// wrapped in a try/catch just to be safe
				try {
					if(filterTree.getSelectionCount() == 0){
						DisplayUtils.showError("A filter set from the filter tree must be selected before applying a filter.");
						return;
					} else {
						// assume everything works and disable the button to prevent further actions
						applyFilterButton.setEnabled(false);
					}
					
					// get the selected tree node
					TreeItem treeItem = filterTree.getSelection()[0];
					FilterTreeNode node = (FilterTreeNode) treeItem.getData();
					
					// get the selected filter
					Filter filter = (Filter) filterSearchBar.getData(filterSearchBar.getText());
					
					// make a copy of the parameters, cause they can change later
					HashMap<String,Object> filterParametersCopy = new HashMap<String,Object>(filterParameters);
					node.addChild(filter, filterParametersCopy);

					// refresh the tree
					refreshFilterTree();
					clearFilterSelection();
				} catch (Throwable t){
					DisplayUtils.showError(t, "An unexpected error applying filter occurred.");
					
					// something went wrong, give the user a chance to fix and apply again
					applyFilterButton.setEnabled(true);
				}
			}
		});
		
		// handle filter tree item selection
		filterTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(filterTree.getSelectionCount() == 1){
					clearFilterSelection();
					// get the input set of the selected tree item
					TreeItem treeItem = filterTree.getSelection()[0];
					FilterTreeNode node = (FilterTreeNode) treeItem.getData();
					populateFilterSearchBarResults(node.getApplicableFilters());
					
					if(node instanceof FilterNode){
						// show the parameters the filter was applied with
						populateAppliedParameters(((FilterNode) node).getFilter(), ((FilterNode) node).getFilterParameters());
					}
				} else {
					// nothing is applicable
					clearFilterSelection();
					populateFilterSearchBarResults(new LinkedList<Filter>());
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
	
	private void populateAppliedParameters(Filter filter, Map<String, Object> filterParameters) {
		filterDescriptionText.setText(filter.getDescription());
		StyledText filterParametersSummary = new StyledText(filterParametersScrolledComposite, SWT.READ_ONLY | SWT.WRAP);
		filterParametersSummary.setEditable(false);
		filterParametersSummary.setText(summarizeFilterParameters(filter, filterParameters));
		filterParametersScrolledComposite.setContent(filterParametersSummary);
		filterParametersScrolledComposite.setMinSize(filterParametersSummary.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private String summarizeFilterParameters(Filter filter, Map<String, Object> filterParameters) {
		StringBuilder result = new StringBuilder();
		for(Entry<String,Object> filterParameter : filterParameters.entrySet()){
			result.append(filterParameter.getKey() + ": " + filterParameter.getValue().toString() + "\n");
		}
		
		String summary = result.toString().trim();
		if(summary.equals("")){
			return "No parameters were provided to the selected filter.";
		} else {
			return summary;
		}
	}

	private void clearFilterSelection() {
		Label noSelectedFilterLabel = new Label(filterParametersScrolledComposite, SWT.NONE);
		noSelectedFilterLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		noSelectedFilterLabel.setText("No filter selected.");
		filterDescriptionText.setText("No filter selected.");
		filterParametersScrolledComposite.setContent(noSelectedFilterLabel);
		filterParametersScrolledComposite.setMinSize(noSelectedFilterLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		filterParameters.clear();
		clearValidationErrorMessage();
		applyFilterButton.setEnabled(false);
	}
	
	private void populateFilterSelection() {
		// filter has to be selected for this event to occur
		// wrapped in a try/catch just to be safe
		try {
			Filter filter = (Filter) filterSearchBar.getData(filterSearchBar.getText());
			
			filterDescriptionText.setText(filter.getDescription());
			
			filterParameters.clear();
			validateFilterParameters(filter);
			
			if(filter.getPossibleParameters().isEmpty()){
				Label noParamsLabel = new Label(filterParametersScrolledComposite, SWT.NONE);
				noParamsLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				noParamsLabel.setAlignment(SWT.CENTER);
				noParamsLabel.setText("No parameters available for this filter.");
				filterParametersScrolledComposite.setContent(noParamsLabel);
				filterParametersScrolledComposite.setMinSize(noParamsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			} else {
				Composite inputComposite = new Composite(filterParametersScrolledComposite, SWT.NONE);
				inputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				inputComposite.setLayout(new GridLayout(1, false));

				Label requiredFieldsLabel = new Label(inputComposite, SWT.NONE);
				requiredFieldsLabel.setText("*Indicates required fields.");
				requiredFieldsLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
				
				// add the parameters in alphabetical order for UI consistency (flags are ordered first)
				LinkedList<String> parameterNames = new LinkedList<String>(filter.getPossibleParameters().keySet());
				Collections.sort(parameterNames, new Comparator<String>(){
					@Override
					public int compare(String p1, String p2) {
						boolean p1Flag = filter.getPossibleFlags().contains(p1);
						boolean p2Flag = filter.getPossibleFlags().contains(p2);
						if(p1Flag && !p2Flag){
							return -1;
						} else if(p1Flag && p2Flag){
							return p1.compareTo(p2);
						} else {
							return 1;
						}
					}
				});
				
				for(String parameterName : parameterNames){
					final Class<? extends Object> parameterType = filter.getPossibleParameters().get(parameterName);
					final boolean requiredParameter = filter.getRequiredParameters().contains(parameterName);
					
					if(parameterType == Boolean.class){
						Composite booleanInputComposite = new Composite(inputComposite, SWT.NONE);
						booleanInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						booleanInputComposite.setLayout(new GridLayout(3, false));
						booleanInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

						final Button enableBooleanInputCheckbox = new Button(booleanInputComposite, SWT.CHECK);
						enableBooleanInputCheckbox.setEnabled(!requiredParameter);
						enableBooleanInputCheckbox.setToolTipText(filter.getParameterDescription(parameterName));
						enableBooleanInputCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));

						final Label booleanInputLabel = new Label(booleanInputComposite, SWT.NONE);
						booleanInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						if(filter.getPossibleFlags().contains(parameterName)){	
							booleanInputLabel.setText(parameterName);
							booleanInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
							
							enableBooleanInputCheckbox.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									boolean enabled = enableBooleanInputCheckbox.getSelection();
									booleanInputLabel.setEnabled(enabled);
									if(enabled){
										filterParameters.put(parameterName, true);
										validateFilterParameters(filter);
									} else {
										filterParameters.remove(parameterName);
										validateFilterParameters(filter);
									}
								}
							});
						} else {
							booleanInputLabel.setEnabled(requiredParameter);
							booleanInputLabel.setText((requiredParameter ? "*" : "") + parameterName + ":");
							booleanInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
							
							final Button booleanInputCheckbox = new Button(booleanInputComposite, SWT.CHECK);
							booleanInputCheckbox.setEnabled(requiredParameter);
							booleanInputCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							
							booleanInputCheckbox.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									filterParameters.put(parameterName, booleanInputCheckbox.getSelection());
									validateFilterParameters(filter);
								}
							});
							
							enableBooleanInputCheckbox.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									boolean enabled = enableBooleanInputCheckbox.getSelection();
									booleanInputLabel.setEnabled(enabled);
									booleanInputCheckbox.setEnabled(enabled);
									if(enabled){
										filterParameters.put(parameterName, booleanInputCheckbox.getSelection());
										validateFilterParameters(filter);
									} else {
										filterParameters.remove(parameterName);
										validateFilterParameters(filter);
									}
								}
							});
						}
						
						
					} else if(parameterType == String.class){
						Composite stringInputComposite = new Composite(inputComposite, SWT.NONE);
						stringInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						stringInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						stringInputComposite.setLayout(new GridLayout(3, false));

						final Button enableStringInputCheckbox = new Button(stringInputComposite, SWT.CHECK);
						enableStringInputCheckbox.setEnabled(!requiredParameter);
						enableStringInputCheckbox.setToolTipText(filter.getParameterDescription(parameterName));
						enableStringInputCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));

						final Label stringInputLabel = new Label(stringInputComposite, SWT.NONE);
						stringInputLabel.setEnabled(requiredParameter);
						stringInputLabel.setText((requiredParameter ? "*" : "") + parameterName + ":");
						stringInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
						stringInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));

						final Text stringInputText = new Text(stringInputComposite, SWT.BORDER);
						stringInputText.setEnabled(requiredParameter);
						stringInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						stringInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));

						enableStringInputCheckbox.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								boolean enabled = enableStringInputCheckbox.getSelection();
								stringInputLabel.setEnabled(enabled);
								stringInputText.setEnabled(enabled);
								if(enabled){
									String text = stringInputText.getText();
									if(!text.equals("")){
										filterParameters.put(parameterName, text);
										validateFilterParameters(filter);
									} else {
										setValidationErrorMessage(parameterName + " must be an non-empty string.");
										applyFilterButton.setEnabled(false);
									}
								} else {
									filterParameters.remove(parameterName);
									validateFilterParameters(filter);
								}
							}
						});

						stringInputText.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								String text = stringInputText.getText();
								if(!text.equals("")){
									filterParameters.put(parameterName, text);
									validateFilterParameters(filter);
								} else {
									setValidationErrorMessage(parameterName + " must be an non-empty string.");
									applyFilterButton.setEnabled(false);
								}
							}
						});
					} else if(parameterType == Integer.class){
						Composite integerInputComposite = new Composite(inputComposite, SWT.NONE);
						integerInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						integerInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						integerInputComposite.setLayout(new GridLayout(3, false));

						final Button enableIntegerInputCheckbox = new Button(integerInputComposite, SWT.CHECK);
						enableIntegerInputCheckbox.setEnabled(!requiredParameter);
						enableIntegerInputCheckbox.setToolTipText(filter.getParameterDescription(parameterName));
						enableIntegerInputCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						final Label integerInputLabel = new Label(integerInputComposite, SWT.NONE);
						integerInputLabel.setEnabled(requiredParameter);
						integerInputLabel.setText((requiredParameter ? "*" : "") + parameterName + ":");
						integerInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
						integerInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						final Text integerInputText = new Text(integerInputComposite, SWT.BORDER);
						integerInputText.setEnabled(requiredParameter);
						integerInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						integerInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));

						enableIntegerInputCheckbox.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								boolean enabled = enableIntegerInputCheckbox.getSelection();
								integerInputLabel.setEnabled(enabled);
								integerInputText.setEnabled(enabled);
								if(enabled){
									try {
										filterParameters.put(parameterName, Integer.parseInt(integerInputText.getText()));
										validateFilterParameters(filter);
									} catch (Exception ex){
										setValidationErrorMessage(parameterName + " must be an integer.");
										applyFilterButton.setEnabled(false);
									}
								} else {
									filterParameters.remove(parameterName);
									validateFilterParameters(filter);
								}
							}
						});

						integerInputText.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								try {
									filterParameters.put(parameterName, Integer.parseInt(integerInputText.getText()));
									validateFilterParameters(filter);
								} catch (Exception ex){
									setValidationErrorMessage(parameterName + " must be an integer.");
									applyFilterButton.setEnabled(false);
								}
							}
						});
					} else if(parameterType == Double.class){
						Composite doubleInputComposite = new Composite(inputComposite, SWT.NONE);
						doubleInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						doubleInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						doubleInputComposite.setLayout(new GridLayout(3, false));

						final Button enableDoubleInputCheckbox = new Button(doubleInputComposite, SWT.CHECK);
						enableDoubleInputCheckbox.setEnabled(!requiredParameter);
						enableDoubleInputCheckbox.setToolTipText(filter.getParameterDescription(parameterName));
						enableDoubleInputCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						final Label doubleInputLabel = new Label(doubleInputComposite, SWT.NONE);
						doubleInputLabel.setEnabled(requiredParameter);
						doubleInputLabel.setText((requiredParameter ? "*" : "") + parameterName + ":");
						doubleInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
						doubleInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						final Text doubleInputText = new Text(doubleInputComposite, SWT.BORDER);
						doubleInputText.setEnabled(requiredParameter);
						doubleInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						doubleInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						enableDoubleInputCheckbox.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								boolean enabled = enableDoubleInputCheckbox.getSelection();
								doubleInputLabel.setEnabled(enabled);
								doubleInputText.setEnabled(enabled);
								if(enabled){
									try {
										filterParameters.put(parameterName, Double.parseDouble(doubleInputText.getText()));
										validateFilterParameters(filter);
									} catch (Exception ex){
										setValidationErrorMessage(parameterName + " must be a double.");
										applyFilterButton.setEnabled(false);
									}
								} else {
									filterParameters.remove(parameterName);
									validateFilterParameters(filter);
								}
							}
						});

						doubleInputText.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								try {
									filterParameters.put(parameterName, Double.parseDouble(doubleInputText.getText()));
									validateFilterParameters(filter);
								} catch (Exception ex){
									setValidationErrorMessage(parameterName + " must be a double.");
									applyFilterButton.setEnabled(false);
								}
							}
						});
					} else {
						DisplayUtils.showError("This filter has unsupported parameter types!");
					}
				}
				
				filterParametersScrolledComposite.setContent(inputComposite);
				filterParametersScrolledComposite.setMinSize(inputComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
			
		} catch (Throwable t){
			DisplayUtils.showError(t, "An unexpected error populating filter contents occurred.");
		}
	}
	
	private void validateFilterParameters(Filter filter){
		try {
			filter.checkParameters(filterParameters);
			clearValidationErrorMessage();
			applyFilterButton.setEnabled(true);
		} catch (Exception e){
			setValidationErrorMessage(e.getMessage());
			applyFilterButton.setEnabled(false);
		}
	}
	
	private void clearValidationErrorMessage() {
		errorLabel.setText("");
	}
	
	private void setValidationErrorMessage(String message) {
		errorLabel.setText(message);
	}

	private void populateFilterSearchBarResults(Collection<Filter> applicableFilters){
		// update the search bar with the applicable filters
		filterSearchBar.removeAll();
		applicableFiltersLabel.setText("(" + applicableFilters.size() + "/" + Filters.getRegisteredFilters().size() + ") Applicable Filters");
		for(Filter filter : applicableFilters){
			filterSearchBar.add(filter.getName());
			filterSearchBar.setData(filter.getName(), filter);
		}
		filterSearchBar.setEnabled(!applicableFilters.isEmpty());
	}
	
	private void addFileMenuItems(ToolItem fileMenuDropDownItem) {
		DropdownSelectionListener fileListener = new DropdownSelectionListener(fileMenuDropDownItem);
		fileListener.add("Save Filter Tree", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
				    FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
				    dialog.setFilterNames(new String[] { "XML Files", "All Files (*.*)" });
				    dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
				    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				    dialog.setFileName("filter-tree-" + sdf.format(new Date()) + ".xml");
				    File outputFile = new File(dialog.open());
					
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

					// filter tree
					Document doc = docBuilder.newDocument();
					Element treeElement = doc.createElement("tree");
					doc.appendChild(treeElement);

					// filter tree root elements
					for(FilterRootNode root : treeRoots){
						Element rootElement = doc.createElement("rootset");
						treeElement.appendChild(rootElement);
						rootElement.setAttribute("name", root.getName());
						
						// filter tree nodes
						for(FilterTreeNode node : root.getChildren()){
							addFilters(doc, rootElement, node);
						}
						
						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						transformerFactory.setAttribute("indent-number", new Integer(2));
						Transformer transformer = transformerFactory.newTransformer();
						transformer.setOutputProperty(OutputKeys.INDENT, "yes");
						DOMSource source = new DOMSource(doc);
						StreamResult result = new StreamResult(outputFile);
						transformer.transform(source, result);
					}
				} catch (Exception e){
					DisplayUtils.showError(e, "Could not save the filter tree!");
				}
			}

			private void addFilters(Document doc, Element parentElement, FilterTreeNode node) {
				FilterNode filter = (FilterNode) node;
				Element filterElement = doc.createElement("filter");
				parentElement.appendChild(filterElement);
				filterElement.setAttribute("code", filter.getFilter().getClass().getName());
				for(String parameter : filter.getFilterParameters().keySet()){
					Element parametersElement = doc.createElement("parameter");
					filterElement.appendChild(parametersElement);
					parametersElement.setAttribute("name", parameter);
					parametersElement.setAttribute("type", filter.getFilter().getPossibleParameters().get(parameter).getName());
					parametersElement.setAttribute("value", filter.getFilterParameters().get(parameter).toString());
				}
				for(FilterTreeNode child : filter.children){
					addFilters(doc, filterElement, child);
				}
			}
		});
//		fileListener.add("Load Filter Chain", new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent event) {
//				// TODO: implement
//				
//			}
//		});
		fileMenuDropDownItem.addSelectionListener(fileListener);
	}

	private void addOptionMenuItems(ToolItem optionMenuDropDownItem) {
		DropdownSelectionListener optionListener = new DropdownSelectionListener(optionMenuDropDownItem);
		optionListener.add("Add Selected as Root Set", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Q currentSelection = Common.toQ(selection);
				String name = DisplayUtils.promptString("Add Root Set", "Root Set Name:", false);
				if(name != null){
					if(name.trim().equals("")){
						DisplayUtils.showError("Root set name must contain some non-whitespace characters.");
					} else {
						try {
							treeRoots.add(new FilterRootNode(currentSelection, name, true));
							String plurality = ((treeRoots.size() > 1 || treeRoots.size() == 0) ? "s" : "");
							filterTreeLabel.setText("Filter Tree (" + treeRoots.size() + " root" + plurality + ")");
							refreshFilterTree();
						} catch (IllegalArgumentException e){
							DisplayUtils.showError("Could not add root set. " + e.getMessage());
						} catch (Throwable t){
							DisplayUtils.showError(t, "Could not add root set.");
						}
					}
				}
			}
		});
		optionListener.add("Rename Root Set", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(filterTree.getSelectionCount() == 1){
					TreeItem treeItem = filterTree.getSelection()[0];
					FilterRootNode root = (FilterRootNode) treeItem.getData();
					String name = DisplayUtils.promptString("Rename Root Set", "Root Set Name:", false);
					if(name != null){
						if(name.trim().equals("")){
							DisplayUtils.showError("Root set name must contain some non-whitespace characters.");
						} else {
							try {
								root.rename(name);
								refreshFilterTree();
							} catch (Exception e){
								DisplayUtils.showError("Could not rename root set. " + e.getMessage());
							}
						}
					}
				} else {
					DisplayUtils.showError("Please select a root set in the filter tree to rename.");
				}
			}
		});
		optionListener.add("Load Default Rootsets", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				for(FilterableRootset rootset : FilterableRootsets.getRegisteredRootSets()){
					try{
						treeRoots.add(new FilterRootNode(rootset.getRootSet(), rootset.getName(), false, true));
						String plurality = ((treeRoots.size() > 1 || treeRoots.size() == 1) ? "s" : "");
						filterTreeLabel.setText("Filter Tree (" + treeRoots.size() + " root" + plurality + ")");
						refreshFilterTree();
					} catch (IllegalArgumentException e1){
						Log.warning("Could not add filters: ", e1);
						// root set is already loaded or was empty, but this is ok for defaults
					} catch (Exception e2){
						Log.warning("Could not add filters: ", e2);
						DisplayUtils.showError(e2, "There was an error loading the default rootsets.");
					}
				}
			}
		});
		optionListener.add("Delete Selected Root Set", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(filterTree.getSelectionCount() == 1){
					TreeItem treeItem = filterTree.getSelection()[0];
					FilterRootNode root = (FilterRootNode) treeItem.getData();
					treeRoots.remove(root);
					root.delete();
					String plurality = ((treeRoots.size() > 1 || treeRoots.size() == 1) ? "s" : "");
					filterTreeLabel.setText("Filter Tree (" + treeRoots.size() + " root" + plurality + ")");
					// clear the display
					populateFilterSearchBarResults(new LinkedList<Filter>());
					refreshFilterTree();
				} else {
					DisplayUtils.showError("Please select a root set in the filter tree to delete.");
				}
			}
		});
		optionListener.add("Delete All Root Sets (Reset Filter View)", new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				clearFilterSelection();
				for(FilterRootNode root : treeRoots){
					root.delete();
				}
				treeRoots.clear();
				filterTreeLabel.setText("Filter Tree (0 roots)");
				refreshFilterTree();
			}
		});
		optionMenuDropDownItem.addSelectionListener(optionListener);
	}

	private void refreshFilterTree() {
		filterTree.setRedraw(false);
		
		// clear the tree
		filterTree.removeAll();
		
		// add each tree node
		for(FilterRootNode treeRoot : treeRoots){
			addTreeRootItem(treeRoot);
		}
		
		// set whether or not the nodes are expanded
		restoreExpandedState(filterTree.getItems());
		
		filterTree.setRedraw(true);
		filterTree.redraw();
	}
	
	private void restoreExpandedState(TreeItem[] treeItems){
		for(TreeItem treeItem : treeItems){
			FilterTreeNode node = (FilterTreeNode) treeItem.getData();
			treeItem.setExpanded(node.isExpanded());
			restoreExpandedState(treeItem.getItems());
		}
	}

	private void addTreeRootItem(FilterRootNode root){
		TreeItem treeItem = new TreeItem(filterTree, SWT.VIRTUAL);
		treeItem.setData(root);
		treeItem.setText(root.getName() + " " + summarizeContent(root.getOutput()));
		for(FilterTreeNode node : root.getChildren()){
			addFilterTreeItem(node, treeItem);
		}
	}
	
	private void addFilterTreeItem(FilterTreeNode node, TreeItem treeItem){
		TreeItem subTreeItem = new TreeItem(treeItem, SWT.VIRTUAL);
		subTreeItem.setData(node);
		subTreeItem.setText(node.getName() + " " + summarizeContent(node.getOutput()));
		for(FilterTreeNode child : node.getChildren()){
			addFilterTreeItem(child, subTreeItem);
		}
	}
	
	private String summarizeContent(Graph content){
		String result = "(";
		if(content.nodes().isEmpty() && content.edges().isEmpty()){
			// empty
			result += "empty";
		} else if(content.edges().isEmpty()){
			// only nodes
			String plurality = ((content.nodes().size() > 1 || content.nodes().size() == 0) ? "s" : "");
			result += content.nodes().size() + " node" + plurality;
		} else if(content.nodes().isEmpty()){
			// only edges
			String plurality = ((content.edges().size() > 1 || content.edges().size() == 0) ? "s" : "");
			result += content.edges().size() + " edge" + plurality;
		} else {
			// nodes and edges
			String nodePlurality = ((content.nodes().size() > 1 || content.nodes().size() == 0) ? "s" : "");
			String edgePlurality = ((content.edges().size() > 1 || content.edges().size() == 0) ? "s" : "");
			result += content.nodes().size() + " node" + nodePlurality + ", " + content.edges().size() + " edge" + edgePlurality;
		}
		result += ")";
		return result;
	}
	
	@Override
	public void setFocus() {}
}