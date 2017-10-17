package com.ensoftcorp.open.commons.utilities.selection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;

public class GraphSelectionEvent extends StructuredSelection {

	private List<GraphElement> graphElements = new LinkedList<GraphElement>();
	
	public GraphSelectionEvent(Q selection){
		Graph graph = selection.eval();
		for(Node node : graph.nodes()){
			graphElements.add(node);
		}
		for(Edge edge : graph.edges()){
			graphElements.add(edge);
		}
	}
	
	public GraphSelectionEvent(Graph selection){
		for(Node node : selection.nodes()){
			graphElements.add(node);
		}
		for(Edge edge : selection.edges()){
			graphElements.add(edge);
		}
	}
	
	@Override
	public Object getFirstElement(){
		if(graphElements.isEmpty()){
			return null;
		}
		return graphElements.get(0);
	}
	
	@Override
	public Iterator iterator(){
		return graphElements.iterator();
	}
	
	@Override
	public Object[] toArray(){
		GraphElement[] result = new GraphElement[graphElements.size()];
		graphElements.toArray(result);
		return result;
	}
	
	@Override
	public List toList(){
		return new LinkedList<GraphElement>(graphElements);
	}
}
