package org.kelvinst.psf_import.core.def;

public class ScmInfo implements IScmInfo {

	private String name;
	private String repository;
	private String branch;
	private String path;
	
	public ScmInfo(String name, String repository, String branch, String path) {
		super();
		this.name = name;
		this.repository = repository;
		this.branch = branch;
		this.path = path;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getRepository() {
		return repository;
	}

	@Override
	public String getBranch() {
		return branch;
	}

	@Override
	public String getPath() {
		return path;
	}
	
}
