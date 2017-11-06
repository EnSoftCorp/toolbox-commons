package com.ensoftcorp.open.commons.codepainter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Configurable {

	private Map<String,Class<? extends Object>> parameterNames = new HashMap<String,Class<? extends Object>>();
	private Map<String,Object> defaultParameterValues = new HashMap<String,Object>();
	private Map<String,String> parameterDescriptions = new HashMap<String,String>();
	private HashSet<String> flags = new HashSet<String>();
	
	/**
	 * Flags are booleans that are always true
	 * @param name
	 * @param description
	 */
	protected void addPossibleFlag(String name, String description, boolean enabledByDefault){
		parameterNames.put(name, Boolean.class);
		parameterDescriptions.put(name, description);
		defaultParameterValues.put(name, enabledByDefault);
		flags.add(name);
	}
	
	/**
	 * Adds a possible parameter type with a default value
	 * @param name
	 * @param type
	 */
	protected void addPossibleParameter(String name, Class<? extends Object> type, String description, Object defaultValue){
		parameterNames.put(name, type);
		parameterDescriptions.put(name, description);
		defaultParameterValues.put(name, defaultValue);
	}
	
	/**
	 * Returns a copy of all possible parameter types
	 * @return
	 */
	public Map<String,Class<? extends Object>> getPossibleParameters(){
		Map<String,Class<? extends Object>> possibleParams = new HashMap<String,Class<? extends Object>>();
		possibleParams.putAll(parameterNames);
		return possibleParams;
	}
	
	/**
	 * Returns the set of possible flags
	 * @return
	 */
	public Set<String> getPossibleFlags(){
		return new HashSet<String>(flags);
	}
	
	/**
	 * Returns the parameter description or null if the parameter does not exist
	 * @param parameter
	 * @return
	 */
	public String getParameterDescription(String parameter){
		return parameterDescriptions.get(parameter);
	}
	
	/**
	 * Returns the description of a flag
	 * @param parameter
	 * @return
	 */
	public String getFlagDescription(String parameter){
		return getParameterDescription(parameter);
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
	 * Returns true if the given flag was set
	 * @param name
	 * @param parameters
	 * @return
	 */
	public boolean isFlagSet(String name, Map<String,Object> parameters){
		return isParameterSet(name, parameters);
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
	 * @throws IllegalArgumentException  
	 */
	public void checkParameters(Map<String,Object> parameters) throws IllegalArgumentException {
		for(Entry<String,Object> parameter : parameters.entrySet()){
			checkParameter(parameter.getKey(), parameter.getValue());
		}
	}
	
	/**
	 * Checks a parameter name and value
	 * @param name
	 * @param value
	 */
	private void checkParameter(String name, Object value) throws IllegalArgumentException {
		if(!parameterNames.containsKey(name)){
			throw new IllegalArgumentException(name + " is not a valid parameter.");
		} else if(parameterNames.get(name) != value.getClass()){
			throw new IllegalArgumentException(name + " must be a " + parameterNames.get(name).getName() + " type.");
		}
	}
	
}
