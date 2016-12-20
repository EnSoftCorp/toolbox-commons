package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.LinkedList;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.filters.Filter;

public class FilterTreeNode {

	public LinkedList<FilterTreeNode> nodes = new LinkedList<FilterTreeNode>();
	
	private Graph graph;
	private Filter filter;
	private String name;
	private boolean expanded;
	
	public FilterTreeNode(Q input, Filter filter, boolean expanded){
		this.graph = input.eval();
		if(graph.nodes().isEmpty()){
			throw new IllegalArgumentException("Input is empty.");
		}
		if(!filter.isApplicableTo(input)){
			throw new IllegalArgumentException("Filter is not applicable to imput.");
		}
		this.filter = filter;
		this.name = filter.toString();
		this.expanded = expanded;
	}
	
	public Filter getFilter(){
		return filter;
	}
	
	public String getName(){
		return name;
	}
	
	public Q getInput(){
		return Common.toQ(graph);
	}
	
	public boolean isExpanded(){
		return expanded;
	}
}
