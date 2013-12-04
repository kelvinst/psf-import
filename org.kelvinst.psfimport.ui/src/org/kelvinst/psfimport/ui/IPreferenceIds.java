package org.kelvinst.psfimport.ui;

public interface IPreferenceIds {
	public static final String PREFIX = PsfImportPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	/*
	 * Preference to enable the import of a project set to be run in the
	 * background
	 */
	public static final String RUN_IN_BACKGROUND = PREFIX
			+ "run_in_background"; //$NON-NLS-1$
}
