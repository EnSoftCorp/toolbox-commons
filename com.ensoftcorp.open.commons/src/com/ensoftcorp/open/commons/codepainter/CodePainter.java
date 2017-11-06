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
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IExplorableScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.scripts.util.SimpleScriptUtil;
import com.ensoftcorp.atlas.ui.selection.event.FrontierEdgeExploreEvent;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;

public abstract class CodePainter extends FilteringAtlasSmartViewScript implements IResizableScript, IExplorableScript {
	
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
	
	/**
	 * Defines strategies for resolving coloring conflicts
	 */
	public static enum ColorPaletteConflictStrategy {
		CHOOSE_FIRST_MATCH, CHOOSE_LAST_MATCH, MIX_COLORS
	}
	
	protected ArrayList<ColorPalette> appliedColorPalettes = new ArrayList<ColorPalette>();
	protected ColorPaletteConflictStrategy conflictStrategy = ColorPaletteConflictStrategy.CHOOSE_FIRST_MATCH;
	
	public void setColorPaletteConflictStrategy(ColorPaletteConflictStrategy conflictStrategy){
		this.conflictStrategy = conflictStrategy;
	}
	
	/**
	 * The computation specific coloring scheme. If there is no computation specific coloring then null may be returned.
	 * @return
	 */
	public abstract ColorPalette getComputationSpecificColorPalette();
	
	protected ColorPalette getActiveColorPalette(){
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
				ColorPalette baseColorPalette = getComputationSpecificColorPalette() != null ? getComputationSpecificColorPalette() : ColorPalette.getEmptyColorPalette();
				ArrayList<ColorPalette> colorPalettes = getAppliedColorPalettes();
				ColorPaletteConflictStrategy currentConflictStrategy = conflictStrategy;
				
				// compute node colorings for the active color palettes
				Map<Node, Color> nodeColors = new HashMap<Node, Color>();
				Map<Node, List<Color>> nodeColorsToMix = new HashMap<Node, List<Color>>();
				nodeColors.putAll(baseColorPalette.getNodeColors());
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
								if(baseColorPalette.getNodeColors().containsKey(node)){
									colors.add(baseColorPalette.getNodeColors().get(node));
								}
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
				ColorPalette baseColorPalette = getComputationSpecificColorPalette() != null ? getComputationSpecificColorPalette() : ColorPalette.getEmptyColorPalette();
				ArrayList<ColorPalette> colorPalettes = getAppliedColorPalettes();
				ColorPaletteConflictStrategy currentConflictStrategy = conflictStrategy;
				
				// compute edge colorings for the active color palettes
				Map<Edge, Color> edgeColors = new HashMap<Edge, Color>();
				Map<Edge, List<Color>> edgeColorsToMix = new HashMap<Edge, List<Color>>();
				edgeColors.putAll(baseColorPalette.getEdgeColors());
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
								if(baseColorPalette.getEdgeColors().containsKey(edge)){
									colors.add(baseColorPalette.getEdgeColors().get(edge));
								}
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
				ColorPalette baseColorPalette = getComputationSpecificColorPalette() != null ? getComputationSpecificColorPalette() : ColorPalette.getEmptyColorPalette();
				ArrayList<ColorPalette> colorPalettes = getAppliedColorPalettes();
				ColorPaletteConflictStrategy currentConflictStrategy = conflictStrategy;
				
				// compute color legend for the active color palettes
				Map<Color, String> legend = new HashMap<Color, String>();
				legend.putAll(baseColorPalette.getNodeColorLegend());
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
				ColorPalette baseColorPalette = getComputationSpecificColorPalette() != null ? getComputationSpecificColorPalette() : ColorPalette.getEmptyColorPalette();
				ArrayList<ColorPalette> colorPalettes = getAppliedColorPalettes();
				ColorPaletteConflictStrategy currentConflictStrategy = conflictStrategy;
				
				// compute color legend for the active color palettes
				Map<Color, String> legend = new HashMap<Color, String>();
				legend.putAll(baseColorPalette.getEdgeColorLegend());
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
	
	protected ArrayList<ColorPalette> getAppliedColorPalettes(){
		return new ArrayList<ColorPalette>(appliedColorPalettes);
	}
	
	public void addColorPalette(ColorPalette palette){
		if(palette != null){
			ColorPalette baseColorPalette = getComputationSpecificColorPalette() != null ? getComputationSpecificColorPalette() : ColorPalette.getEmptyColorPalette();
			if(!baseColorPalette.equals(palette)){
				appliedColorPalettes.add(palette);
			}
		}
	}
	
	public void addColorPalette(ColorPalette palette, int index){
		if(palette != null){
			ColorPalette baseColorPalette = getComputationSpecificColorPalette() != null ? getComputationSpecificColorPalette() : ColorPalette.getEmptyColorPalette();
			if(!baseColorPalette.equals(palette)){
				appliedColorPalettes.add(index, palette);
			}
		}
	}
	
	public void removeColorPalette(ColorPalette palette){
		appliedColorPalettes.remove(palette);
	}
	
	public void removeColorPalette(int index){
		appliedColorPalettes.remove(index);
	}
	
	public abstract String getTitle();
	
	/**
	 * Defines the category this code painter is classified under.
	 * Optionally categories can be qualified for multiple levels with "/"
	 * @return
	 */
	public abstract String getCategory();

	protected abstract String[] getSupportedNodeTags();
	
	protected abstract String[] getSupportedEdgeTags();

	public abstract int getDefaultStepTop();

	public abstract int getDefaultStepBottom();
	
	public FrontierStyledResult explore(FrontierEdgeExploreEvent event, FrontierStyledResult oldResult) {
		return SimpleScriptUtil.explore(this, event, oldResult);
	}

	/**
	 * Computes a new styled frontier result for a given selection event and the
	 * number of steps forward and reverse to explore on the frontier.
	 */
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward){
		Q convertedSelections = convertSelection(filter(event));
		UnstyledFrontierResult frontierResult = computeFrontierResult(filter(event), reverse, forward);
		if(frontierResult == null){
			return null;
		}
		
		// update the canvas each color palette will be applied to
		if(this.getComputationSpecificColorPalette() != null){
			this.getComputationSpecificColorPalette().setCanvas(frontierResult.getResult());
		}
		for(ColorPalette palette : appliedColorPalettes){
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
	 * Computes a new styled result for a given selection event.
	 */
	protected final StyledResult selectionChanged(IAtlasSelectionEvent event, Q filteredSelection){
		// this is dead code, but exists to satisfy interfaces
		return null;
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
	
	public abstract UnstyledFrontierResult computeFrontierResult(Q filteredSelections, int reverse, int forward);

	public UnstyledResult computeResult(Q filteredSelections) {
		return new UnstyledResult(computeFrontierResult(filteredSelections, 0, 0).getResult());
	}

}
