package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class AddSnomedLabel {

    public static final String BP_URL = "http://bioportal.bioontology.org/visualize/40403/";
    public static final String SN_NS = "http://www.ihtsdo.org/#";
    public static final String SN_PREFIX = "SCTID_";
    public static final String SN_NAME = "SNOMED CT";

    public static void main(String[] args) {
        Collection errors = new ArrayList();

        Project snomedPrj = Project.loadProjectFromFile(
                "/work/protege/projects/snomed/2009.07.31/snomed_on_localhost.pprj", errors);
        Project snomedMappingPrj = Project.loadProjectFromFile(
                "/work/protege/projects/icd/content_model/icd_int/snomed_mappings.pprj", errors);

        if (errors.size() > 0) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }

        OWLModel snomedKb = (OWLModel) snomedPrj.getKnowledgeBase();
        OWLModel snomedMappingKb = (OWLModel) snomedMappingPrj.getKnowledgeBase();

        RDFProperty bpShortTermProp = snomedMappingKb.getRDFProperty(ICDContentModelConstants.BP_SHORT_TERM_ID_PROP);
        RDFProperty bpOntologyLabelProp = snomedMappingKb
                .getRDFProperty(ICDContentModelConstants.BP_ONTOLOGY_LABEL_PROP);
        RDFProperty bpOntologyIdProp = snomedMappingKb.getRDFProperty(ICDContentModelConstants.BP_ONTOLOGY_ID_PROP);
        RDFProperty urlProp = snomedMappingKb.getRDFProperty(ICDContentModelConstants.URL_PROP);
        RDFProperty ontologyIdProp = snomedMappingKb.getRDFProperty(ICDContentModelConstants.ONTOLOGYID_PROP);
        RDFProperty termIdProp = snomedMappingKb.getRDFProperty(ICDContentModelConstants.TERM_ID_PROP);
        RDFProperty labelProp = snomedMappingKb.getRDFProperty(ICDContentModelConstants.LABEL_PROP);

        RDFSNamedClass refTermCls = snomedMappingKb.getRDFSNamedClass("http://who.int/icd#ReferenceTerm");
        Collection<RDFResource> refTerms = refTermCls.getInstances(true);

        for (RDFResource refTerm : refTerms) {
            String termId = (String) refTerm.getPropertyValue(termIdProp);
            if (termId != null && refTerm.getPropertyValue(labelProp) == null) {
                try {
                    RDFSNamedClass snomedCls = snomedKb.getRDFSNamedClass(SN_NS + SN_PREFIX + termId);
                    if (snomedCls != null) {
                        RDFSLiteral snoLabelLit = (RDFSLiteral) snomedCls.getLabels().iterator().next();
                        String snoLabel = snoLabelLit.getString();
                        refTerm.setPropertyValue(labelProp, snoLabel);
                        refTerm.setPropertyValue(urlProp, BP_URL + termId);

                        refTerm.setPropertyValue(bpOntologyIdProp, "40403");
                        refTerm.setPropertyValue(bpOntologyLabelProp, SN_NAME);
                        refTerm.setPropertyValue(ontologyIdProp, SN_NAME);
                        refTerm.setPropertyValue(bpShortTermProp, termId);

                    }
                } catch (Exception e) {
                    Log.getLogger().log(Level.WARNING,
                            "Error at setting label for " + termId + ". Message: " + e.getMessage());
                }
            }
        }

        snomedMappingPrj.save(errors);

        if (errors.size() > 0) {
            ProjectManager.getProjectManager().displayErrors("Errors at save", errors);
        }

    }
}
