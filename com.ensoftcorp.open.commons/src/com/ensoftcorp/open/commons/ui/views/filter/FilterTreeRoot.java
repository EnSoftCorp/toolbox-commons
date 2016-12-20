package com.ensoftcorp.open.commons.ui.views.filter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;

public class FilterTreeRoot {

	private static Set<String> uniqueNames = new HashSet<String>();
	
	public LinkedList<FilterTreeNode> nodes = new LinkedList<FilterTreeNode>();
	
	private Graph graph;
	private String name;
	private boolean expanded;
	
	public FilterTreeRoot(Q input, String name, boolean expanded){
		this.graph = input.eval();
		if(graph.nodes().isEmpty()){
			throw new IllegalArgumentException("Input is empty. Please make an Atlas selection.");
		}
		this.name = name;
		if(uniqueNames.contains(name)){
			throw new IllegalArgumentException("Root set names must be unique.");
		} else {
			uniqueNames.add(name);
		}
		this.expanded = expanded;
	}
	
	public String getName(){
		return name;
	}
	
	public void rename(String newName){
		if(uniqueNames.contains(newName)){
			throw new IllegalArgumentException("Root set names must be unique.");
		} else {
			uniqueNames.remove(name);
			this.name = newName;
			uniqueNames.add(newName);
		}
	}
	
	public boolean isExpanded(){
		return expanded;
	}
	
	public Q getRootInput(){
		return Common.toQ(graph);
	}
	
}
