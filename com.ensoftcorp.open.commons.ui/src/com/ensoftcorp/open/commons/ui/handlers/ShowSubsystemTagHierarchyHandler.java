package com.ensoftcorp.open.commons.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.viewer.graph.XCSGHierarchyGraphUtil;
import com.ensoftcorp.open.commons.subsystems.Subsystem;
import com.ensoftcorp.open.commons.ui.utilities.DisplayUtils;

/**
 * A menu handler for the complete subsystem tag hierarchy
 * 
 * @author Ben Holland
 */
public class ShowSubsystemTagHierarchyHandler extends AbstractHandler {
	public ShowSubsystemTagHierarchyHandler() {}

	/**
	 * Opens a prompt to enter a graph element address to show
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// check to see if the subsystems have been registered yet
		boolean subsystemTagHierarchyExists = false;
		for(String tag : XCSG.HIERARCHY.registeredTags()){
			if(tag.equals(Subsystem.ROOT_SUBSYSTEM_TAG)){
				subsystemTagHierarchyExists = true;
			}
		}
		
		// show the subsystem hierarchy
		if(subsystemTagHierarchyExists){
			Q hierarchy = XCSGHierarchyGraphUtil.getXCSGHiearchyQ(Subsystem.ROOT_SUBSYSTEM_TAG);
			DisplayUtils.show(hierarchy, "Subsystem Hierarchy");
		} else {
			DisplayUtils.showError("Subsystems have not been registered yet!");
		}

		// returns the result of the execution (reserved for future use, must be null)
		return null;
	}
	
}