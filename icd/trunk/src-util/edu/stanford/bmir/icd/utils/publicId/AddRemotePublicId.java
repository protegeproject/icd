package edu.stanford.bmir.icd.utils.publicId;

import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class AddRemotePublicId {

    private static Logger log = Log.getLogger(AddRemotePublicId.class);

    public static void main(String[] args) {
        if (args.length != 4) {
            log.severe("Arguments missing: serrver name, user name, password, prj name");
            return;
        }

        Project prj = connectToRemoteProject(args);

        if (prj == null) {
            log.log(Level.SEVERE, "Cannot connect to remote project: " + args);
            return;
        }

        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            log.severe("Failed to load remote project: " + args);
            return;
        }

        ICDContentModel icdContentModel = new ICDContentModel(owlModel);
        RDFProperty publicIDProp = icdContentModel.getPublicIdProperty();

        if (publicIDProp == null) {
            log.severe("Missing publicIId prop. Exiiting");
            return;
        }

        log.info("Started remote job at:" + new Date());
        long t0 = System.currentTimeMillis();

        try{
            new AddPublicIdOnServer(owlModel).execute();
        } catch (Throwable t) {
            Log.getLogger().log(Level.WARNING, "Error at executing remote job", t);
        }

        log.info("Ended remote job at:" + new Date());
        log.info("Task took: " + (System.currentTimeMillis() - t0)/1000 +" secs.");

    }


    private static Project connectToRemoteProject(String[] args){
        Project prj = null;
        try {
            prj = RemoteProjectManager.getInstance().getProject(args[0], args[1], args[2], args[3], false);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot connect to remote project: " + args, e.getMessage());
            return null;
        }
        return prj;
    }


    static class AddPublicIdOnServer extends ProtegeJob {

        private static final long serialVersionUID = 7895759535282049363L;

        private static ICDContentModel icdContentModel;
        private static RDFProperty publicIDProp;

        public AddPublicIdOnServer(KnowledgeBase kb) {
            super(kb);
        }


        @Override
        public Object run() throws ProtegeException {
            icdContentModel = new ICDContentModel((OWLModel)getKnowledgeBase());
            publicIDProp = icdContentModel.getPublicIdProperty();

            addPublicId();
            return null;
        }


        private static void addPublicId() {
            Log.getLogger().info("Started get ICD cats at: " + new Date());
            Collection<RDFSNamedClass> cats = icdContentModel.getICDCategories();
            Log.getLogger().info("End get ICD cats at: " + new Date());

            int i = 0;
            int addedPublicIdCount = 0;

            for (RDFSNamedClass cat : cats) {
                if (addPublicIdToClass(cat) == true) {
                    addedPublicIdCount ++;
                }

                i++;

                if (i % 1000 == 0) {
                    Log.getLogger().info("Processed " + i + " classes on " + new Date());
                }
            }

            Log.getLogger().info("All done with " + i + " classes. Added the publicId to " + addedPublicIdCount + " classes. Done at " + new Date());
        }

        private static boolean addPublicIdToClass(RDFSNamedClass cat) {
            if (icdContentModel.getPublicId(cat) != null) {
                return false;
            }
            try {
                String publicId = ICDIDUtil.getPublicId(cat.getName());
                if (publicId == null) {
                    Log.getLogger().warning("Could not get public ID from ID server for class: " + cat.getName());
                } else {
                    cat.setPropertyValue(publicIDProp, publicId);
                    Log.getLogger().info("Adding public id to class: " + cat.getName() + " publicId: " + publicId);
                    return true;
                }
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Exception at adding public ID for class: " + cat, e);
            }
            return false;
        }

    }

}
