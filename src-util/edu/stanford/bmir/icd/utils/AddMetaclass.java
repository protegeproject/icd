package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class AddMetaclass {
    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;
    private static RDFSNamedClass metaclass;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: AddMetaclass pprj_file_name metaclass_name [top_class_name]+");
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
        
        metaclass = (RDFSNamedClass) owlModel.getCls(args[1]);
        
        if (metaclass == null) {
        	System.out.println("Could not find metaclass: " + args[1]);
        	return;
        }
        
        icdContentModel = new ICDContentModel(owlModel);

        RDFSNamedClass topClass = icdContentModel.getICDCategoryClass();
        if (args.length > 2) {
        	for (int i = 2; i < args.length; i++) {
	        	topClass = (RDFSNamedClass) owlModel.getCls(args[i]);
	            
	            if (topClass == null) {
	            	System.out.println("ERROR: Could not find top class: " + args[i]);
	            	continue;
	            }
	            
	            addMetaclass(topClass);
        	}
        }
        else {
        	addMetaclass(topClass);
        }
    }

    private static void addMetaclass(RDFSNamedClass topClass) {

    	Log.getLogger().info("Started to retrieve disease classes at: " + new Date());
    	long t0 = System.currentTimeMillis();

        owlModel.setGenerateEventsEnabled(false);
        
        Collection<RDFSNamedClass> clses = new ArrayList<RDFSNamedClass>(topClass.getSubclasses(true));
        clses.add(topClass);
        
        long t1 = System.currentTimeMillis();
		Log.getLogger().info("Retrieved " + clses.size() + " classes in " + (t1 - t0)/1000 + " secs. Time: " + new Date());
        
        int i = 0;

        for (RDFSNamedClass cls : clses) {
            i++;
            if (i % 500 == 0) {
                Log.getLogger().info("Added metaclass to: " + i + " classes. Time: " + new Date());
            }
            fixMetacls(cls);
        }

        Log.getLogger().info("Time: " + (System.currentTimeMillis() - t1) /1000 + " sec");
        owlModel.setGenerateEventsEnabled(true);
    }

    private static void fixMetacls(Cls c) {
        if (!c.hasDirectType(metaclass)) {
            c.addDirectType(metaclass);
        }
    }

}
