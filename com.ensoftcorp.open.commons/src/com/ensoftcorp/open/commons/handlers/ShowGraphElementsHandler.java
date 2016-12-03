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
		String addresses = DisplayUtils.promptString("Search Graph Elements", "Enter graph element addresses (comma seperated):", false);
		if(addresses != null){
			// remove all whitespace and convert to lowercase
			addresses = addresses.replaceAll("\\s","").toLowerCase();
			if(addresses.equals("")){
				DisplayUtils.showError("No graph element addresses were entered.");
			} else {
				boolean showGraph = true;
				AtlasSet<GraphElement> graphElements = new AtlasHashSet<GraphElement>();
				for(String address : addresses.split(",")){
					try {
						int hexAddress = Integer.parseInt(address, 16);
						GraphElement ge = Graph.U.getAt(hexAddress);
						if(ge != null){
							graphElements.add(ge);
						} else {
							DisplayUtils.showError("Graph element [" + address + "] does not exist.");
							showGraph = false;
							break;
						}
					} catch (NumberFormatException e){
						DisplayUtils.showError("Graph element address [" + address + "] must be hexadecimal.");
						showGraph = false;
						break;
					}
				}
				if(showGraph){
					DisplayUtils.show(Common.toQ(graphElements), "Graph Elements [" + addresses + "]");
				}
			}
		}
		// returns the result of the execution (reserved for future use, must be null)
		return null;
	}
	
}