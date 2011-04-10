package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class AddCategoryLabelsForBioPortal {

    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;

    public static void main(String[] args) {
        Project prj = Project.loadProjectFromFile("/work/protege/projects/icd/content_model/icd_int/icd_mysql/icd_umbrella_merged_no_collab.pprj", new ArrayList());
        owlModel = (OWLModel) prj.getKnowledgeBase();
        icdContentModel = new ICDContentModel(owlModel);

        addICDCategoryLabels();
    }

    public static void addICDCategoryLabels() {
        long t0 = System.currentTimeMillis();

        RDFProperty labelProp = icdContentModel.getLabelProperty();
        RDFProperty rdfsLabelProp = icdContentModel.getLabelProperty();
        RDFProperty linearizationProp = icdContentModel.getLinearizationProperty();
        RDFProperty diagCriteriaProp = owlModel.getRDFProperty("http://who.int/icd#diagnosticCriteria");
        RDFProperty diagCriteriaTextProp = owlModel.getRDFProperty("http://who.int/icd#diagnosticCriteriaText");
        
        Collection<RDFSNamedClass> icdCategories = icdContentModel.getICDCategories();
        for (RDFSNamedClass icdCategory : icdCategories) {
        	//set icd:label of the category
            String browserText = getBrowserText(icdCategory);
            icdCategory.setPropertyValue(labelProp, browserText);
            
            //set rdfs:label for some of the property values
            // linearization specifications
            Collection<RDFResource> linearizations = icdCategory.getPropertyValues(linearizationProp);
            for (RDFResource linearization : linearizations) {
				linearization.setPropertyValue(rdfsLabelProp, linearization.getBrowserText());
			}
            // diagnostic criteria
            Collection<RDFResource> diagCriteriaInstances = icdCategory.getPropertyValues(diagCriteriaProp);
            for (RDFResource diagCriteria : diagCriteriaInstances) {
            	RDFSLiteral diagCriteriaText = diagCriteria.getPropertyValueLiteral(diagCriteriaTextProp);
            	String label = (diagCriteriaText == null ? "" : diagCriteriaText.getString());
				diagCriteria.setPropertyValue(labelProp, label);
			}

        }

        System.out.println("Labels updated for " + icdCategories.size() + " ICD Categories. Duration : " + ((System.currentTimeMillis() - t0)/1000) + " sec");
    }

    //method copied from OntologyServiceImpl - keep it in sync
    public static String getBrowserText(Frame frame) {
        String bt = frame.getBrowserText();
        if (bt.contains("'")) {
            //delete any leading and trailing 's if present
            bt = bt.replaceAll("^'|'$", "");
            //delete all 's preceding or following any of these characters: [SPACE].-_
            bt = bt.replaceAll("'([ .-_])|([ .-_])'", "$1$2");
        }
        return bt;
    }

}
