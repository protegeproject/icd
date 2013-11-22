package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class SimplifyICFForBioPortal {

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

        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            System.out.println("Failed");
            return;
        }

        simplify(owlModel);

        prj.save(new ArrayList());
    }

    private static void simplify(OWLModel owlModel) {
       OWLNamedClass icfCat = owlModel.getOWLNamedClass("http://who.int/icf#ICFCategory");
       convertTermsToLabels(icfCat);
       removeTypes(icfCat);

       OWLNamedClass qualifiersCls = owlModel.getOWLNamedClass("http://who.int/icf#ICFQualifier");
       convertTermsToLabels(qualifiersCls);
       removeTypes(qualifiersCls);
    }


    private static void convertTermsToLabels(OWLNamedClass parentCls) {
        Collection<RDFSNamedClass> subclasses = new ArrayList<RDFSNamedClass>(parentCls.getSubclasses(true));
        subclasses.add(parentCls);

        for (RDFSNamedClass cls : subclasses) {
           // convertTerm(cls, "http://who.int/icd#icdTitle", "http://www.w3.org/2004/02/skos/core#definition");
            convertTerm(cls, "http://who.int/icd#definition", "http://www.w3.org/2004/02/skos/core#definition");
            convertTerm(cls, "http://who.int/icd#inclusion", "http://who.int/icd#inclusion1");
            convertTerm(cls, "http://who.int/icd#exclusion", "http://who.int/icd#exclusion1");
        }

    }


    private static void convertTerm(RDFSNamedClass cls, String oldPropName, String newPropName) {
        OWLModel owlModel = cls.getOWLModel();
        RDFProperty oldProp = owlModel.getRDFProperty(oldPropName);
        RDFResource valueInst = (RDFResource) cls.getPropertyValue(oldProp);

        if (valueInst != null) {
            String value = (String) valueInst.getPropertyValue(owlModel.getRDFSLabelProperty()) ;

            RDFProperty newProp = owlModel.getRDFProperty(newPropName);
            cls.setPropertyValue(newProp, value);
        }

        cls.removePropertyValue(oldProp, valueInst);
    }


    private static void removeTypes(OWLNamedClass qualifiersCls) {
        Collection<RDFSNamedClass> subclasses = new ArrayList<RDFSNamedClass>(qualifiersCls.getSubclasses(true));
        subclasses.add(qualifiersCls);

        RDFSNamedClass owlClass = qualifiersCls.getOWLModel().getSystemFrames().getOwlNamedClassClass();

        for (RDFSNamedClass cls : subclasses) {
            cls.setRDFType(owlClass);
        }

    }

}
