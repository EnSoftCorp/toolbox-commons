package com.ensoftcorp.open.commons.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.CommonQueries;

/**
 * Filters are generalized implementation of the classic Gang of Four
 * Pipe/Filter design pattern. Filters are chained together and return
 * only a subset of the their input.
 * 
 * @author Ben Holland
 */
public abstract class Filter {

	private static String SUPPORTS_NOTHING = "SUPPORTS_NOTHING";
	private static String SUPPORTS_EVERYTHING = "SUPPORTS_EVERYTHING";
	
	protected static String[] EVERYTHING = { SUPPORTS_NOTHING };
	protected static String[] NOTHING = { SUPPORTS_EVERYTHING };
	
	protected Q input;
	
	protected Filter(Q input){
		this.input = input;
		this.parameterNames = new HashMap<String,Class<? extends Object>>();
		this.parameterValues = new HashMap<String,Object>();
	}
	
	private Map<String,Class<? extends Object>> parameterNames;
	private Map<String,Object> parameterValues;
	
	/**
	 * Adds a possible parameter type to this filter
	 * @param name
	 * @param type
	 */
	protected void addPossibleParameter(String name, Class<? extends Object> type){
		parameterNames.put(name, type);
	}
	
	/**
	 * Returns a copy of all possible parameter types for this filter
	 * @return
	 */
	public Map<String,Class<? extends Object>> getPossibleParameters(){
		Map<String,Class<? extends Object>> possibleParams = new HashMap<String,Class<? extends Object>>();
		possibleParams.putAll(parameterNames);
		return possibleParams;
	}
	
	/**
	 * Sets a parameter value
	 * @param name
	 * @param value
	 */
	protected void setParameterValue(String name, Object value) throws FilterConstructionException {
		if(!parameterNames.containsKey(name)){
			throw new FilterConstructionException("Parameter name [" + name + "] is not a valid parameter for the " + getName() + " filter.");
		} else if(parameterNames.get(name) == value.getClass()){
			throw new FilterConstructionException("Parameter name [" + name + "] must be a " + parameterNames.get(name).getName() + " type.");
		} else {
			parameterValues.put(name, value);
		}
	}
	
	public boolean isParameterSet(String name){
		return parameterValues.containsKey(name);
	}
	
	public Object getParameterValue(String name){
		return parameterValues.get(name);
	}
	
	/**
	 * Returns the instance of the filter
	 * @return
	 */
	public abstract Filter getInstance(Q input, Map<String,Object> parameters) throws FilterConstructionException;

	/**
	 * The display name of the filter
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * A short description of the filter
	 * 
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * The set of supported node tags that this filter can operate on
	 * @return
	 */
	protected abstract String[] getSupportedNodeTags();
	
	/**
	 * The set of supported edge tags that this filter can operate on
	 * @return
	 */
	protected abstract String[] getSupportedEdgeTags();
	
	/**
	 * Returns true if the input contains supported edges or nodes
	 * @param input
	 * @return
	 */
	public boolean isApplicableTo(Q input){
		return !CommonQueries.isEmpty(getSupportedInput(input));
	}
	
	/**
	 * Returns the supported edges and nodes
	 * @param input
	 * @return
	 */
	public Q getSupportedInput(Q input){
		String[] supportedEdgeTags = getSupportedEdgeTags();
		ArrayList<String> edgeTagsToKeep = new ArrayList<String>();
		if(supportedEdgeTags != null){
			for(String tag : supportedEdgeTags){
				if(tag.equals(SUPPORTS_EVERYTHING)){
					edgeTagsToKeep.clear();
					break;
				} else if(tag.equals(SUPPORTS_NOTHING)){
					input = input.retainNodes();
				} else {
					edgeTagsToKeep.add(tag);
				}
			}
			if(!edgeTagsToKeep.isEmpty()){
				String[] tags = new String[edgeTagsToKeep.size()];
				edgeTagsToKeep.toArray(tags);
				Q edgesWithTags = input.edgesTaggedWithAny(tags);
				Q edgesWithoutTags = input.difference(edgesWithTags);
				input = input.difference(edgesWithoutTags);
			}
		}
		
		String[] supportedNodeTags = getSupportedNodeTags();
		ArrayList<String> nodeTagsToKeep = new ArrayList<String>();
		if(supportedNodeTags != null){
			for(String tag : supportedNodeTags){
				if(tag.equals(SUPPORTS_EVERYTHING)){
					nodeTagsToKeep.clear();
					break;
				} else if(tag.equals(SUPPORTS_NOTHING)){
					input = input.retainEdges();
				} else {
					nodeTagsToKeep.add(tag);
				}
			}
			if(!nodeTagsToKeep.isEmpty()){
				String[] tags = new String[nodeTagsToKeep.size()];
				nodeTagsToKeep.toArray(tags);
				Q nodesWithTags = input.nodesTaggedWithAny(tags);
				Q nodesWithoutTags = input.difference(nodesWithTags);
				input = input.difference(nodesWithoutTags);
			}
		}
		
		return input;
	}
	
	/**
	 * The filtering strategy
	 * @param input
	 * @return
	 */
	public abstract Q filter(Q input);
	
	/**
	 * Returns the filtered result. Note that this method enforces
	 * that the result must be a subset of the original input.
	 * @return
	 */
	public Q getFilteredResult(){
		return filter(getSupportedInput(input)).intersection(input);
	}
	
	@Override
	public String toString(){
		Graph g1 = getFilteredResult().eval();
		Long n1 = g1.nodes().size();
		String n1s = n1 > 1 ? "s" : "";
		Long e1 = g1.edges().size();
		String e1s = e1 > 1 ? "s" : "";

		Graph g2 = input.eval();
		Long n2 = g2.nodes().size();
		String n2s = n2 > 1 ? "s" : "";
		Long e2 = g2.edges().size();
		String e2s = e2 > 1 ? "s" : "";
		
		return "[" + n1 + " node" + n1s + ", " + e1 + " edge" + e1s + "] -> " 
					+ getName() 
					+ " -> [" + n2 + " node" + n2s + ", " + e2 + " edge" + e2s + "]";
	}
}
