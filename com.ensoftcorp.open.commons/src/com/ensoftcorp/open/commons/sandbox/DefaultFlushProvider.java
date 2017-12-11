package com.ensoftcorp.open.commons.sandbox;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

public class DefaultFlushProvider implements FlushProvider {

	/**
	 * Flushes the changes made in the sandbox that are restricted to the nodes
	 * and edges in the given graph to the Atlas graph
	 * 
	 * This methods does the following:
	 * 
	 * 1) Adds nodes/edges that are not mirrored to the Atlas graph with the
	 * current tags/attributes
	 * 
	 * 2) Updates (adds/removes) tags and attributes from the corresponding
	 * Atlas graph element's to match the current sandbox tags/attributes for
	 * each node/edge in the sandbox.
	 * 
	 * @param graph the graph containing the set of nodes and edges to flush
	 * 
	 * @return The serialized Atlas graph version of the sandbox
	 */
	@Override
	public Graph flush(SandboxGraph graph, Map<String,SandboxGraphElement> addresses) {
		AtlasSet<Node> nodes = new AtlasHashSet<Node>();
		for(SandboxNode node : graph.nodes()){
			nodes.add((Node) flush(node, addresses));
		}
		AtlasSet<Edge> edges = new AtlasHashSet<Edge>();
		for(SandboxEdge edge : graph.edges()){
			edges.add((Edge) flush(edge, addresses));
		}
		return new UncheckedGraph(nodes, edges);
	}
	
	/**
	 * Flushes the changes made or creation of a sandbox graph element to the
	 * Atlas graph and updates the address map accordingly
	 * 
	 * @param ge
	 * @return
	 */
	public GraphElement flush(SandboxGraphElement ge, Map<String,SandboxGraphElement> addresses) {
		if(!ge.isMirror()){
			if(ge instanceof SandboxNode){
				Node node = Graph.U.createNode();
				// add all the sandbox tags
				for(String tag : ge.tags()){
					node.tag(tag);
				}
				// add all new sandbox attributes
				for(String key : ge.attr().keySet()){
					node.putAttr(key, ge.attr().get(key));
				}
				addresses.remove(ge.getAddress());
				ge.flush(node.address().toAddressString());
				addresses.put(ge.getAddress(), ge);
				return node;
			} else if(ge instanceof SandboxEdge){
				SandboxEdge sandboxEdge = (SandboxEdge) ge;
				// assert: nodes will all have been flushed by the time we are flushing edges
				Node from = (Node) CommonQueries.getGraphElementByAddress(sandboxEdge.from().getAddress());
				Node to = (Node) CommonQueries.getGraphElementByAddress(sandboxEdge.to().getAddress());
				Edge edge = Graph.U.createEdge(from, to);
				// add all the sandbox tags
				for(String tag : ge.tags()){
					edge.tag(tag);
				}
				// add all new sandbox attributes
				for(String key : ge.attr().keySet()){
					edge.putAttr(key, ge.attr().get(key));
				}
				addresses.remove(ge.getAddress());
				ge.flush(edge.address().toAddressString());
				addresses.put(ge.getAddress(), ge);
				return edge;
			} else {
				throw new RuntimeException("Unknown sandbox graph element type.");
			}
		} else {
			GraphElement age = CommonQueries.getGraphElementByAddress(ge.getAddress());
			
			// purge all old tags
			Set<String> tagsToRemove = new HashSet<String>();
			for(String tag : age.tags()){
				tagsToRemove.add(tag);
			}
			for(String tag : tagsToRemove){
				age.tags().remove(tag);
			}
			
			// add all the sandbox tags
			for(String tag : ge.tags()){
				age.tag(tag);
			}
			
			// purge all old attributes
			Set<String> keysToRemove = new HashSet<String>();
			for(String key : age.attr().keys()){
				keysToRemove.add(key);
			}
			for(String key : keysToRemove){
				age.attr().remove(key);
			}
			
			// add all new sandbox attributes
			for(String key : ge.attr().keySet()){
				age.putAttr(key, ge.attr().get(key));
			}
			
			return age;
		}
	}

}
