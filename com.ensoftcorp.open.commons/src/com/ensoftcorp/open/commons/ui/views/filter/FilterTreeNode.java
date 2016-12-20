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
		this.expanded = expanded;
	}
	
	public String getName(){
		return filter.getName();
	}
	
	public Q getInput(){
		return Common.toQ(graph);
	}
	
	public boolean isExpanded(){
		return expanded;
	}
}
