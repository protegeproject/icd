package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        getClamlRef();
        getLinearizationInfo();
        getDisplayStatusAndTagResponsability();
    }

    public static void getICDcategories() {
        long t0 = System.currentTimeMillis();
        Collection<RDFSNamedClass> icdCategories = icdContentModel.getICDCategories();
        System.out.println("ICD Categories count: " + icdCategories.size() + " in time: " + ((System.currentTimeMillis() - t0)/1000) + " sec");
    }

    public static void getCategoryDetails() {
        RDFSNamedClass category = icdContentModel.getICDCategory("http://who.int/icd#L89.2");
        System.out.println("Displayed as in the tree: " + category.getBrowserText());

        String sortingLabel = (String) category.getPropertyValue(icdContentModel.getSortingLabelProperty());
        System.out.println("Sorting label: " + sortingLabel);

        RDFResource defTerm = icdContentModel.getTerm(category, icdContentModel.getDefinitionProperty());
        if (defTerm != null) {
            String definition = (String) defTerm.getPropertyValue(icdContentModel.getLabelProperty());
            String rubricId = (String) defTerm.getPropertyValue(icdContentModel.getIdProperty());

            System.out.println("Definition rubric id: " + rubricId);
            System.out.println("Definition: " + definition);
        }

        Collection<RDFResource> prefilledDefsTerm = icdContentModel.getTerms(category, icdContentModel.getPrefilledDefinitionProperty());
        System.out.println("\nPrefilled defintion terms: " + prefilledDefsTerm);
    }

    public static void getChildren() {
        RDFSNamedClass category = icdContentModel.getICDCategory("http://who.int/icd#I");
        System.out.println("Children of " + category.getBrowserText() + " : " + icdContentModel.getChildren(category));
    }

    public static void getClamlRef() {
        RDFSNamedClass category = icdContentModel.getICDCategory("http://who.int/icd#A65-A69");
        System.out.println("\n" + category.getBrowserText());
        Collection<RDFResource> exclusionTerms = category.getPropertyValues(icdContentModel.getExclusionProperty());
        for (RDFResource exclusionTerm : exclusionTerms) {
            System.out.println("\tExclusion: " + exclusionTerm.getBrowserText());
            Collection<RDFResource> clamlRefs = exclusionTerm.getPropertyValues(icdContentModel.getClamlReferencesProperty());
            if (clamlRefs != null) {
                for (RDFResource clamlRef : clamlRefs) {
                    System.out.println("\t\tClaml Reference: " + clamlRef.getBrowserText());
                    System.out.println("\t\tClaml text: " + clamlRef.getPropertyValue(icdContentModel.getTextProperty()));
                    System.out.println("\t\tClaml usage: " + clamlRef.getPropertyValue(icdContentModel.getUsageProperty()));
                    System.out.println("\t\tClaml category ref in ICD: " + ((RDFResource)clamlRef.getPropertyValue(icdContentModel.getIcdRefProperty())).getBrowserText());
                }
            }
        }
    }

    public static void getLinearizationInfo() {
        RDFSNamedClass category = icdContentModel.getICDCategory("http://who.int/icd#A65-A69");
        System.out.println("\n" + category.getBrowserText());

       Collection<RDFResource> linearizationSpecs = icdContentModel.getLinearizationSpecifications(category);
       for (RDFResource linearizationSpec : linearizationSpecs) {
           RDFResource linearization = (RDFResource) linearizationSpec.getPropertyValue(icdContentModel.getLinearizationViewProperty());
           RDFSNamedClass linearizationParent = (RDFSNamedClass) linearizationSpec.getPropertyValue(icdContentModel.getLinearizationParentProperty());
           Boolean isIncludedInLinearization = (Boolean) linearizationSpec.getPropertyValue(icdContentModel.getIsIncludedInLinearizationProperty());
           String linSortingLabel = (String) linearizationSpec.getPropertyValue(icdContentModel.getLinearizationSortingLabelProperty());

           System.out.println("Linearization: " + linearization.getBrowserText() +
                   "; is included: " + (isIncludedInLinearization == null ? "(not specified)" : isIncludedInLinearization) +
                   "; linearization parent: " +( linearizationParent == null ? "none" : linearizationParent.getBrowserText()) +
                   "; linearization sorting label: " + (linSortingLabel == null ? "(not specified)" : linSortingLabel));
       }
    }

    public static void getDisplayStatusAndTagResponsability() {
        RDFSNamedClass category = icdContentModel.getICDCategory("http://who.int/icd#I");
        System.out.println("\n Category: " + category.getBrowserText());

        RDFResource status = icdContentModel.getDisplayStatus(category);
        System.out.println("Status: " + (status == null ? "none" : status.getBrowserText()));

        RDFResource primaryTAG = icdContentModel.getAssignedPrimaryTag(category);
        System.out.println("Primary TAG: " + (primaryTAG == null ? "none" : primaryTAG.getBrowserText()));

        Collection<RDFResource> localSecondaryTAGs = icdContentModel.getAssignedSecondaryTags(category);
        System.out.println("Assigned local secondary TAGs at this category (does not include inherited TAGs): " + localSecondaryTAGs);

        Map<RDFResource, List<RDFSNamedClass>> involvedTAGs = icdContentModel.getInvolvedTags(category);
        System.out.println("Involved TAGs: ");
        for (RDFResource tag : involvedTAGs.keySet()) {
            System.out.println("TAG: " + tag.getBrowserText() + " ---  inherited from: " + involvedTAGs.get(tag));
        }
    }


}
