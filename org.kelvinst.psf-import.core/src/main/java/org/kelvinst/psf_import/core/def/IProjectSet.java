package org.kelvinst.psf_import.core.def;

import java.util.List;

public interface IProjectSet {

	List<IProvider> getProviders();
	
	List<IWorkingSet> getWorkingSets();
	
}
