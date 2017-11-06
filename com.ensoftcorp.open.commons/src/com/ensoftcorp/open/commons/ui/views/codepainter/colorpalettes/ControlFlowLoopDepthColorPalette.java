package com.ensoftcorp.open.commons.ui.views.codepainter.colorpalettes;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.codepainter.ColorPalette;

public class ControlFlowLoopDepthColorPalette extends ColorPalette {

	/** 
	 * The default blue fill color for CFG nodes
	 */
	public static final Color CONTROL_FLOW_NODE_DEFAULT_COLOR = new Color(51, 175, 243);

	private Map<Node, Color> nodeColors = new HashMap<Node,Color>();
	private HashMap<Color,String> nodeLegend = new HashMap<Color,String>();
	
	@Override
	protected void canvasChanged() {
		nodeColors.clear();
		nodeLegend.clear();
		computeLoopDepthColoring();
		for(Color shade : nodeColors.values()){
			if(shade.equals(CONTROL_FLOW_NODE_DEFAULT_COLOR)){
				nodeLegend.put(shade, "Loop Depth: 0");
			} else {
				int depth = 0;
				String displayDepth = "";
				Color color = CONTROL_FLOW_NODE_DEFAULT_COLOR.darker();
				while(!shade.equals(color)){
					depth++;
					displayDepth = "" + depth;
					color = color.darker();
					if(color.equals(Color.BLACK)){
						// can't get any darker
						displayDepth = depth + "+";
						break;
					}
				}
				nodeLegend.put(shade, "Loop Depth: " + displayDepth);
			}
		}
	}

	@Override
	public String getName() {
		return "Control Flow Loop Depth Color Palette";
	}

	@Override
	public String getDescription() {
		return "A color scheme to shade loop headers and loop children successively darker corresponding to their depth.";
	}

	@Override
	public Map<Node, Color> getNodeColors() {
		return new HashMap<Node,Color>(nodeColors);
	}

	@Override
	public Map<Edge, Color> getEdgeColors() {
		return new HashMap<Edge,Color>();
	}

	@Override
	public Map<Color, String> getNodeColorLegend() {
		return new HashMap<Color,String>(nodeLegend);
	}

	@Override
	public Map<Color, String> getEdgeColorLegend() {
		return new HashMap<Color,String>();
	}

	/**
	 * Computes colors for loops and loop children. Nodes are colored a darker color
	 * than the normal CFG color, that depend on the nesting depth of the loop
	 * header.
	 * 
	 * @param cfg
	 * @return
	 */
	private void computeLoopDepthColoring() {
		// to make the coloring consistent for any selection we will compute
		// colors for the full function of any statements on the canvas
		Q canvasStatements = Common.toQ(canvas).nodes(XCSG.ControlFlow_Node);
		Q fullCanvas = CommonQueries.cfg(CommonQueries.getContainingFunctions(canvasStatements));
		AtlasSet<Node> loopHeaders = fullCanvas.nodesTaggedWithAll(XCSG.Loop).eval().nodes();
		Q loopChildEdges = Common.universe().edges(XCSG.LoopChild).retainEdges();
		
		Map<Node, Color> colorMap = new HashMap<Node, Color>();
		for (Node loopHeader : loopHeaders) {
			Color color = computeLoopDepthColoring(colorMap, loopHeader);
			nodeColors.put(loopHeader, color);
		}
		
		// set color of loop members (other than loop headers) to same color as header 
		Q loopFragments = loopChildEdges.retainNodes().difference(Common.toQ(loopHeaders)).intersection(canvasStatements);
		for (Node member : loopFragments.eval().nodes()) {
			Node loopHeader = loopChildEdges.predecessors(Common.toQ(member)).eval().nodes().one();
			nodeColors.put(member, colorMap.get(loopHeader));
		}
	}

	private Color computeLoopDepthColoring(Map<Node, Color> colorMap, Node loopHeader) {
		Q loopChildEdges = Common.universe().edges(XCSG.LoopChild).retainEdges();
		Color color = colorMap.get(loopHeader);
		if (color == null) {
			Node parentLoopHeader = loopChildEdges.predecessors(Common.toQ(loopHeader)).eval().nodes().one();
			if(parentLoopHeader == null){
				// loop is not nested
				color = CONTROL_FLOW_NODE_DEFAULT_COLOR;
			} else {
				color = computeLoopDepthColoring(colorMap, parentLoopHeader);
				color = color.darker();
			}
			colorMap.put(loopHeader, color);
		}
		return color;
	}
	
}
