package edu.stanford.bmir.icd.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

/**
 * @author csnyulas
 *
 */
public class ImportTAGsAndStatus {

	private static final String PPRJ_FILE_URI = "resources/projects/icd/icd_umbrella.pprj";
	private static final String EXCEL_FILE_TAGS_AND_STATUS = "resources/xls/TAGs.xls";

	private static final String EXCEL_SHEET_TO_IMPORT = "List - Prepared";

	private static URI pprjFileUri = new File(PPRJ_FILE_URI).toURI();
	private static File xlFileTAGsAndStatus = new File(EXCEL_FILE_TAGS_AND_STATUS);

	private static final boolean TEST_RUN = false;
	private static final boolean OVERWRITE_STATUS = false;
	private static Set<String> catIdSet = new HashSet<String>();
	private static Map<String, RDFResource> valueId2IndMap = new HashMap<String, RDFResource>();


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//read arguments
		if (args.length < 2) {
			Log.getLogger().info("Usage: " +
			"ImportTAGsAndStatus pprjFile xlTAGs");
			return;
		}
		else {
			pprjFileUri = new File(args[0]).toURI();
			xlFileTAGsAndStatus = new File(args[1]);
		}

		SystemUtilities.logSystemInfo();
		Log.getLogger().info("\n===== Started import from Excel " + new Date());
		Log.getLogger().info("=== PPRJ File: " + pprjFileUri);
		Log.getLogger().info("=== Excel file for TAGs and status: " + xlFileTAGsAndStatus);

		//open owl model
		OWLModel owlModel = ImportUtils.openOWLModel(pprjFileUri);

		//call migration functions
		//fixXlsContent(xlFileTAGsAndStatus);
		Map<String, CategoryInfo> categoryInfoMap = extractCategoryInfoFromXls(owlModel, xlFileTAGsAndStatus);
		//print out content to be imported
		List<CategoryInfo> catInfos = new ArrayList<CategoryInfo>(categoryInfoMap.values());
		Collections.sort(catInfos);
		for (CategoryInfo catInfo : catInfos) {
//			if (! catInfo.getStatus().contains(CategoryInfo.ICD_NS)) {
//				System.out.print(catInfo.getStatus() + ": ");
				System.out.println(catInfo);
//			}
			catIdSet.add(catInfo.getId());
		}
		//checkForProblems(catInfos);

		writeCategoryInfoToModel(catInfos, owlModel);
		removeRedundantSecondaryTAGs(catInfos, owlModel);

		//TODO check whether we need this or not
		writeChangesToChao(owlModel);

		//finish processing
		Log.getLogger().info("\n===== End import from Excel at " + new Date());
	}



	// ------------ Extract Category Info ------------- //


	private static Map<String, CategoryInfo> extractCategoryInfoFromXls(
			OWLModel owlModel, File excelFile) {

		Log.getLogger().info("\nImporting values TAGs and display statuses from Excel file... ");

		Map<String, CategoryInfo> res = new HashMap<String, CategoryInfo>();

		try {
			Workbook wb = jxl.Workbook.getWorkbook(excelFile);

			Sheet sh = wb.getSheet(EXCEL_SHEET_TO_IMPORT);
			for (int r =1; r < sh.getRows(); r++) { //skip first line (0)

				if ((!TEST_RUN) || (TEST_RUN && (r<10 || r>sh.getRows()-10))) //IF TEST ONLY
				{
					String id = sh.getCell(0,r).getContents();

					if (id != null && id.length() > 0) {

						String primaryTAG = sh.getCell(7,r).getContents();
						String secondaryTAG1 = sh.getCell(8,r).getContents();
						String secondaryTAG2 = sh.getCell(9,r).getContents();
						String secondaryTAG3 = sh.getCell(10,r).getContents();
						String status = sh.getCell(11,r).getContents();

						CategoryInfo catInfo = res.get(id);
						if (catInfo == null) {
							catInfo = new CategoryInfo(id);
							res.put(id, catInfo);
						}
						else {
							Log.getLogger().warning("Duplicate concept id in row " + r + ": " + id);
						}

						catInfo.setPrimaryTAG(primaryTAG);
						catInfo.addSecondaryTAG(secondaryTAG1);
						catInfo.addSecondaryTAG(secondaryTAG2);
						catInfo.addSecondaryTAG(secondaryTAG3);
						catInfo.setStatus(status);
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


	// ------------ Write Category Info to OWL Model ------------- //


	private static void writeCategoryInfoToModel(
			Collection<CategoryInfo> categoryInfo, OWLModel owlModel) {
		Logger logger = Log.getLogger();
		logger.info("\nWrite category information to model... ");

	    ICDContentModel icdContentModel = new ICDContentModel(owlModel);

		RDFProperty propStatus = icdContentModel.getDisplayStatusProperty();
		RDFProperty propPrimaryTAG = icdContentModel.getAssignedPrimaryTagProperty();
		RDFProperty propSecondaryTAG = icdContentModel.getAssignedSecondaryTagProperty();

        for (CategoryInfo catInfo : categoryInfo) {
        	String id = catInfo.getId();
			RDFSNamedClass category = icdContentModel.getICDCategory(id);

			if (id == null || category == null) {
				logger.severe("Category could not be retrieved for id: " + id);
				continue;
			}

			Object oldStatus = category.getPropertyValue(propStatus);
			RDFResource newStatus = getResource(owlModel, catInfo.getStatus());
			if (oldStatus != null && (! oldStatus.equals(newStatus))) {
				if (OVERWRITE_STATUS) {
					logger.warning("Overwriting status of " + id + ". Current value: " + oldStatus + ". New value: " + newStatus);
					category.setPropertyValue(propStatus, newStatus);
				}
				else {
					logger.warning("Status of " + id + " is already set to: " + oldStatus + ". New value: " + newStatus + " will be ignored.");
				}
			}
			else {
				category.setPropertyValue(propStatus, newStatus);
			}

			RDFResource oldPrimaryTag = icdContentModel.getAssignedPrimaryTag(category);
            RDFResource newPrimaryTag = getResource(owlModel, catInfo.getPrimaryTAG());
			if (oldPrimaryTag != null && (! oldPrimaryTag.equals(newPrimaryTag))) {
				logger.warning("Overwriting primary TAG of " + id + ". Current value: " + oldPrimaryTag + ". New value: " + newPrimaryTag);
			}
			category.setPropertyValue(propPrimaryTAG, newPrimaryTag);

			Collection<RDFResource> oldSecondaryTags = new ArrayList<RDFResource>(icdContentModel.getAssignedSecondaryTags(category));
			List<RDFResource> newSecondaryTags = getResourceList(owlModel, catInfo.getSecondaryTAGs());
			if (oldSecondaryTags != null) {
				//setMinus - keep only those old sec. TAGs that are not present in the new sec. TAG list
				oldSecondaryTags.retainAll(catInfo.getSecondaryTAGs());
				if (! oldSecondaryTags.isEmpty()) {
					logger.warning("Overwriting the following secondary TAGs of " + id + ": " + oldSecondaryTags);
				}
			}
            category.setPropertyValues(propSecondaryTAG, newSecondaryTags);
		}

        logger.info("Done!");
	}


	private static RDFResource getResource(OWLModel owlModel, String uri) {
		RDFResource res = valueId2IndMap.get(uri);
		if (res == null) {
			res = owlModel.getRDFResource(uri);
			if (res != null) {
				valueId2IndMap.put(uri, res);
			}
			else {
				Log.getLogger().warning("There is no resource found with name: " + uri);
			}
		}
		return res;
	}

	private static List<RDFResource> getResourceList(OWLModel owlModel, List<String> uris) {
		List<RDFResource> res = new ArrayList<RDFResource>();
		for (String uri : uris) {
			RDFResource resource = getResource(owlModel, uri);
			if (resource != null) {
				res.add(resource);
			}
		}
		return res;
	}


	// ------------ Remove redundant (i.e. already inherited) secondary TAGs ------------- //


	private static void removeRedundantSecondaryTAGs(
			Collection<CategoryInfo> categoryInfo, OWLModel owlModel) {
		Logger logger = Log.getLogger();
		logger.info("\nRemove redundant secondary TAGs... ");

	    ICDContentModel icdContentModel = new ICDContentModel(owlModel);

		RDFProperty propSecondaryTAG = icdContentModel.getAssignedSecondaryTagProperty();

        for (CategoryInfo catInfo : categoryInfo) {
        	String id = catInfo.getId();
			RDFSNamedClass category = icdContentModel.getICDCategory(id);

			if (id == null || category == null) {
				logger.severe("Category could not be retrieved for id: " + id);
				continue;
			}

			Map<RDFResource, List<RDFSNamedClass>> involvedTags = icdContentModel.getInvolvedTags(category);
			Collection<RDFResource> localSecondaryTAGs = icdContentModel.getAssignedSecondaryTags(category);
			if (localSecondaryTAGs != null) {
				for (RDFResource localSecondaryTAG : localSecondaryTAGs) {
					List<RDFSNamedClass> tagAssignedAt = involvedTags.get(localSecondaryTAG);
					//if local secondary TAG was assigned at more than one classes (i.e. it is also inherited from some of the parent categories)
					if (tagAssignedAt.size() > 1) {
						logger.info("Removing secondary TAG from " + id + ": " + localSecondaryTAG);
						category.removePropertyValue(propSecondaryTAG, localSecondaryTAG);
					}
				}
			}
		}

        logger.info("Done!");
	}

	private static void writeChangesToChao(OWLModel owlModel) {
		KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(owlModel);
		if (chaoKb == null) {
			Log.getLogger().info("Could not find ChAO for: " + owlModel);
			return;
		}

		Log.getLogger().info("Starting writing to ChAO from: " + chaoKb.getProject().getProjectURI() + " on " + new Date());

		ChangesProject.initialize(owlModel.getProject());

		ChangeFactory changeFactory = new ChangeFactory(chaoKb);
		OntologyComponentFactory ocFactory = new OntologyComponentFactory(chaoKb);
		PostProcessorManager changes_db = ChangesProject.getPostProcessorManager(owlModel);

		for (String id : catIdSet) {
			Cls cls = owlModel.getCls(id);
			if (cls == null) {
				Log.getLogger().warning("Writing to ChAO: Could not find class " + id);
			}
			else {
				Change change = changeFactory.createComposite_Change(null);
				ServerChangesUtil.createChangeStd(changes_db, change, cls, "Automatic initial import of assigned TAGs and status");
				change.setAuthor("WHO");
			}
		}

	}


	static class CategoryInfo implements Comparable<CategoryInfo> {
		private static final String ICD_NS = "http://who.int/icd#";
		private static final String ICD_PREFIX = "icd:";

		private String id;
		private String status;
		private String primaryTAG;
		private List<String> secondaryTAGs;

		public CategoryInfo(String id) {
			this.id = id;
		}


		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getPrimaryTAG() {
			return primaryTAG;
		}
		public void setPrimaryTAG(String primaryTAG) {
			this.primaryTAG = primaryTAG;
		}
		public List<String> getSecondaryTAGs() {
			return secondaryTAGs;
		}
		public void setSecondaryTAGs(List<String> secondaryTAGs) {
			this.secondaryTAGs = secondaryTAGs;
		}
		public void addSecondaryTAG(String secondaryTAG) {
			if (this.secondaryTAGs == null) {
				this.secondaryTAGs = new ArrayList<String>();
			}
			if (secondaryTAG != null && secondaryTAG.length() > 0) {
				this.secondaryTAGs.add(secondaryTAG);
			}
		}


		public int compareTo(CategoryInfo other) {
			int res = compare(this.primaryTAG, other.primaryTAG);
			if (res == 0) {
				res = compare(this.secondaryTAGs, other.secondaryTAGs);
				if (res == 0) {
					res = compare(this.status, other.status);
					if (res == 0) {
						res = this.id.compareTo(other.id);
					}
				}
			}
			return res;
		}

		private int compare(String s1, String s2) {
			return (s1 == null && s2 == null ? 0 :
				(s1 == null ? -1 :
					(s2 == null ? 1 :
						s1.compareTo(s2))));
		}

		private int compare(List<String> l1, List<String> l2) {
			int res = (l1 == null && l2 == null ? 0 :
				(l1 == null ? -1 :
					(l2 == null ? 1 : 0)));
			//if none of the lists is null
			if (res == 0 && l1 != null) {
				//compare list lengths
				res = l1.size() - l2.size();
				if (res == 0) {
					for (int i = 0; i < l1.size(); i++) {
						res = compare(l1.get(i), l2.get(i));
						if (res != 0) {
							break;
						}
					}
				}
			}
			return res;
		}

		//--- toString() ---//
		@Override
		public String toString() {
			return id.replaceFirst(ICD_NS, ICD_PREFIX) +
			" - " + primaryTAG.replaceFirst(ICD_NS, ICD_PREFIX) +
			" " + (secondaryTAGs == null ? "null" : secondaryTAGs.toString().replaceAll(ICD_NS, ICD_PREFIX)) +
			" - " + status.replaceFirst(ICD_NS, ICD_PREFIX);
		}
	}
}
