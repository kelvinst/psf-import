package org.psf.importer.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

public class ImportProjectSetWizardAction implements IObjectActionDelegate {

	private IStructuredSelection selection;
	
	/**
	 * Constructor
	 */
	public ImportProjectSetWizardAction() {
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
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
