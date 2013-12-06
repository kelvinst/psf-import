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
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.internal.ui.ProjectSetImporter;
import org.eclipse.team.internal.ui.wizards.PsfFilenameStore;
import org.eclipse.ui.IWorkingSet;
import org.kelvinst.psfimport.ui.IPreferenceIds;
import org.kelvinst.psfimport.ui.PsfImportPlugin;

public class ProjectSetFileImportFilesSelectionPage extends WizardPage {
	Combo fileCombo;
	String file = ""; //$NON-NLS-1$
	Button browseButton;

	private int messageType = NONE;

	private PsfFilenameStore psfFilenameStore = PsfFilenameStore.getInstance();
	private Label lblFile;

	public ProjectSetFileImportFilesSelectionPage() {
		super("projectSetFilesPage", "Import project sets", null);
		setDescription("Select the files to import.");
	}

	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		// GridLayout
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 1;
		composite.setLayout(layout1);

		// GridData
		GridData data1 = new GridData();
		data1.verticalAlignment = GridData.FILL;
		data1.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data1);
		initializeDialogUnits(composite);

		Composite inner = new Composite(composite, SWT.NULL);
		inner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);
		
		lblFile = new Label(inner, SWT.NONE);
		lblFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFile.setText("F&ile:");

		fileCombo = new Combo(inner, SWT.DROP_DOWN);
		GridData comboData = new GridData(GridData.FILL_HORIZONTAL);
		comboData.verticalAlignment = GridData.CENTER;
		comboData.grabExcessVerticalSpace = false;
		comboData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		fileCombo.setLayoutData(comboData);

		file = psfFilenameStore.getSuggestedDefault();
		fileCombo.setItems(psfFilenameStore.getHistory());
		fileCombo.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				file = fileCombo.getText();
				updateFile();
			}
		});
		fileCombo.setText(file);

		browseButton = new Button(inner, SWT.PUSH);
		browseButton.setText("B&rowse...");
		GridData comboData1 = new GridData(GridData.FILL_HORIZONTAL);
		comboData1.verticalAlignment = GridData.CENTER;
		comboData1.grabExcessVerticalSpace = false;
		comboData1.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint,
				browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		browseButton.setLayoutData(data);
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog d = new FileDialog(getShell());
				d.setFilterExtensions(new String[] { "*.psf", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
				d.setFilterNames(new String[] {
						"Team Project Set Files (*.psf)", "All Files (*.*)" }); //
				String fileName = getFileName();
				if (fileName != null && fileName.length() > 0) {
					int separator = fileName.lastIndexOf(System.getProperty(
							"file.separator").charAt(0)); //$NON-NLS-1$
					if (separator != -1) {
						fileName = fileName.substring(0, separator);
					}
				} else {
					fileName = ResourcesPlugin.getWorkspace().getRoot()
							.getLocation().toString();
				}
				d.setFilterPath(fileName);
				String f = d.open();
				if (f != null) {
					fileCombo.setText(f);
					file = f;
				}
			}
		});

		setControl(composite);
		Dialog.applyDialogFont(parent);
	}
	
	private void updateFile() {
		boolean complete = false;
		setMessage(null);
		setErrorMessage(null);

		if (file.length() == 0) {
			setMessage("Please specify a file to import.", messageType);
			setPageComplete(false);
			return;
		} else {
			// See if the file exists
			File f = new File(file);
			if (!f.exists()) {
				messageType = ERROR;
				setMessage("The specified file does not exist", messageType);
				setPageComplete(false);
				return;
			} else if (f.isDirectory()) {
				messageType = ERROR;
				setMessage("You have specified a folder", messageType);
				setPageComplete(false);
				return;
			} else if (!ProjectSetImporter.isValidProjectSetFile(file)) {
				messageType = ERROR;
				setMessage(
						"The specified file is not a valid Team Project Set file.",
						messageType);
				setPageComplete(false);
				return;
			}
			complete = true;
		}

		if (complete) {
			setErrorMessage(null);
			setDescription("Import the team project file.");
		}

		setPageComplete(complete);
	}

	public String getFileName() {
		return file;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fileCombo.setFocus();
		}
	}

}
