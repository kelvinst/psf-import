package org.psf.importer.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class ImportProjectSetSelectionPage extends WizardPage {

	/**
	 * Create the wizard.
	 */
	public ImportProjectSetSelectionPage() {
		super("importProjectSetSelectionPage");
		setTitle("Import Project Sets");
		setDescription("Select a directory to search for PSFs.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
	}

}
