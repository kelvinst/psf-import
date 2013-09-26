package org.kelvinst.psf_import.core.def;

import java.util.List;

public class ProjectSet implements IProjectSet {

	private List<IProvider> providers;
	private List<IWorkingSet> workingSets;
	
	public ProjectSet(List<IProvider> providers, List<IWorkingSet> workingSets) {
		super();
		this.providers = providers;
		this.workingSets = workingSets;
	}

	@Override
	public List<IProvider> getProviders() {
		return providers;
	}

	@Override
	public List<IWorkingSet> getWorkingSets() {
		return workingSets;
	}

}
