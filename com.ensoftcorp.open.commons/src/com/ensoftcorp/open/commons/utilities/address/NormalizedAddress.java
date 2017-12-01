package com.ensoftcorp.open.commons.utilities.address;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;

public class NormalizedAddress extends PrioritizedCodemapStage {

	public static final String NORMALIZED_ADDRESS_ATTRIBUTE = "NORMALIZED_ADDRESS";
	
	private static boolean normalized = false;
	
	public static boolean isIndexNormalized(){
		return normalized;
	}
	
	@Override
	public String getDisplayName() {
		return "Normalize Graph Element Addresses";
	}

	@Override
	public String getIdentifier() {
		return "com.ensoftcorp.open.commons.utilities.address.normalization";
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{}; // no dependencies
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		if(CommonsPreferences.isAddressNormalizationEnabled()){
			Log.info("Assigning normalized graph element addresses...");
			normalized = false;
			assignNormalizedAddresses();
		}
	}
	
	/**
	 * Assigns a unique id to nodes an edges
	 */
	private static void assignNormalizedAddresses(){
		// just prioritizing nodes/edges address values as being lower for the graph elements that don't have a source correspondence
		Q sourceNodes = Common.universe().selectNode(XCSG.sourceCorrespondence);
		Q primitiveNodes = Common.universe().difference(sourceNodes);
		Q sourceEdges = Common.universe().selectEdge(XCSG.sourceCorrespondence).retainEdges();
		Q primitiveEdges = Common.universe().differenceEdges(sourceEdges).retainEdges();
		
		long address = 0;
		
		for(Node node : new AtlasHashSet<Node>(primitiveNodes.eval().nodes())){
			node.putAttr(NORMALIZED_ADDRESS_ATTRIBUTE, Long.toHexString(address++));
		}
		
		for(Edge edge : new AtlasHashSet<Edge>(primitiveEdges.eval().edges())){
			edge.putAttr(NORMALIZED_ADDRESS_ATTRIBUTE, Long.toHexString(address++));
		}
		
		for(Node node : new AtlasHashSet<Node>(sourceNodes.eval().nodes())){
			node.putAttr(NORMALIZED_ADDRESS_ATTRIBUTE, Long.toHexString(address++));
		}
		
		for(Edge edge : new AtlasHashSet<Edge>(sourceEdges.eval().edges())){
			edge.putAttr(NORMALIZED_ADDRESS_ATTRIBUTE, Long.toHexString(address++));
		}
		
		normalized = true;
	}
	
}
