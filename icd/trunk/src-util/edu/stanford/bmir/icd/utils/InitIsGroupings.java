package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class InitIsGroupings {

	private static final int UPDATE_INTERVALS_UNCHANGED = 250;
	private static final int UPDATE_INTERVALS_CHANGED = 500;
	public static Set<String> groupingCategories = new HashSet<String>();
	
    public static void main(String[] args) {

    	if (args.length < 2) {
    		System.out.println("Please specify 4 command line arguments:");
    		System.out.println(" - the Protege server name (and port)");
    		System.out.println(" - the user name");
    		System.out.println(" - the password");
    		System.out.println(" - the fully specified name of the CSV file, which contains the list of grouping categories.");
    		return;
    	}
    	
		URI pprjFileUri = new File(args[0]).toURI();
		String server = args[0];
		String user = args[1];
		String password = args[2];
        String csvPath = args[3];

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
            
            Log.getLogger().info("Read the name of " + groupingCategories.size() + " categories from CSV file");
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
        
        updateGroupings(owlModel);
    }


    private static void processLine(String line) {
        final String[] split = line.split("\t");

        String name = getSafeValue(split, 0).trim();
        
        groupingCategories.add(name);

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


	private static void updateGroupings(OWLModel owlModel) {
		Set<String> validGroupingCategories = new HashSet<String>();
		Collection<RDFSNamedClass> unchagedCategories = new ArrayList<RDFSNamedClass>();
		Collection<RDFSNamedClass> unchagedCategoriesNotification = new ArrayList<RDFSNamedClass>();
		
		ICDContentModel icdContentModel = new ICDContentModel(owlModel);
		RDFProperty isGroupingProp = icdContentModel.getIsGroupingProperty();
        Collection<RDFSNamedClass> icdCategories = icdContentModel.getICDCategories();
    	Log.getLogger().info("\n\nFinished retrieving " + icdCategories.size() + " categories");
    	
    	int countChangedCategories = 0;
    	int countUnchangedCategories = 0;
        for (RDFSNamedClass cat : icdCategories) {
        	boolean isGrouping = false;
        	String catId = cat.getURI();
			if (groupingCategories.contains(catId)) {
				isGrouping = true;
				validGroupingCategories.add(catId);
			}
			
			Collection<RDFResource> linSpecs = icdContentModel.getLinearizationSpecifications(cat);
			int linWithModGrouping = 0;
			for (RDFResource linSpec : linSpecs) {
				Boolean propertyValue = (Boolean) linSpec.getPropertyValue(isGroupingProp);
				if (propertyValue != null) {
					//unchagedCategories.add(cat);
					linWithModGrouping++;
				}
			}
			if (linWithModGrouping > 0) {
				if (linWithModGrouping == linSpecs.size()) {
					unchagedCategories.add(cat);
				}
				else {
					unchagedCategoriesNotification.add(cat);
				}
				countUnchangedCategories++;
				if (countUnchangedCategories % UPDATE_INTERVALS_UNCHANGED == 0) {
			    	Log.getLogger().info("\nSkipped " + countUnchangedCategories + " categories so far...");
				}
			}
			else {
                String operationDescription = "Automatic import of the initial value for the isGrouping property. Value set to: " + isGrouping;
                try {
					owlModel.beginTransaction(operationDescription);

    				for (RDFResource linSpec : linSpecs) {
    					linSpec.setPropertyValue(isGroupingProp, isGrouping);
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
			    	Log.getLogger().info("\nChanged " + countChangedCategories + " categories so far...");
				}
			}
		}
    	Log.getLogger().info("\nSet grouping status for " + countChangedCategories + " categories");

        //display categories that weren't changed
    	Log.getLogger().info("\n\nThe following " + unchagedCategories.size() + " categories were not changed because they had their grouping status FULLY set:");
        for (RDFSNamedClass cat : unchagedCategories) {
			Log.getLogger().info(cat.getURI());
		}
    	Log.getLogger().info("\n\nThe following " + unchagedCategoriesNotification.size() + " categories were not changed because they had their grouping status PARTIALLY set:");
        for (RDFSNamedClass cat : unchagedCategoriesNotification) {
        	Log.getLogger().info(cat.getURI());
        }
        
        //display invalid category names
        groupingCategories.removeAll(validGroupingCategories);
        if ( ! groupingCategories.isEmpty() ){
        	Log.getLogger().info("\n\nThe following categories listed in the CVS file were invalid category names:");
        	for (String cat : groupingCategories) {
				Log.getLogger().info(cat);
			}
        }
	}

}
