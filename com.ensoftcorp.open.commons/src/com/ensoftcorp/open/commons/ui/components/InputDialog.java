package com.ensoftcorp.open.commons.ui.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InputDialog extends Dialog {
	private String message;
	private String input;

	public InputDialog(Shell parent, String title, String message) {
		super(parent,  SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.ICON_QUESTION | SWT.RESIZE | SWT.NO_SCROLL);
		this.message = message;
		this.setText(title);
	}

	/**
	 * Displays text prompt and returns result
	 * Returns null if prompt was canceled
	 * @return
	 */
	public String open() {
		Shell shell = new Shell(getParent(), getStyle());

		// set title
		shell.setText(getText());
		
		// create form
		createContents(shell);
		shell.pack();
		
		// sets a minimum size to be the minimum displayable size
		// but gives a little extra width to feel less squished
		shell.setMinimumSize(shell.getBounds().width + 50, shell.getBounds().height);
		
		// center dialog
		Rectangle parentSize = getParent().getBounds();
		Rectangle shellSize = shell.getBounds();
		int locationX = (parentSize.width - shellSize.width)/2+parentSize.x;
		int locationY = (parentSize.height - shellSize.height)/2+parentSize.y;
		shell.setLocation(new Point(locationX, locationY));
		
		// prompt user
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return input;
	}

	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(2, true));

		Label label = new Label(shell, SWT.NONE);
		label.setText(message);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		final Text text = new Text(shell, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		text.setLayoutData(data);

		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		data = new GridData(GridData.FILL_HORIZONTAL);
		cancel.setLayoutData(data);
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				input = null;
				shell.close();
			}
		});
		
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("OK");
		data = new GridData(GridData.FILL_HORIZONTAL);
		ok.setLayoutData(data);
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				input = text.getText();
				shell.close();
			}
		});

		shell.setDefaultButton(ok);
	}
}