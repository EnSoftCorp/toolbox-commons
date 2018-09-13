package com.ensoftcorp.open.commons.ui.views.smart;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.algorithms.DominanceAnalysis;

public class ControlFlowDominanceTreeSmartView extends ControlFlowDominanceSmartView {

	@Override
	public String getTitle() {
		return "Control Flow Dominance Tree";
	}
	
	@Override
	protected Q getDominanceEdges() {
		return Common.universe().edges(DominanceAnalysis.IMMEDIATE_FORWARD_DOMINANCE_EDGE);
	}
	
}