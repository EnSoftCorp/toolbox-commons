package com.ensoftcorp.open.commons.highlighter;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.markup.MarkupProperty.LineStyle;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.utilities.FormattedSourceCorrespondence;

public class CFGHighlighter {

	public static final Color cfgDefault = java.awt.Color.GRAY;
	public static final Color cfgTrue = java.awt.Color.WHITE;
	public static final Color cfgFalse = java.awt.Color.BLACK;
	public static final Color cfgExceptional = java.awt.Color.BLUE;
	
	/**
	 * Adds line numbers as a prefix to nodes
	 * @param q
	 * @param m
	 */
	public static void addPrefixLineNumbers(Q q, Markup m) {
		Map<Node,String> labels = new HashMap<Node,String>();
		for(Node node : q.eval().nodes()) {
			labels.put(node, "Line: " + getLineNumber(node) + "\n");
		}
		LabelMaker.setNodePrefixLabels(m, labels);
	}
	
	private static Long getLineNumber(Node node) {
		long line = -1;
		if(node.hasAttr(XCSG.sourceCorrespondence) && node.getAttr(XCSG.sourceCorrespondence) != null) {
			try {
				line = FormattedSourceCorrespondence.getSourceCorrespondent(node).getStartLineNumber();
			} catch (IOException e) {
				line = -1;
			}
		}
		return line;
	}
	
	/**
	 * GRAY  = Unconditional ControlFlow Edge
	 * BLACK (dashed) = Conditional True ControlFlow Edge
	 * BLACK = Conditional False ControlFlow Edge
	 * BLUE  = Exceptional ControlFlow Edge
	 * @param m
	 */
	public static void applyPrintableHighlightsForCFG(Markup m) {
		Q cfEdge = Common.universe().edges(XCSG.ControlFlow_Edge);
		m.setEdge(cfEdge, MarkupProperty.EDGE_COLOR, cfgDefault);
		Q cvTrue = Common.universe().selectEdge(XCSG.conditionValue, true, Boolean.TRUE, "true");
		Q cvFalse = Common.universe().selectEdge(XCSG.conditionValue, false, Boolean.FALSE, "false");
		
		m.setEdge(cvTrue, MarkupProperty.EDGE_COLOR, Color.BLACK);
		m.setEdge(cvTrue, MarkupProperty.EDGE_STYLE, LineStyle.DASHED);
		m.setEdge(cvTrue, MarkupProperty.LABEL_TEXT, "true");
		
		m.setEdge(cvFalse, MarkupProperty.EDGE_COLOR, cfgFalse);
		m.setEdge(cvFalse, MarkupProperty.LABEL_TEXT, "false");
		
		m.setEdge(Common.universe().edges(XCSG.ExceptionalControlFlow_Edge), MarkupProperty.EDGE_COLOR, cfgExceptional);
		LoopHighlighter.applyHighlightsForLoopDepth(m);
	}
	
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
		Q cvTrue = Common.universe().selectEdge(XCSG.conditionValue, true, Boolean.TRUE, "true");
		Q cvFalse = Common.universe().selectEdge(XCSG.conditionValue, false, Boolean.FALSE, "false");
		
		m.setEdge(cvTrue, MarkupProperty.EDGE_COLOR, cfgTrue);
		m.setEdge(cvTrue, MarkupProperty.LABEL_TEXT, "true");
		
		m.setEdge(cvFalse, MarkupProperty.EDGE_COLOR, cfgFalse);
		m.setEdge(cvFalse, MarkupProperty.LABEL_TEXT, "false");
		
		m.setEdge(Common.universe().edges(XCSG.ExceptionalControlFlow_Edge), MarkupProperty.EDGE_COLOR, cfgExceptional);
		LoopHighlighter.applyHighlightsForLoopDepth(m);
	}
	
}
