package com.ensoftcorp.open.commons.ui.views.codepainter.colorpalettes;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.codepainter.ColorPalette;

public class ControlFlowEdgeTypeColorPalette extends ColorPalette {

	public static final Color CONDITIONAL_TRUE_CONTROL_FLOW_COLOR = Color.WHITE;
	public static final Color CONDITIONAL_FALSE_CONTROL_FLOW_COLOR = Color.BLACK;
	public static final Color UNCONDITIONAL_CONTROL_FLOW_COLOR = Color.GRAY; // this is also the default coloring
	public static final Color EXCEPTIONAL_CONTROL_FLOW_COLOR = Color.GREEN.darker();
	public static final Color LOOPBACK_EDGE_CONTROL_FLOW_COLOR = Color.BLUE;
	
	private Map<Edge, Color> edgeColors = new HashMap<Edge,Color>();
	
	public ControlFlowEdgeTypeColorPalette(){}
	
//	@Override
//	public Markup getMarkup() {
//		// add labels for conditionValue
//		Markup m2 = new Markup(super.getMarkup()) {
//			@Override
//			public PropertySet get(GraphElement element) {
//				if (element instanceof Edge) {
//					if (element.hasAttr(XCSG.conditionValue)) {
//						return new PropertySet().set(MarkupProperty.LABEL_TEXT, ""+element.getAttr(XCSG.conditionValue)); //$NON-NLS-1$
//					}
//				}
//				return null;
//			}
//		};
//		return m2;
//	}
	
	@Override
	public void clearCanvas(){
		edgeColors.clear();
		super.clearCanvas();
	}
	
	@Override
	protected void canvasChanged() {
		edgeColors.clear();
		
		// need to expand the canvas for frontier edges
		// expand to the full function of any statements on the canvas
		Q canvasStatements = Common.toQ(canvas).nodes(XCSG.ControlFlow_Node);
		Q fullCanvas = CommonQueries.cfg(CommonQueries.getContainingFunctions(canvasStatements));
		Q controlFlowEdges = fullCanvas.edges(XCSG.ControlFlow_Edge);
		
		// color all edges gray to start, this is the default color
		// and unconditional edges will remain gray
		for(Edge edge : controlFlowEdges.eval().edges()){
			edgeColors.put(edge, UNCONDITIONAL_CONTROL_FLOW_COLOR);
		}
		
		// color the conditional true edges
		Q conditionalTrue = controlFlowEdges.selectEdge(XCSG.conditionValue, Boolean.TRUE, "true");
		for(Edge edge : conditionalTrue.eval().edges()){
			edgeColors.put(edge, CONDITIONAL_TRUE_CONTROL_FLOW_COLOR);
		}
		
		// color the conditional false edges
		Q conditionalFalse = controlFlowEdges.selectEdge(XCSG.conditionValue, Boolean.FALSE, "false");
		for(Edge edge : conditionalFalse.eval().edges()){
			edgeColors.put(edge, CONDITIONAL_FALSE_CONTROL_FLOW_COLOR);
		}

		// color the exceptional edges
		Q exceptionalControlFlow = controlFlowEdges.edges(XCSG.ExceptionalControlFlow_Edge);
		for(Edge edge : exceptionalControlFlow.eval().edges()){
			edgeColors.put(edge, EXCEPTIONAL_CONTROL_FLOW_COLOR);
		}
		
		// color the loop back edges
		Q loopbackEdge = controlFlowEdges.edges(XCSG.ControlFlowBackEdge);
		for(Edge edge : loopbackEdge.eval().edges()){
			edgeColors.put(edge, LOOPBACK_EDGE_CONTROL_FLOW_COLOR);
		}
	}

	@Override
	public String getName() {
		return "Control Flow Edge Type";
	}

	@Override
	public String getDescription() {
		return "A color scheme to differentiate conditional true/false, unconditional, and exceptional control flow paths.";
	}

	@Override
	public Map<Node, Color> getNodeColors() {
		return new HashMap<Node,Color>();
	}

	@Override
	public Map<Edge, Color> getEdgeColors() {
		return new HashMap<Edge,Color>(edgeColors);
	}

	@Override
	public Map<Color, String> getNodeColorLegend() {
		return new HashMap<Color,String>();
	}

	@Override
	public Map<Color, String> getEdgeColorLegend() {
		HashMap<Color,String> legend = new HashMap<Color,String>();
		legend.put(CONDITIONAL_TRUE_CONTROL_FLOW_COLOR, "Conditional True Control Flow");
		legend.put(CONDITIONAL_FALSE_CONTROL_FLOW_COLOR, "Conditional False Control Flow");
		legend.put(UNCONDITIONAL_CONTROL_FLOW_COLOR, "Unconditional Control Flow");
		legend.put(EXCEPTIONAL_CONTROL_FLOW_COLOR, "Exceptional Control Flow");
		legend.put(LOOPBACK_EDGE_CONTROL_FLOW_COLOR, "Loopback Control Flow");
		return legend;
	}

}
