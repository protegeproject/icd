package edu.stanford.bmir.icd.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class AddTabulationListLinearizations {
	private static final String EXCEL_FILE_SPEC_MORTALITY_LIST = "resources/xls/Mortlistsmatching_v4.xls";
	private static final String FIRST_EXCEL_FILE_VERSION = "1";
	private static final String SECOND_EXCEL_FILE_VERSION = "2";
	
	private static final String CLASS_CH97_SPECIAL_TABULATION_LIST_FOR_MORTALITY_AND_MORBIDITY = "http://who.int/icd#3264_98ae51f9_6af8_41ef_b732_06c5fb864693";
	private static final String CLASS_TABULATION_LIST_FOR_MORTALITY = "http://who.int/icd#7428_890e173e_8bc7_44a1_9345_a738531fdd8e";
	private static final String CLASS_TABULATION_LIST_FOR_MORBIDITY = "http://who.int/icd#3324_98ae51f9_6af8_41ef_b732_06c5fb864693";
	private static final String CLASS_TABULATION_LIST_VERBAL_AUTOPSY = "http://who.int/icd#3336_98ae51f9_6af8_41ef_b732_06c5fb864693";
	
	private static final String[] tabulationListNames = new String[] {"Tabulation_List_M1", "Tabulation_List_M2", "Tabulation_List_M3", "Tabulation_List_M4", "Tabulation_List_Mb", "Tabulation_List_Verbal_Autopsy"};
	
	private static File xlFileSpecMortalityList = new File(EXCEL_FILE_SPEC_MORTALITY_LIST);
	private static String xlFileVersion = SECOND_EXCEL_FILE_VERSION;

    private static Map<String, LinearizationInfo> linearizationInfoMap;
	
    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;
	//private static RDFSNamedClass linearizationMetaClass;

    private static RDFSNamedClass historicLinearizationSpecificationClass;
    private static RDFProperty icd10TabulationListProp;
    private static RDFProperty linearizationViewProp;
    private static RDFProperty isIncludedInLinearizationProp;
    private static RDFProperty isGroupingProp;
    private static RDFProperty linearizationSortingLabelProp;
    private static RDFProperty sortingLabelProp;

	private static ArrayList<OWLIndividual> linearizationViewsForTabLists;

    
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: AddTabulationListLinearizations pprjFileName xlFileSpecMortalityList xlFileVersion");
            return;
        }

        Collection<?> errors = new ArrayList<Object>();
        Project prj = Project.loadProjectFromFile(args[0], errors);
        if (errors != null) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }
		xlFileSpecMortalityList = new File(args[1]);
		xlFileVersion = args[2];

		Log.getLogger().info("\n===== Starting import from Excel " + new Date());
		Log.getLogger().info("=== PPRJ File: " + args[0]);
		Log.getLogger().info("=== Excel file no mortality: " + xlFileSpecMortalityList);
		Log.getLogger().info("=== Excel file version: " + xlFileVersion);

		linearizationInfoMap = readExcelFile(xlFileSpecMortalityList, xlFileVersion);
		System.out.println(linearizationInfoMap);

        owlModel = (OWLModel) prj.getKnowledgeBase();

        if (owlModel == null) {
            System.out.println("Failed");
            return;
        }
        icdContentModel = new ICDContentModel(owlModel);

        //linearizationMetaClass = icdContentModel.getLinearizationMetaClass();
        historicLinearizationSpecificationClass = icdContentModel.getLinearizationHistoricSpecificationClass();
        
        icd10TabulationListProp = icdContentModel.getLinearizationICD10TabulationProperty();
        linearizationViewProp = icdContentModel.getLinearizationViewProperty();
        isIncludedInLinearizationProp = icdContentModel.getIsIncludedInLinearizationProperty();
        isGroupingProp = icdContentModel.getIsGroupingProperty();
        linearizationSortingLabelProp = icdContentModel.getLinearizationSortingLabelProperty();
        sortingLabelProp = icdContentModel.getSortingLabelProperty();

        linearizationViewsForTabLists = new ArrayList<OWLIndividual>();
        for (String tabListName : tabulationListNames) {
        	linearizationViewsForTabLists.add(owlModel.getOWLIndividual(ICDContentModelConstants.NS + tabListName));	
		}

        fixLinearizations();
    }

    private static void fixLinearizations() {
        long t0 = System.currentTimeMillis();

        Log.getLogger().setLevel(Level.FINE);

        owlModel.setGenerateEventsEnabled(false);
        //--------------------
        System.out.println("Preparing mortality tabulation lists");
        RDFSNamedClass tabListMtCls = icdContentModel.getICDCategory(CLASS_TABULATION_LIST_FOR_MORTALITY);

        if (tabListMtCls != null) {
        	addHistoricLinearizationSpecification(tabListMtCls, 0, 1, null, new Integer[]{0, 1, 2, 3}, null);
        }
        else {
        	Log.getLogger().warning("Did not find class \"Tabulation list for mortality\": " + CLASS_TABULATION_LIST_FOR_MORTALITY);
        }
        System.out.println("Time: " + (System.currentTimeMillis() - t0) /1000 + " sec");

        //--------------------
        System.out.println("Preparing morbidity tabulation list");
        RDFSNamedClass tabListMbCls = icdContentModel.getICDCategory(CLASS_TABULATION_LIST_FOR_MORBIDITY);

        if (tabListMbCls != null) {
        	addHistoricLinearizationSpecification(tabListMbCls, 0, 1, null, new Integer[]{4}, null);
        }
        else {
        	Log.getLogger().warning("Did not find class \"Tabulation list for morbidity\": " + CLASS_TABULATION_LIST_FOR_MORBIDITY);
        }
        System.out.println("Time: " + (System.currentTimeMillis() - t0) /1000 + " sec");

        //--------------------
        System.out.println("Preparing verbal autopsy tabulation list");
        RDFSNamedClass tabListVACls = icdContentModel.getICDCategory(CLASS_TABULATION_LIST_VERBAL_AUTOPSY);

        if (tabListVACls != null) {
        	addHistoricLinearizationSpecification(tabListVACls, 0, 2, "VA-", new Integer[]{5}, null);
        }
        else {
        	Log.getLogger().warning("Did not find class \"Tabulation list for verbal autopsy\": " + CLASS_TABULATION_LIST_VERBAL_AUTOPSY);
        }
        
        for (Handler logHandler : Log.getLogger().getHandlers()) {
        	logHandler.flush();
        }
        System.out.println("Time: " + (System.currentTimeMillis() - t0) /1000 + " sec");
        Log.getLogger().info("Done!");
    }


    private static void addHistoricLinearizationSpecification(
			RDFSNamedClass crtCls, int crtLevel, int maxLevel, 
			String sortingLabelPrefix, Integer[] includeInLinearizations, LinearizationInfo linInfo) {
		if (crtLevel <= maxLevel ) {
			String conceptSortingLabel = (String) crtCls.getPropertyValue(sortingLabelProp);
			if (crtLevel == 0 || sortingLabelPrefix == null ||
					(conceptSortingLabel != null && conceptSortingLabel.startsWith(sortingLabelPrefix))) {
				addHistoricLinearizationSpecification(crtCls, Arrays.asList(includeInLinearizations), crtLevel==1, linInfo);
			}
			else {
				Log.getLogger().info("Sorting label of '" + crtCls.getBrowserText() + "' did not match sorting label prefix: " + sortingLabelPrefix);
			}
				
			Collection<?> subclasses = crtCls.getSubclasses(false);
			int n = subclasses.size();
			int i = 0;
			for (Object child : subclasses) {
				if (crtLevel == 0) {
					System.out.println("" + (++i) + "/" + n + " (" + i*100/n + "%)");
				}
				
				if (child instanceof RDFSNamedClass) {
					RDFSNamedClass childCls = (RDFSNamedClass) child;
					LinearizationInfo childLinInfo = linInfo;
					if (childLinInfo == null) {
						String childTitle = childCls.getBrowserText();
						childTitle = childTitle.trim().toLowerCase();
						if (childTitle.startsWith("'") && childTitle.endsWith("'")) {
							childTitle = childTitle.substring(1, childTitle.length() - 1);
						}
						childLinInfo = linearizationInfoMap.get(childTitle);
						if (childLinInfo == null && crtLevel == 0) { //i.e. child level == 1
							Log.getLogger().warning("Could not find Excel table entry for: " + childTitle);
						}
					}
					addHistoricLinearizationSpecification(childCls, crtLevel + 1, maxLevel, 
							sortingLabelPrefix, includeInLinearizations, childLinInfo);
				}
				else {
					Log.getLogger().warning("Invalid sublcass of " + crtCls + ": " + child + " is not an RDFSNamedClass!");
				}
			}
		}
	}

	private static void addHistoricLinearizationSpecification(RDFSNamedClass crtCls, 
			List<Integer> includeInLinearizations, boolean isGrouping, LinearizationInfo linInfo) {
		Log.getLogger().info("Adding historic LinearizationSpecifications to " + crtCls.getBrowserText() + "("  + crtCls + "): " + linInfo);
		
		for (int i = 0; i < linearizationViewsForTabLists.size(); i++) {
			OWLIndividual linView = linearizationViewsForTabLists.get(i);
			
			RDFResource linSpec = historicLinearizationSpecificationClass.createInstance(null);
			linSpec.addPropertyValue(linearizationViewProp, linView);
			if (linInfo != null) {
				String codeTabList = linInfo.getTabListCode(i);
				if (codeTabList != null && codeTabList.length() > 0) {
					linSpec.setPropertyValue(isIncludedInLinearizationProp, true);
					linSpec.addPropertyValue(linearizationSortingLabelProp, codeTabList);
					linSpec.addPropertyValue(isGroupingProp, isGrouping);
				}
				else {
					linSpec.setPropertyValue(isIncludedInLinearizationProp, false);
				}
			}
			else {
				//we don't have linInfo - use the linearization indexes to decide in which linearizations to include this concept
				if (includeInLinearizations.contains(i)) {
					String codeTabList = (String) crtCls.getPropertyValue(sortingLabelProp);
					linSpec.setPropertyValue(isIncludedInLinearizationProp, true);
					linSpec.addPropertyValue(isGroupingProp, isGrouping);
					if (codeTabList != null) {
						linSpec.addPropertyValue(linearizationSortingLabelProp, codeTabList);
					}
				}
				else {
					linSpec.setPropertyValue(isIncludedInLinearizationProp, false);
				}
			}
			
			crtCls.addPropertyValue(icd10TabulationListProp, linSpec);
		}
		
//		for (int i = 0; i < linearizationViewsForIcd10Linearizations.size(); i++) {
//			OWLIndividual linView = linearizationViewsForIcd10Linearizations.get(i);
//			
//			RDFResource linSpec = historicLinearizationSpecificationClass.createInstance(null);
//			linSpec.addPropertyValue(linearizationViewProp, linView);
//			crtCls.addPropertyValue(icd10LinearizationProp, linSpec);
//		}
	}



	private static Map<String, LinearizationInfo> readExcelFile(File excelFile, String xlFileVersion) {
		LinkedHashMap<String, LinearizationInfo> result = new LinkedHashMap<String, LinearizationInfo>();
		
		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);
			Sheet sh = wb.getSheet(0);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)
				
				//if (r<10 || r>sh.getRows()-10) //JUST FOR TEST
				{
					if (FIRST_EXCEL_FILE_VERSION.equals(xlFileVersion)) {
						String col1 = sh.getCell(0,r).getContents();
						String col2 = sh.getCell(1,r).getContents();
						String titleLabel = col1 + " " + col2;
						
						String colTL1 = sh.getCell(2,r).getContents();
						String colTL2 = sh.getCell(3,r).getContents();
						String colTL3 = sh.getCell(4,r).getContents();
						String colTL4 = sh.getCell(5,r).getContents();
	
						LinearizationInfo linearizationInfo = new LinearizationInfo(titleLabel, colTL1, colTL2, colTL3, colTL4);
						result.put(titleLabel.trim().toLowerCase(), linearizationInfo);
					}
					else if (SECOND_EXCEL_FILE_VERSION.equals(xlFileVersion)) {
						String titleLabel = sh.getCell(1,r).getContents();
						
						String colTL1 = sh.getCell(3,r).getContents();
						String colTL2 = sh.getCell(4,r).getContents();
						String colTL3 = sh.getCell(5,r).getContents();
						String colTL4 = sh.getCell(6,r).getContents();
	
						LinearizationInfo linearizationInfo = new LinearizationInfo(titleLabel, colTL1, colTL2, colTL3, colTL4);
						result.put(titleLabel.trim().toLowerCase(), linearizationInfo);
					}
					else {
						Log.getLogger().warning("WRONG EXCEL FILE VERSION: " + xlFileVersion);
					}
				}
			}
			System.out.println("Done with reading excel file!");
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	private static class LinearizationInfo {
		String label;

		String[] tabListCodes = new String[tabulationListNames.length];
		
		public LinearizationInfo() {
		}
		
		public LinearizationInfo(String label) {
			this();
			setLabel(label);
		}
		
		public LinearizationInfo(String label, String codeTL1, String codeTL2, String codeTL3, String codeTL4) {
			this(label);
			setTabListCode(1, codeTL1);
			setTabListCode(2, codeTL2);
			setTabListCode(3, codeTL3);
			setTabListCode(4, codeTL4);
		}
		
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		
		public String[] getTabListCodes() {
			return tabListCodes;
		}
		public String getTabListCode(int i) {
			return tabListCodes[i];
		}
		public void setTabListCode(int i, String codeTabListI) {
			this.tabListCodes[i-1] = codeTabListI;
		}
		
		@Override
		public String toString() {
			String res = "";
			res += getLabel();
			res += " ";
			res += Arrays.toString(getTabListCodes());
			return res;
		}
	}
	
}
