package org.kelvinst.psf_import.core.def;


public class Project implements IProject {

	private String name;
	private IScmInfo scmInfo;

	public Project(String name, IScmInfo scmInfo) {
		super();
		this.name = name;
		this.scmInfo = scmInfo;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IScmInfo getScmInfo() {
		return scmInfo;
	}
	
}
