package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICD_API_Example {

    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;

    public static void main(String[] args) {
        if (args.length != 1) {
           System.out.println("Argument missing: pprj file name");
            return;
        }

        Collection errors = new ArrayList();
        Project prj = Project.loadProjectFromFile(args[0], errors);

        if (errors != null) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }

        owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            System.out.println("Failed to load the ICD project from this location: " + args[0]);
            return;
        }

        owlModel = (OWLModel) prj.getKnowledgeBase();
        icdContentModel = new ICDContentModel(owlModel);

        //getICDcategories(); //takes around 90 secs to get the result back, that is why it is commented out
        getCategoryDetails();
        getChildren();
        getClamlRef();
        getLinearizationInfo();
        getDisplayStatusAndTagResponsability();
        getPublicId();
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

    public static void getResidualGenerationInfo() {
    	RDFSNamedClass category = icdContentModel.getICDCategory("http://who.int/icd#I");
        System.out.println("\n" + category.getBrowserText());
        
        Boolean suppOtherSpecResGeneration = (Boolean) category.getPropertyValue(icdContentModel.getSuppressOtherSpecifiedResidualsProperty());
        System.out.println("suppress 'Other Specified' residual generation: " + (suppOtherSpecResGeneration == null ? "(not set)" : suppOtherSpecResGeneration));
        
        Boolean suppUnspecResGeneration = (Boolean) category.getPropertyValue(icdContentModel.getSuppressUnspecifiedResidualsProperty());
        System.out.println("suppress 'Other Specified' residual generation: " + (suppUnspecResGeneration == null ? "(not set)" : suppUnspecResGeneration));
        
        RDFResource otherSpecResTitle = (RDFResource) category.getPropertyValue(icdContentModel.getOtherSpecifiedResidualTitleProperty());
        System.out.println("'Other Specified' residual title: " + (otherSpecResTitle == null ? "(not set)" : otherSpecResTitle.getPropertyValue(icdContentModel.getLabelProperty())));
        
        RDFResource unspecResTitle = (RDFResource) category.getPropertyValue(icdContentModel.getUnspecifiedResidualTitleProperty());
        System.out.println("'Unspecified' residual title: " + (unspecResTitle == null ? "(not set)" : unspecResTitle.getPropertyValue(icdContentModel.getLabelProperty())));
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

    public static void getPublicId() {
        String icatId = "http://who.int/icd#V";
        RDFSNamedClass category = icdContentModel.getICDCategory(icatId);
        String publicId1 = icdContentModel.getPublicId(category);
        System.out.println("Public ID of " + category.getBrowserText() + " (iCAT id=" + category.getName() + ") --- retrieved by category reference --- is: " + publicId1);

        String publicId2 = icdContentModel.getPublicId(icatId);
        System.out.println("Public ID of iCAT category with id=" + icatId + " --- retrieved by category iCAT ID --- is: " + publicId2);

       String publicId = "http://id.who.int/icd/entity/334423054";
       RDFSNamedClass icdCategory = icdContentModel.getICDCategoryByPublicId(publicId); //This is a slow method
       System.out.println("Get the ICD category with public ID: " + publicId +". Result: " + icdCategory.getBrowserText() + ", iCAT ID:" + icdCategory.getName());
    }


}
