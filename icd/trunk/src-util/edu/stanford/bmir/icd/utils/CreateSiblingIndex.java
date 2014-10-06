package edu.stanford.bmir.icd.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class CreateSiblingIndex {

    private static OWLModel owlModel;
    private static ICDContentModel cm;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Needs 1 params: ICD pprj file");
            return;
        }

        String fileName = args[0];

        List errors = new ArrayList();
        Project prj = Project.loadProjectFromFile(fileName , errors);
        if (errors.size() > 0) {
            System.out.println("There were errors at loading project: " + fileName);
            System.exit(1);
        }

        owlModel = (OWLModel) prj.getKnowledgeBase();
        cm = new ICDContentModel(owlModel);

        createSiblingIndex();

    }

    private static void createSiblingIndex() {
        Collection<RDFSNamedClass> cats = cm.getICDCategories();
        cats.add(cm.getICDCategoryClass());

        Log.getLogger().info("Got: " + cats.size() + " categories. Date: " + new Date());


        int i = 0;
        for (RDFSNamedClass cat : cats) {
            if (cat.getSubclassCount() > 0) {
                createSiblingIndex(cat);
            }
            if (i % 1000 == 0) {
                Log.getLogger().info("Processed " + i + " cats. Date: " + new Date());
            }
            i ++;
        }

    }

    private static void createSiblingIndex(RDFSNamedClass cat) {
        cm.checkIndexAndRecreate(cat, true);
    }
}
