package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.IDGenerator;
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
 * 
 * @author csnyulas
 *
 */

public class AddMultipleLinearizations {
	private static final String OPTION_PART_OF = "-partOf";
	private static final String OPTION_NOT_PART_OF = "-notPartOf";
	private static final String OPTION_GROUPING = "-gr";
	private static final String OPTION_NO_GROUPING = "-nogr";

	private static Logger log = Log.getLogger(AddMultipleLinearizations.class);

    private static ICDContentModel icdContentModel;
    private static OWLModel owlModel;

	private static RDFSNamedClass topCategory;
    private static List<RDFIndividual> linearizationViewInstances = new ArrayList<RDFIndividual>();
    private static Boolean isPartOf;
    private static Boolean isGrouping;
    private static OWLNamedClass linearizationSpecificationClass;
    private static RDFProperty linearizationProp;
    private static RDFProperty linearizationViewProp;


    public static void main(String[] args) {
    	int nonOptArgCount = countNonOptionArguments(args);
        if (nonOptArgCount < 3) {
            log.severe("Argument missing: pprj file name, category name or linearization name");
            usage();
            return;
        }

        isPartOf = getAlternativeOptionValue(args, OPTION_PART_OF, OPTION_NOT_PART_OF);
		isGrouping = getAlternativeOptionValue(args, OPTION_GROUPING, OPTION_NO_GROUPING);

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


        topCategory = icdContentModel.getICDCategory(getNonOptionArgument(args, 1));
        if (topCategory == null) {
            log.severe("Abort. Failed to find category: " + topCategory);
            return;
        }
        
        for (int i = 2; i < nonOptArgCount; i++) {
	        RDFIndividual linearizationViewInst = owlModel.getRDFIndividual(getNonOptionArgument(args, i));
	        if (linearizationViewInst == null) {
	            log.severe("Abort. Failed to find linearization view: " + linearizationViewInst);
	            return;
	        }
	        linearizationViewInstances.add(linearizationViewInst);
        }

        init();

        long t0 = System.currentTimeMillis();
        log.info("Started getting ICD categories at: " + new Date());

        Collection<RDFSNamedClass> cats = icdContentModel.getRDFSNamedClassCollection(topCategory.getSubclasses(true));

        log.info("Got " + cats.size() + " ICD categories at: " + new Date() + " in " + (System.currentTimeMillis() -  t0)/1000 + " secs.");

        cats.add(topCategory);

        addLinearization(cats);

        log.info("Finished at: " + new Date());
    }

	private static void usage() {
		Log.getLogger().info(
				"Usage: AddLinearizationAdvanced [-partOf|-notPartOf] [-gr|-nogr] pprj_file_name top_category linearization_view_1 [linearization_view_2 ...]");
	}
	
    private static int countNonOptionArguments(String[] args) {
		int nonOptArgCount = 0;
		for (String arg : args) {
			if (isOptionArgument(arg)) {
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
			if ( ! isOptionArgument(args[i]) ) {
				currNonOptArgIndex++;
				if (currNonOptArgIndex == index) {
					return args[i];
				}
			}
		}
		throw new IndexOutOfBoundsException("Could not find non-option argument of index " + index);
	}
	
	private static void init() {
        icdContentModel = new ICDContentModel(owlModel);

        linearizationSpecificationClass = (OWLNamedClass) icdContentModel.getLinearizationSpecificationClass();
        linearizationProp = icdContentModel.getLinearizationProperty();
        linearizationViewProp = icdContentModel.getLinearizationViewProperty();
    }

    private static void addLinearization(Collection<RDFSNamedClass> cats) {
        int i = 0;
        for (RDFSNamedClass cat : cats) {
            addLinearization(cat);

            i++;
            if (i % 1000 == 0) {
                log.info("Processed " + i + " categories at " + new Date());
            }
       }

    }

    private static void addLinearization(RDFSNamedClass cat) {
    	for (RDFIndividual linearizationViewInst : linearizationViewInstances) {
	        try{
	            RDFResource linSpec = linearizationSpecificationClass.createInstance(IDGenerator.getNextUniqueId());
	            linSpec.setPropertyValue(linearizationViewProp, linearizationViewInst);
	            cat.addPropertyValue(linearizationProp, linSpec);
	        } catch (Exception e) {
	            log.warning("------- Error at adding lin to " + cat);
	        }
    	}
    }
}
