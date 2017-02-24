package com.ensoftcorp.open.commons.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.operation.InducedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.db.set.SingletonAtlasSet;

/**
 * A wrapper for the JGraphT implementation of StronglyConnectedComponent (SCC) computations
 * 
 * @author Tom Deering, Ben Holland
 */
public class StronglyConnectedComponents {
	private Graph graph;
	private AtlasSet<Node> nodes;
	private AtlasSet<Edge> edges;
	private DirectedPseudograph<GraphElement, GraphElement> jGraph;

	public StronglyConnectedComponents(Graph graph) {
		this.graph = graph;
		nodes = graph.nodes();
		edges = graph.edges();
		init();
	}

	public StronglyConnectedComponents(AtlasSet<Node> nodes, AtlasSet<Edge> edges) {
		graph = new InducedGraph(nodes, edges);
		this.nodes = graph.nodes();
		this.edges = graph.edges();
		init();
	}

	private void init() {
		jGraph = new DirectedPseudograph<GraphElement, GraphElement>(GraphElement.class);
		for (GraphElement node : nodes) {
			jGraph.addVertex(node);
		}
		for (GraphElement edge : edges) {
			jGraph.addEdge(edge.getNode(EdgeDirection.FROM), edge.getNode(EdgeDirection.TO), edge);
		}
	}

	/**
	 * Returns the graph's strongly-connected-components in the graph.
	 * 
	 * @return
	 */
	public List<AtlasHashSet<Node>> findSCCs() {
		// note: we have a choice of algorithms:
		// GabowStrongConnectivityInspector - Allows obtaining the strongly connected components of a directed graph. 
		// 									  The implemented algorithm follows Cheriyan-Mehlhorn/Gabow's algorithm 
		// 									  Presented in Path-based depth-first search for strong and biconnected 
		// 									  components by Gabow (2000). The running time is order of O(|V|+|E|)
		// KosarajuStrongConnectivityInspector - Complements the ConnectivityInspector class with the capability to 
		// 										 compute the strongly connected components of a directed graph. The
		//										 algorithm is implemented after "Cormen et al: Introduction to algorithms",
		// 										 Chapter 22.5. It has a running time of O(V + E). Unlike ConnectivityInspector,
		// 										 this class does not implement incremental inspection. The full algorithm is 
		// 										 executed at the first call of stronglyConnectedSets() or isStronglyConnected().
		StrongConnectivityAlgorithm<GraphElement, GraphElement> sci = new KosarajuStrongConnectivityInspector<GraphElement, GraphElement>(jGraph);
		LinkedList<AtlasHashSet<Node>> result = new LinkedList<AtlasHashSet<Node>>();
		for(Set<GraphElement> scc : sci.stronglyConnectedSets()){
			AtlasHashSet<Node> set = new AtlasHashSet<Node>();
			for(GraphElement node : scc){
				set.add((Node) node);
			}
			result.add(set);
		}
		return result;
	}

	/**
	 * Returns the graph's roots, which may be single nodes or else root SCCs.
	 * 
	 * @return
	 */
	public List<AtlasSet<GraphElement>> roots() {
		List<AtlasSet<GraphElement>> roots = new LinkedList<AtlasSet<GraphElement>>();

		DirectedNeighborIndex<GraphElement, GraphElement> dni = new DirectedNeighborIndex<GraphElement, GraphElement>(jGraph);

		// Find single-node roots
		for (GraphElement ge : jGraph.vertexSet()) {
			if (dni.predecessorsOf(ge).isEmpty()) {
				roots.add(new SingletonAtlasSet<GraphElement>(ge));
			}
		}

		// note: we have a choice of algorithms:
		// GabowStrongConnectivityInspector - Allows obtaining the strongly connected components of a directed graph. 
		// 									  The implemented algorithm follows Cheriyan-Mehlhorn/Gabow's algorithm 
		// 									  Presented in Path-based depth-first search for strong and biconnected 
		// 									  components by Gabow (2000). The running time is order of O(|V|+|E|)
		// KosarajuStrongConnectivityInspector - Complements the ConnectivityInspector class with the capability to 
		// 										 compute the strongly connected components of a directed graph. The
		//										 algorithm is implemented after "Cormen et al: Introduction to algorithms",
		// 										 Chapter 22.5. It has a running time of O(V + E). Unlike ConnectivityInspector,
		// 										 this class does not implement incremental inspection. The full algorithm is 
		// 										 executed at the first call of stronglyConnectedSets() or isStronglyConnected().
		
		// Find root SCCs which are roots from each SCC, pick one representative
		StrongConnectivityAlgorithm<GraphElement, GraphElement> sci = new KosarajuStrongConnectivityInspector<GraphElement, GraphElement>(jGraph);
		List<Set<GraphElement>> sccs = sci.stronglyConnectedSets();

		AtlasSet<GraphElement> rootSCCSet = new AtlasHashSet<GraphElement>();
		for (Set<GraphElement> scc : sccs) {
			boolean rootSCC = true;

			for (GraphElement ge : scc) {
				if (!scc.containsAll(dni.predecessorsOf(ge))) {
					rootSCC = false;
					break;
				}
				rootSCCSet.add(ge);
			}

			if (rootSCC) {
				roots.add(rootSCCSet);
				rootSCCSet = new AtlasHashSet<GraphElement>();
			} else {
				rootSCCSet.clear();
			}
		}

		return roots;
	}

	/**
	 * Returns an iterator which iterates over the elements of this graph in a
	 * forward DFS order, after having identified SCCs and converted the graph
	 * to a DAG.
	 * 
	 * @return
	 */
	public Iterator<Node> forwardDFSIterator() {
		return new ConvertedDAG(true).dfsIterator();
	}

	/**
	 * Returns an iterator which iterates over the elements of this graph in a
	 * forward DFS order, after having identified SCCs and converted the graph
	 * to a DAG.
	 * 
	 * @return
	 */
	public Iterator<Node> reverseDFSIterator() {
		return new ConvertedDAG(false).dfsIterator();
	}

	public Graph getGraph() {
		return graph;
	}

	public AtlasSet<Node> getNodes() {
		return nodes;
	}

	public AtlasSet<Edge> getEdges() {
		return edges;
	}

	private class ConvertedDAG {
		/**
		 * Translation from nodes to the SCC that they represent
		 */
		private Map<Object, AtlasSet<Node>> convertedToSCC;

		/**
		 * The DAG after converting SCCs into single collapsed nodes
		 */
		private SimpleDirectedGraph<Object, Object> dag;

		public ConvertedDAG(boolean forward) {
			dag = new SimpleDirectedGraph<Object, Object>(Object.class);

			Set<GraphElement> toConvert = new HashSet<GraphElement>(jGraph.vertexSet());
			List<AtlasHashSet<Node>> sccs = findSCCs();
			convertedToSCC = new HashMap<Object, AtlasSet<Node>>(sccs.size());
			Map<AtlasSet<Node>, Object> sccToConverted = new HashMap<AtlasSet<Node>, Object>(sccs.size());
			Map<GraphElement, Object> elementToConverted = new HashMap<GraphElement, Object>(toConvert.size());

			/*
			 * Create the nodes of the new DAG
			 */
			for (AtlasSet<Node> scc : sccs) {
				Object replacement = new Object();
				dag.addVertex(replacement);
				convertedToSCC.put(replacement, scc);
				sccToConverted.put(scc, replacement);
				for (GraphElement ge : scc) {
					elementToConverted.put(ge, replacement);
					toConvert.remove(ge);
				}
			}

			for (GraphElement ge : toConvert) {
				dag.addVertex(ge);
			}

			/*
			 * Create the edges of the new DAG
			 */
			EdgeDirection originDirection, destDirection;
			if (forward) {
				originDirection = EdgeDirection.FROM;
				destDirection = EdgeDirection.TO;
			} else {
				originDirection = EdgeDirection.TO;
				destDirection = EdgeDirection.FROM;
			}
			for (GraphElement edge : jGraph.edgeSet()) {
				Object origin = elementToConverted.get(edge.getNode(originDirection));
				Object dest = elementToConverted.get(edge.getNode(destDirection));

				if (origin != dest)
					dag.addEdge(origin, dest);
			}
		}

		public Iterator<Node> dfsIterator() {
			final DepthFirstIterator<Object, Object> dfi = new DepthFirstIterator<Object, Object>(dag);

			return new Iterator<Node>() {
				Iterator<Node> sccIter;

				@Override
				public boolean hasNext() {
					return (sccIter != null && sccIter.hasNext()) || dfi.hasNext();
				}

				@Override
				public Node next() {
					if (sccIter != null && sccIter.hasNext()) {
						return sccIter.next();
					}
					sccIter = null;

					Object next = dfi.next();
					if (next instanceof Node) {
						return (Node) next;
					} else {
						sccIter = convertedToSCC.get(next).iterator();
						return sccIter.next();
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
}