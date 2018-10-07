package com.ensoftcorp.open.commons.ui.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Adds drop down functionality to the toolbar menu item
 */
public class DropdownSelectionListener extends SelectionAdapter {
	private Menu menu;

	public DropdownSelectionListener(ToolItem dropdown) {
		this.menu = new Menu(dropdown.getParent().getShell());
	}

	/**
	 * Adds an item to the dropdown list
	 * 
	 * @param item the item to add
	 */
	public void add(String name, SelectionListener selectionListener) {
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(name);
		menuItem.addSelectionListener(selectionListener);
	}

	/**
	 * Creates the drop down menu
	 */
	public void widgetSelected(SelectionEvent event) {
		// the button or the arrow widget was selected
		// so draw the drop down list
		ToolItem item = (ToolItem) event.widget;
		Rectangle rect = item.getBounds();
		Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
		menu.setLocation(pt.x, pt.y + rect.height);
		menu.setVisible(true);
	}
}