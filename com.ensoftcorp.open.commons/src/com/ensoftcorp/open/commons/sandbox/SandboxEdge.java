package com.ensoftcorp.open.commons.sandbox;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

public class SandboxEdge extends SandboxGraphElement {

	private final SandboxNode FROM;
	private final SandboxNode TO;

	/**
	 * Constructs a mirrored sandbox edge of the given Atlas edge
	 * @param edge
	 */
	public SandboxEdge(int sandboxInstanceID, Edge edge, SandboxNode from, SandboxNode to) {
		this(sandboxInstanceID, Sandbox.addrStr(edge), from, to);
		for(String tag : edge.tags()){
			tags().add(tag);
		}
		for(String key : edge.attr().keys()){
			attr().put(key, edge.getAttr(key));
		}
	}
	
	/**
	 * Constructs a new SandboxEdge directed from the "from" node to the "to" node
	 * @param from
	 * @param to
	 */
	public SandboxEdge(int sandboxInstanceID, String address, SandboxNode from, SandboxNode to) {
		super(sandboxInstanceID, address);
		FROM = from;
		TO = to;
	}

	/**
	 * Returns the node this edge is directed from
	 * @return
	 */
	public SandboxNode from(){
		return FROM;
	}
	
	/**
	 * Returns the node this edge is directed to
	 * @return
	 */
	public SandboxNode to(){
		return TO;
	}
	
	/**
	 * Returns the node corresponding to the given direction
	 * @param direction
	 * @return
	 */
	public SandboxNode getNode(EdgeDirection direction){
		if(direction == EdgeDirection.FROM){
			return FROM;
		} else {
			return TO;
		}
	}
	
	public Edge toAtlasEdge(){
		if(isMirror()){
			return CommonQueries.getEdgeByAddress(address);
		} else {
			return null;
		}
	}
	
}
