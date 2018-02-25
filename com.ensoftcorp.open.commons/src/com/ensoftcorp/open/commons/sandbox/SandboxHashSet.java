package com.ensoftcorp.open.commons.sandbox;

import java.util.HashSet;

public class SandboxHashSet<T extends SandboxGraphElement> extends HashSet<T> {

	private static final long serialVersionUID = 1L;
	
	private final int sandboxInstanceID;
	
	public SandboxHashSet(int sandboxInstanceID){
		this.sandboxInstanceID = sandboxInstanceID;
	}
	
	public SandboxHashSet(Sandbox sandbox){
		this(sandbox.getInstanceID());
	}
	
	public SandboxHashSet(SandboxGraph graph) {
		this(graph.getSandboxInstanceID());
	}

	/**
	 * Returns the sandbox instance this graph element set belongs to
	 * @return
	 */
	public int getSandboxInstanceID(){
		return sandboxInstanceID;
	}
	
	@Override
	public boolean add(T ge){
		if(ge.getSandboxInstanceID() == sandboxInstanceID){
			return super.add(ge);
		} else {
			throw new RuntimeException("SandboxGraphElement [" + ge.getAddress() + "] is not in this sandbox!");
		}
	}
	
	/**
	 * Returns one item from the set
	 * @return
	 */
	public T one(){
		for(T t : this){
			return t;
		}
		return null;
	}
	
	public SandboxHashSet<T> filter(String attr, Object value){
		SandboxHashSet<T> result = new SandboxHashSet<T>(sandboxInstanceID);
		if(attr != null && value != null){
			for(T t : this){
				if(t.hasAttr(attr) && t.attr().get(attr) != null && t.attr().get(attr).equals(value)){
					result.add(t);
				}
			}
		}
		return result;
	}
	
}
