package com.ensoftcorp.open.commons.codemap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.ICFG;
import com.ensoftcorp.open.commons.algorithms.ICFG.CallResolutionStrategy;
import com.ensoftcorp.open.commons.algorithms.ICFG.DefaultCallResolutionStrategy;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;

public class ICFGConstruction extends PrioritizedCodemapStage {
	
	/**
	 * The unique identifier for the codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.codemap.icfg"; //$NON-NLS-1$

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		// technically this could depend on the call graph toolbox indexers, but that is
		// not implemented currently
		return new String[] {};
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		boolean runIndexer = CommonsPreferences.isConstructICFGEnabled();
		if(runIndexer) {
			CallResolutionStrategy callResolutionStrategy = new DefaultCallResolutionStrategy();
			Log.info("Constructing inter-procedural control flow graphs using " + callResolutionStrategy.getStrategyTagName() + " call resolution strategy...");
			// construct the ICFG for the entire index
			for(Node cgRoot : callResolutionStrategy.getCallGraphRoots()) {
				if(!CommonQueries.isEmpty(Common.toQ(cgRoot).children().nodes(XCSG.ControlFlow_Node))) {
					new ICFG(cgRoot, Common.empty().eval().nodes(), callResolutionStrategy);
				}
			}
		}
		return runIndexer;
	}

	@Override
	public String getDisplayName() {
		return "Interprocedural Control Flow Graph Construction";
	}
	
}
