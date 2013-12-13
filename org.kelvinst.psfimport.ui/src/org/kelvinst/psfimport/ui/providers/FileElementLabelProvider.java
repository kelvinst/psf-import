package org.kelvinst.psfimport.ui.providers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.kelvinst.psfimport.ui.elements.FileElement;

public class FileElementLabelProvider extends LabelProvider {

	private ResourceManager resourceManager;

	public Image getImage(Object element) {
		if (element == null)
			return null;
		ImageDescriptor descriptor = ((FileElement) element).getImageDescriptor();
		return (Image) getResourceManager().get(descriptor);
	}

	public String getText(Object element) {
		return element == null ? "" : ((FileElement) element).getName();
	}

	/**
	 * Lazy load the resource manager
	 * 
	 * @return The resource manager, create one if necessary
	 */
	private ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new LocalResourceManager(JFaceResources.getResources());
		}

		return resourceManager;
	}

}
