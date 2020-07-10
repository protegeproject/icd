package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.IcdIdGenerator;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class AddAllPostcoordinationSpecs {
    private static Logger log = Log.getLogger(AddAllPostcoordinationSpecs.class);

    private static ICDContentModel cm;
    private static OWLModel owlModel;


    public static void main(String[] args) {
        if (args.length != 2) {
            log.severe("Arguments missing: (1) pprj file name, (2) top level cls");
            return;
        }

        Project prj = Project.loadProjectFromFile(args[0], new ArrayList<>());

        owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            log.severe("Abort. Failed to load pprj file");
            return;
        }

        cm = new ICDContentModel(owlModel);

        RDFSNamedClass topCls = owlModel.getRDFSNamedClass(args[1]);
        if (topCls == null) {
        	log.severe("Could not find top cls: " + args[1]);
        	return;
        }
        
        long t0 = System.currentTimeMillis();
        log.info("Started getting subclasses of " + args[1] + " at: " + new Date());

        Collection<RDFSClass> clses = topCls.getSubclasses(true);

        log.info("Got " + clses.size() + " subclasses at: " + new Date() + " in " + (System.currentTimeMillis() -  t0)/1000 + " secs.");

        clses.add(topCls);

        addPCSpecs(clses);

        log.info("Saving..");
        
        prj.save(new ArrayList<>());
        
        log.info("Finished at: " + new Date());
    }


  
    private static void addPCSpecs(Collection<RDFSClass> cats) {
        int i = 0;
        for (RDFSClass cat : cats) {
        	if (cat instanceof RDFSNamedClass) {
        		addPCSpecs((RDFSNamedClass) cat);
        	}

            i++;
            if (i % 1000 == 0) {
                log.info("Processed " + i + " categories at " + new Date());
            }
       }

    }

    private static void addPCSpecs(RDFSNamedClass cls) {
    	
    	Collection<RDFResource> lins = new ArrayList<RDFResource>();
    	for (RDFResource linSpec : (Collection<RDFResource>) cls.getPropertyValues(cm.getLinearizationProperty())) {
    		RDFResource linView = (RDFResource) linSpec.getPropertyValue(cm.getLinearizationViewProperty());
    		lins.add(linView);
    	}
    	
      	Map<RDFResource, RDFResource> lin2PcSpec = new HashMap<RDFResource, RDFResource>();
    	for (RDFResource pcSpec : cm.getAllowedPostcoordinationSpecifications(cls)) {
    		lin2PcSpec.put((RDFResource)pcSpec.getPropertyValue(cm.getLinearizationViewProperty()), pcSpec);
		}
    	
    	for (RDFResource lin : lins) {
			if (lin2PcSpec.get(lin) != null) { //a PC spec for this lin exists already
				continue;
			}
			
			RDFResource pcSpec = cm.getPostcoordinationAxesSpecificationClass().createRDFIndividual(IcdIdGenerator.getNextUniqueId(owlModel));
			pcSpec.setPropertyValue(cm.getLinearizationViewProperty(), lin);
			
			cls.addPropertyValue(cm.getAllowedPostcoordinationAxesProperty(), pcSpec);
		}
    	
    }

  
}
