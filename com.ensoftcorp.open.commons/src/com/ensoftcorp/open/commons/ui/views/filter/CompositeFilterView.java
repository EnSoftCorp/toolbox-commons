package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
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

import com.ensoftcorp.open.commons.filters.Filter;
import com.ensoftcorp.open.commons.filters.Filters;
import com.ensoftcorp.open.commons.filters.rootset.FilterableRootsets;

public class CompositeFilterView extends ViewPart {
	
	public CompositeFilterView() {
		setTitleImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/toolbox.gif"));
		setPartName("Composite Filter View");
		
		// load plugin filter contributions
		Filters.loadFilterContributions();
		FilterableRootsets.loadFilterContributions();
	}

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.filter.CompositeFilterView";
	
	private static final int FONT_SIZE = 11;
	
	private Set<Filter> selectedFilters = new HashSet<Filter>();
	private Set<Filter> applicableFilters = new HashSet<Filter>();
	
	private Comparator<Filter> filterComparator = new FilterNameComparator();
	
	private ScrolledComposite selectedFiltersScrolledComposite;
	private ScrolledComposite applicableFiltersScrolledComposite;

	private static class FilterNameComparator implements Comparator<Filter> {
		@Override
		public int compare(Filter a, Filter b) {
			return a.getName().compareTo(b.getName());
		}
	}
	
	@Override
	public void createPartControl(Composite composite) {
		composite.setLayout(new GridLayout(1, false));
		
		SashForm compositeFiltersSashForm = new SashForm(composite, SWT.NONE);
		compositeFiltersSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite controlsComposite = new Composite(compositeFiltersSashForm, SWT.NONE);
		controlsComposite.setLayout(new GridLayout(1, false));
		
		Group rootsetGroup = new Group(controlsComposite, SWT.NONE);
		rootsetGroup.setLayout(new GridLayout(1, false));
		rootsetGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rootsetGroup.setText("Rootset: (xx nodes, yy edges)");
		
		Button showRootsetButton = new Button(rootsetGroup, SWT.NONE);
		showRootsetButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		showRootsetButton.setText("Show Rootset");
		
		Group resultGroup = new Group(controlsComposite, SWT.NONE);
		resultGroup.setLayout(new GridLayout(1, false));
		resultGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		resultGroup.setText("Result: (xx nodes, yy edges)");
		
		Button showResultButton = new Button(resultGroup, SWT.NONE);
		showResultButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		showResultButton.setText("Show Result");
		
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
		
		Group sortGroup = new Group(controlsComposite, SWT.NONE);
		sortGroup.setLayout(new GridLayout(1, false));
		sortGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		sortGroup.setText("Sort Selected Filters");
		
		Button sortByImpactButton = new Button(sortGroup, SWT.NONE);
		sortByImpactButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sortByImpactButton.setText("Sort by Impact");
		
		Button sortByNameButton = new Button(sortGroup, SWT.NONE);
		sortByNameButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sortByNameButton.setText("Sort by Name");
		
		Composite filtersComposite = new Composite(compositeFiltersSashForm, SWT.NONE);
		filtersComposite.setLayout(new GridLayout(1, false));
		
		Group rootsetSelectionGroup = new Group(filtersComposite, SWT.NONE);
		rootsetSelectionGroup.setLayout(new GridLayout(2, false));
		rootsetSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rootsetSelectionGroup.setText("Rootset Selection");

		Button predefinedRootsetRadio = new Button(rootsetSelectionGroup, SWT.RADIO);
		predefinedRootsetRadio.setSelection(true);
		predefinedRootsetRadio.setText("Predefined Rootset: ");
		
		Combo predefinedRootsetSearchBar = new Combo(rootsetSelectionGroup, SWT.NONE);
		predefinedRootsetSearchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button taggedRootsetRadio = new Button(rootsetSelectionGroup, SWT.RADIO);
		taggedRootsetRadio.setText("Tagged Rootset: ");
		
		Group group = new Group(rootsetSelectionGroup, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group taggedWithAnyGroup = new Group(group, SWT.NONE);
		taggedWithAnyGroup.setLayout(new GridLayout(4, false));
		taggedWithAnyGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		taggedWithAnyGroup.setText("Tagged With Any");
		
		Button nodesTaggedWithAnyCheckbox = new Button(taggedWithAnyGroup, SWT.CHECK);
		nodesTaggedWithAnyCheckbox.setEnabled(false);
		nodesTaggedWithAnyCheckbox.setText("Nodes: ");
		
		Text nodesTaggedWithAnyText = new Text(taggedWithAnyGroup, SWT.BORDER);
		nodesTaggedWithAnyText.setToolTipText("A comma seperated list of tag values. Raw values or XCSG common names such as XCSG.ControlFlow_Node can be entered.");
		nodesTaggedWithAnyText.setEnabled(false);
		nodesTaggedWithAnyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button edgesTaggedWithAnyCheckbox = new Button(taggedWithAnyGroup, SWT.CHECK);
		edgesTaggedWithAnyCheckbox.setEnabled(false);
		edgesTaggedWithAnyCheckbox.setText("Edges: ");
		
		Text edgesTaggedWithAnyText = new Text(taggedWithAnyGroup, SWT.BORDER);
		edgesTaggedWithAnyText.setToolTipText("A comma seperated list of tag values. Raw values or XCSG common names such as XCSG.ControlFlow_Edge can be entered.");
		edgesTaggedWithAnyText.setEnabled(false);
		edgesTaggedWithAnyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group taggedWithAllGroup = new Group(group, SWT.NONE);
		taggedWithAllGroup.setLayout(new GridLayout(4, false));
		taggedWithAllGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		taggedWithAllGroup.setText("Tagged With All");
		
		Button nodesTaggedWithAllCheckbox = new Button(taggedWithAllGroup, SWT.CHECK);
		nodesTaggedWithAllCheckbox.setEnabled(false);
		nodesTaggedWithAllCheckbox.setText("Nodes: ");
		
		Text nodesTaggedWithAllText = new Text(taggedWithAllGroup, SWT.BORDER);
		nodesTaggedWithAllText.setToolTipText("A comma seperated list of tag values. Raw values or XCSG common names such as XCSG.ControlFlow_Node can be entered.");
		nodesTaggedWithAllText.setEnabled(false);
		nodesTaggedWithAllText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button edgesTaggedWithAllCheckbox = new Button(taggedWithAllGroup, SWT.CHECK);
		edgesTaggedWithAllCheckbox.setEnabled(false);
		edgesTaggedWithAllCheckbox.setText("Edges: ");
		
		Text edgesTaggedWithAllText = new Text(taggedWithAllGroup, SWT.BORDER);
		edgesTaggedWithAllText.setToolTipText("A comma seperated list of tag values. Raw values or XCSG common names such as XCSG.ControlFlow_Edge can be entered.");
		edgesTaggedWithAllText.setEnabled(false);
		edgesTaggedWithAllText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button userDefinedRootsetRadio = new Button(rootsetSelectionGroup, SWT.RADIO);
		userDefinedRootsetRadio.setText("User Defined Rootset");
		
		Label userDefinedRootsetStatusLabel = new Label(rootsetSelectionGroup, SWT.NONE);
		userDefinedRootsetStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		SashForm filtersSashForm = new SashForm(filtersComposite, SWT.NONE);
		filtersSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group selectedFiltersGroup = new Group(filtersSashForm, SWT.NONE);
		selectedFiltersGroup.setText("Selected Filters");
		selectedFiltersGroup.setLayout(new GridLayout(1, false));
		
		selectedFiltersScrolledComposite = new ScrolledComposite(selectedFiltersGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		selectedFiltersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		selectedFiltersScrolledComposite.setExpandHorizontal(true);
		selectedFiltersScrolledComposite.setExpandVertical(true);
		
		Group applicableFiltersGroup = new Group(filtersSashForm, SWT.NONE);
		applicableFiltersGroup.setText("Applicable Filters");
		applicableFiltersGroup.setLayout(new GridLayout(1, false));
		
		applicableFiltersScrolledComposite = new ScrolledComposite(applicableFiltersGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		applicableFiltersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		applicableFiltersScrolledComposite.setExpandHorizontal(true);
		applicableFiltersScrolledComposite.setExpandVertical(true);
		
		filtersSashForm.setWeights(new int[] {1, 1});
		compositeFiltersSashForm.setWeights(new int[] {230, 800});

		predefinedRootsetRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isSelected = predefinedRootsetRadio.getSelection();
				
				// disable other radios
				taggedRootsetRadio.setSelection(!isSelected);
				userDefinedRootsetRadio.setSelection(!isSelected);
				
				// enable controls
				predefinedRootsetSearchBar.setEnabled(isSelected);
			}
		});

		taggedRootsetRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isSelected = taggedRootsetRadio.getSelection();
				
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
					userDefinedRootsetStatusLabel.setText("Execute Shell Command: CompositeFilterView.setRootset(Q rootset, String name)");
					userDefinedRootsetStatusLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
				} else {
					userDefinedRootsetStatusLabel.setText("");
				}
			}
		});
		
		nodesTaggedWithAnyText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				nodesTaggedWithAnyCheckbox.setSelection(!nodesTaggedWithAnyText.getText().isEmpty());
			}
		});
		
		edgesTaggedWithAnyText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				edgesTaggedWithAnyCheckbox.setSelection(!edgesTaggedWithAnyText.getText().isEmpty());
			}
		});
		
		nodesTaggedWithAllText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				nodesTaggedWithAllCheckbox.setSelection(!nodesTaggedWithAllText.getText().isEmpty());
			}
		});
		
		edgesTaggedWithAllText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				edgesTaggedWithAllCheckbox.setSelection(!edgesTaggedWithAllText.getText().isEmpty());
			}
		});
		
		refreshSelectedFilters();
		refreshApplicableFilters();
	}
	
	private void refreshApplicableFilters() {
		Composite applicableFiltersContentComposite = new Composite(applicableFiltersScrolledComposite, SWT.NONE);
		applicableFiltersContentComposite.setLayout(new GridLayout(1, false));
		
		ExpandBar applicableFilterExpandBar = new ExpandBar(applicableFiltersContentComposite, SWT.NONE);
		applicableFilterExpandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ExpandItem applicableFilterExpandBarItem = new ExpandItem(applicableFilterExpandBar, SWT.NONE);
		applicableFilterExpandBarItem.setExpanded(true);
		applicableFilterExpandBarItem.setText("Filter: TODO");
		
		Composite applicableFilterComposite = new Composite(applicableFilterExpandBar, SWT.NONE);
		applicableFilterExpandBarItem.setControl(applicableFilterComposite);
		applicableFilterComposite.setLayout(new GridLayout(1, false));
		
		Group applicableFilterDescriptionGroup = new Group(applicableFilterComposite, SWT.NONE);
		applicableFilterDescriptionGroup.setLayout(new GridLayout(1, false));
		applicableFilterDescriptionGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		applicableFilterDescriptionGroup.setText("Description");
		
		StyledText applicableFilterDescription = new StyledText(applicableFilterDescriptionGroup, SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.READ_ONLY | SWT.WRAP);
		applicableFilterDescription.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		applicableFilterDescription.setEditable(false);
		applicableFilterDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group applicableFilterParametersGroup = new Group(applicableFilterComposite, SWT.NONE);
		applicableFilterParametersGroup.setText("Filter Parameters");
		applicableFilterParametersGroup.setLayout(new GridLayout(1, false));
		applicableFilterParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button addFilterButton = new Button(applicableFilterComposite, SWT.NONE);
		addFilterButton.setText("\u2190 Add Filter");
		
		applicableFilterExpandBarItem.setHeight(applicableFilterExpandBarItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		applicableFiltersScrolledComposite.setContent(applicableFiltersContentComposite);
		applicableFiltersScrolledComposite.setMinSize(applicableFiltersContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void refreshSelectedFilters() {
		Composite selectedFiltersContentComposite = new Composite(selectedFiltersScrolledComposite, SWT.NONE);
		selectedFiltersContentComposite.setLayout(new GridLayout(1, false));
		
		ExpandBar selectedFilterExpandBar = new ExpandBar(selectedFiltersContentComposite, SWT.NONE);
		selectedFilterExpandBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		ExpandItem selectedFilterExpandBarItem = new ExpandItem(selectedFilterExpandBar, SWT.NONE);
		selectedFilterExpandBarItem.setExpanded(true);
		selectedFilterExpandBarItem.setText("[Disabled] Filter: TODO");
		
		Composite selectedFilterComposite = new Composite(selectedFilterExpandBar, SWT.NONE);
		selectedFilterExpandBarItem.setControl(selectedFilterComposite);
		selectedFilterComposite.setLayout(new GridLayout(1, false));
		
		Group selectedFilterParametersGroup = new Group(selectedFilterComposite, SWT.NONE);
		selectedFilterParametersGroup.setText("Filter Parameters");
		selectedFilterParametersGroup.setLayout(new GridLayout(1, false));
		selectedFilterParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group selectedFilterImpactGroup = new Group(selectedFilterComposite, SWT.NONE);
		selectedFilterImpactGroup.setLayout(new GridLayout(3, false));
		selectedFilterImpactGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		selectedFilterImpactGroup.setText("Filter Impact: filtered xx nodes (xx %), yy edges (yy %)");
		
		Button toggleFilterActivationButton = new Button(selectedFilterImpactGroup, SWT.NONE);
		toggleFilterActivationButton.setText("Enable Filter");
		
		Button deleteFilterButton = new Button(selectedFilterImpactGroup, SWT.NONE);
		deleteFilterButton.setText("Delete Filter");
		
		Button showSelectedFilterResultButton = new Button(selectedFilterImpactGroup, SWT.NONE);
		showSelectedFilterResultButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		showSelectedFilterResultButton.setText("Show Result");
		
		selectedFilterExpandBarItem.setHeight(selectedFilterExpandBarItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

		selectedFiltersScrolledComposite.setContent(selectedFiltersContentComposite);
		selectedFiltersScrolledComposite.setMinSize(selectedFiltersContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void setFocus() {
		
	}
}
