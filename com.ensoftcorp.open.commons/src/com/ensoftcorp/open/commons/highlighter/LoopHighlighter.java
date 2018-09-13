package com.ensoftcorp.open.commons.highlighter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.open.commons.xcsg.Toolbox;

public class LoopHighlighter {

	/** 
	 * The default blue fill color for CFG nodes
	 */
	private static final Color cfgNodeFillColor = new Color(51, 175, 243);
	private static List<Color> depthToColor = new ArrayList<>();
	static {
		Color color = cfgNodeFillColor;
		int MAX_DEPTH = 11;
		for (int i=0; i<MAX_DEPTH; i++) {
			depthToColor.add(color);
			color = color.darker();
		}
	}

	/**
	 * Adds markup for loops and loop children. Nodes are colored a darker color
	 * than the normal CFG color, that depend on the nesting depth of the loop
	 * header.
	 * 
	 * @param cfg
	 * @return
	 */
	public static Markup applyHighlightsForLoopDepth(Markup m) {
		for (int i=1; i<depthToColor.size(); i++) {
			Q depth = Query.universe().selectNode(Toolbox.loopDepth, i);
			m.setNode(depth, MarkupProperty.NODE_BACKGROUND_COLOR, depthToColor.get(i));
		}
		return m;
	}

}
