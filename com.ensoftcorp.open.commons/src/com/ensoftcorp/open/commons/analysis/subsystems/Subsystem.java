package com.ensoftcorp.open.commons.analysis.subsystems;

import java.util.Arrays;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.open.commons.log.Log;

public abstract class Subsystem {

	public abstract String getName();
	public abstract String getDescription();
	public abstract String getTag();
	public abstract String[] getPackages();
	
	/**
	 * Adds the subystem tag if it does not exist
	 */
	public void tagSubsystem(){
		Log.info("Tagging " + getName() + " subsystem");
		for(String pkg : getPackages()){
			for(Node pkgNode : new AtlasHashSet<Node>(Common.pkg(pkg).eval().nodes())){
				pkgNode.tag(getTag());
			}
		}
	}
	
	/**
	 * Removes the subystem tag if it exists
	 */
	public void untagSubsystem(){
		Log.info("Removing " + getName() + " subsystem tags");
		for(String pkg : getPackages()){
			for(Node pkgNode : new AtlasHashSet<Node>(Common.pkg(pkg).eval().nodes())){
				pkgNode.tags().remove(getTag());
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String tag = getTag();
		String[] packages = getPackages();
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + Arrays.hashCode(packages);
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
		Subsystem other = (Subsystem) obj;

		String tag = getTag();
		String[] packages = getPackages();

		String otherTag = other.getTag();
		String[] otherPackages = other.getPackages();

		if (!Arrays.equals(packages, otherPackages)) {
			return false;
		}
		if (tag == null) {
			if (otherTag != null) {
				return false;
			}
		} else if (!tag.equals(otherTag)) {
			return false;
		}
		return true;
	}
	
}
