package edu.stanford.bmir.protegex.icd.diff;

import javax.swing.Icon;

import edu.stanford.smi.protege.util.ComponentUtilities;

public class ICDIcons {

	
	public static Icon getAddedIcon () {
		return getIconFromFile("TreeAdded.gif");
	}
	
	public static Icon getDeletedIcon () {
		return getIconFromFile("TreeDeleted.gif");
	}
	
	public static Icon getClsWarningIcon () {
		return getIconFromFile("ClassWithWarning.gif");
	}
	
	
	private static Icon getIconFromFile (String fileName) {		
		return ComponentUtilities.loadImageIcon(ICDDiff.class, "images/" + fileName);
	}
	
}
