package org.kelvinst.psfimport.ui;

import org.eclipse.ui.model.WorkbenchContentProvider;

public abstract class FileElementContentProvider extends WorkbenchContentProvider {
	
	protected FileStructureProvider fileStructureProvider;
	
	public FileElementContentProvider(FileStructureProvider fileStructureProvider) {
		this.fileStructureProvider = fileStructureProvider;
	}
	
	@Override
	public FileElement getParent(Object element) {
		return (FileElement) super.getParent(element);
	}
	
	@Override
	public abstract FileElement[] getChildren(Object o);
	
}