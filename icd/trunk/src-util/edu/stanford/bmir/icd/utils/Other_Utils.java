package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

public class Other_Utils {
    private static OWLModel owlModel;

    public static void main(String[] args) {
        //Project prj = Project.loadProjectFromFile("/work/protege/projects/icd/ICECI/iceci_vOriginal_Source_1.2_OWL_version_1.01.pprj", new ArrayList());

        Project prj = Project.loadProjectFromFile("/work/protege/projects/icd/content_model/icd_int/2010.07.21 icd11 merged for bp/icd11alpha_db.pprj", new ArrayList());

        owlModel = (OWLModel)prj.getKnowledgeBase();

       // fixClassesRDFSLabel();
       // fixTermsRDFSLabel();
       // fixLinSpecLabel();
        deleteEmptyTerms();
    }


	private static void fixClassesRDFSLabel() {
        RDFProperty code = owlModel.getRDFProperty("http://who.int/iceci#code");
        RDFProperty prefName = owlModel.getRDFProperty("http://who.int/iceci#preferredName");

        Collection<OWLNamedClass> clses = owlModel.getUserDefinedOWLNamedClasses();

        for (OWLNamedClass cls : clses) {
            String label = cls.getPropertyValue(code) + " " + cls.getPropertyValue(prefName);
            label.replace("\"", "");

            cls.setPropertyValue(owlModel.getRDFSLabelProperty(), label);
        }

        owlModel.getProject().save(new ArrayList());
    }

    private static void fixTermsRDFSLabel() {
    	OWLNamedClass linSpecClass = owlModel.getOWLNamedClass("http://who.int/icd#LinearizationSpecification");
    	OWLNamedClass termClass = owlModel.getOWLNamedClass("http://who.int/icd#Term");
    	RDFProperty linViewProp = owlModel.getRDFProperty("http://who.int/icd#linearizationView");
    	RDFProperty isIncludedProp = owlModel.getRDFProperty("http://who.int/icd#isIncludedInLinearization");

    	Collection<RDFResource> termInstances = termClass.getInstances(true);

    	for (RDFResource inst : termInstances) {
			if (inst.hasDirectType(linSpecClass)) {
				fixBrowserTextForLinInst(inst, linViewProp, isIncludedProp);
			} else {
				Collection labels = inst.getLabels();
				if (labels == null || labels.size() == 0) {
					inst.addLabel(StringUtilities.removeAllQuotes(inst.getBrowserText()), null);
				}
			}
		}
	}


	private static void fixLinSpecLabel() {
		OWLNamedClass linSpecClass = owlModel.getOWLNamedClass("http://who.int/icd#LinearizationSpecification");
		RDFProperty linViewProp = owlModel.getRDFProperty("http://who.int/icd#linearizationView");
    	RDFProperty isIncludedProp = owlModel.getRDFProperty("http://who.int/icd#isIncludedInLinearization");

    	Collection<RDFResource> termInstances = linSpecClass.getInstances(true);

    	for (RDFResource inst : termInstances) {
    		fixBrowserTextForLinInst(inst, linViewProp, isIncludedProp);
    	}

	}

    private static void fixBrowserTextForLinInst(RDFResource inst, RDFProperty linViewProp, RDFProperty isIncludedProp) {
    	Collection labels = inst.getLabels();
		if (labels != null && labels.size() > 0) { return; }

		RDFResource view = (RDFResource) inst.getPropertyValue(linViewProp);
		if (view == null) {
			Log.getLogger().warning("Malformed LinearizationSpecification. No view : " + inst);
			return;
		}
		String viewtext = StringUtilities.removeAllQuotes(view.getBrowserText());
		Boolean isIncluded = (Boolean) inst.getPropertyValue(isIncludedProp);

		String label = "Included in " + viewtext + ": " + (isIncluded == null ? "not specified" : isIncluded.booleanValue());

		inst.addLabel(label, null);
    }


	private static void deleteEmptyTerms() {
    	OWLNamedClass termClass = owlModel.getOWLNamedClass("http://who.int/icd#Term");
    	RDFProperty labelProp = owlModel.getRDFProperty("http://who.int/icd#label");

    	Collection<RDFResource> termInstances = termClass.getInstances(true);

    	for (RDFResource inst : termInstances) {
			Object value = inst.getPropertyValue(labelProp);
			if (value == null) {
				inst.delete();
			}
		}

	}


}
