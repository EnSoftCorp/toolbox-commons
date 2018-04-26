package com.ensoftcorp.open.commons.highlighter;

import java.awt.Color;

import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

public class CFGHighlighter {

	public static final Color cfgDefault = java.awt.Color.GRAY;
	public static final Color cfgTrue = java.awt.Color.WHITE;
	public static final Color cfgFalse = java.awt.Color.BLACK;
	public static final Color cfgExceptional = java.awt.Color.BLUE;
	
	/**
	 * GRAY  = Unconditional ControlFlow Edge
	 * WHITE = Conditional True ControlFlow Edge
	 * BLACK = Conditional False ControlFlow Edge
	 * BLUE  = Exceptional ControlFlow Edge
	 * @param m
	 */
	public static void applyHighlightsForCFG(Markup m) {
		Q cfEdge = Common.universe().edges(XCSG.ControlFlow_Edge);
		m.setEdge(cfEdge, MarkupProperty.EDGE_COLOR, cfgDefault);
		Q cvTrue = Common.universe().selectEdge(XCSG.conditionValue, Boolean.TRUE, "true");
		Q cvFalse = Common.universe().selectEdge(XCSG.conditionValue, Boolean.FALSE, "false");
		
		m.setEdge(cvTrue, MarkupProperty.EDGE_COLOR, cfgTrue);
//		m.setEdge(cvTrue, MarkupProperty.EDGE_COLOR, Color.BLACK);
//		m.setEdge(cvTrue, MarkupProperty.EDGE_STYLE, LineStyle.DASHED);
		m.setEdge(cvTrue, MarkupProperty.LABEL_TEXT, "true");
		
		m.setEdge(cvFalse, MarkupProperty.EDGE_COLOR, cfgFalse);
		m.setEdge(cvFalse, MarkupProperty.LABEL_TEXT, "false");
		
		m.setEdge(Common.universe().edges(XCSG.ExceptionalControlFlow_Edge), MarkupProperty.EDGE_COLOR, cfgExceptional);
		LoopHighlighter.applyHighlightsForLoopDepth(m);
	}
	
}
