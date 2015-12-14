package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
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

/**
 * This script adds one linearization specification to all entities in ICD.
 * <p>
 * It is similar to based on {@link AddMultipleLinearizations} script, 
 * with the difference that it always adds the linearization specified in the argument
 * to all ICD categories, not only to a given branch/subset of categories.
 * <p>
 * If you need to add multiple linearizations to all ICD entities, you can either
 * call this script multiple times, or you can call the {@link AddMultipleLinearizations}
 * script with the top_category_class being "ICD Category".
 */

public class AddLinearization {
    private static Logger log = Log.getLogger(AddLinearization.class);

    private static ICDContentModel icdContentModel;
    private static OWLModel owlModel;

    private static RDFIndividual linearizationViewInst = null;
    private static OWLNamedClass linearizationSpecificationClass;
    private static RDFProperty linearizationProp;
    private static RDFProperty linearizationViewProp;


    public static void main(String[] args) {
        if (args.length != 2) {
            log.severe("Argument missing: pprj file name or linearization name");
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


        linearizationViewInst = owlModel.getRDFIndividual(args[1]);
        if (linearizationViewInst == null) {
            log.severe("Abort. Failed to find linearization view: " + linearizationViewInst);
            return;
        }

        init();

        long t0 = System.currentTimeMillis();
        log.info("Started getting ICD categories at: " + new Date());

        Collection<RDFSNamedClass> cats = icdContentModel.getICDCategories();

        log.info("Got " + cats.size() + " ICD categories at: " + new Date() + " in " + (System.currentTimeMillis() -  t0)/1000 + " secs.");

        cats.add(icdContentModel.getICDCategoryClass());

        addLinearization(cats);

        log.info("Finished at: " + new Date());
    }

    private static void init() {
        icdContentModel = new ICDContentModel(owlModel);

        linearizationSpecificationClass = (OWLNamedClass) icdContentModel.getLinearizationSpecificationClass();
        linearizationProp = icdContentModel.getLinearizationProperty();
        linearizationViewProp = icdContentModel.getLinearizationViewProperty();
    }

    private static void addLinearization(Collection<RDFSNamedClass> cats) {
        int i = 0;
        for (RDFSNamedClass cat : cats) {
            addLinearization(cat);

            i++;
            if (i % 1000 == 0) {
                log.info("Processed " + i + " categories at " + new Date());
            }
       }

    }

    private static void addLinearization(RDFSNamedClass cat) {
        try{
            RDFResource linSpec = linearizationSpecificationClass.createInstance(IDGenerator.getNextUniqueId());
            linSpec.setPropertyValue(linearizationViewProp, linearizationViewInst);
            cat.addPropertyValue(linearizationProp, linSpec);
        } catch (Exception e) {
            log.warning("------- Error at adding lin to " + cat);
        }

    }
}
