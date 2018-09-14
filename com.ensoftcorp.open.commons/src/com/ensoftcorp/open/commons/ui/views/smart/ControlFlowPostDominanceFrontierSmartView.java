package com.ensoftcorp.open.commons.ui.views.smart;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.open.commons.algorithms.DominanceAnalysis;

public class ControlFlowPostDominanceFrontierSmartView extends ControlFlowDominanceSmartView {

	@Override
	public String getTitle() {
		return "Control Flow Post Dominance Frontier";
	}
	
	@Override
	protected Q getDominanceEdges() {
		return DominanceAnalysis.getPostDominanceFrontierEdges();
	}
	
}