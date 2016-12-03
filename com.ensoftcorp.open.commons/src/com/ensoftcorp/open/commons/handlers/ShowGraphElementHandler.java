package com.ensoftcorp.open.commons.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;

/**
 * A menu handler for showing a specific graph element with a given address
 * 
 * @author Ben Holland
 */
public class ShowGraphElementHandler extends AbstractHandler {
	public ShowGraphElementHandler() {}

	/**
	 * Opens a prompt to enter a graph element address to show
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String address = DisplayUtils.promptString("Search GraphElement", "Enter GraphElement Address:");
		if(address != null){
			address = address.trim().toUpperCase();
			try {
				int hexAddress = Integer.parseInt(address,16);
				GraphElement ge = Graph.U.getAt(hexAddress);
				if(ge != null){
					DisplayUtils.show(ge, "GraphElement " + address);
				} else {
					DisplayUtils.showMessage("GraphElement " + address + " does not exist.");
				}
			} catch (NumberFormatException e){
				DisplayUtils.showError("GraphElement address must be entered in hexadecimal.");
			}
		}
		// returns the result of the execution (reserved for future use, must be null)
		return null;
	}
	
}