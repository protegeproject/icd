package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class FixDuplicateLinearizations {
    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;
	private static RDFSNamedClass linearizationMetaClass;//???

    private static RDFProperty linearizationViewProp;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: FixDuplicateLinearizations pprj_file_name [top_class_name]+");
            return;
        }

        Collection<?> errors = new ArrayList<Object>();
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
        
        linearizationMetaClass = icdContentModel.getLinearizationMetaClass();
        linearizationViewProp = icdContentModel.getLinearizationViewProperty();

        RDFSNamedClass topClass = icdContentModel.getICDCategoryClass();
        if (args.length > 1) {
        	for (int i = 1; i < args.length; i++) {
	        	topClass = owlModel.getOWLNamedClass(args[i]);
	            
	            if (topClass == null) {
	            	System.out.println("ERROR: Could not find top class: " + args[i]);
	            	continue;
	            }
	            
	            fixLinearizations(topClass);
        	}
        }
        else {
        	fixLinearizations(topClass);
        }
    }
    
    private static void fixLinearizations(RDFSNamedClass topClass) {
        long t0 = System.currentTimeMillis();

        Log.getLogger().setLevel(Level.FINE);

        owlModel.setGenerateEventsEnabled(false);

        int classCount = 1;
        int fixedClassCount = 0;
        if (fixLinearization(topClass)) {
        	fixedClassCount++;
        }
        
        Collection<?> subclses = topClass.getSubclasses(true);
        for (Object subcls :subclses) {
        	classCount++;
            if (subcls instanceof RDFSNamedClass) {
                if (fixLinearization((RDFSNamedClass)subcls)) {
                	fixedClassCount++;
                }
            }
        }
        Log.getLogger().info("" + fixedClassCount + " classes out of " + classCount + " had been fixed in the " + topClass + " class hierarchy.");

        System.out.println("Time: " + (System.currentTimeMillis() - t0) /1000 + " sec");
    }


    private static boolean fixLinearization(RDFSNamedClass c) {
    	boolean changed = false;

    	if ( c.getRDFTypes().contains(linearizationMetaClass) ) {
            ArrayList<RDFResource> linViews = new ArrayList<RDFResource>();
            
            Collection<RDFResource> linearizationSpecs = icdContentModel.getLinearizationSpecifications(c);
            
            for (RDFResource linSpec : linearizationSpecs) {
            	RDFResource linView = (RDFResource) linSpec.getPropertyValue(linearizationViewProp);
            	if (linViews.contains(linView)) {
            		linSpec.delete();
                    if (Log.getLogger().isLoggable(Level.FINER)) {
                        Log.getLogger().finer("Removed " + linView.getBrowserText() + " linearization " + 
                        		linSpec.getBrowserText() + " from " + c.getBrowserText());
                    }
                    changed = true;
            	}
            	else {
            		linViews.add(linView);
            	}
     		}
    	}
    	return changed;
    }

}
