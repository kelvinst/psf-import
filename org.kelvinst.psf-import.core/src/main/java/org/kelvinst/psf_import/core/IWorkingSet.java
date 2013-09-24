package org.kelvinst.psf_import.core;

import java.util.List;

public interface IWorkingSet {

	WorkingSetType getType();
	
	List<IProject> getProjects();
	
}
