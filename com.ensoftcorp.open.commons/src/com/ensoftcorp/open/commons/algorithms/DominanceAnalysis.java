package com.ensoftcorp.open.commons.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.DominanceAnalysis.DominanceAnalysisHelper.Multimap;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.codemap.PrioritizedCodemapStage;
import com.ensoftcorp.open.commons.log.Log;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;
import com.ensoftcorp.open.commons.xcsg.XCSG_Extension;

public class DominanceAnalysis extends PrioritizedCodemapStage {

	/**
	 * The unique identifier for the PDG codemap stage
	 */
	public static final String IDENTIFIER = "com.ensoftcorp.open.commons.dominance";
	
	/**
	 * Used to tag the edges from a node that immediately forward dominate
	 * (post-dominate) a node. The set of ifdom edges forms the dominator tree.
	 * 
	 * Wikipedia: The immediate dominator or idom of a node n is the unique node
	 * that strictly dominates n but does not strictly dominate any other node
	 * that strictly dominates n. Every node, except the entry node, has an
	 * immediate dominator. Analogous to the definition of dominance above, a
	 * node z is said to post-dominate a node n if all paths to the exit node of
	 * the graph starting at n must go through z. Similarly, the immediate
	 * post-dominator of a node n is the postdominator of n that doesn't
	 * strictly postdominate any other strict postdominators of n. A dominator
	 * tree is a tree where each node's children are those nodes it immediately
	 * dominates. Because the immediate dominator is unique, it is a tree. The
	 * start node is the root of the tree.
	 */
	@XCSG_Extension
	public static final String IMMEDIATE_FORWARD_DOMINANCE_EDGE = "ifdom";
	
	/**
	 * Used to tag the edges from a node the identify the node's dominance
	 * frontier.
	 * 
	 * Wikipedia: The dominance frontier of a node d is the set of all nodes n
	 * such that d dominates an immediate predecessor of n, but d does not
	 * strictly dominate n. It is the set of nodes where d's dominance stops.
	 */
	@XCSG_Extension
	public static final String DOMINANCE_FRONTIER_EDGE = "domfontier";
	
	public DominanceAnalysis() {}
	
	public static Q getForwardDominanceTrees(){
		return Common.universe().edges(IMMEDIATE_FORWARD_DOMINANCE_EDGE).retainEdges();
	}
	
	public static Q getDominanceFrontiers(){
		return Common.universe().edges(DOMINANCE_FRONTIER_EDGE).retainEdges();
	}

	@Override
	public String getDisplayName() {
		return "Computing Control Flow Graph Dominator Trees";
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String[] getCodemapStageDependencies() {
		return new String[]{};
	}

	@Override
	public void performIndexing(IProgressMonitor monitor) {
		if(CommonsPreferences.isComputeControlFlowGraphDominanceTreesEnabled() || CommonsPreferences.isComputeExceptionalControlFlowGraphDominanceTreesEnabled()){
			Log.info("Computing Control Flow Graph Dominator Trees");
			AtlasSet<Node> functions = Common.universe().nodes(XCSG.Function).eval().nodes();
			SubMonitor task = SubMonitor.convert(monitor, (int) functions.size());
			int functionsCompleted = 0;
			for(Node function : functions){
				Q cfg;
				if(CommonsPreferences.isComputeControlFlowGraphDominanceTreesEnabled()){
					cfg = CommonQueries.cfg(function);
				} else {
					cfg = CommonQueries.excfg(function);
				}
				Graph g = cfg.eval();
				AtlasSet<Node> roots = cfg.nodes(XCSG.controlFlowRoot).eval().nodes();
				AtlasSet<Node> exits = cfg.nodes(XCSG.controlFlowExitPoint).eval().nodes();
				if(g.nodes().isEmpty() || roots.isEmpty() || exits.isEmpty()){
					// nothing to compute
					task.setWorkRemaining(((int) functions.size())-(functionsCompleted++));
					continue;
				} else {
					try {
						UniqueEntryExitGraph uexg = new UniqueEntryExitControlFlowGraph(g, roots, exits, CommonsPreferences.isMasterEntryExitContainmentRelationshipsEnabled());
						computeDominance(uexg);
					} catch (Exception e){
						Log.error("Error computing control flow graph dominance tree", e);
					}
					if(monitor.isCanceled()){
						Log.warning("Cancelled: Computing Control Flow Graph Dominator Trees");
						break;
					}
					task.setWorkRemaining(((int) functions.size())-(functionsCompleted++));
				}
			}
		}
	}
	
	/**
	 * Returns the dominator tree
	 * 
	 * Note this method will compute both the dominance frontier and the
	 * dominator tree. If you want both edges, call the
	 * computeDominance(UniqueEntryExitGraph ucfg) method directly.
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computeForwardDominatorTree(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> treeEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(IMMEDIATE_FORWARD_DOMINANCE_EDGE)){
				treeEdges.add(edge);
			}
		}
		return Common.toQ(treeEdges).eval();
	}
	
	/**
	 * Returns the dominance frontier (each edge represents a frontier node for
	 * a given from node)
	 * 
	 * Note this method will compute both the dominance frontier and the
	 * dominator tree. If you want both edges, call the
	 * computeDominance(UniqueEntryExitGraph ucfg) method directly.
	 * 
	 * @param ucfg
	 * @return
	 */
	public static Graph computeDominanceFrontier(UniqueEntryExitGraph ucfg){
		AtlasSet<Edge> frontierEdges = new AtlasHashSet<Edge>();
		Graph dominance = computeDominance(ucfg);
		for(Edge edge : dominance.edges()){
			if(edge.taggedWith(DOMINANCE_FRONTIER_EDGE)){
				frontierEdges.add(edge);
			}
		}
		return Common.toQ(frontierEdges).eval();
	}

	/**
	 * Returns a graph of ifdom and domfrontier edges
	 * @param ucfg
	 * @return
	 */
	public static Graph computeDominance(UniqueEntryExitGraph ucfg) {
		DominanceAnalysisHelper dominanceAnalysis = new DominanceAnalysisHelper(ucfg, true);
		// compute the dominator tree
		Multimap<Node> dominanceTree = dominanceAnalysis.getDominatorTree();
		AtlasSet<Edge> dominanceEdges = new AtlasHashSet<Edge>();
		for(Entry<Node, Set<Node>> entry : dominanceTree.entrySet()){
			Node fromNode = entry.getKey();
			for(Node toNode : entry.getValue()){
				Q forwardDominanceEdges = Common.universe().edges(IMMEDIATE_FORWARD_DOMINANCE_EDGE);
				Edge forwardDominanceEdge = forwardDominanceEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
				if(forwardDominanceEdge == null){
					forwardDominanceEdge = Graph.U.createEdge(fromNode, toNode);
					forwardDominanceEdge.tag(IMMEDIATE_FORWARD_DOMINANCE_EDGE);
					forwardDominanceEdge.putAttr(XCSG.name, IMMEDIATE_FORWARD_DOMINANCE_EDGE);
				}
				dominanceEdges.add(forwardDominanceEdge);
			}
		}
		// compute the dominance frontier
		Multimap<Node> dominanceFrontier = dominanceAnalysis.getDominanceFrontiers();
		for(Entry<Node, Set<Node>> entry : dominanceFrontier.entrySet()){
			Node fromNode = entry.getKey();
			for(Node toNode : entry.getValue()){
				Q dominanceFrontierEdges = Common.universe().edges(DOMINANCE_FRONTIER_EDGE);
				Edge dominanceFrontierEdge = dominanceFrontierEdges.betweenStep(Common.toQ(fromNode), Common.toQ(toNode)).eval().edges().one();
				if(dominanceFrontierEdge == null){
					dominanceFrontierEdge = Graph.U.createEdge(fromNode, toNode);
					dominanceFrontierEdge.tag(DOMINANCE_FRONTIER_EDGE);
					dominanceFrontierEdge.putAttr(XCSG.name, DOMINANCE_FRONTIER_EDGE);
				}
				dominanceEdges.add(dominanceFrontierEdge);
			}
		}
		return Common.toQ(dominanceEdges).eval();
	}
	
	/**
	 * An implementation of the O(n log n) Lengauer-Tarjan algorithm for building
	 * the <a href="http://en.wikipedia.org/wiki/Dominator_%28graph_theory%29">dominator
	 * tree</a> of a {@link ControlFlowGraph cfg}.
	 */
	protected static class DominanceAnalysisHelper {
		/**
		 * Control flow graph for dominance computation
		 */
		private UniqueEntryExitGraph graph;

		/**
		 * To compute dominators or post-dominators
		 */
		private boolean postdom;

		/**
		 * Semidominator numbers by block.
		 */
		private Map<Node, Integer> semi = new HashMap<Node, Integer>();

		/**
		 * Parents by block.
		 */
		private Map<Node, Node> parent = new HashMap<Node, Node>();

		/**
		 * Predecessors by block.
		 */
		private Multimap<Node> pred = new Multimap<Node>();

		/**
		 * Blocks in DFS order; used to look up a block from its semidominator
		 * numbering.
		 */
		private ArrayList<Node> vertex = new ArrayList<Node>();

		/**
		 * Blocks by semidominator block.
		 */
		private Multimap<Node> bucket = new Multimap<Node>();

		/**
		 * idominator map, built iteratively.
		 */
		private Map<Node, Node> idom = new HashMap<Node, Node>();

		/**
		 * Dominance frontiers of this dominator tree, built on demand.
		 */
		private Multimap<Node> dominanceFrontiers = null;

		/**
		 * Dominator tree, built on demand from the idominator map.
		 */
		private Multimap<Node> dominatorTree = null;

		/**
		 * Auxiliary data structure used by the O(m log n) eval/link implementation:
		 * ancestor relationships in the forest (the processed tree as it's built
		 * back up).
		 */
		private Map<Node, Node> ancestor = new HashMap<Node, Node>();

		/**
		 * Auxiliary data structure used by the O(m log n) eval/link implementation:
		 * node with least semidominator seen during traversal of a path from node
		 * to subtree root in the forest.
		 */
		private Map<Node, Node> label = new HashMap<Node, Node>();

		/**
		 * A topological traversal of the dominator tree, built on demand.
		 */
		private LinkedList<Node> topologicalTraversalImpl = null;

		/**
		 * Construct a DominatorTree from a root.
		 * 
		 * @param root
		 *            the root of the graph.
		 */
		public DominanceAnalysisHelper(UniqueEntryExitGraph graph, boolean postdom) {
			this.graph = graph;
			this.postdom = postdom;
			if (this.postdom) {
				this.dfs(this.graph.getExitNode());
			} else {
				this.dfs(this.graph.getEntryNode());
			}
			this.computeDominators();
		}

		/**
		 * Create and/or fetch the map of immediate dominators.
		 * 
		 * @return the map from each block to its immediate dominator (if it has
		 *         one).
		 */
		public Map<Node, Node> getIdoms() {
			return this.idom;
		}

		/**
		 * Compute and/or fetch the dominator tree as a Multimap.
		 * 
		 * @return the dominator tree.
		 */
		public Multimap<Node> getDominatorTree() {
			if (this.dominatorTree == null) {
				this.dominatorTree = new Multimap<Node>();
				for (Node node : this.idom.keySet()){
					dominatorTree.get(this.idom.get(node)).add(node);
				}
			}
			return this.dominatorTree;
		}

		/**
		 * Compute and/or fetch the dominance frontiers as a Multimap.
		 * 
		 * @return a Multimap where the set of nodes mapped to each key node is the
		 *         set of nodes in the key node's dominance frontier.
		 */
		public Multimap<Node> getDominanceFrontiers() {
			if (this.dominanceFrontiers == null) {
				this.dominanceFrontiers = new Multimap<Node>();

				getDominatorTree(); // touch the dominator tree

				for (Node x : reverseTopologicalTraversal()) {
					Set<Node> dfx = this.dominanceFrontiers.get(x);

					// Compute DF(local)
					for (Node y : getSuccessors(x)){
						if (idom.get(y) != x){
							dfx.add(y);
						}
					}

					// Compute DF(up)
					for (Node z : this.dominatorTree.get(x)){
						for (Node y : this.dominanceFrontiers.get(z)){
							if (idom.get(y) != x){
								dfx.add(y);
							}
						}
					}
				}
			}

			return this.dominanceFrontiers;
		}

		/**
		 * Create and/or fetch a topological traversal of the dominator tree, such
		 * that for every node, idom(node) appears before node.
		 * 
		 * @return the topological traversal of the dominator tree, as an immutable
		 *         List.
		 */
		public List<Node> topologicalTraversal() {
			return Collections.unmodifiableList(getToplogicalTraversalImplementation());
		}

		/**
		 * Create and/or fetch a reverse topological traversal of the dominator
		 * tree, such that for every node, node appears before idom(node).
		 * 
		 * @return a reverse topological traversal of the dominator tree, as an
		 *         immutable List.
		 */
		public Iterable<Node> reverseTopologicalTraversal() {
			return new Iterable<Node>() {
				@Override
				public Iterator<Node> iterator() {
					return getToplogicalTraversalImplementation().descendingIterator();
				}
			};
		}

		/**
		 * Depth-first search the graph and initialize data structures.
		 * 
		 * @param roots
		 *            the root(s) of the flowgraph. One of these is the start block,
		 *            the others are exception handlers.
		 */
		private void dfs(Node entryNode) {
			Iterator<Node> it = new DepthFirstPreorderIterator(this.graph, entryNode, this.postdom);

			while (it.hasNext()) {
				Node node = it.next();

				if (!semi.containsKey(node)) {
					vertex.add(node);

					// Initial assumption: the node's semidominator is itself.
					semi.put(node, semi.size());
					label.put(node, node);

					for (Node child : getSuccessors(node)) {
						pred.get(child).add(node);
						if (!semi.containsKey(child)) {
							parent.put(child, node);
						}
					}
				}
			}
		}

		/**
		 * Steps 2, 3, and 4 of Lengauer-Tarjan.
		 */
		private void computeDominators() {
			int lastSemiNumber = semi.size() - 1;

			for (int i = lastSemiNumber; i > 0; i--) {
				Node w = vertex.get(i);
				Node p = this.parent.get(w);

				// step 2: compute semidominators
				// for each v in pred(w)...
				int semidominator = semi.get(w);
				for (Node v : pred.get(w)){
					semidominator = Math.min(semidominator, semi.get(eval(v)));
				}

				semi.put(w, semidominator);
				bucket.get(vertex.get(semidominator)).add(w);

				// Link w into the forest via its parent, p
				link(p, w);

				// step 3: implicitly compute idominators
				// for each v in bucket(parent(w)) ...
				for (Node v : bucket.get(p)) {
					Node u = eval(v);
					if (semi.get(u) < semi.get(v)){
						idom.put(v, u);
					} else {
						idom.put(v, p);
					}
				}

				bucket.get(p).clear();
			}

			// step 4: explicitly compute idominators
			for (int i = 1; i <= lastSemiNumber; i++) {
				Node w = vertex.get(i);
				if (idom.get(w) != vertex.get((semi.get(w)))){
					idom.put(w, idom.get(idom.get(w)));
				}
			}
		}

		/**
		 * Extract the node with the least-numbered semidominator in the (processed)
		 * ancestors of the given node.
		 * 
		 * @param v
		 *            - the node of interest.
		 * @return "If v is the root of a tree in the forest, return v. Otherwise,
		 *         let r be the root of the tree which contains v. Return any vertex
		 *         u != r of miniumum semi(u) on the path r-*v."
		 */
		private Node eval(Node v) {
			// This version of Lengauer-Tarjan implements
			// eval(v) as a path-compression procedure.
			compress(v);
			return label.get(v);
		}

		/**
		 * Traverse ancestor pointers back to a subtree root, then propagate the
		 * least semidominator seen along this path through the "label" map.
		 */
		private void compress(Node v) {
			Stack<Node> worklist = new Stack<Node>();
			worklist.add(v);

			Node a = this.ancestor.get(v);

			// Traverse back to the subtree root.
			while (this.ancestor.containsKey(a)) {
				worklist.push(a);
				a = this.ancestor.get(a);
			}

			// Propagate semidominator information forward.
			Node ancestor = worklist.pop();
			int leastSemi = semi.get(label.get(ancestor));

			while (!worklist.empty()) {
				Node descendent = worklist.pop();
				int currentSemi = semi.get(label.get(descendent));

				if (currentSemi > leastSemi){
					label.put(descendent, label.get(ancestor));
				} else {
					leastSemi = currentSemi;
				}

				// Prepare to process the next iteration.
				ancestor = descendent;
			}
		}

		/**
		 * Simple version of link(parent,child) simply links the child into the
		 * parent's forest, with no attempt to balance the subtrees or otherwise
		 * optimize searching.
		 */
		private void link(Node parent, Node child) {
			this.ancestor.put(child, parent);
		}

		/**
		 * Multimap maps a key to a set of values.
		 */
		@SuppressWarnings("serial")
		public static class Multimap<T> extends HashMap<T, Set<T>> {
			/**
			 * Fetch the set for a given key, creating it if necessary.
			 * 
			 * @param key
			 *            - the key.
			 * @return the set of values mapped to the key.
			 */
			@SuppressWarnings("unchecked")
			@Override
			public Set<T> get(Object key) {
				if (!this.containsKey(key))
					this.put((T) key, new HashSet<T>());

				return super.get(key);
			}
		}

		/**
		 * Create/fetch the topological traversal of the dominator tree.
		 * 
		 * @return {@link this.topologicalTraversal}, the traversal of the dominator
		 *         tree such that for any node n with a dominator, n appears before
		 *         idom(n).
		 */
		private LinkedList<Node> getToplogicalTraversalImplementation() {
			if (this.topologicalTraversalImpl == null) {
				this.topologicalTraversalImpl = new LinkedList<Node>();
				for (Node node : this.vertex) {
					int idx = this.topologicalTraversalImpl.indexOf(this.idom.get(node));
					if (idx != -1){
						this.topologicalTraversalImpl.add(idx + 1, node);
					} else {
						this.topologicalTraversalImpl.add(node);
					}
				}
			}
			return this.topologicalTraversalImpl;
		}

		private AtlasSet<Node> getSuccessors(Node node) {
			if (postdom) {
				return this.graph.getPredecessors(node);
			} else {
				return this.graph.getSuccessors(node);
			}
		}
	}

}
