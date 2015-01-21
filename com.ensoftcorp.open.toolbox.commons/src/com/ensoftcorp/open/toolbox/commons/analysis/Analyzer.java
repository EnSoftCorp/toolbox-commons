package com.ensoftcorp.open.toolbox.commons.analysis;

import com.ensoftcorp.atlas.core.query.Attr.Edge;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.open.toolbox.commons.SetDefinitions;

/**
 * A class containing common functionality needed for an analysis task.
 * 
 * This class is immutable. It should be instantiated with the script's
 * parameterized arguments, and the getter methods will do lazy evaluation as
 * necessary.
 * 
 * @author Tom Deering, Ben Holland
 * 
 */
public abstract class Analyzer {

	private Q envelope = null;

	protected void clearEnvelopeCache() {
		envelope = null;
	}

	/**
	 * Gets the name of this Script
	 * 
	 * @return
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Returns the script simple name
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Gets the description of this Script
	 * 
	 * @return
	 */
	public abstract String getDescription();

	/**
	 * Gets a human-readable description of the assumptions made by this script.
	 * 
	 * @return
	 */
	public abstract String[] getAssumptions();

	/**
	 * Constructs a new script without options
	 */
	public Analyzer() {}

	/**
	 * Evaluate and cache the analysis result
	 * @return
	 */
	public final Q getEnvelope() {
		if (envelope == null) {
			envelope = evaluateEnvelope();
		}
		return envelope;
	}

	/**
	 * Subclasses must implement the actual logic to compute an envelope.
	 * 
	 * @return
	 */
	protected abstract Q evaluateEnvelope();

	/**
	 * Can be used to preemptively extend in the given context which Atlas may
	 * not know about it. Not all toolbox inserted edges are tagged with special
	 * ENUMs Case and point resource indexer declares edges for the element
	 * structures.
	 * 
	 * @param q
	 * @param context
	 * @return
	 */
	public Q extendInContext(Q q, Q context) {
		return context.edgesTaggedWithAny(Edge.DECLARES).reverse(q).union(q);
	}

	/**
	 * A cache of the script context
	 */
	protected Q context = Common.universe();
	protected Q appContext = SetDefinitions.app();

	/**
	 * A valid script option that many scripts choose to utilize is returning
	 * results calculated in a given context. Setting the context also clears
	 * the cached envelope result.
	 * 
	 * @param context
	 */
	public void setContext(Q context) {
		this.context = context;
		appContext = context.intersection(SetDefinitions.app()).retainNodes().induce(context);
		clearEnvelopeCache();
	}

	/**
	 * A helper method for returning the current script context
	 * 
	 * @return
	 */
	public Q getContext() {
		return context;
	}

	/**
	 * A helper method for returning the current script app context
	 * 
	 * @return
	 */
	public Q getAppContext() {
		return appContext;
	}
}
