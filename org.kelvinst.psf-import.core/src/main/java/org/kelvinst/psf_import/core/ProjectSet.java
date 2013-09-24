package org.kelvinst.psf_import.core;

import java.util.List;

public class ProjectSet implements IProjectSet {

	private List<IProject> projects;
	private List<IWorkingSet> workingSets;
	
	public ProjectSet(List<IProject> projects, List<IWorkingSet> workingSets) {
		super();
		this.projects = projects;
		this.workingSets = workingSets;
	}

	@Override
	public List<IProject> getProjects() {
		return projects;
	}

	@Override
	public List<IWorkingSet> getWorkingSets() {
		return workingSets;
	}

}
