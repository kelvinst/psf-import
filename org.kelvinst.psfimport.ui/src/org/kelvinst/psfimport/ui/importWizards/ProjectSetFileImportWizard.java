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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.ITeamUIImages;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.wizards.PsfFilenameStore;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.kelvinst.psfimport.ui.ImportProjectSetOperation;
import org.xml.sax.SAXException;

public class ProjectSetFileImportWizard extends Wizard implements IImportWizard {
	ProjectSetFileImportWizardMainPage mainPage;

	public ProjectSetFileImportWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(TeamUIMessages.ProjectSetImportWizard_Project_Set_1);
	}

	public void addPages() {
		mainPage = new ProjectSetFileImportWizardMainPage(
				"projectSetMainPage", TeamUIMessages.ProjectSetImportWizard_Import_a_Project_Set_3, TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_PROJECTSET_IMPORT_BANNER)); //$NON-NLS-1$ 
		addPage(mainPage);
	}

	public boolean performFinish() {
		final boolean[] result = new boolean[] { false };
		try {
			new ImportProjectSetOperation(mainPage.isRunInBackgroundOn() ? null
					: getContainer(), mainPage.getFileName(),
					mainPage.getWorkingSets()).run();
			result[0] = true;
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof TeamException) {
				ErrorDialog.openError(getShell(), null, null,
						((TeamException) target).getStatus());
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
						new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, NLS.bind(
								TeamUIMessages.ProjectSetImportWizard_2,
								new String[] { target.getMessage() }), target));
				return false;
			}
			ErrorDialog.openError(
					getShell(),
					null,
					null,
					new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, NLS.bind(
							TeamUIMessages.ProjectSetImportWizard_3,
							new String[] { target.getMessage() }), target));
		}
		return result[0];
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// The code that finds "selection" is broken (it is always empty), so we
		// must dig for the selection in the workbench.
		PsfFilenameStore.getInstance().setDefaultFromSelection(workbench);
	}
}
