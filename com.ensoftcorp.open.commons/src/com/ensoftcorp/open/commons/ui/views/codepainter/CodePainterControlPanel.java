package com.ensoftcorp.open.commons.ui.views.codepainter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.atlas.core.indexing.IIndexListener;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.codepainter.CodePainter;
import com.ensoftcorp.open.commons.codepainter.CodePainters;
import com.ensoftcorp.open.commons.codepainter.ColorPalette;
import com.ensoftcorp.open.commons.codepainter.ColorPalettes;
import com.ensoftcorp.open.commons.ui.views.codepainter.CodePainterSmartView.CodePainterSmartViewEventListener;
import com.ensoftcorp.open.commons.utilities.selection.GraphSelectionProviderView;

public class CodePainterControlPanel extends GraphSelectionProviderView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.codepainter.controlpanel"; //$NON-NLS-1$
	
	private CTabFolder folder;
	
	public CodePainterControlPanel(){
		setPartName("Code Painter Control Panel");
		setTitleImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/toolbox.gif"));
		CodePainters.loadCodePainterContributions();
		ColorPalettes.loadColorPaletteContributions();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		boolean indexExists = IndexingUtil.indexExists();
		
		parent.setLayout(new GridLayout(1, false));
		
		folder = new CTabFolder(parent, SWT.NONE);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		folder.setBorderVisible(true);
		folder.setSimple(false); // adds the Eclipse style "swoosh"
		
		final CTabItem codePainterSelectionTab = new CTabItem(folder, SWT.NONE);
		codePainterSelectionTab.setText("Code Painters");
		Composite codePainterSelectionComposite = new Composite(folder, SWT.NONE);
		codePainterSelectionTab.setControl(codePainterSelectionComposite);
		codePainterSelectionComposite.setLayout(new GridLayout(1, false));
		
		Composite searchCodePaintersComposite = new Composite(codePainterSelectionComposite, SWT.NONE);
		searchCodePaintersComposite.setLayout(new GridLayout(2, false));
		searchCodePaintersComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final CTabItem codePainterConfigurationsTab = new CTabItem(folder, SWT.NONE);
		codePainterConfigurationsTab.setText("Configurations");
		Composite codePainterConfigurationComposite = new Composite(folder, SWT.NONE);
		codePainterConfigurationsTab.setControl(codePainterConfigurationComposite);
		codePainterConfigurationComposite.setLayout(new GridLayout(1, false));
		
		final CTabItem codePainterColorPalettesTab = new CTabItem(folder, SWT.NONE);
		codePainterColorPalettesTab.setText("Color Palettes");
		
		Composite codePainterColorPalettesComposite = new Composite(folder, SWT.NONE);
		codePainterColorPalettesTab.setControl(codePainterColorPalettesComposite);
		codePainterColorPalettesComposite.setLayout(new GridLayout(1, false));
		
		final CTabItem codePainterLegendTab = new CTabItem(folder, SWT.NONE);
		codePainterLegendTab.setText("Legend");
		
		Button searchCodePaintersCheckbox = new Button(searchCodePaintersComposite, SWT.CHECK);
		searchCodePaintersCheckbox.setText("Search Code Painters: ");
		searchCodePaintersCheckbox.setEnabled(indexExists);
		
		Combo selectedCodePainterCombo = new Combo(searchCodePaintersComposite, SWT.NONE);
		selectedCodePainterCombo.setEnabled(false);
		selectedCodePainterCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group browseCodePaintersGroup = new Group(codePainterSelectionComposite, SWT.NONE);
		browseCodePaintersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browseCodePaintersGroup.setLayout(new GridLayout(1, false));
		browseCodePaintersGroup.setText("Browse Code Painters:");
		
		Tree categorizedCodePaintersTree = new Tree(browseCodePaintersGroup, SWT.NONE);
		categorizedCodePaintersTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		categorizedCodePaintersTree.setEnabled(indexExists);
		
		// add code painters to categorized tree
		categorizedCodePaintersTree.removeAll();
		
		categorizedCodePaintersTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(categorizedCodePaintersTree.getSelectionCount() == 1){
					CodePainter codePainter = (CodePainter) categorizedCodePaintersTree.getSelection()[0].getData();
					if(codePainter != null){
						if(CodePainterSmartView.setCodePainter(codePainter)){
							selectedCodePainterCombo.setText(codePainter.getTitle());
							refreshSelection();
						}
					}
				}
			}
		});
		
		// populate the combo with the registered code painters
		selectedCodePainterCombo.removeAll();
		ArrayList<CodePainter> codePainters = new ArrayList<CodePainter>(CodePainters.getRegisteredCodePainters());
		Collections.sort(codePainters, new Comparator<CodePainter>(){
			@Override
			public int compare(CodePainter cp1, CodePainter cp2) {
				int categoryComparison = cp1.getCategory().compareTo(cp2.getCategory());
				if(categoryComparison == 0){
					return cp1.getTitle().compareTo(cp2.getTitle());
				} else {
					return categoryComparison;
				}
			}
		});
		for(CodePainter codePainter : codePainters){
			selectedCodePainterCombo.add(codePainter.getTitle());
			selectedCodePainterCombo.setData(codePainter.getTitle(), codePainter);
		}
		for(CodePainter codePainter : codePainters){
			String qualifiedCategory = codePainter.getCategory();
			String[] categoryNames = qualifiedCategory.split("/");
			TreeItem category = null;
			for(int i=0; i<categoryNames.length; i++){
				String categoryName = categoryNames[i];
				boolean categoryExists = false;
				
				// if this is the first level search the tree roots for the category
				// otherwise search the parent categories children for the current category level
				TreeItem[] children = (i==0 ? categorizedCodePaintersTree.getItems() : category.getItems());
				
				for(TreeItem child : children){
					if(child.getText().equals(categoryName)){
						categoryExists = true;
						category = child;
						break;
					}
				}
				if(!categoryExists){
					TreeItem child;
					if(i == 0){
						child = new TreeItem(categorizedCodePaintersTree, SWT.NONE);
					} else {
						child = new TreeItem(category, SWT.NONE);
					}
					child.setText(categoryName);
					child.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/folder.gif"));
					category = child;
				}
			}
			TreeItem codePainterItem = new TreeItem(category, SWT.NONE);
			codePainterItem.setText(codePainter.getTitle());
			codePainterItem.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/brush.gif"));
			codePainterItem.setData(codePainter);
		}
		
		searchCodePaintersCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = searchCodePaintersCheckbox.getSelection();
				categorizedCodePaintersTree.setEnabled(!enabled);
				selectedCodePainterCombo.setEnabled(enabled);
			}
		});
		
		// update the selected code painter upon combo selection
		selectedCodePainterCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CodePainter codePainter = (CodePainter) selectedCodePainterCombo.getData(selectedCodePainterCombo.getText());
				if(codePainter != null){
					if(CodePainterSmartView.setCodePainter(codePainter)){
						refreshSelection();
					}
				}
			}
		});
		
		Group grpControls = new Group(codePainterColorPalettesComposite, SWT.NONE);
		grpControls.setText("Applicable Color Palettes");
		grpControls.setLayout(new GridLayout(2, false));
		grpControls.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Combo applicableColorPalettesCombo = new Combo(grpControls, SWT.NONE);
		applicableColorPalettesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button addColorPaletteLayerCombo = new Button(grpControls, SWT.NONE);
		addColorPaletteLayerCombo.setText("Add Layer");
		
		Group colorPaletteLayersGroup = new Group(codePainterColorPalettesComposite, SWT.NONE);
		colorPaletteLayersGroup.setText("Color Palette Layers");
		colorPaletteLayersGroup.setLayout(new GridLayout(1, false));
		colorPaletteLayersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		ScrolledComposite colorPaletteLayersScrolledComposite = new ScrolledComposite(colorPaletteLayersGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		colorPaletteLayersScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		colorPaletteLayersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		colorPaletteLayersScrolledComposite.setExpandHorizontal(true);
		colorPaletteLayersScrolledComposite.setExpandVertical(true);
		
		Composite colorPaletteLayersContentComposite = new Composite(colorPaletteLayersScrolledComposite, SWT.NONE);
		colorPaletteLayersContentComposite.setLayout(new GridLayout(1, false));
		
		ExpandBar colorPaletteExpandBar = new ExpandBar(colorPaletteLayersContentComposite, SWT.NONE);
		colorPaletteExpandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ExpandItem colorPaletteExpandItem = new ExpandItem(colorPaletteExpandBar, SWT.NONE);
		colorPaletteExpandItem.setExpanded(true);
		colorPaletteExpandItem.setText("Color Palette Name");
		
		Composite colorPaletteContentComposite = new Composite(colorPaletteExpandBar, SWT.NONE);
		colorPaletteExpandItem.setControl(colorPaletteContentComposite);
		colorPaletteExpandItem.setHeight(colorPaletteExpandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		colorPaletteContentComposite.setLayout(new GridLayout(1, false));
		
		Composite colorPaletteOverviewComposite = new Composite(colorPaletteContentComposite, SWT.NONE);
		colorPaletteOverviewComposite.setLayout(new GridLayout(2, false));
		colorPaletteOverviewComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label descriptionLabel = new Label(colorPaletteOverviewComposite, SWT.NONE);
		descriptionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		descriptionLabel.setText("Description: ");
		
		StyledText descriptionText = new StyledText(colorPaletteOverviewComposite, SWT.BORDER);
		descriptionText.setEditable(false);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label enabledLabel = new Label(colorPaletteOverviewComposite, SWT.NONE);
		enabledLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		enabledLabel.setText("Enabled: ");
		
		Button enabledCheckbox = new Button(colorPaletteOverviewComposite, SWT.CHECK);
		enabledCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		
		CTabFolder colorPaletteTabFolder = new CTabFolder(colorPaletteContentComposite, SWT.NONE);
		colorPaletteTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		colorPaletteTabFolder.setBorderVisible(true);
		colorPaletteTabFolder.setSimple(false); // adds the Eclipse style "swoosh"
		
		CTabItem colorPaletteConfigurationsTabItem = new CTabItem(colorPaletteTabFolder, SWT.NONE);
		colorPaletteConfigurationsTabItem.setText("Configurations");
		colorPaletteTabFolder.setSelection(colorPaletteConfigurationsTabItem);
		
		Composite composite = new Composite(colorPaletteTabFolder, SWT.NONE);
		colorPaletteConfigurationsTabItem.setControl(composite);
		composite.setLayout(new GridLayout(1, false));
		
		Label lblTodo = new Label(composite, SWT.NONE);
		lblTodo.setText("TODO");
		
		CTabItem colorPaletteColoringTabItem = new CTabItem(colorPaletteTabFolder, SWT.NONE);
		colorPaletteColoringTabItem.setText("Coloring");
		
		Composite composite_1 = new Composite(colorPaletteTabFolder, SWT.NONE);
		colorPaletteColoringTabItem.setControl(composite_1);
		composite_1.setLayout(new GridLayout(1, false));
		
		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setText("TODO");
		
		Composite colorPaletteControlsComposite = new Composite(colorPaletteContentComposite, SWT.NONE);
		colorPaletteControlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		colorPaletteControlsComposite.setLayout(new GridLayout(3, false));
		
		Button moveUpButton = new Button(colorPaletteControlsComposite, SWT.NONE);
		moveUpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		moveUpButton.setBounds(0, 0, 60, 25);
		moveUpButton.setText("Move Up");
		
		Button moveDownButton = new Button(colorPaletteControlsComposite, SWT.NONE);
		moveDownButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		moveDownButton.setBounds(0, 0, 75, 25);
		moveDownButton.setText("Move Down");
		
		Button deleteButton = new Button(colorPaletteControlsComposite, SWT.NONE);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		deleteButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		deleteButton.setText("Delete");
		colorPaletteLayersScrolledComposite.setContent(colorPaletteLayersContentComposite);
		colorPaletteLayersScrolledComposite.setMinSize(colorPaletteLayersContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		// compute the expand bar item height
		colorPaletteExpandItem.setHeight(colorPaletteExpandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		// initialized the empty legend
		SashForm codePainterLegendSashForm = new SashForm(folder, SWT.NONE);
		codePainterLegendTab.setControl(codePainterLegendSashForm);
		codePainterLegendSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		Group legendNodesGroup = new Group(codePainterLegendSashForm, SWT.NONE);
		legendNodesGroup.setText("Nodes");
		legendNodesGroup.setLayout(new GridLayout(1, false));
		
		ScrolledComposite legendNodesScrolledComposite = new ScrolledComposite(legendNodesGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		legendNodesScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		legendNodesScrolledComposite.setExpandHorizontal(true);
		legendNodesScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		legendNodesScrolledComposite.setExpandVertical(true);
		
		Composite legendNodesContentComposite = new Composite(legendNodesScrolledComposite, SWT.NONE);
		legendNodesContentComposite.setLayout(new GridLayout(1, false));
		legendNodesScrolledComposite.setContent(legendNodesContentComposite);
		legendNodesScrolledComposite.setMinSize(legendNodesContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Group legendEdgesGroup = new Group(codePainterLegendSashForm, SWT.NONE);
		legendEdgesGroup.setText("Edges");
		legendEdgesGroup.setLayout(new GridLayout(1, false));
		
		ScrolledComposite legendEdgesScrolledComposite = new ScrolledComposite(legendEdgesGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		legendEdgesScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		legendEdgesScrolledComposite.setExpandHorizontal(true);
		legendEdgesScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		legendEdgesScrolledComposite.setExpandVertical(true);
		
		Composite legendEdgesContentComposite = new Composite(legendEdgesScrolledComposite, SWT.NONE);
		legendEdgesContentComposite.setLayout(new GridLayout(1, false));
		legendEdgesScrolledComposite.setContent(legendEdgesContentComposite);
		legendEdgesScrolledComposite.setMinSize(legendEdgesContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		codePainterLegendSashForm.setWeights(new int[] {1, 1});

		// set the default tab
		folder.setSelection(codePainterSelectionTab);
		
		// add index listeners to disable UI when index is changing
		final Display display = parent.getShell().getDisplay();
		
		IndexingUtil.addListener(new IIndexListener(){
			@Override
			public void indexOperationCancelled(IndexOperation op) {}

			@Override
			public void indexOperationComplete(IndexOperation op) {
				display.syncExec(new Runnable(){
					@Override
					public void run() {
						folder.setSelection(codePainterSelectionTab);
						searchCodePaintersCheckbox.setEnabled(true);
						categorizedCodePaintersTree.setEnabled(true);
					}
				});
			}

			@Override
			public void indexOperationError(IndexOperation op, Throwable error) {}

			@Override
			public void indexOperationScheduled(IndexOperation op) {}

			@Override
			public void indexOperationStarted(IndexOperation op) {
				display.syncExec(new Runnable(){
					@Override
					public void run() {
						folder.setSelection(codePainterSelectionTab);
						searchCodePaintersCheckbox.setSelection(false);
						searchCodePaintersCheckbox.setEnabled(false);
						selectedCodePainterCombo.setEnabled(false);
						categorizedCodePaintersTree.setEnabled(false);
					}
				});
			}
		});

		// register code painter smart view listeners
		CodePainterSmartView.addListener(new CodePainterSmartViewEventListener(){
			@Override
			public void selectionChanged(IAtlasSelectionEvent event, int reverse, int forward) {
				display.asyncExec(new Runnable(){
					@Override
					public void run() {
						updateLegend(legendNodesScrolledComposite, legendEdgesScrolledComposite);
					}
				});
			}

			@Override
			public void codePainterChanged(CodePainter codePainter) {
				display.asyncExec(new Runnable(){
					@Override
					public void run() {
						updateLegend(legendNodesScrolledComposite, legendEdgesScrolledComposite);
					}
				});
			}
		});
		
		// register as a graph selection provider
		registerGraphSelectionProvider();
	}

	private void updateLegend(ScrolledComposite legendNodesScrolledComposite, ScrolledComposite legendEdgesScrolledComposite) {
		Composite legendNodesContentComposite = new Composite(legendNodesScrolledComposite, SWT.NONE);
		legendNodesContentComposite.setLayout(new GridLayout(1, false));
		
		// colors must be unique, names should be, but may not be unique
		// sort first by name then color
		Comparator<Entry<Color,String>> legendOrdering = new Comparator<Entry<Color,String>>() {
			@Override
			public int compare(Entry<Color,String> e1, Entry<Color,String> e2) {
				String n1 = e1.getValue();
				String n2 = e2.getValue();
				int nameComparison = n1.compareTo(n2);
				if(nameComparison == 0){
					Color c1 = e1.getKey();
					Color c2 = e2.getKey();
					return Integer.compare((c1.getRed() + c1.getGreen() + c1.getBlue()), 
							(c2.getRed() + c2.getGreen() + c2.getBlue()));
				} else {
					return nameComparison;
				}
			}
		};
		
		ColorPalette activeColorPalette = ColorPalette.getEmptyColorPalette();
		CodePainter activeCodePainter = CodePainterSmartView.getCodePainter();
		if(activeCodePainter != null){
			activeColorPalette = activeCodePainter.getActiveColorPalette();
		}
		
		// sort node colors for consistency and add to panel
		List<Entry<Color,String>> nodeLegendEntries = new ArrayList<Entry<Color,String>>(activeColorPalette.getNodeColorLegend().entrySet());
		Collections.sort(nodeLegendEntries, legendOrdering);
		boolean isFirstNode = true;
		for(Entry<Color,String> legendEntry : nodeLegendEntries){
			Color legendColor = legendEntry.getKey();
			String legendName = legendEntry.getValue();
			
			if(!isFirstNode){
				Label nodesSeparator = new Label(legendNodesContentComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
				nodesSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			} else {
				isFirstNode = false;
			}
			
			Composite legendNodesColorComposite = new Composite(legendNodesContentComposite, SWT.NONE);
			legendNodesColorComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			legendNodesColorComposite.setLayout(new GridLayout(2, false));
			
			Composite nodesColorComposite = new Composite(legendNodesColorComposite, SWT.BORDER);
			nodesColorComposite.setBackground(SWTResourceManager.getColor(legendColor.getRed(), legendColor.getGreen(), legendColor.getBlue()));
			GridData gd_nodesColorComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_nodesColorComposite.widthHint = 20;
			gd_nodesColorComposite.heightHint = 20;
			nodesColorComposite.setLayoutData(gd_nodesColorComposite);
			
			Label nodesColorLabel = new Label(legendNodesColorComposite, SWT.NONE);
			nodesColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			String text = legendName;
			nodesColorLabel.setText(text != null ? text : "");
			
			nodesColorComposite.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					// TODO: implement
				}
			});
		}
		legendNodesScrolledComposite.setContent(legendNodesContentComposite);
		legendNodesScrolledComposite.setMinSize(legendNodesContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		
		Composite legendEdgesContentComposite = new Composite(legendEdgesScrolledComposite, SWT.NONE);
		legendEdgesContentComposite.setLayout(new GridLayout(1, false));
		
		// sort edge colors for consistency and add to panel
		List<Entry<Color,String>> edgeLegendEntries = new ArrayList<Entry<Color,String>>(activeColorPalette.getEdgeColorLegend().entrySet());
		Collections.sort(edgeLegendEntries, legendOrdering);
		boolean isFirstEdge = true;
		for(Entry<Color,String> legendEntry : edgeLegendEntries){
			Color legendColor = legendEntry.getKey();
			String legendName = legendEntry.getValue();
			
			if(!isFirstEdge){
				Label edgesSeparator = new Label(legendEdgesContentComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
				edgesSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			} else {
				isFirstEdge = false;
			}
			
			Composite legendEdgesColorComposite = new Composite(legendEdgesContentComposite, SWT.NONE);
			legendEdgesColorComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			legendEdgesColorComposite.setLayout(new GridLayout(2, false));
			
			Composite edgesColorComposite = new Composite(legendEdgesColorComposite, SWT.BORDER);
			edgesColorComposite.setBackground(SWTResourceManager.getColor(legendColor.getRed(), legendColor.getGreen(), legendColor.getBlue()));
			GridData gd_edgesColorComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_edgesColorComposite.widthHint = 20;
			gd_edgesColorComposite.heightHint = 20;
			edgesColorComposite.setLayoutData(gd_edgesColorComposite);
			
			Label edgesColorLabel = new Label(legendEdgesColorComposite, SWT.NONE);
			edgesColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			String text = legendName;
			edgesColorLabel.setText(text != null ? text : "");
			
			edgesColorComposite.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					// TODO: implement
				}
			});
		}
		legendEdgesScrolledComposite.setContent(legendEdgesContentComposite);
		legendEdgesScrolledComposite.setMinSize(legendEdgesContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void setFocus() {}
}
