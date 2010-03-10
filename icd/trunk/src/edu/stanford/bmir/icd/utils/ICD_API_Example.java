package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICD_API_Example {

    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;

    public static void main(String[] args) {
        Project prj = Project.loadProjectFromFile("/work/protege/projects/icd/content_model/icd_int/icd_mysql/icd_umbrella.pprj", new ArrayList());
        owlModel = (OWLModel) prj.getKnowledgeBase();
        icdContentModel = new ICDContentModel(owlModel);

        //getICDcategories(); //takes around 90 secs to get the result back, that is why it is commented out
        getCategoryDetails();
        getChildren();
    }

    public static void getICDcategories() {
        long t0 = System.currentTimeMillis();
        Collection<RDFSNamedClass> icdCategories = icdContentModel.getICDCategories();
        System.out.println("ICD Categories count: " + icdCategories.size() + " in time: " + ((System.currentTimeMillis() - t0)/1000) + " sec");
    }

    public static void getCategoryDetails() {
        RDFSNamedClass category = icdContentModel.getICDCategory("http://who.int/icd#L89.2");
        System.out.println("Displayed as in the tree: " + category.getBrowserText());

        RDFResource defTerm = (RDFResource) category.getPropertyValue(icdContentModel.getDefinitionProperty());
        if (defTerm != null) {
            String definition = (String) defTerm.getPropertyValue(icdContentModel.getLabelProperty());
            System.out.println("Definition: " + definition);
        }
    }

    public static void getChildren() {
        RDFSNamedClass category = icdContentModel.getICDCategory("http://who.int/icd#I");
        System.out.println("Children of " + category.getBrowserText() + " : " + icdContentModel.getChildren(category));
    }



}
