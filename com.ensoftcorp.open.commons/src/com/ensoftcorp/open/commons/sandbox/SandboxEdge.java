package com.ensoftcorp.open.commons.sandbox;

import com.ensoftcorp.atlas.core.db.graph.Edge;

public class SandboxEdge extends SandboxGraphElement {

	private final SandboxNode FROM;
	private final SandboxNode TO;

	/**
	 * Constructs a mirrored sandbox edge of the given Atlas edge
	 * @param edge
	 */
	public SandboxEdge(Edge edge) {
		this(edge.address().toAddressString(), true, new SandboxNode(edge.from()), new SandboxNode(edge.to()));
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
	public SandboxEdge(String address, SandboxNode from, SandboxNode to) {
		this(address, false, from, to);
	}
	
	/**
	 * Constructs a new SandboxEdge directed from the "from" node to the "to" node
	 * @param from
	 * @param to
	 */
	private SandboxEdge(String address, boolean mirror, SandboxNode from, SandboxNode to) {
		super(address, mirror);
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
	
}
