package org.kelvinst.psfimport.ui.core;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.kelvinst.psfimport.ui.PsfImportPlugin;
import org.xml.sax.SAXException;

public class ImportProjectSetJob extends Job {

	private String psfFile;
	private IWorkingSet[] workingSets;

	public ImportProjectSetJob(String psfFile, IWorkingSet[] workingSets) {
		super("Importando psf \"" + psfFile + "\"...");
		this.psfFile = psfFile;
		this.workingSets = workingSets;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			IProject[] newProjects = ProjectSetImporter.importProjectSet(psfFile, getShell(), monitor);
			createWorkingSet(workingSets, newProjects);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof TeamException) {
				return ((TeamException) target).getStatus();
			}
			if (target instanceof SAXException) {
				return new Status(IStatus.ERROR, PsfImportPlugin.PLUGIN_ID, 0, NLS.bind("An error occurred while parsing the project set file: {0}",
						target.getMessage()), target);
			}
			return new Status(IStatus.ERROR, PsfImportPlugin.PLUGIN_ID, 0, NLS.bind("An error occurred while performing the project set import: {0}",
					target.getMessage()), target);
		}
		return new Status(IStatus.OK, PsfImportPlugin.PLUGIN_ID, 0, null, null);
	}

	/**
	 * Return a shell that can be used by the operation to display dialogs, etc.
	 * 
	 * @return a shell
	 */
	private Shell getShell() {
		final Shell[] shell = new Shell[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbench workbench = PsfImportPlugin.getDefault().getWorkbench();
				if (workbench != null) {
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					if (window != null) {
						shell[0] = window.getShell();
						return;
					}
				}

				Display display = Display.getCurrent();
				if (display == null) {
					display = Display.getDefault();
				}
				if (display.isDisposed()) {
					shell[0] = null;
					return;
				}
				shell[0] = new Shell(display);
			}
		});

		return shell[0];
	}

	private void createWorkingSet(IWorkingSet[] workingSets, IProject[] projects) {
		IWorkingSetManager manager = PsfImportPlugin.getDefault().getWorkbench().getWorkingSetManager();
		String workingSetName;
		for (int i = 0; i < workingSets.length; i++) {
			workingSetName = workingSets[i].getName();
			IWorkingSet oldSet = manager.getWorkingSet(workingSetName);
			if (oldSet == null) {
				IWorkingSet newSet = manager.createWorkingSet(workingSetName, projects);
				manager.addWorkingSet(newSet);
			} else {
				// don't overwrite the old elements
				IAdaptable[] tempElements = oldSet.getElements();
				IAdaptable[] adaptedProjects = oldSet.adaptElements(projects);
				IAdaptable[] finalElementList = new IAdaptable[tempElements.length + adaptedProjects.length];
				System.arraycopy(tempElements, 0, finalElementList, 0, tempElements.length);
				System.arraycopy(adaptedProjects, 0, finalElementList, tempElements.length, adaptedProjects.length);
				oldSet.setElements(finalElementList);
			}
		}
	}

}
