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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.SimpleWorkingSetSelectionDialog;
import org.kelvinst.psfimport.ui.IPreferenceIds;
import org.kelvinst.psfimport.ui.PsfImportPlugin;

public class ProjectSetFileImportWorkingSetsSelectionPage extends WizardPage {
	// a wizard shouldn't be in an error state until the state has been modified
	// by the user
	private int messageType = NONE;

	private static final IWorkingSet[] EMPTY_WORKING_SET_ARRAY = new IWorkingSet[0];

	private static final String WORKINGSET_SELECTION_HISTORY = "workingset_selection_history"; //$NON-NLS-1$
	private static final int MAX_HISTORY_SIZE = 5;

	private Button runInBackgroundCheckbox;
	
	private Label workingSetLabel;
	private Combo workingSetCombo;
	private Button workingSetSelectButton;
	private Button workingSetEnableButton;
	private Button workingSetAutomagicModeButton;
	
	private IWorkingSet[] selectedWorkingSets;
	private ArrayList<String> workingSetSelectionHistory;
	private String[] workingSetTypeIds;

	public ProjectSetFileImportWorkingSetsSelectionPage() {
		super("projectSetFilesPage", "Import project sets", null);
		setDescription("Configure the working sets to apply to the imported projects.");
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

		GridData comboData1 = new GridData(GridData.FILL_HORIZONTAL);
		comboData1.verticalAlignment = GridData.CENTER;
		comboData1.grabExcessVerticalSpace = false;
		comboData1.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);

		Group workingSetGroup = new Group(composite, SWT.NONE);
		workingSetGroup.setFont(composite.getFont());
		workingSetGroup.setText("Working sets");
		workingSetGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));
		workingSetGroup.setLayout(new GridLayout(1, false));

		workingSetTypeIds = new String[] {
				"org.eclipse.ui.resourceWorkingSetPage", //$NON-NLS-1$
				"org.eclipse.jdt.ui.JavaWorkingSetPage" }; //$NON-NLS-1$
		Arrays.sort(workingSetTypeIds); // we'll be performing some searches
		// with these later - presort them
		selectedWorkingSets = EMPTY_WORKING_SET_ARRAY;
		workingSetSelectionHistory = loadSelectionHistory(workingSetTypeIds);
		setSelectedWorkingSets(findApplicableWorkingSets(null));

		createContent(workingSetGroup);

		GridData gd1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd1.widthHint = 0;
		gd1.heightHint = SWT.DEFAULT;
		gd1.horizontalSpan = 3;

		runInBackgroundCheckbox = new Button(composite, SWT.CHECK);
		runInBackgroundCheckbox.setText("Run the import in the bac&kground");
		runInBackgroundCheckbox.setLayoutData(gd1);
		runInBackgroundCheckbox.setSelection(isRunInBackgroundPreferenceOn());

		setControl(composite);
		Dialog.applyDialogFont(parent);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

	/**
	 * Return the working sets selected on the page or an empty array if none
	 * were selected.
	 * 
	 * @return the selected working sets or an empty array
	 */
	public IWorkingSet[] getWorkingSets() {
		if (workingSetEnableButton.getSelection()) {
			return selectedWorkingSets;
		}
		return EMPTY_WORKING_SET_ARRAY;
	}

	private static boolean isRunInBackgroundPreferenceOn() {
		return PsfImportPlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceIds.RUN_IN_BACKGROUND);
	}

	public boolean isRunInBackgroundOn() {
		return runInBackgroundCheckbox.getSelection();
	}

	public boolean isAutomagicMode() {
		return workingSetAutomagicModeButton.getSelection();
	}
	
	/**
	 * Filters the given working sets such that the following is true: for each
	 * IWorkingSet s in result: s.getId() is element of workingSetIds
	 * 
	 * @param workingSets
	 *            the array to filter
	 * @param workingSetIds
	 *            the acceptable working set ids
	 * @return the filtered elements
	 */
	public static IWorkingSet[] filterWorkingSets(IWorkingSet[] workingSets,
			String[] workingSetIds) {

		// create a copy so we can sort the array without mucking it up for
		// clients.
		String[] workingSetIdsCopy = new String[workingSetIds.length];
		System.arraycopy(workingSetIds, 0, workingSetIdsCopy, 0,
				workingSetIds.length);
		Arrays.sort(workingSetIdsCopy);

		ArrayList result = new ArrayList();

		for (int i = 0; i < workingSets.length; i++) {
			if (Arrays.binarySearch(workingSetIdsCopy, workingSets[i].getId()) >= 0)
				result.add(workingSets[i]);
		}

		return (IWorkingSet[]) result.toArray(new IWorkingSet[result.size()]);
	}

	/**
	 * Set the current selection in the workbench.
	 * 
	 * @param selection
	 *            the selection to present in the UI or <b>null</b>
	 * @deprecated use {@link #setSelectedWorkingSets(IWorkingSet[])} and
	 *             {@link #findApplicableWorkingSets(IStructuredSelection)}
	 *             instead.
	 */
	public void setSelection(IStructuredSelection selection) {
		selectedWorkingSets = findApplicableWorkingSets(selection);

		if (workingSetCombo != null)
			updateSelectedWorkingSets();
	}

	/**
	 * Set the current selection of working sets. This array will be filtered to
	 * contain only working sets that are applicable to this instance.
	 * 
	 * @param workingSets
	 *            the working sets
	 */
	public void setSelectedWorkingSets(IWorkingSet[] workingSets) {
		selectedWorkingSets = filterWorkingSets(Arrays.asList(workingSets));
		if (workingSetCombo != null)
			updateSelectedWorkingSets();
	}

	/**
	 * Retrieves a working set from the given <code>selection</code> or an empty
	 * array if no working set could be retrieved. This selection is filtered
	 * based on the criteria used to construct this instance.
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
	 * Prune a list of working sets such that they all match the criteria set
	 * out by this block.
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
		return (IWorkingSet[]) result.toArray(new IWorkingSet[result.size()]);
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
	 * Add this block to the <code>parent</parent>
	 * 
	 * @param parent
	 *            the parent to add the block to
	 */
	public void createContent(final Composite parent) {
		int numColumn = 3;

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		composite.setLayout(new GridLayout(3, false));

		workingSetAutomagicModeButton = new Button(composite, SWT.CHECK);
		workingSetAutomagicModeButton.setText("Automagic mode");
		GridData automagicData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		automagicData.horizontalSpan = numColumn;
		workingSetAutomagicModeButton.setLayoutData(automagicData);
		
		workingSetEnableButton = new Button(composite, SWT.CHECK);
		workingSetEnableButton.setText("Add projec&t to working sets");
		GridData enableData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		enableData.horizontalSpan = numColumn;
		workingSetEnableButton.setLayoutData(enableData);
		workingSetEnableButton.setSelection(selectedWorkingSets.length > 0);

		workingSetLabel = new Label(composite, SWT.NONE);
		workingSetLabel.setText("W&orking sets:");

		workingSetCombo = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
		GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textData.horizontalSpan = numColumn - 2;
		textData.horizontalIndent = 0;
		workingSetCombo.setLayoutData(textData);

		workingSetSelectButton = new Button(composite, SWT.PUSH);
		workingSetSelectButton.setText("S&elect...");
		workingSetSelectButton.setFont(JFaceResources.getDialogFont());

		GC gc = new GC(workingSetSelectButton);
		gc.setFont(workingSetSelectButton.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();

		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
				IDialogConstants.BUTTON_WIDTH);
		Point minSize = workingSetSelectButton.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		workingSetSelectButton.setLayoutData(data);

		workingSetSelectButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				SimpleWorkingSetSelectionDialog dialog = new SimpleWorkingSetSelectionDialog(
						parent.getShell(), workingSetTypeIds,
						selectedWorkingSets, false);
				dialog.setMessage("The new project will be added to the selected working sets:");

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

		workingSetEnableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnableState(workingSetEnableButton.getSelection());
			}
		});
		updateEnableState(workingSetEnableButton.getSelection());

		workingSetCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSelectedWorkingSets();
			}
		});

		workingSetCombo.setItems(getHistoryEntries());
		if (selectedWorkingSets.length == 0
				&& workingSetSelectionHistory.size() > 0) {
			workingSetCombo.select(historyIndex(workingSetSelectionHistory
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
		workingSetSelectButton.setEnabled(enabled);
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
			workingSetEnableButton.setSelection(false);
			updateEnableState(false);
		}
	}

	private String[] getHistoryEntries() {
		String[] history = workingSetSelectionHistory
				.toArray(new String[workingSetSelectionHistory.size()]);
		Arrays.sort(history, new Comparator() {
			public int compare(Object o1, Object o2) {
				return Collator.getInstance().compare(o1, o2);
			}
		});
		return history;
	}

	private void historyInsert(String entry) {
		workingSetSelectionHistory.remove(entry);
		workingSetSelectionHistory.add(0, entry);
		storeSelectionHistory();
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

	private void storeSelectionHistory() {
		String[] history;
		if (workingSetSelectionHistory.size() > MAX_HISTORY_SIZE) {
			List subList = workingSetSelectionHistory.subList(0,
					MAX_HISTORY_SIZE);
			history = (String[]) subList.toArray(new String[subList.size()]);
		} else {
			history = (String[]) workingSetSelectionHistory
					.toArray(new String[workingSetSelectionHistory.size()]);
		}
		PsfImportPlugin.getDefault().getDialogSettings()
				.put(WORKINGSET_SELECTION_HISTORY, history);
	}

	private ArrayList<String> loadSelectionHistory(String[] workingSetIds) {
		String[] strings = PsfImportPlugin.getDefault().getDialogSettings()
				.getArray(WORKINGSET_SELECTION_HISTORY);
		if (strings == null || strings.length == 0)
			return new ArrayList<String>();

		ArrayList<String> result = new ArrayList<String>();

		HashSet<String> workingSetIdsSet = new HashSet<String>(
				Arrays.asList(workingSetIds));

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

}
