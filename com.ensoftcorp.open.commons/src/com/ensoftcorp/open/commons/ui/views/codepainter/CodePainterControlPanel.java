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
		
		ScrolledComposite codePainterColorPalettesScrolledComposite = new ScrolledComposite(folder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		codePainterColorPalettesTab.setControl(codePainterColorPalettesScrolledComposite);
		codePainterColorPalettesScrolledComposite.setExpandHorizontal(true);
		codePainterColorPalettesScrolledComposite.setExpandVertical(true);
		
		Composite codePainterColorPaletteContentComposite = new Composite(codePainterColorPalettesScrolledComposite, SWT.NONE);
		codePainterColorPaletteContentComposite.setLayout(new GridLayout(1, false));
		
		Label lblNewLabel = new Label(codePainterColorPaletteContentComposite, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel.setText("New Label");
		
		Label label = new Label(codePainterColorPaletteContentComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite composite = new Composite(codePainterColorPaletteContentComposite, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnEnabled = new Button(composite, SWT.CHECK);
		btnEnabled.setSelection(true);
		btnEnabled.setText("Enabled");
		
		ExpandBar expandBar = new ExpandBar(composite, SWT.NONE);
		expandBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ExpandItem xpndtmNewExpanditem = new ExpandItem(expandBar, SWT.NONE);
		xpndtmNewExpanditem.setExpanded(true);
		xpndtmNewExpanditem.setText("Base Color Palette: Empty");
		
		Composite composite_1 = new Composite(expandBar, SWT.NONE);
		xpndtmNewExpanditem.setControl(composite_1);
		xpndtmNewExpanditem.setHeight(xpndtmNewExpanditem.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		composite_1.setLayout(new GridLayout(1, false));
		
		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite_2.setLayout(new GridLayout(2, false));
		
		Composite color = new Composite(composite_2, SWT.BORDER);
		color.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		GridData gd_color = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_color.widthHint = 20;
		gd_color.heightHint = 20;
		color.setLayoutData(gd_color);
		
		color.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO: implement
			}
		});
		
		Label colorLabel = new Label(composite_2, SWT.NONE);
		colorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		colorLabel.setSize(439, 15);
		colorLabel.setText("Color Label");
		
		Label label_2 = new Label(composite_1, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label label_1 = new Label(codePainterColorPaletteContentComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		codePainterColorPalettesScrolledComposite.setContent(codePainterColorPaletteContentComposite);
		codePainterColorPalettesScrolledComposite.setMinSize(codePainterColorPaletteContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
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
