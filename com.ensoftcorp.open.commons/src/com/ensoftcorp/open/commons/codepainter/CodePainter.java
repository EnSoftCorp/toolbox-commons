package com.ensoftcorp.open.commons.codepainter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.ui.scripts.selections.IExplorableScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.scripts.util.SimpleScriptUtil;
import com.ensoftcorp.atlas.ui.selection.event.FrontierEdgeExploreEvent;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;

public abstract class CodePainter extends Configurable implements IResizableScript, IExplorableScript {
	
	/**
	 * Holds an unstyled result
	 */
	public static class UnstyledResult {
		private Q result;
		
		public UnstyledResult(Q result){
			this.result = result;
		}
		
		public Q getResult(){
			return result;
		}
	}
	
	/**
	 * Holds and unstyled frontier result
	 */
	public static class UnstyledFrontierResult {
		private Q result;
		private Q frontierForward;
		private Q frontierReverse;
		
		public UnstyledFrontierResult(Q result, Q frontierReverse, Q frontierForward){
			this.result = result;
			this.frontierReverse = frontierReverse;
			this.frontierForward = frontierForward;
		}
		
		public Q getResult(){
			return result;
		}
		
		public Q getFrontierForward(){
			return frontierForward;
		}
		
		public Q getFrontierReverse(){
			return frontierReverse;
		}
	}
	
	public CodePainter(){
		restoreDefaultColorPalettes();
	}
	
	/**
	 * Defines strategies for resolving coloring conflicts
	 */
	public static enum ColorPaletteConflictStrategy {
		CHOOSE_FIRST_MATCH, CHOOSE_LAST_MATCH, MIX_COLORS
	}
	
	private ArrayList<ColorPalette> colorPalettes = new ArrayList<ColorPalette>();
	private Map<ColorPalette,Boolean> enabledColorPalettes = new HashMap<ColorPalette,Boolean>(); 
	private ColorPaletteConflictStrategy conflictStrategy = ColorPaletteConflictStrategy.CHOOSE_FIRST_MATCH;
	
	/**
	 * Returns a default list of ordered color palettes
	 * The 0th element represents the first color palette layer the will be applied
	 * @return
	 */
	public abstract List<ColorPalette> getDefaultColorPalettes();
	
	/**
	 * Restores default color palettes
	 */
	public void restoreDefaultColorPalettes(){
		this.colorPalettes.clear();
		this.enabledColorPalettes.clear();
		for(ColorPalette colorPalette : getDefaultColorPalettes()){
			this.addColorPalette(colorPalette);
		}
	}
	
	/**
	 * Sets a coloring conflict resolution strategy
	 * @param conflictStrategy
	 */
	public void setColorPaletteConflictStrategy(ColorPaletteConflictStrategy conflictStrategy){
		this.conflictStrategy = conflictStrategy;
	}
	
	public ColorPalette getActiveColorPalette(){
		ColorPalette activeColorPalette = new ColorPalette(){
			@Override
			public String getName() {
				return "Active Color Palette";
			}

			@Override
			public String getDescription() {
				return "The result of the base (default) color pallete and all applied color palletes with a " + conflictStrategy.toString() + " coloring conflict resolution strategy";
			}

			@Override
			public Map<Node, Color> getNodeColors() {
				// get the color palettes and conflict resolution strategy
				List<ColorPalette> colorPalettes = getEnabledColorPalettes();
				ColorPaletteConflictStrategy currentConflictStrategy = conflictStrategy;
				
				// compute node colorings for the active color palettes
				Map<Node, Color> nodeColors = new HashMap<Node, Color>();
				Map<Node, List<Color>> nodeColorsToMix = new HashMap<Node, List<Color>>();
				for(ColorPalette colorPalette : colorPalettes){
					for(Entry<Node,Color> entry : colorPalette.getNodeColors().entrySet()){
						Node node = entry.getKey();
						Color color = entry.getValue();
						boolean isColored = nodeColors.containsKey(node);
						boolean conflict = isColored && !nodeColors.get(node).equals(color);
						if(conflict){
							if(currentConflictStrategy == ColorPaletteConflictStrategy.CHOOSE_FIRST_MATCH){
								// we already have the final coloring for this node
								continue;
							} else if(currentConflictStrategy == ColorPaletteConflictStrategy.CHOOSE_LAST_MATCH){
								// we have a new coloring for this node
								nodeColors.put(node, color);
								continue;
							}
						}
						
						if(isColored && currentConflictStrategy == ColorPaletteConflictStrategy.MIX_COLORS){
							if(nodeColorsToMix.containsKey(node)){
								nodeColorsToMix.get(node).add(color);
							} else {
								List<Color> colors = new LinkedList<Color>();
								colors.add(color);
								nodeColorsToMix.put(node, colors);
							}
							continue;
						}
						
						// if we reach this point, this is just a new coloring with no conflicts
						nodeColors.put(node, color);
					}
				}
				
				// update final node coloring with mixed colors
				if(currentConflictStrategy == ColorPaletteConflictStrategy.MIX_COLORS){
					for(Entry<Node,List<Color>> entry : nodeColorsToMix.entrySet()){
						ArrayList<Color> colorsList = new ArrayList<Color>(entry.getValue());
						Color[] colors = new Color[colorsList.size()];
						colorsList.toArray(colors);
						Color mix = ColorMixer.mix(colors);
						nodeColors.put(entry.getKey(), mix);
					}
				}
				
				return nodeColors;
			}

			@Override
			public Map<Edge, Color> getEdgeColors() {
				// get the color palettes and conflict resolution strategy
				List<ColorPalette> colorPalettes = getEnabledColorPalettes();
				ColorPaletteConflictStrategy currentConflictStrategy = conflictStrategy;
				
				// compute edge colorings for the active color palettes
				Map<Edge, Color> edgeColors = new HashMap<Edge, Color>();
				Map<Edge, List<Color>> edgeColorsToMix = new HashMap<Edge, List<Color>>();
				for(ColorPalette colorPalette : colorPalettes){
					for(Entry<Edge,Color> entry : colorPalette.getEdgeColors().entrySet()){
						Edge edge = entry.getKey();
						Color color = entry.getValue();
						boolean isColored = edgeColors.containsKey(edge);
						boolean conflict = isColored && !edgeColors.get(edge).equals(color);
						if(conflict){
							if(currentConflictStrategy == ColorPaletteConflictStrategy.CHOOSE_FIRST_MATCH){
								// we already have the final coloring for this edge
								continue;
							} else if(currentConflictStrategy == ColorPaletteConflictStrategy.CHOOSE_LAST_MATCH){
								// we have a new coloring for this edge
								edgeColors.put(edge, color);
								continue;
							}
						}
						
						if(isColored && currentConflictStrategy == ColorPaletteConflictStrategy.MIX_COLORS){
							if(edgeColorsToMix.containsKey(edge)){
								edgeColorsToMix.get(edge).add(color);
							} else {
								List<Color> colors = new LinkedList<Color>();
								colors.add(color);
								edgeColorsToMix.put(edge, colors);
							}
							continue;
						}
						
						// if we reach this point, this is just a new coloring with no conflicts
						edgeColors.put(edge, color);
					}
				}
				
				// update final edge coloring with mixed colors
				if(currentConflictStrategy == ColorPaletteConflictStrategy.MIX_COLORS){
					for(Entry<Edge,List<Color>> entry : edgeColorsToMix.entrySet()){
						ArrayList<Color> colorsList = new ArrayList<Color>(entry.getValue());
						Color[] colors = new Color[colorsList.size()];
						colorsList.toArray(colors);
						Color mix = ColorMixer.mix(colors);
						edgeColors.put(entry.getKey(), mix);
					}
				}
				
				return edgeColors;
			}

			@Override
			public Map<Color, String> getNodeColorLegend() {
				// get the color palettes and conflict resolution strategy
				List<ColorPalette> colorPalettes = getEnabledColorPalettes();
				ColorPaletteConflictStrategy currentConflictStrategy = conflictStrategy;
				
				// compute color legend for the active color palettes
				Map<Color, String> legend = new HashMap<Color, String>();
				for(ColorPalette colorPalette : colorPalettes){
					for(Entry<Color,String> entry : colorPalette.getNodeColorLegend().entrySet()){
						Color color = entry.getKey();
						String name = entry.getValue();
						boolean isNamed = legend.containsKey(color);
						boolean conflict = isNamed && !legend.get(color).equals(name);
						if(conflict){
							if(currentConflictStrategy == ColorPaletteConflictStrategy.CHOOSE_FIRST_MATCH){
								// we already have the final name for this color
								continue;
							} else if(currentConflictStrategy == ColorPaletteConflictStrategy.CHOOSE_LAST_MATCH){
								// we have a new name for this color
								legend.put(color, name);
								continue;
							}
						}
						
						if(isNamed && currentConflictStrategy == ColorPaletteConflictStrategy.MIX_COLORS){
							// its both so just append the name
							String previousName = legend.remove(color);
							String newName = previousName + ", " + name;
							legend.put(color, newName);
							continue;
						}
						
						// if we reach this point, this is just a new name with no conflicts
						legend.put(color, name);
					}
				}
				
				return legend;
			}

			@Override
			public Map<Color, String> getEdgeColorLegend() {
				// get the color palettes and conflict resolution strategy
				List<ColorPalette> colorPalettes = getEnabledColorPalettes();
				ColorPaletteConflictStrategy currentConflictStrategy = conflictStrategy;
				
				// compute color legend for the active color palettes
				Map<Color, String> legend = new HashMap<Color, String>();
				for(ColorPalette colorPalette : colorPalettes){
					for(Entry<Color,String> entry : colorPalette.getEdgeColorLegend().entrySet()){
						Color color = entry.getKey();
						String name = entry.getValue();
						boolean isNamed = legend.containsKey(color);
						boolean conflict = isNamed && !legend.get(color).equals(name);
						if(conflict){
							if(currentConflictStrategy == ColorPaletteConflictStrategy.CHOOSE_FIRST_MATCH){
								// we already have the final name for this color
								continue;
							} else if(currentConflictStrategy == ColorPaletteConflictStrategy.CHOOSE_LAST_MATCH){
								// we have a new name for this color
								legend.put(color, name);
								continue;
							}
						}
						
						if(isNamed && currentConflictStrategy == ColorPaletteConflictStrategy.MIX_COLORS){
							// its both so just append the name
							String previousName = legend.remove(color);
							String newName = previousName + ", " + name;
							legend.put(color, newName);
							continue;
						}
						
						// if we reach this point, this is just a new name with no conflicts
						legend.put(color, name);
					}
				}
				
				return legend;
			}

			@Override
			protected void canvasChanged() {}
			
		};
		return activeColorPalette;
	}
	
	/**
	 * Returns an ordered list of color palettes
	 * The 0th element represents the first layer of coloring
	 * @return
	 */
	public List<ColorPalette> getColorPalettes(){
		return new ArrayList<ColorPalette>(colorPalettes);
	}
	
	/**
	 * Returns an ordered list of color palettes that are enabled.
	 * This result is a subset of the result produced by getColorPalettes()
	 * The 0th element represents the first layer of coloring
	 * @return
	 */
	public List<ColorPalette> getEnabledColorPalettes(){
		List<ColorPalette> enabledColorPalettes = new LinkedList<ColorPalette>();
		for(ColorPalette colorPalette : getColorPalettes()){
			if(isColorPaletteEnabled(colorPalette)){
				enabledColorPalettes.add(colorPalette);
			}
		}
		return enabledColorPalettes;
	}
	
	/**
	 * Adds a color palette to the code painter and enables the color palette
	 * @param palette
	 */
	public void addColorPalette(ColorPalette palette){
		if(palette != null){
			if(!colorPalettes.contains(palette)){
				colorPalettes.add(palette);
				enabledColorPalettes.put(palette, true);
			}
		}
	}
	
	/**
	 * Adds a color palette to the code painter at the given index and shifts
	 * any subsequent color palettes to the right of the index
	 * 
	 * @param palette
	 * @param index
	 */
	public void addColorPalette(ColorPalette palette, int index){
		if(palette != null){
			if(!colorPalettes.contains(palette)){
				colorPalettes.add(index, palette);
				enabledColorPalettes.put(palette, true);
			}
		}
	}
	
	/**
	 * Removes a color palette from the code painter
	 * @param palette
	 */
	public void removeColorPalette(ColorPalette palette){
		colorPalettes.remove(palette);
		enabledColorPalettes.remove(palette);
	}
	
	/**
	 * Removes a color palette at the given index and shifts any subsequent
	 * methods to the left of the index
	 * 
	 * @param index
	 */
	public void removeColorPalette(int index){
		enabledColorPalettes.remove(colorPalettes.remove(index));
	}
	
	/**
	 * Returns true if the color palette exists in the code painter and the
	 * palette is enabled
	 * 
	 * @param palette
	 * @return
	 */
	public boolean isColorPaletteEnabled(ColorPalette palette){
		return enabledColorPalettes.containsKey(palette) && enabledColorPalettes.get(palette);
	}
	
	/**
	 * Enables the color palette if the color palette exists and isn't already enabled
	 * @param palette
	 */
	public void enableColorPalette(ColorPalette palette){
		if(enabledColorPalettes.containsKey(palette)){
			enabledColorPalettes.put(palette, true);
		}
	}
	
	/**
	 * Disables the color palette if the color palette exists and isn't already disabled
	 * @param palette
	 */
	public void disableColorPalette(ColorPalette palette){
		if(enabledColorPalettes.containsKey(palette)){
			enabledColorPalettes.put(palette, false);
		}
	}
	
	/**
	 * Returns the name of the code painter
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Returns a plaintext description of the code painter
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Defines the category this code painter is classified under.
	 * Optionally categories can be qualified for multiple levels with "/"
	 * @return
	 */
	public abstract String getCategory();

	/**
	 * Returns the supported node types this code painter can respond to
	 * @return
	 */
	public abstract String[] getSupportedNodeTags();
	
	/**
	 * Returns the supported edge types this code painter can respond to
	 * @return
	 */
	public abstract String[] getSupportedEdgeTags();

	/**
	 * Returns the number of reverse steps out of the graph to compute
	 * @return
	 */
	public abstract int getDefaultStepReverse();
	
	/**
	 * This is a legacy interface to get the number of steps to reverse
	 * Use getDefaultStepReverse method instead.
	 */
	@Deprecated
	@Override
	public int getDefaultStepTop() {
		return getDefaultStepReverse();
	}

	/**
	 * This is a legacy interface to get the number of steps to forward
	 * Use getDefaultStepForward method instead.
	 */
	@Deprecated
	@Override
	public int getDefaultStepBottom() {
		return getDefaultStepForward();
	}
	
	/**
	 * Returns the number of forward steps out of the graph to compute
	 * @return
	 */
	public abstract int getDefaultStepForward();
	
	@Override
	public FrontierStyledResult explore(FrontierEdgeExploreEvent event, FrontierStyledResult oldResult) {
		return SimpleScriptUtil.explore(this, event, oldResult);
	}

	/**
	 * Indicates that the code painter supports selections on nothing
	 */
	protected static final String[] NOTHING = null;
	
	/**
	 * Indicates that the code painter supports selections on everything
	 */
	protected static final String[] EVERYTHING = new String[]{};

	/**
	 * Filters the code painter selection to the supported node and edge types
	 * @param event
	 * @return
	 */
	protected Q filter(IAtlasSelectionEvent event) {
		return filter(event.getSelection());
	}

	/**
	 * Filters the input to the code painters supported node and edge types
	 * @param input
	 * @return
	 */
	protected Q filter(Q input) {
		String[] supportedNodeTags = getSupportedNodeTags();
		String[] supportedEdgeTags = getSupportedEdgeTags();
		Q result = Common.empty();
		if (supportedNodeTags != null) {
			if (supportedNodeTags.length > 0) {
				result = result.union(new Q[] { input.nodesTaggedWithAny(supportedNodeTags).retainNodes() });
			} else {
				result = result.union(new Q[] { input.retainNodes() });
			}
		}
		if (supportedEdgeTags != null) {
			if (supportedEdgeTags.length > 0) {
				result = result.union(new Q[] { input.edgesTaggedWithAny(supportedEdgeTags).retainEdges() });
			} else {
				result = result.union(new Q[] { input.retainEdges() });
			}
		}
		return result;
	}
	
	/**
	 * Returns true if the input contains nodes or edges supported by the code painter
	 * @param input
	 * @return
	 */
	public boolean isApplicableTo(Q input){
		return !CommonQueries.isEmpty(filter(input));
	}
	
	/**
	 * Computes a new styled frontier result for a given selection event and the
	 * number of steps forward and reverse to explore on the frontier.
	 */
	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward){
		// do not update the result if selection is invalid or empty
		Q filteredSelection = filter(event);
		if ((filteredSelection == null) || (CommonQueries.isEmpty(filteredSelection))) {
			return null;
		}
		
		Q convertedSelections = convertSelection(filteredSelection);
		UnstyledFrontierResult frontierResult = computeFrontierResult(filteredSelection, reverse, forward);
		
		// if the frontier result was returned null (which indicates the display
		// should not be updated) then do not update the styled result
		if(frontierResult == null){
			return null;
		}
		
		// update the canvas of each color palette
		for(ColorPalette palette : colorPalettes){
			palette.setCanvas(frontierResult.getResult());
		}
		
		// compute the styled frontier result to display
		ColorPalette palette = getActiveColorPalette();
		Markup markup = palette.getMarkup();
		FrontierStyledResult result = new FrontierStyledResult(frontierResult.getResult(), frontierResult.getFrontierReverse(), frontierResult.getFrontierForward(), markup);
		
		// highlight the selections
		result.setInput(convertedSelections);
		
		return result;
	}
	
	/**
	 * Filters the selection event to selections that the code painter responds
	 * to. Ideally any selection that a script can respond to should be included
	 * in the result or converted to a selection that is contained in the final
	 * result. If the selection is just a convenience input (for example a data
	 * flow node) that corresponds to a selection contained in the final result
	 * (say a control flow node that contains the selected data flow node) then 
	 * this method should be overridden to convert the selection to selections 
	 * contained in the final result.
	 * 
	 * @param event
	 * @return
	 */
	public Q convertSelection(Q filteredSelections){
		return filteredSelections;
	}
	
	/**
	 * Computes an unstyled frontier result for the given selection and steps
	 * reverse/forward.
	 * 
	 * @param filteredSelections
	 * @param reverse
	 * @param forward
	 * @return
	 */
	public abstract UnstyledFrontierResult computeFrontierResult(Q filteredSelections, int reverse, int forward);

	/**
	 * Computes an unstyled result for the given selection. This method is
	 * equivalent to computing a frontier result with 0 steps reverse/forward.
	 * 
	 * @param filteredSelections
	 * @return
	 */
	public UnstyledResult computeResult(Q filteredSelections) {
		return new UnstyledResult(computeFrontierResult(filteredSelections, 0, 0).getResult());
	}

}
