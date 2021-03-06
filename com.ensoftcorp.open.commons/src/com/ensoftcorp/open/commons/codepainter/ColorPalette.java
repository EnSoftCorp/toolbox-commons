package com.ensoftcorp.open.commons.codepainter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.markup.PropertySet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

public abstract class ColorPalette extends Configurable {
	
	public static final Color SELECTION_COLOR = new Color(255,253,40);
	private static final Color[] RESERVED_COLORS = new Color[]{ SELECTION_COLOR };
	
	public static final Color[] getReservedColors(){
		Color[] colors = new Color[RESERVED_COLORS.length];
		System.arraycopy(RESERVED_COLORS, 0, colors, 0, RESERVED_COLORS.length);
		return colors;
	}
	
	/**
	 * Returns an empty color palette
	 * @return
	 */
	public static ColorPalette getEmptyColorPalette(){
		return new ColorPalette (){
			@Override
			public String getName() {
				return "Empty Color Palette";
			}

			@Override
			public String getDescription() {
				return "No applied coloring.";
			}
			
			@Override
			public Map<Node, Color> getNodeColors() {
				return new HashMap<Node,Color>();
			}

			@Override
			public Map<Edge, Color> getEdgeColors() {
				return new HashMap<Edge,Color>();
			}

			@Override
			public Map<Color, String> getNodeColorLegend() {
				return new HashMap<Color,String>();
			}

			@Override
			public Map<Color, String> getEdgeColorLegend() {
				return new HashMap<Color,String>();
			}

			@Override
			protected void canvasChanged() {}
		};
	}
	
	protected Graph canvas = Common.empty().eval();
	
	/**
	 * Clears the canvas
	 * This should be called in the event that the index changes or if graph elements become stale.
	 */
	public void clearCanvas(){
		canvas = Common.empty().eval();
	}
	
	/**
	 * Updates the canvas (nodes and edges that coloring will be applied to)
	 * This is required for color palettes that may change dynamically
	 * @param canvas
	 */
	public final void setCanvas(Q canvas){
		this.canvas = canvas.eval();
		canvasChanged();
	}
	
	protected abstract void canvasChanged();
	
	public abstract String getName();
	
	public abstract String getDescription();
	
	public final Markup getMarkup(){
		// TODO: move this to CFG specific color palettes and make getMarkup more extensible for labels
		// add labels for conditionValue
		Markup labels = new Markup() {
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge) {
					if (element.hasAttr(XCSG.conditionValue)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, element.getAttr(XCSG.conditionValue).toString()); //$NON-NLS-1$
					}
				}
				return null;
			}
		};
		
		Markup markup = new Markup(labels);
		for(Entry<Node,Color> nodeColoring : getNodeColors().entrySet()){
			Node node = nodeColoring.getKey();
			Color color = nodeColoring.getValue();
			markup.setNode(Common.toQ(node), MarkupProperty.NODE_BACKGROUND_COLOR, color);
		}
		for(Entry<Edge,Color> edgeColoring : getEdgeColors().entrySet()){
			Edge edge = edgeColoring.getKey();
			Color color = edgeColoring.getValue();
			markup.setEdge(Common.toQ(edge), MarkupProperty.EDGE_COLOR, color);
		}
		
		return markup;
	}
	
	public abstract Map<Node,Color> getNodeColors();
	
	public abstract Map<Edge,Color> getEdgeColors();
	
	public abstract Map<Color,String> getNodeColorLegend();
	
	public abstract Map<Color,String> getEdgeColorLegend();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
		ColorPalette other = (ColorPalette) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}
	
}
