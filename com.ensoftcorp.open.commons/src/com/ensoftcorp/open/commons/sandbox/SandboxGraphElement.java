package com.ensoftcorp.open.commons.sandbox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class SandboxGraphElement {

	private final int sandboxInstanceID;
	private String address;

	private final Set<String> tags;
	private final Map<String, Object> attributes;

	protected SandboxGraphElement(int sandboxInstanceID, String address) {
		this(sandboxInstanceID, address, new HashSet<String>(), new HashMap<String, Object>());
	}

	protected SandboxGraphElement(int sandboxInstanceID, String address, Set<String> tags, Map<String, Object> attributes) {
		this.sandboxInstanceID = sandboxInstanceID;
		this.address = address;
		this.tags = tags;
		this.attributes = attributes;
	}

	/**
	 * Returns the sandbox instance this graph element belongs to
	 * @return
	 */
	public int getSandboxInstanceID(){
		return sandboxInstanceID;
	}
	
	/**
	 * Returns the address of this sandbox graph element
	 * 
	 * @return
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Returns whether or not this sandbox graph element was originally a mirror
	 * or an Atlas graph element
	 * 
	 * @return
	 */
	public boolean isMirror() {
		return address.startsWith(Sandbox.SANDBOX_ADDRESS_PREFIX);
	}

	/**
	 * Updates the sandbox's temporary address with the flushed address
	 * @param address
	 */
	public void flush(String address){
		if(!isMirror()){
			this.address = address;
		}
	}
	
	/**
	 * Returns the set of tags currently applied to this sandbox graph element
	 * 
	 * @return
	 */
	public Set<String> tags() {
		return tags;
	}
	
	/**
	 * Adds the tag.
	 * @param tag
	 * @return
	 */
	public boolean tag(String tag){
		return tags().add(tag);
	}
	
	/**
	 * Returns true if tag is present
	 * @param tag
	 * @return
	 */
	public boolean taggedWith(String tag) {
		return tags().contains(tag);
	}

	/**
	 * Returns the attribute map currently associated to this sandbox graph
	 * element
	 * 
	 * @return
	 */
	public Map<String, Object> attr() {
		return attributes;
	}
	
	/**
	 * Adds the attribute value.
	 * @param key
	 * @param value
	 * @return
	 */
	public Object putAttr(String key, Object value){
		return attr().put(key, value);
	}
	
	/**
	 * Gets the attribute value.
	 * @param key
	 * @return
	 */
	public Object getAttr(String key){
		return attr().get(key);
	}
	
	/**
	 * Returns true if the attribute key is present
	 * @param tag
	 * @return
	 */
	public boolean hasAttr(String key){
		return attr().containsKey(key);
	}
	
	@Override
	public String toString() {
		StringBuilder tagsToString = new StringBuilder();
		for(String tag : tags){
			tagsToString.append(tag + "\n");
		}
		StringBuilder atrributesToString = new StringBuilder();
		for(String key : attributes.keySet()){
			atrributesToString.append(key + ": " + attributes.get(key).toString() + "\n");
		}
		return "Sandbox=" + sandboxInstanceID + ", Address=" + address + ", Mirror=" + isMirror() + "]"
				+ "\nTags: {" + tagsToString.toString().trim() + "}\nAttributes: {" + atrributesToString.toString().trim() + "}";
	}

	/**
	 * Sandbox graph elements are equal if they have the same address
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		return result;
	}

	/**
	 * Sandbox graph elements are equal if they have the same address
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SandboxGraphElement other = (SandboxGraphElement) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

}
