package org.psf.importer.wizard;

import org.eclipse.jface.wizard.Wizard;

public class ImportProjectSetWizard extends Wizard {

	public ImportProjectSetWizard() {
		setWindowTitle("New Wizard");
	}

	@Override
	public void addPages() {
		addPage(new ImportProjectSetSelectionPage());
	}

	@Override
	public boolean performFinish() {
		return false;
	}

}
