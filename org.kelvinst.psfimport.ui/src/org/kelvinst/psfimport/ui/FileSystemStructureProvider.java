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
package org.kelvinst.psfimport.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * This class provides information regarding the structure and
 * content of specified file system File objects.
 * 
 * class copied from org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider as its singleton
 */
public class FileSystemStructureProvider implements IImportStructureProvider {

	private Set<String> visitedDirs;

    /* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public List<File> getChildren(Object element) {
        File folder = (File) element;
        String[] children = folder.list();
        int childrenLength = children == null ? 0 : children.length;
        List<File> result = new ArrayList<File>(childrenLength);

        for (int i = 0; i < childrenLength; i++) {
        	File file = new File(folder, children[i]);
        	if(isRecursiveLink(file))
        		continue;     
        	// TODO - Switch the extension validation by a FileNameFilter or something like this
        	if (file.isDirectory() || "psf".equals(getFileExtension(file)))
        		result.add(file);
		}
        
        return result;
    }

    private String getFileExtension(File file) {
        String name = file.getAbsolutePath();
		int lastDot = name.lastIndexOf('.');
        return lastDot < 0 ? "" : name.substring(lastDot + 1); //$NON-NLS-1$
    }

    
    private void initVisitedDirs(){
    	if(visitedDirs == null){
    		visitedDirs = new HashSet<String>();
    	}
    }
    
	private boolean isRecursiveLink(File childFile) {

		if (childFile.isDirectory()) {
			try {
				String canonicalPath = childFile.getCanonicalPath();
				initVisitedDirs();
				return !visitedDirs.add(canonicalPath);
			} catch (IOException e) {
				PsfImportPlugin.error(e.getMessage(), e);
			}
		}
		return false;
	}

	/* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public InputStream getContents(Object element) {
        try {
            return new FileInputStream((File) element);
        } catch (FileNotFoundException e) {
        	PsfImportPlugin.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public String getFullPath(Object element) {
        return ((File) element).getPath();
    }

    /* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public String getLabel(Object element) {

        //Get the name - if it is empty then return the path as it is a file root
        File file = (File) element;
        String name = file.getName();
        if (name.length() == 0) {
			return file.getPath();
		}
        return name;
    }

    /* (non-Javadoc)
     * Method declared on IImportStructureProvider
     */
    public boolean isFolder(Object element) {
        return ((File) element).isDirectory();
    }
    
    /**
     * Clears the visited dir information
     */
    public void clearVisitedDirs() {
    	if(visitedDirs!=null)
    		visitedDirs.clear();
    }
}
