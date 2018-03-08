package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.WHOFICContentModelConstants;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class UpdateAssociatedWith {

	private static final int UPDATE_INTERVALS_UNCHANGED = 250;
	private static final int UPDATE_INTERVALS_CHANGED = 100;
	private static final String HAS_MANIFESTATION_FLAG = "-m";
	private static final String CAUSING_CONDITION_FLAG = "-c";
	public static Set<String> categories = new HashSet<String>();

    public static void main(String[] args) {
    	if (args.length < 5) {
    		System.out.println("Please specify 5 command line arguments:");
    		System.out.println(" - the Protege server name (and port)");
    		System.out.println(" - the user name");
    		System.out.println(" - the password");
    		System.out.println(" - the fully specified name of the CSV file, which contains the list of grouping categories.");
    		System.out.println(" - one of the following flags: '-c' for causingCondition or '-m' for hasManifestation.");
    		return;
    	}
    	
		//URI pprjFileUri = new File(args[0]).toURI();
		String server = args[0];
		String user = args[1];
		String password = args[2];
        String csvPath = args[3];
        String flag = args[4];
        
    	if (! (flagEquals(flag, HAS_MANIFESTATION_FLAG) || flagEquals(flag, CAUSING_CONDITION_FLAG)) ) {
    		System.out.println("Please specify one of the following flags: " + HAS_MANIFESTATION_FLAG + " and " + CAUSING_CONDITION_FLAG);
    		return;
    	}        

        //read list of groupings category names from CSV file
        try {
            BufferedReader input = new BufferedReader(new FileReader(csvPath));
            //input.readLine(); //skip first line

            String line = null;
            while ((line = input.readLine()) != null) {
                if (line != null) {
                    try {
                        processLine(line);
                    } catch (Exception e) {
                        Log.getLogger().log(Level.WARNING, " Could not read line: " + line, e);
                    }
                }
            }
            
            Log.getLogger().info("Read the name of " + categories.size() + " categories from CSV file");
            Log.getLogger().info("");
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at parsing csv", e);
            return;
        }

        RemoteProjectManager rpm = RemoteProjectManager.getInstance();
        Project p = rpm.getProject(server, user, password, "ICD", true);
        OWLModel owlModel = (OWLModel) p.getKnowledgeBase();
        
        //open ICD umbrella OWL model
        //OWLModel owlModel = ImportUtils.openOWLModel(pprjFileUri);
        
        updateAssociatedWith(owlModel, flag);
}

    
    private static boolean flagEquals(String flag, String expectedValue) {
    	return expectedValue.equals(flag);
    }

    private static void processLine(String line) {
        //final String[] split = line.split("\t");
        final String[] split = line.split(",");

        String name = getSafeValue(split, 0).trim();

        categories.add(name);

        //Log.getLogger().info(name);

    }

    private static String getSafeValue(final String[] split, final int index) {
        if (index >= split.length) {
            return "";
        }
        String string = split[index];
        if (string == null || string.length() == 0) {
            return "";
        }
        return string.replaceAll("\"", "");
    }


	private static void updateAssociatedWith(OWLModel owlModel, String flag) {
		ICDContentModel icdContentModel = new ICDContentModel(owlModel);
		RDFProperty assocWithProp = owlModel.getRDFProperty(ICDContentModelConstants.PC_AXIS_ASSOCIATED_WITH);
		RDFSNamedClass assocWithRefTermClass = 
				owlModel.getRDFSNamedClass(ICDContentModelConstants.NS + "AssociatedWithReferenceTerm");
		RDFProperty newProp = (flagEquals(flag, CAUSING_CONDITION_FLAG) ?
				owlModel.getRDFProperty(ICDContentModelConstants.PC_AXIS_HAS_CAUSING_CONDITION) : 
				(flagEquals(flag, HAS_MANIFESTATION_FLAG) ?
						owlModel.getRDFProperty(ICDContentModelConstants.PC_AXIS_HAS_MANIFESTATION) : null));
		RDFSNamedClass newRefTermClass = (flagEquals(flag, CAUSING_CONDITION_FLAG) ?
				owlModel.getRDFSNamedClass(ICDContentModelConstants.NS + "CausingConditionReferenceTerm") : 
				(flagEquals(flag, HAS_MANIFESTATION_FLAG) ?
						owlModel.getRDFSNamedClass(ICDContentModelConstants.NS + "ManifestingConditionReferenceTerm") : null));

        RDFProperty allowedPostCoordinationAxisPropertyProperty = icdContentModel.getAllowedPostcoordinationAxisPropertyProperty();
        RDFProperty requiredPostCoordinationAxisPropertyProperty = icdContentModel.getRequiredPostcoordinationAxisPropertyProperty();

        int countChangedCategories = 0;
		for (String catName : categories) {
			RDFSNamedClass cat = icdContentModel.getICDCategory(catName);
			if (cat == null) {
				Log.getLogger().info("WARNING: Did not find category: " + catName);
				continue;
			}
			String browserText = cat.getBrowserText();
			Log.getLogger().info("\nProcessing " + browserText + " (" + catName + ")");
			
            String operationDescription = "Automatic conversion of 'associatedWith' post-coordination axes to '" + newProp.getLocalName() + "' for "+ browserText +".";
			try {
				owlModel.beginTransaction(operationDescription, catName);
	
				Collection<RDFResource> pcAxesSpecs = icdContentModel.getAllowedPostcoorcdinationSpecifications(cat);
	
				for (RDFResource pcAxesSpec : pcAxesSpecs) {
					List<RDFProperty> selectedRequiredPCAxes = icdContentModel.getSelectedRequiredPostcoordinationAxes(pcAxesSpec);
					if (selectedRequiredPCAxes.contains(assocWithProp)) {
						Log.getLogger().info("  required:" + pcAxesSpec.getPropertyValue(icdContentModel.getLinearizationViewProperty()));
						//remove assocWithProp and add newProp
	                	pcAxesSpec.removePropertyValue(requiredPostCoordinationAxisPropertyProperty, assocWithProp);
						pcAxesSpec.addPropertyValue(requiredPostCoordinationAxisPropertyProperty, newProp);
					}
					List<RDFProperty> selectedAllowedPCAxes = icdContentModel.getSelectedAllowedPostcoordinationAxes(pcAxesSpec, false);
					if (selectedAllowedPCAxes.contains(assocWithProp)) {
						Log.getLogger().info("  allowed:" + pcAxesSpec.getPropertyValue(icdContentModel.getLinearizationViewProperty()));
						//remove assocWithProp and add newProp
						pcAxesSpec.removePropertyValue(allowedPostCoordinationAxisPropertyProperty, assocWithProp);
						pcAxesSpec.addPropertyValue(allowedPostCoordinationAxisPropertyProperty, newProp);
					}
				}
				
				Collection<RDFResource> assocWithRefTerms = cat.getPropertyValues(assocWithProp);
				Log.getLogger().info("  assocWith reference term: " + assocWithRefTerms);
				for (RDFResource assocWithRefTerm : assocWithRefTerms) {
					//remove it from assocWithProp and add it to newProp
					cat.removePropertyValue(assocWithProp, assocWithRefTerm);
					cat.addPropertyValue(newProp, assocWithRefTerm);
					
					//change the reference term's type
					assocWithRefTerm.addRDFType(newRefTermClass);
					assocWithRefTerm.removeRDFType(assocWithRefTermClass);
				}
				
				owlModel.commitTransaction();
			} catch (Exception e) {
				Log.getLogger().log(
						Level.SEVERE,
						"Error during operation " + operationDescription + " at " + cat, e);
				owlModel.rollbackTransaction();
				throw new RuntimeException(e.getMessage(), e);
			}
			
			Log.getLogger().info(cat + ": " + operationDescription);
			countChangedCategories++;
			if (countChangedCategories % UPDATE_INTERVALS_CHANGED == 0) {
				System.out.println("\nChanged " + countChangedCategories + " categories so far...");
			}

		}
			
	}

	private static String getBrowserText(ICDContentModel cm, RDFSNamedClass res) {
	    String sortingLabel = (String) res.getPropertyValue(cm.getSortingLabelProperty());
	    sortingLabel = sortingLabel == null ? "" : sortingLabel;
	    RDFResource titleTerm = cm.getTerm(res, cm.getIcdTitleProperty());
	    String title = (String) titleTerm.getPropertyValue(cm.getLabelProperty());
	    title = title == null ? "" : title;
	    return  sortingLabel + " " + title;
	}

}
