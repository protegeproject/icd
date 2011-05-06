/**
 * 
 */
package edu.stanford.bmir.icd.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.bmir.icd.claml.ICDContentModelConstants;
import edu.stanford.bmir.icd.utils.ImportChapterXX.CategoryInfo;
import edu.stanford.bmir.icd.utils.ImportChapterXX.ICD10Reference;
import edu.stanford.bmir.icd.utils.ImportChapterXX.ICECIReference;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * This class is for the second round of importing new content 
 * for Chapter XX (External Causes and Injuries)
 * with values specified in a spreadsheet, as of April/May 2011.
 * 
 * @author csnyulas
 *
 */
public class ImportChapterXX_April2011 {
	
	private static final String PPRJ_FILE_URI = "resources/projects/icd/icd_umbrella.pprj";
	private static final String EXCEL_FILE_CHAPTER_XX = "resources/xls/Ch20_Spreadsheet_to_import_20110502.xls";

	private static final String EXCEL_SHEET_1 = "Sheet1";
	private static final String EXCEL_SHEET_ICD10_CODES_AND_DESCRIPTIONS_NEW = "ICD-10 Code+Desc UPDATED 3007";
	private static final String EXCEL_SHEET_ICECI_CODES_AND_DESCRIPTIONS = "ICECI Code+Desc";

	private static final String CLASS_ICD10_REFERENCE_TERM = "http://who.int/icd#ICD10ReferenceTerm";
	private static final String CLASS_EXTERNAL_CAUSE_TERM = "http://who.int/icd#ExternalCauseTerm";

	private static final String PROPERTY_ICD10_REFERENCE = "http://who.int/icd#icd10Reference";
	private static final String PROPERTY_INTENT = "http://who.int/icd#intent";
	private static final String PROPERTY_INTENT_DESCRIPTOR = "http://who.int/icd#intentDescriptor";
	private static final String PROPERTY_MECHANISM = "http://who.int/icd#mechanismOfInjury";
	private static final String PROPERTY_MECHANISM_DETAILS = "http://who.int/icd#transportDimension";
	private static final String PROPERTY_OBJECT = "http://who.int/icd#objectOrSubstanceProducingInjury";
	private static final String PROPERTY_PLACE = "http://who.int/icd#placeOfOccurrence";
	private static final String PROPERTY_ACTIVITY = "http://who.int/icd#activityWhenInjured";
	private static final String PROPERTY_SUBSTANCE_USE = "http://who.int/icd#substanceUse";

	private static final String CLASS_BULK_RETIRE = ICDContentModelConstants.NS + "BulkRetire_";
	
	private static final String ICD10_BP_ONTOLOGY_VERSION_ID = "44103";
	private static final String ICD10_BP_ONTOLOGY_LABEL = "ICD10";
	private static final String ICD10_ONTOLOGY_ID = ICD10_BP_ONTOLOGY_LABEL;
	private static final String ICD10_TERM_ID_PREFIX = "http://purl.bioontology.org/ontology/" + ICD10_BP_ONTOLOGY_LABEL + "/";
	private static final String ICD10_URL_PREFIX = "http://bioportal.bioontology.org/visualize/" + ICD10_BP_ONTOLOGY_VERSION_ID + "/?conceptid=";

	private static final String ICECI_BP_ONTOLOGY_VERSION_ID = "42765";
	private static final String ICECI_BP_ONTOLOGY_LABEL = "ICECI";
	private static final String ICECI_ONTOLOGY_ID = ICECI_BP_ONTOLOGY_LABEL;
	private static final String ICECI_TERM_ID_PREFIX = "http://purl.bioontology.org/ontology/" + ICECI_BP_ONTOLOGY_LABEL + "/";
	private static final String ICECI_URL_PREFIX = "http://bioportal.bioontology.org/visualize/" + ICECI_BP_ONTOLOGY_VERSION_ID + "/?conceptid=";
	
	private static URI pprjFileUri = new File(PPRJ_FILE_URI).toURI();
	private static File xlFileChaperXX = new File(EXCEL_FILE_CHAPTER_XX);
	
	private static final boolean TEST_RUN = false; 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//read arguments
		if (args.length < 2) {
			Log.getLogger().info("Usage: " +
			"ImportChapterXX_April2011 pprjFile xlFileChaperXX");
			return;
		}
		else {
			pprjFileUri = new File(args[0]).toURI();
			xlFileChaperXX = new File(args[1]);
		}

		SystemUtilities.logSystemInfo();
		Log.getLogger().info("\n===== Started import from Excel " + new Date());
		Log.getLogger().info("=== PPRJ File: " + pprjFileUri);
		Log.getLogger().info("=== Excel file Chapter XX: " + xlFileChaperXX);

		//open owl model
		OWLModel owlModel = ImportUtils.openOWLModel(pprjFileUri);

		//call migration functions
		fixXlsContent(xlFileChaperXX);
		Map<String, CategoryInfo> categoryInfoMap = extractCategoryInfoFromXls(owlModel, xlFileChaperXX);
		//print out content to be imported
		List<CategoryInfo> catInfos = new ArrayList<CategoryInfo>(categoryInfoMap.values());
		Collections.sort(catInfos);
		for (CategoryInfo catInfo : catInfos) {
			System.out.println(catInfo);
		}
		
		cleanUpChapterXX(owlModel);
		writeCategoryInfoToModel(catInfos, owlModel);

		//finish processing
		Log.getLogger().info("\n===== End import from Excel at " + new Date());
	}


	// ------------ Fix Content ------------- //

	private static void fixXlsContent(File excelFile) {
		Log.getLogger().info("\nFixing sorting labels in Excel file... ");

		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);
			//File tmpOutputXlsFile = File.createTempFile("ch20", ".xls", new File("."));
			WritableWorkbook wwb = Workbook.createWorkbook(excelFile, wb);

			WritableSheet sh = wwb.getSheet(EXCEL_SHEET_1);
			if (sh != null) {
				fixSortingLabels(sh, 0);
				fixTitle(sh, 1);
			}
			else {
				Log.getLogger().warning("Warning! Sheet '" + EXCEL_SHEET_1 + "' could not be opened.");
			}

			wwb.write();
			wwb.close();

			wb.close();
			Log.getLogger().info("Done!");
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}


	private static void fixSortingLabels(WritableSheet sh, int columnSortingLabel) {
		for (int r =1; r < sh.getRows(); r++) { //skip first line (0)

			if ((!TEST_RUN) || (TEST_RUN && (r<10 || r>sh.getRows()-10))) //IF TEST ONLY
			{
				WritableCell cellSortLabel = sh.getWritableCell(columnSortingLabel,r);
				String sortLabel = cellSortLabel.getContents();
				int indexOfUnnecessaryTrailingChars = sortLabel.length();
				while (indexOfUnnecessaryTrailingChars >= 1 
						&& (sortLabel.charAt(indexOfUnnecessaryTrailingChars - 1) == '.'
							|| sortLabel.charAt(indexOfUnnecessaryTrailingChars - 1) == '*')) {
					indexOfUnnecessaryTrailingChars --;
				}
				sortLabel = sortLabel.substring(0, indexOfUnnecessaryTrailingChars);
				
				//modify cell content if necessary
				if ( ! sortLabel.equals(cellSortLabel.getContents()) ) {
					if (cellSortLabel.getType() == CellType.LABEL) {
						((Label)cellSortLabel).setString(sortLabel);
					}
					else {
						Log.getLogger().warning("Warning!!! Can't modify content for cell (" +
								cellSortLabel.getColumn() + "," + cellSortLabel.getRow() + ") on worksheet " + sh.getName());
					}
				}
			}
		}
	}

	
	private static void fixTitle(WritableSheet sh, int columnTitle) {
		for (int r =1; r < sh.getRows(); r++) { //skip first line (0)

			if ((!TEST_RUN) || (TEST_RUN && (r<10 || r>sh.getRows()-10))) //IF TEST ONLY
			{
				WritableCell cellTitle = sh.getWritableCell(columnTitle,r);
				String title = cellTitle.getContents();
				title = title.trim();
				if (title.endsWith(":")) {
					title = title.substring(0, title.length() - 1);
				}
				//modify cell content if necessary
				if ( ! title.equals(cellTitle.getContents()) ) {
					if (cellTitle.getType() == CellType.LABEL) {
						((Label)cellTitle).setString(title);
					}
					else {
						Log.getLogger().warning("Warning!!! Can't modify content for cell (" +
								cellTitle.getColumn() + "," + cellTitle.getRow() + ") on worksheet " + sh.getName());
					}
				}
			}
		}
	}
	

	// ------------ Fix Content ------------- //


	private static Map<String, CategoryInfo> extractCategoryInfoFromXls(OWLModel owlModel,
			File excelFile) {
		Log.getLogger().info("\nImporting values for chapter XX from Excel file... ");

		Map<String, CategoryInfo> res = new HashMap<String, CategoryInfo>();

		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);
			
			Map<String, String> icd10CodeToLabel = new HashMap<String, String>();
			Sheet sh = wb.getSheet(EXCEL_SHEET_ICD10_CODES_AND_DESCRIPTIONS_NEW);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)
				String icd10Code = sh.getCell(0,r).getContents();
				String icd10Label = sh.getCell(1,r).getContents();
				icd10CodeToLabel.put(icd10Code, icd10Label);
			}
			
			Map<String, String> iceciCodeToLabel = new HashMap<String, String>();
			sh = wb.getSheet(EXCEL_SHEET_ICECI_CODES_AND_DESCRIPTIONS);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)
				String iceciCode = sh.getCell(0,r).getContents();
				String iceciLabel = sh.getCell(1,r).getContents();
				iceciCodeToLabel.put(iceciCode, iceciLabel);
			}
			
			sh = wb.getSheet(EXCEL_SHEET_1);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)

				if ((!TEST_RUN) || (TEST_RUN && (r<10 || r>sh.getRows()-10))) //IF TEST ONLY
				{
					String sortLabel = sh.getCell(0,r).getContents();
					String title = sh.getCell(1,r).getContents();
					
					String icd10Codes = sh.getCell(2,r).getContents();
					String parentSortLabel = sh.getCell(4,r).getContents();

					String intentCodes = sh.getCell(5,r).getContents();
					String mechCodes = sh.getCell(6,r).getContents();
					String mechDetCodes = sh.getCell(7,r).getContents();
					String objectCodes = sh.getCell(8,r).getContents();
					String activityCodes = sh.getCell(9,r).getContents();
					String placeCodes = sh.getCell(10,r).getContents();
					String perpetratorCodes = sh.getCell(11,r).getContents();
					String drugUseCodes = sh.getCell(12,r).getContents();
					String alcUseCodes = sh.getCell(13,r).getContents();

					CategoryInfo catInfo = res.get(sortLabel);
					if (catInfo == null) {
						catInfo = new CategoryInfo(sortLabel, title);
						res.put(sortLabel, catInfo);
					}
					
					catInfo.setParentLabel(parentSortLabel);
					
					for (String icd10Code : tokenizeICD10Codes(icd10Codes)) {
						catInfo.addIcd10Code(new ICD10Reference(icd10Code, icd10CodeToLabel.get(icd10Code)));
					}
					for (String intentCode : tokenizeICECICodes(intentCodes)) {
						catInfo.addIntentCode(new ICECIReference(intentCode, iceciCodeToLabel.get(intentCode)));
					}
					for (String mechCode : tokenizeICECICodes(mechCodes)) {
						catInfo.addMechanismCode(new ICECIReference(mechCode, iceciCodeToLabel.get(mechCode)));
					}
					for (String mechDetCode : tokenizeICECICodes(mechDetCodes)) {
						catInfo.addMechanismDetailsCode(new ICECIReference(mechDetCode, iceciCodeToLabel.get(mechDetCode)));
					}
					for (String objectCode : tokenizeICECICodes(objectCodes)) {
						catInfo.addObjectCode(new ICECIReference(objectCode, iceciCodeToLabel.get(objectCode)));
					}
					for (String activityCode : tokenizeICECICodes(activityCodes)) {
						catInfo.addActivityCode(new ICECIReference(activityCode, iceciCodeToLabel.get(activityCode)));
					}
					for (String placeCode : tokenizeICECICodes(placeCodes)) {
						catInfo.addPlaceCode(new ICECIReference(placeCode, iceciCodeToLabel.get(placeCode)));
					}
					for (String perpetratorCode : tokenizeICECICodes(perpetratorCodes)) {
						catInfo.addIntentDescriptorCode(new ICECIReference(perpetratorCode, iceciCodeToLabel.get(perpetratorCode)));
					}
					for (String drugUseCode : tokenizeICECICodes(drugUseCodes)) {
						catInfo.addSubstanceUseCode(new ICECIReference(drugUseCode, iceciCodeToLabel.get(drugUseCode)));
					}
					for (String alcUseCode : tokenizeICECICodes(alcUseCodes)) {
						catInfo.addSubstanceUseCode(new ICECIReference(alcUseCode, iceciCodeToLabel.get(alcUseCode)));
					}

				}
			}

			Log.getLogger().info("Done!");
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}

	private static List<String> tokenizeICD10Codes(String icd10Codes) {
		List<String> res = new ArrayList<String>();
		if (icd10Codes == null || icd10Codes.trim().length() == 0 
				|| icd10Codes.trim().equalsIgnoreCase("new")) {
			return res;
		}
		String[] tokens = icd10Codes.split(",");
		for (String icd10Token : tokens) {
			icd10Token = icd10Token.trim();
			if (icd10Token.contains(":")) {
				String startICD10Code = icd10Token.substring(0, icd10Token.indexOf(":"));
				String endICD10Code = icd10Token.substring(icd10Token.indexOf(":") + 1);
				
				//separate startICD10Code into prefix containing arbitrary characters and numeric suffix
				int i = startICD10Code.length();
				while (i >= 1 && startICD10Code.charAt(i-1) >= '0' && startICD10Code.charAt(i-1) <= '9') {
					i--;
				}
				String startICD10CodePrefix = startICD10Code.substring(0, i);
				String startICD10CodeSuffix = startICD10Code.substring(i);
				
				//separate endICD10Code into prefix containing arbitrary characters and numeric suffix
				i = endICD10Code.length();
				while (i >= 1 && endICD10Code.charAt(i-1) >= '0' && endICD10Code.charAt(i-1) <= '9') {
					i--;
				}
				String endICD10CodePrefix = endICD10Code.substring(0, i);
				String endICD10CodeSuffix = endICD10Code.substring(i);

				//generate intermediate codes
				if (startICD10CodePrefix.equals(endICD10CodePrefix)) {
					try {
						int start = Integer.parseInt(startICD10CodeSuffix);
						int end = Integer.parseInt(endICD10CodeSuffix);
						if (start <= end) {
							for (int k = start; k <= end; k++) {
								boolean addLeadingZero = startICD10CodeSuffix.length() > 1 && k < 10;
								res.add(startICD10CodePrefix + (addLeadingZero ? "0" : "") + k);
							}
						}
						else {
							Log.getLogger().severe("ERROR: Invalid code range: " + icd10Token + 
									" - Starting ICD-10 code '" + startICD10Code + "' needs to preceed ending ICD-10 code '" + endICD10Code + "'");
						}
					} catch (NumberFormatException e) {
						Log.getLogger().severe("ERROR: Invalid code range: " + icd10Token +
									" - Both starting and ending ICD-10 codes '" + startICD10Code + "' and '" + endICD10Code + "' need to end in a number.");
					}
				}
				else {
					Log.getLogger().severe("ERROR: Invalid code range: " + icd10Token + 
							" - Could not extract ICD-10 codes, because prefix of the starting and endig code" +
							" ('" + startICD10CodePrefix + "' and '" + endICD10CodePrefix + "') did not match." +
							" You can try to fix this issue by splitting up the code range in smaller code ranges that share the same preffix");
				}
			}
			else {
				res.add(icd10Token);
			}
		}
		return res;
	}

	
	private static List<String> tokenizeICECICodes(String iceciCodes) {
		List<String> res = new ArrayList<String>();
		if (iceciCodes == null || iceciCodes.trim().length() == 0
				|| iceciCodes.trim().equalsIgnoreCase("new")) {
			return res;
		}
		String[] tokens = iceciCodes.split(",");
		for (String iceciToken : tokens) {
			iceciToken = iceciToken.trim();
			res.add(iceciToken);
		}
		return res;
	}

	
	private static void cleanUpChapterXX(OWLModel owlModel) {
		Log.getLogger().info("\nClean-up existing content of Chapter XX... ");

		RDFProperty propSortingLabel = owlModel.getRDFProperty(ICDContentModelConstants.SORTING_LABEL_PROP);
		
		RDFSNamedClass categoryXX = owlModel.getRDFSNamedClass(ICDContentModelConstants.EXTERNAL_CAUSES_TOP_CLASS);
		if (categoryXX == null) {
			Log.getLogger().severe("ERROR: We can't find the top level class for Chapter XX (" + 
					ICDContentModelConstants.EXTERNAL_CAUSES_TOP_CLASS + "). Cleanup operation will be aborted");
			return;
		}
		
		Log.getLogger().info("\nActual cleaning is starting now: " + new Date());
		
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
		String categoryNameBulkRetire = CLASS_BULK_RETIRE + dateFormat.format(now);
		RDFSNamedClass categoryBulkRetire = owlModel.getRDFSNamedClass(categoryNameBulkRetire);
		
		for (Object subclass : categoryXX.getSubclasses(false)) {
			if (subclass instanceof RDFSNamedClass) {
				RDFSNamedClass oldCat = (RDFSNamedClass)subclass;
				Object sortLabel = oldCat.getPropertyValue(propSortingLabel);
				if (sortLabel != null && sortLabel.toString().trim().length() > 0) {
					if (categoryBulkRetire == null) {
					    ICDContentModel icdContentModel = new ICDContentModel(owlModel);
						categoryBulkRetire = icdContentModel.createICDCategory(categoryNameBulkRetire, categoryXX.getName());
						//set title term
			            RDFResource titleTerm = icdContentModel.createTitleTerm();
			            icdContentModel.fillTerm(titleTerm, null,  categoryBulkRetire.getLocalName(), null);
			            icdContentModel.addTitleTermToClass(categoryBulkRetire, titleTerm);
					}
					oldCat.addSuperclass(categoryBulkRetire);
					oldCat.removeSuperclass(categoryXX);
				}
			}
		}

		Log.getLogger().info("Done!");
	}


	//TODO check and fix
	private static void writeCategoryInfoToModel(
			Collection<CategoryInfo> categoryInfo, OWLModel owlModel) {
		Log.getLogger().info("\nWrite category information to model... ");

		RDFProperty propSortingLabel = owlModel.getRDFProperty(ICDContentModelConstants.SORTING_LABEL_PROP);
		RDFProperty propICD10Ref = owlModel.getRDFProperty(PROPERTY_ICD10_REFERENCE);
		RDFProperty propIntent = owlModel.getRDFProperty(PROPERTY_INTENT);
		RDFProperty propIntentDescriptor = owlModel.getRDFProperty(PROPERTY_INTENT_DESCRIPTOR);
		RDFProperty propMechanism = owlModel.getRDFProperty(PROPERTY_MECHANISM);
		RDFProperty propMechanismDetails = owlModel.getRDFProperty(PROPERTY_MECHANISM_DETAILS);
		RDFProperty propObject = owlModel.getRDFProperty(PROPERTY_OBJECT);
		RDFProperty propPlace = owlModel.getRDFProperty(PROPERTY_PLACE);
		RDFProperty propActivity = owlModel.getRDFProperty(PROPERTY_ACTIVITY);
		RDFProperty propSubstanceUse = owlModel.getRDFProperty(PROPERTY_SUBSTANCE_USE);
		
		RDFResourceBean resourceBean = new RDFResourceBean(owlModel);
		
	    ICDContentModel icdContentModel = new ICDContentModel(owlModel);
        //Collection<String> superClses = CollectionUtilities.createCollection(ICDContentModelConstants.EXTERNAL_CAUSES_TOP_CLASS);

        Map<String, String> parentLabelToIdMap = new HashMap<String, String>();
        parentLabelToIdMap.put("2", ICDContentModelConstants.EXTERNAL_CAUSES_TOP_CLASS);
        
        for (CategoryInfo catInfo : categoryInfo) {
            String superclassName = parentLabelToIdMap.get(catInfo.getParentLabel());
            if (superclassName == null) {
            	Log.getLogger().severe("ERROR: We can't create category " + catInfo + " becaues its parent, " + catInfo.getParentLabel() + ", was not created yet");
            	continue;
            }
			RDFSNamedClass category = icdContentModel.createICDCategory(null, superclassName);
			parentLabelToIdMap.put(catInfo.getSortingLabel(), category.getName());
			
            category.setPropertyValue(owlModel.getRDFSLabelProperty(), catInfo.getFullLabel());

            category.setPropertyValue(propSortingLabel, catInfo.getSortingLabel());

            RDFResource titleTerm = icdContentModel.createTitleTerm();
            icdContentModel.fillTerm(titleTerm, null,  catInfo.getTitle(), null);
            icdContentModel.addTitleTermToClass(category, titleTerm);

            addICD10References(category, catInfo.getIcd10Codes(), propICD10Ref, resourceBean);
            
            addICECIReferences(category, catInfo.getIntentCodes(), propIntent, resourceBean);
            addICECIReferences(category, catInfo.getIntentDescriptorCodes(), propIntentDescriptor, resourceBean);
            addICECIReferences(category, catInfo.getMechanismCodes(), propMechanism, resourceBean);
            addICECIReferences(category, catInfo.getMechanismDetailsCodes(), propMechanismDetails, resourceBean);
            addICECIReferences(category, catInfo.getObjectCodes(), propObject, resourceBean);
            addICECIReferences(category, catInfo.getPlaceCodes(), propPlace, resourceBean);
            addICECIReferences(category, catInfo.getActivityCodes(), propActivity, resourceBean);
            addICECIReferences(category, catInfo.getSubstanceUseCodes(), propSubstanceUse, resourceBean);
		}

        Log.getLogger().info("Done!");
	}


	private static void addICD10References(RDFSNamedClass category,
			List<ICD10Reference> icd10Codes, RDFProperty propICD10Ref,
			RDFResourceBean resBean) {
		if (icd10Codes != null) {
		    for (ICD10Reference icd10Code : icd10Codes) {
		    	RDFResource icd10Ref = resBean.getClsICD10RefTerm().createInstance(null);
		    	icd10Ref.setPropertyValue(resBean.getPropBpOntologyId(), ICD10_BP_ONTOLOGY_VERSION_ID);
		    	icd10Ref.setPropertyValue(resBean.getPropBpOntologyLabel(), ICD10_BP_ONTOLOGY_LABEL);
		    	icd10Ref.setPropertyValue(resBean.getPropBpShortTerm(), icd10Code.code);
		    	icd10Ref.setPropertyValue(resBean.getPropLabel(), icd10Code.label);
		    	icd10Ref.setPropertyValue(resBean.getPropOntologyId(), ICD10_ONTOLOGY_ID);
		    	icd10Ref.setPropertyValue(resBean.getPropTermId(), ICD10_TERM_ID_PREFIX + icd10Code.code);
		    	icd10Ref.setPropertyValue(resBean.getPropUrl(), ICD10_URL_PREFIX + icd10Code.code);

		    	category.addPropertyValue(propICD10Ref, icd10Ref);
		    }
		}
	}

	
	private static void addICECIReferences(RDFSNamedClass category,
			List<ICECIReference> iceciCodes, RDFProperty propICECICodeRef,
			RDFResourceBean resBean) {
		if (iceciCodes != null) {
			for (ICECIReference iceciCode : iceciCodes) {
				RDFResource iceciRef = resBean.getClsExternalCauseTerm().createInstance(null);
				iceciRef.setPropertyValue(resBean.getPropBpOntologyId(), ICECI_BP_ONTOLOGY_VERSION_ID);
				iceciRef.setPropertyValue(resBean.getPropBpOntologyLabel(), ICECI_BP_ONTOLOGY_LABEL);
				iceciRef.setPropertyValue(resBean.getPropBpShortTerm(), iceciCode.code);
				iceciRef.setPropertyValue(resBean.getPropLabel(), iceciCode.label);
				iceciRef.setPropertyValue(resBean.getPropOntologyId(), ICECI_ONTOLOGY_ID);
				iceciRef.setPropertyValue(resBean.getPropTermId(), ICECI_TERM_ID_PREFIX + iceciCode.code);
				iceciRef.setPropertyValue(resBean.getPropUrl(), ICECI_URL_PREFIX + iceciCode.code);
				
				category.addPropertyValue(propICECICodeRef, iceciRef);
			}
		}
	}
	
	
	private static class RDFResourceBean {

        private RDFProperty propBpShortTerm;
        private RDFProperty propBpOntologyLabel;
        private RDFProperty propBpOntologyId;
		private RDFProperty propUrl;
        private RDFProperty propOntologyId;
        private RDFProperty propTermId;
        private RDFProperty propLabel;
        
        private RDFSNamedClass clsICD10RefTerm;
        private RDFSNamedClass clsExtCauseTerm;
        
        public RDFResourceBean(OWLModel owlModel) {
            propBpShortTerm = owlModel.getRDFProperty(ICDContentModelConstants.BP_SHORT_TERM_ID_PROP);
            propBpOntologyLabel = owlModel.getRDFProperty(ICDContentModelConstants.BP_ONTOLOGY_LABEL_PROP);
            propBpOntologyId = owlModel.getRDFProperty(ICDContentModelConstants.BP_ONTOLOGY_ID_PROP);
            propUrl = owlModel.getRDFProperty(ICDContentModelConstants.URL_PROP);
            propOntologyId = owlModel.getRDFProperty(ICDContentModelConstants.ONTOLOGYID_PROP);
            propTermId = owlModel.getRDFProperty(ICDContentModelConstants.TERM_ID_PROP);
            propLabel = owlModel.getRDFProperty(ICDContentModelConstants.LABEL_PROP);

            clsICD10RefTerm = owlModel.getRDFSNamedClass(CLASS_ICD10_REFERENCE_TERM);
            clsExtCauseTerm = owlModel.getRDFSNamedClass(CLASS_EXTERNAL_CAUSE_TERM);
        }
        
        public RDFProperty getPropBpShortTerm() {
			return propBpShortTerm;
		}
		public RDFProperty getPropBpOntologyLabel() {
			return propBpOntologyLabel;
		}
		public RDFProperty getPropBpOntologyId() {
			return propBpOntologyId;
		}
		public RDFProperty getPropUrl() {
			return propUrl;
		}
		public RDFProperty getPropOntologyId() {
			return propOntologyId;
		}
		public RDFProperty getPropTermId() {
			return propTermId;
		}
		public RDFProperty getPropLabel() {
			return propLabel;
		}
		
		public RDFSNamedClass getClsICD10RefTerm() {
			return clsICD10RefTerm;
		}
		public RDFSNamedClass getClsExternalCauseTerm() {
			return clsExtCauseTerm;
		}
	}
}
