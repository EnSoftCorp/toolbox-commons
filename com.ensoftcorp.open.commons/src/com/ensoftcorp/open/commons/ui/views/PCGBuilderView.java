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

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.selection.IAtlasSelectionListener;
import com.ensoftcorp.atlas.ui.selection.SelectionUtil;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.commons.analysis.StandardQueries;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;
import org.eclipse.wb.swt.SWTResourceManager;

@SuppressWarnings("restriction")
public class PCGBuilderView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ensoftcorp.open.commons.ui.views.pcgBuilderView";
	
	// the current Atlas selection
	private AtlasSet<Node> selection =  new AtlasHashSet<Node>();
	
	private AtlasSet<Node> callGraphFunctions = new AtlasHashSet<Node>();

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
				messageBox.setText("Exiting Application");
				int response = messageBox.open();
				if (response != SWT.YES) {
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
		
		Group callGraphContextGroup = new Group(groupSashForm, SWT.NONE);
		callGraphContextGroup.setText("Call Graph Context");
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
				AtlasSet<Node> functions = getFilteredSelections(XCSG.Function);
				if(functions.isEmpty()){
					if(selection.isEmpty()){
						DisplayUtils.showError("Nothing is selected.");
					} else {
						DisplayUtils.showError("Selections must be functions.");
					}
				} else {
					callGraphFunctions.addAll(functions); // TODO: if addAll behaved properly we could use an if here...
					refreshCallGraphElements(callGraphContextScrolledComposite);
				}
			}
		});
		
		showButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DisplayUtils.showError("Not implemented yet.");
			}
		});
		
		// set the tab selection to this newly created tab
		pcgFolder.setSelection(pcgFolder.getItemCount()-1);
	}
	
	private void refreshCallGraphElements(final ScrolledComposite callGraphContextScrolledComposite) {
		Composite callGraphContextScrolledCompositeContent = new Composite(callGraphContextScrolledComposite, SWT.NONE);
		for(Node function : callGraphFunctions){
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
					callGraphFunctions.remove(function);
					refreshCallGraphElements(callGraphContextScrolledComposite);
				}
			});
		}
		callGraphContextScrolledComposite.setContent(callGraphContextScrolledCompositeContent);
		callGraphContextScrolledComposite.setMinSize(callGraphContextScrolledCompositeContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
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
	
	private class PCGComponents implements Comparable<PCGComponents> {
		private String name;
		private long createdAt;
		private AtlasSet<Node> callGraphNodes;
		private AtlasSet<Edge> callGraphEdges;
		private AtlasSet<Node> differentiatingCallsitesSetA;
		private AtlasSet<Node> differentiatingCallsitesSetB;
		private AtlasSet<Node> controlFlowEvents;

		public PCGComponents(String name) {
			this.name = name;
			this.createdAt = System.currentTimeMillis();
			this.callGraphNodes = new AtlasHashSet<Node>();
			this.callGraphEdges = new AtlasHashSet<Edge>();
			this.differentiatingCallsitesSetA = new AtlasHashSet<Node>();
			this.differentiatingCallsitesSetB = new AtlasHashSet<Node>();
			this.controlFlowEvents = new AtlasHashSet<Node>();
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setCallGraph(Q callGraph){
			Graph cg = callGraph.eval();
			this.callGraphNodes = cg.nodes();
			this.callGraphEdges = cg.edges();
		}
		
		public AtlasSet<Node> getCallGraphNodes() {
			return callGraphNodes;
		}

		/**
		 * Sets nodes and induces call edges
		 * @param callGraphEdges
		 */
		public void setCallGraphNodes(Q callGraphNodes) {
			setCallGraphNodes(callGraphNodes.eval().nodes());
		}
		
		/**
		 * Sets nodes and induces call edges
		 * @param callGraphEdges
		 */
		public void setCallGraphNodes(AtlasSet<Node> callGraphNodes) {
			this.callGraphNodes = callGraphNodes;
			this.callGraphEdges = Common.toQ(callGraphNodes).induce(Common.universe().edgesTaggedWithAny(XCSG.Call)).eval().edges();
		}

		public AtlasSet<Edge> getCallGraphEdges() {
			return callGraphEdges;
		}

		/**
		 * Sets edges and nodes contained in the edges
		 * @param callGraphEdges
		 */
		public void setCallGraphEdges(Q callGraphEdges) {
			setCallGraphEdges(callGraphEdges.eval().edges());
		}
		
		/**
		 * Sets edges and nodes contained in the edges
		 * @param callGraphEdges
		 */
		public void setCallGraphEdges(AtlasSet<Edge> callGraphEdges) {
			this.callGraphEdges = callGraphEdges;
			this.callGraphNodes = Common.toGraph(callGraphEdges).nodes();
		}

		public AtlasSet<Node> getDifferentiatingCallsitesSetA() {
			return differentiatingCallsitesSetA;
		}

		public void setDifferentiatingCallsitesSetA(AtlasSet<Node> differentiatingCallsitesSetA) {
			this.differentiatingCallsitesSetA = differentiatingCallsitesSetA;
		}

		public AtlasSet<Node> getDifferentiatingCallsitesSetB() {
			return differentiatingCallsitesSetB;
		}

		public void setDifferentiatingCallsitesSetB(AtlasSet<Node> differentiatingCallsitesSetB) {
			this.differentiatingCallsitesSetB = differentiatingCallsitesSetB;
		}

		public AtlasSet<Node> getControlFlowEvents() {
			return controlFlowEvents;
		}

		public void setControlFlowEvents(AtlasSet<Node> controlFlowEvents) {
			this.controlFlowEvents = controlFlowEvents;
		}

		private PCGBuilderView getOuterType() {
			return PCGBuilderView.this;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
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
			PCGComponents other = (PCGComponents) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public int compareTo(PCGComponents other) {
			return Long.compare(this.createdAt, other.createdAt);
		}
	}
}
