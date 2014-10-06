package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class FixMetaClasses {
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
            System.out.println("Failed");
            return;
        }
        icdContentModel = new ICDContentModel(owlModel);

        fixMetaclasses();
    }

    private static void fixMetaclasses() {
        long t0 = System.currentTimeMillis();

        owlModel.setGenerateEventsEnabled(false);
        RDFSNamedClass icdCatCls = icdContentModel.getICDCategoryClass();

        RDFSNamedClass extCauseCls = icdContentModel.getExternalCausesTopClass();

        Collection<RDFSNamedClass> catMetaclasses = icdContentModel.getRegularDiseaseMetaclasses();

        Collection<RDFSNamedClass> topClses = new ArrayList(icdCatCls.getDirectSubclasses());
        topClses.remove(extCauseCls);

        //fix regular disease
        for (RDFSNamedClass topCls : topClses) {
            fixMetaclses(topCls, catMetaclasses);
            Collection<Cls> subclses = topCls.getSubclasses();
            for (Cls subcls :subclses) {
                if (subcls instanceof RDFSNamedClass) {
                    fixMetaclses(subcls, catMetaclasses);
                }
            }
        }

        //fix external causes
        Collection<RDFSNamedClass> extCauseMetaclses = icdContentModel.getExternalCauseMetaclasses();
        Collection<RDFSNamedClass> extClses = extCauseCls.getSubclasses();
        for (RDFSNamedClass cls : extClses) {
            if (cls instanceof RDFSNamedClass) {
                fixMetaclses(cls, extCauseMetaclses);
            }
        }

        System.out.println("Time: " + (System.currentTimeMillis() - t0) /1000 + " sec");
    }

    private static void fixMetaclses(Cls c, Collection<RDFSNamedClass> metaclasses) {
        for (RDFSNamedClass metacls : metaclasses) {
            if (!c.hasType(metacls)) {
                c.addDirectType(metacls);
            }
        }
        Collection<Cls> extraMetaclases = new ArrayList<Cls>(c.getDirectTypes());
        extraMetaclases.removeAll(metaclasses);

        for (Cls metacls : extraMetaclases) {
            c.removeDirectType(metacls);
        }
    }


}
