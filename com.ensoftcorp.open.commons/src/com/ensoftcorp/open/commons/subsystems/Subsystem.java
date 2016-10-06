package com.ensoftcorp.open.commons.subsystems;

import java.util.Arrays;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.log.Log;

public abstract class Subsystem {

	public static String ROOT_SUBSYSTEM_TAG = "SUBSYSTEM";

	/**
	 * The display name of the subsystem
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * A short description of the subsystem
	 * 
	 * @return
	 */
	public abstract String getDescription();

	/**
	 * Defines the subystem tag
	 * 
	 * @return
	 */
	public abstract String getTag();

	/**
	 * Defines the parent tags in the subsystem tag hierarchy
	 * Subsystems may have multiple parents and multiple children
	 * 
	 * @return
	 */
	public String[] getParentTags() {
		return new String[]{ROOT_SUBSYSTEM_TAG};
	}

	/**
	 * The set of namespaces/folders that should be tagged in the subsystem
	 * 
	 * @return
	 */
	public String[] getNamespaces() {
		return new String[] {};
	}
	
	/**
	 * The set of types that should be tagged in the subsystem
	 * 
	 * Note: This finer grained resolution is for specifying individual types in
	 * a namespace. If the type is already covered by the namespace it is not
	 * necessary to specify it here.
	 * 
	 * @return
	 */
	public String[] getTypes() {
		return new String[] {};
	}

	/**
	 * Adds the subystem tag if it does not exist
	 */
	public void tagSubsystem() {
		Log.info("Tagging " + getName() + " subsystem");
		String[] pkgs = getNamespaces();
		if (pkgs != null) {
			for (String pkg : pkgs) {
				for (Node pkgNode : new AtlasHashSet<Node>(Common.pkg(pkg).eval().nodes())) {
					pkgNode.tag(getTag());
				}
			}
		}
	}

	/**
	 * Removes the subystem tag if it exists
	 */
	public void untagSubsystem() {
		Log.info("Removing " + getName() + " subsystem tags");
		String[] pkgs = getNamespaces();
		if (pkgs != null) {
			for (String pkg : pkgs) {
				for (Node pkgNode : new AtlasHashSet<Node>(Common.pkg(pkg).eval().nodes())) {
					pkgNode.tags().remove(getTag());
				}
			}
		}
	}

	/**
	 * Two subsystems are equivalent if they have the same tag and set of
	 * namespaces/folders
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String tag = getTag();
		String[] namespaces = getNamespaces();
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + Arrays.hashCode(namespaces);
		return result;
	}

	/**
	 * Two subsystems are equivalent if they have the same tag and set of
	 * namespaces
	 */
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
		String[] namespaces = getNamespaces();

		String otherTag = other.getTag();
		String[] otherNamespaces = other.getNamespaces();

		if (!Arrays.equals(namespaces, otherNamespaces)) {
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
