package com.ensoftcorp.open.commons.sandbox;

import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Graph;

public interface FlushProvider {

	/**
	 * Flushes the changes made in the sandbox that are restricted to the nodes
	 * and edges in the given graph to the Atlas graph and updates the address map
	 * accordingly.
	 * 
	 * @param graph the graph containing the set of nodes and edges to flush
	 * 
	 * @return The serialized Atlas graph version of the sandbox
	 */
	public Graph flush(SandboxGraph graph, Map<String,SandboxGraphElement> addresses);
	
}
