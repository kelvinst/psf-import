package org.kelvinst.psf_import.core;

import java.util.List;

public interface IProject {

	String getName();
	
	String getScmRepository();
	
	String getScmPath();

	ProjectType getType();
	
	List<IWorkingSet> getWrokingSets();
	
}
