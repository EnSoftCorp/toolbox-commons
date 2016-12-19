package com.ensoftcorp.open.commons.filters;

import java.util.ArrayList;

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
	
	public Filter(Q input){
		this.input = input;
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
	protected String[] getSupportedNodeTags() {
		return EVERYTHING;
	}
	
	/**
	 * The set of supported edge tags that this filter can operate on
	 * @return
	 */
	protected String[] getSupportedEdgeTags() {
		return EVERYTHING;
	}
	
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
	
}
