package com.ensoftcorp.open.commons.ui.views.codepainter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.markup.Markup;

public class EmptyColorPalette extends ColorPalette {

	@Override
	public String getName() {
		return "Empty Color Palette";
	}

	@Override
	public String getDescription() {
		return "No applied coloring.";
	}

	@Override
	public IMarkup getMarkup() {
		return new Markup();
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

}
