package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class FixICHILogicalDefinitions {
	
    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;
    
    private static RDFSNamedClass ichiHealthIntervCls;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: FixICHILogicalDefinitions pprj_file_name");
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
        
        ichiHealthIntervCls = (RDFSNamedClass) owlModel.getRDFSNamedClass(ICDContentModelConstants.HEALTH_INTERVENTION_CLASS);

        icdContentModel = new ICDContentModel(owlModel);


        fixLogDefs(ichiHealthIntervCls);
    }

    private static void fixLogDefs(RDFSNamedClass topClass) {

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
            if (i % 1000 == 0) {
                Log.getLogger().info("Fixed log defs for " + i + " classes. Time: " + new Date());
            }
            fixLogDef(cls);
        }

        Log.getLogger().info("Time: " + (System.currentTimeMillis() - t1) /1000 + " sec");
        owlModel.setGenerateEventsEnabled(true);
    }

    private static void fixLogDef(RDFSNamedClass c) {
    	Collection<?> supers = c.getSuperclasses(false);
    	Collection<?> equivalentClasses = c.getEquivalentClasses();
       
        boolean isHealthIntSuper = false;
        boolean isAnotherSuper = false;
        
        for (Object s : supers) {
			if (s instanceof OWLNamedClass) {
				if (s.equals(ichiHealthIntervCls)) {
					isHealthIntSuper = true;
				}
				else {
					isAnotherSuper = true;
				}
			}
		}
        
        for (Object s : supers) {
        	if (equivalentClasses.contains(s)) {
        		continue;
        	}
        	if (s instanceof OWLIntersectionClass) {
        		OWLIntersectionClass sInt = (OWLIntersectionClass)s;
        		Collection<RDFSClass> fillers = sInt.getOperands();
        		OWLNamedClass superClass = null;
        	    OWLIntersectionClass copyRestr = owlModel.createOWLIntersectionClass();
        	    
        	    for (RDFSClass f : fillers) {
        	    	if (f instanceof OWLNamedClass) {
        	    		superClass = (OWLNamedClass) f;
        	    	}
        	    	else {
        	    		OWLSomeValuesFrom filler = (OWLSomeValuesFrom)f;
        	            OWLSomeValuesFrom clone = owlModel.createOWLSomeValuesFrom(filler.getOnProperty(), filler.getSomeValuesFrom());
        	            copyRestr.addOperand(clone);
        	    	}
        	    }
        	    if (superClass != null) {
        	    	Log.getLogger().info( c.toString() + ". Superclass: " + s.toString() + ". Fillers: " + fillers.toString());
        	    	Log.getLogger().info("     REPLACING " + sInt.getBrowserText() + " WITH:   " + superClass.getBrowserText() + "  AND " + copyRestr.getBrowserText());
        	        if (isAnotherSuper == false) {
//        	          c.addSuperclass(superClass);
//        	          c.addSuperclass(copyRestr);
//        	          c.removeSuperclass(sInt);
        	        }
        	    }

        	}
        }
    }

}
