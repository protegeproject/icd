package edu.stanford.bmir.icd.utils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

public class UpdateLinearizations {
	
	private static final String PPRJ_FILE_URI = "resources/projects/icd/icd_umbrella.pprj";
	private static final String EXCEL_FILE_ICD_ENTITIES = "resources/xls/mortality_nongrouping.xls";

	private static final String OPTION_MORTALITY = "-mt";
	private static final String OPTION_NO_MORTALITY = "-nomt";
	private static final String OPTION_MORBIDITY = "-mb";
	private static final String OPTION_NO_MORBIDITY = "-nomb";
	private static final String OPTION_OPHTHALMOLOGY = "-opht";
	private static final String OPTION_NO_OPHTHALMOLOGY = "-noopht";
	private static final String OPTION_GROUPING = "-gr";
	private static final String OPTION_NO_GROUPING = "-nogr";
	private static final String OPTION_LIN_PARENT = "-parent";
	
//	private static final String CLASS_LINEARIZATION_SPECIFICATION = ICDContentModelConstants.LINEARIZATION_SPECIFICATION_CLASS;
	private static final String PROPERTY_LINEARIZATION = ICDContentModelConstants.LINEARIZATION_PROP;
	private static final String PROPERTY_LINEARIZATION_VIEW = ICDContentModelConstants.LINEARIZATION_VIEW_PROP;
//	private static final String PROPERTY_LINEARIZATION_PARENT = ICDContentModelConstants.LINEARIZATION_PARENT_PROP;
	private static final String PROPERTY_IS_INCLUDED_IN_LINEARIZATION = ICDContentModelConstants.IS_INCLUDED_IN_LINEARIZATION_PROP;
	private static final String PROPERTY_IS_GROUPING = ICDContentModelConstants.IS_GROUPING_PROP;
	private static final String PROPERTY_VALUE_MORTALITY = ICDContentModelConstants.LINEARIZATION_VIEW_MORTALITY;
	private static final String PROPERTY_VALUE_MORBIDITY = ICDContentModelConstants.LINEARIZATION_VIEW_MORBIDITY;
	private static final String PROPERTY_VALUE_OPHTHALMOLOGY = ICDContentModelConstants.LINEARIZATION_VIEW_OPHTHALMOLOGY;
	
	
	private static URI pprjFileUri = new File(PPRJ_FILE_URI).toURI();
	private static File xlFileIcdEntities = new File(EXCEL_FILE_ICD_ENTITIES);
	private static Boolean includedInMortality;
	private static Boolean includedInMorbidity;
	private static Boolean includedInOphthalmology;
	private static Boolean isGrouping;
	
	public static void main(String args[]) {
		//read arguments
		
		List<String> optionArgs = getOptionArguments(args);
		List<String> fixedArgs = getNonOptionArguments(args);
		System.out.println("Fixed args:" + fixedArgs);
		System.out.println("Option args:" + optionArgs);
		if (fixedArgs.size() < 2 || optionArgs.size() == 0) {
			usage();
			return;
		}
		else {
			pprjFileUri = new File(fixedArgs.get(0)).toURI();
			xlFileIcdEntities = new File(fixedArgs.get(1));
			
			includedInMortality = getAlternativeOptionValue(optionArgs, OPTION_MORTALITY, OPTION_NO_MORTALITY);
			includedInMorbidity = getAlternativeOptionValue(optionArgs, OPTION_MORBIDITY, OPTION_NO_MORBIDITY);
			includedInOphthalmology = getAlternativeOptionValue(optionArgs, OPTION_OPHTHALMOLOGY, OPTION_NO_OPHTHALMOLOGY);
			isGrouping = getAlternativeOptionValue(optionArgs, OPTION_GROUPING, OPTION_NO_GROUPING);
			if (optionArgs.contains(OPTION_LIN_PARENT)) {
				Log.getLogger().warning("The '" + OPTION_LIN_PARENT + "' option is not supported at the moment. This option will be ignored");
			}
			
		
		}
		
		SystemUtilities.logSystemInfo();
		Log.getLogger().info("\n===== Started update from Excel " + new Date());
		Log.getLogger().info("=== PPRJ File: " + pprjFileUri);
		Log.getLogger().info("=== Excel file ICD entities: " + xlFileIcdEntities);
		if (includedInMortality != null) {Log.getLogger().info("=== Include in Mortality: " + includedInMortality);}
		if (includedInMorbidity != null) {Log.getLogger().info("=== Include in Morbidity: " + includedInMorbidity);}
		if (includedInOphthalmology != null) {Log.getLogger().info("=== Include in Ophthalmology: " + includedInOphthalmology);}
		if (isGrouping != null) {Log.getLogger().info("=== Is Grouping: " + isGrouping);}
		
		//open owl model
		OWLModel owlModel = ImportUtils.openOWLModel(pprjFileUri);
		
		//call update function
		updateLinearizationsFromXls(owlModel, xlFileIcdEntities);
		
		//finish processing
		Log.getLogger().info("\n===== End update from Excel at " + new Date());
	}


	private static void usage() {
		Log.getLogger().info("Usage: " +
		"UpdateLinearizations [-mt|-nomt] [-mb|-nomb] [-opht|-noopht] [-gr|nogr] [-parent] pprjFile xlFileICDEntities" +
		"\n" +
		"\nNote: at least one of the options is required");
	}

	
	private static List<String> getOptionArguments(String args[]) {
		ArrayList<String> res = new ArrayList<String>();
		for (String arg : args) {
			if (arg.startsWith("-")) {
				res.add(arg);
			}
		}
		return res;
	}
	
	private static List<String> getNonOptionArguments(String args[]) {
		ArrayList<String> res = new ArrayList<String>();
		for (String arg : args) {
			if (!arg.isEmpty() && !arg.startsWith("-")) {
				res.add(arg);
			}
		}
		return res;
	}
	
	private static Boolean getAlternativeOptionValue(List<String> optionArgs, 
			String trueOptionArg, String falseOptionArg) {
		Boolean res = null;
		if (optionArgs.contains(trueOptionArg)) {
			if (optionArgs.contains(falseOptionArg)) {
				Log.getLogger().warning("The '" + trueOptionArg + "' and '" + falseOptionArg + "' options cannot be present simultanously. These options will be ignored");
				usage();
			}
			else {
				res = true;
			}
		}
		else if (optionArgs.contains(falseOptionArg)) {
			res = false;
		}
		return res;
	}
	
	private static void updateLinearizationsFromXls(OWLModel owlModel, File excelFile) {
		Log.getLogger().info("\nUpdating linearization specifications for entities in Excel file... ");
		
//		OWLNamedClass classLinSpec = owlModel.getOWLNamedClass(CLASS_LINEARIZATION_SPECIFICATION);
		RDFProperty propLinearization = owlModel.getRDFProperty(PROPERTY_LINEARIZATION);
		RDFProperty propLinearizationView = owlModel.getRDFProperty(PROPERTY_LINEARIZATION_VIEW);
//		RDFProperty propLinearizationParent = owlModel.getRDFProperty(PROPERTY_LINEARIZATION_PARENT);
		RDFProperty propIsIncludedInLinearization = owlModel.getRDFProperty(PROPERTY_IS_INCLUDED_IN_LINEARIZATION);
		RDFProperty propIsGrouping = owlModel.getRDFProperty(PROPERTY_IS_GROUPING);
		RDFResource mortality = owlModel.getRDFResource(PROPERTY_VALUE_MORTALITY);
		RDFResource morbidity = owlModel.getRDFResource(PROPERTY_VALUE_MORBIDITY);
		RDFResource ophthalmology = owlModel.getRDFResource(PROPERTY_VALUE_OPHTHALMOLOGY);
		
		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);
			Sheet sh = wb.getSheet(0);
			for (int r =0; r < sh.getRows(); r++) { //change r=0 to r=1 to skip the first line (0)
				
				//if (r<10 || r>sh.getRows()-10) //JUST FOR TEST
				{
					String col1 = sh.getCell(0,r).getContents();
//					String col2 = "";//TODO replace this with next line when dealing with parents
					String col2 = (sh.getColumns() > 1 ? sh.getCell(1,r).getContents() : "");
					OWLNamedClass cat = owlModel.getOWLNamedClass(col1);
					if (cat == null) {
						Log.getLogger().warning("Problem! Category not found for row " + (r+1) + ": " + col1 + " | " + col2);
						continue;
					}
					List<RDFResource> linearizationsToUpdate = new ArrayList<RDFResource>();
					if (includedInMortality != null) {
						linearizationsToUpdate.add(mortality);
					}
					if (includedInMorbidity != null) {
						linearizationsToUpdate.add(morbidity);
					}
					if (includedInOphthalmology != null) {
						linearizationsToUpdate.add(ophthalmology);
					}
					
					List<RDFResource> linearizationsToBeFound = new ArrayList<RDFResource>(linearizationsToUpdate);
					if (linearizationsToBeFound.size() > 0) {
						Collection<?> linValues = cat.getPropertyValues(propLinearization);
						RDFResource linInstance = null;
						for (Object linValue : linValues) {
							//if we get a ClassCastException the program will be interrupted (which should be fine) 
							Object linearizationView = ((RDFResource)linValue).getPropertyValue(propLinearizationView);
							if (linearizationsToBeFound.contains(linearizationView)) {
								linInstance = (RDFResource) linValue;
								linearizationsToBeFound.remove((RDFResource) linearizationView);
								
								if (linearizationView.equals(mortality)) {
									linInstance.setPropertyValue(propIsIncludedInLinearization, includedInMortality);
								}
								else if (linearizationView.equals(morbidity)) {
									linInstance.setPropertyValue(propIsIncludedInLinearization, includedInMorbidity);
								}
								else if (linearizationView.equals(ophthalmology)) {
									linInstance.setPropertyValue(propIsIncludedInLinearization, includedInOphthalmology);
								}
								
								if (isGrouping != null) {
									linInstance.setPropertyValue(propIsGrouping, isGrouping);
								}
								
								//TODO set the linearization parent based on col2
							}
							
							if (linearizationsToBeFound.isEmpty()) {
								break;
							}
						}
						if ( ! linearizationsToBeFound.isEmpty() ) {
							Log.getLogger().warning("Category specified in row " + (r+1) + ": " + col1 + " | " + col2 + " had no LinearizationSpecification instance for views " + linearizationsToBeFound);
//							//create lin.spec. instance and set property values (like above)
//							for (RDFResource linearizationView : linearizationsToBeFound) {
//								linInstance = classLinSpec.createInstance(null);
//								cat.addPropertyValue(propLinearization, linInstance);
////								set all the necessary property values
//							}
						}
						
					}
				}
			}
			Log.getLogger().info("Done!");
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
