package com.ensoftcorp.open.commons.sandbox;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.open.commons.analysis.CommonQueries;

public class SandboxNode extends SandboxGraphElement {

	public SandboxNode(int sandboxInstanceID, Node node) {
		super(sandboxInstanceID, Sandbox.addrStr(node));
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
	
	public Node toAtlasNode(){
		if(isMirror()){
			return CommonQueries.getNodeByAddress(address);
		} else {
			return null;
		}
	}

}
