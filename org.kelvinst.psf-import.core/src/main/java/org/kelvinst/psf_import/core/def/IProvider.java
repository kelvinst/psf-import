package org.kelvinst.psf_import.core.def;

import java.util.List;

public interface IProvider {

	List<IProject> getProjects();
	
	ProviderType getType();
	
}
