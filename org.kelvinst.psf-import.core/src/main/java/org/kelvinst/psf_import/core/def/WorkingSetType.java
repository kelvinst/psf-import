package org.kelvinst.psf_import.core.def;

public enum WorkingSetType {

	JAVA("org.eclipse.jdt.ui.JavaWorkingSetPage"),
	RESOURCE("org.eclipse.ui.resourceWorkingSetPage");

	private String pageId;
	
	private WorkingSetType(String pageId) {
		this.pageId = pageId;
	}
	
	public String getPageId() {
		return pageId;
	}
	
}
