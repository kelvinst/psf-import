package org.kelvinst.psfimport.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PsfImportPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.kelvinst.psfimport.ui"; //$NON-NLS-1$

	// The shared instance
	private static PsfImportPlugin plugin;
	
	/**
	 * The constructor
	 */
	public PsfImportPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PsfImportPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    /**
     * Logs the given message and throwable to the platform log.
     * 
     * If you have a status object in hand call log(String, IStatus) instead.
     * 
     * This convenience method is for internal use by the IDE Workbench only and
     * must not be called outside the IDE Workbench.
     * 
     * @param message
     *            A high level UI message describing when the problem happened.
     * @param t
     *            The throwable from where the problem actually occurred.
     */
    public static void error(String message, Throwable t) {
        error(message, new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR,
        		message, null));
    }

    /**
     * Logs the given message and status to the platform log.
     * 
     * This convenience method is for internal use by the IDE Workbench only and
     * must not be called outside the IDE Workbench.
     * 
     * @param message
     *            A high level UI message describing when the problem happened.
     *            May be <code>null</code>.
     * @param status
     *            The status describing the problem. Must not be null.
     */
    public static void error(String message, IStatus status) {
        if (message != null) {
            getDefault().getLog().log(
                    new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR,
							message, null));
        }

        getDefault().getLog().log(status);
    }
}
