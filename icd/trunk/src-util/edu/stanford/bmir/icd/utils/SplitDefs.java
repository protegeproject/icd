package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class SplitDefs {
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

        int i = 0;

        long t0 = System.currentTimeMillis();
        Collection<RDFSNamedClass> cats = cm.getICDCategories();
        Log.getLogger().info("Time to retrieve categories: " + (System.currentTimeMillis() - t0)/1000 + " sec");

        for (RDFSNamedClass cat : cats) {
            RDFResource defTerm = (RDFResource) cat.getPropertyValue(cm.getDefinitionProperty());
            if (defTerm != null) {
                String def  = (String) defTerm.getPropertyValue(cm.getLabelProperty());
                if (def != null) {
                   String[] words = def.split("\\ ");
                   if (words.length > 100) {
                       cat.addPropertyValue(cm.getLongDefinitionProperty(), defTerm);
                       cat.removePropertyValue(cm.getDefinitionProperty(), defTerm);
                       Log.getLogger().info((i++) + ". Def word count: " + words.length + " Moved def to long def " + cat);
                   }
                }
            }
        }

    }
}
