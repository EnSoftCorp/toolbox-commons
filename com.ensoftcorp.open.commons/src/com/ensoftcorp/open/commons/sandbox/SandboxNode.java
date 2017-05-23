package com.ensoftcorp.open.commons.sandbox;

import com.ensoftcorp.atlas.core.db.graph.Node;

public class SandboxNode extends SandboxGraphElement {

	public SandboxNode(int sandboxInstanceID, Node node) {
		super(sandboxInstanceID, node.address().toAddressString());
		for(String tag : node.tags()){
			tags().add(tag);
		}
		for(String key : node.attr().keys()){
			attr().put(key, node.getAttr(key));
		}
	}

	public SandboxNode(int sandboxInstanceID, String address) {
		super(sandboxInstanceID, address);
	}

}
