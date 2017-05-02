package com.ensoftcorp.open.commons.analyzers;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Analyzer other = (Analyzer) obj;
		if (this.getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!this.getName().equals(other.getName()))
			return false;
		return true;
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
	 * A category to classify this analyzer under
	 * @return
	 */
	public abstract String getCategory();
	
	/**
	 * Returns a short description of the analyzer
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Optionally specifies a set of code map stage dependencies 
	 * @return
	 */
	public String[] getCodemapStageDependencies(){
		return new String[]{};
	}
	
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
	public abstract List<Result> getResults(Q context);
	
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
	public static Q getAllResults(List<Result> results){
		AtlasSet<Node> nodes = new AtlasHashSet<Node>();
		AtlasSet<Edge> edges = new AtlasHashSet<Edge>();
		for(Result result : results){
			Graph g = result.getQ().eval();
			nodes.addAll(g.nodes());
			edges.addAll(g.edges());
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
