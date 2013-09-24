package org.kelvinst.psf_import.core;

import java.util.ArrayList;
import java.util.List;

public class Project implements IProject {

	private String name;
	private String scmRepository;
	private String scmPath;
	private String scmTag;

	public Project(String name, String scmRepository, String scmPath, String scmTag) {
		super();
		this.name = name;
		this.scmRepository = scmRepository;
		this.scmPath = scmPath;
		this.scmTag = scmTag;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getScmRepository() {
		return scmRepository;
	}

	@Override
	public String getScmPath() {
		return scmPath;
	}
	
	@Override
	public String getScmTag() {
		return scmTag;
	}

}
