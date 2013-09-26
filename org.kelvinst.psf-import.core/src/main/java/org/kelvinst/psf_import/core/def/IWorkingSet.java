package org.kelvinst.psf_import.core.def;

import java.util.List;

public interface IWorkingSet {

	WorkingSetType getType();
	
	List<IWorkingSetItem> getItems();
	
}
