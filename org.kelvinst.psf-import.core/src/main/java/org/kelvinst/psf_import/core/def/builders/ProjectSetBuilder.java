package org.kelvinst.psf_import.core.def.builders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.kelvinst.psf_import.core.def.IProject;
import org.kelvinst.psf_import.core.def.IProjectSet;
import org.kelvinst.psf_import.core.def.IProvider;
import org.kelvinst.psf_import.core.def.IScmInfo;
import org.kelvinst.psf_import.core.def.IWorkingSet;
import org.kelvinst.psf_import.core.def.Project;
import org.kelvinst.psf_import.core.def.ProjectSet;
import org.kelvinst.psf_import.core.def.Provider;
import org.kelvinst.psf_import.core.def.ProviderType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class ProjectSetBuilder {

	public static IProjectSet build(File file) {
		try {
			return build(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file));
		} catch (Exception e) {
			throw new IllegalStateException("Error parsing project set file: ".concat(file.getAbsolutePath()), e);
		}
	}
	
	public static IProjectSet build(Document document) {
		return build(document.getDocumentElement());
	}

	private static IProjectSet build(Element psf) {
		List<IProvider> providers = buildProviders(psf.getElementsByTagName("provider"));
		
		List<IWorkingSet> workingSets = buildWorkingSets(psf);
		
		return new ProjectSet(providers, workingSets);
	}

	private static List<IProvider> buildProviders(NodeList providerNodes) {
		List<IProvider> result = new ArrayList<IProvider>(); 
		
		for (int i = 0; i < providerNodes.getLength(); i++) {
			result.add(buildProvider((Element) providerNodes.item(i)));
		}
		
		return result;
	}

	private static Provider buildProvider(Element providerTag) {
		ProviderType type = ProviderType.fromId(providerTag.getAttribute("id"));
		
		List<IProject> projects = buildProjects(type, providerTag.getElementsByTagName("project"));
		
		return new Provider(projects, type);
	}

	private static List<IProject> buildProjects(ProviderType providerType, NodeList projectNodes) {
		List<IProject> result = new ArrayList<IProject>(); 
		
		for (int i = 0; i < projectNodes.getLength(); i++) {
		    result.add(buildProject(providerType, (Element) projectNodes.item(i)));	    
		}
		
		return result;
	}
	
	private static IProject buildProject(ProviderType providerType, Element projectTag) {
		IScmInfo scmInfo = providerType.getScmInfoBuilder().build(projectTag.getAttribute("reference"));
		return new Project(scmInfo.getName(), scmInfo);
	}
	
	private static List<IWorkingSet> buildWorkingSets(Element psf) {
		return null;
	}
	
}
