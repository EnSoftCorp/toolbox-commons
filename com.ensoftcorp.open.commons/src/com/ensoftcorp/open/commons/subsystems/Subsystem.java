package com.ensoftcorp.open.commons.subsystems;

import java.util.Arrays;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.script.Common;

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
	 * A unique category used for enabling/disabling groups of tagging instructions
	 * 
	 * This is a identifier key to assert that tagging instructions with 
	 * interdependencies are enabled/disabled together to maintain the consistency 
	 * of the subsystem hierarchy.
	 * 
	 * @return
	 */
	public abstract String getCategory();
	
	/**
	 * This is a human readable description of the category of subsystem tagging instructions
	 * 
	 * @return
	 */
	public abstract String getCategoryDescription();
	
	/**
	 * Defines the subystem tag
	 * 
	 * @return
	 */
	public abstract String getTag();

	/**
	 * Defines the parent tags in the subsystem tag hierarchy Subsystems may
	 * have multiple parents and multiple children
	 * 
	 * @return
	 */
	public String[] getParentTags() {
		return new String[] { ROOT_SUBSYSTEM_TAG };
	}

	/**
	 * The set of namespaces/folders that should be tagged in the subsystem
	 * 
	 * Format: "PackageName"
	 * Example: "java.lang"
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
	 * Format: "PackageName.TypeName"
	 * Example: "java.lang.Math"
	 * 
	 * @return
	 */
	public String[] getTypes() {
		return new String[] {};
	}
	
	/**
	 * The set of methods that should be tagged in the subsystem
	 * 
	 * Note: This finer grained resolution is for specifying individual methods in
	 * a namespace. If the type is already covered by the namespace it is not
	 * necessary to specify it here.
	 * 
	 * Format: "PackageName.TypeName MethodName"
	 * Example: "java.lang.Math random"
	 * 
	 * @return
	 */
	public String[] getMethods() {
		return new String[] {};
	}

	/**
	 * Adds the subystem tag if it does not exist
	 */
	public void tagSubsystem() {
//		Log.info("Tagging " + getName() + " subsystem...");
		String[] pkgs = getNamespaces();
		if (pkgs != null) {
			for (String pkg : pkgs) {
				for (Node pkgNode : new AtlasHashSet<Node>(Common.pkg(pkg).eval().nodes())) {
					pkgNode.tag(getTag());
				}
			}
		}
		String[] types = getTypes();
		if (types != null) {
			for (String type : types) {
				String typePackage = type.substring(0, type.lastIndexOf("."));
				String typeName = type.substring(type.lastIndexOf(".")+1);
				for (Node typeNode : new AtlasHashSet<Node>(Common.typeSelect(typePackage, typeName).eval().nodes())) {
					typeNode.tag(getTag());
				}
			}
		}
		String[] methods = getMethods();
		if (methods != null) {
			for (String method : methods) {
				String typePackage = method.substring(0, method.lastIndexOf("."));
				String typeName = method.substring(method.lastIndexOf(".")+1);
				String methodName = method.split(" ")[1];
				for (Node methodNode : new AtlasHashSet<Node>(Common.methodSelect(typePackage, typeName, methodName).eval().nodes())) {
					methodNode.tag(getTag());
				}
			}
		}
	}

	/**
	 * Removes the subystem tag if it exists
	 */
	public void untagSubsystem() {
//		Log.info("Removing " + getName() + " subsystem tags...");
		String[] pkgs = getNamespaces();
		if (pkgs != null) {
			for (String pkg : pkgs) {
				for (Node pkgNode : new AtlasHashSet<Node>(Common.pkg(pkg).eval().nodes())) {
					pkgNode.tags().remove(getTag());
				}
			}
		}
		String[] types = getTypes();
		if (types != null) {
			for (String type : types) {
				String typePackage = type.substring(0, type.lastIndexOf("."));
				String typeName = type.substring(type.lastIndexOf("."));
				for (Node typeNode : new AtlasHashSet<Node>(Common.typeSelect(typePackage, typeName).eval().nodes())) {
					typeNode.tags().remove(getTag());
				}
			}
		}
		String[] methods = getMethods();
		if (methods != null) {
			for (String method : methods) {
				String typePackage = method.substring(0, method.lastIndexOf("."));
				String typeName = method.substring(method.lastIndexOf(".")+1);
				String methodName = method.split(" ")[1];
				for (Node methodNode : new AtlasHashSet<Node>(Common.methodSelect(typePackage, typeName, methodName).eval().nodes())) {
					methodNode.tags().remove(getTag());
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "Subsystem [name=" + getName() + ", tag=" + getTag() + ", parents=" + Arrays.toString(getParentTags()) + "]";
	}

	/**
	 * Two subsystems are equivalent if they have the same tag, and set of parents
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getTag() == null) ? 0 : getTag().hashCode());
		result = prime * result + Arrays.hashCode(getParentTags());
		return result;
	}

	/**
	 * Two subsystems are equivalent if they have the same tag, and set of parents
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		Subsystem other = (Subsystem) obj;
		if (getTag() == null) {
			if (other.getTag() != null){
				return false;
			}
		} else if (!getTag().equals(other.getTag())){
			return false;
		}
		if (!Arrays.equals(getParentTags(), other.getParentTags())){
			return false;
		}
		return true;
	}
}
