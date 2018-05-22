package com.ensoftcorp.open.commons.highlighter;

import java.awt.Color;
import java.util.Arrays;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.markup.IMarkup;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.markup.MarkupProperty;
import com.ensoftcorp.atlas.core.markup.PropertySet;
import com.ensoftcorp.atlas.core.markup.UnionMarkup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;

public class WhitePaperThemedCFG {

	public static final Color cfgDefault = java.awt.Color.GRAY;
	public static final Color cfgTrue = java.awt.Color.WHITE;
	public static final Color cfgFalse = java.awt.Color.BLACK;
	public static final Color cfgExceptional = java.awt.Color.BLUE;
	public static final Color pcgEvent = java.awt.Color.YELLOW;
	public static final Color ipcgMaster = java.awt.Color.GRAY;
	
	/**
	 * GRAY  = Unconditional ControlFlow Edge
	 * WHITE = Conditional True ControlFlow Edge
	 * BLACK = Conditional False ControlFlow Edge
	 * BLUE  = Exceptional ControlFlow Edge
	 * @param m
	 */
	public static void applyHighlightsForCFEdges(Markup m) {
		Q cfEdge = Common.universe().edges(XCSG.ControlFlow_Edge);
		m.setEdge(cfEdge, MarkupProperty.EDGE_COLOR, cfgDefault);
		Q cvTrue = Common.universe().selectEdge(XCSG.conditionValue, true, Boolean.TRUE, "true");
		Q cvFalse = Common.universe().selectEdge(XCSG.conditionValue, false, Boolean.FALSE, "false");
		m.setEdge(cvTrue, MarkupProperty.EDGE_COLOR, cfgTrue);
		m.setEdge(cvFalse, MarkupProperty.EDGE_COLOR, cfgFalse);
		m.setEdge(Common.universe().edges(XCSG.ControlFlowBackEdge), MarkupProperty.EDGE_COLOR, cfgExceptional);
	}
	
	public static final Markup GREYSCALE_MARKUP = new Markup() {
		private final Color NODE_COLOR = new Color(130, 130, 130);
		private final Color FOLDER_COLOR = new Color(170, 170, 170, 50);
		private final Color DATAFLOW_COLOR = new Color(110, 110, 110);
		private final Color CONTROLFLOW_COLOR = new Color(150, 150, 150);
		private final Color SHADOW_COLOR = new Color(0, 0, 0, 50);
		
		private final PropertySet NODES = new PropertySet().set(MarkupProperty.NODE_BACKGROUND_COLOR, NODE_COLOR)
				.set(MarkupProperty.NODE_GROUP_COLOR, FOLDER_COLOR)
				.set(MarkupProperty.NODE_BORDER_COLOR, MarkupProperty.Colors.BLACK)
				.set(MarkupProperty.NODE_SHADOW_COLOR, SHADOW_COLOR);
		private final PropertySet DATAFLOW = new PropertySet().set(MarkupProperty.NODE_BACKGROUND_COLOR, DATAFLOW_COLOR)
				.set(MarkupProperty.NODE_GROUP_COLOR, FOLDER_COLOR)
				.set(MarkupProperty.NODE_BORDER_COLOR, MarkupProperty.Colors.BLACK)
				.set(MarkupProperty.NODE_SHADOW_COLOR, SHADOW_COLOR);
		private final PropertySet CONTROLFLOW = new PropertySet()
				.set(MarkupProperty.NODE_BACKGROUND_COLOR, CONTROLFLOW_COLOR)
				.set(MarkupProperty.NODE_GROUP_COLOR, FOLDER_COLOR)
				.set(MarkupProperty.NODE_BORDER_COLOR, MarkupProperty.Colors.BLACK)
				.set(MarkupProperty.NODE_SHADOW_COLOR, SHADOW_COLOR);

		@Override
		public PropertySet get(GraphElement element) {
			if (element.taggedWith(XCSG.ControlFlow_Node) /*|| element.taggedWith(PCG.PCGNode.EventFlow_Master_Entry) || element.taggedWith(PCG.PCGNode.EventFlow_Master_Exit)*/) {
				return CONTROLFLOW;
			}
			if (element.taggedWith(XCSG.DataFlow_Node)) {
				return DATAFLOW;
			}
			if (element.taggedWith(XCSG.Node)) {
				return NODES;
			}

			return new PropertySet();
		}
	};
	
//	public static Q cfgWithEntryExitNodes(Q function) {
//		Q cfg = CommonQueries.cfg(function);
//		PCG pcg = PCGFactory.create(cfg, cfg.nodes(XCSG.controlFlowRoot), cfg.nodes(XCSG.controlFlowExitPoint), cfg.nodes(XCSG.ControlFlow_Node));
//		return pcg.getPCG();
//	}
	
//	public static void create(Q function, Q selected) {
//		Q cfg = cfgWithEntryExitNodes(function);
//		IMarkup markup = markup(cfg, selected);
//		cfg = cfg.union(Common.universe().edges(XCSG.Contains).reverse(cfg));
//		DisplayUtil.displayGraph(markup, cfg.eval(), function.eval().nodes().one().getAttr(XCSG.name).toString());
//	}
	
//	public static void createPCG(Q function, Q selected) {
//		//Q cfg = cfgWithEntryExitNodes(function);
//		PCG pcg = PCGFactory.create(selected);
//		Q pcgQ = pcg.getPCG();
//		IMarkup markup = markup(pcgQ, selected);
//		pcgQ = pcgQ.union(Common.universe().edges(XCSG.Contains).reverse(pcgQ));
//		DisplayUtil.displayGraph(markup, pcgQ.eval(), function.eval().nodes().one().getAttr(XCSG.name).toString());
//	}
	
	public static void createAcyclic(Q function, Q selected) {
		Q cfg = CommonQueries.cfg(function);
		cfg = cfg.differenceEdges(cfg.edges(XCSG.ControlFlowBackEdge));
		IMarkup markup = markup(cfg, selected);
		cfg = cfg.union(Common.universe().edges(XCSG.Contains).reverse(cfg));
		DisplayUtils.show(cfg, markup, true, function.eval().nodes().one().getAttr(XCSG.name).toString());
	}
	
	public static IMarkup markup(Q cfg) {
		Markup m = new Markup();
		Q cfEdge = cfg.edges(XCSG.ControlFlow_Edge);
		m.setEdge(cfEdge, MarkupProperty.EDGE_COLOR, cfgDefault);
		// highlight control flow edges
		applyHighlightsForCFEdges(m);
		return new UnionMarkup(Arrays.asList(m, GREYSCALE_MARKUP));
	}
	
	public static IMarkup markup(Q cfg, Q selected) {
		
		// labels for conditionValue
		Markup m2 = new Markup() {
			
			@Override
			public PropertySet get(GraphElement element) {
				if (element instanceof Edge) {
					if (element.taggedWith(XCSG.ControlFlow_Edge) && element.hasAttr(XCSG.conditionValue)) {
						return new PropertySet().set(MarkupProperty.LABEL_TEXT, ""+element.getAttr(XCSG.conditionValue)); //$NON-NLS-1$
					}
				}
				if(element instanceof Node) {
					if(element.taggedWith(XCSG.ControlFlow_Node)) {
						SourceCorrespondence sc = (SourceCorrespondence)element.getAttr(XCSG.sourceCorrespondence);
						if(sc != null) {
							return new PropertySet().set(MarkupProperty.LABEL_TEXT, (sc.startLine) + ": " +element.getAttr(XCSG.name));
						}
						
					}
				}
				return null;
			}
		};

		Markup m = new Markup(m2);

		// treat event flow edges as control flow edges
		Q cfEdge = cfg.edges(XCSG.ControlFlow_Edge);
		m.setEdge(cfEdge, MarkupProperty.EDGE_COLOR, cfgDefault);
		m.setNode(selected, MarkupProperty.NODE_BACKGROUND_COLOR, pcgEvent);
		// highlight control flow edges
		applyHighlightsForCFEdges(m);
		return new UnionMarkup(Arrays.asList(m, GREYSCALE_MARKUP));
	}
}
