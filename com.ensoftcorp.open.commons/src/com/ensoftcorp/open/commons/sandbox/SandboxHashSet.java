package com.ensoftcorp.open.commons.sandbox;

import java.util.HashSet;

public class SandboxHashSet<T extends SandboxGraphElement> extends HashSet<T> {

	private static final long serialVersionUID = 1L;
	
	private final int sandboxInstanceID;
	
	public SandboxHashSet(int sandboxInstanceID){
		this.sandboxInstanceID = sandboxInstanceID;
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
			super.add(ge);
		} else {
			throw new RuntimeException("SandboxGraphElement [" + ge.getAddress() + "] is not in this sandbox!");
		}
		return false;
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
	
}
