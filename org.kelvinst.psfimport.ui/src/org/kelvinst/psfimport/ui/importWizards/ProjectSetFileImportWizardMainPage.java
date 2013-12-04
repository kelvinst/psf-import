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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.ProjectSetImporter;
import org.eclipse.team.internal.ui.SWTUtils;
import org.eclipse.team.internal.ui.wizards.PsfFilenameStore;
import org.eclipse.team.internal.ui.wizards.PsfUrlStore;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.SimpleWorkingSetSelectionDialog;
import org.kelvinst.psfimport.ui.PsfImportPlugin;

public class ProjectSetFileImportWizardMainPage extends WizardPage {
	Combo fileCombo;
	String file = ""; //$NON-NLS-1$
	Button browseButton;

	String urlString = ""; //$NON-NLS-1$
	Combo urlCombo;

	// input type radios
	private Button fileInputButton;
	private Button urlInputButton;

	// input type
	public static final int InputType_file = 0;
	public static final int InputType_URL = 1;
	private int inputType = InputType_file;

	private boolean runInBackground = isRunInBackgroundPreferenceOn();
	// a wizard shouldn't be in an error state until the state has been modified
	// by the user
	private int messageType = NONE;
	private WorkingSetConfigurationBlock workingSetBlock;

	private PsfFilenameStore psfFilenameStore = PsfFilenameStore.getInstance();
	private PsfUrlStore psfUrlStore = PsfUrlStore.getInstance();

	public ProjectSetFileImportWizardMainPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription("Import the team project file.");
	}

	private void setInputType(int inputTypeSelected) {
		this.inputType = inputTypeSelected;
		// reset the message type and give the user fresh chance to input
		// correct data
		messageType = NONE;
		// update controls
		fileInputButton.setSelection(inputType == InputType_file);
		fileCombo.setEnabled(inputType == InputType_file);
		browseButton.setEnabled(inputType == InputType_file);
		urlInputButton.setSelection(inputType == InputType_URL);
		urlCombo.setEnabled(inputType == InputType_URL);
		// validate field
		if (inputType == InputType_file)
			updateFileEnablement();
		if (inputType == InputType_URL)
			updateUrlEnablement();

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

		// set F1 help
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(composite, IHelpContextIds.IMPORT_PROJECT_SET_PAGE);

		Composite inner = new Composite(composite, SWT.NULL);
		inner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);

		fileInputButton = new Button(inner, SWT.RADIO);
		fileInputButton.setText("F&ile");
		fileInputButton.setEnabled(true);
		fileInputButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setInputType(InputType_file);
			}
		});

		fileCombo = new Combo(inner, SWT.DROP_DOWN);
		GridData comboData = new GridData(GridData.FILL_HORIZONTAL);
		comboData.verticalAlignment = GridData.CENTER;
		comboData.grabExcessVerticalSpace = false;
		comboData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		fileCombo.setLayoutData(comboData);

		file = psfFilenameStore.getSuggestedDefault();
		fileCombo.setItems(psfFilenameStore.getHistory());
		fileCombo.setText(file);
		fileCombo.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				file = fileCombo.getText();
				updateFileEnablement();
			}
		});

		browseButton = new Button(inner, SWT.PUSH);
		browseButton.setText("B&rowse...");

		urlInputButton = new Button(inner, SWT.RADIO);
		urlInputButton.setText("&URL");
		urlInputButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setInputType(InputType_URL);
			}
		});

		urlCombo = new Combo(inner, SWT.DROP_DOWN);
		GridData comboData1 = new GridData(GridData.FILL_HORIZONTAL);
		comboData1.verticalAlignment = GridData.CENTER;
		comboData1.grabExcessVerticalSpace = false;
		comboData1.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		urlCombo.setLayoutData(comboData1);

		urlString = psfUrlStore.getSuggestedDefault();
		urlCombo.setItems(psfUrlStore.getHistory());
		urlCombo.setText(urlString);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		urlCombo.setLayoutData(gd);
		urlCombo.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				urlString = urlCombo.getText();
				updateUrlEnablement();
			}
		});

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

		Group workingSetGroup = new Group(composite, SWT.NONE);
		workingSetGroup.setFont(composite.getFont());
		workingSetGroup.setText("Working sets");
		workingSetGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));
		workingSetGroup.setLayout(new GridLayout(1, false));

		workingSetBlock = new WorkingSetConfigurationBlock(new String[] {
				"org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
				"org.eclipse.jdt.ui.JavaWorkingSetPage" }, //$NON-NLS-1$
				WorkbenchPlugin.getDefault().getDialogSettings());
		workingSetBlock.setWorkingSets(workingSetBlock
				.findApplicableWorkingSets(null));
		workingSetBlock.createContent(workingSetGroup);

		GridData gd1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd1.widthHint = 0;
		gd1.heightHint = SWT.DEFAULT;
		gd1.horizontalSpan = 3;

		Button runInBackgroundCheckbox = new Button(composite, SWT.CHECK);
		runInBackgroundCheckbox.setText("Run the import in the bac&kground");
		runInBackgroundCheckbox.setLayoutData(gd1);
		runInBackgroundCheckbox.setSelection(isRunInBackgroundPreferenceOn());
		runInBackgroundCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runInBackground = !runInBackground;
			}
		});

		setControl(composite);
		setDefaultInputType();
		Dialog.applyDialogFont(parent);
	}

	private void setDefaultInputType() {
		// check for clipboard contents
		Control c = getControl();
		if (c != null) {
			Clipboard clipboard = new Clipboard(c.getDisplay());
			Object o = clipboard.getContents(TextTransfer.getInstance());
			clipboard.dispose();
			if (o instanceof String) {
				try {
					URL url = new URL((String) o);
					if (url != null) {
						setInputType(InputType_URL);
						urlCombo.setText((String) o);
						return;
					}
				} catch (MalformedURLException e) {
					// ignore, it's not and URL
				}
			}
		}
		setInputType(InputType_file);
	}

	private void updateUrlEnablement() {
		boolean complete = false;
		setMessage(null);
		setErrorMessage(null);

		if (urlString.length() == 0) {
			setMessage("Please specify an URL to import.", messageType);
			complete = false;
		} else {

			try {
				new URL(urlString);
				// the URL is correct, we can clear the error message
				complete = true;
			} catch (MalformedURLException e) {
				messageType = ERROR;
				setMessage("Malformed URL", messageType);
				complete = false;
			}
		}

		if (complete) {
			setErrorMessage(null);
			setDescription("Import the team project file.");
		}

		setPageComplete(complete);
	}

	private void updateFileEnablement() {
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

	public String getUrl() {
		return urlString;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fileCombo.setFocus();
		}
	}

	/**
	 * Return the working sets selected on the page or an empty array if none
	 * were selected.
	 * 
	 * @return the selected working sets or an empty array
	 */
	public IWorkingSet[] getWorkingSets() {
		return workingSetBlock.getSelectedWorkingSets();
	}

	private static boolean isRunInBackgroundPreferenceOn() {
		return PsfImportPlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceIds.RUN_IMPORT_IN_BACKGROUND);
	}

	public boolean isRunInBackgroundOn() {
		return runInBackground;
	}

	public int getInputType() {
		return inputType;
	}

	public String getURLContents() {
		try {
			PsfUrlStore.getInstance().remember(urlString);
			String urlContent = Utilities.getURLContents(new URL(urlString),
					getContainer());
			if (ProjectSetImporter.isValidProjectSetString(urlContent)) {
				return urlContent;
			} else {
				messageType = ERROR;
				setMessage(
						"The specified file is not a valid Team Project Set file.",
						messageType);
				setPageComplete(false);
				return null;
			}
		} catch (OperationCanceledException e) { // ignore
		} catch (InterruptedException e) { // ignore
		} catch (InvocationTargetException e) {
			messageType = ERROR;
			setMessage("File from given URL cannot be loaded", messageType);
			setPageComplete(false);
		} catch (MalformedURLException e) {
			// ignore as we tested it with modify listener on combo
		}
		return null;
	}

	/**
	 * Instances of this class provide a reusable composite with controls that
	 * allow the selection of working sets. This class is most useful in
	 * {@link IWizardPage} instances that wish to create resources and
	 * pre-install them into particular working sets.
	 * 
	 * @since 3.4
	 * 
	 */
	public static class WorkingSetConfigurationBlock {

		/**
		 * Filters the given working sets such that the following is true: for
		 * each IWorkingSet s in result: s.getId() is element of workingSetIds
		 * 
		 * @param workingSets
		 *            the array to filter
		 * @param workingSetIds
		 *            the acceptable working set ids
		 * @return the filtered elements
		 */
		public static IWorkingSet[] filter(IWorkingSet[] workingSets,
				String[] workingSetIds) {

			// create a copy so we can sort the array without mucking it up for
			// clients.
			String[] workingSetIdsCopy = new String[workingSetIds.length];
			System.arraycopy(workingSetIds, 0, workingSetIdsCopy, 0,
					workingSetIds.length);
			Arrays.sort(workingSetIdsCopy);

			ArrayList result = new ArrayList();

			for (int i = 0; i < workingSets.length; i++) {
				if (Arrays.binarySearch(workingSetIdsCopy,
						workingSets[i].getId()) >= 0)
					result.add(workingSets[i]);
			}

			return (IWorkingSet[]) result
					.toArray(new IWorkingSet[result.size()]);
		}

		/**
		 * Empty working set array constant.
		 */
		private static final IWorkingSet[] EMPTY_WORKING_SET_ARRAY = new IWorkingSet[0];

		private static final String WORKINGSET_SELECTION_HISTORY = "workingset_selection_history"; //$NON-NLS-1$
		private static final int MAX_HISTORY_SIZE = 5;

		private Label workingSetLabel;
		private Combo workingSetCombo;
		private Button selectButton;
		private Button enableButton;

		private IWorkingSet[] selectedWorkingSets;
		private ArrayList selectionHistory;
		private final IDialogSettings dialogSettings;
		private final String[] workingSetTypeIds;

		private final String selectLabel;

		private final String comboLabel;

		private final String addButtonLabel;

		/**
		 * Create a new instance of this working set block using default labels.
		 * 
		 * @param workingSetIds
		 *            working set ids from which the user can choose
		 * @param settings
		 *            to store/load the selection history
		 */
		public WorkingSetConfigurationBlock(String[] workingSetIds,
				IDialogSettings settings) {
			this(workingSetIds, settings, null, null, null);
		}

		/**
		 * Create a new instance of this working set block using custom labels.
		 * 
		 * @param workingSetIds
		 *            working set ids from which the user can choose
		 * @param settings
		 *            to store/load the selection history
		 * @param addButtonLabel
		 *            the label to use for the checkable enablement button. May
		 *            be <code>null</code> to use the default value.
		 * @param comboLabel
		 *            the label to use for the recent working set combo. May be
		 *            <code>null</code> to use the default value.
		 * @param selectLabel
		 *            the label to use for the select button. May be
		 *            <code>null</code> to use the default value.
		 */
		public WorkingSetConfigurationBlock(String[] workingSetIds,
				IDialogSettings settings, String addButtonLabel,
				String comboLabel, String selectLabel) {
			Assert.isNotNull(workingSetIds);
			Assert.isNotNull(settings);

			workingSetTypeIds = workingSetIds;
			Arrays.sort(workingSetIds); // we'll be performing some searches
										// with these later - presort them
			selectedWorkingSets = EMPTY_WORKING_SET_ARRAY;
			dialogSettings = settings;
			selectionHistory = loadSelectionHistory(settings, workingSetIds);

			this.addButtonLabel = addButtonLabel == null ? WorkbenchMessages.WorkingSetGroup_EnableWorkingSet_button
					: addButtonLabel;
			this.comboLabel = comboLabel == null ? WorkbenchMessages.WorkingSetConfigurationBlock_WorkingSetText_name
					: comboLabel;
			this.selectLabel = selectLabel == null ? WorkbenchMessages.WorkingSetConfigurationBlock_SelectWorkingSet_button
					: selectLabel;
		}

		/**
		 * Set the current selection in the workbench.
		 * 
		 * @param selection
		 *            the selection to present in the UI or <b>null</b>
		 * @deprecated use {@link #setWorkingSets(IWorkingSet[])} and
		 *             {@link #findApplicableWorkingSets(IStructuredSelection)}
		 *             instead.
		 */
		public void setSelection(IStructuredSelection selection) {
			selectedWorkingSets = findApplicableWorkingSets(selection);

			if (workingSetCombo != null)
				updateSelectedWorkingSets();
		}

		/**
		 * Set the current selection of working sets. This array will be
		 * filtered to contain only working sets that are applicable to this
		 * instance.
		 * 
		 * @param workingSets
		 *            the working sets
		 */
		public void setWorkingSets(IWorkingSet[] workingSets) {
			selectedWorkingSets = filterWorkingSets(Arrays.asList(workingSets));
			if (workingSetCombo != null)
				updateSelectedWorkingSets();
		}

		/**
		 * Retrieves a working set from the given <code>selection</code> or an
		 * empty array if no working set could be retrieved. This selection is
		 * filtered based on the criteria used to construct this instance.
		 * 
		 * @param selection
		 *            the selection to retrieve the working set from
		 * @return the selected working set or an empty array
		 */
		public IWorkingSet[] findApplicableWorkingSets(
				IStructuredSelection selection) {
			if (selection == null)
				return EMPTY_WORKING_SET_ARRAY;

			return filterWorkingSets(selection.toList());
		}

		/**
		 * Prune a list of working sets such that they all match the criteria
		 * set out by this block.
		 * 
		 * @param elements
		 *            the elements to filter
		 * @return the filtered elements
		 */
		private IWorkingSet[] filterWorkingSets(Collection elements) {
			ArrayList result = new ArrayList();
			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
				Object element = iterator.next();
				if (element instanceof IWorkingSet
						&& verifyWorkingSet((IWorkingSet) element)) {
					result.add(element);
				}
			}
			return (IWorkingSet[]) result
					.toArray(new IWorkingSet[result.size()]);
		}

		/**
		 * Verifies that the given working set is suitable for selection in this
		 * block.
		 * 
		 * @param workingSetCandidate
		 *            the candidate to test
		 * @return whether it is suitable
		 */
		private boolean verifyWorkingSet(IWorkingSet workingSetCandidate) {
			return !workingSetCandidate.isAggregateWorkingSet()
					&& Arrays.binarySearch(workingSetTypeIds,
							workingSetCandidate.getId()) >= 0;
		}

		/**
		 * Return the currently selected working sets. If the controls
		 * representing this block are disabled this array will be empty
		 * regardless of the currently selected values.
		 * 
		 * @return the selected working sets
		 */
		public IWorkingSet[] getSelectedWorkingSets() {
			if (enableButton.getSelection()) {
				return selectedWorkingSets;
			}
			return EMPTY_WORKING_SET_ARRAY;
		}

		/**
		 * Add this block to the <code>parent</parent>
		 * 
		 * @param parent
		 *            the parent to add the block to
		 */
		public void createContent(final Composite parent) {
			int numColumn = 3;

			Composite composite = new Composite(parent, SWT.NONE);
			composite
					.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			composite.setLayout(new GridLayout(numColumn, false));

			enableButton = new Button(composite, SWT.CHECK);
			enableButton.setText(addButtonLabel);
			GridData enableData = new GridData(SWT.FILL, SWT.CENTER, true,
					false);
			enableData.horizontalSpan = numColumn;
			enableButton.setLayoutData(enableData);
			enableButton.setSelection(selectedWorkingSets.length > 0);

			workingSetLabel = new Label(composite, SWT.NONE);
			workingSetLabel.setText(comboLabel);

			workingSetCombo = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
			GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
			textData.horizontalSpan = numColumn - 2;
			textData.horizontalIndent = 0;
			workingSetCombo.setLayoutData(textData);

			selectButton = new Button(composite, SWT.PUSH);
			selectButton.setText(selectLabel);
			setButtonLayoutData(selectButton);
			selectButton.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					SimpleWorkingSetSelectionDialog dialog = new SimpleWorkingSetSelectionDialog(
							parent.getShell(), workingSetTypeIds,
							selectedWorkingSets, false);
					dialog.setMessage(WorkbenchMessages.WorkingSetGroup_WorkingSetSelection_message);

					if (dialog.open() == Window.OK) {
						IWorkingSet[] result = dialog.getSelection();
						if (result != null && result.length > 0) {
							selectedWorkingSets = result;
							PlatformUI.getWorkbench().getWorkingSetManager()
									.addRecentWorkingSet(result[0]);
						} else {
							selectedWorkingSets = EMPTY_WORKING_SET_ARRAY;
						}
						updateWorkingSetSelection();
					}
				}
			});

			enableButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateEnableState(enableButton.getSelection());
				}
			});
			updateEnableState(enableButton.getSelection());

			workingSetCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					updateSelectedWorkingSets();
				}
			});

			workingSetCombo.setItems(getHistoryEntries());
			if (selectedWorkingSets.length == 0 && selectionHistory.size() > 0) {
				workingSetCombo.select(historyIndex((String) selectionHistory
						.get(0)));
				updateSelectedWorkingSets();
			} else {
				updateWorkingSetSelection();
			}
		}

		private void updateEnableState(boolean enabled) {
			workingSetLabel.setEnabled(enabled);
			workingSetCombo
					.setEnabled(enabled
							&& (selectedWorkingSets.length > 0 || getHistoryEntries().length > 0));
			selectButton.setEnabled(enabled);
		}

		private void updateWorkingSetSelection() {
			if (selectedWorkingSets.length > 0) {
				workingSetCombo.setEnabled(true);
				StringBuffer buf = new StringBuffer();

				buf.append(selectedWorkingSets[0].getLabel());
				for (int i = 1; i < selectedWorkingSets.length; i++) {
					IWorkingSet ws = selectedWorkingSets[i];
					buf.append(',').append(' ');
					buf.append(ws.getLabel());
				}

				String currentSelection = buf.toString();
				int index = historyIndex(currentSelection);
				historyInsert(currentSelection);
				if (index >= 0) {
					workingSetCombo.select(index);
				} else {
					workingSetCombo.setItems(getHistoryEntries());
					workingSetCombo.select(historyIndex(currentSelection));
				}
			} else {
				enableButton.setSelection(false);
				updateEnableState(false);
			}
		}

		private String[] getHistoryEntries() {
			String[] history = (String[]) selectionHistory
					.toArray(new String[selectionHistory.size()]);
			Arrays.sort(history, new Comparator() {
				public int compare(Object o1, Object o2) {
					return Collator.getInstance().compare(o1, o2);
				}
			});
			return history;
		}

		private void historyInsert(String entry) {
			selectionHistory.remove(entry);
			selectionHistory.add(0, entry);
			storeSelectionHistory(dialogSettings);
		}

		private int historyIndex(String entry) {
			for (int i = 0; i < workingSetCombo.getItemCount(); i++) {
				if (workingSetCombo.getItem(i).equals(entry))
					return i;
			}

			return -1;
		}

		// copied from org.eclipse.jdt.internal.ui.text.JavaCommentScanner
		private String[] split(String value, String delimiters) {
			StringTokenizer tokenizer = new StringTokenizer(value, delimiters);
			int size = tokenizer.countTokens();
			String[] tokens = new String[size];
			int i = 0;
			while (i < size)
				tokens[i++] = tokenizer.nextToken();
			return tokens;
		}

		private void updateSelectedWorkingSets() {
			String item = workingSetCombo.getItem(workingSetCombo
					.getSelectionIndex());
			String[] workingSetNames = split(item, ", "); //$NON-NLS-1$

			IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
					.getWorkingSetManager();
			selectedWorkingSets = new IWorkingSet[workingSetNames.length];
			for (int i = 0; i < workingSetNames.length; i++) {
				IWorkingSet set = workingSetManager
						.getWorkingSet(workingSetNames[i]);
				Assert.isNotNull(set);
				selectedWorkingSets[i] = set;
			}
		}

		private void storeSelectionHistory(IDialogSettings settings) {
			String[] history;
			if (selectionHistory.size() > MAX_HISTORY_SIZE) {
				List subList = selectionHistory.subList(0, MAX_HISTORY_SIZE);
				history = (String[]) subList
						.toArray(new String[subList.size()]);
			} else {
				history = (String[]) selectionHistory
						.toArray(new String[selectionHistory.size()]);
			}
			settings.put(WORKINGSET_SELECTION_HISTORY, history);
		}

		private ArrayList loadSelectionHistory(IDialogSettings settings,
				String[] workingSetIds) {
			String[] strings = settings.getArray(WORKINGSET_SELECTION_HISTORY);
			if (strings == null || strings.length == 0)
				return new ArrayList();

			ArrayList result = new ArrayList();

			HashSet workingSetIdsSet = new HashSet(Arrays.asList(workingSetIds));

			IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
					.getWorkingSetManager();
			for (int i = 0; i < strings.length; i++) {
				String[] workingSetNames = split(strings[i], ", "); //$NON-NLS-1$
				boolean valid = true;
				for (int j = 0; j < workingSetNames.length && valid; j++) {
					IWorkingSet workingSet = workingSetManager
							.getWorkingSet(workingSetNames[j]);
					if (workingSet == null) {
						valid = false;
					} else {
						if (!workingSetIdsSet.contains(workingSet.getId()))
							valid = false;
					}
				}
				if (valid) {
					result.add(strings[i]);
				}
			}

			return result;
		}

		/*
		 * Copy from DialogPage with changes to accomodate the lack of a Dialog
		 * context.
		 */
		private GridData setButtonLayoutData(Button button) {
			button.setFont(JFaceResources.getDialogFont());

			GC gc = new GC(button);
			gc.setFont(button.getFont());
			FontMetrics fontMetrics = gc.getFontMetrics();
			gc.dispose();

			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
					IDialogConstants.BUTTON_WIDTH);
			Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			data.widthHint = Math.max(widthHint, minSize.x);
			button.setLayoutData(data);
			return data;
		}
	}
}
