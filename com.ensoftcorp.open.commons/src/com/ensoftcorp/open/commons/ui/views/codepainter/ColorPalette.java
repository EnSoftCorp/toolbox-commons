package com.ensoftcorp.open.commons.ui.views.codepainter;

import java.awt.Color;
import java.util.Map;
import java.util.Map.Entry;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.script.Common;

public abstract class ColorPalette {
	
	public abstract String getName();
	
	public abstract String getDescription();
	
	public IMarkup getMarkup(){
		Markup markup = new Markup();
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
