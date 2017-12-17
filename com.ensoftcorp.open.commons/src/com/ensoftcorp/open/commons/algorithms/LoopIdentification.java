package com.ensoftcorp.open.commons.algorithms;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.operation.ForwardGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.db.set.SingletonAtlasSet;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.log.Log;

/**
 * Uses algorithm from Wei et al. to identify loops, even irreducible ones.
 * 
 * "A New Algorithm for Identifying Loops in Decompilation". Static Analysis
 * Lecture Notes in Computer Science Volume 4634, 2007, pp 170-183
 * http://link.springer.com/chapter/10.1007%2F978-3-540-74061-2_11
 * http://www.lenx.100871.net/papers/loop-SAS.pdf
 * 
 * Original Implementation: https://github.com/EnSoftCorp/jimple-toolbox-commons
 * 
 * @author Ben Holland - modifications for general graph use
 */
public class LoopIdentification {

	private AtlasSet<Node> traversed, reentryNodes, irreducible;
	private AtlasSet<Edge> reentryEdges, loopbacks;
	private Graph graph;

	/** The node's position in the DFSP (Depth-first search path) */
	private Map<Node, Integer> dfsp;
	private Map<Node, Node> innermostLoopHeaders;
	private Node root;

	/**
	 * Identify all loop fragments, headers, re-entries, and nesting in the
	 * universe graph, applying the tags and attributes in interfaces CFGNode
	 * and CFGEdge.
	 * 
	 * NOTE: Handles both natural and irreducible loops
	 * 
	 * @return
	 */
	public LoopIdentification(Graph graph, Node root) {
		
		if(graph == null){
			throw new IllegalArgumentException("Parameter graph is null.");
		}
		
		if(root == null){
			throw new IllegalArgumentException("Parameter root is null.");
		}
		
		root = Common.toQ(graph).intersection(Common.toQ(root)).eval().nodes().one();
		
		if(root == null){
			throw new IllegalArgumentException("Parameter root is not contained in the graph.");
		}
		
		this.graph = graph;
		this.root = root;
		this.traversed = new AtlasHashSet<Node>();
		this.reentryNodes = new AtlasHashSet<Node>();
		this.reentryEdges = new AtlasHashSet<Edge>();
		this.irreducible = new AtlasHashSet<Node>();
		this.loopbacks = new AtlasHashSet<Edge>();
		this.dfsp = new HashMap<Node, Integer>();
		this.innermostLoopHeaders = new HashMap<Node, Node>();
		run();
	}

	private void run() {
		try {
			// clear data from previous function
			reentryNodes.clear();
			reentryEdges.clear();
			irreducible.clear();
			traversed.clear();
			innermostLoopHeaders.clear();
			loopbacks.clear();
			dfsp.clear();

			for (Node node : new ForwardGraph(graph, new SingletonAtlasSet<Node>(root)).nodes()) {
				dfsp.put(node, 0);
			}

			// run loop identification algorithm
			
			// a recursive strategy may overflow the call stack in some cases
			// so not using the loopDFSRecursive(root, 1) implementation
			// better to use an equivalent iterative strategy
			loopDFSIterative(root, 1); 
		} catch (Throwable t) {
			Log.error("Problem in loop analyzer for root:\n" + root, t);
		}
	}

	/**
	 * Recursively traverse the current node, returning its innermost loop
	 * header
	 * 
	 * @param b0
	 * @param position
	 * @return
	 */
	@SuppressWarnings("unused")
	private void loopDFSRecursive(Node b0, int position) {
		traversed.add(b0);
		dfsp.put(b0, position);

		for (Edge cfgEdge : graph.edges(b0, NodeDirection.OUT)) {
			Node b = cfgEdge.getNode(EdgeDirection.TO);

			if (!traversed.contains(b)) {
				// Paper Case A
				// new
				loopDFSRecursive(b, position + 1);
				Node nh = innermostLoopHeaders.get(b);
				tag_lhead(b0, nh);
			} else {
				if (dfsp.get(b) > 0) {
					// Paper Case B
					// Mark b as a loop header
					loopbacks.add(cfgEdge);
					tag_lhead(b0, b);
				} else {
					Node h = innermostLoopHeaders.get(b);
					if (h == null) {
						// Paper Case C
						// do nothing
						continue;
					}

					if (dfsp.get(h) > 0) {
						// Paper Case D
						// h in DFSP(b0)
						tag_lhead(b0, h);
					} else {
						// Paper Case E
						// h not in DFSP(b0)
						reentryNodes.add(b);
						reentryEdges.add(cfgEdge);
						irreducible.add(h);

						while ((h = innermostLoopHeaders.get(h)) != null) {
							if (dfsp.get(h) > 0) {
								tag_lhead(b0, h);
								break;
							}
							irreducible.add(h);
						}
					}
				}
			}
		}

		dfsp.put(b0, 0);
	}

	private void tag_lhead(Node b, Node h) {
		if (h == null || h.equals(b)){
			return;
		}
		
		Node cur1 = b;
		Node cur2 = h;

		Node ih;
		while ((ih = innermostLoopHeaders.get(cur1)) != null) {
			if (ih.equals(cur2)){
				return;
			}
			if (dfsp.get(ih) < dfsp.get(cur2)) {
				innermostLoopHeaders.put(cur1, cur2);
				cur1 = cur2;
				cur2 = ih;
			} else {
				cur1 = ih;
			}
		}
		innermostLoopHeaders.put(cur1, cur2);
	}

	private Deque<Frame> stack = new ArrayDeque<Frame>();

	private static class Frame {
		int programCounter = 0;
		Node b = null;
		Node b0 = null;
		int position = 0;
		Iterator<Edge> iterator = null;
	}

	private static final int ENTER = 0;
	private static final int EACH_CFG_EDGE = 1;
	private static final int POP = 2;

	/**
	 * Iterative implementation, equivalent to loopDFSRecursive()
	 * 
	 * @param b0
	 * @param position
	 * @return
	 */
	private void loopDFSIterative(Node _b0, int _position) {
		stack.clear();

		Frame f = new Frame();
		f.b0 = _b0;
		f.position = _position;
		f.programCounter = ENTER;

		stack.push(f);

		stack: while (!stack.isEmpty()) {
			f = stack.peek();

			switch (f.programCounter) {
				case POP: {
					Node nh = innermostLoopHeaders.get(f.b);
					tag_lhead(f.b0, nh);
					f.programCounter = EACH_CFG_EDGE;
					continue stack;
				}
				case ENTER:
					traversed.add(f.b0);
					dfsp.put(f.b0, f.position);
					f.iterator = graph.edges(f.b0, NodeDirection.OUT).iterator();
					// FALL THROUGH
				case EACH_CFG_EDGE:
					while (f.iterator.hasNext()) {
						Edge cfgEdge = f.iterator.next();
						f.b = cfgEdge.getNode(EdgeDirection.TO);
						if (!traversed.contains(f.b)) {
							// Paper Case A
							// new
							// BEGIN CONVERTED TO ITERATIVE
							// RECURSE: loopDFS(b, position + 1);
	
							f.programCounter = POP;
	
							Frame f2 = new Frame();
							f2.b0 = f.b;
							f2.position = f.position + 1;
							f2.programCounter = ENTER;
	
							stack.push(f2);
							continue stack;
	
							// case POP:
							// Node nh = innermostLoopHeaders.get(b);
							// tag_lhead(b0, nh);
	
							// END CONVERTED TO ITERATIVE
						} else {
							if (dfsp.get(f.b) > 0) {
								// Paper Case B
								// Mark b as a loop header
								loopbacks.add(cfgEdge);
								tag_lhead(f.b0, f.b);
							} else {
								Node h = innermostLoopHeaders.get(f.b);
								if (h == null) {
									// Paper Case C
									// do nothing
									continue;
								}
								
								if (dfsp.get(h) > 0) {
									// Paper Case D
									// h in DFSP(b0)
									tag_lhead(f.b0, h);
								} else {
									// Paper Case E
									// h not in DFSP(b0)
									reentryNodes.add(f.b);
									reentryEdges.add(cfgEdge);
									irreducible.add(h);
	
									while ((h = innermostLoopHeaders.get(h)) != null) {
										if (dfsp.get(h) > 0) {
											tag_lhead(f.b0, h);
											break;
										}
										irreducible.add(h);
									}
								}
							}
						}
					}
					
					dfsp.put(f.b0, 0);
					stack.pop();
			}
		}
	}

	public AtlasSet<Node> getReentryNodes() {
		return new AtlasHashSet<Node>(reentryNodes);
	}

	public AtlasSet<Node> getIrreducible() {
		return new AtlasHashSet<Node>(irreducible);
	}

	public AtlasSet<Edge> getReentryEdges() {
		return new AtlasHashSet<Edge>(reentryEdges);
	}

	public AtlasSet<Edge> getLoopbacks() {
		return new AtlasHashSet<Edge>(loopbacks);
	}

	public Map<Node, Node> getInnermostLoopHeaders() {
		return new HashMap<Node, Node>(innermostLoopHeaders);
	}
}
