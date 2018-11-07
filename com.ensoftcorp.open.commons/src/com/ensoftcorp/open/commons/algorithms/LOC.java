package com.ensoftcorp.open.commons.algorithms;

import java.util.ArrayList;
import java.util.Collections;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

/**
 * Utilities for counting lines of code in a function
 * 
 * @author Sharwan Ram
 */
public class LOC {

	public static Integer getLOC(Q functions) {
		return getLOC(functions.eval().nodes().one());
	}

	public static Integer getLOC(Node node) {
		int startLine = LOC.getStartingLineNumber(node);
		int endLine = LOC.getLastLineNumber(node);
		int TotalLOC = endLine - startLine + 1;
		return TotalLOC;
	}

	private static Integer getStartingLineNumber(Node node) {
		int lineNumber = -1;
		if (node.hasAttr(XCSG.sourceCorrespondence)) {
			SourceCorrespondence sc = (SourceCorrespondence) node.getAttr(XCSG.sourceCorrespondence);
			if (sc != null) {
				lineNumber = sc.startLine;
			}
		}
		return lineNumber;
	}

	private static Integer getLastLineNumber(Node node) {
		ArrayList<Integer> lineNumbers = new ArrayList<Integer>();
		for (Node n : CommonQueries.cfg(node).leaves().eval().nodes()) {
			lineNumbers.add(lastLineNumber(n));
		}
		return Collections.max(lineNumbers) + 1;
	}

	private static Integer lastLineNumber(Node node) {
		int lineNumber = -1;
		if (node.hasAttr(XCSG.sourceCorrespondence)) {
			SourceCorrespondence sc = (SourceCorrespondence) node.getAttr(XCSG.sourceCorrespondence);
			if (sc != null) {

				lineNumber = sc.startLine;
			}
		} else {
			lineNumber = predecessorLineNumber(node);
		}
		return lineNumber;
	}

	private static Integer predecessorLineNumber(Node node) {
		ArrayList<Integer> lineNumbers = new ArrayList<Integer>();
		for (Node n : CommonQueries.cfg(CommonQueries.getContainingFunction(node)).predecessors(Common.toQ(node)).eval()
				.nodes()) {
			lineNumbers.add(lastLineNumber(n));
		}
		return Collections.max(lineNumbers);
	}
}
