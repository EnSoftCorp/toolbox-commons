package com.ensoftcorp.open.commons.ui.views.smart;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.commons.algorithms.DominanceAnalysis;

public class ControlFlowDominanceFrontierSmartView extends ControlFlowDominanceSmartView {

	@Override
	public String getTitle() {
		return "Control Flow Dominance Frontier";
	}
	
	@Override
	protected Q getDominanceEdges() {
		return DominanceAnalysis.getDominanceFrontierEdges();
	}
	
}