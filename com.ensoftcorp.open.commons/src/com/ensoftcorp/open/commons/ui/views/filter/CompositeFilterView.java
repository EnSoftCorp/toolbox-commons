package com.ensoftcorp.open.commons.ui.views.filter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ExpandAdapter;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.filters.Filter;
import com.ensoftcorp.open.commons.filters.Filters;
import com.ensoftcorp.open.commons.filters.InvalidFilterParameterException;
import com.ensoftcorp.open.commons.filters.rootset.FilterableRootset;
import com.ensoftcorp.open.commons.filters.rootset.FilterableRootsets;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;
import com.ensoftcorp.open.commons.xcsg.XCSGConstantNameValueMapping;

public class CompositeFilterView extends ViewPart {
	
	public CompositeFilterView() {
		setTitleImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/toolbox.gif"));
		setPartName("Composite Filter View");
		
		// load plugin filter contributions
		Filters.loadFilterContributions();
		FilterableRootsets.loadFilterContributions();
	}
	
	private static Map<String,String> xcsgConstantNameToValueMap = XCSGConstantNameValueMapping.getXCSGConstantNameToValueMap();

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.filter.CompositeFilterView";
	
	private static final int FONT_SIZE = 11;
	private static final DecimalFormat df = new DecimalFormat("#.##");
	
	private Graph selectedRootset = Common.empty().eval();
	private Graph evaluatedResult = Common.empty().eval();
	private Set<SelectedFilterState> selectedFilters = new HashSet<SelectedFilterState>();
	private Set<ApplicableFilterState> applicableFilters = new HashSet<ApplicableFilterState>();
	
	private Comparator<SelectedFilterState> selectedFilterComparator = new FilterNodeImpactComparator().reversed();
	private Comparator<ApplicableFilterState> applicableFilterComparator = new ApplicableFilterNameComparator();

	private static class FilterNodeImpactComparator implements Comparator<SelectedFilterState> {
		@Override
		public int compare(SelectedFilterState a, SelectedFilterState b) {
			return Long.compare(a.getNodeImpact(), b.getNodeImpact());
		}
	}
	
	private static class FilterEdgeImpactComparator implements Comparator<SelectedFilterState> {
		@Override
		public int compare(SelectedFilterState a, SelectedFilterState b) {
			return Long.compare(a.getEdgeImpact(), b.getEdgeImpact());
		}
	}
	
	private static class SelectedFilterNameComparator implements Comparator<SelectedFilterState> {
		@Override
		public int compare(SelectedFilterState a, SelectedFilterState b) {
			return a.getFilter().getName().compareTo(b.getFilter().getName());
		}
	}
	
	private static class ApplicableFilterNameComparator implements Comparator<ApplicableFilterState> {
		@Override
		public int compare(ApplicableFilterState a, ApplicableFilterState b) {
			return a.getFilter().getName().compareTo(b.getFilter().getName());
		}
	}
	
	private ScrolledComposite selectedFiltersScrolledComposite;
	private ScrolledComposite applicableFiltersScrolledComposite;
	private Group rootsetGroup;
	private Button nodesTaggedWithAnyCheckbox;
	private Button nodesTaggedWithAllCheckbox;
	private Button edgesTaggedWithAnyCheckbox;
	private Button edgesTaggedWithAllCheckbox;
	private Text nodesTaggedWithAnyText;
	private Text nodesTaggedWithAllText;
	private Text edgesTaggedWithAnyText;
	private Text edgesTaggedWithAllText;
	private Text tagResultText;
	private Group resultGroup;
	
	@Override
	public void createPartControl(Composite composite) {
		composite.setLayout(new GridLayout(1, false));
		
		SashForm compositeFiltersSashForm = new SashForm(composite, SWT.NONE);
		compositeFiltersSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite controlsComposite = new Composite(compositeFiltersSashForm, SWT.NONE);
		controlsComposite.setLayout(new GridLayout(1, false));
		
		rootsetGroup = new Group(controlsComposite, SWT.NONE);
		rootsetGroup.setLayout(new GridLayout(1, false));
		rootsetGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rootsetGroup.setText("Rootset: (empty)");
		
		Button showRootsetButton = new Button(rootsetGroup, SWT.NONE);
		showRootsetButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		showRootsetButton.setText("Show Rootset");
		
		resultGroup = new Group(controlsComposite, SWT.NONE);
		resultGroup.setLayout(new GridLayout(1, false));
		resultGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		resultGroup.setText("Result: (empty)");
		
		Button showResultButton = new Button(resultGroup, SWT.NONE);
		showResultButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		showResultButton.setText("Show Result");
		
		showResultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DisplayUtils.show(Common.toQ(evaluatedResult), "Filtered Result");
			}
		});
		
		Composite tagResultComposite = new Composite(resultGroup, SWT.NONE);
		tagResultComposite.setLayout(new GridLayout(2, false));
		tagResultComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		tagResultText = new Text(tagResultComposite, SWT.BORDER);
		tagResultText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button tagResultButton = new Button(tagResultComposite, SWT.NONE);
		tagResultButton.setEnabled(false);
		tagResultButton.setText("Tag Result");
		
		Group recipeGroup = new Group(controlsComposite, SWT.NONE);
		recipeGroup.setLayout(new GridLayout(1, false));
		recipeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		recipeGroup.setText("Recipe Management");
		
		Button loadRecipeButton = new Button(recipeGroup, SWT.NONE);
		loadRecipeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		loadRecipeButton.setText("Load Recipe");
		
		Button saveRecipeButton = new Button(recipeGroup, SWT.NONE);
		saveRecipeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		saveRecipeButton.setText("Save Recipe");
		
		Composite filtersComposite = new Composite(compositeFiltersSashForm, SWT.NONE);
		filtersComposite.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		filtersComposite.setLayout(new GridLayout(1, false));
		
		Group rootsetSelectionGroup = new Group(filtersComposite, SWT.NONE);
		rootsetSelectionGroup.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		rootsetSelectionGroup.setLayout(new GridLayout(2, false));
		rootsetSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rootsetSelectionGroup.setText("Rootset Selection");

		Button predefinedRootsetRadio = new Button(rootsetSelectionGroup, SWT.RADIO);
		predefinedRootsetRadio.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		predefinedRootsetRadio.setSelection(true);
		predefinedRootsetRadio.setText("Predefined Rootset: ");
		
		Combo predefinedRootsetSearchBar = new Combo(rootsetSelectionGroup, SWT.NONE);
		predefinedRootsetSearchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		for(FilterableRootset rootset : FilterableRootsets.getRegisteredRootSets()){
			predefinedRootsetSearchBar.add(rootset.getName());
			predefinedRootsetSearchBar.setData(rootset.getName(), rootset);
		}
		
		Button taggedRootsetRadio = new Button(rootsetSelectionGroup, SWT.RADIO);
		taggedRootsetRadio.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		taggedRootsetRadio.setText("Tagged Rootset: ");
		
		Group group = new Group(rootsetSelectionGroup, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group taggedWithAnyGroup = new Group(group, SWT.NONE);
		taggedWithAnyGroup.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		taggedWithAnyGroup.setLayout(new GridLayout(4, false));
		taggedWithAnyGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		taggedWithAnyGroup.setText("Tagged With Any");
		
		nodesTaggedWithAnyCheckbox = new Button(taggedWithAnyGroup, SWT.CHECK);
		nodesTaggedWithAnyCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		nodesTaggedWithAnyCheckbox.setEnabled(false);
		nodesTaggedWithAnyCheckbox.setText("Nodes: ");
		
		nodesTaggedWithAnyText = new Text(taggedWithAnyGroup, SWT.BORDER);
		nodesTaggedWithAnyText.setToolTipText("A comma seperated list of tag values. Raw values or XCSG common names such as XCSG.ControlFlow_Node can be entered.");
		nodesTaggedWithAnyText.setEnabled(false);
		nodesTaggedWithAnyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		edgesTaggedWithAnyCheckbox = new Button(taggedWithAnyGroup, SWT.CHECK);
		edgesTaggedWithAnyCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		edgesTaggedWithAnyCheckbox.setEnabled(false);
		edgesTaggedWithAnyCheckbox.setText("Edges: ");
		
		edgesTaggedWithAnyText = new Text(taggedWithAnyGroup, SWT.BORDER);
		edgesTaggedWithAnyText.setToolTipText("A comma seperated list of tag values. Raw values or XCSG common names such as XCSG.ControlFlow_Edge can be entered.");
		edgesTaggedWithAnyText.setEnabled(false);
		edgesTaggedWithAnyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group taggedWithAllGroup = new Group(group, SWT.NONE);
		taggedWithAllGroup.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		taggedWithAllGroup.setLayout(new GridLayout(4, false));
		taggedWithAllGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		taggedWithAllGroup.setText("Tagged With All");
		
		nodesTaggedWithAllCheckbox = new Button(taggedWithAllGroup, SWT.CHECK);
		nodesTaggedWithAllCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		nodesTaggedWithAllCheckbox.setEnabled(false);
		nodesTaggedWithAllCheckbox.setText("Nodes: ");
		
		nodesTaggedWithAllText = new Text(taggedWithAllGroup, SWT.BORDER);
		nodesTaggedWithAllText.setToolTipText("A comma seperated list of tag values. Raw values or XCSG common names such as XCSG.ControlFlow_Node can be entered.");
		nodesTaggedWithAllText.setEnabled(false);
		nodesTaggedWithAllText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		edgesTaggedWithAllCheckbox = new Button(taggedWithAllGroup, SWT.CHECK);
		edgesTaggedWithAllCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		edgesTaggedWithAllCheckbox.setEnabled(false);
		edgesTaggedWithAllCheckbox.setText("Edges: ");
		
		edgesTaggedWithAllText = new Text(taggedWithAllGroup, SWT.BORDER);
		edgesTaggedWithAllText.setToolTipText("A comma seperated list of tag values. Raw values or XCSG common names such as XCSG.ControlFlow_Edge can be entered.");
		edgesTaggedWithAllText.setEnabled(false);
		edgesTaggedWithAllText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button userDefinedRootsetRadio = new Button(rootsetSelectionGroup, SWT.RADIO);
		userDefinedRootsetRadio.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		userDefinedRootsetRadio.setText("User Defined Rootset");
		
		Label userDefinedRootsetStatusLabel = new Label(rootsetSelectionGroup, SWT.NONE);
		userDefinedRootsetStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		SashForm filtersSashForm = new SashForm(filtersComposite, SWT.NONE);
		filtersSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group selectedFiltersGroup = new Group(filtersSashForm, SWT.NONE);
		selectedFiltersGroup.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		selectedFiltersGroup.setText("Selected Filters");
		selectedFiltersGroup.setLayout(new GridLayout(1, false));
		
		Composite sortSelectedFiltersComposite = new Composite(selectedFiltersGroup, SWT.NONE);
		sortSelectedFiltersComposite.setLayout(new GridLayout(2, false));
		sortSelectedFiltersComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label sortSelectedFiltersLabel = new Label(sortSelectedFiltersComposite, SWT.NONE);
		sortSelectedFiltersLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		sortSelectedFiltersLabel.setText("Sort: ");
		
		Combo sortSelectedFiltersCombo = new Combo(sortSelectedFiltersComposite, SWT.READ_ONLY);
		sortSelectedFiltersCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(sortSelectedFiltersCombo.getText().equals("Sort by Name (A \u2192 Z)")){
					selectedFilterComparator = new SelectedFilterNameComparator();
				} else if(sortSelectedFiltersCombo.getText().equals("Sort by Name (Z \u2192 A)")) {
					selectedFilterComparator = new SelectedFilterNameComparator().reversed();
				} else if(sortSelectedFiltersCombo.getText().equals("Sort by Node Impact (Low \u2192 High)")){
					selectedFilterComparator = new FilterNodeImpactComparator();
				} else if(sortSelectedFiltersCombo.getText().equals("Sort by Node Impact (High \u2192 Low)")) {
					selectedFilterComparator = new FilterNodeImpactComparator().reversed();
				} else if(sortSelectedFiltersCombo.getText().equals("Sort by Edge Impact (Low \u2192 High)")){
					selectedFilterComparator = new FilterEdgeImpactComparator();
				} else if(sortSelectedFiltersCombo.getText().equals("Sort by Edge Impact (High \u2192 Low)")) {
					selectedFilterComparator = new FilterEdgeImpactComparator().reversed();
				}
				refreshSelectedFilters();
			}
		});
		sortSelectedFiltersCombo.setItems(new String[] {"Sort by Name (A \u2192 Z)", "Sort by Name (Z \u2192 A)", "Sort by Node Impact (Low \u2192 High)", "Sort by Node Impact (High \u2192 Low)", "Sort by Edge Impact (Low \u2192 High)", "Sort by Edge Impact (High \u2192 Low)"});
		sortSelectedFiltersCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sortSelectedFiltersCombo.select(3);
		
		selectedFiltersScrolledComposite = new ScrolledComposite(selectedFiltersGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		selectedFiltersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		selectedFiltersScrolledComposite.setExpandHorizontal(true);
		selectedFiltersScrolledComposite.setExpandVertical(true);
		
		Group applicableFiltersGroup = new Group(filtersSashForm, SWT.NONE);
		applicableFiltersGroup.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		applicableFiltersGroup.setText("Applicable Filters");
		applicableFiltersGroup.setLayout(new GridLayout(1, false));
		
		Composite sortApplicableFiltersComposite = new Composite(applicableFiltersGroup, SWT.NONE);
		sortApplicableFiltersComposite.setLayout(new GridLayout(2, false));
		sortApplicableFiltersComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label sortApplicableFiltersLabel = new Label(sortApplicableFiltersComposite, SWT.NONE);
		sortApplicableFiltersLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		sortApplicableFiltersLabel.setFont(SWTResourceManager.getFont(".SF NS Text", 11, SWT.NORMAL));
		sortApplicableFiltersLabel.setText("Sort: ");
		
		Combo sortApplicableFiltersCombo = new Combo(sortApplicableFiltersComposite, SWT.READ_ONLY);
		sortApplicableFiltersCombo.setItems(new String[] {"Sort by Name (A \u2192 Z)", "Sort by Name (Z \u2192 A)"});
		sortApplicableFiltersCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sortApplicableFiltersCombo.select(0);
		
		sortApplicableFiltersCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(sortApplicableFiltersCombo.getText().equals("Sort by Name (A \u2192 Z)")){
					applicableFilterComparator = new ApplicableFilterNameComparator();
				} else if(sortApplicableFiltersCombo.getText().equals("Sort by Name (Z \u2192 A)")) {
					applicableFilterComparator = new ApplicableFilterNameComparator().reversed();
				}
				refreshApplicableFilters();
			}
		});
		
		applicableFiltersScrolledComposite = new ScrolledComposite(applicableFiltersGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		applicableFiltersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		applicableFiltersScrolledComposite.setExpandHorizontal(true);
		applicableFiltersScrolledComposite.setExpandVertical(true);
		
		filtersSashForm.setWeights(new int[] {1, 1});
		compositeFiltersSashForm.setWeights(new int[] {230, 800});

		predefinedRootsetSearchBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedRootset = ((FilterableRootset) predefinedRootsetSearchBar.getData(predefinedRootsetSearchBar.getText())).getRootSet().eval();
				refreshRootset();
			}
		});
		
		predefinedRootsetRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isSelected = predefinedRootsetRadio.getSelection();
				
				predefinedRootsetSearchBar.setEnabled(isSelected);
				
				if(isSelected){
					selectedRootset = Common.empty().eval();
					
					userDefinedRootsetStatusLabel.setText("");
			
					nodesTaggedWithAnyCheckbox.setEnabled(false);
					edgesTaggedWithAnyCheckbox.setEnabled(false);
					nodesTaggedWithAllCheckbox.setEnabled(false);
					edgesTaggedWithAllCheckbox.setEnabled(false);
					nodesTaggedWithAnyText.setEnabled(false);
					edgesTaggedWithAnyText.setEnabled(false);
					nodesTaggedWithAllText.setEnabled(false);
					edgesTaggedWithAllText.setEnabled(false);
				}
				
				// disable other radios
				taggedRootsetRadio.setSelection(!isSelected);
				userDefinedRootsetRadio.setSelection(!isSelected);
				
				// enable controls
				predefinedRootsetSearchBar.setEnabled(isSelected);
				
				refreshRootset();
			}
		});

		taggedRootsetRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isSelected = taggedRootsetRadio.getSelection();
				
				if(isSelected){
					predefinedRootsetSearchBar.setEnabled(false);
					userDefinedRootsetStatusLabel.setText("");
					
					selectedRootset = Common.empty().eval();
				}
				
				// disable other radios
				predefinedRootsetRadio.setSelection(!isSelected);
				userDefinedRootsetRadio.setSelection(!isSelected);
				
				// enable controls
				nodesTaggedWithAnyCheckbox.setEnabled(isSelected);
				edgesTaggedWithAnyCheckbox.setEnabled(isSelected);
				nodesTaggedWithAllCheckbox.setEnabled(isSelected);
				edgesTaggedWithAllCheckbox.setEnabled(isSelected);
				nodesTaggedWithAnyText.setEnabled(isSelected);
				edgesTaggedWithAnyText.setEnabled(isSelected);
				nodesTaggedWithAllText.setEnabled(isSelected);
				edgesTaggedWithAllText.setEnabled(isSelected);
				
				refreshRootset();
			}
		});

		userDefinedRootsetRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isSelected = userDefinedRootsetRadio.getSelection();
				
				// disable other radios
				predefinedRootsetRadio.setSelection(!isSelected);
				taggedRootsetRadio.setSelection(!isSelected);
				
				if(isSelected){
					predefinedRootsetSearchBar.setEnabled(false);
					
					nodesTaggedWithAnyCheckbox.setEnabled(false);
					edgesTaggedWithAnyCheckbox.setEnabled(false);
					nodesTaggedWithAllCheckbox.setEnabled(false);
					edgesTaggedWithAllCheckbox.setEnabled(false);
					nodesTaggedWithAnyText.setEnabled(false);
					edgesTaggedWithAnyText.setEnabled(false);
					nodesTaggedWithAllText.setEnabled(false);
					edgesTaggedWithAllText.setEnabled(false);
					
					userDefinedRootsetStatusLabel.setText("Execute Shell Command: CompositeFilterView.setRootset(Q rootset, String name)");
					userDefinedRootsetStatusLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
					selectedRootset = Common.empty().eval();
				} else {
					userDefinedRootsetStatusLabel.setText("");
				}
				
				refreshRootset();
			}
		});
		
		nodesTaggedWithAnyCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(taggedRootsetRadio.getSelection()){
					selectedRootset = getTaggedRootset().eval();
					refreshRootset();
				}
			}
		});
		
		edgesTaggedWithAnyCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(taggedRootsetRadio.getSelection()){
					selectedRootset = getTaggedRootset().eval();
					refreshRootset();
				}
			}
		});
		
		nodesTaggedWithAllCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(taggedRootsetRadio.getSelection()){
					selectedRootset = getTaggedRootset().eval();
					refreshRootset();
				}
			}
		});
		
		edgesTaggedWithAllCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(taggedRootsetRadio.getSelection()){
					selectedRootset = getTaggedRootset().eval();
					refreshRootset();
				}
			}
		});
		
		nodesTaggedWithAnyText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				nodesTaggedWithAnyCheckbox.setSelection(!nodesTaggedWithAnyText.getText().isEmpty());
				if(key.character == '\r' || key.character == ','){
					selectedRootset = getTaggedRootset().eval();
					refreshRootset();
				}
			}
		});
		
		edgesTaggedWithAnyText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				edgesTaggedWithAnyCheckbox.setSelection(!edgesTaggedWithAnyText.getText().isEmpty());
				if(key.character == '\r' || key.character == ','){
					selectedRootset = getTaggedRootset().eval();
					refreshRootset();
				}
			}
		});
		
		nodesTaggedWithAllText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				nodesTaggedWithAllCheckbox.setSelection(!nodesTaggedWithAllText.getText().isEmpty());
				if(key.character == '\r' || key.character == ','){
					selectedRootset = getTaggedRootset().eval();
					refreshRootset();
				}
			}
		});
		
		edgesTaggedWithAllText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent key) {
				edgesTaggedWithAllCheckbox.setSelection(!edgesTaggedWithAllText.getText().isEmpty());
				if(key.character == '\r' || key.character == ','){
					selectedRootset = getTaggedRootset().eval();
					refreshRootset();
				}
			}
		});
		
		tagResultText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				tagResultButton.setEnabled(!tagResultText.getText().isEmpty());
			}
		});
		
		tagResultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String tag = tagResultText.getText();
				for(Node node : evaluatedResult.nodes()){
					node.tag(tag);
				}
				for(Edge edge : evaluatedResult.edges()){
					edge.tag(tag);
				}
				DisplayUtils.showMessage("Successfully applied tag: \"" + tag + "\" to filtered result.");
			}
		});
	}

	private Q getTaggedRootset(){
		Q nodesTaggedWithAny = Common.empty();
		if(nodesTaggedWithAnyCheckbox.getSelection()){
			String selection = nodesTaggedWithAnyText.getText();
			String[] tags = getTags(selection);
			if(tags != null && tags.length > 0){
				nodesTaggedWithAny = Common.universe().nodesTaggedWithAny(tags);
			}
		}
		Q nodesTaggedWithAll = Common.empty();
		if(nodesTaggedWithAllCheckbox.getSelection()){
			String selection = nodesTaggedWithAllText.getText();
			String[] tags = getTags(selection);
			if(tags != null && tags.length > 0){
				nodesTaggedWithAll = Common.universe().nodesTaggedWithAll(tags);
			}
		}
		Q edgesTaggedWithAny = Common.empty();
		if(edgesTaggedWithAnyCheckbox.getSelection()){
			String selection = edgesTaggedWithAnyText.getText();
			String[] tags = getTags(selection);
			if(tags != null && tags.length > 0){
				edgesTaggedWithAny = Common.universe().edgesTaggedWithAny(tags);
			}
		}
		Q edgesTaggedWithAll = Common.empty();
		if(edgesTaggedWithAllCheckbox.getSelection()){
			String selection = edgesTaggedWithAllText.getText();
			String[] tags = getTags(selection);
			if(tags != null && tags.length > 0){
				edgesTaggedWithAll = Common.universe().edgesTaggedWithAll(tags);
			}
		}
		return nodesTaggedWithAny.union(nodesTaggedWithAll, edgesTaggedWithAny, edgesTaggedWithAll);
	}

	private String[] getTags(String selection) {
		ArrayList<String> tags = new ArrayList<String>();
		if(!selection.isEmpty()){
			if(selection.contains(",")){
				for(String tag : selection.split(",")){
					tag = tag.trim();
					if(xcsgConstantNameToValueMap.containsKey(tag)){
						tags.add(xcsgConstantNameToValueMap.get(tag));
					} else {
						tags.add(tag);
					}
				}
			} else {
				String tag = selection.trim();
				if(xcsgConstantNameToValueMap.containsKey(tag)){
					tags.add(xcsgConstantNameToValueMap.get(tag));
				} else {
					tags.add(tag);
				}
			}
		}
		String[] result = new String[tags.size()];
		result = tags.toArray(result);
		return result;
	}
	
	private void refreshRootset() {
		applicableFilters.clear();
		selectedFilters.clear();
		
		for(Filter filter : Filters.getApplicableFilters(Common.toQ(selectedRootset))){
			applicableFilters.add(new ApplicableFilterState(filter, false));
		}
		
		if(selectedRootset.nodes().isEmpty() && selectedRootset.edges().isEmpty()){
			rootsetGroup.setText("Rootset: (empty)");
			resultGroup.setText("Result: (empty)");
		} else {
			rootsetGroup.setText("Rootset: (" + selectedRootset.nodes().size() + " nodes, " + selectedRootset.edges().size() + " edges)");
			resultGroup.setText("Result: (" + selectedRootset.nodes().size() + " nodes, " + selectedRootset.edges().size() + " edges)");
		}
		
		refreshSelectedFilters();
		refreshApplicableFilters();
	}
	
	private void refreshApplicableFilters() {
		// save the old scroll position and content origin
		int scrollPosition = applicableFiltersScrolledComposite.getVerticalBar().getSelection();
		org.eclipse.swt.graphics.Point origin = applicableFiltersScrolledComposite.getOrigin();
		
		Composite applicableFiltersContentComposite = new Composite(applicableFiltersScrolledComposite, SWT.NONE);
		applicableFiltersContentComposite.setLayout(new GridLayout(1, false));
		
		List<ApplicableFilterState> sortedApplicableFilters = new ArrayList<ApplicableFilterState>(applicableFilters);
		Collections.sort(sortedApplicableFilters, applicableFilterComparator);
		
		for(ApplicableFilterState applicableFilterState : sortedApplicableFilters){
			Filter filter = applicableFilterState.getFilter();
			
			ExpandBar applicableFilterExpandBar = new ExpandBar(applicableFiltersContentComposite, SWT.NONE);
			applicableFilterExpandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			ExpandItem applicableFilterExpandBarItem = new ExpandItem(applicableFilterExpandBar, SWT.NONE);
			applicableFilterExpandBarItem.setExpanded(applicableFilterState.isExpanded());
			applicableFilterExpandBarItem.setText("Filter: " + filter.getName());
			
			Composite applicableFilterComposite = new Composite(applicableFilterExpandBar, SWT.NONE);
			applicableFilterExpandBarItem.setControl(applicableFilterComposite);
			applicableFilterComposite.setLayout(new GridLayout(1, false));
			
			Group applicableFilterDescriptionGroup = new Group(applicableFilterComposite, SWT.NONE);
			applicableFilterDescriptionGroup.setLayout(new GridLayout(1, false));
			applicableFilterDescriptionGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			applicableFilterDescriptionGroup.setText("Description");
			
			// TODO: wrap looks much nicer, but is causing height calculation issues
			StyledText applicableFilterDescription = new StyledText(applicableFilterDescriptionGroup, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.READ_ONLY /*| SWT.WRAP*/);
			applicableFilterDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
			applicableFilterDescription.setEditable(false);
			applicableFilterDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			applicableFilterDescription.setText(filter.getDescription());
			
			Group applicableFilterParametersGroup = new Group(applicableFilterComposite, SWT.NONE);
			applicableFilterParametersGroup.setText("Filter Parameters");
			applicableFilterParametersGroup.setLayout(new GridLayout(1, false));
			applicableFilterParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			// TODO: consider removing parameters scrolled composite, it is stealing focus from expand bar listing scrolling and doesn't seem to be necessary anyway
			ScrolledComposite parametersScrolledComposite = new ScrolledComposite(applicableFilterParametersGroup, SWT.H_SCROLL | SWT.V_SCROLL);
			parametersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			parametersScrolledComposite.setExpandHorizontal(true);
			parametersScrolledComposite.setExpandVertical(true); 

			if(filter.getPossibleParameters().isEmpty()){
				Label noParamsLabel = new Label(parametersScrolledComposite, SWT.NONE);
				noParamsLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				noParamsLabel.setAlignment(SWT.CENTER);
				noParamsLabel.setText("No parameters available for this filter.");
				parametersScrolledComposite.setContent(noParamsLabel);
				parametersScrolledComposite.setMinSize(noParamsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			} else {
				Composite inputComposite = new Composite(parametersScrolledComposite, SWT.NONE);
				inputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				inputComposite.setLayout(new GridLayout(1, false));

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
					final boolean requiredParameter = filter.getRequiredParameters().contains(parameterName);
					if(requiredParameter){
						Label requiredFieldsLabel = new Label(inputComposite, SWT.NONE);
						requiredFieldsLabel.setText("*Indicates required fields.");
						requiredFieldsLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						break;
					}
				}
				
				for(String parameterName : parameterNames){
					final Class<? extends Object> parameterType = filter.getPossibleParameters().get(parameterName);
					final boolean requiredParameter = filter.getRequiredParameters().contains(parameterName);
					final boolean isFlag = filter.getPossibleFlags().contains(parameterName);
					
					if(parameterType == Boolean.class){
						Composite booleanInputComposite = new Composite(inputComposite, SWT.NONE);
						booleanInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						booleanInputComposite.setLayout(new GridLayout(2, false));
						booleanInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

						final Label booleanInputLabel = new Label(booleanInputComposite, SWT.NONE);
						booleanInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						if(isFlag){	
							booleanInputLabel.setText("Flag: "+ parameterName);
							booleanInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
						} else {
							booleanInputLabel.setEnabled(requiredParameter);
							booleanInputLabel.setText((requiredParameter ? "*" : "") + parameterName + ":");
							booleanInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
							
							final Button booleanInputCheckbox = new Button(booleanInputComposite, SWT.CHECK);
							booleanInputCheckbox.setEnabled(requiredParameter);
							booleanInputCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							booleanInputCheckbox.setEnabled(false);
						}
					} else if(parameterType == String.class){
						Composite stringInputComposite = new Composite(inputComposite, SWT.NONE);
						stringInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						stringInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						stringInputComposite.setLayout(new GridLayout(2, false));
						
						final Label stringInputLabel = new Label(stringInputComposite, SWT.NONE);
						stringInputLabel.setEnabled(requiredParameter);
						stringInputLabel.setText((requiredParameter ? "*" : "") + parameterName + ":");
						stringInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
						stringInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));

						final Text stringInputText = new Text(stringInputComposite, SWT.BORDER);
						stringInputText.setEnabled(requiredParameter);
						stringInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						stringInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						stringInputText.setEnabled(false);
					} else if(parameterType == Integer.class){
						Composite integerInputComposite = new Composite(inputComposite, SWT.NONE);
						integerInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						integerInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						integerInputComposite.setLayout(new GridLayout(2, false));
						
						final Label integerInputLabel = new Label(integerInputComposite, SWT.NONE);
						integerInputLabel.setEnabled(requiredParameter);
						integerInputLabel.setText((requiredParameter ? "*" : "") + parameterName + ":");
						integerInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
						integerInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						final Text integerInputText = new Text(integerInputComposite, SWT.BORDER);
						integerInputText.setEnabled(requiredParameter);
						integerInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						integerInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						integerInputText.setEnabled(false);
					} else if(parameterType == Double.class){
						Composite doubleInputComposite = new Composite(inputComposite, SWT.NONE);
						doubleInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						doubleInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						doubleInputComposite.setLayout(new GridLayout(2, false));

						final Label doubleInputLabel = new Label(doubleInputComposite, SWT.NONE);
						doubleInputLabel.setEnabled(requiredParameter);
						doubleInputLabel.setText((requiredParameter ? "*" : "") + parameterName + ":");
						doubleInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
						doubleInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						final Text doubleInputText = new Text(doubleInputComposite, SWT.BORDER);
						doubleInputText.setEnabled(requiredParameter);
						doubleInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						doubleInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						doubleInputText.setEnabled(false);
					} else {
						Label unsupportedParamsLabel = new Label(parametersScrolledComposite, SWT.NONE);
						unsupportedParamsLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						unsupportedParamsLabel.setAlignment(SWT.CENTER);
						unsupportedParamsLabel.setText("This filter has unsupported parameter types!");
						parametersScrolledComposite.setContent(unsupportedParamsLabel);
						parametersScrolledComposite.setMinSize(unsupportedParamsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					}
				}
				
				parametersScrolledComposite.setContent(inputComposite);
				parametersScrolledComposite.setMinSize(inputComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
			
			Button addFilterButton = new Button(applicableFilterComposite, SWT.NONE);
			addFilterButton.setText("\u2190 Add Filter");

			addFilterButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectedFilters.add(new SelectedFilterState(filter, true, selectedRootset, true));
					refreshSelectedFilters();
					refreshApplicableFilters();
				}
			});
			
			// disable the add button if the filter is already selected
			for(SelectedFilterState selectedFilter : selectedFilters){
				if(selectedFilter.getFilter().getName().equals(filter.getName())){
					addFilterButton.setEnabled(false);
					break;
				}
			}
			
			applicableFilterExpandBarItem.setExpanded(applicableFilterState.isExpanded());
			applicableFilterExpandBarItem.setHeight(applicableFilterExpandBarItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			
			// tracking the expand state of the expanded items
			final ExpandAdapter expandAdapter = new ExpandAdapter() {
				@Override
				public void itemCollapsed(ExpandEvent e) {
					applicableFilterState.setExpanded(false);
					refreshApplicableFilters();
				}

				@Override
				public void itemExpanded(ExpandEvent e) {
					applicableFilterState.setExpanded(true);
					refreshApplicableFilters();
				}
			};
			applicableFilterExpandBar.addExpandListener(expandAdapter);
		}
		
		// update the content
		applicableFiltersScrolledComposite.setContent(applicableFiltersContentComposite);
		applicableFiltersScrolledComposite.setMinSize(applicableFiltersContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		// set the scroll position on redraw
		applicableFiltersScrolledComposite.getVerticalBar().setSelection(scrollPosition);
		applicableFiltersScrolledComposite.setOrigin(origin);
	}

	private void refreshSelectedFilters() {
		// save the old scroll position and content origin
		int scrollPosition = selectedFiltersScrolledComposite.getVerticalBar().getSelection();
		org.eclipse.swt.graphics.Point origin = selectedFiltersScrolledComposite.getOrigin();
		
		Composite selectedFiltersContentComposite = new Composite(selectedFiltersScrolledComposite, SWT.NONE);
		selectedFiltersContentComposite.setLayout(new GridLayout(1, false));
		
		List<SelectedFilterState> sortedSelectedFilters = new ArrayList<SelectedFilterState>(selectedFilters);
		Collections.sort(sortedSelectedFilters, selectedFilterComparator);
		
		for(SelectedFilterState selectedFilterState : sortedSelectedFilters){
			Filter filter = selectedFilterState.getFilter();
			
			// force the filter to be disabled if parameters are not configured
			try {
				filter.checkParameters(selectedFilterState.filterParameters);
			} catch (Exception e){
				selectedFilterState.setEnabled(false);
			}
			
			ExpandBar selectedFilterExpandBar = new ExpandBar(selectedFiltersContentComposite, SWT.NONE);
			selectedFilterExpandBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
			
			ExpandItem selectedFilterExpandBarItem = new ExpandItem(selectedFilterExpandBar, SWT.NONE);
			selectedFilterExpandBarItem.setExpanded(selectedFilterState.isExpanded());
			if(selectedFilterState.isEnabled()){
				selectedFilterExpandBarItem.setText("Filter: " + filter.getName());
			} else {
				selectedFilterExpandBarItem.setText("[Disabled] Filter: " + filter.getName());
			}
			
			Composite selectedFilterComposite = new Composite(selectedFilterExpandBar, SWT.NONE);
			selectedFilterExpandBarItem.setControl(selectedFilterComposite);
			selectedFilterComposite.setLayout(new GridLayout(1, false));
			
			Group selectedFilterParametersGroup = new Group(selectedFilterComposite, SWT.NONE);
			selectedFilterParametersGroup.setText("Filter Parameters");
			selectedFilterParametersGroup.setLayout(new GridLayout(1, false));
			selectedFilterParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			// TODO: consider removing parameters scrolled composite, it is stealing focus from expand bar listing scrolling and doesn't seem to be necessary anyway
			ScrolledComposite parametersScrolledComposite = new ScrolledComposite(selectedFilterParametersGroup, SWT.H_SCROLL | SWT.V_SCROLL);
			parametersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			parametersScrolledComposite.setExpandHorizontal(true);
			parametersScrolledComposite.setExpandVertical(true); 

			Group selectedFilterImpactGroup = new Group(selectedFilterComposite, SWT.NONE);
			selectedFilterImpactGroup.setLayout(new GridLayout(3, false));
			selectedFilterImpactGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			updateFilterImpact(selectedFilterState, selectedFilterImpactGroup);

			Button toggleFilterActivationButton = new Button(selectedFilterImpactGroup, SWT.NONE);
			if(selectedFilterState.isEnabled()){
				toggleFilterActivationButton.setText("Disable Filter");
			} else {
				toggleFilterActivationButton.setText("Enable Filter");
			}
			
			Button deleteFilterButton = new Button(selectedFilterImpactGroup, SWT.NONE);
			deleteFilterButton.setText("Delete Filter");
			
			Button showSelectedFilterResultButton = new Button(selectedFilterImpactGroup, SWT.NONE);
			showSelectedFilterResultButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
			showSelectedFilterResultButton.setText("Show Result");

			if(filter.getPossibleParameters().isEmpty()){
				Label noParamsLabel = new Label(parametersScrolledComposite, SWT.NONE);
				noParamsLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				noParamsLabel.setAlignment(SWT.CENTER);
				noParamsLabel.setText("No parameters available for this filter.");
				parametersScrolledComposite.setContent(noParamsLabel);
				parametersScrolledComposite.setMinSize(noParamsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			} else {
				Composite inputComposite = new Composite(parametersScrolledComposite, SWT.NONE);
				inputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				inputComposite.setLayout(new GridLayout(1, false));

				// add the parameters in alphabetical order for UI consistency
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
					final boolean requiredParameter = filter.getRequiredParameters().contains(parameterName);
					if(requiredParameter){
						Label requiredFieldsLabel = new Label(inputComposite, SWT.NONE);
						requiredFieldsLabel.setText("*Indicates required fields.");
						requiredFieldsLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						break;
					}
				}
				
				Label validationLabel = new Label(inputComposite, SWT.NONE);
				validationLabel.setText("");
				validationLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
				validationLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
				validationLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

				for(String parameterName : parameterNames){
					final Class<? extends Object> parameterType = filter.getPossibleParameters().get(parameterName);
					final boolean requiredParameter = filter.getRequiredParameters().contains(parameterName);
					final boolean isFlag = filter.getPossibleFlags().contains(parameterName);
					
					if(parameterType == Boolean.class){
						Composite booleanInputComposite = new Composite(inputComposite, SWT.NONE);
						booleanInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						booleanInputComposite.setLayout(new GridLayout(3, false));
						booleanInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

						final Button enableBooleanInputCheckbox = new Button(booleanInputComposite, SWT.CHECK);
						enableBooleanInputCheckbox.setEnabled(!requiredParameter);
						enableBooleanInputCheckbox.setToolTipText(filter.getParameterDescription(parameterName));
						enableBooleanInputCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						// restore saved state
						enableBooleanInputCheckbox.setSelection(selectedFilterState.filterParameters.containsKey(parameterName));
						
						final Label booleanInputLabel = new Label(booleanInputComposite, SWT.NONE);
						booleanInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						if(isFlag){	
							booleanInputLabel.setText("Flag: " + parameterName);
							booleanInputLabel.setToolTipText(filter.getParameterDescription(parameterName));
							
							enableBooleanInputCheckbox.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									boolean enabled = enableBooleanInputCheckbox.getSelection();
									booleanInputLabel.setEnabled(enabled);
									if(enabled){
										selectedFilterState.filterParameters.put(parameterName, true);
									} else {
										selectedFilterState.filterParameters.remove(parameterName);
									}
									updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
									if(enabled){
										updateResults();
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
							
							// restore saved state
							if(selectedFilterState.filterParameters.containsKey(parameterName)){
								booleanInputCheckbox.setSelection(true);
							}
							
							booleanInputCheckbox.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									selectedFilterState.filterParameters.put(parameterName, booleanInputCheckbox.getSelection());
									updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
									updateResults();
								}
							});
							
							enableBooleanInputCheckbox.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									boolean enabled = enableBooleanInputCheckbox.getSelection();
									booleanInputLabel.setEnabled(enabled);
									booleanInputCheckbox.setEnabled(enabled);
									if(enabled){
										selectedFilterState.filterParameters.put(parameterName, booleanInputCheckbox.getSelection());
									} else {
										selectedFilterState.filterParameters.remove(parameterName);
									}
									updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
									if(enabled){
										updateResults();
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

						// restore saved state
						if(selectedFilterState.filterParameters.containsKey(parameterName)){
							enableStringInputCheckbox.setSelection(true);
							if(selectedFilterState.filterParameters.get(parameterName) != null){
								stringInputText.setText(selectedFilterState.filterParameters.get(parameterName).toString());
							}
						}
						
						enableStringInputCheckbox.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								boolean enabled = enableStringInputCheckbox.getSelection();
								
								// save enabled state
								if(enabled){
									selectedFilterState.filterParameters.put(parameterName, null);
								} else {
									selectedFilterState.filterParameters.remove(parameterName);
								}
								
								stringInputLabel.setEnabled(enabled);
								stringInputText.setEnabled(enabled);
								if(enabled){
									String text = stringInputText.getText();
									if(!text.equals("")){
										selectedFilterState.filterParameters.put(parameterName, text);
										updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
									} else {
										validationLabel.setText(parameterName + " must be an non-empty string.");
										if(selectedFilterState.isEnabled()){
											selectedFilterState.setEnabled(false);
											refreshSelectedFilters();
										}
									}
									updateResults();
								} else {
									selectedFilterState.filterParameters.remove(parameterName);
									updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
								}
							}
						});

						stringInputText.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								String text = stringInputText.getText();
								if(!text.equals("")){
									selectedFilterState.filterParameters.put(parameterName, text);
									updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
									updateResults();
								} else {
									validationLabel.setText(parameterName + " must be an non-empty string.");
									if(selectedFilterState.isEnabled()){
										selectedFilterState.setEnabled(false);
										refreshSelectedFilters();
									}
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

						// restore saved state
						if(selectedFilterState.filterParameters.containsKey(parameterName)){
							enableIntegerInputCheckbox.setSelection(true);
							if(selectedFilterState.filterParameters.get(parameterName) != null){
								integerInputText.setText(selectedFilterState.filterParameters.get(parameterName).toString());
							}
						}
						
						enableIntegerInputCheckbox.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								boolean enabled = enableIntegerInputCheckbox.getSelection();
								
								// save enabled state
								if(enabled){
									selectedFilterState.filterParameters.put(parameterName, null);
								} else {
									selectedFilterState.filterParameters.remove(parameterName);
								}
								
								integerInputLabel.setEnabled(enabled);
								integerInputText.setEnabled(enabled);
								if(enabled){
									try {
										selectedFilterState.filterParameters.put(parameterName, Integer.parseInt(integerInputText.getText()));
										updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
									} catch (Exception ex){
										validationLabel.setText(parameterName + " must be an integer.");
										if(selectedFilterState.isEnabled()){
											selectedFilterState.setEnabled(false);
											refreshSelectedFilters();
										}
									}
									updateResults();
								} else {
									selectedFilterState.filterParameters.remove(parameterName);
									updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
								}
							}
						});

						integerInputText.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								try {
									selectedFilterState.filterParameters.put(parameterName, Integer.parseInt(integerInputText.getText()));
									updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
									updateResults();
								} catch (Exception ex){
									validationLabel.setText(parameterName + " must be an integer.");
									if(selectedFilterState.isEnabled()){
										selectedFilterState.setEnabled(false);
										refreshSelectedFilters();
									}
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
						
						// restore saved state
						if(selectedFilterState.filterParameters.containsKey(parameterName)){
							enableDoubleInputCheckbox.setSelection(true);
							if(selectedFilterState.filterParameters.get(parameterName) != null){
								doubleInputText.setText(selectedFilterState.filterParameters.get(parameterName).toString());
							}
						}
						
						enableDoubleInputCheckbox.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								boolean enabled = enableDoubleInputCheckbox.getSelection();
								
								// save enabled state
								if(enabled){
									selectedFilterState.filterParameters.put(parameterName, null);
								} else {
									selectedFilterState.filterParameters.remove(parameterName);
								}
								
								doubleInputLabel.setEnabled(enabled);
								doubleInputText.setEnabled(enabled);
								if(enabled){
									try {
										selectedFilterState.filterParameters.put(parameterName, Double.parseDouble(doubleInputText.getText()));
										updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
										updateResults();
									} catch (Exception ex){
										validationLabel.setText(parameterName + " must be a double.");
										if(selectedFilterState.isEnabled()){
											selectedFilterState.setEnabled(false);
											refreshSelectedFilters();
										}
									}
								} else {
									selectedFilterState.filterParameters.remove(parameterName);
									updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
								}
							}
						});

						doubleInputText.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								try {
									selectedFilterState.filterParameters.put(parameterName, Double.parseDouble(doubleInputText.getText()));
									updateFilterResult(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
									updateResults();
								} catch (Exception ex){
									validationLabel.setText(parameterName + " must be a double.");
									if(selectedFilterState.isEnabled()){
										selectedFilterState.setEnabled(false);
										refreshSelectedFilters();
									}
								}
							}
						});
					} else {
						Label unsupportedParamsLabel = new Label(parametersScrolledComposite, SWT.NONE);
						unsupportedParamsLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						unsupportedParamsLabel.setAlignment(SWT.CENTER);
						unsupportedParamsLabel.setText("This filter has unsupported parameter types!");
						parametersScrolledComposite.setContent(unsupportedParamsLabel);
						parametersScrolledComposite.setMinSize(unsupportedParamsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					}
				}
				
				// check if the filter should be disabled due to missing configuration values
				validateFilterParameters(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup);
				
				parametersScrolledComposite.setContent(inputComposite);
				parametersScrolledComposite.setMinSize(inputComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
			
			toggleFilterActivationButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectedFilterState.setEnabled(!selectedFilterState.isEnabled());
					refreshSelectedFilters();
					updateResults();
				}
			});
			
			deleteFilterButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectedFilters.remove(selectedFilterState);
					refreshSelectedFilters();
					refreshApplicableFilters();
				}
			});
			
			showSelectedFilterResultButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					DisplayUtils.show(Common.toQ(selectedFilterState.getFilteredRootset()), selectedFilterState.getFilter().getName());
				}
			});
			
			// tracking the expand state of the expanded items
			final ExpandAdapter expandAdapter = new ExpandAdapter() {
				@Override
				public void itemCollapsed(ExpandEvent e) {
					selectedFilterState.setExpanded(false);
					refreshSelectedFilters();
				}

				@Override
				public void itemExpanded(ExpandEvent e) {
					selectedFilterState.setExpanded(true);
					refreshSelectedFilters();
				}
			};
			selectedFilterExpandBar.addExpandListener(expandAdapter);
			
			selectedFilterExpandBarItem.setHeight(selectedFilterExpandBarItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		}
		
		// update the content
		selectedFiltersScrolledComposite.setContent(selectedFiltersContentComposite);
		selectedFiltersScrolledComposite.setMinSize(selectedFiltersContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		// set the scroll position on redraw
		selectedFiltersScrolledComposite.getVerticalBar().setSelection(scrollPosition);
		selectedFiltersScrolledComposite.setOrigin(origin);
	}

	private void updateFilterResult(SelectedFilterState selectedFilterState, Filter filter, Label validationLabel, Group selectedFilterImpactGroup) {
		if(validateFilterParameters(selectedFilterState, filter, validationLabel, selectedFilterImpactGroup)){
			try {
				selectedFilterState.updateFilterResult();
				updateFilterImpact(selectedFilterState, selectedFilterImpactGroup);
			} catch (InvalidFilterParameterException ex) {
				// should never happen
				Log.warning("Invalid Filter Configuration", ex);
			}
		}
	}

	private void updateFilterImpact(SelectedFilterState selectedFilterState, Group selectedFilterImpactGroup) {
		if(selectedFilterState.getNodeImpact() == 0 && selectedFilterState.getEdgeImpact() == 0){
			selectedFilterImpactGroup.setText("Filter Impact: none");
		} else {
			double nodePercentage = ((double) selectedFilterState.getNodeImpact() / (double) selectedRootset.nodes().size()) * 100.0;
			double edgePercentage = ((double) selectedFilterState.getEdgeImpact() / (double) selectedRootset.edges().size()) * 100.0;
			selectedFilterImpactGroup.setText("Filter Impact: filtered " + selectedFilterState.getNodeImpact() + " nodes (" + df.format(nodePercentage) + " %), " + selectedFilterState.getEdgeImpact() + " edges (" + df.format(edgePercentage) + " %)");
		}
	}
	
	private void updateResults() {
		ArrayList<Q> filteredResults = new ArrayList<Q>();
		for(SelectedFilterState filter : selectedFilters){
			if(filter.isEnabled()){
				filteredResults.add(Common.toQ(filter.getFilteredRootset()));
			}
		}
		Q result;
		if(filteredResults.isEmpty()){
			result = Common.toQ(selectedRootset);
		} else if(filteredResults.size() == 1){
			result = filteredResults.get(0);
		} else {
			Q first = filteredResults.remove(0);
			Q[] rest = new Q[filteredResults.size()];
			filteredResults.toArray(rest);
			result = first.intersection(rest);
		}
		evaluatedResult = result.eval();
		
		if(evaluatedResult.nodes().isEmpty() && evaluatedResult.edges().isEmpty()){
			resultGroup.setText("Result: (empty)");
		} else {
			resultGroup.setText("Result: (" + evaluatedResult.nodes().size() + " nodes, " + evaluatedResult.edges().size() + " edges)");
		}
	}

	private boolean validateFilterParameters(SelectedFilterState selectedFilterState, Filter filter, Label validationLabel, Group selectedFilterImpactGroup) {
		boolean isValid = validateFilterParameters(filter, selectedFilterState.filterParameters, validationLabel, selectedFilterImpactGroup);
		if(!isValid){
			if(selectedFilterState.isEnabled()){
				selectedFilterState.setEnabled(false);
				refreshSelectedFilters();
			}
		}
		return isValid;
	}
	
	private boolean validateFilterParameters(Filter filter, Map<String, Object> filterParameters, Label validationLabel, Group selectedFilterImpactGroup){
		try {
			filter.checkParameters(filterParameters);
			validationLabel.setText("");
			return true;
		} catch (Exception e){
			if(e.getMessage() != null){
				validationLabel.setText(e.getMessage());
			} else {
				validationLabel.setText("Caught: " + e.getClass().getSimpleName());
			}
			selectedFilterImpactGroup.setText("Filter Impact: none");
			return false;
		}
	}

	@Override
	public void setFocus() {
		
	}
}
