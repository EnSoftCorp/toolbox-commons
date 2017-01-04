package com.ensoftcorp.open.commons.filters;

import java.util.ArrayList;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.subsystems.Subsystems;

/**
 * Filters nodes based on how whether or not they or their children interact with specified subsystems
 * 
 * @author Ben Holland
 */
public class SubsystemInteractionFilter extends NodeFilter {

	private static final String SUBSYSTEM_TAGS = "SUBSYSTEM_TAGS";
	private static final String DATA_FLOW_INTERACTION = "DATA_FLOW_INTERACTION";
	private static final String CONTROL_FLOW_INTERACTION = "CONTROL_FLOW_INTERACTION";
	private static final String INTERACTION_EDGE_TAGS = "INTERACTION_EDGE_TAGS";

	public SubsystemInteractionFilter() {
		this.addPossibleParameter(SUBSYSTEM_TAGS, String.class, true, "A comma separated list of subsystem tags");
		this.addPossibleParameter(DATA_FLOW_INTERACTION, Boolean.class, false, "Includes XCSG.Data_Flow_Edge edges in the interaction edge types");
		this.addPossibleParameter(CONTROL_FLOW_INTERACTION, Boolean.class, false, "Includes XCSG.Control_Flow_Edge edges in the interaction edge types");
		this.addPossibleParameter(INTERACTION_EDGE_TAGS, String.class, false, "A comma separated list of edge tags to include in interaction edge types");
		this.setMinimumNumberParametersRequired(2);
	}

	@Override
	public String getName() {
		return "Subsystem Interactions";
	}

	@Override
	public String getDescription() {
		return "Filters nodes based on how whether or not they or their children interact with specified subsystems.";
	}

	@Override
	public Q filter(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);
		input = super.filter(input, parameters);
		
		String[] subsystems = ((String) getParameterValue(SUBSYSTEM_TAGS, parameters)).replaceAll("\\s","").split(",");

		ArrayList<String> interactionEdgeTags = new ArrayList<String>();
		
		if(isParameterSet(DATA_FLOW_INTERACTION, parameters)){
			if((Boolean) getParameterValue(DATA_FLOW_INTERACTION, parameters)){
				interactionEdgeTags.add(XCSG.DataFlow_Edge);
			}
		}
		
		if(isParameterSet(CONTROL_FLOW_INTERACTION, parameters)){
			if((Boolean) getParameterValue(CONTROL_FLOW_INTERACTION, parameters)){
				interactionEdgeTags.add(XCSG.ControlFlow_Edge);
			}
		}
		
		if(isParameterSet(INTERACTION_EDGE_TAGS, parameters)){
			String[] tags = ((String) getParameterValue(INTERACTION_EDGE_TAGS, parameters)).replaceAll("\\s","").split(",");
			for(String tag : tags){
				interactionEdgeTags.add(tag);
			}
		}
		
		String[] interactionEdges = new String[interactionEdgeTags.size()];
		interactionEdgeTags.toArray(interactionEdges);
		
		AtlasSet<Node> result = new AtlasHashSet<Node>();
		for(String subsystem : subsystems){
			Q interaction = Subsystems.getSubsystemInteractions(input.contained(), subsystem, interactionEdges);
			result.addAll(interaction.containers().intersection(input).eval().nodes());
		}
		
		return Common.toQ(result);
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return EVERYTHING;
	}

}
