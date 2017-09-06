package com.ensoftcorp.open.commons.filters;

import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
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
	private static final String RETAIN_ALL_NODES = "RETAIN_ALL_NODES";
	private static final String RETAIN_ALL_EDGES = "RETAIN_ALL_EDGES";
	private static final String RETAIN_UNAMED = "RETAIN_UNAMED";
	private static final String CASE_INSENSITIVE = "CASE_INSENSITIVE";
	
	// parameters
	private static final String EXACT_NAME = "EXACT_NAME";
	private static final String NAME_PREFIX = "NAME_PREFIX";
	private static final String NAME_SUFFIX = "NAME_SUFFIX";
	private static final String NAME_SUBSTRING = "NAME_SUBSTRING";

	public NameFilter() {
		this.addPossibleFlag(EXCLUDE_MATCHES, "Retain only nodes and edges that do not have the given name.");
		this.addPossibleFlag(RETAIN_ALL_NODES, "All nodes will be retained even if matched.");
		this.addPossibleFlag(RETAIN_ALL_EDGES, "All edges will be retained even if matched.");
		this.addPossibleFlag(RETAIN_UNAMED, "Retains nodes and edges that do not have a name attribute (without this flag unamed nodes and edges will be removed from the result).");
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
	protected Q filterInput(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException {
		checkParameters(parameters);

		AtlasSet<Node> nodesToSave = new AtlasHashSet<Node>();
		AtlasSet<Edge> edgesToSave = new AtlasHashSet<Edge>();
		if(this.isFlagSet(RETAIN_ALL_NODES, parameters)){
			for(Node node : input.retainNodes().eval().nodes()){
				nodesToSave.add(node);
			}
		}
		if(this.isFlagSet(RETAIN_ALL_EDGES, parameters)){
			for(Edge edge : input.retainEdges().eval().edges()){
				edgesToSave.add(edge);
			}
		}
		
		Graph graph = input.eval();
		AtlasSet<Node> nodes = new AtlasHashSet<Node>(graph.nodes());
		AtlasSet<Edge> edges = new AtlasHashSet<Edge>(graph.edges());
		
		if(this.isFlagSet(RETAIN_UNAMED, parameters)){
			// filter nodes
			for(Node node : nodes){
				if(node.getAttr(XCSG.name) == null){
					nodesToSave.add(node);
				}
			}
			// filter edges
			for(Edge edge : edges){
				if(edge.getAttr(XCSG.name) == null){
					edgesToSave.add(edge);
				}
			}
		}
		// remove the unamed nodes and edges from the sets to filter
		// filter nodes
		AtlasSet<Node> nullNodesToRemove = new AtlasHashSet<Node>();
		for(Node node : nodes){
			if(node.getAttr(XCSG.name) == null){
				nullNodesToRemove.add(node);
			}
		}
		for(Node node : nullNodesToRemove){
			nodes.remove(node);
		}
		// filter edges
		AtlasSet<Edge> nullEdgesToRemove = new AtlasHashSet<Edge>();
		for(Edge edge : edges){
			if(edge.getAttr(XCSG.name) == null){
				nullEdgesToRemove.add(edge);
			}
		}
		for(Edge edge : nullEdgesToRemove){
			edges.remove(edge);
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
		
		nodes.addAll(nodesToSave);
		edges.addAll(edgesToSave);
		Q result = Common.toQ(nodes).union(Common.toQ(edges));
		
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
