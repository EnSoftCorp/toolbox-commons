package com.ensoftcorp.open.commons.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A menu handler for configuring the toolbox commons analysis preferences
 * 
 * @author Ben Holland
 */
public class ToolboxPreferencesHandler extends AbstractHandler {
	public ToolboxPreferencesHandler() {}

	/**
	 * Opens the Atlas Toolbox preferences root menu and filters the result to
	 * the root menu and its children
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String id = "com.ensoftcorp.open.commons.ui.toolbox.preferences";
		
		ArrayList<String> preferencePageTree = new ArrayList<String>();
		
		PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
	    List<IPreferenceNode> preferences = pm.getElements(PreferenceManager.PRE_ORDER);
	    for(IPreferenceNode page : preferences){
	    	if(page.getId().equals(id)){
	    		HashSet<String> pageIds = new HashSet<String>();
	    		findSubPages(page, pageIds);
	    		preferencePageTree.addAll(pageIds);
	    		break;
	    	}
	    }
		
	    String[] ids = new String[preferencePageTree.size()];
	    preferencePageTree.toArray(ids);
		return PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), id, ids, null).open();
	}
	
	private void findSubPages(IPreferenceNode root, HashSet<String> pageIds){
		pageIds.add(root.getId());
		for(IPreferenceNode page : root.getSubNodes()){
			findSubPages(page, pageIds);
		}
	}
	
}