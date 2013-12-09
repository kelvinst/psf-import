package org.kelvinst.psfimport.ui;

import java.util.List;

public final class FolderContentProvider extends FileElementContentProvider {
	
	public FolderContentProvider(FileStructureProvider fileStructureProvider) {
		super(fileStructureProvider);
	}

	@Override
	public FileElement[] getChildren(Object o) {
		if (o instanceof FileElement) {
			FileElement element = (FileElement) o;
			List<FileElement> folders = element.getFolders(fileStructureProvider);
			return folders.toArray(new FileElement[folders.size()]);
		}
		return new FileElement[0];
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
	
}