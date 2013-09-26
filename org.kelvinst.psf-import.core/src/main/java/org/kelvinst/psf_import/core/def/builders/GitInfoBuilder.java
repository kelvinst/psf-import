package org.kelvinst.psf_import.core.def.builders;

import org.apache.commons.lang.StringUtils;
import org.kelvinst.psf_import.core.def.IScmInfo;
import org.kelvinst.psf_import.core.def.ScmInfo;

public class GitInfoBuilder implements IScmInfoBuilder {

	@Override
	public IScmInfo build(String reference) {
		String[] split = reference.split(",");
		String path = split[3];
		return new ScmInfo(StringUtils.substringAfterLast(path, "/"), split[1], split[2], path);
	}

}
