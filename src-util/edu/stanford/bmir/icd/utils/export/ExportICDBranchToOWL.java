package edu.stanford.bmir.icd.utils.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.jena.creator.NewOwlProjectCreator;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ExportICDBranchToOWL {
	private final static Logger log = Log.getLogger(ExportICDBranchToOWL.class);
	
	private final static String TARGET_ONT_NAME = "http://who.int/icd_flattened/anatomy";
	private final static String TARGET_SYN_PROP = "http://who.int/icd_flattened/synonym";
	
    private static OWLModel sourceOwlModel;
    private static JenaOWLModel targetOwlModel;
    private static RDFSNamedClass sourceTopClass;
    private static RDFProperty targetSynProp;
    
    private static ICDContentModel cm;
   

    public static void main(String[] args) {
        if (args.length != 3) {
            log.severe("Needs 3 params: (1) ICD pprj or OWL file, "
            		+ "(2) top class to export, and "
            		+ "(3) Output OWL file");
            return;
        }

        String fileName = args[0];

        sourceOwlModel = openOWLFile(fileName);
        if (sourceOwlModel == null) {
        	log.severe("Could not open OWL file " + fileName);
        	System.exit(1);
        }
     
        sourceTopClass = sourceOwlModel.getRDFSNamedClass(args[1]);
        if (sourceTopClass == null) {
        	log.severe("Could not find top class " + args[1]);
        	System.exit(1);
        }
        
       targetOwlModel = createTargetOntology();
       if (targetOwlModel == null) {
    	   log.severe("Could not create target ontology");
    	    System.exit(1);
       }
       
       cm = new ICDContentModel(sourceOwlModel);
        
        SystemUtilities.logSystemInfo();
		log.info("\n===== Started export " + new Date());
		log.info("=== Ontology file: " + args[0]);
		log.info("=== Top class: " + args[1]);
		log.info("=== Output file: " + args[2]);
		
		try {
			exportBranch();
			saveTargetOntology(args[2]);
		}
		catch (Throwable t) {
			log.log(Level.SEVERE, t.getMessage(), t);
		}
		
		//finish processing
		log.info("\n===== End export at " + new Date());
    }
    
    
    private static void saveTargetOntology(String targetPath) throws Exception {
    	targetOwlModel.save(new File(targetPath).toURI());
	}


	private static OWLModel openOWLFile(String fileName) {
    	OWLModel owlModel = null;
    	
    	if (fileName.endsWith(".pprj")) { //pprj file
    		List errors = new ArrayList();
            Project prj = Project.loadProjectFromFile(fileName , errors);
            if (errors.size() > 0) {
                log.severe("There were errors at loading project: " + fileName);
                return null;
            }
            owlModel = (OWLModel) prj.getKnowledgeBase();
    	} else { //Assume OWL file
    		try {
				owlModel = ProtegeOWL.createJenaOWLModelFromURI(fileName);
			} catch (OntologyLoadException e) {
				log.log(Level.SEVERE, e.getMessage(),e);
			}
    	}
    	return owlModel;
    }
    
	private static JenaOWLModel createTargetOntology() {
		JenaOWLModel owlModel = null;
		try {
			Collection errors = new ArrayList();
			NewOwlProjectCreator creator = new NewOwlProjectCreator();
			creator.setOntologyName(TARGET_ONT_NAME);
			creator.create(errors);
			owlModel = creator.getOwlModel();
			
			targetSynProp = owlModel.createAnnotationProperty(TARGET_SYN_PROP);
		} catch (OntologyLoadException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return owlModel;
	}

	private static void exportBranch() {
		Collection<RDFSNamedClass> sourceClasses = getClassesToExport();
		sourceClasses = new ArrayList<RDFSNamedClass>(sourceClasses);
		sourceClasses.add(sourceTopClass);
		
		for (RDFSNamedClass sourceClass : sourceClasses) {
			exportClass(sourceClass);
		}
		
		addSuperClses(sourceClasses);
		targetOwlModel.getOWLNamedClass(sourceTopClass.getName()).addSuperclass(targetOwlModel.getOWLThingClass());
	}

	private static void addSuperClses(Collection<RDFSNamedClass> sourceClasses) {
		for (RDFSNamedClass sourceCls : sourceClasses) {
			Collection<RDFSNamedClass> sourceSuperClses = sourceCls.getSuperclasses(false);
			for (RDFSNamedClass sourceSuperCls : sourceSuperClses) {
				if (sourceSuperCls.getSuperclasses(true).contains(sourceTopClass) || sourceSuperCls.equals(sourceTopClass)) {
					try {
						OWLNamedClass targetCls = targetOwlModel.getOWLNamedClass(sourceCls.getName());
						OWLNamedClass targetSuperCls = targetOwlModel.getOWLNamedClass(sourceSuperCls.getName());
						targetCls.addSuperclass(targetSuperCls);
						targetCls.removeSuperclass(targetOwlModel.getOWLThingClass());
					} catch (Exception e) {
						log.log(Level.WARNING, "Error at adding superclasses for " + sourceCls.getName() + ", " + sourceCls.getBrowserText());
					}
				} 
			}
		}
	}


	private static void exportClass(RDFSNamedClass sourceOwlClass) {
		try {
			OWLNamedClass targetOWLCls = targetOwlModel.createOWLNamedClass(sourceOwlClass.getName());
			targetOWLCls.addPropertyValue(targetOwlModel.getRDFSLabelProperty(), cm.getTitleLabel(sourceOwlClass));
			createSyns(sourceOwlClass, targetOWLCls);
    	} 
    	catch (Exception e) {
			log.severe("Could not export: " + sourceOwlClass);
		}
	}

	
	private static void createSyns(RDFSNamedClass sourceOwlClass, OWLNamedClass targetOWLCls) {
		for (String syn : cm.getSynonymLabels(sourceOwlClass)) {
			targetOWLCls.addPropertyValue(targetSynProp, syn);
		}
	}


	private static Collection<RDFSNamedClass> getClassesToExport() {
		return sourceTopClass.getSubclasses(true);
	}

    
}
