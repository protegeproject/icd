package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class MoveSynonymsToBaseIndex {
    //file = "/work/protege/projects/icd/content_model/icd_int/icd_mysql/icd_umbrella.pprj"

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Needs 1 params: ICD pprj file");
            return;
        }

        String fileName = args[0];

        Project prj = Project.loadProjectFromFile(fileName , new ArrayList());

        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();
        ICDContentModel cm = new ICDContentModel(owlModel);

        long t0 = System.currentTimeMillis();
        Collection<RDFSNamedClass> cats = cm.getICDCategories();
        Log.getLogger().info("Time to retrieve categories: " + (System.currentTimeMillis() - t0)/1000 + " sec");

        int i = 0;

        for (RDFSNamedClass cat : cats) {
            Collection<RDFResource> synTerms = cat.getPropertyValues(cm.getSynonymProperty());
            if (synTerms != null && synTerms.size() > 0) {
                for (RDFResource synTerm : synTerms) {
                    cat.addPropertyValue(cm.getBaseIndexProperty(), synTerm);
                    cat.removePropertyValue(cm.getSynonymProperty(), synTerm);
                    synTerm.setPropertyValue(cm.getIndexTypeProperty(), cm.getIndexTypeSynonymInst());
                }
               i++;
                if (i % 200 == 0) {
                    Log.getLogger().info("Moved " + i + " categories.");
                }
            }
        }

    }
}
