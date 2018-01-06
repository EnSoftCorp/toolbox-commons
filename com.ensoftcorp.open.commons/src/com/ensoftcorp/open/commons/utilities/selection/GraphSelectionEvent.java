package com.ensoftcorp.open.commons.utilities.selection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;

public class GraphSelectionEvent extends StructuredSelection {

	private boolean expired = false;
	private List<GraphElement> graphElements = new LinkedList<GraphElement>();
	
	public GraphSelectionEvent(Q selection){
		this(selection!=null ? selection.eval() : Common.empty().eval());
	}
	
	public GraphSelectionEvent(Graph selection){
		if(selection != null){
			for(Node node : selection.nodes()){
				graphElements.add(node);
			}
			for(Edge edge : selection.edges()){
				graphElements.add(edge);
			}
		}
	}
	
	@Override
	public Object getFirstElement(){
		if(graphElements.isEmpty()){
			return null;
		}
		return graphElements.get(0);
	}
	
	@SuppressWarnings("rawtypes")
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
	
	@SuppressWarnings("rawtypes")
	@Override
	public List toList(){
		return new LinkedList<GraphElement>(graphElements);
	}

	public void expire() {
		graphElements.clear();
		expired = true;
	}

	public boolean isExpired() {
		return expired;
	}
	
}
