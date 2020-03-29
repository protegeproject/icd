package edu.stanford.bmir.icd.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * Class declaring utility functions for ICD utility classes 
 * (like {@link ImportChapterXX}, {@link ImportLinearizationsAndBiologicalSex}, etc.
 * 
 * @author csnyulas
 *
 */
public class ImportUtils {


	/**
	 * Please refer to the documentation of the {@link #openKb(URI)}
	 * 
	 * @param pprjFileUri the URI to a Protege project file
	 * @return an {@link OWLModel} instance to access the content of the ontology specified by pprjFileUri
	 */
	public static OWLModel openOWLModel(URI pprjFileUri) {
		Log.getLogger().info("\nOpening OWL model... ");
		KnowledgeBase kb = openKb(pprjFileUri);
		if (kb instanceof OWLModel) {
			return (OWLModel)kb;
		}
		else {
			Log.getLogger().log(Level.SEVERE, String.format(
					"Protege KB for %s is not of type OWLModel: %s", pprjFileUri, kb));
			return null;
		}
	}

	/**
	 * Opens a Protege project file and returns a Protege {@link KnowledgeBase} 
	 * to access its content. <br>
	 * <b>Note:</b> Since the ontology returned by this method is meant to be used 
	 * in ICD utility applications, this method also disables change tracking to improve performance.
	 * 
	 * @param pprjFileUri
	 * @return
	 */
	public static KnowledgeBase openKb(URI pprjFileUri) {
		Collection<String> errors = new ArrayList<String>();
		Project prj = Project.loadProjectFromURI(pprjFileUri, errors);
		prj.setChangeTrackingActive(false);
		return prj.getKnowledgeBase();
	}

}
