package com.ensoftcorp.open.commons.codemap;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasNodeHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.xcsg.Toolbox;

public class LoopDepth extends PrioritizedCodemapStage {
	/**
	 * The unique identifier for the codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.loopdepth"; //$NON-NLS-1$

	@Override
	public String getDisplayName() {
		return "Computing Loop Depth"; //$NON-NLS-1$
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		
		// TODO: Does this depend on DLI executing first??? ~BH
		// If so this may need to be moved down to accompany loop detection algorithms
		
		return new String[]{};
	}

	@Override
	public boolean performIndexing(IProgressMonitor monitor) {
		Q loopHeaders = Query.universe().nodes(XCSG.Loop).nodes(XCSG.Language.C, XCSG.Language.CPP, XCSG.Language.Java).difference(Query.universe().nodes(XCSG.Language.Jimple));
		Q edges = Query.universe().edges(XCSG.LoopChild);
		Q loops = loopHeaders.reverseStepOn(edges);
		Q qlevel1LoopHeaders = loops.roots();
		AtlasSet<Node> level1LoopHeaders = new AtlasNodeHashSet(qlevel1LoopHeaders.eval().nodes());
		for (Node loopHeader : level1LoopHeaders) {
			recordLoopDepth(loopHeader, 1);
		}
		return true;
	}

	private void recordLoopDepth(Node loopHeader, int depth) {
		loopHeader.putAttr(Toolbox.loopDepth, depth);
		AtlasSet<Edge> loopChildren = loopHeader.out(XCSG.LoopChild);
		for (Edge loopChild : loopChildren) {
			Node member = loopChild.to();
			if (member.taggedWith(XCSG.Loop)) {
				recordLoopDepth(member, depth+1);
			} else {
				member.putAttr(Toolbox.loopDepth, depth);
			}
		}
	}

}
