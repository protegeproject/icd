package edu.stanford.bmir.icd.utils.export;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.writer.rdfxml.rdfwriter.OWLModelAllTripleStoresWriter;

public class ExportOWLModel {

	private static Logger log = Log.getLogger(ExportOWLModel.class);

	private static OWLModel owlModel;
	// private static ICDContentModel icdContentModel;

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage: ExportOWLModel db_pprj_file_name exported_owl_filename");
			return;
		}

		Collection errors = new ArrayList();
		Project prj = Project.loadProjectFromFile(args[0], errors);

		if (errors != null) {
			ProjectManager.getProjectManager().displayErrors("Errors", errors);
		}

		owlModel = (OWLModel) prj.getKnowledgeBase();

		if (owlModel == null) {
			System.out.println("Failed to get the ontology: " + args[0]);
			log.log(Level.SEVERE, "Failed to get the ontology: " + args[0]);
			return;
		}

		URI fileURI = (new File(args[1])).toURI();
		log.info("Started the OWL export to " + fileURI + " on: " + new Date());
		
		writeOWLModel(fileURI);
		
		log.info("Ended export of OWL file: " + new Date());

		/*
		 * icdContentModel = new ICDContentModel(owlModel);
		 */
	}

	private static void writeOWLModel(URI fileURI) throws Exception {
		try {
			OWLModelAllTripleStoresWriter writer = new OWLModelAllTripleStoresWriter(owlModel, fileURI, true);
			writer.write();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Failed to save file: " +fileURI + ". Message: "+ ex.getMessage(), ex);
			throw ex;
		}

	}

}
