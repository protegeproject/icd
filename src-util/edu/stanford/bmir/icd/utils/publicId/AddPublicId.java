package edu.stanford.bmir.icd.utils.publicId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class AddPublicId {

    private static Logger log = Log.getLogger(AddPublicId.class);

    private static ICDContentModel icdContentModel;
    private static OWLModel owlModel;
    private static RDFProperty publicIDProp;


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
            log.severe("Failed");
            return;
        }
        icdContentModel = new ICDContentModel(owlModel);
        publicIDProp = icdContentModel.getPublicIdProperty();

        if (publicIDProp == null) {
            log.severe("Missing publicIId prop. Exiiting");
            return;
        }

        Collection<RDFSNamedClass> cats = icdContentModel.getICDCategories();
        System.out.println(cats.size());
        //addPublicId();
    }


    private static void addPublicId() {
        log.info("Started get ICD cats at: " + new Date());
        Collection<RDFSNamedClass> cats = icdContentModel.getICDCategories();
        log.info("End get ICD cats at: " + new Date());

        int i = 0;

        for (RDFSNamedClass cat : cats) {
            try {
                String publicId = ICDIDUtil.getPublicId(cat.getName());
                if (publicId == null) {
                    Log.getLogger().warning("Could not get public ID for newly created class: " + cat.getName());
                } else {
                    cat.setPropertyValue(publicIDProp, publicId);

                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Exception at adding public ID for class: " + cat, e);
            }

            i++;

            if (i % 1000 == 0) {
                log.info("Processed " + i + " classes on " + new Date());
            }

        }

        log.info("All done with " + i + " classes at " + new Date());
    }


}
