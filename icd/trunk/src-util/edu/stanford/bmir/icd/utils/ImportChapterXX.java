package edu.stanford.bmir.icd.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * This class imports new content for Chapter XX (External Causes and Injuries)
 * with values specified in a spreadsheet.
 *
 * @author csnyulas
 *
 */
public class ImportChapterXX {

	private static final String PPRJ_FILE_URI = "resources/projects/icd/icd_umbrella.pprj";
	private static final String EXCEL_FILE_CHAPTER_XX = "resources/xls/ICD_ImportTemplate.xls";

	private static final String EXCEL_SHEET_ICD10_CODES = "Import iCAT";
	private static final String EXCEL_SHEET_ICD10_CODES_NEW = "Import iCAT UPDATED 3007";
	private static final String EXCEL_SHEET_CATEGORY_COMMENTS = "Notes for TAG";
	private static final String EXCEL_SHEET_ICD10_CODES_AND_DESCRIPTIONS = "ICD-10 Code+Desc & ICD-11 Mech";
	private static final String EXCEL_SHEET_ICD10_CODES_AND_DESCRIPTIONS_NEW = "ICD-10 Code+Desc UPDATED 3007";

	private static final String CLASS_ICD10_REFERENCE_TERM = "http://who.int/icd#ICD10ReferenceTerm";

	private static final String PROPERTY_ICD10_REFERENCE = "http://who.int/icd#icd10Reference";

	private static final String ICD10_BP_ONTOLOGY_VERSION_ID = "44103";
	private static final String ICD10_BP_ONTOLOGY_LABEL = "ICD10";
	private static final String ICD10_ONTOLOGY_ID = ICD10_BP_ONTOLOGY_LABEL;
	private static final String ICD10_TERM_ID_PREFIX = "http://purl.bioontology.org/ontology/" + ICD10_BP_ONTOLOGY_LABEL + "/";
	private static final String ICD10_URL_PREFIX = "http://bioportal.bioontology.org/visualize/" + ICD10_BP_ONTOLOGY_VERSION_ID + "/?conceptid=";

	private static URI pprjFileUri = new File(PPRJ_FILE_URI).toURI();
	private static File xlFileChaperXX = new File(EXCEL_FILE_CHAPTER_XX);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//read arguments
		if (args.length < 2) {
			Log.getLogger().info("Usage: " +
			"ImportChapterXX pprjFile xlFileChaperXX");
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
		List<CategoryInfo> catInfos = new ArrayList(categoryInfoMap.values());
		Collections.sort(catInfos);
		for (CategoryInfo catInfo : catInfos) {
			System.out.println(catInfo);
		}
		writeCategoryInfoToModel(catInfos, owlModel);

		//finish processing
		Log.getLogger().info("\n===== End import from Excel at " + new Date());
	}


	private static void fixXlsContent(File excelFile) {
		// TODO Auto-generated method stub
		Log.getLogger().info("\nFixing sorting labels in Excel file... ");

		Map<String, CategoryInfo> res = new HashMap<String, CategoryInfo>();

		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);
			//File tmpOutputXlsFile = File.createTempFile("ch20", ".xls", new File("."));
			WritableWorkbook wwb = Workbook.createWorkbook(excelFile, wb);

			WritableSheet sh = wwb.getSheet(EXCEL_SHEET_CATEGORY_COMMENTS);
			if (sh != null) {
				fixSortingLabels(sh, 0);
			}
			else {
				System.out.println("Warning! Sheet '" + EXCEL_SHEET_CATEGORY_COMMENTS + "' could not be opened.");
			}

			sh = wwb.getSheet(EXCEL_SHEET_ICD10_CODES);
			if (sh != null) {
				fixSortingLabels(sh, 1);
			}
			else {
				System.out.println("Warning! Sheet '" + EXCEL_SHEET_ICD10_CODES + "' could not be opened.");
			}

			sh = wwb.getSheet(EXCEL_SHEET_ICD10_CODES_NEW);
			if (sh != null) {
				fixSortingLabels(sh, 1);
			}
			else {
				System.out.println("Warning! Sheet '" + EXCEL_SHEET_ICD10_CODES_NEW + "' could not be opened.");
			}

			sh = wwb.getSheet(EXCEL_SHEET_ICD10_CODES_AND_DESCRIPTIONS);
			if (sh != null) {
				fixSortingLabels(sh, 2);
			}
			else {
				System.out.println("Warning! Sheet '" + EXCEL_SHEET_ICD10_CODES_AND_DESCRIPTIONS + "' could not be opened.");
			}

			sh = wwb.getSheet(EXCEL_SHEET_ICD10_CODES_AND_DESCRIPTIONS_NEW);
			if (sh != null) {
				fixSortingLabels(sh, 2);
			}
			else {
				System.out.println("Warning! Sheet '" + EXCEL_SHEET_ICD10_CODES_AND_DESCRIPTIONS_NEW + "' could not be opened.");
			}

			wwb.write();
			wwb.close();

			wb.close();
			System.out.println("Done!");
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

			//if (r<10 || r>sh.getRows()-10) //JUST FOR TEST
			{
				WritableCell cellSortLabel = sh.getWritableCell(columnSortingLabel,r);
				String sortLabel = cellSortLabel.getContents();
				int posFirstSpace = sortLabel.indexOf(' ');
				if (posFirstSpace > 0) {
					if (sortLabel.charAt(posFirstSpace - 1) == ':') {
						//replace colon (:) with period (.)
						sortLabel = sortLabel.substring(0, posFirstSpace - 1) + "." + sortLabel.substring(posFirstSpace);
					}
					else if (sortLabel.charAt(posFirstSpace - 1) != '.') {
						//insert period (.)
						sortLabel = sortLabel.substring(0, posFirstSpace) + "." + sortLabel.substring(posFirstSpace);
					}
				}
				//modify cell content if necessary
				if ( ! sortLabel.equals(cellSortLabel.getContents()) ) {
					if (cellSortLabel.getType() == CellType.LABEL) {
						((Label)cellSortLabel).setString(sortLabel);
					}
					else {
						System.out.println("Warning!!! Can't modify content for cell (" +
								cellSortLabel.getColumn() + "," + cellSortLabel.getRow() + ") on worksheet " + sh.getName());
					}
				}
			}
		}
	}


	private static Map<String, CategoryInfo> extractCategoryInfoFromXls(OWLModel owlModel,
			File excelFile) {
		Log.getLogger().info("\nImporting values for chapter XX from Excel file... ");

		Map<String, CategoryInfo> res = new HashMap<String, CategoryInfo>();

		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);
			Sheet sh = wb.getSheet(EXCEL_SHEET_CATEGORY_COMMENTS);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)

				//if (r<10 || r>sh.getRows()-10) //JUST FOR TEST
				{
					String sortLabel = sh.getCell(0,r).getContents();
					String comment = sh.getCell(1,r).getContents();

					CategoryInfo catInfo = res.get(sortLabel);
					if (catInfo == null) {
						catInfo = new CategoryInfo(sortLabel);
						res.put(sortLabel, catInfo);
					}
					catInfo.setDescription(comment);
				}
			}

			sh = wb.getSheet(EXCEL_SHEET_ICD10_CODES_AND_DESCRIPTIONS_NEW);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)

				//if (r<10 || r>sh.getRows()-10) //JUST FOR TEST
				{
					String icd10Code = sh.getCell(0,r).getContents();
					String icd10Label = sh.getCell(1,r).getContents();
					String sortLabel = sh.getCell(2,r).getContents();

					CategoryInfo catInfo = res.get(sortLabel);
					if (catInfo == null) {
						catInfo = new CategoryInfo(sortLabel);
						res.put(sortLabel, catInfo);
					}
					catInfo.addIcd10Code(new ICD10Reference(icd10Code, icd10Label));
				}
			}

			System.out.println("Done!");
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}


	private static void writeCategoryInfoToModel(
			Collection<CategoryInfo> categoryInfo, OWLModel owlModel) {
		RDFProperty propSortingLabel = owlModel.getRDFProperty(ICDContentModelConstants.SORTING_LABEL_PROP);
		RDFProperty propICD10Ref = owlModel.getRDFProperty(PROPERTY_ICD10_REFERENCE);

        RDFProperty propBpShortTerm = owlModel.getRDFProperty(ICDContentModelConstants.BP_SHORT_TERM_ID_PROP);
        RDFProperty propBpOntologyLabel = owlModel.getRDFProperty(ICDContentModelConstants.BP_ONTOLOGY_LABEL_PROP);
        RDFProperty propBpOntologyId = owlModel.getRDFProperty(ICDContentModelConstants.BP_ONTOLOGY_ID_PROP);
        RDFProperty propUrl = owlModel.getRDFProperty(ICDContentModelConstants.URL_PROP);
        RDFProperty propOntologyId = owlModel.getRDFProperty(ICDContentModelConstants.ONTOLOGYID_PROP);
        RDFProperty propTermId = owlModel.getRDFProperty(ICDContentModelConstants.TERM_ID_PROP);
        RDFProperty propLabel = owlModel.getRDFProperty(ICDContentModelConstants.LABEL_PROP);

        RDFSNamedClass clsICD10RefTerm = owlModel.getRDFSNamedClass(CLASS_ICD10_REFERENCE_TERM);

	    ICDContentModel icdContentModel = new ICDContentModel(owlModel);
        Collection<String> superClses = CollectionUtilities.createCollection(ICDContentModelConstants.EXTERNAL_CAUSES_TOP_CLASS);

        for (CategoryInfo catInfo : categoryInfo) {
            RDFSNamedClass category = icdContentModel.createICDCategory(null, superClses);
            category.setPropertyValue(owlModel.getRDFSLabelProperty(), catInfo.getFullLabel());

            category.setPropertyValue(propSortingLabel, catInfo.getSortingLabel());

            RDFResource titleTerm = icdContentModel.createTitleTerm();
            icdContentModel.fillTerm(titleTerm, null,  catInfo.getTitle(), null);
            icdContentModel.addTitleTermToClass(category, titleTerm);

            List<ICD10Reference> icd10Codes = catInfo.getIcd10Codes();
            if (icd10Codes != null) {
	            for (ICD10Reference icd10Code : icd10Codes) {
	            	RDFResource icd10Ref = clsICD10RefTerm.createInstance(null);
	            	icd10Ref.setPropertyValue(propBpOntologyId, ICD10_BP_ONTOLOGY_VERSION_ID);
	            	icd10Ref.setPropertyValue(propBpOntologyLabel, ICD10_BP_ONTOLOGY_LABEL);
	            	icd10Ref.setPropertyValue(propBpShortTerm, icd10Code.code);
	            	icd10Ref.setPropertyValue(propLabel, icd10Code.label);
	            	icd10Ref.setPropertyValue(propOntologyId, ICD10_ONTOLOGY_ID);
	            	icd10Ref.setPropertyValue(propTermId, ICD10_TERM_ID_PREFIX + icd10Code.code);
	            	icd10Ref.setPropertyValue(propUrl, ICD10_URL_PREFIX + icd10Code.code);

	            	category.addPropertyValue(propICD10Ref, icd10Ref);
	            }
            }
		}

	}


	static class ICD10Reference {
		String code;
		String label;

		public ICD10Reference (String icd10code, String label) {
			this.code = icd10code;
			this.label = label;
		}
	}


	static class ICECIReference implements Comparable<ICECIReference> {
		String code;
		String label;

		public ICECIReference (String iceciCode, String label) {
			this.code = iceciCode;
			this.label = label;
		}

		public int compareTo(ICECIReference other) {
			return this.code.compareTo(other.code);
		}
	}


	static class CategoryInfo implements Comparable<CategoryInfo> {
		private static final String CODE_SEPARATOR = ". ";

		private String sortingLabel;
		private String title;
		private String parentLabel;
		private String description;
		private List<ICD10Reference> icd10Codes;
		private List<ICECIReference> intentCodes;
		private List<ICECIReference> intentDescriptorCodes;
		private List<ICECIReference> mechanismCodes;
		private List<ICECIReference> mechanismDetailsCodes;
		private List<ICECIReference> objectCodes;
		private List<ICECIReference> placeCodes;
		private List<ICECIReference> activityCodes;
		private List<ICECIReference> substanceUseCodes;

		public CategoryInfo(String sortingLabel, String title) {
			this.sortingLabel = sortingLabel;
			this.title = title;
		}

		public CategoryInfo(String label) {
			int posEndOfCode = label.indexOf(CODE_SEPARATOR);
			if (posEndOfCode > 0) {
				sortingLabel = label.substring(0, posEndOfCode);
				title = label.substring(posEndOfCode + CODE_SEPARATOR.length());
			}
			else {
				sortingLabel = "";
				title = label;
			}
		}

		public String getSortingLabel() {
			return sortingLabel;
		}
		public String getTitle() {
			return title;
		}
		public String getFullLabel() {
			return sortingLabel + CODE_SEPARATOR + title;
		}
		public void setParentLabel(String parentLabel) {
			this.parentLabel = parentLabel;
		}
		public String getParentLabel() {
			return parentLabel;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getDescription() {
			return description;
		}
		private void removeAllIcd10Codes() {
			this.icd10Codes = new ArrayList<ICD10Reference>();
		}
		public void addIcd10Code(ICD10Reference icd10Code) {
			if (this.icd10Codes == null) {
				removeAllIcd10Codes();
			}
			if (icd10Code != null) {
				if (icd10Code.code != null && icd10Code.code.trim().length() > 0) {
					this.icd10Codes.add(icd10Code);
				}
			}
		}
		public List<ICD10Reference> getIcd10Codes() {
			return icd10Codes;
		}
		//intentCodes
		private void removeAllIntentCodes() {
			this.intentCodes = new ArrayList<ICECIReference>();
		}
		public void addIntentCode(ICECIReference intentCode) {
			if (this.intentCodes == null) {
				removeAllIntentCodes();
			}
			if (intentCode != null) {
				if (intentCode.code != null && intentCode.code.trim().length() > 0) {
					this.intentCodes.add(intentCode);
				}
			}
		}
		public List<ICECIReference> getIntentCodes() {
			return intentCodes;
		}
		//intentDescriptorCodes
		private void removeAllIntentDescriptorCodes() {
			this.intentDescriptorCodes = new ArrayList<ICECIReference>();
		}
		public void addIntentDescriptorCode(ICECIReference intentDescriptorCode) {
			if (this.intentDescriptorCodes == null) {
				removeAllIntentDescriptorCodes();
			}
			if (intentDescriptorCode != null) {
				if (intentDescriptorCode.code != null && intentDescriptorCode.code.trim().length() > 0) {
					this.intentDescriptorCodes.add(intentDescriptorCode);
				}
			}
		}
		public List<ICECIReference> getIntentDescriptorCodes() {
			return intentDescriptorCodes;
		}
		//mechanismCodes
		private void removeAllMechanismCodes() {
			this.mechanismCodes = new ArrayList<ICECIReference>();
		}
		public void addMechanismCode(ICECIReference mechanismCode) {
			if (this.mechanismCodes == null) {
				removeAllMechanismCodes();
			}
			if (mechanismCode != null) {
				if (mechanismCode.code != null && mechanismCode.code.trim().length() > 0) {
					this.mechanismCodes.add(mechanismCode);
				}
			}
		}
		public List<ICECIReference> getMechanismCodes() {
			return mechanismCodes;
		}
		//mechanismDetailsCodes
		private void removeAllMechanismDetailsCodes() {
			this.mechanismDetailsCodes = new ArrayList<ICECIReference>();
		}
		public void addMechanismDetailsCode(ICECIReference mechanismDetailsCode) {
			if (this.mechanismDetailsCodes == null) {
				removeAllMechanismDetailsCodes();
			}
			if (mechanismDetailsCode != null) {
				if (mechanismDetailsCode.code != null && mechanismDetailsCode.code.trim().length() > 0) {
					this.mechanismDetailsCodes.add(mechanismDetailsCode);
				}
			}
		}
		public List<ICECIReference> getMechanismDetailsCodes() {
			return mechanismDetailsCodes;
		}
		//objectCodes
		private void removeAllObjectCodes() {
			this.objectCodes = new ArrayList<ICECIReference>();
		}
		public void addObjectCode(ICECIReference objectCode) {
			if (this.objectCodes == null) {
				removeAllObjectCodes();
			}
			if (objectCode != null) {
				if (objectCode.code != null && objectCode.code.trim().length() > 0) {
					this.objectCodes.add(objectCode);
				}
			}
		}
		public List<ICECIReference> getObjectCodes() {
			return objectCodes;
		}
		//placeCodes
		private void removeAllPlaceCodes() {
			this.placeCodes = new ArrayList<ICECIReference>();
		}
		public void addPlaceCode(ICECIReference placeCode) {
			if (this.placeCodes == null) {
				removeAllPlaceCodes();
			}
			if (placeCode != null) {
				if (placeCode.code != null && placeCode.code.trim().length() > 0) {
					this.placeCodes.add(placeCode);
				}
			}
		}
		public List<ICECIReference> getPlaceCodes() {
			return placeCodes;
		}
		//activityCodes
		private void removeAllActivityCodes() {
			this.activityCodes = new ArrayList<ICECIReference>();
		}
		public void addActivityCode(ICECIReference activityCode) {
			if (this.activityCodes == null) {
				removeAllActivityCodes();
			}
			if (activityCode != null) {
				if (activityCode.code != null && activityCode.code.trim().length() > 0) {
					this.activityCodes.add(activityCode);
				}
			}
		}
		public List<ICECIReference> getActivityCodes() {
			return activityCodes;
		}
		//substanceUseCodes
		private void removeAllSubstanceUseCodes() {
			this.substanceUseCodes = new ArrayList<ICECIReference>();
		}
		public void addSubstanceUseCode(ICECIReference substanceUseCode) {
			if (this.substanceUseCodes == null) {
				removeAllSubstanceUseCodes();
			}
			if (substanceUseCode != null) {
				if (substanceUseCode.code != null && substanceUseCode.code.trim().length() > 0) {
					this.substanceUseCodes.add(substanceUseCode);
				}
			}
		}
		public List<ICECIReference> getSubstanceUseCodes() {
			return substanceUseCodes;
		}

		@Override
        public String toString() {
			String res = getFullLabel();
			//add comment
			res += " (" +
				(description != null && description.length()>30 ? description.substring(0, 25)+"..." : description) +
				")";
			//add icd 10 codes
			res += " " + icd10ReferenceList2String(icd10Codes);
			//add all the iceci references
			String iceciRefs = iceciReferenceList2String(intentCodes) +
					iceciReferenceList2String(intentDescriptorCodes) +
					iceciReferenceList2String(mechanismCodes) +
					iceciReferenceList2String(mechanismDetailsCodes) +
					iceciReferenceList2String(objectCodes) +
					iceciReferenceList2String(placeCodes) +
					iceciReferenceList2String(activityCodes) +
					iceciReferenceList2String(substanceUseCodes);
			res += " " + iceciRefs.replaceAll("\\]\\[", " / ");
			return res;
		}

		private String icd10ReferenceList2String(List<ICD10Reference> list) {
			String res = "[";
			boolean first = true;
			if (list != null) {
				for (ICD10Reference icd10Ref : list) {
					if (first) {
						first = false;
					}
					else {
						res += ", ";
					}
					res += icd10Ref.code;
				}
			}
			res += "]";
			return res;
		}

		private String iceciReferenceList2String(List<ICECIReference> list) {
			String res = "[";
			boolean first = true;
			if (list != null) {
				for (ICECIReference iceciRef : list) {
					if (first) {
						first = false;
					}
					else {
						res += ", ";
					}
					res += iceciRef.code;
				}
			}
			res += "]";
			return res;
		}

		public int compareTo(CategoryInfo other) {
			int res = this.parentLabel.compareTo(other.parentLabel);
			if (res == 0) {
				res = this.sortingLabel.compareTo(other.sortingLabel);
				if (res == 0) {
					res = (this.icd10Codes == null && other.icd10Codes == null ? 0 :
							this.icd10Codes == null ? -1 :
								other.icd10Codes == null ? 1 :
									this.icd10Codes.size() - other.icd10Codes.size() );
					if (res == 0) {
						res = this.description.compareTo(other.description);
					}
				}
			}
			return res;
		}
	}

}
