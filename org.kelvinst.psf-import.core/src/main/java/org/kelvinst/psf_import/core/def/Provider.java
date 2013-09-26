package org.kelvinst.psf_import.core.def;

import java.util.List;

public class Provider implements IProvider {

	private List<IProject> projects;
	private ProviderType type;

	public Provider(List<IProject> projects, ProviderType type) {
		super();
		this.projects = projects;
		this.type = type;
	}

	@Override
	public List<IProject> getProjects() {
		return projects;
	}

	@Override
	public ProviderType getType() {
		return type;
	}

}
