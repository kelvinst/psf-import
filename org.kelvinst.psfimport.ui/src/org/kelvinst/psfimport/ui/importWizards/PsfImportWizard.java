/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.kelvinst.psfimport.ui.importWizards;

import java.io.File;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.kelvinst.psfimport.ui.core.ImportProjectSetJob;
import org.kelvinst.psfimport.ui.elements.FileElement;

public class PsfImportWizard extends Wizard implements IImportWizard {
	PsfImportWizardFilesSelectionPage filesPage;
	PsfImportWizardWorkingSetsSelectionPage workingSetsPage;

	public PsfImportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle("Team Project Set");
	}

	public void addPages() {
		filesPage = new PsfImportWizardFilesSelectionPage();
		addPage(filesPage);

		workingSetsPage = new PsfImportWizardWorkingSetsSelectionPage();
		addPage(workingSetsPage);
	}

	public boolean performFinish() {
		List<FileElement> resources = filesPage.getSelectedResources();

		for (FileElement element : resources) {
			File file = element.getFile();
			if (!file.isDirectory()) {
				new ImportProjectSetJob(file.getAbsolutePath(), workingSetsPage.getWorkingSets()).schedule();
			}
		}

		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

}
