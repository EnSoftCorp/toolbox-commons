package com.ensoftcorp.open.commons.ui.views.smart;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.algorithms.DominanceAnalysis;

public class ControlFlowDominanceFrontierSmartView extends ControlFlowDominanceSmartView {

	@Override
	public String getTitle() {
		return "Control Flow Dominance Frontier";
	}
	
	@Override
	protected Q getDominanceEdges() {
		return  Common.universe().edges(DominanceAnalysis.DOMINANCE_FRONTIER_EDGE);
	}

}