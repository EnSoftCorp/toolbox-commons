package com.ensoftcorp.open.commons.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
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
	
	protected static String[] EVERYTHING = { SUPPORTS_EVERYTHING };
	protected static String[] NOTHING = { SUPPORTS_NOTHING };
	
	protected Q input = Common.empty();
	
	private Map<String,Class<? extends Object>> parameterNames = new HashMap<String,Class<? extends Object>>();
	private Map<String,Boolean> requiredParameters = new HashMap<String,Boolean>();
	
	/**
	 * Adds a possible parameter type to this filter
	 * @param name
	 * @param type
	 */
	protected void addPossibleParameter(String name, Class<? extends Object> type, boolean required){
		parameterNames.put(name, type);
		requiredParameters.put(name, required);
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
	 * Returns a copy of all possible parameter types for this filter
	 * @return
	 */
	public Set<String> getRequiredParameters(){
		return new HashSet<String>(requiredParameters.keySet());
	}
	
	/**
	 * Returns true if the given parameter has been specified
	 * @param name
	 * @param parameters
	 * @return
	 */
	public boolean isParameterSet(String name, Map<String,Object> parameters){
		return parameters.containsKey(name);
	}
	
	/**
	 * Returns the specified parameter value or null if the parameter is unspecified
	 * @param name
	 * @param parameters
	 * @return
	 */
	public Object getParameterValue(String name, Map<String,Object> parameters){
		return parameters.get(name);
	}
	
	/**
	 * Type checks expected parameters and rejects undeclared passed parameters
	 * Also checks that all required parameters have been specified
	 * @return
	 * @throws InvalidFilterParameterException  
	 */
	public void checkParameters(Map<String,Object> parameters) throws InvalidFilterParameterException {
		for(Entry<String,Object> parameter : parameters.entrySet()){
			checkParameter(parameter.getKey(), parameter.getValue());
		}
		for(String parameter : getRequiredParameters()){
			if(!parameters.containsKey(parameter)){
				throw new InvalidFilterParameterException("Missing required parameter: " + parameter);
			}
		}
	}
	
	/**
	 * Checks a parameter name and value
	 * @param name
	 * @param value
	 */
	private void checkParameter(String name, Object value) throws InvalidFilterParameterException {
		if(!parameterNames.containsKey(name)){
			throw new InvalidFilterParameterException("Parameter name [" + name + "] is not a valid parameter for the " + getName() + " filter.");
			
		} else if(parameterNames.get(name) == value.getClass()){
			throw new InvalidFilterParameterException("Parameter name [" + name + "] must be a " + parameterNames.get(name).getName() + " type.");
		}
	}

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
	public abstract Q filter(Q input, Map<String,Object> parameters) throws InvalidFilterParameterException;
	
	/**
	 * Returns the filter name
	 */
	@Override
	public String toString(){
		return getName();
	}
}
