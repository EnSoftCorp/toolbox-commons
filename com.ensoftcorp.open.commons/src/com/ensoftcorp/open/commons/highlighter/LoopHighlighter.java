package com.ensoftcorp.open.commons.highlighter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.log.Log;

public class LoopHighlighter {

	/** 
	 * The default blue fill color for CFG nodes
	 */
	public static final Color cfgNodeFillColor = new Color(51, 175, 243);

	/**
	 * Adds markup for loops and loop children. Nodes are colored a darker color
	 * than the normal CFG color, that depend on the nesting depth of the loop
	 * header.
	 * 
	 * @param cfg
	 * @return
	 */
	public static Markup applyHighlightsForLoopDepth(Markup m) {
		Q loopChildEdges =  Common.universe().edges(XCSG.LoopChild);
		Q loopHeadersQ = Common.universe().nodesTaggedWithAll(XCSG.Loop);
		AtlasSet<Node> loopHeaders = loopHeadersQ.eval().nodes();
		
		Map<Node, Color> colorMap = new HashMap<Node, Color>();
		for (Node loopHeader : loopHeaders) {
			Color color = applyHighlightsForLoopDepth(colorMap, loopHeader);
			m.set(loopHeader, MarkupProperty.NODE_BACKGROUND_COLOR, color);
		}
		
		// set color of loop members (other than loop headers) to same color as header 
		Q loopChildren = loopChildEdges.successors(loopHeadersQ);
		for(Node child : loopChildren.eval().nodes()) {
			Node loopHeader = loopChildEdges.predecessors(Common.toQ(child)).eval().nodes().one();
			m.set(child, MarkupProperty.NODE_BACKGROUND_COLOR, colorMap.get(loopHeader));
		}
		return m;
	}

	private static Color applyHighlightsForLoopDepth(Map<Node, Color> colorMap, Node loopHeader) {
		Q loopChildEdges =  Common.universe().edges(XCSG.LoopChild);
		Color color = colorMap.get(loopHeader);
		if (color == null) {
			Node parentLoopHeader = loopChildEdges.predecessors(Common.toQ(loopHeader)).eval().nodes().one();
			if (parentLoopHeader == null) {
				// loop is not nested
				color = cfgNodeFillColor;
			} else {
				color = applyHighlightsForLoopDepth(colorMap, parentLoopHeader);
				color = color.darker();
			}
			colorMap.put(loopHeader, color);
		}
		return color;
	}
}
