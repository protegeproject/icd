package edu.stanford.bmir.icd.utils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;

public class ImportLinearizationsAndBiologicalSex {
	
	private static final String PPRJ_FILE_URI = "resources/projects/icd/icd_umbrella.pprj";
	private static final String EXCEL_FILE_BIOLOGICAL_SEX = "resources/xls/Code_and_sex-add_children.xls";
	private static final String EXCEL_FILE_NO_MORTALITY = "resources/xls/Not-mortailty.xls";

	private static final String CLASS_ICD_CATEGORY = "http://who.int/icd#ICDCategory";
	
	private static final String PROPERTY_BIOLOGICAL_SEX = "http://who.int/icd#biologicalSex";
	private static final String PROPERTY_VALUE_BIOLOGICAL_SEX_NA = "http://who.int/icd#BiologicalSexNotAppSCTerm";
	private static final String PROPERTY_VALUE_MALE = "http://who.int/icd#MaleSCTerm";
	private static final String PROPERTY_VALUE_FEMALE = "http://who.int/icd#FemaleSCTerm";
	
	private static final String CLASS_LINEARIZATION_SPECIFICATION = "http://who.int/icd#LinearizationSpecification";
	private static final String PROPERTY_LINEARIZATION = "http://who.int/icd#linearization";
	private static final String PROPERTY_LINEARIZATION_VIEW = "http://who.int/icd#linearizationView";
	private static final String PROPERTY_LINEARIZATION_PARENT = "http://who.int/icd#linearizationParent";
	private static final String PROPERTY_IS_INCLUDED_IN_LINEARIZATION = "http://who.int/icd#isIncludedInLinearization";
	private static final String PROPERTY_VALUE_MORTALITY = "http://who.int/icd#Mortality";
	private static final String PROPERTY_VALUE_MORBIDITY = "http://who.int/icd#Morbidity";
	
	
	private static URI pprjFileUri = new File(PPRJ_FILE_URI).toURI();
	private static File xlFileBiologicalSex = new File(EXCEL_FILE_BIOLOGICAL_SEX);
	private static File xlFileNoMortality = new File(EXCEL_FILE_NO_MORTALITY);
	
	public static void main(String args[]) {
		//read arguments
		if (args.length < 3) {
			Log.getLogger().info("Usage: " +
			"ImportLinearizationsAndBiologicalSex pprjFile xlFileBiologicalSex xlFileNoMortality");
			return;
		}
		else {
			pprjFileUri = new File(args[0]).toURI();
			xlFileBiologicalSex = new File(args[1]);
			xlFileNoMortality = new File(args[2]);
		}
		
		SystemUtilities.logSystemInfo();
		Log.getLogger().info("\n===== Started import from Excel " + new Date());
		Log.getLogger().info("=== PPRJ File: " + pprjFileUri);
		Log.getLogger().info("=== Excel file biological sex: " + xlFileBiologicalSex);
		Log.getLogger().info("=== Excel file no mortality: " + xlFileNoMortality);
		
		//open owl model
		OWLModel owlModel = ImportUtils.openOWLModel(pprjFileUri);
		
		//call migration functions
		setAllBiologicalSexToNA(owlModel);
		importBiologicalSexFromXls(owlModel, xlFileBiologicalSex);
		importNoMortalityFromXls(owlModel, xlFileNoMortality);
		
		//finish processing
		Log.getLogger().info("\n===== End import from Excel at " + new Date());
	}

	
	private static void setAllBiologicalSexToNA(OWLModel owlModel) {
		Log.getLogger().info("\nSetting all biological sex values to N/A... ");

		RDFProperty propBiologicalSex = owlModel.getRDFProperty(PROPERTY_BIOLOGICAL_SEX);
		RDFResource sexNA = owlModel.getRDFResource(PROPERTY_VALUE_BIOLOGICAL_SEX_NA);
		if (sexNA == null) {
			Log.getLogger().warning(PROPERTY_VALUE_BIOLOGICAL_SEX_NA + 
					" not found. Setting all biological sex values to N/A will be aborted.");
			return;
		}
		
		OWLNamedClass clsIcdCategory = owlModel.getOWLNamedClass(CLASS_ICD_CATEGORY);
		Collection<?> allIcdCategories = clsIcdCategory.getSubclasses(true);
		int n = allIcdCategories.size();
		int percentageBlockSize = n > 50 ? n / 50 : 1;
		int i=0;
		for (Object icdCategory : allIcdCategories) {
			i++;
			if (icdCategory instanceof OWLNamedClass) {
				((OWLNamedClass)icdCategory).setPropertyValue(propBiologicalSex, sexNA);
			}
			if ( i % percentageBlockSize == 0 ) {
				System.out.print(".");
				//System.out.println("" + i + ". " + icdCategory);
			}
		}
		System.out.println("\nBiological Sex for " + n + " category set to: N/A");
	}

	
	private static void importBiologicalSexFromXls(OWLModel owlModel, File excelFile) {
		Log.getLogger().info("\nImporting values for special biological sex from Excel file... ");
		
		RDFProperty propBiologicalSex = owlModel.getRDFProperty(PROPERTY_BIOLOGICAL_SEX);
		RDFResource male = owlModel.getRDFResource(PROPERTY_VALUE_MALE);
		RDFResource female = owlModel.getRDFResource(PROPERTY_VALUE_FEMALE);
		RDFResource sexNA = owlModel.getRDFResource(PROPERTY_VALUE_BIOLOGICAL_SEX_NA);

		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);
			Sheet sh = wb.getSheet(0);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)
				
				//if (r<10 || r>sh.getRows()-10) //JUST FOR TEST
				{
					String col1 = sh.getCell(0,r).getContents();
					String col2 = sh.getCell(1,r).getContents();
					OWLNamedClass cat = owlModel.getOWLNamedClass(col1);
					if (cat == null) {
						System.out.println("Problem! Category not found for row " + (r+1) + ": " + col1 + " | " + col2);
						continue;
					}
					RDFResource biologicalSex = null;
					if ("male".equals(col2.toLowerCase())) {
						biologicalSex = male;
					}
					else if ("female".equals(col2.toLowerCase())) {
						biologicalSex = female;
					}
					else {
						System.out.println("Problem! Invalid biological sex specified in row " + (r+1) + ": " + col1 + " | " + col2);
						continue;
					}
					if (biologicalSex != null) {
						Object oldValue = cat.getPropertyValue(propBiologicalSex);
						if (oldValue == null || oldValue.equals(sexNA)) {
							cat.setPropertyValue(propBiologicalSex, biologicalSex);
						}
						else {
							System.out.println("Biological sex for category specified in row " + (r+1) + ": " + col1 + " | " + col2 + " was already set to " + oldValue);
						}
					}
				}
			}		
			System.out.println("Done!");
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void importNoMortalityFromXls(OWLModel owlModel, File excelFile) {
		Log.getLogger().info("\nImporting values for no mortality linearization from Excel file... ");
		
		OWLNamedClass classLinSpec = owlModel.getOWLNamedClass(CLASS_LINEARIZATION_SPECIFICATION);
		RDFProperty propLinearization = owlModel.getRDFProperty(PROPERTY_LINEARIZATION);
		RDFProperty propLinearizationView = owlModel.getRDFProperty(PROPERTY_LINEARIZATION_VIEW);
//		RDFProperty propLinearizationParent = owlModel.getRDFProperty(PROPERTY_LINEARIZATION_PARENT);
		RDFProperty propIsIncludedInLinearization = owlModel.getRDFProperty(PROPERTY_IS_INCLUDED_IN_LINEARIZATION);
		RDFResource mortality = owlModel.getRDFResource(PROPERTY_VALUE_MORTALITY);
//		RDFResource morbidity = owlModel.getRDFResource(PROPERTY_VALUE_MORBIDITY);
		
		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);
			Sheet sh = wb.getSheet(0);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)
				
				//if (r<10 || r>sh.getRows()-10) //JUST FOR TEST
				{
					String col1 = sh.getCell(0,r).getContents();
					String col2 = sh.getCell(1,r).getContents();
					OWLNamedClass cat = owlModel.getOWLNamedClass(col1);
					if (cat == null) {
						System.out.println("Problem! Category not found for row " + (r+1) + ": " + col1 + " | " + col2);
						continue;
					}
					RDFResource linearization = null;
					if ("nm".equals(col2.toLowerCase())
							|| "nmr".equals(col2.toLowerCase())) {
						linearization  = mortality;
					}
//					else if ("nmr".equals(col2.toLowerCase())) {
//						linearization = morbidity;
//					}
					else {
						System.out.println("Problem! Invalid value specified in row " + (r+1) + ": " + col1 + " | " + col2);
						continue;
					}
					if (linearization != null) {
						Collection<?> linValues = cat.getPropertyValues(propLinearization);
						RDFResource linInstance = null;
						for (Object linValue : linValues) {
							//if we get a ClassCastException the program will be interrupted (which should be fine) 
							if (((RDFResource)linValue).getPropertyValue(propLinearizationView).equals(linearization)) {
								linInstance = (RDFResource) linValue;
								break;
							}
						}
						if (linInstance == null) {
							System.out.println("Category specified in row " + (r+1) + ": " + col1 + " | " + col2 + " had no LinearizationSpecification instance for view " + linearization);
							//create lin.spec. instance
							linInstance = classLinSpec.createInstance(null);
							cat.addPropertyValue(propLinearization, linInstance);
						}
						
						//set value "false" for this linearization
						linInstance.setPropertyValue(propIsIncludedInLinearization, Boolean.FALSE);
						//TODO check if the following command is necessary or not: 
						//linInstance.setPropertyValue(propLinearizationParent, null);
					}
				}
			}
			System.out.println("Done!");
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
