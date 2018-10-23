package com.ensoftcorp.open.commons.codemap;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class PrioritizedCodemapStage {

	/**
	 * Returns the display name of the codemap stage
	 * @return
	 */
	public abstract String getDisplayName();

	/**
	 * A unique string that serves as the identity of this PrioritizedCodemapStage
	 * @return
	 */
	public abstract String getIdentifier();

	/**
	 * Returns a set of prioritized codemap stage identifier strings that should
	 * perform indexing before this prioritized codemap stage
	 * 
	 * Note: this should represent a conservative set of the worst case dependencies. 
	 * That is, if it is possible that under some configuration of the toolbox that
	 * the toolbox could depend on a prioritized codemap stage, then it should be listed here.
	 * 
	 * Note: mutual dependencies are not supported!
	 * 
	 * @return
	 */
	public abstract String[] getCodemapStageDependencies();
	
	/**
	 * The codemap stage indexing task to be performed. 
	 * 
	 * @param monitor
	 * 
	 * @return Returns true if the codemap stage was actually run.
	 */
	public abstract boolean performIndexing(IProgressMonitor monitor);
	
	@Override
	public String toString() {
		return getIdentifier();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
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
		PrioritizedCodemapStage other = (PrioritizedCodemapStage) obj;
		if (getIdentifier() == null) {
			if (other.getIdentifier() != null)
				return false;
		} else if (!getIdentifier().equals(other.getIdentifier()))
			return false;
		return true;
	}
	
}