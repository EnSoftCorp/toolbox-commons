package com.ensoftcorp.open.commons.dashboard.handlers;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ensoftcorp.open.commons.dashboard.ui.DashboardView;
import com.ensoftcorp.open.commons.utils.DisplayUtils;

/**
 * A menu selection handler for opening the preferences page
 * 
 * @author Ben Holland
 */
public class OpenDashboardViewHandler extends AbstractHandler {
	public OpenDashboardViewHandler() {}

	/**
	 * Opens the permission usage view
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				try {
					IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
					IViewPart view = page.showView(DashboardView.ID);
					page.activate(view);
				} catch (PartInitException e) {
					DisplayUtils.showError(e, "Could not load Dashboard.");
				}
			}
		});
		
		return null;
	}
	
}

