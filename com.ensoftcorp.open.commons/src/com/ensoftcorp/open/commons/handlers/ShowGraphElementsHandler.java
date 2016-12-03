package com.ensoftcorp.open.commons.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.utilities.DisplayUtils;

/**
 * A menu handler for showing a specific graph element with a given address
 * 
 * @author Ben Holland
 */
public class ShowGraphElementsHandler extends AbstractHandler {
	public ShowGraphElementsHandler() {}

	/**
	 * Opens a prompt to enter a graph element address to show
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String addresses = DisplayUtils.promptString("Search GraphElement", "Enter GraphElement Addresses (comma seperated):");
		if(addresses != null){
			// remove all whitespace and convert to lowercase
			addresses = addresses.replaceAll("\\s","").toLowerCase();
			AtlasSet<GraphElement> graphElements = new AtlasHashSet<GraphElement>();
			for(String address : addresses.split(",")){
				try {
					int hexAddress = Integer.parseInt(address,16);
					GraphElement ge = Graph.U.getAt(hexAddress);
					if(ge != null){
						graphElements.add(ge);
					} else {
						DisplayUtils.showMessage("GraphElement " + address + " does not exist.");
						break;
					}
					
				} catch (NumberFormatException e){
					DisplayUtils.showError("GraphElement address (" + address + ") is not a hexadecimal address.");
					break;
				}
			}
			DisplayUtils.show(Common.toQ(graphElements), "GraphElements " + addresses);
		}
		// returns the result of the execution (reserved for future use, must be null)
		return null;
	}
	
}