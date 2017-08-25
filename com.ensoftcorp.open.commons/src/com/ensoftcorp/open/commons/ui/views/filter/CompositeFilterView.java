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
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;

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
		
		Group grpSort = new Group(controlsComposite, SWT.NONE);
		grpSort.setLayout(new GridLayout(1, false));
		grpSort.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		grpSort.setText("Sort");
		
		Button btnSortBy = new Button(grpSort, SWT.NONE);
		btnSortBy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnSortBy.setText("Sort by Impact");
		
		Button btnSortByName = new Button(grpSort, SWT.NONE);
		btnSortByName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnSortByName.setText("Sort by Name");
		
		Button btnSortByType = new Button(grpSort, SWT.NONE);
		btnSortByType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnSortByType.setText("Sort by Type");
		
		Composite filtersComposite = new Composite(sashForm, SWT.NONE);
		filtersComposite.setLayout(new GridLayout(1, false));
		
		Composite rootsetComposite = new Composite(filtersComposite, SWT.NONE);
		rootsetComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		rootsetComposite.setLayout(new GridLayout(2, false));
		
		Label rootsetLabel = new Label(rootsetComposite, SWT.NONE);
		rootsetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		rootsetLabel.setText("Rootset: ");
		
		Combo rootsetSearchBar = new Combo(rootsetComposite, SWT.NONE);
		rootsetSearchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		SashForm sashForm_1 = new SashForm(filtersComposite, SWT.NONE);
		sashForm_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group grpSelectedFilters = new Group(sashForm_1, SWT.NONE);
		grpSelectedFilters.setText("Selected Filters");
		grpSelectedFilters.setLayout(new GridLayout(1, false));
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(grpSelectedFilters, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		Composite composite_1 = new Composite(scrolledComposite, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		
		ExpandBar expandBar_2 = new ExpandBar(composite_1, SWT.NONE);
		expandBar_2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		ExpandItem xpndtmFilterTodo = new ExpandItem(expandBar_2, SWT.NONE);
		xpndtmFilterTodo.setExpanded(true);
		xpndtmFilterTodo.setText("[Disabled] Filter: TODO");
		
		Composite composite_5 = new Composite(expandBar_2, SWT.NONE);
		xpndtmFilterTodo.setControl(composite_5);
		composite_5.setLayout(new GridLayout(1, false));
		
		Group grpFilterParameters = new Group(composite_5, SWT.NONE);
		grpFilterParameters.setText("Filter Parameters");
		grpFilterParameters.setLayout(new GridLayout(1, false));
		grpFilterParameters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group grpFilterImpactFiltered = new Group(composite_5, SWT.NONE);
		grpFilterImpactFiltered.setLayout(new GridLayout(3, false));
		grpFilterImpactFiltered.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpFilterImpactFiltered.setText("Filter Impact: filtered xx nodes (xx %), yy edges (yy %)");
		
		Button btnEnableFilter = new Button(grpFilterImpactFiltered, SWT.NONE);
		btnEnableFilter.setText("Enable Filter");
		
		Button btnDeleteFilter = new Button(grpFilterImpactFiltered, SWT.NONE);
		btnDeleteFilter.setText("Delete Filter");
		
		Button btnShowResult = new Button(grpFilterImpactFiltered, SWT.NONE);
		btnShowResult.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnShowResult.setText("Show Result");
		xpndtmFilterTodo.setHeight(150);
		scrolledComposite.setContent(composite_1);
		scrolledComposite.setMinSize(composite_1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Group grpApplicableFilters = new Group(sashForm_1, SWT.NONE);
		grpApplicableFilters.setText("Applicable Filters");
		grpApplicableFilters.setLayout(new GridLayout(1, false));
		
		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(grpApplicableFilters, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);
		
		Composite composite_2 = new Composite(scrolledComposite_1, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		
		ExpandBar expandBar_1 = new ExpandBar(composite_2, SWT.NONE);
		expandBar_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ExpandItem xpndtmNewExpanditem_1 = new ExpandItem(expandBar_1, SWT.NONE);
		xpndtmNewExpanditem_1.setExpanded(true);
		xpndtmNewExpanditem_1.setText("Filter: TODO");
		
		Composite composite_4 = new Composite(expandBar_1, SWT.NONE);
		xpndtmNewExpanditem_1.setControl(composite_4);
		xpndtmNewExpanditem_1.setHeight(150);
		composite_4.setLayout(new GridLayout(1, false));
		
		Group grpFilterParameters_1 = new Group(composite_4, SWT.NONE);
		grpFilterParameters_1.setText("Filter Parameters");
		grpFilterParameters_1.setLayout(new GridLayout(1, false));
		grpFilterParameters_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scrolledComposite_1.setContent(composite_2);
		scrolledComposite_1.setMinSize(composite_2.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashForm_1.setWeights(new int[] {1, 1});
		sashForm.setWeights(new int[] {250, 931});
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
