package com.ensoftcorp.open.commons.analysis.subsystems;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Subsystems {

	private static Set<Subsystem> SUBSYSTEMS = Collections.synchronizedSet(new HashSet<Subsystem>());
	
	/**
	 * Registers a new subsystem 
	 * @param subsystem
	 */
	public static synchronized void registerSubsystem(Subsystem subsystem){
		SUBSYSTEMS.add(subsystem);
	}
	
	/**
	 * Unregisters a subsystem
	 * @param subsystem
	 */
	public static synchronized void unregisterSubsystem(Subsystem subsystem){
		SUBSYSTEMS.remove(subsystem);
	}
	
	/**
	 * Applies all registered subsystem tags
	 */
	public static synchronized void tagSubsystems(){
		for(Subsystem subsystem : SUBSYSTEMS){
			subsystem.tagSubsystem();
		}
	}
	
	/**
	 * Removes all registered subsystem tags
	 */
	public static synchronized void untagSubsystems(){
		for(Subsystem subsystem : SUBSYSTEMS){
			subsystem.untagSubsystem();
		}
	}
	
}
