package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.IcdIdGenerator;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * This script adds one or more postcoordination specifications to all entities in any given branch of ICD.
 *
 */

public class AddMultiplePostCoordinationSpecs {

	private static Logger log = Log.getLogger(AddMultiplePostCoordinationSpecs.class);

    private static ICDContentModel cm;
    private static OWLModel owlModel;

	private static RDFSNamedClass topCategory;
    private static List<RDFIndividual> linearizationViewInstances = new ArrayList<RDFIndividual>();
  

    public static void main(String[] args) {
    	int nonOptArgCount = countNonOptionArguments(args);
        if (nonOptArgCount < 3) {
            log.severe("Argument missing: pprj file name, category name or linearization name");
            return;
        }

        Collection errors = new ArrayList();
        Project prj = Project.loadProjectFromFile(getNonOptionArgument(args, 0), errors);

        if (errors != null) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }

        owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            log.severe("Abort. Failed to load pprj file");
            return;
        }

        cm = new ICDContentModel(owlModel);

        topCategory = cm.getICDCategory(getNonOptionArgument(args, 1));
        if (topCategory == null) {
            log.severe("Abort. Failed to find category: " + topCategory);
            return;
        }
        
        for (int i = 2; i < nonOptArgCount; i++) {
	        String linViewArg = getNonOptionArgument(args, i);
			RDFIndividual linearizationViewInst = owlModel.getRDFIndividual(linViewArg);
	        if (linearizationViewInst == null) {
	            log.severe("Abort. Failed to find linearization view: " + linViewArg);
	            return;
	        }
	        linearizationViewInstances.add(linearizationViewInst);
        }

        long t0 = System.currentTimeMillis();
        log.info("Started getting ICD classes at: " + new Date());

        Collection<RDFSNamedClass> clses = cm.getRDFSNamedClassCollection(topCategory.getSubclasses(true));

        log.info("Got " + clses.size() + " ICD classes at: " + new Date() + " in " + (System.currentTimeMillis() -  t0)/1000 + " secs.");

        clses.add(topCategory);

        addPCSpecs(clses);
        
        log.info("Saving ontology..");
        
        prj.save(new ArrayList<>());

        log.info("Finished at: " + new Date());
        
    }

	
    private static int countNonOptionArguments(String[] args) {
		int nonOptArgCount = 0;
		for (String arg : args) {
			if ( (! arg.isEmpty()) && (! isOptionArgument(arg))) {
				nonOptArgCount++;
			}
		}
		return nonOptArgCount;
	}

	private static boolean isOptionArgument(String arg) {
		return arg.startsWith("-");
	}


	private static String getNonOptionArgument(String[] args, int index) {
		int currNonOptArgIndex = -1;
		for (int i = 0; i < args.length; i++) {
			if ( (! args[i].isEmpty()) &&  (! isOptionArgument(args[i])) ) {
				currNonOptArgIndex++;
				if (currNonOptArgIndex == index) {
					return args[i];
				}
			}
		}
		throw new IndexOutOfBoundsException("Could not find non-option argument of index " + index);
	}
	

    private static void addPCSpecs(Collection<RDFSNamedClass> clses) {
    	 int i = 0;
         for (RDFSClass cls : clses) {
         	if (cls instanceof RDFSNamedClass) {
         		addPCSpecs((RDFSNamedClass) cls);
         	}

             i++;
             if (i % 1000 == 0) {
                 log.info("Processed " + i + " classes");
             }
        }
    }

    private static void addPCSpecs(RDFSNamedClass cls) {
    	
     	Map<RDFResource, RDFResource> lin2PcSpec = new HashMap<RDFResource, RDFResource>();
    	for (RDFResource pcSpec : cm.getAllowedPostcoordinationSpecifications(cls)) {
    		lin2PcSpec.put((RDFResource)pcSpec.getPropertyValue(cm.getLinearizationViewProperty()), pcSpec);
		}
    	
    	for (RDFIndividual lin : linearizationViewInstances) {
			if (lin2PcSpec.get(lin) != null) { //a PC spec for this lin exists already
				continue;
			}
			
			RDFResource pcSpec = cm.getPostcoordinationAxesSpecificationClass().createRDFIndividual(IcdIdGenerator.getNextUniqueId(owlModel));
			pcSpec.setPropertyValue(cm.getLinearizationViewProperty(), lin);
			
			cls.addPropertyValue(cm.getAllowedPostcoordinationAxesProperty(), pcSpec);
		}
    	
    }
}
