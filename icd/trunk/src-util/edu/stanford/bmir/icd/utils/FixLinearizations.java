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
    private static RDFProperty linearizationProp;
    private static RDFProperty linearizationViewProp;
    private static RDFProperty linearizationParentProp;
    
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
        linearizationProp = icdContentModel.getLinearizationProperty();
        linearizationViewProp = icdContentModel.getLinearizationViewProperty();
        linearizationParentProp = icdContentModel.getLinearizationParentProperty();

        fixLinearizations();
    }

    private static void fixLinearizations() {
        long t0 = System.currentTimeMillis();

        owlModel.setGenerateEventsEnabled(false);
        RDFSNamedClass icdCatCls = icdContentModel.getICDCategoryClass();

        Collection<RDFResource> linearizationViewInstances = icdContentModel.getLinearizationValueSet();
        Collection<RDFResource> missingIcdCategoryLinViews = removeLinearizationParentsAndGetMissingLinearizations(icdCatCls, linearizationViewInstances);
        
        //checking whether whether changes are necessary or not
        boolean proceed = true;
        if (missingIcdCategoryLinViews.isEmpty()) {
        	System.out.println("There is no linarization view missing from 'ICD Categories', " +
        			"which means that probably there is no point in running this time-consuming script!");
        	try {
        		System.out.println("Do you wish to run the script on all the ICD categories, nevertheless? [Y/N] ");
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
        	for (RDFResource linView : missingIcdCategoryLinViews) {
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
    
    
    private static void fixLinearization(RDFSNamedClass c, Collection<RDFResource> linViewInstances) {
        Collection<RDFResource> missingLinSpecs = removeLinearizationParentsAndGetMissingLinearizations(c, linViewInstances);

    	for (RDFResource linViewInstance : missingLinSpecs) {
            RDFResource linSpec = linearizationSpecificationClass.createInstance(IDGenerator.getNextUniqueId());
            linSpec.setPropertyValue(linearizationViewProp, linViewInstance);

            c.addPropertyValue(linearizationProp, linSpec);
            Log.getLogger().fine("Added " + linViewInstance.getBrowserText() + " to " + c.getBrowserText());
		}
    }

    
    private static Collection<RDFResource> removeLinearizationParentsAndGetMissingLinearizations(RDFSNamedClass c, Collection<RDFResource> linViewInstances) {
    	ArrayList<RDFResource> res = new ArrayList<RDFResource>();
    	
    	if ( c.getRDFTypes().contains(linearizationMetaClass) ) {
    		res.addAll(linViewInstances);
    		
            Collection<RDFResource> linearizationSpecs = icdContentModel.getLinearizationSpecifications(c);
            
            RDFSNamedClass singleParent = getSingleParent(c);

            for (RDFResource linSpec : linearizationSpecs) {
            	Object linView = linSpec.getPropertyValue(linearizationViewProp);
            	//remove linearization parent if necessary
            	if (singleParent != null) {
            		Object linParent = linSpec.getPropertyValue(linearizationParentProp);
            		if (singleParent.equals(linParent)) {
            			linSpec.removePropertyValue(linearizationParentProp, linParent);
            		}
            		else {
            			//if we have a linearization parent that is not a direct superclass
            			if (linParent != null && !c.getSuperclasses(false).contains(linParent)) {
            				if (c.getSuperclasses(true).contains(linParent)) {
            					Log.getLogger().log(Level.INFO, "POSSIBLE ERROR IN THE MODEL: The linearization parent of " + c +
            							" for linearization " + linView + " does not refer to a parent, but to a higher order ancestor: " + linParent);
            				}
            				else {
            					Log.getLogger().log(Level.WARNING, "ERROR IN THE MODEL: The linearization parent of " + c +
            							" for linearization " + linView + " does not refer to an ancestor (superclass), but to:" + linParent);
            				}
            			}
            		}
            	}
            	//remove this linearization view from the result
    			boolean found = res.remove(linView);
    			if (!found) {
    				Log.getLogger().log(Level.WARNING, "The linearization view " + linView + " referred by the linearization spec." + linSpec + 
    						" at class " + c + " could not be removed from the list of available LinearizationView instances");
    			}
    		}
    	}
    	
        return res;
    }

    
	/**
	 * Checks whether the class <code>c</code> has exactly one superclass, and in case
	 * it does it returns that superclass. If the class has more than one superclass
	 * the method returns null.
	 * 
	 * @param c a class
	 * @return
	 */
	private static RDFSNamedClass getSingleParent(RDFSNamedClass c) {
		RDFSNamedClass singleParent = null;
		Collection<?> superclasses = c.getSuperclasses(false);
		if (superclasses != null && superclasses.size() > 0) {
			boolean lookingForFirst = true;
			for (Object superclass : superclasses) {
				if (superclass instanceof RDFSNamedClass) {
					if (lookingForFirst) {
						//found the first valid parent
						singleParent = (RDFSNamedClass) superclass;
					}
					else {
						//this is one of the multiple parents: reset singleParent to null
						singleParent = null;
					}
					lookingForFirst = false;
				}
			}
		}
		return singleParent;
	}


}
