package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import com.ensoftcorp.open.commons.filters.Filter;
import com.ensoftcorp.open.commons.filters.Filters;
import com.ensoftcorp.open.commons.filters.rootset.FilterableRootsets;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Group;
import org.eclipse.wb.swt.ResourceManager;

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
	
	private Set<Filter> activeFilters = new HashSet<Filter>();
	private Combo searchBar;
	private ScrolledComposite activeFiltersScrolledComposite;

	@Override
	public void createPartControl(Composite composite) {
		composite.setLayout(new GridLayout(1, false));
		
		SashForm sashForm = new SashForm(composite, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite controlsComposite = new Composite(sashForm, SWT.NONE);
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
		
		Button btnNewButton_2 = new Button(recipeGroup, SWT.NONE);
		btnNewButton_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnNewButton_2.setText("Load Recipe");
		
		Button saveRecipeButton = new Button(recipeGroup, SWT.NONE);
		saveRecipeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		saveRecipeButton.setText("Save Recipe");
		
		Composite filtersComposite = new Composite(sashForm, SWT.NONE);
		filtersComposite.setLayout(new GridLayout(1, false));
		
		Composite composite_1 = new Composite(filtersComposite, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label rootsetLabel = new Label(composite_1, SWT.NONE);
		rootsetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		rootsetLabel.setText("Rootset: ");
		
		Combo rootsetSearchBar = new Combo(composite_1, SWT.NONE);
		rootsetSearchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite filterSearchComposite = new Composite(filtersComposite, SWT.NONE);
		filterSearchComposite.setLayout(new GridLayout(3, false));
		filterSearchComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label applicableFiltersLabel = new Label(filterSearchComposite, SWT.NONE);
		applicableFiltersLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		applicableFiltersLabel.setText("Applicable Filters: ");
		
		Combo filtersComboBar = new Combo(filterSearchComposite, SWT.NONE);
		filtersComboBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button button = new Button(filterSearchComposite, SWT.NONE);
		button.setText("+");
		
		ScrolledComposite activeFiltersScrolledComposite2 = new ScrolledComposite(filtersComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		activeFiltersScrolledComposite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		activeFiltersScrolledComposite2.setExpandHorizontal(true);
		activeFiltersScrolledComposite2.setExpandVertical(true);
		
		Composite activeFiltersScrolledCompositeContet = new Composite(activeFiltersScrolledComposite2, SWT.NONE);
		activeFiltersScrolledCompositeContet.setLayout(new GridLayout(1, false));
		
		ExpandBar activeFilterExpandBar = new ExpandBar(activeFiltersScrolledCompositeContet, SWT.NONE);
		activeFilterExpandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ExpandItem activeFilterExpandBarItem = new ExpandItem(activeFilterExpandBar, SWT.NONE);
		activeFilterExpandBarItem.setExpanded(true);
		activeFilterExpandBarItem.setText("Filter: TODO");
		
		Composite activeFilterContent = new Composite(activeFilterExpandBar, SWT.NONE);
		activeFilterExpandBarItem.setControl(activeFilterContent);
		activeFilterContent.setLayout(new GridLayout(1, false));
		
		Group filterParametersGroup = new Group(activeFilterContent, SWT.NONE);
		filterParametersGroup.setText("Filter Parameters");
		filterParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group activeFilterControlsGroup = new Group(activeFilterContent, SWT.NONE);
		activeFilterControlsGroup.setLayout(new GridLayout(2, false));
		activeFilterControlsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		activeFilterControlsGroup.setText("Filtered: xx nodes (xx %), yy edges (yy %)");
		
		Button deleteFilterButton = new Button(activeFilterControlsGroup, SWT.NONE);
		deleteFilterButton.setText("Delete Filter");
		
		Button showFilterResultButton = new Button(activeFilterControlsGroup, SWT.NONE);
		showFilterResultButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		showFilterResultButton.setText("Show Result");
		activeFilterExpandBarItem.setHeight(180);
		activeFiltersScrolledComposite2.setContent(activeFiltersScrolledCompositeContet);
		activeFiltersScrolledComposite2.setMinSize(activeFiltersScrolledCompositeContet.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashForm.setWeights(new int[] {200, 381});
//		Composite searchBarComposite = new Composite(composite, SWT.BORDER);
//		searchBarComposite.setLayout(new GridLayout(2, false));
//		searchBarComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		Label searchLabel = new Label(composite, SWT.NONE);
//		searchLabel.setText("Search: ");
//		
//		searchBar = new Combo(searchBarComposite, SWT.NONE);
//		searchBar.setEnabled(false);
//		searchBar.setToolTipText("Search for a filter by typing part of the name and pressing return or selecting an autocomplete suggestion.");
//		searchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		activeFiltersScrolledComposite = new ScrolledComposite(composite, SWT.BORDER | SWT.V_SCROLL);
//		activeFiltersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		activeFiltersScrolledComposite.setExpandHorizontal(true);
//		activeFiltersScrolledComposite.setExpandVertical(true);
	}

	@Override
	public void setFocus() {
		
	}
}
