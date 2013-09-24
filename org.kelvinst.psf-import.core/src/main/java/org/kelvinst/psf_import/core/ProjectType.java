package org.kelvinst.psf_import.core;

public enum ProjectType {

	JAVA("org.eclipse.jdt.ui.PersistableJavaElementFactory"),
	RESOURCE("org.eclipse.ui.internal.model.ResourceFactory");
	
	private String defaultFactoryId;

	private ProjectType(String defaultFactoryId) {
		this.defaultFactoryId = defaultFactoryId;
	}
	
	public String getDefaultFactoryId() {
		return defaultFactoryId;
	}
	
}
