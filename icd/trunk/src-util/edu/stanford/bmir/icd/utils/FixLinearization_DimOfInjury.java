package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * Based on AddLinerization.java
 */

public class FixLinearization_DimOfInjury {
    private static Logger log = Log.getLogger(FixLinearization_DimOfInjury.class);
    
    private static String DIM_Of_INJ = "http://who.int/icd#3210_81655b5c_debe_4590_b8ad_ea6448685723";
    private static String MMS_LIN_VIEW = "http://who.int/icd#Morbidity";
   
    private static ICDContentModel icdContentModel;
    private static OWLModel owlModel;

    
    private static OWLNamedClass dimOfInjCls;
    private static RDFResource mmsLinView;
    
    private static RDFProperty linearizationProp;
    private static RDFProperty linearizationViewProp;
    private static RDFProperty isGroupingProp;
    private static RDFProperty isAuxProp;
    private static RDFProperty isIncludedProp;


    public static void main(String[] args) {
/*       
  		if (args.length != 1) {
            log.severe("Argument missing: pprj file name");
            return;
        }
*/
        
        if (args.length != 4) {
            log.severe("Arguments missing: server name, user name, password, prj name");
            return;
        }

        Collection errors = new ArrayList();
        //Project prj = Project.loadProjectFromFile(args[0], errors);
        
        Project prj = connectToRemoteProject(args);

        if (prj == null) {
            log.log(Level.SEVERE, "Cannot connect to remote project: " + args[0] +" " + args[1] + " " + args[2] + " " + args[3]);
            return;
        }

        if (errors != null) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }

        owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            log.severe("Abort. Failed to load pprj file");
            return;
        }

        init();

        long t0 = System.currentTimeMillis();
        log.info("Started getting classes at: " + new Date());

        Collection<RDFSNamedClass> cats = new ArrayList<RDFSNamedClass>();
        cats.add(dimOfInjCls);
        cats.addAll(dimOfInjCls.getSubclasses(true));

        log.info("Got " + cats.size() + " classes at: " + new Date() + " in " + (System.currentTimeMillis() -  t0)/1000 + " secs.");

        owlModel.setGenerateEventsEnabled(false);
        
        fixLinearization(cats);

        owlModel.setGenerateEventsEnabled(true);
        
        log.info("Finished at: " + new Date());
    }
    
    private static Project connectToRemoteProject(String[] args){
        Project prj = null;
        try {
            prj = RemoteProjectManager.getInstance().getProject(args[0], args[1], args[2], args[3], false);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot connect to remote project: " + args, e.getMessage());
            return null;
        }
        return prj;
    }

    private static void init() {
        icdContentModel = new ICDContentModel(owlModel);

        dimOfInjCls = owlModel.getOWLNamedClass(DIM_Of_INJ);
        mmsLinView = owlModel.getOWLIndividual(MMS_LIN_VIEW);
   
        linearizationProp = icdContentModel.getLinearizationProperty();
        linearizationViewProp = icdContentModel.getLinearizationViewProperty();
        isGroupingProp = icdContentModel.getIsGroupingProperty();
        isAuxProp = icdContentModel.getIsAuxiliaryAxisChildProperty();
        isIncludedProp = icdContentModel.getIsIncludedInLinearizationProperty();
    }

    private static void fixLinearization(Collection<RDFSNamedClass> cats) {
        int i = 0;
        for (RDFSNamedClass cat : cats) {
            fixLinearization(cat);

            i++;
            if (i % 1000 == 0) {
                log.info("Processed " + i + " classes at " + new Date());
            }
       }

    }

    private static void fixLinearization(RDFSNamedClass cls) {
        try{
            
        	List<RDFResource> lins = (List<RDFResource>) cls.getPropertyValues(linearizationProp);
        	
        	Set<RDFResource> seenLins = new HashSet<RDFResource>();
        	
        	for (RDFResource lin : lins) {
				RDFResource linView = (RDFResource) lin.getPropertyValue(linearizationViewProp);
				
				if (seenLins.contains(linView) == true) {
					log.warning("Delete for cls\t" + cls.getBrowserText() + "\t" + ((RDFResource)lin.getPropertyValue(linearizationViewProp)).getBrowserText() +
							"\t" + lin.getPropertyValue(isIncludedProp) + 
							"\t" + lin.getPropertyValue(isGroupingProp) +
							"\t" + lin.getPropertyValue(isAuxProp)
							);
					
					lin.delete();
					
				} else { //first time we see it
					log.warning("Keep for cls\t" + cls.getBrowserText() + "\t" + ((RDFResource)lin.getPropertyValue(linearizationViewProp)).getBrowserText() +
							"\t" + lin.getPropertyValue(isIncludedProp) + 
							"\t" + lin.getPropertyValue(isGroupingProp) +
							"\t" + lin.getPropertyValue(isAuxProp)
							);
					
					if (linView.equals(mmsLinView) == false) {
						lin.setPropertyValue(isIncludedProp, null);
						lin.setPropertyValue(isGroupingProp, null);
						lin.setPropertyValue(isAuxProp, null);
					}
				}
				
				seenLins.add(linView);
        	}
        	
        } catch (Exception e) {
            log.severe("------- Error at processing  " + cls);
        }

    }
}
