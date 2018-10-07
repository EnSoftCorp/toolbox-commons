package com.ensoftcorp.open.commons.ui.views.smart;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.commons.algorithms.DominanceAnalysis;

public class ControlFlowDominanceTreeSmartView extends ControlFlowDominanceSmartView {

	@Override
	public String getTitle() {
		return "Control Flow Dominance Tree";
	}
	
	@Override
	protected Q getDominanceEdges() {
		return DominanceAnalysis.getDominatorTreeEdges();
	}
	
}