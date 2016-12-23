package com.ensoftcorp.open.commons.analyzers;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.markup.Markup;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;

/**
 * A base class for implementing program analyzers
 * 
 * @author Ben Holland
 */
public abstract class Analyzer {

	/**
	 * Returns a unique string
	 * Can be used for generating result keys
	 * @return
	 */
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	
	/**
	 * Just a pair class to hold a display name and the result
	 * Note: display name does not need to be unique
	 */
	public static class Result {
		private String displayLabel;
		private Object data = null;
		private Q q;
		
		public Result(String displayLabel, Q q){
			this.displayLabel = displayLabel;
			this.q = q;
		}
		
		public String getDisplayLabel(){
			return displayLabel;
		}
		
		public Q getQ(){
			return q;
		}
		
		public Object getData(){
			return data;
		}
		
		public void setData(Object data){
			this.data = data;
		}
	}
	
	/**
	 * Returns a name of the analyzer
	 * @return
	 */
	public String getName(){
		return this.getClass().getSimpleName();
	}
	
	/**
	 * Returns a short description of the analyzer
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Returns an array of assumptions made when writing the analyzer
	 * @return
	 */
	public String[] getAssumptions(){
		return new String[]{};
	}
	
	/**
	 * Return analyzer's labeled results
	 * Results are for results within a given context
	 * @return
	 */
	public abstract Map<String,Result> getResults(Q context);
	
	/**
	 * Defines the sorted ordering for the results (by label)
	 * @return
	 */
	public Comparator<Result> getResultOrder(){
		return new Comparator<Result>(){
			@Override
			public int compare(Result o1, Result o2) {
				return o1.getDisplayLabel().compareToIgnoreCase(o2.getDisplayLabel());
			}
		};
	}
	
	/**
	 * Returns a union of all results
	 * @return
	 */
	public Q getAllResults(Q context){
		AtlasSet<Node> nodes = new AtlasHashSet<Node>();
		AtlasSet<Edge> edges = new AtlasHashSet<Edge>();
		for(Entry<String,Result> entry : getResults(context).entrySet()){
			Graph result = entry.getValue().getQ().eval();
			nodes.addAll(result.nodes());
			edges.addAll(result.edges());
		}
		return Common.toQ(edges).union(Common.toQ(nodes));
	}
	
	/**
	 * Returns markup for the analyzer result
	 * @return
	 */
	public Markup getMarkup(){
		return new Markup();
	}
	
	/**
	 * Returns a color key legend to interpret the markup results
	 * @return
	 */
	public Map<Color,String> getMarkupKey(){
		return new HashMap<Color,String>();
	}
	
}
