package org.kelvinst.psfimport.ui.providers;

import java.util.List;

import org.kelvinst.psfimport.ui.elements.FileElement;

public final class FileContentProvider extends FileElementContentProvider {
	
	public FileContentProvider(FileStructureProvider fileStructureProvider) {
		super(fileStructureProvider);
	}

	@Override
	public FileElement[] getChildren(Object o) {
		if (o instanceof FileElement) {
			FileElement element = (FileElement) o;
			List<FileElement> files = element.getFiles(fileStructureProvider);
			return files.toArray(new FileElement[files.size()]);
		}
		return new FileElement[0];
	}
	
}