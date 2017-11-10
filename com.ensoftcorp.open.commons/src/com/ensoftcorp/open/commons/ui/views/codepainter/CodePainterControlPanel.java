package com.ensoftcorp.open.commons.ui.views.codepainter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ExpandAdapter;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.atlas.core.indexing.IIndexListener;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.codepainter.CodePainter;
import com.ensoftcorp.open.commons.codepainter.CodePainters;
import com.ensoftcorp.open.commons.codepainter.ColorPalette;
import com.ensoftcorp.open.commons.codepainter.ColorPalettes;
import com.ensoftcorp.open.commons.ui.views.smart.CodePainterSmartView;
import com.ensoftcorp.open.commons.ui.views.smart.CodePainterSmartView.CodePainterSmartViewEventListener;
import com.ensoftcorp.open.commons.utilities.selection.GraphSelectionProviderView;

public class CodePainterControlPanel extends GraphSelectionProviderView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.codepainter.controlpanel"; //$NON-NLS-1$
	
	private static final int FONT_SIZE = 11;
	private static final int MAX_COLOR_PALETTE_TAB_FOLDER_HEIGHT = 150;
	private static final org.eclipse.swt.graphics.Color DEFAULT_FOLDER_TAB_COLOR = SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT);

	// just using this to reduce the number of warnings about unsupported configuration options
	// one warning per instance is enough
	@SuppressWarnings("rawtypes")
	private static Map<String,Set<Class>> unsupportedConfigurationsWarnings = new HashMap<String,Set<Class>>();
	
	private Display display;
	private CTabFolder folder;
	private Combo availableColorPalettesCombo;
	private StyledText codePainterDetailsText;
	private Label codePainterConfigurationErrorLabel;
	private Button addColorPaletteLayerButton;
	private ScrolledComposite colorPaletteLayersScrolledComposite;
	private ScrolledComposite legendNodesScrolledComposite;
	private ScrolledComposite legendEdgesScrolledComposite;
	private ScrolledComposite codePainterConfigurationsScrolledComposite;

	// colors must be unique, names should be, but may not be unique
	// sort first by name then color
	private Comparator<Entry<Color,String>> legendOrdering = new Comparator<Entry<Color,String>>() {
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
	
	public CodePainterControlPanel(){
		setPartName("Code Painter Control Panel");
		setTitleImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/brush.gif"));
		CodePainters.loadCodePainterContributions();
		ColorPalettes.loadColorPaletteContributions();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		display = parent.getShell().getDisplay();
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
		
		final CTabItem codePainterDetailsTab = new CTabItem(folder, SWT.NONE);
		codePainterDetailsTab.setText("Details");
		Composite codePainterDetailsComposite = new Composite(folder, SWT.NONE);
		codePainterDetailsTab.setControl(codePainterDetailsComposite);
		codePainterDetailsComposite.setLayout(new GridLayout(1, false));

		final CTabItem codePainterConfigurationsTab = new CTabItem(folder, SWT.NONE);
		codePainterConfigurationsTab.setText("Configurations");
		Composite codePainterConfigurationComposite = new Composite(folder, SWT.NONE);
		codePainterConfigurationsTab.setControl(codePainterConfigurationComposite);
		codePainterConfigurationComposite.setLayout(new GridLayout(1, false));
		
		codePainterConfigurationsScrolledComposite = new ScrolledComposite(codePainterConfigurationComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		codePainterConfigurationsScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		codePainterConfigurationsScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		codePainterConfigurationsScrolledComposite.setExpandHorizontal(true);
		codePainterConfigurationsScrolledComposite.setExpandVertical(true);
		
		refreshCodePainterConfigurations();
		
		Composite codePainterConfigurationsControlComposite = new Composite(codePainterConfigurationComposite, SWT.NONE);
		codePainterConfigurationsControlComposite.setLayout(new GridLayout(2, false));
		codePainterConfigurationsControlComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		codePainterConfigurationErrorLabel = new Label(codePainterConfigurationsControlComposite, SWT.NONE);
		codePainterConfigurationErrorLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
		codePainterConfigurationErrorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button restoreDefaultConfigurationsButton = new Button(codePainterConfigurationsControlComposite, SWT.NONE);
		restoreDefaultConfigurationsButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		restoreDefaultConfigurationsButton.setText("Restore Defaults");
		
		final CTabItem codePainterColorPalettesTab = new CTabItem(folder, SWT.NONE);
		codePainterColorPalettesTab.setText("Color Palettes");
		Composite codePainterColorPalettesComposite = new Composite(folder, SWT.NONE);
		codePainterColorPalettesTab.setControl(codePainterColorPalettesComposite);
		codePainterColorPalettesComposite.setLayout(new GridLayout(1, false));
		
		final CTabItem codePainterLegendTab = new CTabItem(folder, SWT.NONE);
		codePainterLegendTab.setText("Legend");
		Composite codePainterLegendComposite = new Composite(folder, SWT.NONE);
		codePainterLegendTab.setControl(codePainterLegendComposite);
		codePainterLegendComposite.setLayout(new GridLayout(1, false));
		
		restoreDefaultConfigurationsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CodePainter activeCodePainter = CodePainterSmartView.getCodePainter();
				if(activeCodePainter != null){
					activeCodePainter.restoreDefaultConfigurations();
					refreshCodePainterConfigurations();
					refreshSelection();
				}
			}
		});
		
		// setup code painter details tab
		codePainterDetailsText = new StyledText(codePainterDetailsComposite, SWT.READ_ONLY | SWT.WRAP);
		codePainterDetailsText.setMargins(5, 5, 5, 5);
		codePainterDetailsText.setEditable(false);
		codePainterDetailsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		refreshCodePainterDetails();
		
		// setup code painters selection tab
		Composite searchCodePaintersComposite = new Composite(codePainterSelectionComposite, SWT.NONE);
		searchCodePaintersComposite.setLayout(new GridLayout(2, false));
		searchCodePaintersComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
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
							selectedCodePainterCombo.setText(codePainter.getName());
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
					return cp1.getName().compareTo(cp2.getName());
				} else {
					return categoryComparison;
				}
			}
		});
		for(CodePainter codePainter : codePainters){
			selectedCodePainterCombo.add(codePainter.getName());
			selectedCodePainterCombo.setData(codePainter.getName(), codePainter);
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
			codePainterItem.setText(codePainter.getName());
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
		
		Group colorPalettesGroup = new Group(codePainterColorPalettesComposite, SWT.NONE);
		colorPalettesGroup.setText("Available Color Palettes");
		colorPalettesGroup.setLayout(new GridLayout(3, false));
		colorPalettesGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		availableColorPalettesCombo = new Combo(colorPalettesGroup, SWT.READ_ONLY);
		availableColorPalettesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		addColorPaletteLayerButton = new Button(colorPalettesGroup, SWT.NONE);
		addColorPaletteLayerButton.setText("Add Layer");
		addColorPaletteLayerButton.setEnabled(false);
		
		Group colorPaletteLayersGroup = new Group(codePainterColorPalettesComposite, SWT.NONE);
		colorPaletteLayersGroup.setText("Applied Color Palette Layers");
		colorPaletteLayersGroup.setLayout(new GridLayout(1, false));
		colorPaletteLayersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		colorPaletteLayersScrolledComposite = new ScrolledComposite(colorPaletteLayersGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		colorPaletteLayersScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		colorPaletteLayersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		colorPaletteLayersScrolledComposite.setExpandHorizontal(true);
		colorPaletteLayersScrolledComposite.setExpandVertical(true);
		
		addColorPaletteLayerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ColorPalette selectedColorPalette = (ColorPalette) availableColorPalettesCombo.getData(availableColorPalettesCombo.getText());
				if(selectedColorPalette != null){
					CodePainter activeCodePainter = CodePainterSmartView.getCodePainter();
					if(activeCodePainter != null){
						activeCodePainter.addColorPalette(selectedColorPalette);
						colorPaletteStates.add(new ColorPaletteState(selectedColorPalette));
					}
					addColorPaletteLayerButton.setEnabled(false);
					refreshColorPaletteLayers();
					refreshSelection();
				}
			}
		});
		
		refreshColorPaletteLayers();
		
		Button restoreDefaultColorPalettesButton = new Button(colorPalettesGroup, SWT.NONE);
		restoreDefaultColorPalettesButton.setText("Restore Defaults");
		
		restoreDefaultColorPalettesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CodePainter activeCodePainter = CodePainterSmartView.getCodePainter();
				if(activeCodePainter != null){
					activeCodePainter.restoreDefaultColorPalettes();
					initializeColorPaletteState();
					refreshColorPaletteLayers();
					refreshSelection();
				}
			}
		});
		
		// initialize the empty legend
		Composite conflictResolutionStrategyComposite = new Composite(codePainterLegendComposite, SWT.NONE);
		conflictResolutionStrategyComposite.setLayout(new GridLayout(2, false));
		conflictResolutionStrategyComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label conflictResolutionLabel = new Label(conflictResolutionStrategyComposite, SWT.NONE);
		conflictResolutionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		conflictResolutionLabel.setText("Color Conflict Resolution Strategy: ");
		
		Combo conflictResolutionStrategyCombo = new Combo(conflictResolutionStrategyComposite, SWT.READ_ONLY);
		conflictResolutionStrategyCombo.setItems(new String[] {"First Match", "Last Match", "Mix Colors"});
		conflictResolutionStrategyCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		conflictResolutionStrategyCombo.select(0);
		
		SashForm codePainterLegendSashForm = new SashForm(codePainterLegendComposite, SWT.NONE);
		codePainterLegendSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group legendNodesGroup = new Group(codePainterLegendSashForm, SWT.NONE);
		legendNodesGroup.setText("Nodes");
		legendNodesGroup.setLayout(new GridLayout(1, false));
		
		legendNodesScrolledComposite = new ScrolledComposite(legendNodesGroup, SWT.H_SCROLL | SWT.V_SCROLL);
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
		
		legendEdgesScrolledComposite = new ScrolledComposite(legendEdgesGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		legendEdgesScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		legendEdgesScrolledComposite.setExpandHorizontal(true);
		legendEdgesScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		legendEdgesScrolledComposite.setExpandVertical(true);
		
		Composite legendEdgesContentComposite = new Composite(legendEdgesScrolledComposite, SWT.NONE);
		legendEdgesContentComposite.setLayout(new GridLayout(1, false));
		legendEdgesScrolledComposite.setContent(legendEdgesContentComposite);
		legendEdgesScrolledComposite.setMinSize(legendEdgesContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		codePainterLegendSashForm.setWeights(new int[] {1, 1});
		
		conflictResolutionStrategyCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CodePainter activeCodePainter = CodePainterSmartView.getCodePainter();
				if(activeCodePainter != null){
					activeCodePainter.setColorPaletteConflictStrategy(CodePainter.ColorPaletteConflictStrategy.values()[conflictResolutionStrategyCombo.getSelectionIndex()]);
					refreshLegend();
				}
			}
		});

		// set the default tab
		folder.setSelection(codePainterSelectionTab);
		
		if(indexExists){
			enableFolder(folder);
		} else {
			disableFolder(folder);
		}
		
		// add index listeners to disable UI when index is changing
		IndexingUtil.addListener(new IIndexListener(){
			@Override
			public void indexOperationCancelled(IndexOperation op) {}

			@Override
			public void indexOperationComplete(IndexOperation op) {
				display.syncExec(new Runnable(){
					@Override
					public void run() {
						searchCodePaintersCheckbox.setEnabled(true);
						categorizedCodePaintersTree.setEnabled(true);
						enableFolder(folder);
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
						disableFolder(folder);
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
				// ignoring selection change reported by smart view, we will
				// monitor selection changes directly from the event stream
				// since smart view may not be open
			}

			@Override
			public void codePainterChanged(CodePainter codePainter) {
				display.asyncExec(new Runnable(){
					@Override
					public void run() {
						// reset the code painter layer state
						refreshCodePainterDetails();
						refreshCodePainterConfigurations();
						initializeColorPaletteState();
						refreshColorPaletteLayers();
						refreshLegend();
					}
				});
			}
		});
		
		// register as a graph selection provider
		registerGraphSelectionProvider();
	}
	
	private void enableFolder(CTabFolder folder) {
		Control[] tabs = folder.getTabList();
		for(Control tab : tabs){
			tab.setBackground(DEFAULT_FOLDER_TAB_COLOR);
		}
		folder.setEnabled(true);
	}
	
	private void disableFolder(CTabFolder folder) {
		folder.setEnabled(false);
		Control[] tabs = folder.getTabList();
		for(Control tab : tabs){
			tab.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		}
	}

	@Override
	public void selectionChanged() {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				refreshColorPaletteLayers();
				refreshLegend();
			}
		});
	}
	
	private void refreshCodePainterConfigurations() {
		CodePainter activeCodePainter = CodePainterSmartView.getCodePainter();
		
		if(activeCodePainter != null){
			if (activeCodePainter.getPossibleParameters().isEmpty()) {
				StyledText message = new StyledText(codePainterConfigurationsScrolledComposite, SWT.READ_ONLY | SWT.WRAP);
				message.setMargins(5, 5, 5, 5);
				message.setEditable(false);
				message.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				message.setText("No parameters available for this code painter.");
				codePainterConfigurationsScrolledComposite.setContent(message);
				codePainterConfigurationsScrolledComposite.setMinSize(message.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			} else {
				Composite inputComposite = new Composite(codePainterConfigurationsScrolledComposite, SWT.NONE);
				inputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				inputComposite.setLayout(new GridLayout(1, false));
				
				// add the parameters in alphabetical order for UI consistency (flags are ordered first)
				LinkedList<String> parameterNames = new LinkedList<String>(activeCodePainter.getPossibleParameters().keySet());
				Collections.sort(parameterNames, new Comparator<String>(){
					@Override
					public int compare(String p1, String p2) {
						boolean p1Flag = activeCodePainter.getPossibleFlags().contains(p1);
						boolean p2Flag = activeCodePainter.getPossibleFlags().contains(p2);
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
					final Class<? extends Object> parameterType = activeCodePainter.getPossibleParameters().get(parameterName);
					
					if(parameterType == Boolean.class){
						Composite booleanInputComposite = new Composite(inputComposite, SWT.NONE);
						booleanInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						booleanInputComposite.setLayout(new GridLayout(3, false));
						booleanInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));

						if(activeCodePainter.getPossibleFlags().contains(parameterName)){
							final Button enableFlagCheckbox = new Button(booleanInputComposite, SWT.CHECK);
							enableFlagCheckbox.setToolTipText(activeCodePainter.getParameterDescription(parameterName));
							enableFlagCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
							
							final Label flagLabel = new Label(booleanInputComposite, SWT.NONE);
							flagLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
							
							flagLabel.setText(parameterName);
							flagLabel.setToolTipText(activeCodePainter.getParameterDescription(parameterName));
							enableFlagCheckbox.setSelection(activeCodePainter.isFlagSet(parameterName));
							
							enableFlagCheckbox.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									boolean enabled = enableFlagCheckbox.getSelection();
									try {
										if(enabled){
											activeCodePainter.setFlag(parameterName);
										} else {
											activeCodePainter.unsetFlag(parameterName);
										}
										clearCodePainterValidationErrorMessage();
										refreshSelection();
									} catch (Throwable t){
										setCodePainterValidationErrorMessage(t.getMessage());
									}
								}
							});
						} else {
							final Label booleanInputLabel = new Label(booleanInputComposite, SWT.NONE);
							booleanInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
							
							booleanInputLabel.setText(parameterName + ":");
							booleanInputLabel.setToolTipText(activeCodePainter.getParameterDescription(parameterName));
							
							final Button booleanInputCheckbox = new Button(booleanInputComposite, SWT.CHECK);
							booleanInputCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							booleanInputCheckbox.setSelection((Boolean) activeCodePainter.getParameterValue(parameterName));
							
							booleanInputCheckbox.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									try {
										clearCodePainterValidationErrorMessage();
										activeCodePainter.setParameterValue(parameterName, booleanInputCheckbox.getSelection());
										refreshSelection();
									} catch (Throwable t){
										setCodePainterValidationErrorMessage(t.getMessage());
									}
								}
							});
						}
					} else if(parameterType == String.class){
						Composite stringInputComposite = new Composite(inputComposite, SWT.NONE);
						stringInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						stringInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						stringInputComposite.setLayout(new GridLayout(3, false));

						final Label stringInputLabel = new Label(stringInputComposite, SWT.NONE);
						stringInputLabel.setText(parameterName + ":");
						stringInputLabel.setToolTipText(activeCodePainter.getParameterDescription(parameterName));
						stringInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));

						final Text stringInputText = new Text(stringInputComposite, SWT.BORDER);
						stringInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						stringInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						stringInputText.setText((String) activeCodePainter.getParameterValue(parameterName));

						Button applyConfigurationButton = new Button(stringInputComposite, SWT.NONE);
						applyConfigurationButton.setText("Apply");
						applyConfigurationButton.setEnabled(false);
						applyConfigurationButton.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									applyConfigurationButton.setEnabled(false);
									activeCodePainter.setParameterValue(parameterName, stringInputText.getText());
									clearCodePainterValidationErrorMessage();
									refreshSelection();
								} catch (Throwable t){
									setCodePainterValidationErrorMessage(t.getMessage());
								}
							}
						});
						
						stringInputText.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								String text = stringInputText.getText();
								if(!text.equals("")){
									clearCodePainterValidationErrorMessage();
									applyConfigurationButton.setEnabled(true);
								} else {
									applyConfigurationButton.setEnabled(false);
									setCodePainterValidationErrorMessage(parameterName + " must be an non-empty string.");
								}
							}
						});
					} else if(parameterType == Integer.class){
						Composite integerInputComposite = new Composite(inputComposite, SWT.NONE);
						integerInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						integerInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						integerInputComposite.setLayout(new GridLayout(3, false));
						
						final Label integerInputLabel = new Label(integerInputComposite, SWT.NONE);
						integerInputLabel.setText(parameterName + ":");
						integerInputLabel.setToolTipText(activeCodePainter.getParameterDescription(parameterName));
						integerInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						final Text integerInputText = new Text(integerInputComposite, SWT.BORDER);
						integerInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						integerInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						integerInputText.setText(((Integer) activeCodePainter.getParameterValue(parameterName)).toString());

						Button applyConfigurationButton = new Button(integerInputComposite, SWT.NONE);
						applyConfigurationButton.setText("Apply");
						applyConfigurationButton.setEnabled(false);
						applyConfigurationButton.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									applyConfigurationButton.setEnabled(false);
									Integer value = Integer.parseInt(integerInputText.getText());
									activeCodePainter.setParameterValue(parameterName, value);
									clearCodePainterValidationErrorMessage();
									refreshSelection();
								} catch (Throwable t){
									setCodePainterValidationErrorMessage(t.getMessage());
								}
							}
						});
						
						integerInputText.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								try {
									Integer.parseInt(integerInputText.getText());
									clearCodePainterValidationErrorMessage();
									applyConfigurationButton.setEnabled(true);
								} catch (NumberFormatException ex){
									applyConfigurationButton.setEnabled(false);
									setCodePainterValidationErrorMessage(parameterName + " must be an integer.");
								}
							}
						});
					} else if(parameterType == Double.class){
						Composite doubleInputComposite = new Composite(inputComposite, SWT.NONE);
						doubleInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						doubleInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						doubleInputComposite.setLayout(new GridLayout(3, false));
						
						final Label doubleInputLabel = new Label(doubleInputComposite, SWT.NONE);
						doubleInputLabel.setText(parameterName + ":");
						doubleInputLabel.setToolTipText(activeCodePainter.getParameterDescription(parameterName));
						doubleInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						final Text doubleInputText = new Text(doubleInputComposite, SWT.BORDER);
						doubleInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						doubleInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						doubleInputText.setText(((Double) activeCodePainter.getParameterValue(parameterName)).toString());

						Button applyConfigurationButton = new Button(doubleInputComposite, SWT.NONE);
						applyConfigurationButton.setText("Apply");
						applyConfigurationButton.setEnabled(false);
						applyConfigurationButton.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									applyConfigurationButton.setEnabled(false);
									Double value;
									try {
										value = (double) Integer.parseInt(doubleInputText.getText());
									} catch (NumberFormatException ex){
										value = Double.parseDouble(doubleInputText.getText());
									}
									activeCodePainter.setParameterValue(parameterName, value);
									clearCodePainterValidationErrorMessage();
									refreshSelection();
								} catch (Throwable t){
									setCodePainterValidationErrorMessage(t.getMessage());
								}
							}
						});
						
						doubleInputText.addKeyListener(new KeyAdapter() {
							@Override
							public void keyReleased(KeyEvent e) {
								try {
									try {
										Integer.parseInt(doubleInputText.getText());
									} catch (NumberFormatException ex){
										Double.parseDouble(doubleInputText.getText());
									}
									clearCodePainterValidationErrorMessage();
									applyConfigurationButton.setEnabled(true);
								} catch (NumberFormatException ex){
									applyConfigurationButton.setEnabled(false);
									setCodePainterValidationErrorMessage(parameterName + " must be a double.");
								}
							}
						});
					} else if(parameterType == Color.class){
						Composite colorInputComposite = new Composite(inputComposite, SWT.NONE);
						colorInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
						colorInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
						colorInputComposite.setLayout(new GridLayout(3, false));

						Color currentColor = (Color) activeCodePainter.getParameterValue(parameterName);
						
						Composite colorComposite = new Composite(colorInputComposite, SWT.BORDER);
						colorComposite.setBackground(SWTResourceManager.getColor(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue()));
						GridData colorCompositeSizeHint = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
						colorCompositeSizeHint.widthHint = 20;
						colorCompositeSizeHint.heightHint = 20;
						colorComposite.setLayoutData(colorCompositeSizeHint);
						
						final Label colorInputLabel = new Label(colorInputComposite, SWT.NONE);
						colorInputLabel.setText(parameterName);
						colorInputLabel.setToolTipText(activeCodePainter.getParameterDescription(parameterName));
						colorInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
						
						colorComposite.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseUp(MouseEvent e) {
								ColorDialog colorPickerDialog = new ColorDialog(display.getActiveShell());

						        // set the current color
								colorPickerDialog.setRGB(new RGB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue()));

						        // set the title
								colorPickerDialog.setText("Choose a Color");

						        // open the dialog and retrieve the selected color
								RGB rgb = colorPickerDialog.open();
								if (rgb != null) {
									Color newColor = new Color(rgb.red, rgb.green, rgb.blue);
									if(!currentColor.equals(newColor)){
										colorComposite.setBackground(SWTResourceManager.getColor(newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
										activeCodePainter.setParameterValue(parameterName, newColor);
										refreshSelection();
									}
								}
							}
						});
					} else {
						if(unsupportedConfigurationsWarnings.containsKey(activeCodePainter.getName())){
							if(!unsupportedConfigurationsWarnings.get(activeCodePainter.getName()).contains(parameterType)){
								unsupportedConfigurationsWarnings.get(activeCodePainter.getName()).add(parameterType);
								Log.warning(activeCodePainter.getName() + " code painter has unsupported parameter type " + parameterType.getName() + " !");
							}
						} else {
							@SuppressWarnings("rawtypes")
							Set<Class> unsupportedTypes = new HashSet<Class>();
							unsupportedTypes.add(parameterType);
							unsupportedConfigurationsWarnings.put(activeCodePainter.getName(), unsupportedTypes);
							Log.warning(activeCodePainter.getName() + " code painter has unsupported parameter type " + parameterType.getName() + " !");
						}
					}
				}
				
				codePainterConfigurationsScrolledComposite.setContent(inputComposite);
				codePainterConfigurationsScrolledComposite.setMinSize(inputComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		} else {
			StyledText message = new StyledText(codePainterConfigurationsScrolledComposite, SWT.READ_ONLY | SWT.WRAP);
			message.setMargins(5, 5, 5, 5);
			message.setEditable(false);
			message.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			message.setText("No code painter selected.");
			codePainterConfigurationsScrolledComposite.setContent(message);
			codePainterConfigurationsScrolledComposite.setMinSize(message.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
	}
	
	private void clearCodePainterValidationErrorMessage(){
		codePainterConfigurationErrorLabel.setText("");
	}
	
	private void setCodePainterValidationErrorMessage(String message){
		if(message != null){
			codePainterConfigurationErrorLabel.setText(message);
		}
	}
		
	private void refreshCodePainterDetails() {
		CodePainter activeCodePainter = CodePainterSmartView.getCodePainter();
		if(activeCodePainter == null){
			codePainterDetailsText.setText("No code painter selected.");
		} else {
			StringBuilder details = new StringBuilder();
			details.append(activeCodePainter.getName() + " - " + activeCodePainter.getDescription() + "\n\n");

			details.append("Supported Selections: \n");
			String[] supportedNodes = activeCodePainter.getSupportedNodeTags();
			if(supportedNodes == null){
				details.append("Node Type: Node selections are ignored.\n");
			} else if(supportedNodes.length == 0){
				details.append("Node Types: Responds to any node selection.\n");
			} else {
				details.append("Node Types: " + Arrays.toString(supportedNodes) + "\n");
			}
			String[] supportedEdges = activeCodePainter.getSupportedEdgeTags();
			if(supportedEdges == null){
				details.append("Edge Types: Edge selections are ignored.\n");
			} else if(supportedEdges.length == 0){
				details.append("Edge Types: Responds to any edge selection.\n");
			} else {
				details.append("Edge Types: " + Arrays.toString(supportedEdges) + "\n");
			}
			
			codePainterDetailsText.setText(details.toString().trim());
		}
	}

	/*
	 * Small helper class to visually track the state of the color palette layers
	 */
	private static class ColorPaletteState {
		private ColorPalette colorPalette;
		private boolean expanded;
		private int selectedTab;
		
		public ColorPaletteState(ColorPalette colorPalette) {
			this.colorPalette = colorPalette;
			this.expanded = false;
			this.selectedTab = 0;
		}
		
		public void setSelectedTab(int selectedTab){
			this.selectedTab = selectedTab;
		}
		
		public int getSelectedTab(){
			return selectedTab;
		}

		public boolean isExpanded() {
			return expanded;
		}
		
		public void setExpanded(boolean expanded) {
			this.expanded = expanded;
		}
		
		public ColorPalette getColorPalette() {
			return colorPalette;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((colorPalette == null) ? 0 : colorPalette.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ColorPaletteState other = (ColorPaletteState) obj;
			if (colorPalette == null) {
				if (other.colorPalette != null)
					return false;
			} else if (!colorPalette.equals(other.colorPalette))
				return false;
			return true;
		}
	}
	
	// current color palette states (and ordering, 0 index is first layer)
	private List<ColorPaletteState> colorPaletteStates = new LinkedList<ColorPaletteState>();
	
	private void initializeColorPaletteState() {
		colorPaletteStates.clear();
		CodePainter activeCodePainter = CodePainterSmartView.getCodePainter();
		if(activeCodePainter != null){
			for(ColorPalette colorPalette : activeCodePainter.getColorPalettes()){
				colorPaletteStates.add(new ColorPaletteState(colorPalette));
			}
		}
	}
	
	private void refreshColorPaletteLayers() {
		// reset the view
		colorPaletteLayersScrolledComposite.setEnabled(false);
		
		// save the old scroll position and content origin
		int scrollPosition = colorPaletteLayersScrolledComposite.getVerticalBar().getSelection();
		org.eclipse.swt.graphics.Point origin = colorPaletteLayersScrolledComposite.getOrigin();
		
		Composite colorPaletteLayersContentComposite = new Composite(colorPaletteLayersScrolledComposite, SWT.NONE);
		colorPaletteLayersContentComposite.setLayout(new GridLayout(1, false));
		availableColorPalettesCombo.removeAll();
		addColorPaletteLayerButton.setEnabled(false);
		
		// get the active code painter
		CodePainter activeCodePainter = CodePainterSmartView.getCodePainter();
		if(activeCodePainter != null){
		
			// populate the available color palettes that are not already applied
			if(activeCodePainter != null){
				for(ColorPalette colorPalette : ColorPalettes.getRegisteredColorPalettes()){
					if(!activeCodePainter.getColorPalettes().contains(colorPalette)){
						availableColorPalettesCombo.add(colorPalette.getName());
						availableColorPalettesCombo.setData(colorPalette.getName(), colorPalette);
					}
				}
			}
			
			availableColorPalettesCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ColorPalette selectedColorPalette = (ColorPalette) availableColorPalettesCombo.getData(availableColorPalettesCombo.getText());
					addColorPaletteLayerButton.setEnabled(selectedColorPalette != null);
				}
			});
			
			for(ColorPaletteState colorPaletteState : colorPaletteStates){
				ColorPalette colorPalette = colorPaletteState.getColorPalette();
				ExpandBar colorPaletteExpandBar = new ExpandBar(colorPaletteLayersContentComposite, SWT.NONE);
				colorPaletteExpandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				
				ExpandItem colorPaletteExpandItem = new ExpandItem(colorPaletteExpandBar, SWT.NONE);
				colorPaletteExpandItem.setExpanded(colorPaletteState.isExpanded());
				if(activeCodePainter.isColorPaletteEnabled(colorPalette)){
					colorPaletteExpandItem.setText(colorPalette.getName());
				} else {
					colorPaletteExpandItem.setText("[DISABLED] " + colorPalette.getName());
				}
				
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
				descriptionText.setText(colorPalette.getDescription());
				
				Label enabledLabel = new Label(colorPaletteOverviewComposite, SWT.NONE);
				enabledLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				enabledLabel.setText("Enabled: ");
				
				boolean isColorPaletteEnabled = activeCodePainter.isColorPaletteEnabled(colorPalette);
				Button enabledCheckbox = new Button(colorPaletteOverviewComposite, SWT.CHECK);
				enabledCheckbox.setSelection(isColorPaletteEnabled);
	
				CTabFolder colorPaletteTabFolder = new CTabFolder(colorPaletteContentComposite, SWT.NONE);
				colorPaletteTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				colorPaletteTabFolder.setBorderVisible(true);
				colorPaletteTabFolder.setSimple(false); // adds the Eclipse style "swoosh"
				
				colorPaletteTabFolder.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						colorPaletteState.setSelectedTab(colorPaletteTabFolder.getSelectionIndex());
					}
				});
				
				enabledCheckbox.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						boolean enabled = enabledCheckbox.getSelection();
						if(enabled){
							enableFolder(colorPaletteTabFolder);
							activeCodePainter.enableColorPalette(colorPalette);
							colorPaletteExpandItem.setText(colorPalette.getName());
						} else {
							disableFolder(colorPaletteTabFolder);
							activeCodePainter.disableColorPalette(colorPalette);
							colorPaletteExpandItem.setText("[DISABLED] " + colorPalette.getName());
						}
						refreshSelection();
					}
				});
				
				CTabItem colorPaletteConfigurationsTabItem = new CTabItem(colorPaletteTabFolder, SWT.NONE);
				colorPaletteConfigurationsTabItem.setText("Configurations");
				
				Composite colorPaletteConfigurationsComposite = new Composite(colorPaletteTabFolder, SWT.NONE);
				colorPaletteConfigurationsTabItem.setControl(colorPaletteConfigurationsComposite);
				colorPaletteConfigurationsComposite.setLayout(new GridLayout(1, false));
				
				ScrolledComposite colorPaletteConfigurationsScrolledComposite = new ScrolledComposite(colorPaletteConfigurationsComposite, SWT.H_SCROLL | SWT.V_SCROLL);
				colorPaletteConfigurationsScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				colorPaletteConfigurationsScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				colorPaletteConfigurationsScrolledComposite.setExpandHorizontal(true);
				colorPaletteConfigurationsScrolledComposite.setExpandVertical(true);

				Label colorPaletteConfigurationErrorLabel = new Label(colorPaletteConfigurationsComposite, SWT.NONE);
				colorPaletteConfigurationErrorLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
				colorPaletteConfigurationErrorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				
				if (colorPalette.getPossibleParameters().isEmpty()) {
					StyledText message = new StyledText(colorPaletteConfigurationsScrolledComposite, SWT.READ_ONLY | SWT.WRAP);
					message.setMargins(5, 5, 5, 5);
					message.setEditable(false);
					message.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
					message.setText("No parameters available for this color palette.");
					colorPaletteConfigurationsScrolledComposite.setContent(message);
					colorPaletteConfigurationsScrolledComposite.setMinSize(message.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				} else {
					Composite inputComposite = new Composite(colorPaletteConfigurationsScrolledComposite, SWT.NONE);
					inputComposite.setLayout(new GridLayout(1, false));
					inputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
					
					// add the parameters in alphabetical order for UI consistency (flags are ordered first)
					LinkedList<String> parameterNames = new LinkedList<String>(colorPalette.getPossibleParameters().keySet());
					Collections.sort(parameterNames, new Comparator<String>(){
						@Override
						public int compare(String p1, String p2) {
							boolean p1Flag = colorPalette.getPossibleFlags().contains(p1);
							boolean p2Flag = colorPalette.getPossibleFlags().contains(p2);
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
						final Class<? extends Object> parameterType = colorPalette.getPossibleParameters().get(parameterName);
						
						if(parameterType == Boolean.class){
							Composite booleanInputComposite = new Composite(inputComposite, SWT.NONE);
							booleanInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
							booleanInputComposite.setLayout(new GridLayout(3, false));
							booleanInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));

							if(colorPalette.getPossibleFlags().contains(parameterName)){
								final Button enableFlagCheckbox = new Button(booleanInputComposite, SWT.CHECK);
								enableFlagCheckbox.setToolTipText(colorPalette.getParameterDescription(parameterName));
								enableFlagCheckbox.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
								
								final Label flagLabel = new Label(booleanInputComposite, SWT.NONE);
								flagLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
								
								flagLabel.setText(parameterName);
								flagLabel.setToolTipText(colorPalette.getParameterDescription(parameterName));
								enableFlagCheckbox.setSelection(colorPalette.isFlagSet(parameterName));
								
								enableFlagCheckbox.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent e) {
										boolean enabled = enableFlagCheckbox.getSelection();
										try {
											if(enabled){
												colorPalette.setFlag(parameterName);
											} else {
												colorPalette.unsetFlag(parameterName);
											}
											clearColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel);
											refreshSelection();
										} catch (Throwable t){
											setColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel, t.getMessage());
										}
									}
								});
							} else {
								final Label booleanInputLabel = new Label(booleanInputComposite, SWT.NONE);
								booleanInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
								
								booleanInputLabel.setText(parameterName + ":");
								booleanInputLabel.setToolTipText(colorPalette.getParameterDescription(parameterName));
								
								final Button booleanInputCheckbox = new Button(booleanInputComposite, SWT.CHECK);
								booleanInputCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
								booleanInputCheckbox.setSelection((Boolean) colorPalette.getParameterValue(parameterName));
								
								booleanInputCheckbox.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent e) {
										try {
											clearColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel);
											colorPalette.setParameterValue(parameterName, booleanInputCheckbox.getSelection());
											refreshSelection();
										} catch (Throwable t){
											setColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel, t.getMessage());
										}
									}
								});
							}
						} else if(parameterType == String.class){
							Composite stringInputComposite = new Composite(inputComposite, SWT.NONE);
							stringInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
							stringInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							stringInputComposite.setLayout(new GridLayout(3, false));

							final Label stringInputLabel = new Label(stringInputComposite, SWT.NONE);
							stringInputLabel.setText(parameterName + ":");
							stringInputLabel.setToolTipText(colorPalette.getParameterDescription(parameterName));
							stringInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));

							final Text stringInputText = new Text(stringInputComposite, SWT.BORDER);
							stringInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							stringInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
							stringInputText.setText((String) colorPalette.getParameterValue(parameterName));

							Button applyConfigurationButton = new Button(stringInputComposite, SWT.NONE);
							applyConfigurationButton.setText("Apply");
							applyConfigurationButton.setEnabled(false);
							applyConfigurationButton.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									try {
										applyConfigurationButton.setEnabled(false);
										colorPalette.setParameterValue(parameterName, stringInputText.getText());
										clearColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel);
										refreshSelection();
									} catch (Throwable t){
										setColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel, t.getMessage());
									}
								}
							});
							
							stringInputText.addKeyListener(new KeyAdapter() {
								@Override
								public void keyReleased(KeyEvent e) {
									String text = stringInputText.getText();
									if(!text.equals("")){
										clearColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel);
										applyConfigurationButton.setEnabled(true);
									} else {
										applyConfigurationButton.setEnabled(false);
										setColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel, parameterName + " must be an non-empty string.");
									}
								}
							});
						} else if(parameterType == Integer.class){
							Composite integerInputComposite = new Composite(inputComposite, SWT.NONE);
							integerInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
							integerInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							integerInputComposite.setLayout(new GridLayout(3, false));
							
							final Label integerInputLabel = new Label(integerInputComposite, SWT.NONE);
							integerInputLabel.setText(parameterName + ":");
							integerInputLabel.setToolTipText(colorPalette.getParameterDescription(parameterName));
							integerInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
							
							final Text integerInputText = new Text(integerInputComposite, SWT.BORDER);
							integerInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							integerInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
							integerInputText.setText(((Integer) colorPalette.getParameterValue(parameterName)).toString());

							Button applyConfigurationButton = new Button(integerInputComposite, SWT.NONE);
							applyConfigurationButton.setText("Apply");
							applyConfigurationButton.setEnabled(false);
							applyConfigurationButton.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									try {
										applyConfigurationButton.setEnabled(false);
										Integer value = Integer.parseInt(integerInputText.getText());
										colorPalette.setParameterValue(parameterName, value);
										clearColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel);
										refreshSelection();
									} catch (Throwable t){
										setColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel, t.getMessage());
									}
								}
							});
							
							integerInputText.addKeyListener(new KeyAdapter() {
								@Override
								public void keyReleased(KeyEvent e) {
									try {
										Integer.parseInt(integerInputText.getText());
										clearColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel);
										applyConfigurationButton.setEnabled(true);
									} catch (NumberFormatException ex){
										applyConfigurationButton.setEnabled(false);
										setColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel, parameterName + " must be an integer.");
									}
								}
							});
						} else if(parameterType == Double.class){
							Composite integerInputComposite = new Composite(inputComposite, SWT.NONE);
							integerInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
							integerInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							integerInputComposite.setLayout(new GridLayout(3, false));
							
							final Label doubleInputLabel = new Label(integerInputComposite, SWT.NONE);
							doubleInputLabel.setText(parameterName + ":");
							doubleInputLabel.setToolTipText(colorPalette.getParameterDescription(parameterName));
							doubleInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
							
							final Text doubleInputText = new Text(integerInputComposite, SWT.BORDER);
							doubleInputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							doubleInputText.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
							doubleInputText.setText(((Double) colorPalette.getParameterValue(parameterName)).toString());

							Button applyConfigurationButton = new Button(integerInputComposite, SWT.NONE);
							applyConfigurationButton.setText("Apply");
							applyConfigurationButton.setEnabled(false);
							applyConfigurationButton.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									try {
										applyConfigurationButton.setEnabled(false);
										Double value;
										try {
											value = (double) Integer.parseInt(doubleInputText.getText());
										} catch (NumberFormatException ex){
											value = Double.parseDouble(doubleInputText.getText());
										}
										colorPalette.setParameterValue(parameterName, value);
										clearColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel);
										refreshSelection();
									} catch (Throwable t){
										setColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel, t.getMessage());
									}
								}
							});
							
							doubleInputText.addKeyListener(new KeyAdapter() {
								@Override
								public void keyReleased(KeyEvent e) {
									try {
										try {
											Integer.parseInt(doubleInputText.getText());
										} catch (NumberFormatException ex){
											Double.parseDouble(doubleInputText.getText());
										}
										clearColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel);
										applyConfigurationButton.setEnabled(true);
									} catch (NumberFormatException ex){
										applyConfigurationButton.setEnabled(false);
										setColorPaletteValidationErrorMessage(colorPaletteConfigurationErrorLabel, parameterName + " must be a double.");
									}
								}
							});
						} else if(parameterType == Color.class){
							Composite colorInputComposite = new Composite(inputComposite, SWT.NONE);
							colorInputComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
							colorInputComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
							colorInputComposite.setLayout(new GridLayout(3, false));
							
							Color currentColor = (Color) colorPalette.getParameterValue(parameterName);
							
							Composite colorComposite = new Composite(colorInputComposite, SWT.BORDER);
							colorComposite.setBackground(SWTResourceManager.getColor(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue()));
							GridData colorCompositeSizeHint = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
							colorCompositeSizeHint.widthHint = 20;
							colorCompositeSizeHint.heightHint = 20;
							colorComposite.setLayoutData(colorCompositeSizeHint);
							
							final Label colorInputLabel = new Label(colorInputComposite, SWT.NONE);
							colorInputLabel.setText(parameterName);
							colorInputLabel.setToolTipText(colorPalette.getParameterDescription(parameterName));
							colorInputLabel.setFont(SWTResourceManager.getFont(".SF NS Text", FONT_SIZE, SWT.NORMAL));
							
							colorComposite.addMouseListener(new MouseAdapter() {
								@Override
								public void mouseUp(MouseEvent e) {
									ColorDialog colorPickerDialog = new ColorDialog(display.getActiveShell());

							        // set the current color
									colorPickerDialog.setRGB(new RGB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue()));

							        // set the title
									colorPickerDialog.setText("Choose a Color");

							        // open the dialog and retrieve the selected color
									RGB rgb = colorPickerDialog.open();
									if (rgb != null) {
										Color newColor = new Color(rgb.red, rgb.green, rgb.blue);
										if(!currentColor.equals(newColor)){
											colorComposite.setBackground(SWTResourceManager.getColor(newColor.getRed(), newColor.getGreen(), newColor.getBlue()));
											colorPalette.setParameterValue(parameterName, newColor);
											refreshSelection();
										}
									}
								}
							});
						} else {
							if(unsupportedConfigurationsWarnings.containsKey(colorPalette.getName())){
								if(!unsupportedConfigurationsWarnings.get(colorPalette.getName()).contains(parameterType)){
									unsupportedConfigurationsWarnings.get(colorPalette.getName()).add(parameterType);
									Log.warning(colorPalette.getName() + " color palette has unsupported parameter type " + parameterType.getName() + " !");
								}
							} else {
								@SuppressWarnings("rawtypes")
								Set<Class> unsupportedTypes = new HashSet<Class>();
								unsupportedTypes.add(parameterType);
								unsupportedConfigurationsWarnings.put(colorPalette.getName(), unsupportedTypes);
								Log.warning(colorPalette.getName() + " color palette has unsupported parameter type " + parameterType.getName() + " !");
							}
						}
					}
					
					Point size = inputComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					GridData sizeHint = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
					sizeHint.heightHint = size.y > MAX_COLOR_PALETTE_TAB_FOLDER_HEIGHT ? MAX_COLOR_PALETTE_TAB_FOLDER_HEIGHT : size.y;
					inputComposite.setLayoutData(sizeHint);
					
					colorPaletteConfigurationsScrolledComposite.setContent(inputComposite);
					colorPaletteConfigurationsScrolledComposite.setMinSize(inputComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
				
				CTabItem colorPaletteColoringTabItem = new CTabItem(colorPaletteTabFolder, SWT.NONE);
				colorPaletteColoringTabItem.setText("Coloring");
				
				Composite colorPaletteColoringComposite = new Composite(colorPaletteTabFolder, SWT.NONE);
				colorPaletteColoringTabItem.setControl(colorPaletteColoringComposite);
				colorPaletteColoringComposite.setLayout(new GridLayout(1, false));
				
				SashForm codePainterLegendSashForm = new SashForm(colorPaletteColoringComposite, SWT.NONE);
				
				Group legendNodesGroup = new Group(codePainterLegendSashForm, SWT.NONE);
				legendNodesGroup.setText("Nodes");
				legendNodesGroup.setLayout(new GridLayout(1, false));
				
				ScrolledComposite legendNodesScrolledComposite = new ScrolledComposite(legendNodesGroup, SWT.H_SCROLL | SWT.V_SCROLL);
				legendNodesScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				legendNodesScrolledComposite.setExpandHorizontal(true);
				legendNodesScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				legendNodesScrolledComposite.setExpandVertical(true);
				
				Group legendEdgesGroup = new Group(codePainterLegendSashForm, SWT.NONE);
				legendEdgesGroup.setText("Edges");
				legendEdgesGroup.setLayout(new GridLayout(1, false));
				
				ScrolledComposite legendEdgesScrolledComposite = new ScrolledComposite(legendEdgesGroup, SWT.H_SCROLL | SWT.V_SCROLL);
				legendEdgesScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
				legendEdgesScrolledComposite.setExpandHorizontal(true);
				legendEdgesScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				legendEdgesScrolledComposite.setExpandVertical(true);
				
				codePainterLegendSashForm.setWeights(new int[] {1, 1});

				refreshColorPaletteLegend(colorPalette, legendNodesScrolledComposite, legendEdgesScrolledComposite);
				
				Point legendSize = codePainterLegendSashForm.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				GridData legendSizeHint = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
				legendSizeHint.widthHint = legendSize.x;
				legendSizeHint.heightHint = legendSize.y > MAX_COLOR_PALETTE_TAB_FOLDER_HEIGHT ? MAX_COLOR_PALETTE_TAB_FOLDER_HEIGHT : legendSize.y;
				codePainterLegendSashForm.setLayoutData(legendSizeHint);
				
				Composite colorPaletteControlsComposite = new Composite(colorPaletteContentComposite, SWT.NONE);
				colorPaletteControlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				colorPaletteControlsComposite.setLayout(new GridLayout(1, false));
				
				Button deleteButton = new Button(colorPaletteControlsComposite, SWT.NONE);
				deleteButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
				deleteButton.setText("Delete Layer");
				deleteButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						activeCodePainter.removeColorPalette(colorPalette);
						colorPaletteStates.remove(colorPaletteState);
						refreshColorPaletteLayers();
						refreshSelection();
					}
				});
				
				// compute the expand bar item height
				colorPaletteExpandItem.setHeight(colorPaletteExpandItem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				
				colorPaletteExpandBar.addExpandListener(new ExpandAdapter() {
					@Override
					public void itemExpanded(ExpandEvent e) {
						colorPaletteState.setExpanded(true);
						refreshColorPaletteLayers();
					}
					@Override
					public void itemCollapsed(ExpandEvent e) {
						colorPaletteState.setExpanded(false);
						refreshColorPaletteLayers();
					}
				});
				
				colorPaletteTabFolder.setSelection(colorPaletteState.getSelectedTab());
				
				if(!isColorPaletteEnabled){
					disableFolder(colorPaletteTabFolder);
				}
			}
		}
		
		// update the color palette layer content
		colorPaletteLayersScrolledComposite.setContent(colorPaletteLayersContentComposite);
		colorPaletteLayersScrolledComposite.setMinSize(colorPaletteLayersContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		// set the scroll position on redraw
		colorPaletteLayersScrolledComposite.getVerticalBar().setSelection(scrollPosition);
		colorPaletteLayersScrolledComposite.setOrigin(origin);
		
		colorPaletteLayersScrolledComposite.setEnabled(true);			
	}
	
	private void clearColorPaletteValidationErrorMessage(Label colorPaletteConfigurationErrorLabel) {
		colorPaletteConfigurationErrorLabel.setText("");
	}
	
	private void setColorPaletteValidationErrorMessage(Label colorPaletteConfigurationErrorLabel, String message) {
		if(message != null){
			colorPaletteConfigurationErrorLabel.setText(message);
		}
	}

	private void refreshColorPaletteLegend(ColorPalette colorPalette, ScrolledComposite legendNodesScrolledComposite, ScrolledComposite legendEdgesScrolledComposite) {
		Composite legendNodesContentComposite = new Composite(legendNodesScrolledComposite, SWT.NONE);
		legendNodesContentComposite.setLayout(new GridLayout(1, false));
		
		// sort node colors for consistency and add to panel
		List<Entry<Color,String>> nodeLegendEntries = new ArrayList<Entry<Color,String>>(colorPalette.getNodeColorLegend().entrySet());
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
			GridData nodesColorCompositeSizeHint = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			nodesColorCompositeSizeHint.widthHint = 20;
			nodesColorCompositeSizeHint.heightHint = 20;
			nodesColorComposite.setLayoutData(nodesColorCompositeSizeHint);
			
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
		List<Entry<Color,String>> edgeLegendEntries = new ArrayList<Entry<Color,String>>(colorPalette.getEdgeColorLegend().entrySet());
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
			GridData edgesColorCompositeSizeHint = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			edgesColorCompositeSizeHint.widthHint = 20;
			edgesColorCompositeSizeHint.heightHint = 20;
			edgesColorComposite.setLayoutData(edgesColorCompositeSizeHint);
			
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

	private void refreshLegend() {
		legendNodesScrolledComposite.setEnabled(false);
		legendEdgesScrolledComposite.setEnabled(false);
		
		// save the old scroll position and content origin
		int legendNodesScrollPosition = legendNodesScrolledComposite.getVerticalBar().getSelection();
		org.eclipse.swt.graphics.Point legendNodesOrigin = legendNodesScrolledComposite.getOrigin();
		
		Composite legendNodesContentComposite = new Composite(legendNodesScrolledComposite, SWT.NONE);
		legendNodesContentComposite.setLayout(new GridLayout(1, false));
		
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
		
		// set the scroll position on redraw
		legendNodesScrolledComposite.getVerticalBar().setSelection(legendNodesScrollPosition);
		legendNodesScrolledComposite.setOrigin(legendNodesOrigin);
		
		// save the old scroll position and content origin
		int legendEdgesScrollPosition = legendEdgesScrolledComposite.getVerticalBar().getSelection();
		org.eclipse.swt.graphics.Point legendEdgesOrigin = legendEdgesScrolledComposite.getOrigin();
		
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
		
		// set the scroll position on redraw
		legendEdgesScrolledComposite.getVerticalBar().setSelection(legendEdgesScrollPosition);
		legendEdgesScrolledComposite.setOrigin(legendEdgesOrigin);
		
		legendNodesScrolledComposite.setEnabled(true);
		legendEdgesScrolledComposite.setEnabled(true);
	}

	@Override
	public void setFocus() {}
}
