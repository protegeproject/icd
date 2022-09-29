package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.IcdIdGenerator;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * This script adds one or more linearizations to all entities in any given branch of ICD.
 * For example, it can be used to support the generation of linearization specific X-Chapter, 
 * by adding all the possible linearization specifications to all entities under 
 * the X-Chapter branch.
 * <p>
 * It is based on {@link AddLinearization} script, but it extends it by taking more arguments,
 * namely the name of the top level class of the branch where the new linearizations should be 
 * added (the {@link AddLinearization} script assumed that it is "ICD Categories"), and possibly
 * more than just one linearization view name, in case there is more than one linearization 
 * is to be added to these classes.
 * <p>
 * If a linearization specification for one of the linearization views already exists, it will
 * reuse it and set its properties as defined in the properties files. So, it is safe to run the
 * script multiple times with the same settings.
 * 
 * @author csnyulas
 *
 */

public class AddMultipleLinearizations {
	private static Logger log = Log.getLogger(AddMultipleLinearizations.class);
	
	private static final String OPTION_PART_OF = "-partOf";
	private static final String OPTION_NOT_PART_OF = "-notPartOf";
	private static final String OPTION_GROUPING = "-gr";
	private static final String OPTION_NO_GROUPING = "-nogr";
	private static final String OPTION_AUX_AX_CHILD = "-auxAxChild";
	private static final String OPTION_NOT_AUX_AX_CHILD = "-notAuxAxChild";
	
	//these are different types of options 
	
	private static final String OPTION_OVERRIDE_EXISTING_FLAGS = "-overrideExistingFlags";
	private static final String OPTION_NOT_OVERRIDE_EXISTING_FLAGS = "-notOverrideExistingFlags";
	//If this option is set, then it will overide all the other options.
	private static final String OPTION_COPY_MMS_FLAGS = "-copyMMSFlags";
	// This option does not make any sense, but keep it for the consistency sake. 
	// Ralph Waldo Emerson: “A foolish consistency is the hobgoblin of little minds, 
	// adored by little statesmen and philosophers and divines."
	private static final String OPTION_NOT_COPY_MMS_FLAGS = "-notCopyMMSFlags"; 
	
	private static final String MMS_LIN_VIEW = "http://who.int/icd#Morbidity";

    private static ICDContentModel cm;
    private static OWLModel owlModel;

	private static RDFSNamedClass topCls;
    private static List<RDFIndividual> linearizationViewInstances = new ArrayList<RDFIndividual>();
  
    private static Boolean isPartOf;
    private static Boolean isGrouping;
    private static Boolean isAuxiliaryAxisChild;
    
    private static boolean isOverrideExistingFlags = true; //by default override existing flags
    private static boolean isCopyMMSFlags = false;
    
    private static OWLNamedClass linearizationSpecificationClass;
    
    private static RDFProperty linearizationProp;
    private static RDFProperty linearizationViewProp;
    private static RDFProperty isIncludedInLinearizationProp;
    private static RDFProperty isGroupingProp;
    private static RDFProperty isAuxiliaryAxisChildProp;
    
    private static RDFResource mmsLinViewInstance;


    public static void main(String[] args) {
    	int nonOptArgCount = countNonOptionArguments(args);
        if (nonOptArgCount < 3) {
            log.severe("Argument missing: (1) pprj_file_name (2) top_cls (3) linearization_name(s)");
            usage();
            return;
        }

        isPartOf = getAlternativeOptionValue(args, OPTION_PART_OF, OPTION_NOT_PART_OF);
		isGrouping = getAlternativeOptionValue(args, OPTION_GROUPING, OPTION_NO_GROUPING);
		isAuxiliaryAxisChild = getAlternativeOptionValue(args, OPTION_AUX_AX_CHILD, OPTION_NOT_AUX_AX_CHILD);
		
		Boolean b = getAlternativeOptionValue(args, OPTION_OVERRIDE_EXISTING_FLAGS, OPTION_NOT_OVERRIDE_EXISTING_FLAGS);
		isOverrideExistingFlags = (b == null || b == true) ? true : false;
		
		b = getAlternativeOptionValue(args, OPTION_COPY_MMS_FLAGS, OPTION_NOT_COPY_MMS_FLAGS);
		isCopyMMSFlags = (b != null && b == true);
		
		printOpts();
		
        Collection errors = new ArrayList();
        Project prj = Project.loadProjectFromFile(getNonOptionArgument(args, 0), errors);

        if (errors != null) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }

        owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            log.severe("Abort. Failed to load pprj file");
            return;
        }

        init();

        topCls = cm.getICDCategory(getNonOptionArgument(args, 1));
        if (topCls == null) {
            log.severe("Abort. Failed to find top class: " + topCls);
            return;
        }
        
        for (int i = 2; i < nonOptArgCount; i++) {
	        String linViewArg = getNonOptionArgument(args, i);
			RDFIndividual linearizationViewInst = owlModel.getRDFIndividual(linViewArg);
	        if (linearizationViewInst == null) {
	            log.severe("Abort. Failed to find linearization view: " + linViewArg);
	            return;
	        }
	        linearizationViewInstances.add(linearizationViewInst);
        }

        mmsLinViewInstance = owlModel.getRDFIndividual(MMS_LIN_VIEW);
        
        long t0 = System.currentTimeMillis();
        log.info("Started getting ICD classes at: " + new Date());

        Collection<RDFSNamedClass> cats = cm.getRDFSNamedClassCollection(topCls.getSubclasses(true));
        cats.add(topCls); //adding also the top class

        log.info("Got " + cats.size() + " ICD classes at: " + new Date() + " in " + (System.currentTimeMillis() -  t0)/1000 + " secs.");

        cats.add(topCls);

        addLinearization(cats);
        
        log.info("Saving ontology..");
        
        prj.save(new ArrayList<>());

        log.info("Finished at: " + new Date());
        
    }

	private static void usage() {
		Log.getLogger().info(
				"Usage: AddMultipleLinearizations [-partOf|-notPartOf] [-gr|-nogr] "
				+ "[-overideExistingFlags|-notOverideExistingFlags] "
				+ "[-copyMMSFlags|-notCopyMMSFlags] "
				+ "pprj_file_name top_category linearization_view_1 [linearization_view_2 ...]");
	}
	
	private static void printOpts() {
		log.info("===== Options =====");
		log.info("Option: isPartOf = " + isPartOf);
		log.info("Option: isGrouping = " + isGrouping);
		log.info("Option: isAuxiliaryAxisChild = " + isAuxiliaryAxisChild);
		
		log.info("Option: isOverideExistingFlags = " + isOverrideExistingFlags);
		log.info("Option: isCopyMMSFlags = " + isCopyMMSFlags);
		log.info("====================");
	}
	
    private static int countNonOptionArguments(String[] args) {
		int nonOptArgCount = 0;
		for (String arg : args) {
			if ( (! arg.isEmpty()) && (! isOptionArgument(arg))) {
				nonOptArgCount++;
			}
		}
		return nonOptArgCount;
	}

	private static boolean isOptionArgument(String arg) {
		return arg.startsWith("-");
	}

	private static Boolean getAlternativeOptionValue(String[] args, String trueOptionArg, String falseOptionArg) {
		Boolean res = null;
		List<String> argList = Arrays.asList(args);
		if (argList.contains(trueOptionArg)) {
			if (argList.contains(falseOptionArg)) {
				Log.getLogger().warning("The '" + trueOptionArg + "' and '" + falseOptionArg + "' options cannot be present simultanously. These options will be ignored");
				usage();
			}
			else {
				res = true;
			}
		}
		else if (argList.contains(falseOptionArg)) {
			res = false;
		}
		return res;
	}

	private static String getNonOptionArgument(String[] args, int index) {
		int currNonOptArgIndex = -1;
		for (int i = 0; i < args.length; i++) {
			if ( (! args[i].isEmpty()) &&  (! isOptionArgument(args[i])) ) {
				currNonOptArgIndex++;
				if (currNonOptArgIndex == index) {
					return args[i];
				}
			}
		}
		throw new IndexOutOfBoundsException("Could not find non-option argument of index " + index);
	}
	
	private static void init() {
        cm = new ICDContentModel(owlModel);

        linearizationSpecificationClass = (OWLNamedClass) cm.getLinearizationSpecificationClass();
        linearizationProp = cm.getLinearizationProperty();
        linearizationViewProp = cm.getLinearizationViewProperty();
        isIncludedInLinearizationProp = cm.getIsIncludedInLinearizationProperty();
        isGroupingProp = cm.getIsGroupingProperty();
        isAuxiliaryAxisChildProp = cm.getIsAuxiliaryAxisChildProperty();
    }

    private static void addLinearization(Collection<RDFSNamedClass> clses) {
        int i = 0;
        for (RDFSNamedClass cls : clses) {
            addLinearization(cls);

            i++;
            if (i % 1000 == 0) {
                log.info("Processed " + i + " classes at " + new Date());
            }
       }
    }

    private static void addLinearization(RDFSNamedClass cls) {
    	for (RDFIndividual linearizationViewInst : linearizationViewInstances) {
    		
	        try{
	        	Boolean linSpecExists = false;
	            RDFResource linSpec = getOrCreateLinSpecInstance(cls, linearizationViewInst, linSpecExists);
	            
	            //not overriding existing flags
	            if (linSpecExists != null && linSpecExists == true && isOverrideExistingFlags == false) { 
	            	continue; //skip this lin
	            }
	            
	            Boolean tmpIsPartOf = isPartOf;
	            Boolean tmpIsGrouping = isGrouping;
	            Boolean tmpisAuxiliaryAxisChild = isAuxiliaryAxisChild;
	            
	            if (isCopyMMSFlags == true) {
	            	RDFResource mmsLinInst = getMMSLinSpecInstance(cls);
	            	
	            	if (mmsLinInst != null && linearizationViewInst.equals(mmsLinInst) == false) {
	            		tmpIsPartOf = (Boolean) mmsLinInst.getPropertyValue(isIncludedInLinearizationProp);
	            		tmpIsGrouping = (Boolean) mmsLinInst.getPropertyValue(isGroupingProp);
	            		tmpisAuxiliaryAxisChild = (Boolean) mmsLinInst.getPropertyValue(isAuxiliaryAxisChildProp);
	            	}
	            }
	            
	            if (tmpIsPartOf != null) {
	            	linSpec.setPropertyValue(isIncludedInLinearizationProp, tmpIsPartOf.booleanValue());
	            }
	            if (tmpIsGrouping != null) {
	            	linSpec.setPropertyValue(isGroupingProp, tmpIsGrouping.booleanValue());
	            }
	            if (tmpisAuxiliaryAxisChild != null) {
	            	linSpec.setPropertyValue(isAuxiliaryAxisChildProp, tmpisAuxiliaryAxisChild.booleanValue());
	            }
	            
	        } catch (Exception e) {
	            log.warning("------- Error at adding lin to " + cls);
	        }
    	}
    }
    
    private static RDFResource getOrCreateLinSpecInstance(RDFSNamedClass cls, RDFResource linView, Boolean linSpecExists) {
    	RDFResource linSpec = cm.getLinearizationSpecificationForView(cls, linView);
    	
    	if (linSpec != null) {
    		linSpecExists = true;
    		return linSpec;
    	}
    	
		//log.info("Creating lin spec for: " + cm.getTitleLabel(cls) + " . Lin View: " + linView);
		
		linSpec = linearizationSpecificationClass.createInstance((IcdIdGenerator.getNextUniqueId(owlModel)));
        linSpec.setPropertyValue(linearizationViewProp, linView);
        cls.addPropertyValue(linearizationProp, linSpec);
        
        linSpecExists = false;
	
    	return linSpec;
    }
    
    private static RDFResource getMMSLinSpecInstance(RDFSNamedClass cls) {
    	return cm.getLinearizationSpecificationForView(cls, mmsLinViewInstance);
    }
}
