/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.dialogs.IElementFilter;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.kelvinst.psfimport.ui.FileElement;
import org.kelvinst.psfimport.ui.FileStructureProvider;

/**
 * Page 1 of the base resource import-from-file-system Wizard
 */
public class ProjectSetFileImportFilesSelectionPage extends WizardPage implements Listener {
	// widgets
	protected Combo sourceNameField;

	protected Button sourceBrowseButton;

	protected Button overwriteExistingResourcesCheckbox;

	// A boolean to indicate if the user has typed anything
	private boolean entryChanged = false;

	private FileStructureProvider fileStructureProvider = new FileStructureProvider();

	// dialog store id constants
	private final static String STORE_SOURCE_NAMES_ID = "WizardFileSystemResourceImportPage1.STORE_SOURCE_NAMES_ID";//$NON-NLS-1$

	private final static String STORE_OVERWRITE_EXISTING_RESOURCES_ID = "WizardFileSystemResourceImportPage1.STORE_OVERWRITE_EXISTING_RESOURCES_ID";//$NON-NLS-1$

	private static final String SELECT_SOURCE_TITLE = "Import from directory";

	private static final String SELECT_SOURCE_MESSAGE = "Select a directory to import from.";

	protected static final String SOURCE_EMPTY_MESSAGE = "Source must not be empty.";

	protected static final int SIZING_TEXT_FIELD_WIDTH = 250;

	protected static final int COMBO_HISTORY_LENGTH = 5;

	private IResource currentResourceSelection;

	/**
	 * The <code>selectionGroup</code> field should have been created with a
	 * private modifier. Subclasses should not access this field directly.
	 */
	private FileElement root;

	private Object currentTreeSelection;

	private Collection expandedTreeNodes = new HashSet();

	private Map checkedStateStore = new HashMap(9);

	private HashSet whiteCheckedTreeItems = new HashSet();

	private ITreeContentProvider treeContentProvider;

	private IStructuredContentProvider listContentProvider;

	private ILabelProvider treeLabelProvider;

	private ILabelProvider listLabelProvider;

	// widgets
	private CheckboxTreeViewer treeViewer;

	private CheckboxTableViewer listViewer;

	// height hint for viewers
	private static int PREFERRED_HEIGHT = 150;

	/**
	 * An empty array that can be returned from a call to
	 * {@link #getListeners()} when {@link #listenerList} is <code>null</code>.
	 */
	private static final Object[] EMPTY_ARRAY = new Object[0];

	/**
	 * A collection of objects listening to changes to this manager. This
	 * collection is <code>null</code> if there are no listeners.
	 */
	private transient ListenerList listenerList = null;

	/**
	 * Adds a listener to this manager that will be notified when this manager's
	 * state changes.
	 * 
	 * @param listener
	 *            The listener to be added; must not be <code>null</code>.
	 */
	protected synchronized final void addListenerObject(final Object listener) {
		if (listenerList == null) {
			listenerList = new ListenerList(ListenerList.IDENTITY);
		}

		listenerList.add(listener);
	}

	/**
	 * Clears all of the listeners from the listener list.
	 */
	protected synchronized final void clearListeners() {
		if (listenerList != null) {
			listenerList.clear();
		}
	}

	/**
	 * Returns the listeners attached to this event manager.
	 * 
	 * @return The listeners currently attached; may be empty, but never
	 *         <code>null</code>
	 */
	protected final Object[] getListeners() {
		final ListenerList list = listenerList;
		if (list == null) {
			return EMPTY_ARRAY;
		}

		return list.getListeners();
	}

	/**
	 * Whether one or more listeners are attached to the manager.
	 * 
	 * @return <code>true</code> if listeners are attached to the manager;
	 *         <code>false</code> otherwise.
	 */
	protected final boolean isListenerAttached() {
		return listenerList != null;
	}

	/**
	 * Removes a listener from this manager.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	protected synchronized final void removeListenerObject(final Object listener) {
		if (listenerList != null) {
			listenerList.remove(listener);

			if (listenerList.isEmpty()) {
				listenerList = null;
			}
		}
	}

	/**
	 * This method must be called just before this window becomes visible.
	 */
	public void aboutToOpen() {
		determineWhiteCheckedDescendents(root);
		checkNewTreeElements(treeContentProvider.getElements(root));
		currentTreeSelection = null;

		// select the first element in the list
		Object[] elements = treeContentProvider.getElements(root);
		Object primary = elements.length > 0 ? elements[0] : null;
		if (primary != null) {
			treeViewer.setSelection(new StructuredSelection(primary));
		}
		treeViewer.getControl().setFocus();
	}

	/**
	 * Add the passed listener to self's collection of clients that listen for
	 * changes to element checked states
	 * 
	 * @param listener
	 *            ICheckStateListener
	 */
	public void addCheckStateListener(ICheckStateListener listener) {
		addListenerObject(listener);
	}

	/**
	 * Return a boolean indicating whether all children of the passed tree
	 * element are currently white-checked
	 * 
	 * @return boolean
	 * @param treeElement
	 *            java.lang.Object
	 */
	protected boolean areAllChildrenWhiteChecked(Object treeElement) {
		Object[] children = treeContentProvider.getChildren(treeElement);
		for (int i = 0; i < children.length; ++i) {
			if (!whiteCheckedTreeItems.contains(children[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Return a boolean indicating whether all list elements associated with the
	 * passed tree element are currently checked
	 * 
	 * @return boolean
	 * @param treeElement
	 *            java.lang.Object
	 */
	protected boolean areAllElementsChecked(Object treeElement) {
		List checkedElements = (List) checkedStateStore.get(treeElement);
		if (checkedElements == null) {
			return false;
		}

		return getListItemsSize(treeElement) == checkedElements.size();
	}

	/**
	 * Iterate through the passed elements which are being realized for the
	 * first time and check each one in the tree viewer as appropriate
	 */
	protected void checkNewTreeElements(Object[] elements) {
		for (int i = 0; i < elements.length; ++i) {
			Object currentElement = elements[i];
			boolean checked = checkedStateStore.containsKey(currentElement);
			treeViewer.setChecked(currentElement, checked);
			treeViewer.setGrayed(currentElement, checked && !whiteCheckedTreeItems.contains(currentElement));
		}
	}

	/**
	 * An item was checked in one of self's two views. Determine which view this
	 * occurred in and delegate appropriately
	 * 
	 * @param event
	 *            CheckStateChangedEvent
	 */
	public void checkListStateChanged(final CheckStateChangedEvent event) {

		// Potentially long operation - show a busy cursor
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				if (event.getCheckable().equals(treeViewer)) {
					treeItemChecked(event.getElement(), event.getChecked());
				} else {
					listItemChecked(event.getElement(), event.getChecked(), true);
				}

				notifyCheckStateChangeListeners(event);
			}
		});
	}

	/**
	 * Lay out and initialize self's visual components.
	 * 
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 * @param style
	 *            the style flags for the new Composite
	 * @param useHeightHint
	 *            If true yse the preferredHeight.
	 */
	protected void createContents(Composite parent, int style, boolean useHeightHint) {
		// group pane
		Composite composite = new Composite(parent, style);
		composite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createTreeViewer(composite, useHeightHint);
		createListViewer(composite, useHeightHint);

		initialize();
	}

	/**
	 * Create this group's list viewer.
	 */
	protected void createListViewer(Composite parent, boolean useHeightHint) {
		listViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		if (useHeightHint) {
			data.heightHint = PREFERRED_HEIGHT;
		}
		listViewer.getTable().setLayoutData(data);
		listViewer.getTable().setFont(parent.getFont());
		listViewer.setContentProvider(listContentProvider);
		listViewer.setLabelProvider(listLabelProvider);
		listViewer.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkListStateChanged(event);
			}
		});
	}

	/**
	 * Create this group's tree viewer.
	 */
	protected void createTreeViewer(Composite parent, boolean useHeightHint) {
		Tree tree = new Tree(parent, SWT.CHECK | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		if (useHeightHint) {
			data.heightHint = PREFERRED_HEIGHT;
		}
		tree.setLayoutData(data);
		tree.setFont(parent.getFont());

		treeViewer = new CheckboxTreeViewer(tree);
		treeViewer.setContentProvider(treeContentProvider);
		treeViewer.setLabelProvider(treeLabelProvider);
		treeViewer.addTreeListener(new ITreeViewerListener() {

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				expandTreeElement(event.getElement());
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				// We don't need to do anything with this
			}
		});
		treeViewer.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkListStateChanged(event);
			}
		});
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object selectedElement = selection.getFirstElement();
				if (selectedElement == null) {
					currentTreeSelection = null;
					listViewer.setInput(currentTreeSelection);
					return;
				}

				// ie.- if not an item deselection
				if (selectedElement != currentTreeSelection) {
					populateListViewer(selectedElement);
				}

				currentTreeSelection = selectedElement;
			}
		});
	}

	/**
	 * Returns a boolean indicating whether the passed tree element should be at
	 * LEAST gray-checked. Note that this method does not consider whether it
	 * should be white-checked, so a specified tree item which should be
	 * white-checked will result in a <code>true</code> answer from this method.
	 * To determine whether a tree item should be white-checked use method
	 * #determineShouldBeWhiteChecked(Object).
	 * 
	 * @param treeElement
	 *            java.lang.Object
	 * @return boolean
	 * @see #determineShouldBeWhiteChecked(Object)
	 */
	protected boolean determineShouldBeAtLeastGrayChecked(Object treeElement) {
		// if any list items associated with treeElement are checked then it
		// retains its gray-checked status regardless of its children
		List checked = (List) checkedStateStore.get(treeElement);
		if (checked != null && (!checked.isEmpty())) {
			return true;
		}

		// if any children of treeElement are still gray-checked then
		// treeElement
		// must remain gray-checked as well. Only ask expanded nodes
		if (expandedTreeNodes.contains(treeElement)) {
			Object[] children = treeContentProvider.getChildren(treeElement);
			for (int i = 0; i < children.length; ++i) {
				if (checkedStateStore.containsKey(children[i])) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns a boolean indicating whether the passed tree item should be
	 * white-checked.
	 * 
	 * @return boolean
	 * @param treeElement
	 *            java.lang.Object
	 */
	protected boolean determineShouldBeWhiteChecked(Object treeElement) {
		return areAllChildrenWhiteChecked(treeElement) && areAllElementsChecked(treeElement);
	}

	/**
	 * Recursively add appropriate tree elements to the collection of known
	 * white-checked tree elements.
	 * 
	 * @param treeElement
	 *            java.lang.Object
	 */
	protected void determineWhiteCheckedDescendents(Object treeElement) {
		// always go through all children first since their white-checked
		// statuses will be needed to determine the white-checked status for
		// this tree element
		Object[] children = treeContentProvider.getElements(treeElement);
		for (int i = 0; i < children.length; ++i) {
			determineWhiteCheckedDescendents(children[i]);
		}

		// now determine the white-checked status for this tree element
		if (determineShouldBeWhiteChecked(treeElement)) {
			setWhiteChecked(treeElement, true);
		}
	}

	/**
	 * Cause the tree viewer to expand all its items
	 */
	public void expandAll() {
		treeViewer.expandAll();
	}

	/**
	 * Expand an element in a tree viewer
	 */
	private void expandTreeElement(final Object item) {
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
			public void run() {

				// First see if the children need to be given their checked
				// state at all. If they've
				// already been realized then this won't be necessary
				if (expandedTreeNodes.contains(item)) {
					checkNewTreeElements(treeContentProvider.getChildren(item));
				} else {

					expandedTreeNodes.add(item);
					if (whiteCheckedTreeItems.contains(item)) {
						// If this is the first expansion and this is a
						// white checked node then check the children
						Object[] children = treeContentProvider.getChildren(item);
						for (int i = 0; i < children.length; ++i) {
							if (!whiteCheckedTreeItems.contains(children[i])) {
								Object child = children[i];
								setWhiteChecked(child, true);
								treeViewer.setChecked(child, true);
								checkedStateStore.put(child, new ArrayList());
							}
						}

						// Now be sure to select the list of items too
						setListForWhiteSelection(item);
					}
				}

			}
		});
	}

	/**
	 * Add all of the selected children of nextEntry to result recursively. This
	 * does not set any values in the checked state.
	 * 
	 * @param treeElement
	 *            The tree elements being queried
	 * @param addAll
	 *            a boolean to indicate if the checked state store needs to be
	 *            queried
	 * @param filter
	 *            IElementFilter - the filter being used on the data
	 * @param monitor
	 *            IProgressMonitor or null that the cancel is polled for
	 */
	private void findAllSelectedListElements(Object treeElement, String parentLabel, boolean addAll, IElementFilter filter, IProgressMonitor monitor)
			throws InterruptedException {

		String fullLabel = null;
		if (monitor != null && monitor.isCanceled()) {
			return;
		}
		if (monitor != null) {
			fullLabel = getFullLabel(treeElement, parentLabel);
			monitor.subTask(fullLabel);
		}

		if (addAll) {
			filter.filterElements(listContentProvider.getElements(treeElement), monitor);
		} else { // Add what we have stored
			if (checkedStateStore.containsKey(treeElement)) {
				filter.filterElements((Collection) checkedStateStore.get(treeElement), monitor);
			}
		}

		Object[] treeChildren = treeContentProvider.getChildren(treeElement);
		for (int i = 0; i < treeChildren.length; i++) {
			Object child = treeChildren[i];
			if (addAll) {
				findAllSelectedListElements(child, fullLabel, true, filter, monitor);
			} else { // Only continue for those with checked state
				if (checkedStateStore.containsKey(child)) {
					findAllSelectedListElements(child, fullLabel, whiteCheckedTreeItems.contains(child), filter, monitor);
				}
			}

		}
	}

	/**
	 * Find all of the white checked children of the treeElement and add them to
	 * the collection. If the element itself is white select add it. If not then
	 * add any selected list elements and recurse down to the children.
	 * 
	 * @param treeElement
	 *            java.lang.Object
	 * @param result
	 *            java.util.Collection
	 */
	private void findAllWhiteCheckedItems(Object treeElement, Collection result) {

		if (whiteCheckedTreeItems.contains(treeElement)) {
			result.add(treeElement);
		} else {
			Collection listChildren = (Collection) checkedStateStore.get(treeElement);
			// if it is not in the store then it and it's children are not
			// interesting
			if (listChildren == null) {
				return;
			}
			result.addAll(listChildren);
			Object[] children = treeContentProvider.getChildren(treeElement);
			for (int i = 0; i < children.length; ++i) {
				findAllWhiteCheckedItems(children[i], result);
			}
		}
	}

	/**
	 * Returns whether all items in the list are checked. This method is
	 * required, because this widget will keep items grey checked even though
	 * all children are selected (see grayUpdateHierarchy()).
	 * 
	 * @return true if all items in the list are checked - false if not
	 */
	public boolean isEveryItemChecked() {
		// Iterate through the children of the root as the root is not in
		// the store
		Object[] children = treeContentProvider.getChildren(root);
		for (int i = 0; i < children.length; ++i) {
			if (!whiteCheckedTreeItems.contains(children[i])) {
				if (!treeViewer.getGrayed(children[i]))
					return false;
				if (!isEveryChildrenChecked(children[i]))
					return false;
			}
		}
		return true;
	}

	/**
	 * Verifies of all list items of the tree element are checked, and if all
	 * children are white checked. If not, verify their children so that if an
	 * element is not white checked, but all its children are while checked,
	 * then, all items are considered checked.
	 * 
	 * @param treeElement
	 *            the treeElement which status to verify
	 * @return true if all items are checked, false otherwise.
	 */
	private boolean isEveryChildrenChecked(Object treeElement) {
		List checked = (List) checkedStateStore.get(treeElement);
		if (checked != null && (!checked.isEmpty())) {
			Object[] listItems = listContentProvider.getElements(treeElement);
			if (listItems.length != checked.size())
				return false;
		}
		Object[] children = treeContentProvider.getChildren(treeElement);
		for (int i = 0; i < children.length; ++i) {
			if (!whiteCheckedTreeItems.contains(children[i])) {
				if (!treeViewer.getGrayed(children[i]))
					return false;
				if (!isEveryChildrenChecked(children[i]))
					return false;
			}
		}
		return true;
	}

	/**
	 * Returns a flat list of all of the leaf elements.
	 * 
	 * @return all of the leaf elements.
	 */
	public List getAllListItems() {

		final ArrayList returnValue = new ArrayList();

		IElementFilter passThroughFilter = new IElementFilter() {

			public void filterElements(Collection elements, IProgressMonitor monitor) {
				returnValue.addAll(elements);
			}

			public void filterElements(Object[] elements, IProgressMonitor monitor) {
				for (int i = 0; i < elements.length; i++) {
					returnValue.add(elements[i]);
				}
			}
		};

		try {
			Object[] children = treeContentProvider.getChildren(root);
			for (int i = 0; i < children.length; ++i) {
				findAllSelectedListElements(children[i], null, true, passThroughFilter, null);
			}
		} catch (InterruptedException exception) {
			return new ArrayList();
		}
		return returnValue;

	}

	/**
	 * Returns a list of all of the items that are white checked. Any folders
	 * that are white checked are added and then any files from white checked
	 * folders are added.
	 * 
	 * @return the list of all of the items that are white checked
	 */
	public List getAllWhiteCheckedItems() {

		List result = new ArrayList();

		// Iterate through the children of the root as the root is not in
		// the store
		Object[] children = treeContentProvider.getChildren(root);
		for (int i = 0; i < children.length; ++i) {
			findAllWhiteCheckedItems(children[i], result);
		}

		return result;
	}

	/**
	 * Answer the number of elements that have been checked by the user.
	 * 
	 * @return int
	 */
	public int getCheckedElementCount() {
		return checkedStateStore.size();
	}

	/**
	 * Get the full label of the treeElement (its name and its parent's name).
	 * 
	 * @param treeElement
	 *            - the element being exported
	 * @param parentLabel
	 *            - the label of the parent, can be null
	 * @return String
	 */
	protected String getFullLabel(Object treeElement, String parentLabel) {
		String label = parentLabel;
		if (parentLabel == null) {
			label = ""; //$NON-NLS-1$
		}
		IPath parentName = new Path(label);

		String elementText = treeLabelProvider.getText(treeElement);
		if (elementText == null) {
			return parentName.toString();
		}
		return parentName.append(elementText).toString();
	}

	/**
	 * Return a count of the number of list items associated with a given tree
	 * item.
	 * 
	 * @return int
	 * @param treeElement
	 *            java.lang.Object
	 */
	protected int getListItemsSize(Object treeElement) {
		Object[] elements = listContentProvider.getElements(treeElement);
		return elements.length;
	}

	/**
	 * Get the table the list viewer uses.
	 * 
	 * @return org.eclipse.swt.widgets.Table
	 */
	public Table getListTable() {
		return this.listViewer.getTable();
	}

	/**
	 * Logically gray-check all ancestors of treeItem by ensuring that they
	 * appear in the checked table
	 */
	protected void grayCheckHierarchy(Object treeElement) {

		// expand the element first to make sure we have populated for it
		expandTreeElement(treeElement);

		// if this tree element is already gray then its ancestors all are
		// as well
		if (checkedStateStore.containsKey(treeElement)) {
			return; // no need to proceed upwards from here
		}

		checkedStateStore.put(treeElement, new ArrayList());
		Object parent = treeContentProvider.getParent(treeElement);
		if (parent != null) {
			grayCheckHierarchy(parent);
		}
	}

	/**
	 * Set the checked state of self and all ancestors appropriately. Do not
	 * white check anyone - this is only done down a hierarchy.
	 */
	private void grayUpdateHierarchy(Object treeElement) {

		boolean shouldBeAtLeastGray = determineShouldBeAtLeastGrayChecked(treeElement);

		treeViewer.setGrayChecked(treeElement, shouldBeAtLeastGray);

		if (whiteCheckedTreeItems.contains(treeElement)) {
			whiteCheckedTreeItems.remove(treeElement);
		}

		// proceed up the tree element hierarchy
		Object parent = treeContentProvider.getParent(treeElement);
		if (parent != null) {
			grayUpdateHierarchy(parent);
		}
	}

	/**
	 * Set the initial checked state of the passed list element to true.
	 * 
	 * @param element
	 */
	public void initialCheckListItem(Object element) {
		Object parent = treeContentProvider.getParent(element);
		selectAndReveal(parent);
		// Check the element in the viewer as if it had been manually
		// checked
		listViewer.setChecked(element, true);
		// As this is not done from the UI then set the box for updating
		// from the selection to false
		listItemChecked(element, true, false);
		grayUpdateHierarchy(parent);
	}

	/**
	 * Set the initial checked state of the passed element to true, as well as
	 * to all of its children and associated list elements
	 * 
	 * @param element
	 */
	public void initialCheckTreeItem(Object element) {
		treeItemChecked(element, true);
		selectAndReveal(element);
	}

	private void selectAndReveal(Object treeElement) {
		treeViewer.reveal(treeElement);
		IStructuredSelection selection = new StructuredSelection(treeElement);
		treeViewer.setSelection(selection);
	}

	/**
	 * Initialize this group's viewers after they have been laid out.
	 */
	protected void initialize() {
		treeViewer.setInput(root);
		this.expandedTreeNodes = new ArrayList();
		this.expandedTreeNodes.add(root);

	}

	/**
	 * Callback that's invoked when the checked status of an item in the list is
	 * changed by the user. Do not try and update the hierarchy if we are
	 * building the initial list.
	 */
	protected void listItemChecked(Object listElement, boolean state, boolean updatingFromSelection) {
		List checkedListItems = (List) checkedStateStore.get(currentTreeSelection);
		// If it has not been expanded do so as the selection of list items
		// will affect gray state
		if (!expandedTreeNodes.contains(currentTreeSelection)) {
			expandTreeElement(currentTreeSelection);
		}

		if (state) {
			if (checkedListItems == null) {
				// since the associated tree item has gone from 0 -> 1
				// checked
				// list items, tree checking may need to be updated
				grayCheckHierarchy(currentTreeSelection);
				checkedListItems = (List) checkedStateStore.get(currentTreeSelection);
			}
			checkedListItems.add(listElement);
		} else {
			checkedListItems.remove(listElement);
			if (checkedListItems.isEmpty()) {
				// since the associated tree item has gone from 1 -> 0
				// checked
				// list items, tree checking may need to be updated
				ungrayCheckHierarchy(currentTreeSelection);
			}
		}

		// Update the list with the selections if there are any
		if (checkedListItems.size() > 0) {
			checkedStateStore.put(currentTreeSelection, checkedListItems);
		}
		if (updatingFromSelection) {
			grayUpdateHierarchy(currentTreeSelection);
		}
	}

	/**
	 * Notify all checked state listeners that the passed element has had its
	 * checked state changed to the passed state
	 */
	protected void notifyCheckStateChangeListeners(final CheckStateChangedEvent event) {
		Object[] array = getListeners();
		for (int i = 0; i < array.length; i++) {
			final ICheckStateListener l = (ICheckStateListener) array[i];
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					l.checkStateChanged(event);
				}
			});
		}
	}

	/**
	 * Set the contents of the list viewer based upon the specified selected
	 * tree element. This also includes checking the appropriate list items.
	 * 
	 * @param treeElement
	 *            java.lang.Object
	 */
	protected void populateListViewer(final Object treeElement) {
		listViewer.setInput(treeElement);

		// If the element is white checked but not expanded we have not set
		// up all of the children yet
		if (!(expandedTreeNodes.contains(treeElement)) && whiteCheckedTreeItems.contains(treeElement)) {

			// Potentially long operation - show a busy cursor
			BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
				public void run() {
					setListForWhiteSelection(treeElement);
					listViewer.setAllChecked(true);
				}
			});

		} else {
			List listItemsToCheck = (List) checkedStateStore.get(treeElement);

			if (listItemsToCheck != null) {
				Iterator listItemsEnum = listItemsToCheck.iterator();
				while (listItemsEnum.hasNext()) {
					listViewer.setChecked(listItemsEnum.next(), true);
				}
			}
		}
	}

	/**
	 * Logically gray-check all ancestors of treeItem by ensuring that they
	 * appear in the checked table. Add any elements to the selectedNodes so we
	 * can track that has been done.
	 */
	private void primeHierarchyForSelection(Object item, Set selectedNodes) {

		// Only prime it if we haven't visited yet
		if (selectedNodes.contains(item)) {
			return;
		}

		checkedStateStore.put(item, new ArrayList());

		// mark as expanded as we are going to populate it after this
		expandedTreeNodes.add(item);
		selectedNodes.add(item);

		Object parent = treeContentProvider.getParent(item);
		if (parent != null) {
			primeHierarchyForSelection(parent, selectedNodes);
		}
	}

	/**
	 * Remove the passed listener from self's collection of clients that listen
	 * for changes to element checked states
	 * 
	 * @param listener
	 *            ICheckStateListener
	 */
	public void removeCheckStateListener(ICheckStateListener listener) {
		removeListenerObject(listener);
	}

	/**
	 * Select or deselect all of the elements in the tree depending on the value
	 * of the selection boolean. Be sure to update the displayed files as well.
	 * 
	 * @param selection
	 */
	public void setAllSelections(final boolean selection) {

		// If there is no root there is nothing to select
		if (root == null) {
			return;
		}

		// Potentially long operation - show a busy cursor
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				setTreeChecked(root, selection);
				listViewer.setAllChecked(selection);
			}
		});
	}

	/**
	 * The treeElement has been white selected. Get the list for the element and
	 * set it in the checked state store.
	 * 
	 * @param treeElement
	 *            the element being updated
	 */
	private void setListForWhiteSelection(Object treeElement) {

		Object[] listItems = listContentProvider.getElements(treeElement);
		List listItemsChecked = new ArrayList();
		for (int i = 0; i < listItems.length; ++i) {
			listItemsChecked.add(listItems[i]);
		}

		checkedStateStore.put(treeElement, listItemsChecked);
	}

	/**
	 * Set the list viewer's providers to those passed
	 * 
	 * @param contentProvider
	 *            ITreeContentProvider
	 * @param labelProvider
	 *            ILabelProvider
	 */
	public void setListProviders(IStructuredContentProvider contentProvider, ILabelProvider labelProvider) {
		listViewer.setContentProvider(contentProvider);
		listViewer.setLabelProvider(labelProvider);
	}

	/**
	 * Set the comparator that is to be applied to self's list viewer
	 * 
	 * @param comparator
	 *            the sorter for the list
	 */
	public void setListComparator(ViewerComparator comparator) {
		listViewer.setComparator(comparator);
	}

	/**
	 * Set the root of the widget to be new Root. Regenerate all of the tables
	 * and lists from this value.
	 * 
	 * @param newRoot
	 */
	public void setRoot(FileElement newRoot) {
		this.root = newRoot;
		initialize();
	}

	/**
	 * Set the checked state of the passed tree element appropriately, and do so
	 * recursively to all of its child tree elements as well
	 */
	protected void setTreeChecked(Object treeElement, boolean state) {

		if (treeElement.equals(currentTreeSelection)) {
			listViewer.setAllChecked(state);
		}

		if (state) {
			setListForWhiteSelection(treeElement);
		} else {
			checkedStateStore.remove(treeElement);
		}

		setWhiteChecked(treeElement, state);
		treeViewer.setChecked(treeElement, state);
		treeViewer.setGrayed(treeElement, false);

		// now logically check/uncheck all children as well if it has been
		// expanded
		if (expandedTreeNodes.contains(treeElement)) {
			Object[] children = treeContentProvider.getChildren(treeElement);
			for (int i = 0; i < children.length; ++i) {
				setTreeChecked(children[i], state);
			}
		}
	}

	/**
	 * Set the tree viewer's providers to those passed
	 * 
	 * @param contentProvider
	 *            ITreeContentProvider
	 * @param labelProvider
	 *            ILabelProvider
	 */
	public void setTreeProviders(ITreeContentProvider contentProvider, ILabelProvider labelProvider) {
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);
	}

	/**
	 * Set the comparator that is to be applied to self's tree viewer
	 * 
	 * @param comparator
	 *            the comparator for the tree
	 */
	public void setTreeComparator(ViewerComparator comparator) {
		treeViewer.setComparator(comparator);
	}

	/**
	 * Adjust the collection of references to white-checked tree elements
	 * appropriately.
	 * 
	 * @param treeElement
	 *            java.lang.Object
	 * @param isWhiteChecked
	 *            boolean
	 */
	protected void setWhiteChecked(Object treeElement, boolean isWhiteChecked) {
		if (isWhiteChecked) {
			if (!whiteCheckedTreeItems.contains(treeElement)) {
				whiteCheckedTreeItems.add(treeElement);
			}
		} else {
			whiteCheckedTreeItems.remove(treeElement);
		}
	}

	/**
	 * Callback that's invoked when the checked status of an item in the tree is
	 * changed by the user.
	 */
	protected void treeItemChecked(Object treeElement, boolean state) {

		// recursively adjust all child tree elements appropriately
		setTreeChecked(treeElement, state);

		Object parent = treeContentProvider.getParent(treeElement);

		// workspace root is not shown in the tree, so ignore it
		if (parent == null || parent instanceof IWorkspaceRoot) {
			return;
		}

		// now update upwards in the tree hierarchy
		if (state) {
			grayCheckHierarchy(parent);
		} else {
			ungrayCheckHierarchy(parent);
		}

		// Update the hierarchy but do not white select the parent
		grayUpdateHierarchy(parent);
	}

	/**
	 * Logically un-gray-check all ancestors of treeItem iff appropriate.
	 */
	protected void ungrayCheckHierarchy(Object treeElement) {
		if (!determineShouldBeAtLeastGrayChecked(treeElement)) {
			checkedStateStore.remove(treeElement);
		}

		Object parent = treeContentProvider.getParent(treeElement);
		if (parent != null) {
			ungrayCheckHierarchy(parent);
		}
	}

	/**
	 * Set the checked state of self and all ancestors appropriately
	 */
	protected void updateHierarchy(Object treeElement) {

		boolean whiteChecked = determineShouldBeWhiteChecked(treeElement);
		boolean shouldBeAtLeastGray = determineShouldBeAtLeastGrayChecked(treeElement);

		treeViewer.setChecked(treeElement, shouldBeAtLeastGray);
		setWhiteChecked(treeElement, whiteChecked);
		if (whiteChecked) {
			treeViewer.setGrayed(treeElement, false);
		} else {
			treeViewer.setGrayed(treeElement, shouldBeAtLeastGray);
		}

		// proceed up the tree element hierarchy but gray select all of them
		Object parent = treeContentProvider.getParent(treeElement);
		if (parent != null) {
			grayUpdateHierarchy(parent);
		}
	}

	/**
	 * Set the focus on to the list widget.
	 */
	public void setTreeviewFocus() {

		treeViewer.getTree().setFocus();
		if (treeViewer.getSelection().isEmpty()) {
			Object[] elements = treeContentProvider.getElements(root);
			if (elements.length > 0) {
				StructuredSelection selection = new StructuredSelection(elements[0]);
				treeViewer.setSelection(selection);
			}
		}

	}

	/**
	 * Creates an instance of this class
	 * 
	 * @param selection
	 *            IStructuredSelection
	 */
	public ProjectSetFileImportFilesSelectionPage(IStructuredSelection selection) {
		super("fileSystemImportPage1");

		// Initialize to null
		currentResourceSelection = null;
		if ((selection != null) && (selection.size() == 1)) {
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IAdaptable) {
				Object resource = ((IAdaptable) firstElement).getAdapter(IResource.class);
				if (resource != null) {
					currentResourceSelection = (IResource) resource;
				}
			}
		}

		if (currentResourceSelection != null) {
			if (currentResourceSelection.getType() == IResource.FILE) {
				currentResourceSelection = currentResourceSelection.getParent();
			}

			if (!currentResourceSelection.isAccessible()) {
				currentResourceSelection = null;
			}
		}

		setTitle("Import project sets");
		setDescription("Select the files to import.");
	}

	/**
	 * Adds an entry to a history, while taking care of duplicate history items
	 * and excessively long histories. The assumption is made that all histories
	 * should be of length
	 * <code>WizardDataTransferPage.COMBO_HISTORY_LENGTH</code>.
	 * 
	 * @param history
	 *            the current history
	 * @param newEntry
	 *            the entry to add to the history
	 */
	protected String[] addToHistory(String[] history, String newEntry) {
		java.util.ArrayList l = new java.util.ArrayList(Arrays.asList(history));
		addToHistory(l, newEntry);
		String[] r = new String[l.size()];
		l.toArray(r);
		return r;
	}

	/**
	 * Adds an entry to a history, while taking care of duplicate history items
	 * and excessively long histories. The assumption is made that all histories
	 * should be of length
	 * <code>WizardDataTransferPage.COMBO_HISTORY_LENGTH</code>.
	 * 
	 * @param history
	 *            the current history
	 * @param newEntry
	 *            the entry to add to the history
	 */
	protected void addToHistory(List history, String newEntry) {
		history.remove(newEntry);
		history.add(0, newEntry);

		// since only one new item was added, we can be over the limit
		// by at most one item
		if (history.size() > COMBO_HISTORY_LENGTH) {
			history.remove(COMBO_HISTORY_LENGTH);
		}
	}

	/**
	 * Creates a new label with a bold font.
	 * 
	 * @param parent
	 *            the parent control
	 * @param text
	 *            the label text
	 * @return the new label control
	 */
	protected Label createBoldLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(text);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Creates a new label with a bold font.
	 * 
	 * @param parent
	 *            the parent control
	 * @param text
	 *            the label text
	 * @return the new label control
	 */
	protected Label createPlainLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		label.setFont(parent.getFont());
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Creates a horizontal spacer line that fills the width of its container.
	 * 
	 * @param parent
	 *            the parent control
	 */
	protected void createSpacer(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		spacer.setLayoutData(data);
	}

	/**
	 * Get a path from the supplied text widget.
	 * 
	 * @return org.eclipse.core.runtime.IPath
	 */
	protected IPath getPathFromText(Text textField) {
		String text = textField.getText();
		// Do not make an empty path absolute so as not to confuse with the root
		if (text.length() == 0) {
			return new Path(text);
		}

		return (new Path(text)).makeAbsolute();
	}

	/**
	 * Queries the user to supply a container resource.
	 * 
	 * @return the path to an existing or new container, or <code>null</code> if
	 *         the user cancelled the dialog
	 */
	protected IPath queryForContainer(IContainer initialSelection, String msg) {
		return queryForContainer(initialSelection, msg, null);
	}

	/**
	 * Queries the user to supply a container resource.
	 * 
	 * @return the path to an existing or new container, or <code>null</code> if
	 *         the user cancelled the dialog
	 */
	protected IPath queryForContainer(IContainer initialSelection, String msg, String title) {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getControl().getShell(), initialSelection, allowNewContainerName(), msg);
		if (title != null) {
			dialog.setTitle(title);
		}
		dialog.showClosedProjects(false);
		dialog.open();
		Object[] result = dialog.getResult();
		if (result != null && result.length == 1) {
			return (IPath) result[0];
		}
		return null;
	}

	/**
	 * Displays a Yes/No question to the user with the specified message and
	 * returns the user's response.
	 * 
	 * @param message
	 *            the question to ask
	 * @return <code>true</code> for Yes, and <code>false</code> for No
	 */
	protected boolean queryYesNoQuestion(String message) {
		MessageDialog dialog = new MessageDialog(getContainer().getShell(), IDEWorkbenchMessages.Question, (Image) null, message, MessageDialog.NONE,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0) {
			protected int getShellStyle() {
				return super.getShellStyle() | SWT.SHEET;
			}
		};
		// ensure yes is the default

		return dialog.open() == 0;
	}

	/**
	 * Determine if the page is complete and update the page appropriately.
	 */
	protected void updatePageCompletion() {
		boolean pageComplete = determinePageCompletion();
		setPageComplete(pageComplete);
		if (pageComplete) {
			setErrorMessage(null);
		}
	}

	/**
	 * Returns whether this page's destination specification controls currently
	 * all contain valid values.
	 * <p>
	 * The <code>WizardDataTransferPage</code> implementation of this method
	 * returns <code>true</code>. Subclasses may reimplement this hook method.
	 * </p>
	 * 
	 * @return <code>true</code> indicating validity of all controls in the
	 *         destination specification group
	 */
	protected boolean validateDestinationGroup() {
		return true;
	}

	/**
	 * Returns whether this page's options group's controls currently all
	 * contain valid values.
	 * <p>
	 * The <code>WizardDataTransferPage</code> implementation of this method
	 * returns <code>true</code>. Subclasses may reimplement this hook method.
	 * </p>
	 * 
	 * @return <code>true</code> indicating validity of all controls in the
	 *         options group
	 */
	protected boolean validateOptionsGroup() {
		return true;
	}

	/**
	 * Create the options specification widgets.
	 * 
	 * @param parent
	 *            org.eclipse.swt.widgets.Composite
	 */
	protected void createOptionsGroup(Composite parent) {
		// options group
		Group optionsGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		optionsGroup.setLayout(layout);
		optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		optionsGroup.setText(IDEWorkbenchMessages.WizardExportPage_options);
		optionsGroup.setFont(parent.getFont());

		createOptionsGroupButtons(optionsGroup);

	}

	/**
	 * Display an error dialog with the specified message.
	 * 
	 * @param message
	 *            the error message
	 */
	protected void displayErrorDialog(String message) {
		MessageDialog.open(MessageDialog.ERROR, getContainer().getShell(), getErrorDialogTitle(), message, SWT.SHEET);
	}

	/**
	 * Display an error dislog with the information from the supplied exception.
	 * 
	 * @param exception
	 *            Throwable
	 */
	protected void displayErrorDialog(Throwable exception) {
		String message = exception.getMessage();
		// Some system exceptions have no message
		if (message == null) {
			message = NLS.bind(IDEWorkbenchMessages.WizardDataTransfer_exceptionMessage, exception);
		}
		displayErrorDialog(message);
	}

	/**
	 * The <code>WizardResourceImportPage</code> implementation of this
	 * <code>WizardDataTransferPage</code> method returns <code>true</code>.
	 * Subclasses may override this method.
	 */
	protected boolean allowNewContainerName() {
		return true;
	}

	/**
	 * Create the import source selection widget
	 */
	protected void createFileSelectionGroup(Composite parent) {

		root = new FileElement("Dummy", null, true);
		this.treeContentProvider = getFolderProvider();
		this.listContentProvider = getFileProvider();
		this.treeLabelProvider = new WorkbenchLabelProvider();
		this.listLabelProvider = new WorkbenchLabelProvider();
		createContents(parent, SWT.NONE, DialogUtil.inRegularFontMode(parent));

		ICheckStateListener listener = new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateWidgetEnablements();
			}
		};

		WorkbenchViewerComparator comparator = new WorkbenchViewerComparator();
		setTreeComparator(comparator);
		setListComparator(comparator);
		addCheckStateListener(listener);

	}

	/*
	 * @see WizardDataTransferPage.getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return IDEWorkbenchMessages.WizardImportPage_errorDialogTitle;
	}

	/**
	 * Returns this page's list of currently-specified resources to be imported.
	 * This is the primary resource selection facility accessor for subclasses.
	 * 
	 * @return a list of resources currently selected for export (element type:
	 *         <code>IResource</code>)
	 */
	protected List<FileElement> getSelectedResources() {
		final ArrayList<FileElement> returnValue = new ArrayList<FileElement>();

		IElementFilter passThroughFilter = new IElementFilter() {

			public void filterElements(Collection elements, IProgressMonitor monitor) {
				returnValue.addAll(elements);
			}

			public void filterElements(Object[] elements, IProgressMonitor monitor) {
				for (int i = 0; i < elements.length; i++) {
					returnValue.add((FileElement) elements[i]);
				}
			}
		};

		try {
			// Iterate through the children of the root as the root is not in
			// the store
			Object[] children = treeContentProvider.getChildren(root);
			for (int i = 0; i < children.length; ++i) {
				findAllSelectedListElements(children[i], null, whiteCheckedTreeItems.contains(children[i]), passThroughFilter, null);
			}
		} catch (InterruptedException exception) {
			return new ArrayList<FileElement>();
		}

		return returnValue;
	}

	/**
	 * Returns this page's list of currently-specified resources to be imported
	 * filtered by the IElementFilter.
	 * 
	 */
	protected void getSelectedResources(IElementFilter filter, IProgressMonitor monitor) throws InterruptedException {
		// Iterate through the children of the root as the root is not in
		// the store
		Object[] children = treeContentProvider.getChildren(root);
		for (int i = 0; i < children.length; ++i) {
			findAllSelectedListElements(children[i], null, whiteCheckedTreeItems.contains(children[i]), filter, monitor);
		}
	}

	/**
	 * Returns the error message for when the source conflicts with the
	 * destination.
	 */
	protected final String getSourceConflictMessage() {
		return (IDEWorkbenchMessages.WizardImportPage_importOnReceiver);
	}

	/*
	 * @see WizardDataTransferPage.determinePageCompletion.
	 */
	protected boolean determinePageCompletion() {
		// Check for valid projects before making the user do anything
		if (noOpenProjects()) {
			setErrorMessage(IDEWorkbenchMessages.WizardImportPage_noOpenProjects);
			return false;
		}

		boolean complete = validateSourceGroup() && validateDestinationGroup() && validateOptionsGroup();

		// Avoid draw flicker by not clearing the error
		// message unless all is valid.
		if (complete) {
			setErrorMessage(null);
		}

		return complete;
	}

	/**
	 * Returns whether or not the passed workspace has any open projects
	 * 
	 * @return boolean
	 */
	private boolean noOpenProjects() {
		IProject[] projects = IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isOpen()) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		composite.setFont(parent.getFont());

		createSourceGroup(composite);

		createOptionsGroup(composite);

		restoreWidgetValues();
		updateWidgetEnablements();
		setPageComplete(determinePageCompletion());
		setErrorMessage(null); // should not initially have error message

		setControl(composite);
		validateSourceGroup();
	}

	/**
	 * Create the import options specification widgets.
	 */
	protected void createOptionsGroupButtons(Group optionsGroup) {

		// overwrite... checkbox
		overwriteExistingResourcesCheckbox = new Button(optionsGroup, SWT.CHECK);
		overwriteExistingResourcesCheckbox.setFont(optionsGroup.getFont());
		overwriteExistingResourcesCheckbox.setText(DataTransferMessages.FileImport_overwriteExisting);

		updateWidgetEnablements();
	}

	private Composite createAdvancedSection(Composite parent) {
		Composite linkedResourceComposite = new Composite(parent, SWT.NONE);
		linkedResourceComposite.setFont(parent.getFont());
		linkedResourceComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		linkedResourceComposite.setLayout(layout);

		Button tmp = new Button(linkedResourceComposite, SWT.CHECK);
		int indent = tmp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		tmp.dispose();

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		gridData.horizontalIndent = indent;

		Composite relativeGroup = new Composite(linkedResourceComposite, 0);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalIndent = indent;
		relativeGroup.setFont(parent.getFont());
		relativeGroup.setLayoutData(gridData);

		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		relativeGroup.setLayout(layout);

		updateWidgetEnablements();

		return linkedResourceComposite;

	}

	/**
	 * Create the group for creating the root directory
	 */
	protected void createRootDirectoryGroup(Composite parent) {
		Composite sourceContainerGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		sourceContainerGroup.setLayout(layout);
		sourceContainerGroup.setFont(parent.getFont());
		sourceContainerGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		Label groupLabel = new Label(sourceContainerGroup, SWT.NONE);
		groupLabel.setText(getSourceLabel());
		groupLabel.setFont(parent.getFont());

		// source name entry field
		sourceNameField = new Combo(sourceContainerGroup, SWT.BORDER);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		sourceNameField.setLayoutData(data);
		sourceNameField.setFont(parent.getFont());

		sourceNameField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateFromSourceField();
			}
		});

		sourceNameField.addKeyListener(new KeyListener() {
			/*
			 * @see KeyListener.keyPressed
			 */
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					entryChanged = false;
					updateFromSourceField();
				}
			}

			/*
			 * @see KeyListener.keyReleased
			 */
			public void keyReleased(KeyEvent e) {
			}
		});

		sourceNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				entryChanged = true;
			}
		});

		sourceNameField.addFocusListener(new FocusListener() {
			/*
			 * @see FocusListener.focusGained(FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				// Do nothing when getting focus
			}

			/*
			 * @see FocusListener.focusLost(FocusEvent)
			 */
			public void focusLost(FocusEvent e) {
				// Clear the flag to prevent constant update
				if (entryChanged) {
					entryChanged = false;
					updateFromSourceField();
				}

			}
		});

		// source browse button
		sourceBrowseButton = new Button(sourceContainerGroup, SWT.PUSH);
		sourceBrowseButton.setText(DataTransferMessages.DataTransfer_browse);
		sourceBrowseButton.addListener(SWT.Selection, this);
		sourceBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		sourceBrowseButton.setFont(parent.getFont());
		setButtonLayoutData(sourceBrowseButton);
	}

	/**
	 * Update the receiver from the source name field.
	 */

	private void updateFromSourceField() {
		setSourceName(sourceNameField.getText());
		
		// Update enablements when this is selected
		updateWidgetEnablements();
		fileStructureProvider.clearVisitedDirs();
		setTreeviewFocus();
	}

	/**
	 * Creates and returns a <code>FileSystemElement</code> if the specified
	 * file system object merits one. The criteria for this are: Also create the
	 * children.
	 */
	protected FileElement createRootElement(File fileSystemObject, FileStructureProvider provider) {
		boolean isContainer = fileSystemObject.isDirectory();
		String elementLabel = provider.getLabel(fileSystemObject);

		// Use an empty label so that display of the element's full name
		// doesn't include a confusing label
		FileElement dummyParent = new FileElement("", null, true);//$NON-NLS-1$
		dummyParent.setPopulated();
		FileElement result = new FileElement(elementLabel, dummyParent, isContainer);
		result.setFile(fileSystemObject);

		// Get the files for the element so as to build the first level
		result.getFiles(provider);

		return dummyParent;
	}

	/**
	 * Create the import source specification widgets
	 */
	protected void createSourceGroup(Composite parent) {
		createRootDirectoryGroup(parent);
		createFileSelectionGroup(parent);
	}

	/**
	 * Answer a boolean indicating whether the specified source currently exists
	 * and is valid
	 */
	protected boolean ensureSourceIsValid() {
		if (new File(getSourceDirectoryName()).isDirectory()) {
			return true;
		}

		setErrorMessage(DataTransferMessages.FileImport_invalidSource);
		return false;
	}

	/**
	 * Execute the passed import operation. Answer a boolean indicating success.
	 */
	protected boolean executeImportOperation(ImportOperation op) {
		initializeOperation(op);

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			displayErrorDialog(e.getTargetException());
			return false;
		}

		IStatus status = op.getStatus();
		if (!status.isOK()) {
			ErrorDialog.openError(getContainer().getShell(), DataTransferMessages.FileImport_importProblems, null, // no
																													// special
																													// message
					status);
			return false;
		}

		return true;
	}

	/**
	 * Returns a content provider for <code>FileSystemElement</code>s that
	 * returns only files as children.
	 */
	protected ITreeContentProvider getFileProvider() {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof FileElement) {
					FileElement element = (FileElement) o;
					return element.getFiles(fileStructureProvider).toArray();
				}
				return new Object[0];
			}
		};
	}

	/**
	 * Answer the root FileSystemElement that represents the contents of the
	 * currently-specified source. If this FileSystemElement is not currently
	 * defined then create and return it.
	 */
	protected FileElement getFileSystemTree() {

		File sourceDirectory = getSourceDirectory();
		if (sourceDirectory == null) {
			return null;
		}

		return selectFiles(sourceDirectory, fileStructureProvider);
	}

	/**
	 * Returns a content provider for <code>FileSystemElement</code>s that
	 * returns only folders as children.
	 */
	protected ITreeContentProvider getFolderProvider() {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof FileElement) {
					FileElement element = (FileElement) o;
					return element.getFolders(fileStructureProvider).toArray();
				}
				return new Object[0];
			}

			public boolean hasChildren(Object o) {
				if (o instanceof FileElement) {
					FileElement element = (FileElement) o;
					if (element.isPopulated()) {
						return getChildren(element).length > 0;
					}

					return true;
				}
				return false;
			}
		};
	}

	/**
	 * Returns a File object representing the currently-named source directory
	 * iff it exists as a valid directory, or <code>null</code> otherwise.
	 */
	protected File getSourceDirectory() {
		return getSourceDirectory(this.sourceNameField.getText());
	}

	/**
	 * Returns a File object representing the currently-named source directory
	 * iff it exists as a valid directory, or <code>null</code> otherwise.
	 * 
	 * @param path
	 *            a String not yet formatted for java.io.File compatability
	 */
	private File getSourceDirectory(String path) {
		File sourceDirectory = new File(getSourceDirectoryName(path));
		if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
			return null;
		}

		return sourceDirectory;
	}

	/**
	 * Answer the directory name specified as being the import source. Note that
	 * if it ends with a separator then the separator is first removed so that
	 * java treats it as a proper directory
	 */
	private String getSourceDirectoryName() {
		return getSourceDirectoryName(this.sourceNameField.getText());
	}

	/**
	 * Answer the directory name specified as being the import source. Note that
	 * if it ends with a separator then the separator is first removed so that
	 * java treats it as a proper directory
	 */
	private String getSourceDirectoryName(String sourceName) {
		IPath result = new Path(sourceName.trim());

		if (result.getDevice() != null && result.segmentCount() == 0) {
			result = result.addTrailingSeparator();
		} else {
			result = result.removeTrailingSeparator();
		}

		return result.toOSString();
	}

	/**
	 * Answer the string to display as the label for the source specification
	 * field
	 */
	protected String getSourceLabel() {
		return DataTransferMessages.FileImport_fromDirectory;
	}

	/**
	 * Handle all events and enablements for widgets in this dialog
	 * 
	 * @param event
	 *            Event
	 */
	public void handleEvent(Event event) {
		if (event.widget == sourceBrowseButton) {
			handleSourceBrowseButtonPressed();
		}

		Widget source = event.widget;

		updateWidgetEnablements();
	}

	/**
	 * Open an appropriate source browser so that the user can specify a source
	 * to import from
	 */
	protected void handleSourceBrowseButtonPressed() {

		String currentSource = this.sourceNameField.getText();
		DirectoryDialog dialog = new DirectoryDialog(sourceNameField.getShell(), SWT.SAVE | SWT.SHEET);
		dialog.setText(SELECT_SOURCE_TITLE);
		dialog.setMessage(SELECT_SOURCE_MESSAGE);
		dialog.setFilterPath(getSourceDirectoryName(currentSource));

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			// Just quit if the directory is not valid
			if ((getSourceDirectory(selectedDirectory) == null) || selectedDirectory.equals(currentSource)) {
				return;
			}
			// If it is valid then proceed to populate
			setErrorMessage(null);
			setSourceName(selectedDirectory);
			setTreeviewFocus();
		}
	}

	/**
	 * Initializes the specified operation appropriately.
	 */
	protected void initializeOperation(ImportOperation op) {
		op.setCreateContainerStructure(false);
		op.setOverwriteResources(overwriteExistingResourcesCheckbox.getSelection());
	}

	/**
	 * Returns whether the extension provided is an extension that has been
	 * specified for export by the user.
	 * 
	 * @param extension
	 *            the resource name
	 * @return <code>true</code> if the resource name is suitable for export
	 *         based upon its extension
	 */
	protected boolean isExportableExtension(String extension) {
		return "psf".equals(extension);
	}

	/**
	 * Repopulate the view based on the currently entered directory.
	 */
	protected void resetSelection() {
		FileElement currentRoot = getFileSystemTree();
		setRoot(currentRoot);
	}

	/**
	 * Use the dialog store to restore widget values to the values that they
	 * held last time this wizard was used to completion
	 */
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
			if (sourceNames == null) {
				return; // ie.- no values stored, so stop
			}

			// set filenames history
			for (int i = 0; i < sourceNames.length; i++) {
				sourceNameField.add(sourceNames[i]);
			}

			// radio buttons and checkboxes
			overwriteExistingResourcesCheckbox.setSelection(settings.getBoolean(STORE_OVERWRITE_EXISTING_RESOURCES_ID));

			updateWidgetEnablements();
		}
	}

	/**
	 * Since Finish was pressed, write widget values to the dialog store so that
	 * they will persist into the next invocation of this wizard page
	 */
	protected void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// update source names history
			String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
			if (sourceNames == null) {
				sourceNames = new String[0];
			}

			sourceNames = addToHistory(sourceNames, getSourceDirectoryName());
			settings.put(STORE_SOURCE_NAMES_ID, sourceNames);

			// radio buttons and checkboxes
			settings.put(STORE_OVERWRITE_EXISTING_RESOURCES_ID, overwriteExistingResourcesCheckbox.getSelection());

		}
	}

	/**
	 * Invokes a file selection operation using the specified file system and
	 * structure provider. If the user specifies files to be imported then this
	 * selection is cached for later retrieval and is returned.
	 */
	protected FileElement selectFiles(final File rootFileSystemObject, final FileStructureProvider structureProvider) {

		final FileElement[] results = new FileElement[1];

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				// Create the root element from the supplied file system object
				results[0] = createRootElement(rootFileSystemObject, structureProvider);
			}
		});

		return results[0];
	}

	/**
	 * Sets the source name of the import to be the supplied path. Adds the name
	 * of the path to the list of items in the source combo and selects it.
	 * 
	 * @param path
	 *            the path to be added
	 */
	protected void setSourceName(String path) {

		if (path.length() > 0) {

			String[] currentItems = this.sourceNameField.getItems();
			int selectionIndex = -1;
			for (int i = 0; i < currentItems.length; i++) {
				if (currentItems[i].equals(path)) {
					selectionIndex = i;
				}
			}
			if (selectionIndex < 0) {
				int oldLength = currentItems.length;
				String[] newItems = new String[oldLength + 1];
				System.arraycopy(currentItems, 0, newItems, 0, oldLength);
				newItems[oldLength] = path;
				this.sourceNameField.setItems(newItems);
				selectionIndex = oldLength;
			}
			this.sourceNameField.select(selectionIndex);

			resetSelection();
		}
	}

	/*
	 * (non-Javadoc) Method declared on IDialogPage. Set the selection up when
	 * it becomes visible.
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		resetSelection();
		if (visible) {
			setTreeviewFocus();
			this.sourceNameField.setFocus();
		}
	}

	/**
	 * Update the selections with those in map . Implemented here to give inner
	 * class visibility
	 * 
	 * @param map
	 *            Map - key tree elements, values Lists of list elements
	 */
	protected void updateSelections(final Map map) {
		Runnable runnable = new Runnable() {
			public void run() {
				// We are replacing all selected items with the given selected
				// items,
				// so reinitialize everything.
				listViewer.setAllChecked(false);
				treeViewer.setCheckedElements(new Object[0]);
				whiteCheckedTreeItems = new HashSet();
				Set selectedNodes = new HashSet();
				checkedStateStore = new HashMap();

				// Update the store before the hierarchy to prevent updating
				// parents
				// before all of the children are done
				Iterator keyIterator = map.keySet().iterator();
				while (keyIterator.hasNext()) {
					Object key = keyIterator.next();
					List selections = (List) map.get(key);
					// Replace the items in the checked state store with those
					// from
					// the supplied items
					checkedStateStore.put(key, selections);
					selectedNodes.add(key);
					// proceed up the tree element hierarchy
					Object parent = treeContentProvider.getParent(key);
					if (parent != null) {
						// proceed up the tree element hierarchy and make sure
						// everything is in the table
						primeHierarchyForSelection(parent, selectedNodes);
					}
				}

				// Update the checked tree items. Since each tree item has a
				// selected
				// item, all the tree items will be gray checked.
				treeViewer.setCheckedElements(checkedStateStore.keySet().toArray());
				treeViewer.setGrayedElements(checkedStateStore.keySet().toArray());

				// Update the listView of the currently selected tree item.
				if (currentTreeSelection != null) {
					Object displayItems = map.get(currentTreeSelection);
					if (displayItems != null) {
						listViewer.setCheckedElements(((List) displayItems).toArray());
					}
				}
			}
		};

		BusyIndicator.showWhile(getShell().getDisplay(), runnable);
	}

	/**
	 * Check if widgets are enabled or disabled by a change in the dialog.
	 * Provided here to give access to inner classes.
	 */
	protected void updateWidgetEnablements() {
		boolean pageComplete = determinePageCompletion();
		setPageComplete(pageComplete);
		if (pageComplete) {
			setMessage(null);
		}
	}

	/**
	 * Answer a boolean indicating whether self's source specification widgets
	 * currently all contain valid values.
	 */
	protected boolean validateSourceGroup() {
		File sourceDirectory = getSourceDirectory();
		if (sourceDirectory == null) {
			setMessage(SOURCE_EMPTY_MESSAGE);
			return false;
		}

		if (sourceConflictsWithDestination(new Path(sourceDirectory.getPath()))) {
			setMessage(null);
			setErrorMessage(getSourceConflictMessage());
			return false;
		}

		List resourcesToExport = getAllWhiteCheckedItems();
		if (resourcesToExport.size() == 0) {
			setMessage(null);
			setErrorMessage(DataTransferMessages.FileImport_noneSelected);
			return false;
		}

		setErrorMessage(null);
		return true;
	}

	/**
	 * Returns whether the source location conflicts with the destination
	 * resource. This will occur if the source is already under the destination.
	 * 
	 * @param sourcePath
	 *            the path to check
	 * @return <code>true</code> if there is a conflict, <code>false</code> if
	 *         not
	 */
	protected boolean sourceConflictsWithDestination(IPath sourcePath) {
		// TODO
		return false;
	}

}
