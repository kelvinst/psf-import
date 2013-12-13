/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.kelvinst.psfimport.ui.elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.kelvinst.psfimport.ui.providers.FileStructureProvider;

/**
 * Instances of this class represent files or file-like entities (eg.- zip file
 * entries) on the local file system. They do not represent resources within the
 * workbench. This distinction is made because the representation of a file
 * system resource is significantly different from that of a workbench resource.
 * 
 * If self represents a collection (eg.- file system directory, zip directory)
 * then its icon will be the folderIcon static field. Otherwise (ie.- self
 * represents a file system file) self's icon is stored in field "icon", and is
 * determined by the extension of the file that self represents.
 * 
 * This class is adaptable, and implements one adapter itself, namely the
 * IWorkbenchAdapter adapter used for navigation and display in the workbench.
 */
public class FileElement {
	private List<FileElement> folders = null;
	private List<FileElement> files = null;
	private boolean isDirectory = false;
	private String name;
	private File file;
	private FileElement parent;
	private boolean populated = false;

	/**
	 * Returns a list of the files that are immediate children. Use the supplied
	 * provider if it needs to be populated. of this folder.
	 */
	public List<FileElement> getFiles(FileStructureProvider provider) {
		if (!populated) {
			populate(provider);
		}
		return getFiles();
	}

	/**
	 * Returns a list of the folders that are immediate children. Use the
	 * supplied provider if it needs to be populated. of this folder.
	 */
	public List<FileElement> getFolders(FileStructureProvider provider) {
		if (!populated) {
			populate(provider);
		}
		return getFolders();
	}

	/**
	 * Return whether or not population has happened for the receiver.
	 */
	public boolean isPopulated() {
		return this.populated;
	}

	/**
	 * Populate the files and folders of the receiver using the supplied
	 * structure provider.
	 * 
	 * @param provider
	 *            org.eclipse.ui.wizards.datatransfer.IImportStructureProvider
	 */
	private void populate(FileStructureProvider provider) {
		File fileSystemObject = getFile();
		if (fileSystemObject != null) {
			List<File> children = provider.getChildren(fileSystemObject);
			if (children != null) {
				Iterator<File> childrenEnum = children.iterator();
				while (childrenEnum.hasNext()) {
					File file = childrenEnum.next();

					String elementLabel = provider.getLabel(file);
					// Create one level below
					FileElement result = new FileElement(elementLabel, this, file.isDirectory());
					result.setFile(file);
				}
			}
		}
		setPopulated();
	}

	/**
	 * Set whether or not population has happened for the receiver to true.
	 */
	public void setPopulated() {
		this.populated = true;
	}

	/**
	 * Creates a new <code>FileSystemElement</code> and initializes it and its
	 * parent if applicable.
	 * 
	 * @param name
	 *            The name of the element
	 * @param parent
	 *            The parent element. May be <code>null</code>
	 * @param isDirectory
	 *            if <code>true</code> this is representing a directory,
	 *            otherwise it is a file.
	 */
	public FileElement(String name, FileElement parent, boolean isDirectory) {
		this.name = name;
		this.parent = parent;
		this.isDirectory = isDirectory;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	/**
	 * Adds the passed child to this object's collection of children.
	 * 
	 * @param child
	 *            FileSystemElement
	 */
	public void addChild(FileElement child) {
		if (child.isDirectory()) {
			if (folders == null) {
				folders = new ArrayList<FileElement>(1);
			}
			folders.add(child);
		} else {
			if (files == null) {
				files = new ArrayList<FileElement>(1);
			}
			files.add(child);
		}
	}

	/**
	 * Returns the extension of this element's filename.
	 * 
	 * @return The extension or an empty string if there is no extension.
	 */
	public String getFileNameExtension() {
		int lastDot = name.lastIndexOf('.');
		return lastDot < 0 ? "" : name.substring(lastDot + 1); //$NON-NLS-1$
	}

	/**
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	public ImageDescriptor getImageDescriptor() {
		if (isDirectory()) {
			return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
		} else {
			return WorkbenchPlugin.getDefault().getEditorRegistry().getImageDescriptor(name);
		}
	}

	/**
	 * Answer the files property of this element. Answer an empty list if the
	 * files property is null. This method should not be used to add children to
	 * the receiver. Use addChild(FileSystemElement) instead.
	 * 
	 * @return AdaptableList The list of files parented by the receiver.
	 */
	public List<FileElement> getFiles() {
		if (files == null) {
			// lazily initialize (can't share result since it's modifiable)
			files = new ArrayList<FileElement>(0);
		}
		return files;
	}

	/**
	 * Returns the file system object property of this element
	 * 
	 * @return the file system object
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Returns a list of the folders that are immediate children of this folder.
	 * Answer an empty list if the folders property is null. This method should
	 * not be used to add children to the receiver. Use
	 * addChild(FileSystemElement) instead.
	 * 
	 * @return AdapatableList The list of folders parented by the receiver.
	 */
	public List<FileElement> getFolders() {
		if (folders == null) {
			// lazily initialize (can't share result since it's modifiable)
			folders = new ArrayList<FileElement>(0);
		}
		return folders;
	}

	/**
	 * Return the parent of this element.
	 * 
	 * @return the parent file system element, or <code>null</code> if this is
	 *         the root
	 */
	public FileElement getParent() {
		return this.parent;
	}

	/**
	 * @return boolean <code>true</code> if this element represents a directory,
	 *         and <code>false</code> otherwise.
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * Removes a sub-folder from this file system element.
	 * 
	 * @param child
	 *            The child to remove.
	 */
	public void removeFolder(FileElement child) {
		if (folders == null) {
			return;
		}
		folders.remove(child);
		child.setParent(null);
	}

	/**
	 * Set the file system object property of this element
	 * 
	 * @param value
	 *            the file system object
	 */
	public void setFile(File value) {
		file = value;
	}

	/**
	 * Sets the parent of this file system element.
	 * 
	 * @param element
	 *            The new parent.
	 */
	public void setParent(FileElement element) {
		parent = element;
	}

	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (isDirectory()) {
			buf.append("Folder(");//$NON-NLS-1$
		} else {
			buf.append("File(");//$NON-NLS-1$
		}
		buf.append(name).append(")");//$NON-NLS-1$
		if (!isDirectory()) {
			return buf.toString();
		}
		buf.append(" folders: ");//$NON-NLS-1$
		buf.append(folders);
		buf.append(" files: ");//$NON-NLS-1$
		buf.append(files);
		return buf.toString();
	}
}
