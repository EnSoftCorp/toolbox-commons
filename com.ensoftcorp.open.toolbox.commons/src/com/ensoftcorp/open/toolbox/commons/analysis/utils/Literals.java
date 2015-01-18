package com.ensoftcorp.open.toolbox.commons.analysis.utils;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.NodeGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;

/**
 * A set of helper utilities for gathering literal values
 * 
 * @author Ben Holland
 */
public class Literals {

	private Literals() {}

	/**
	 * Returns a Q which is the union of all literal nodes in the given graph.
	 * Literals are hard-coded constants, like 5 or "foo"
	 * 
	 * from org.eclipse.jdt.core.dom, valid types of literals are
	 * org.eclipse.jdt.core.dom.StringLiteral
	 * org.eclipse.jdt.core.dom.BooleanLiteral
	 * org.eclipse.jdt.core.dom.NumberLiteral
	 * org.eclipse.jdt.core.dom.CharacterLiteral
	 * org.eclipse.jdt.core.dom.NullLiteral 
	 * org.eclipse.jdt.core.dom.TypeLiteral
	 */
	public static Q literals(Q graph, String literalType) {
		AtlasSet<GraphElement> literals = new AtlasHashSet<GraphElement>();
		for (GraphElement node : graph.nodesTaggedWithAny(Attr.Node.IS_LITERAL).eval().nodes()) {
			Object attr = node.attr().get("id");
			if (attr != null && attr.toString().contains(literalType + "Literal")){
				literals.add(node);
			}
		}
		return Common.toQ(new NodeGraph(literals));
	}

	/**
	 * Returns a Q containing all String, Boolean, Number, Character, Null and Type literals in the given graph
	 * @param graph
	 * @return
	 */
	public static Q literals(Q graph) {
		return literals(graph, "");
	}

}
