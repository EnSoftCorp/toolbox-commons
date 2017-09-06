package com.ensoftcorp.open.commons.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Filters nodes and edges based on their name attributes
 * 
 * @author Ben Holland
 */
public class NameFilter extends Filter {

	// flags
	private static final String EXCLUDE_MATCHES = "EXCLUDE_MATCHES";
	private static final String FILTER_ONLY_NODES = "FILTER_ONLY_NODES";
	private static final String FILTER_ONLY_EDGES = "FILTER_ONLY_EDGES";
	private static final String CASE_INSENSITIVE = "CASE_INSENSITIVE";
	
	// parameters
	private static final String EXACT_NAME = "EXACT_NAME";
	private static final String NAME_PREFIX = "NAME_PREFIX";
	private static final String NAME_SUFFIX = "NAME_SUFFIX";
	private static final String NAME_SUBSTRING = "NAME_SUBSTRING";

	public NameFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only nodes and edges that do not have the given name.");
		this.addPossibleFlag(FILTER_ONLY_NODES, "Only consider nodes for filtering.");
		this.addPossibleFlag(FILTER_ONLY_EDGES, "Only consider edges for filtering.");
		this.addPossibleFlag(CASE_INSENSITIVE, "Do not distinguish between upper or lower case characters.");
		this.addPossibleParameter(EXACT_NAME, String.class, false, "Filters nodes and edges with the given exact name.");
		this.addPossibleParameter(NAME_PREFIX, String.class, false, "Filters nodes and edges with the given name prefix.");
		this.addPossibleParameter(NAME_SUFFIX, String.class, false, "Filters nodes and edges with the given name suffix.");
		this.addPossibleParameter(NAME_SUBSTRING, String.class, false, "Filters nodes and edges with the given name substring.");
	}
	
	@Override
	public String getName() {
		return "Name";
	}

	@Override
	public String getDescription() {
		return "Filters nodes and edges based on their name attributes.";
	}

	@Override
	public Q filter(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);

		AtlasSet<Node> nodes = new AtlasHashSet<Node>();
		AtlasSet<Edge> edges = new AtlasHashSet<Edge>();
		if(this.isFlagSet(FILTER_ONLY_NODES, parameters)){
			for(Node node : input.retainNodes().eval().nodes()){
				nodes.add(node);
			}
		}
		if(this.isFlagSet(FILTER_ONLY_EDGES, parameters)){
			for(Edge edge : input.retainEdges().eval().edges()){
				edges.add(edge);
			}
		}
		
		if(this.isParameterSet(EXACT_NAME, parameters)){
			String exactName = (String) this.getParameterValue(EXACT_NAME, parameters);
			if(this.isFlagSet(CASE_INSENSITIVE, parameters)){
				// filter nodes
				AtlasSet<Node> nodesToRemove = new AtlasHashSet<Node>();
				for(Node node : nodes){
					if(node.getAttr(XCSG.name).toString().equalsIgnoreCase(exactName)){
						nodesToRemove.add(node);
					}
				}
				for(Node node : nodesToRemove){
					nodes.remove(node);
				}
				// filter edges
				AtlasSet<Edge> edgesToRemove = new AtlasHashSet<Edge>();
				for(Edge edge : edges){
					if(edge.getAttr(XCSG.name).toString().equalsIgnoreCase(exactName)){
						edgesToRemove.add(edge);
					}
				}
				for(Edge edge : edgesToRemove){
					edges.remove(edge);
				}
			} else {
				// filter nodes
				AtlasSet<Node> nodesToRemove = new AtlasHashSet<Node>();
				for(Node node : nodes){
					if(node.getAttr(XCSG.name).toString().equals(exactName)){
						nodesToRemove.add(node);
					}
				}
				for(Node node : nodesToRemove){
					nodes.remove(node);
				}
				// filter edges
				AtlasSet<Edge> edgesToRemove = new AtlasHashSet<Edge>();
				for(Edge edge : edges){
					if(edge.getAttr(XCSG.name).toString().equals(exactName)){
						edgesToRemove.add(edge);
					}
				}
				for(Edge edge : edgesToRemove){
					edges.remove(edge);
				}
			}
		}
		
		if(this.isParameterSet(NAME_PREFIX, parameters)){
			String namePrefix = (String) this.getParameterValue(NAME_PREFIX, parameters);
			if(this.isFlagSet(CASE_INSENSITIVE, parameters)){
				// filter nodes
				AtlasSet<Node> nodesToRemove = new AtlasHashSet<Node>();
				for(Node node : nodes){
					if(node.getAttr(XCSG.name).toString().toLowerCase().startsWith(namePrefix.toLowerCase())){
						nodesToRemove.add(node);
					}
				}
				for(Node node : nodesToRemove){
					nodes.remove(node);
				}
				// filter edges
				AtlasSet<Edge> edgesToRemove = new AtlasHashSet<Edge>();
				for(Edge edge : edges){
					if(edge.getAttr(XCSG.name).toString().toLowerCase().startsWith(namePrefix.toLowerCase())){
						edgesToRemove.add(edge);
					}
				}
				for(Edge edge : edgesToRemove){
					edges.remove(edge);
				}
			} else {
				// filter nodes
				AtlasSet<Node> nodesToRemove = new AtlasHashSet<Node>();
				for(Node node : nodes){
					if(node.getAttr(XCSG.name).toString().startsWith(namePrefix)){
						nodesToRemove.add(node);
					}
				}
				for(Node node : nodesToRemove){
					nodes.remove(node);
				}
				// filter edges
				AtlasSet<Edge> edgesToRemove = new AtlasHashSet<Edge>();
				for(Edge edge : edges){
					if(edge.getAttr(XCSG.name).toString().startsWith(namePrefix)){
						edgesToRemove.add(edge);
					}
				}
				for(Edge edge : edgesToRemove){
					edges.remove(edge);
				}
			}
		}
		
		if(this.isParameterSet(NAME_SUFFIX, parameters)){
			String nameSuffix = (String) this.getParameterValue(NAME_SUFFIX, parameters);
			if(this.isFlagSet(CASE_INSENSITIVE, parameters)){
				// filter nodes
				AtlasSet<Node> nodesToRemove = new AtlasHashSet<Node>();
				for(Node node : nodes){
					if(node.getAttr(XCSG.name).toString().toLowerCase().endsWith(nameSuffix.toLowerCase())){
						nodesToRemove.add(node);
					}
				}
				for(Node node : nodesToRemove){
					nodes.remove(node);
				}
				// filter edges
				AtlasSet<Edge> edgesToRemove = new AtlasHashSet<Edge>();
				for(Edge edge : edges){
					if(edge.getAttr(XCSG.name).toString().toLowerCase().endsWith(nameSuffix.toLowerCase())){
						edgesToRemove.add(edge);
					}
				}
				for(Edge edge : edgesToRemove){
					edges.remove(edge);
				}
			} else {
				// filter nodes
				AtlasSet<Node> nodesToRemove = new AtlasHashSet<Node>();
				for(Node node : nodes){
					if(node.getAttr(XCSG.name).toString().endsWith(nameSuffix)){
						nodesToRemove.add(node);
					}
				}
				for(Node node : nodesToRemove){
					nodes.remove(node);
				}
				// filter edges
				AtlasSet<Edge> edgesToRemove = new AtlasHashSet<Edge>();
				for(Edge edge : edges){
					if(edge.getAttr(XCSG.name).toString().endsWith(nameSuffix)){
						edgesToRemove.add(edge);
					}
				}
				for(Edge edge : edgesToRemove){
					edges.remove(edge);
				}
			}
		}
		
		if(this.isParameterSet(NAME_SUBSTRING, parameters)){
			String nameSubstring = (String) this.getParameterValue(NAME_SUBSTRING, parameters);
			if(this.isFlagSet(CASE_INSENSITIVE, parameters)){
				// filter nodes
				AtlasSet<Node> nodesToRemove = new AtlasHashSet<Node>();
				for(Node node : nodes){
					if(node.getAttr(XCSG.name).toString().toLowerCase().contains(nameSubstring.toLowerCase())){
						nodesToRemove.add(node);
					}
				}
				for(Node node : nodesToRemove){
					nodes.remove(node);
				}
				// filter edges
				AtlasSet<Edge> edgesToRemove = new AtlasHashSet<Edge>();
				for(Edge edge : edges){
					if(edge.getAttr(XCSG.name).toString().toLowerCase().contains(nameSubstring.toLowerCase())){
						edgesToRemove.add(edge);
					}
				}
				for(Edge edge : edgesToRemove){
					edges.remove(edge);
				}
			} else {
				// filter nodes
				AtlasSet<Node> nodesToRemove = new AtlasHashSet<Node>();
				for(Node node : nodes){
					if(node.getAttr(XCSG.name).toString().contains(nameSubstring)){
						nodesToRemove.add(node);
					}
				}
				for(Node node : nodesToRemove){
					nodes.remove(node);
				}
				// filter edges
				AtlasSet<Edge> edgesToRemove = new AtlasHashSet<Edge>();
				for(Edge edge : edges){
					if(edge.getAttr(XCSG.name).toString().contains(nameSubstring)){
						edgesToRemove.add(edge);
					}
				}
				for(Edge edge : edgesToRemove){
					edges.remove(edge);
				}
			}
		}
		
		Q graph = Common.toQ(nodes).union(Common.toQ(edges));
		Q result = Common.toQ(graph.eval());
		
		if(isFlagSet(EXCLUDE_MATCHES, parameters)){
			return input.difference(result);
		} else {
			return result;
		}
	}

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[]{ XCSG.Node };
	}

	@Override
	protected String[] getSupportedEdgeTags() {
		return new String[]{ XCSG.Edge };
	}

}
