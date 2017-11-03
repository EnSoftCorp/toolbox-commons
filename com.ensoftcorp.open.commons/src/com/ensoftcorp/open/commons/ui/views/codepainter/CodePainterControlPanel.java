package com.ensoftcorp.open.commons.ui.views.codepainter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
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
import com.ensoftcorp.open.commons.codepainter.CodePainter;
import com.ensoftcorp.open.commons.codepainter.CodePainters;
import com.ensoftcorp.open.commons.codepainter.ColorPalettes;
import com.ensoftcorp.open.commons.utilities.selection.GraphSelectionProviderView;
import org.eclipse.swt.custom.SashForm;

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
		
		final CTabItem codePainterConfigurationsTab = new CTabItem(folder, SWT.NONE);
		codePainterConfigurationsTab.setText("Configurations");
		Composite codePainterConfigurationComposite = new Composite(folder, SWT.NONE);
		codePainterConfigurationsTab.setControl(codePainterConfigurationComposite);
		codePainterConfigurationComposite.setLayout(new GridLayout(1, false));
		
		final CTabItem codePainterColorPalettesTab = new CTabItem(folder, SWT.NONE);
		codePainterColorPalettesTab.setText("Color Palettes");
		
		SashForm sashForm = new SashForm(folder, SWT.NONE);
		codePainterColorPalettesTab.setControl(sashForm);
		
		Group applicableColorPalettesGroup = new Group(sashForm, SWT.NONE);
		applicableColorPalettesGroup.setText("Applicable Color Palettes");
		applicableColorPalettesGroup.setLayout(new GridLayout(1, false));
		
		ScrolledComposite applicableColorPalettesScrolledComposite = new ScrolledComposite(applicableColorPalettesGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		applicableColorPalettesScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		applicableColorPalettesScrolledComposite.setExpandHorizontal(true);
		applicableColorPalettesScrolledComposite.setExpandVertical(true);
		
		Composite applicableColorPalettesContentComposite = new Composite(applicableColorPalettesScrolledComposite, SWT.NONE);
		applicableColorPalettesContentComposite.setLayout(new GridLayout(1, false));
		
		ExpandBar expandBar = new ExpandBar(applicableColorPalettesContentComposite, SWT.NONE);
		expandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ExpandItem xpndtmNewExpanditem = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem.setExpanded(true);
		xpndtmNewExpanditem.setText("CP1");
		
		Composite composite = new Composite(expandBar, SWT.NONE);
		xpndtmNewExpanditem.setControl(composite);
		xpndtmNewExpanditem.setHeight(xpndtmNewExpanditem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		applicableColorPalettesScrolledComposite.setContent(applicableColorPalettesContentComposite);
		applicableColorPalettesScrolledComposite.setMinSize(applicableColorPalettesContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		Group activeColorPalletesGroup = new Group(sashForm, SWT.NONE);
		activeColorPalletesGroup.setText("Active Color Palettes");
		activeColorPalletesGroup.setLayout(new GridLayout(1, false));
		
		ScrolledComposite activeColorPalettesScrolledComposite = new ScrolledComposite(activeColorPalletesGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		activeColorPalettesScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		activeColorPalettesScrolledComposite.setExpandHorizontal(true);
		activeColorPalettesScrolledComposite.setExpandVertical(true);
		
		Composite activeColorPalettesContentComposite = new Composite(activeColorPalettesScrolledComposite, SWT.NONE);
		activeColorPalettesContentComposite.setLayout(new GridLayout(1, false));
		
		ExpandBar expandBar_1 = new ExpandBar(activeColorPalettesContentComposite, SWT.NONE);
		expandBar_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ExpandItem xpndtmNewExpanditem_1 = new ExpandItem(expandBar_1, SWT.NONE);
		xpndtmNewExpanditem_1.setExpanded(true);
		xpndtmNewExpanditem_1.setText("CP2");
		
		Composite composite_1 = new Composite(expandBar_1, SWT.NONE);
		xpndtmNewExpanditem_1.setControl(composite_1);
		xpndtmNewExpanditem_1.setHeight(xpndtmNewExpanditem_1.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		activeColorPalettesScrolledComposite.setContent(activeColorPalettesContentComposite);
		activeColorPalettesScrolledComposite.setMinSize(activeColorPalettesContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashForm.setWeights(new int[] {1, 1});
		
		final CTabItem codePainterLegendsTab = new CTabItem(folder, SWT.NONE);
		codePainterLegendsTab.setText("Legend");
		
		ScrolledComposite codePainterLegendsScrolledComposite = new ScrolledComposite(folder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		codePainterLegendsTab.setControl(codePainterLegendsScrolledComposite);
		codePainterLegendsScrolledComposite.setExpandHorizontal(true);
		codePainterLegendsScrolledComposite.setExpandVertical(true);
		
		Composite codePainterLegendsContentComposite = new Composite(codePainterLegendsScrolledComposite, SWT.NONE);
		codePainterLegendsContentComposite.setLayout(new GridLayout(1, false));
		
		Composite composite_3 = new Composite(codePainterLegendsContentComposite, SWT.NONE);
		composite_3.setLayout(new GridLayout(2, false));
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite_4 = new Composite(composite_3, SWT.BORDER);
		composite_4.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		GridData gd_composite_4 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite_4.widthHint = 20;
		gd_composite_4.heightHint = 20;
		composite_4.setLayoutData(gd_composite_4);
		
		composite_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO: implement
			}
		});
		
		Label lblNewLabel_1 = new Label(composite_3, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_1.setText("Color Label");
		
		Label label_3 = new Label(codePainterLegendsContentComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		codePainterLegendsScrolledComposite.setContent(codePainterLegendsContentComposite);
		codePainterLegendsScrolledComposite.setMinSize(codePainterLegendsContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
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
		
		// set the default tab
		folder.setSelection(codePainterSelectionTab);
		
		// register as a graph selection provider
		registerGraphSelectionProvider();
	}

	@Override
	public void setFocus() {}
}
