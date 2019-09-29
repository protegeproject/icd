package edu.stanford.bmir.icd.utils.export;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.database.OWLDatabaseModel;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.jena.creator.JenaCreator;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.widget.ModalProgressBarManager;
import edu.stanford.smi.protegex.owl.writer.rdfxml.rdfwriter.OWLModelAllTripleStoresWriter;

public class ExportOWLModel {

	private static Logger log = Log.getLogger(ExportOWLModel.class);

	private static OWLModel owlModel;
	// private static ICDContentModel icdContentModel;

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: ExportOWLModel db_pprj_file_name exported_owl_filename [NATIVE|JENA]");
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
		String exportConfig = args[2];
		boolean isNativeExport = exportConfig == null || exportConfig.equalsIgnoreCase("NATIVE") ? true : false;

		log.info("Started the " + (isNativeExport ? "native" : "Jena") + " OWL export to " + fileURI + " on: "
				+ new Date());

		if (isNativeExport == true) {
			writeOWLModelNative(fileURI);
		} else {
			writeOWLModelJena(fileURI);
		}

		log.info("Ended export of OWL file: " + new Date());

		/*
		 * icdContentModel = new ICDContentModel(owlModel);
		 */
	}

	private static void writeOWLModelNative(URI fileURI) throws Exception {
		try {
			OWLModelAllTripleStoresWriter writer = new OWLModelAllTripleStoresWriter(owlModel, fileURI, true);
			writer.write();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Failed to save file: " + fileURI + ". Message: " + ex.getMessage(), ex);
			throw ex;
		}
	}

	private static void writeOWLModelJena(URI fileURI) {
		if (owlModel instanceof OWLDatabaseModel == false) {
			log.info("This is not an OWL database project. Abort");
			return;
		}
		
		 JenaCreator creator = new JenaCreator(owlModel, false, null, null);
		 OntModel newModel = creator.createOntModel();
		
		//OntModel newModel = ((OWLDatabaseModel) owlModel).getOntModel();
		OWLDatabaseModel dbModel = (OWLDatabaseModel) owlModel;
		String xmlBase = dbModel.getTripleStoreModel().getActiveTripleStore().getOriginalXMLBase();
		String defaultNS = dbModel.getNamespaceManager().getDefaultNamespace();
		if (xmlBase == null) {
			if (defaultNS != null && defaultNS.endsWith("#")) {
				xmlBase = defaultNS.substring(0, defaultNS.length() - 1);
			}
		}
		try {
			File file = new File(fileURI);
			// TT: writing to langXMl rather than langXMLAbbrev might be more efficient for
			// DB mode; to be checked
			JenaOWLModel.save(file, newModel, FileUtils.langXML, defaultNS, xmlBase);
		} catch (Throwable t) {
			Log.getLogger().log(Level.SEVERE, "Errors at exporting the OWL Database to OWL file", t);
		}
	}

}
