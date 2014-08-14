package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class AddPostcoordinationLinearizations {
    private static Logger log = Log.getLogger(AddPostcoordinationLinearizations.class);

    private static ICDContentModel icdContentModel;
    private static OWLModel owlModel;

    private static OWLNamedClass postCoordAxesSpecCls;
    private static RDFProperty allowedPostCoordAxesProp;
    private static RDFProperty linearizationViewProp;
    
    private static List<RDFIndividual> postCoordinationLinearizationViews;


    public static void main(String[] args) {
        if (args.length != 1) {
            log.severe("Argument missing: pprj file name");
            return;
        }

        Collection errors = new ArrayList();
        Project prj = Project.loadProjectFromFile(args[0], errors);

        if (errors != null) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }

        owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            log.severe("Abort. Failed to load pprj file");
            return;
        }


        init();

        long t0 = System.currentTimeMillis();
        log.info("Started getting ICD categories at: " + new Date());

        Collection<RDFSNamedClass> cats = icdContentModel.getICDCategories();

        log.info("Got " + cats.size() + " ICD categories at: " + new Date() + " in " + (System.currentTimeMillis() -  t0)/1000 + " secs.");

        cats.add(icdContentModel.getICDCategoryClass());

        addLinearizations(cats);

        log.info("Finished at: " + new Date());
    }

    private static void init() {
        icdContentModel = new ICDContentModel(owlModel);

        postCoordAxesSpecCls = (OWLNamedClass) icdContentModel.getPostcoordinationAxesSpecificationClass();
        allowedPostCoordAxesProp = icdContentModel.getAllowedPostcoordinationAxesProperty();
        linearizationViewProp = icdContentModel.getLinearizationViewProperty();
        
        initPostcoordinationLinearizations();
    }
    
    private static void initPostcoordinationLinearizations() {
    	RDFSNamedClass linViewCls = owlModel.getRDFSNamedClass("http://who.int/icd#ICD11LinearizationView");
    	List<RDFIndividual> views = new ArrayList<RDFIndividual>(linViewCls.getDirectInstances());
    	views.add(owlModel.getRDFIndividual("http://who.int/icd#FoundationComponent"));
    	views.remove(owlModel.getRDFIndividual("http://who.int/icd#Mortality"));
    	views.remove(owlModel.getRDFIndividual("http://who.int/icd#PrimaryCare"));
    	views.remove(owlModel.getRDFIndividual("http://who.int/icd#Primary_Care_Low_RS"));
      	views.remove(owlModel.getRDFIndividual("http://who.int/icd#Primary_Care_High_RS"));
    }

    private static void addLinearizations(Collection<RDFSNamedClass> cats) {
        int i = 0;
        for (RDFSNamedClass cat : cats) {
            addLinearizations(cat);

            i++;
            if (i % 1000 == 0) {
                log.info("Processed " + i + " categories at " + new Date());
            }
       }

    }
    
    private static void addLinearizations(RDFSNamedClass cat) {
    	try {
    		for (RDFIndividual linView : postCoordinationLinearizationViews) {
				addLinearization(cat, linView);
			}
    	} catch (Exception e) {
            log.warning("------- Error at adding linearizations to " + cat);
        }
    }

    private static void addLinearization(RDFSNamedClass cat, RDFIndividual linearizationViewInst ) {
        try{
            RDFResource linSpec = postCoordAxesSpecCls.createInstance(IDGenerator.getNextUniqueId());
            linSpec.setPropertyValue(linearizationViewProp, linearizationViewInst);
            cat.addPropertyValue(allowedPostCoordAxesProp, linSpec);
        } catch (Exception e) {
            log.warning("------- Error at adding linearization " + linearizationViewInst.getName() + " to " + cat);
        }

    }
}
