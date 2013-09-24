package org.kelvinst.psf_import.core;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class ProjectSetFileBuilder {

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
		List<IProject> projects = buildProjects(psf.getElementsByTagName("provider"));
		
		List<IWorkingSet> workingSets = buildWorkingSets(psf);
		
		return new ProjectSet(projects, workingSets);
	}

	private static List<IProject> buildProjects(NodeList providers) {
		return null;
	}
	
	private static List<IWorkingSet> buildWorkingSets(Element psf) {
		return null;
	}
	
}
