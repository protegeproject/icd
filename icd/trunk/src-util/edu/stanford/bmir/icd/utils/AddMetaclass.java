package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class AddMetaclass {
    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;
    private static RDFSNamedClass causalMechMetaclass;

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

        addMetaclass();

        prj.save(errors);
        if (errors != null) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }

    }

    private static void addMetaclass() {
        long t0 = System.currentTimeMillis();

        owlModel.setGenerateEventsEnabled(false);
        RDFSNamedClass ictmCatCls = owlModel.getRDFSNamedClass("http://who.int/ictm#ICTMCategory");

        causalMechMetaclass = owlModel.getRDFSNamedClass("http://who.int/icd#CausalMechanismAndRiskFactorsSection");

        Collection<RDFSNamedClass> clses = ictmCatCls.getSubclasses(true);

        int i = 0;

        for (RDFSNamedClass cls : clses) {
            i++;
            if (i % 500 == 0) {
                Log.getLogger().info("Fixed: " + i + " classes. Time: " + new Date());
            }
            fixMetacls(cls);
        }

        System.out.println("Time: " + (System.currentTimeMillis() - t0) /1000 + " sec");
        owlModel.setGenerateEventsEnabled(true);
    }

    private static void fixMetacls(Cls c) {
        if (!c.hasDirectType(causalMechMetaclass)) {
            c.addDirectType(causalMechMetaclass);
        }
    }

}
