package edu.stanford.bmir.icd.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class FixLinearizations {
    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;
	private static RDFSNamedClass linearizationMetaClass;

    private static OWLNamedClass linearizationSpecificationClass;
    private static RDFProperty linearizationViewProp;
    private static RDFProperty linProp;
    
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

        linearizationMetaClass = icdContentModel.getLinearizationMetaClass();
        linearizationSpecificationClass = (OWLNamedClass) icdContentModel.getLinearizationSpecificationClass();
        linearizationViewProp = icdContentModel.getLinearizationViewProperty();
        linProp = icdContentModel.getLinearizationProperty();

        fixLinearizations();
    }

    private static void fixLinearizations() {
        long t0 = System.currentTimeMillis();

        owlModel.setGenerateEventsEnabled(false);
        RDFSNamedClass icdCatCls = icdContentModel.getICDCategoryClass();

        Collection<RDFResource> linearizationViewInstances = icdContentModel.getLinearizationValueSet();
        Collection<RDFResource> missingIcdCategoryLinSpecs = getMissingLinearizations(icdCatCls, linearizationViewInstances);
        
        //checking whether whether changes are necessary or not
        boolean proceed = true;
        if (missingIcdCategoryLinSpecs.isEmpty()) {
        	System.out.println("There is no linarization view missing from 'ICD Categories', " +
        			"which means that probably there is no point in running this time-consuming script!");
        	try {
        		System.out.print("Do you wish to run the script on all the ICD categories, nevertheless? [Y/N] ");
				int ch = System.in.read();
				if (ch != 'Y' && ch != 'y') {
					proceed = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        else {
        	System.out.println("Adding the following linearizations:");
        	for (RDFResource linView : missingIcdCategoryLinSpecs) {
				System.out.println("    " + linView.getBrowserText());
			}
        }

        //applying the fix
        if (proceed) {
	        fixLinearization(icdCatCls, linearizationViewInstances);
	        Collection<RDFSNamedClass> subclses = icdCatCls.getSubclasses(true);
	        for (RDFSNamedClass subcls :subclses) {
	            if (subcls instanceof RDFSNamedClass) {
	                fixLinearization(subcls, linearizationViewInstances);
	            }
	        }
	        Log.getLogger().info("Done");
        }
        else {
        	System.out.println("There was nothing to be done");
        	Log.getLogger().info("Script aborted on user request");
        }
        
        System.out.println("Time: " + (System.currentTimeMillis() - t0) /1000 + " sec");
    }

    
    private static Collection<RDFResource> getMissingLinearizations(RDFSNamedClass c, Collection<RDFResource> linViewInstances) {
    	ArrayList<RDFResource> res = new ArrayList<RDFResource>();
    	
    	if ( c.getRDFTypes().contains(linearizationMetaClass) ) {
    		res.addAll(linViewInstances);
    		
            Collection<RDFResource> linearizationSpecs = icdContentModel.getLinearizationSpecifications(c);

            for (RDFResource icdCatLinSpec : linearizationSpecs) {
            	Object linView = icdCatLinSpec.getPropertyValue(linearizationViewProp);
    			boolean found = res.remove(linView);
    			if (!found) {
    				Log.getLogger().log(Level.WARNING, "The linearization view referred by the linearization spec." + icdCatLinSpec + 
    						" at class " + c + " could not be removed from the list of available LinearizationView instances");
    			}
    		}
    	}
    	
        return res;
    }
    
    
    private static void fixLinearization(RDFSNamedClass c, Collection<RDFResource> linViewInstances) {
        Collection<RDFResource> missingLinSpecs = getMissingLinearizations(c, linViewInstances);

    	for (RDFResource linViewInstance : missingLinSpecs) {
            RDFResource linSpec = linearizationSpecificationClass.createInstance(IDGenerator.getNextUniqueId());
            linSpec.setPropertyValue(linearizationViewProp, linViewInstance);

            c.addPropertyValue(linProp, linSpec);
            Log.getLogger().info("Added " + linViewInstance.getBrowserText() + " to " + c.getBrowserText());
		}
    }


}
