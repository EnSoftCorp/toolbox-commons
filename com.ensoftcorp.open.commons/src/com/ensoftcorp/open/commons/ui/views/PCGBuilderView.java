package com.ensoftcorp.open.commons.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.IAtlasSelectionListener;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.StandardQueries;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;

@SuppressWarnings("restriction")
public class PCGBuilderView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.pcgBuilderView";
	
	// the current Atlas selection
	private AtlasSet<Node> selection =  new AtlasHashSet<Node>();

	private static Map<String,PCGComponents> pcgs = new HashMap<String,PCGComponents>();
	
	private static boolean initialized = false;
	private static int pcgCounter = 1;
	
	/**
	 * The constructor.
	 */
	public PCGBuilderView() {
		// intentionally left blank
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		final CTabFolder pcgFolder = new CTabFolder(parent, SWT.CLOSE);
		pcgFolder.setBorderVisible(true);
		pcgFolder.setSimple(false); // adds the Eclise style "swoosh"
		
		// add a prompt to ask if we should really close the builder tab
		pcgFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(),
						SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("Close PCG builder instance?");
				messageBox.setText("Closing Tab");
				int response = messageBox.open();
				if (response == SWT.YES) {
					String tabName = pcgFolder.getSelection().getText();
					pcgs.remove(tabName);
				} else {
					event.doit = false;
				}
			}
		});
		
		pcgFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		// create a new PCG if this is the first launch
		if(!initialized){
			int PCG_NUMBER = (pcgCounter++);
			String PCG_NAME = "PCG" + PCG_NUMBER;
			PCGComponents pcg = new PCGComponents(PCG_NAME);
			pcgs.put(PCG_NAME, pcg);
			addPCG(pcgFolder, pcg);
			initialized = true;
		} else {
			// otherwise load what is already in memory
			ArrayList<PCGComponents> sortedPCGs = new ArrayList<PCGComponents>(pcgs.values());
			Collections.sort(sortedPCGs); // sorted by creation time
			for(PCGComponents pcg : sortedPCGs){
				addPCG(pcgFolder, pcg);
			}
		}
		
		// add an add PCG tab button to the action bar
		final Action addPCGAction = new Action() {
			public void run() {
				int PCG_NUMBER = (pcgCounter++);
				String PCG_NAME = "PCG" + PCG_NUMBER;
				PCGComponents pcg = new PCGComponents(PCG_NAME);
				pcgs.put(PCG_NAME, pcg);
				addPCG(pcgFolder, pcg);
			}
		};
		addPCGAction.setText("New PCG");
		addPCGAction.setToolTipText("Creates another PCG builder tab");
		addPCGAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEW_CONFIG));
		addPCGAction.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEW_CONFIG));
		addPCGAction.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEW_CONFIG));
		getViewSite().getActionBars().getToolBarManager().add(addPCGAction);
		
		// setup the Atlas selection event listener
		IAtlasSelectionListener selectionListener = new IAtlasSelectionListener(){
			@Override
			public void selectionChanged(IAtlasSelectionEvent atlasSelection) {
				try {
					selection = atlasSelection.getSelection().eval().nodes();
				} catch (Exception e){
					selection = new AtlasHashSet<Node>();
				}
			}				
		};
		
		// add the selection listener
		SelectionUtil.addSelectionListener(selectionListener);
	}

	private void addPCG(final CTabFolder pcgFolder, final PCGComponents pcg) {
		final CTabItem pcgTab = new CTabItem(pcgFolder, SWT.NONE);
		pcgTab.setText(pcg.getName());
		
		Composite pcgComposite = new Composite(pcgFolder, SWT.NONE);
		pcgTab.setControl(pcgComposite);
		pcgComposite.setLayout(new GridLayout(1, false));
		
		Composite pcgControlPanelComposite = new Composite(pcgComposite, SWT.NONE);
		pcgControlPanelComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		pcgControlPanelComposite.setLayout(new GridLayout(3, false));
		
		Label pcgNameLabel = new Label(pcgControlPanelComposite, SWT.NONE);
		pcgNameLabel.setSize(66, 14);
		pcgNameLabel.setText("PCG Label: ");
		
		Text pcgLabelText = new Text(pcgControlPanelComposite, SWT.BORDER);
		pcgLabelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		pcgLabelText.setSize(473, 19);
		pcgLabelText.setText(pcg.getName());
		
		pcgLabelText.addTraverseListener(new TraverseListener(){
			@Override
			public void keyTraversed(TraverseEvent event) {
				if(event.detail == SWT.TRAVERSE_RETURN){
					String newName = pcgLabelText.getText();
					pcgTab.setText(newName);
					pcg.setName(newName);
				}
			}
		});
		
		Button showButton = new Button(pcgControlPanelComposite, SWT.NONE);
		showButton.setText("Show PCG");
		
		Composite pcgBuilderComposite = new Composite(pcgComposite, SWT.NONE);
		pcgBuilderComposite.setLayout(new GridLayout(1, false));
		pcgBuilderComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		SashForm groupSashForm = new SashForm(pcgBuilderComposite, SWT.NONE);
		groupSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		final Group callGraphContextGroup = new Group(groupSashForm, SWT.NONE);
		callGraphContextGroup.setText("Call Graph Context (0 functions, 0 induced edges)");
		callGraphContextGroup.setLayout(new GridLayout(1, false));
		
		Composite addCallGraphElementComposite = new Composite(callGraphContextGroup, SWT.NONE);
		addCallGraphElementComposite.setLayout(new GridLayout(2, false));
		addCallGraphElementComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label addCallGraphElementLabel = new Label(addCallGraphElementComposite, SWT.NONE);
		addCallGraphElementLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		addCallGraphElementLabel.setBounds(0, 0, 59, 14);
		addCallGraphElementLabel.setText("Add Selected");
		
		final Label addCallGraphElementButton = new Label(addCallGraphElementComposite, SWT.NONE);
		addCallGraphElementButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		addCallGraphElementButton.setSize(20, 20);
		addCallGraphElementButton.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/add_button.png"));
		
		final ScrolledComposite callGraphContextScrolledComposite = new ScrolledComposite(callGraphContextGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		callGraphContextScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		callGraphContextScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		callGraphContextScrolledComposite.setExpandHorizontal(true);
		callGraphContextScrolledComposite.setExpandVertical(true);
		
		Group eventsGroup = new Group(groupSashForm, SWT.NONE);
		eventsGroup.setText("Events");
		eventsGroup.setLayout(new GridLayout(1, false));
		
		SashForm sashForm = new SashForm(eventsGroup, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group differentiatingCallsitesGroup = new Group(sashForm, SWT.NONE);
		differentiatingCallsitesGroup.setLayout(new GridLayout(2, false));
		differentiatingCallsitesGroup.setText("Differentiating Callsites");
		
		Group functionSetAGroup = new Group(differentiatingCallsitesGroup, SWT.NONE);
		functionSetAGroup.setLayout(new GridLayout(1, false));
		functionSetAGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		functionSetAGroup.setText("Function Set A");
		
		Composite functionSetAComposite = new Composite(functionSetAGroup, SWT.NONE);
		functionSetAComposite.setLayout(new GridLayout(2, false));
		functionSetAComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label addDifferentiatingFunctionSetAElementLabel = new Label(functionSetAComposite, SWT.NONE);
		addDifferentiatingFunctionSetAElementLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		addDifferentiatingFunctionSetAElementLabel.setText("Add Selected");
		
		Label addDifferentiatingFunctionSetAElementButton = new Label(functionSetAComposite, SWT.NONE);
		addDifferentiatingFunctionSetAElementButton.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/add_button.png"));
		addDifferentiatingFunctionSetAElementButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		ScrolledComposite functionSetAEventsScrolledComposite = new ScrolledComposite(functionSetAGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		functionSetAEventsScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		functionSetAEventsScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		functionSetAEventsScrolledComposite.setExpandHorizontal(true);
		functionSetAEventsScrolledComposite.setExpandVertical(true);
		
		Group functionSetBGroup = new Group(differentiatingCallsitesGroup, SWT.NONE);
		functionSetBGroup.setLayout(new GridLayout(1, false));
		functionSetBGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		functionSetBGroup.setText("Function Set B");
		
		Composite functionSetBComposite = new Composite(functionSetBGroup, SWT.NONE);
		functionSetBComposite.setLayout(new GridLayout(2, false));
		functionSetBComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label addDifferentiatingFunctionSetBElementLabel = new Label(functionSetBComposite, SWT.NONE);
		addDifferentiatingFunctionSetBElementLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		addDifferentiatingFunctionSetBElementLabel.setText("Add Selected");
		
		Label addDifferentiatingFunctionSetBElementButton = new Label(functionSetBComposite, SWT.NONE);
		addDifferentiatingFunctionSetBElementButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		addDifferentiatingFunctionSetBElementButton.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/add_button.png"));
		
		ScrolledComposite functionSetBEventsScrolledComposite = new ScrolledComposite(functionSetBGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		functionSetBEventsScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		functionSetBEventsScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		functionSetBEventsScrolledComposite.setExpandHorizontal(true);
		functionSetBEventsScrolledComposite.setExpandVertical(true);
		
		Group controlFlowEventsGroup = new Group(sashForm, SWT.NONE);
		controlFlowEventsGroup.setText("Control Flow Events");
		controlFlowEventsGroup.setLayout(new GridLayout(1, false));
		
		Composite addControlFlowEventsElementComposite = new Composite(controlFlowEventsGroup, SWT.NONE);
		addControlFlowEventsElementComposite.setLayout(new GridLayout(2, false));
		addControlFlowEventsElementComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label addControlFlowEventsElementLabel = new Label(addControlFlowEventsElementComposite, SWT.NONE);
		addControlFlowEventsElementLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		addControlFlowEventsElementLabel.setText("Add Selected");
		
		Label addControlFlowEventsElementButton = new Label(addControlFlowEventsElementComposite, SWT.NONE);
		addControlFlowEventsElementButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		addControlFlowEventsElementButton.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/add_button.png"));
		
		ScrolledComposite controlFlowEventsScrolledComposite = new ScrolledComposite(controlFlowEventsGroup, SWT.H_SCROLL | SWT.V_SCROLL);
		controlFlowEventsScrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		controlFlowEventsScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		controlFlowEventsScrolledComposite.setExpandHorizontal(true);
		controlFlowEventsScrolledComposite.setExpandVertical(true);
		sashForm.setWeights(new int[] {1, 1});
		groupSashForm.setWeights(new int[] {250, 441});
		
		addCallGraphElementButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if(selection.isEmpty()){
					DisplayUtils.showError("Nothing is selected.");
				} else {
					AtlasSet<Node> functions = getFilteredSelections(XCSG.Function);
					if(functions.isEmpty()){
						DisplayUtils.showError("Selections must be functions or function callsites.");
					} else {
						if(pcg.addCallGraphFunctions(functions)){
							refreshCallGraphElements(callGraphContextGroup, callGraphContextScrolledComposite, controlFlowEventsScrolledComposite, pcg);
						}
					}
				}
			}
		});
		
		addDifferentiatingFunctionSetAElementButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if(selection.isEmpty()){
					DisplayUtils.showError("Nothing is selected.");
				} else {
					AtlasSet<Node> functions = getFilteredSelections(XCSG.Function);
					if(functions.isEmpty()){
						DisplayUtils.showError("Selections must be functions or function callsites.");
					} else {
						Q a = Common.toQ(pcg.getDifferentiatingCallsitesSetA()).union(Common.toQ(functions));
						Q b = Common.toQ(pcg.getDifferentiatingCallsitesSetB());
						if(!CommonQueries.isEmpty(a.intersection(b))){
							DisplayUtils.showError("Sets A and B must be disjoint sets.");
						} else {
							if(pcg.addDifferentiatingCallsitesSetA(functions)){
								refreshDifferentiatingCallsitesSetAElements(functionSetAEventsScrolledComposite, pcg);
							}
						}
					}
				}
			}
		});
		
		addDifferentiatingFunctionSetBElementButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if(selection.isEmpty()){
					DisplayUtils.showError("Nothing is selected.");
				} else {
					AtlasSet<Node> functions = getFilteredSelections(XCSG.Function);
					if(functions.isEmpty()){
						DisplayUtils.showError("Selections must be functions.");
					} else {
						Q a = Common.toQ(pcg.getDifferentiatingCallsitesSetA());
						Q b = Common.toQ(pcg.getDifferentiatingCallsitesSetB()).union(Common.toQ(functions));
						if(!CommonQueries.isEmpty(a.intersection(b))){
							DisplayUtils.showError("Sets A and B must be disjoint sets.");
						} else {
							if(pcg.addDifferentiatingCallsitesSetB(functions)){
								refreshDifferentiatingCallsitesSetBElements(functionSetBEventsScrolledComposite, pcg);
							}
						}
					}
				}
			}
		});
		
		addControlFlowEventsElementButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if(selection.isEmpty()){
					DisplayUtils.showError("Nothing is selected.");
				} else {
					AtlasSet<Node> controlFlowNodes = getFilteredSelections(XCSG.ControlFlow_Node);
					
					// expand search to control flow nodes that correspond to this node
					if(controlFlowNodes.isEmpty()){
						controlFlowNodes = Common.toQ(selection).containers().nodesTaggedWithAny(XCSG.ControlFlow_Node).eval().nodes();
					}
					
					if(controlFlowNodes.isEmpty()){
						DisplayUtils.showError("Selections must correspond to control flow statements.");
					} else {
						if(pcg.addControlFlowEvents(controlFlowNodes)){
							refreshControlFlowEventElements(controlFlowEventsScrolledComposite,pcg);
						}
						AtlasSet<Node> containingFunctions = new AtlasHashSet<Node>();
						for(Node controlFlowNode : controlFlowNodes){
							containingFunctions.add(StandardQueries.getContainingFunction(controlFlowNode));
						}
						if(pcg.addCallGraphFunctions(containingFunctions)){
							refreshCallGraphElements(callGraphContextGroup, callGraphContextScrolledComposite, controlFlowEventsScrolledComposite, pcg);
						}
					}
				}
			}
		});
		
		showButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean noControlFlowEvents = pcg.getControlFlowEvents().isEmpty();
				boolean noDifferentiatingCallsiteEvents = pcg.getDifferentiatingCallsitesSetA().isEmpty() || pcg.getDifferentiatingCallsitesSetB().isEmpty();
				if(noControlFlowEvents && noDifferentiatingCallsiteEvents){
					DisplayUtils.showError("No control flow events or differentiating callsite events are defined.");
				} else if(pcg.getCallGraphFunctions().isEmpty()){
					DisplayUtils.showError("Call graph context cannot be empty.");
				} else {
					DisplayUtils.showError("Not implemented yet.");
				}
			}
		});
		
		// set the tab selection to this newly created tab
		pcgFolder.setSelection(pcgFolder.getItemCount()-1);
	}
	
	private void refreshCallGraphElements(final Group callGraphContextGroup, final ScrolledComposite callGraphContextScrolledComposite, final ScrolledComposite controlFlowEventsScrolledComposite, final PCGComponents pcg) {
		Composite callGraphContextScrolledCompositeContent = new Composite(callGraphContextScrolledComposite, SWT.NONE);
		
		long numNodes = pcg.getCallGraphFunctions().size();
		Q callEdges = Common.universe().edgesTaggedWithAny(XCSG.Call);
		long numEdges = Common.toQ(pcg.getCallGraphFunctions()).induce(callEdges).eval().edges().size();
		callGraphContextGroup.setText("Call Graph Context (" 
				+ numNodes + " function" + (numNodes > 1 ? "s" : "") 
				+ ", " + numEdges + " induced edge" + (numEdges > 1 ? "s" : "") + ")");
		
		for(Node function : pcg.getCallGraphFunctions()){
			callGraphContextScrolledCompositeContent.setLayout(new GridLayout(1, false));
			
			Label callGraphContextSeperatorLabel = new Label(callGraphContextScrolledCompositeContent, SWT.SEPARATOR | SWT.HORIZONTAL);
			callGraphContextSeperatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Composite callGraphContextEntryComposite = new Composite(callGraphContextScrolledCompositeContent, SWT.NONE);
			callGraphContextEntryComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
			callGraphContextEntryComposite.setLayout(new GridLayout(2, false));
			
			final Label deleteButton = new Label(callGraphContextEntryComposite, SWT.NONE);
			deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
			deleteButton.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/delete_button.png"));

			Label functionLabel = new Label(callGraphContextEntryComposite, SWT.NONE);
			functionLabel.setToolTipText(function.toString());
			functionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			functionLabel.setBounds(0, 0, 59, 14);
			functionLabel.setText(StandardQueries.getQualifiedFunctionName(function));
			
			deleteButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					AtlasSet<Node> controlFlowNodesToRemove = new AtlasHashSet<Node>();
					for(Node controlFlowNode : pcg.getControlFlowEvents()){
						Node containingFunction = StandardQueries.getContainingFunction(controlFlowNode);
						if(function.equals(containingFunction)){
							controlFlowNodesToRemove.add(controlFlowNode);
						}
					}
					
					if(!controlFlowNodesToRemove.isEmpty()){
						MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(),
								SWT.ICON_QUESTION | SWT.YES | SWT.NO);
						messageBox.setMessage("Removing this function from the call graph context would remove " 
								+ controlFlowNodesToRemove.size() + " control flow events. Would you like to proceed?");
						messageBox.setText("Removing Control Flow Events");
						int response = messageBox.open();
						if (response == SWT.YES) {
							pcg.removeCallGraphFunction(function);
							refreshCallGraphElements(callGraphContextGroup, callGraphContextScrolledComposite, controlFlowEventsScrolledComposite, pcg);
							for(Node controlFlowEventToRemove : controlFlowNodesToRemove){
								pcg.removeControlFlowEvent(controlFlowEventToRemove);
							}
							refreshControlFlowEventElements(controlFlowEventsScrolledComposite, pcg);
						}
					} else {
						pcg.removeCallGraphFunction(function);
						refreshCallGraphElements(callGraphContextGroup, callGraphContextScrolledComposite, controlFlowEventsScrolledComposite, pcg);
					}
				}
			});
		}
		callGraphContextScrolledComposite.setContent(callGraphContextScrolledCompositeContent);
		callGraphContextScrolledComposite.setMinSize(callGraphContextScrolledCompositeContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void refreshDifferentiatingCallsitesSetAElements(final ScrolledComposite differentiatingCallsitesSetScrolledComposite, final PCGComponents pcg) {
		Composite differentiatingCallsitesSetScrolledCompositeContent = new Composite(differentiatingCallsitesSetScrolledComposite, SWT.NONE);
		for(Node function : pcg.getDifferentiatingCallsitesSetA()){
			differentiatingCallsitesSetScrolledCompositeContent.setLayout(new GridLayout(1, false));
			
			Label differentiatingCallsitesContextSeperatorLabel = new Label(differentiatingCallsitesSetScrolledCompositeContent, SWT.SEPARATOR | SWT.HORIZONTAL);
			differentiatingCallsitesContextSeperatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Composite differentiatingCallsitesContextEntryComposite = new Composite(differentiatingCallsitesSetScrolledCompositeContent, SWT.NONE);
			differentiatingCallsitesContextEntryComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
			differentiatingCallsitesContextEntryComposite.setLayout(new GridLayout(2, false));
			
			final Label deleteButton = new Label(differentiatingCallsitesContextEntryComposite, SWT.NONE);
			deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
			deleteButton.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/delete_button.png"));

			Label functionLabel = new Label(differentiatingCallsitesContextEntryComposite, SWT.NONE);
			functionLabel.setToolTipText(function.toString());
			functionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			functionLabel.setBounds(0, 0, 59, 14);
			functionLabel.setText(StandardQueries.getQualifiedFunctionName(function));
			
			deleteButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					pcg.removeDifferentiatingCallsiteSetA(function);
					refreshDifferentiatingCallsitesSetAElements(differentiatingCallsitesSetScrolledComposite, pcg);
				}
			});
		}
		differentiatingCallsitesSetScrolledComposite.setContent(differentiatingCallsitesSetScrolledCompositeContent);
		differentiatingCallsitesSetScrolledComposite.setMinSize(differentiatingCallsitesSetScrolledCompositeContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void refreshDifferentiatingCallsitesSetBElements(final ScrolledComposite differentiatingCallsitesSetScrolledComposite, final PCGComponents pcg) {
		Composite differentiatingCallsitesSetScrolledCompositeContent = new Composite(differentiatingCallsitesSetScrolledComposite, SWT.NONE);
		for(Node function : pcg.getDifferentiatingCallsitesSetB()){
			differentiatingCallsitesSetScrolledCompositeContent.setLayout(new GridLayout(1, false));
			
			Label differentiatingCallsitesContextSeperatorLabel = new Label(differentiatingCallsitesSetScrolledCompositeContent, SWT.SEPARATOR | SWT.HORIZONTAL);
			differentiatingCallsitesContextSeperatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Composite differentiatingCallsitesContextEntryComposite = new Composite(differentiatingCallsitesSetScrolledCompositeContent, SWT.NONE);
			differentiatingCallsitesContextEntryComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
			differentiatingCallsitesContextEntryComposite.setLayout(new GridLayout(2, false));
			
			final Label deleteButton = new Label(differentiatingCallsitesContextEntryComposite, SWT.NONE);
			deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
			deleteButton.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/delete_button.png"));

			Label functionLabel = new Label(differentiatingCallsitesContextEntryComposite, SWT.NONE);
			functionLabel.setToolTipText(function.toString());
			functionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			functionLabel.setBounds(0, 0, 59, 14);
			functionLabel.setText(StandardQueries.getQualifiedFunctionName(function));
			
			deleteButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					pcg.removeDifferentiatingCallsiteSetB(function);
					refreshDifferentiatingCallsitesSetBElements(differentiatingCallsitesSetScrolledComposite, pcg);
				}
			});
		}
		differentiatingCallsitesSetScrolledComposite.setContent(differentiatingCallsitesSetScrolledCompositeContent);
		differentiatingCallsitesSetScrolledComposite.setMinSize(differentiatingCallsitesSetScrolledCompositeContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void refreshControlFlowEventElements(final ScrolledComposite controlFlowEventsScrolledComposite, final PCGComponents pcg) {
		Composite controlFlowEventsScrolledCompositeContent = new Composite(controlFlowEventsScrolledComposite, SWT.NONE);
		for(Node event : pcg.getControlFlowEvents()){
			controlFlowEventsScrolledCompositeContent.setLayout(new GridLayout(1, false));
			
			Label controlFlowEventsSeperatorLabel = new Label(controlFlowEventsScrolledCompositeContent, SWT.SEPARATOR | SWT.HORIZONTAL);
			controlFlowEventsSeperatorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Composite controlFlowEventsEntryComposite = new Composite(controlFlowEventsScrolledCompositeContent, SWT.NONE);
			controlFlowEventsEntryComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
			controlFlowEventsEntryComposite.setLayout(new GridLayout(2, false));
			
			final Label deleteButton = new Label(controlFlowEventsEntryComposite, SWT.NONE);
			deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
			deleteButton.setImage(ResourceManager.getPluginImage("com.ensoftcorp.open.commons", "icons/delete_button.png"));

			Label eventLabel = new Label(controlFlowEventsEntryComposite, SWT.NONE);
			eventLabel.setToolTipText(event.toString());
			eventLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			eventLabel.setBounds(0, 0, 59, 14);
			
			Node function = StandardQueries.getContainingFunction(event);
			eventLabel.setText(event.getAttr(XCSG.name).toString() 
					+ " (" + StandardQueries.getQualifiedFunctionName(function) + ")");
			
			deleteButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					pcg.removeControlFlowEvent(event);
					refreshControlFlowEventElements(controlFlowEventsScrolledComposite, pcg);
				}
			});
		}
		controlFlowEventsScrolledComposite.setContent(controlFlowEventsScrolledCompositeContent);
		controlFlowEventsScrolledComposite.setMinSize(controlFlowEventsScrolledCompositeContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private AtlasSet<Node> getFilteredSelections(String... tags){
		AtlasSet<Node> currentSelection = new AtlasHashSet<Node>(selection);
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		for(Node node : currentSelection){
			for(String tag : tags){
				if(node.taggedWith(tag)){
					result.add(node);
					break;
				}
			}
		}
		return result;
	}

	@Override
	public void setFocus() {
		// intentionally left blank
	}
}
