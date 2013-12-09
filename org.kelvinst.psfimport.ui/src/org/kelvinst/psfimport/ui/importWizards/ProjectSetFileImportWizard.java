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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.kelvinst.psfimport.ui.FileElement;
import org.kelvinst.psfimport.ui.ImportProjectSetOperation;
import org.kelvinst.psfimport.ui.PsfImportPlugin;
import org.xml.sax.SAXException;

public class ProjectSetFileImportWizard extends Wizard implements IImportWizard {
	ProjectSetFileImportFilesSelectionPage filesPage;
	ProjectSetFileImportWorkingSetsSelectionPage workingSetsPage;
	private IStructuredSelection selection;

	public ProjectSetFileImportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle("Team Project Set");
	}

	public void addPages() {
		filesPage = new ProjectSetFileImportFilesSelectionPage(selection);
		addPage(filesPage);

		workingSetsPage = new ProjectSetFileImportWorkingSetsSelectionPage();
		addPage(workingSetsPage);
	}

	public boolean performFinish() {
		final boolean[] result = new boolean[] { false };
		try {
			List<FileElement> resources = filesPage.getSelectedResources();

			for (FileElement element : resources) {
				File file = element.getFile();
				if (!file.isDirectory()) {
					new ImportProjectSetOperation(workingSetsPage.isRunInBackgroundOn() ? null : getContainer(), file.getCanonicalPath(),
						workingSetsPage.getWorkingSets()).run();
				}
			}

			result[0] = true;
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof TeamException) {
				ErrorDialog.openError(getShell(), null, null, ((TeamException) target).getStatus());
				return false;
			}
			if (target instanceof RuntimeException) {
				throw (RuntimeException) target;
			}
			if (target instanceof Error) {
				throw (Error) target;
			}
			if (target instanceof SAXException) {
				ErrorDialog.openError(
						getShell(),
						null,
						null,
						new Status(IStatus.ERROR, PsfImportPlugin.PLUGIN_ID, 0, NLS.bind("An error occurred while parsing the project set file: {0}",
								new String[] { target.getMessage() }), target));
				return false;
			}
			ErrorDialog.openError(
					getShell(),
					null,
					null,
					new Status(IStatus.ERROR, PsfImportPlugin.PLUGIN_ID, 0, NLS.bind(
							"An error occurred while performing the project set import: {0}", new String[] { target.getMessage() }), target));
		} catch (IOException e) {
			ErrorDialog.openError(
					getShell(),
					null,
					null,
					new Status(IStatus.ERROR, PsfImportPlugin.PLUGIN_ID, 0, NLS.bind(
							"An error occurred while performing the project set import: {0}", new String[] { e.getMessage() }), e));
		}
		return result[0];
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
