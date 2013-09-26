package org.kelvinst.psf_import.core.def;

public enum WorkingSetItemType {

	JAVA("org.eclipse.jdt.ui.PersistableJavaElementFactory"),
	RESOURCE("org.eclipse.ui.internal.model.ResourceFactory");
	
	private String factoryId;

	private WorkingSetItemType(String factoryId) {
		this.factoryId = factoryId;
	}

	public String getFactoryId() {
		return factoryId;
	}
	
}
