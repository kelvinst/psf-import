package org.kelvinst.psfimport.ui.providers;

import org.eclipse.ui.model.WorkbenchContentProvider;
import org.kelvinst.psfimport.ui.elements.FileElement;

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