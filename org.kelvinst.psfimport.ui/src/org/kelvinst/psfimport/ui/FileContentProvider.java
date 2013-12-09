package org.kelvinst.psfimport.ui;

import java.util.List;

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