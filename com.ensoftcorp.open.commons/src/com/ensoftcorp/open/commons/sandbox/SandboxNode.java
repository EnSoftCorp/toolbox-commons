package com.ensoftcorp.open.commons.sandbox;

import com.ensoftcorp.atlas.core.db.graph.Node;

public class SandboxNode extends SandboxGraphElement {

	public SandboxNode(Node node) {
		super(node.address().toAddressString(), true);
		for(String tag : node.tags()){
			tags().add(tag);
		}
		for(String key : node.attr().keys()){
			attr().put(key, node.getAttr(key));
		}
	}

	public SandboxNode(String address) {
		super(address, false);
	}

}
