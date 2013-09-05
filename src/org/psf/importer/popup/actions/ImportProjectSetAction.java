package org.psf.importer.popup.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.wizards.ImportProjectSetOperation;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.statushandlers.StatusManager;
import org.psf.importer.PsfImportPlugin;

public class ImportProjectSetAction implements IObjectActionDelegate {

	private IStructuredSelection selection;
	
	/**
	 * Constructor
	 */
	public ImportProjectSetAction() {
		super();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			selection = (IStructuredSelection) sel;
		}
	}

	@Override
	public void run(IAction action) {
		final Shell shell = Display.getDefault().getActiveShell();
		try {
			new ProgressMonitorDialog(shell).run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					Iterator<?> iterator= selection.iterator();
					while (iterator.hasNext()) {
						// TODO Evolve this, got from eclipse's ImportProjectSetAction 
						IFile file = (IFile) iterator.next();
						ImportProjectSetOperation op = new ImportProjectSetOperation(null, file.getLocation().toString(), new IWorkingSet[0]);
						op.run();
					}
				}
			});
		} catch (InvocationTargetException exception) {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, PsfImportPlugin.PLUGIN_ID,
							IStatus.ERROR,
							"An error ocurred importing this project",
							exception.getTargetException()),
					StatusManager.LOG | StatusManager.SHOW);
		} catch (InterruptedException exception) {
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
