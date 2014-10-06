package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

/**
 * @author csnyulas
 *
 */
public class ImportIndexTerms {

	private static final String PPRJ_FILE_URI = "resources/projects/icd/icd_umbrella.pprj";
	private static final String CSV_FILE_INDEX_TERMS = "resources/xls/index_terms_20120306.csv";

//	private static final String EXCEL_SHEET_TO_IMPORT = "List - Prepared";

	private static URI pprjFileUri = new File(PPRJ_FILE_URI).toURI();
	private static File csvFileIndexTerms = new File(CSV_FILE_INDEX_TERMS);

	private static final boolean TEST_RUN = false;
	private static final boolean DEBUG = false;

	private static final String SEPARATOR = "\t";
	private static final String QUOTE = "\"";

    private final static String ICD_CATEGORY_PROP = ICDContentModelConstants.NS + "icdCategory";
    private static RDFProperty propICDCategory = null;;

	private static final int COLUMN_TEXT = 0;
	private static final int COLUMN_ID = 1;
	private static final int COLUMN_TERM_ID = 2;
	private static final int COLUMN_IS_SYNONYM = 3;
	private static final int COLUMN_IS_INCLUSION = 4;
	private static final int COLUMN_IS_RETIRED = 5;
	private static final int ROW_COUNT = 87624;		//87386;
	//private static Set<String> catIdSet = new HashSet<String>();
//	private static Map<String, RDFResource> valueId2IndMap = new HashMap<String, RDFResource>();


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//read arguments
		if (args.length < 2) {
			Log.getLogger().info("Usage: " +
			"ImportTAGsAndStatus pprjFile csvIntexTerms");
			return;
		}
		else {
			pprjFileUri = new File(args[0]).toURI();
			csvFileIndexTerms = new File(args[1]);
		}

		SystemUtilities.logSystemInfo();
		Log.getLogger().info("\n===== Started import from CSV " + new Date());
		Log.getLogger().info("=== PPRJ File: " + pprjFileUri);
		Log.getLogger().info("=== CSV file for index terms: " + csvFileIndexTerms);

		//open owl model
		OWLModel owlModel = ImportUtils.openOWLModel(pprjFileUri);

		//call migration functions
		//fixXlsContent(csvFileIndexTerms);
		Map<String, CategoryInfo> categoryInfoMap = extractCategoryInfoFromCsv(owlModel, csvFileIndexTerms);
		//print out content to be imported
		List<CategoryInfo> catInfos = new ArrayList<CategoryInfo>(categoryInfoMap.values());
		Collections.sort(catInfos);
		if (DEBUG) {
		  for (CategoryInfo catInfo : catInfos) {
//			if (! catInfo.getStatus().contains(CategoryInfo.ICD_NS)) {
//				System.out.print(catInfo.getStatus() + ": ");
				System.out.println(catInfo);
//			}
			//catIdSet.add(catInfo.getId());
		  }
		}
		//checkForProblems(catInfos);

		writeCategoryInfoToModel(catInfos, owlModel);
		migrateExclusions(categoryInfoMap, owlModel);

		//TODO check whether we need this or not
		writeChangesToChao(categoryInfoMap, owlModel);

		//finish processing
		Log.getLogger().info("\n===== End import from Excel at " + new Date());
	}



	// ------------ Extract Category Info ------------- //


	private static Map<String, CategoryInfo> extractCategoryInfoFromCsv(
			OWLModel owlModel, File csvFile) {

		Log.getLogger().info("\nImporting index terms from CSV file... ");

		Map<String, CategoryInfo> res = new HashMap<String, CategoryInfo>();

		

        try {
        BufferedReader input = new BufferedReader(new FileReader(csvFile));
        String firstLine = input.readLine();
        int colCount = firstLine.split(SEPARATOR).length;

		int r =1;	//first line (0) is already read

        String line = null;
        while ((line = input.readLine()) != null) {
        	r++;
            if (line != null) {
                try {
                    while (!line.endsWith(QUOTE) && !line.endsWith(SEPARATOR) && 
                    		line.split(SEPARATOR).length < colCount) {
                        line = line + "\n" + input.readLine();
                        r++;
                    }
                    
        			if ((!TEST_RUN) || (TEST_RUN && (r<10 || r>ROW_COUNT-10))) //IF TEST ONLY
        			{
        				processLine(line, res);
        			}
                } catch (Exception e) {
                    Log.getLogger().log(Level.WARNING, " Could not read line " + r + ": " + line, e);
                }
            }
        }
		Log.getLogger().info("Done!");
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at parsing csv", e);
        }

		return res;
		}



    private static void processLine(String line, Map<String, CategoryInfo> res) {
        try {
        	if (DEBUG) {
        		Log.getLogger().info(line);
        	}
            final String[] split = line.split(SEPARATOR);
            removeQuotes(split);
            String label = split[COLUMN_TEXT];
            String id = split[COLUMN_ID];
            
			CategoryInfo catInfo = addNewCategoryInfoToMap(res, id, label);

//			catInfo.setLabel(split[COLUMN_TEXT]);
//			catInfo.setId(split[COLUMN_ID]);
			catInfo.setTermId(split[COLUMN_TERM_ID]);
			String isSyn = split[COLUMN_IS_SYNONYM];
			catInfo.setIsSynonym(isSyn == null || isSyn.isEmpty() ? null : Integer.parseInt(isSyn) == 0 ? false : Integer.parseInt(isSyn) == 1 ? true : null);
			String isIncl = split[COLUMN_IS_INCLUSION];
			catInfo.setIsInclusion(isIncl == null || isIncl.isEmpty() ? null : Integer.parseInt(isIncl) == 0 ? false : Integer.parseInt(isIncl) == 1 ? true : null);

            
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at import", e);
        }
    }



	private static CategoryInfo addNewCategoryInfoToMap(Map<String, CategoryInfo> categoryInfoMap,
			String id, String label) {
		CategoryInfo catInfo = categoryInfoMap.get(label);
		if (catInfo == null) {
			catInfo = new CategoryInfo(id, label);
			categoryInfoMap.put(label, catInfo);
		}
		else {
			//Log.getLogger().warning("Duplicate concept id in row " + r + ": " + id);
			Log.getLogger().warning("Duplicate concept id: " + label);
			
			int i = 2;
			String alt_label = label + " (" + i + ")"; 
			while (categoryInfoMap.get(alt_label) != null) {
				i++;
				alt_label = label + " (" + i + ")";
			}
			
			catInfo = new CategoryInfo(id, label);
			categoryInfoMap.put(alt_label, catInfo);
		}
		return catInfo;
	}
    
	private static void removeQuotes(final String[] split) {
		for (int i = 0; i < split.length; i++) {
			if (split[i].startsWith(QUOTE) && split[i].endsWith(QUOTE)) {
				split[i] = split[i].substring(1, split[i].length()-1);
			}
		}
	}



	// ------------ Write Category Info to OWL Model ------------- //


	private static void writeCategoryInfoToModel(
			Collection<CategoryInfo> categoryInfo, OWLModel owlModel) {
		Logger logger = Log.getLogger();
		logger.info("\nWrite category information to model... ");

	    ICDContentModel icdContentModel = new ICDContentModel(owlModel);

	    int i = 0;
        for (CategoryInfo catInfo : categoryInfo) {
        	String id = catInfo.getId();
			RDFSNamedClass category = icdContentModel.getICDCategory(id);

			if (id == null || category == null) {
				logger.severe("Category could not be retrieved for id: " + id);
				continue;
			}

			String idTerm = catInfo.getTermId();
			RDFIndividual term = null;
			if (idTerm == null || idTerm.trim().length() == 0) {
				term = createTermIndividual(icdContentModel, catInfo);
			}
			else {
				term = owlModel.getOWLIndividual(idTerm);
				if (term == null) {
					Log.getLogger().log(Level.WARNING, "Could not find old index term " + idTerm + " for entry: " + catInfo + " (a new term will be created)");
					term = createTermIndividual(icdContentModel, catInfo);
				}
				else {
					convertIndexTermToBaseIndexTerm(icdContentModel, term, catInfo);
				}
			}
			
			updateProperties(icdContentModel, category, term, catInfo);

        	i++;
        	if (i % 1000 == 0) {
        		System.out.println(i);
        	}
		}

        logger.info("Done!");
	}



	private static RDFIndividual createTermIndividual(ICDContentModel icdContentModel, CategoryInfo catInfo) {
		RDFResource term = null;
		RDFSNamedClass termClass;
		
		if (catInfo.isSynonym) {
			termClass = icdContentModel.getTermSynonymClass();
			term = icdContentModel.createTerm(termClass);
			catInfo.setChangeMsg("Added a new 'synonym' base index term: '" + catInfo.getLabel() + "'.");
		}
		else {
			termClass = icdContentModel.getTermNarrowerClass();
			term = icdContentModel.createTerm(termClass);
			catInfo.setChangeMsg("Added a new 'narrower' base index term: '" + catInfo.getLabel() + "'.");
		}
		term.addPropertyValue(icdContentModel.getLabelProperty(), catInfo.getLabel());
		
		if (catInfo.isInclusion) {
			termClass = icdContentModel.getTermBaseInclusionClass();
			term.addRDFType(termClass);
			catInfo.appendChangeMsg(" Also made this term a base inclusion term.");
		}
		
		return (RDFIndividual)term;
	}


	private static void convertIndexTermToBaseIndexTerm(ICDContentModel icdContentModel, 
			RDFIndividual term, CategoryInfo catInfo) {
		Collection<?> oldTypes = term.getRDFTypes();
		Collection<RDFSNamedClass> newTypes = new ArrayList<RDFSNamedClass>();
		RDFSNamedClass termClass;
		
		if (catInfo.isSynonym) {
			termClass = icdContentModel.getTermSynonymClass();
			if ( ! oldTypes.contains(termClass)) {
				term.addRDFType(termClass);
				catInfo.setChangeMsg("Changed the type of term '" + catInfo.getLabel() + "' to a 'synonym' base index term.");
			}
			else {
				//we don't need to log this, because this could be valid type from before-migration
			}
			newTypes.add(termClass);
		}
		else {
			termClass = icdContentModel.getTermNarrowerClass();
			if ( ! oldTypes.contains(termClass)) {
				term.addRDFType(termClass);
				catInfo.setChangeMsg("Changed the type of term '" + catInfo.getLabel() + "' to a 'narrower' base index term.");
			}
			else {
	            Log.getLogger().log(Level.WARNING, "Term " + term + " ('" + catInfo.getLabel() + "') is already of type " + termClass);
			}
			newTypes.add(termClass);
		}
		
		if (catInfo.isInclusion) {
			termClass = icdContentModel.getTermBaseInclusionClass();
			if ( ! oldTypes.contains(termClass)) {
				term.addRDFType(termClass);
				if (catInfo.getChangeMsg().isEmpty()) {
					catInfo.setChangeMsg("Made the 'synonym' base index term '" + catInfo.getLabel() + "' also a base inclusion term.");
				}
				else {
					catInfo.appendChangeMsg(" Also made this term a base inclusion term.");
				}
			}
			else {
	            Log.getLogger().log(Level.WARNING, "Term " + term + " ('" + catInfo.getLabel() + "') is already of type " + termClass);
			}
			newTypes.add(termClass);
		}

		for (Iterator<?> it = oldTypes.iterator(); it.hasNext();) {
			RDFSClass type = (RDFSClass) it.next();
			if ( ! newTypes.contains(type) ) {
				term.removeRDFType(type);
				//TODO checkForICDCategoryPropertyValue(owlModel, exclusion);
			}
		}
	}


	@SuppressWarnings("deprecation")
	private static void updateProperties(ICDContentModel icdContentModel, RDFSNamedClass category,
			RDFIndividual term, CategoryInfo catInfo) {
		Collection<RDFResource> synonyms = icdContentModel.getTerms(category, icdContentModel.getSynonymProperty());
		Collection<RDFResource> inclusions = icdContentModel.getTerms(category, icdContentModel.getInclusionProperty());
		
		Collection<?> currTypes = term.getRDFTypes();
		if (currTypes.contains(icdContentModel.getTermSynonymClass())) {
			if (synonyms.contains(term)) {
				//OK - do nothing
			}
			else {
				icdContentModel.addSynonymTermToClass(category, term);
			}
		}
		
		if (currTypes.contains(icdContentModel.getTermNarrowerClass())) {
			if (synonyms.contains(term)) {
				category.removePropertyValue(icdContentModel.getSynonymProperty(), term);
			}

			icdContentModel.addNarrowerTermToClass(category, term);
		}
		
		if (inclusions.contains(term)) {
			category.removePropertyValue(icdContentModel.getInclusionProperty(), term);
		}
		if (currTypes.contains(icdContentModel.getTermBaseInclusionClass())) {
			icdContentModel.addBaseInclusionTermToClass(category, term);
		}

	}




//	private static RDFResource getResource(OWLModel owlModel, String uri) {
//		RDFResource res = valueId2IndMap.get(uri);
//		if (res == null) {
//			res = owlModel.getRDFResource(uri);
//			if (res != null) {
//				valueId2IndMap.put(uri, res);
//			}
//			else {
//				Log.getLogger().warning("There is no resource found with name: " + uri);
//			}
//		}
//		return res;
//	}
//
//	private static List<RDFResource> getResourceList(OWLModel owlModel, List<String> uris) {
//		List<RDFResource> res = new ArrayList<RDFResource>();
//		for (String uri : uris) {
//			RDFResource resource = getResource(owlModel, uri);
//			if (resource != null) {
//				res.add(resource);
//			}
//		}
//		return res;
//	}


	// ------------ Migrate exclusions to base exclusions ------------- //


	@SuppressWarnings("deprecation")
	private static void migrateExclusions(
			Map<String, CategoryInfo> categoryInfoMap, OWLModel owlModel) {
		Logger logger = Log.getLogger();
		logger.info("\nMigrate Exclusions... ");

	    ICDContentModel icdContentModel = new ICDContentModel(owlModel);
    	RDFProperty propExclusion = icdContentModel.getExclusionProperty();
    	RDFProperty propLabel = icdContentModel.getLabelProperty();
		RDFSNamedClass clsExclusion = icdContentModel.getTermExclusionClass();
    	RDFSNamedClass clsBaseExclusion = icdContentModel.getTermBaseExclusionClass();
    	
	    Collection<RDFSNamedClass> icdCategories = icdContentModel.getICDCategories();
	    logger.info(icdCategories.size() + " categories have been loaded");
	    
	    int i = 0;
	    for (RDFSNamedClass category : icdCategories) {
	    	Collection<RDFResource> exclusions = icdContentModel.getTerms(category, propExclusion);
	    	for (RDFResource exclusion : exclusions) {
	    		//change type
	    		if ( ! exclusion.hasRDFType(clsBaseExclusion) ) {
	    			exclusion.addRDFType(clsBaseExclusion);
	    		}
	    		if (exclusion.hasRDFType(clsExclusion)) {
	    			exclusion.removeRDFType(clsExclusion);
	    		}
	    		
	    		//change relationship
	    		icdContentModel.addBaseExclusionTermToClass(category, exclusion);
	    		category.removePropertyValue(propExclusion, exclusion);
	    		
	    		String label = (String)exclusion.getPropertyValue(propLabel);
	    		String id = category.getName();
	    		CategoryInfo catInfo = addNewCategoryInfoToMap(categoryInfoMap, id, label);
	    		catInfo.setTermId(exclusion.getName());
	    		catInfo.setChangeMsg("Changed the type of term '" + catInfo.getLabel() + "' to base exclusion term.");
	    		
	    		checkForICDCategoryPropertyValue(owlModel, exclusion);
	    		i++;
	        	if (i % 1000 == 0) {
	        		System.out.println(i);
	        	}
	    	}
	    }
        logger.info(i + " Exclusion terms have been migrated!");

        logger.info("Done!");
	}

	private static void checkForICDCategoryPropertyValue(OWLModel owlModel, RDFResource term) {
		Collection<?> values = term.getPropertyValues(getICDPropertyValue(owlModel));
		if (values != null && values.size() > 0) {
			Log.getLogger().warning("IMPORTANT TODO: Found icdCategory value on term " + term + ": " + values + ". Please migrate these as well");
		}
	}



	private static RDFProperty getICDPropertyValue(OWLModel owlModel) {
		if (propICDCategory == null) {
    		propICDCategory = owlModel.getRDFProperty(ICD_CATEGORY_PROP);
		}
		return propICDCategory;
	}



	private static void writeChangesToChao(Map<String, CategoryInfo> categoryInfoMap, OWLModel owlModel) {
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

		HashMap<String, String> categoryToChangeLog = new HashMap<String, String>();
		
		int i = 0;
		for (String label : categoryInfoMap.keySet()) {
			CategoryInfo catInfo = categoryInfoMap.get(label);

			if (! catInfo.getChangeMsg().isEmpty()) {
				String catId = catInfo.getId();
				String catLog = categoryToChangeLog.get(catId);
				if (catLog == null) {
					catLog = "Automatic migration of synonyms, inclusions and exclusions to base index, base inclusions and base exclusions for %catBrText%: ";
				}
				catLog += "\n\t" + catInfo.getChangeMsg();
				categoryToChangeLog.put(catId, catLog);
			}
			
        	i++;
        	if (i % 1000 == 0) {
        		System.out.println(i);
        	}
		}
		System.out.println(" The total of " + categoryInfoMap.size() + " term changes were consolidate into single change log entries on " + categoryToChangeLog.size() + " categories ");
		
		i = 0;
		for (String catId : categoryToChangeLog.keySet()) {
			Cls cls = owlModel.getCls(catId);
			if (cls == null) {
				Log.getLogger().warning("Writing to ChAO: Could not find class " + catId);
			}
			else {
				Change change = changeFactory.createComposite_Change(null);
				ServerChangesUtil.createChangeStd(changes_db, change, cls, categoryToChangeLog.get(catId).replaceFirst("%catBrText%", cls.getBrowserText()));
				change.setAuthor("WHO");
			}
			
        	i++;
        	if (i % 1000 == 0) {
        		System.out.println(i);
        	}
		}

	}


	static class CategoryInfo implements Comparable<CategoryInfo> {
		private static final String ICD_NS = "http://who.int/icd#";
		private static final String ICD_PREFIX = "icd:";
		private static final String ICTM_NS = "http://who.int/ictm#";
		private static final String ICTM_PREFIX = "ictm:";

		private String label;
		private String id;
		private String termId;
		private Boolean isSynonym;
		private Boolean isInclusion;
		
		private String changeMsg = "";

		public CategoryInfo(String id, String label) {
			this.id = id;
			this.label = label;
		}


		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getTermId() {
			return termId;
		}
		public void setTermId(String termId) {
			this.termId = termId;
		}
		public Boolean getIsSynonym() {
			return isSynonym;
		}
		public void setIsSynonym(Boolean isSynonym) {
			this.isSynonym = isSynonym;
		}
		public Boolean getIsInclusion() {
			return isInclusion;
		}
		public void setIsInclusion(Boolean isInclusion) {
			this.isInclusion = isInclusion;
		}

		public String getChangeMsg() {
			return changeMsg == null ? "" : changeMsg;
		}
		public void setChangeMsg(String changeMsg) {
			this.changeMsg = changeMsg;
		}
		public void appendChangeMsg(String changeMsg) {
			this.changeMsg = getChangeMsg() + changeMsg;
		}


		public int compareTo(CategoryInfo other) {
			int res = - compare(this.isSynonym, other.isSynonym);
			if (res == 0) {
				res = - compare(this.isInclusion, other.isInclusion);
				if (res == 0) {
					res = compare(this.termId, other.termId);
					if (res == 0) {
						res = this.id.compareTo(other.id);
						if (res == 0) {
							res = this.label.compareTo(other.label);
						}
					}
				}
			}
			return res;
		}

		private int compare(Boolean b1, Boolean b2) {
			return (b1 == null && b2 == null ? 0 :
				(b1 == null ? -1 :
					(b2 == null ? 1 :
						b1.compareTo(b2))));
		}
		
		private int compare(String s1, String s2) {
			return (s1 == null && s2 == null ? 0 :
				(s1 == null ? -1 :
					(s2 == null ? 1 :
						s1.compareTo(s2))));
		}

		//--- toString() ---//
		@Override
		public String toString() {
			return id.replaceFirst(ICD_NS, ICD_PREFIX).replaceFirst(ICTM_NS, ICTM_PREFIX) +
			" - " + (termId == null ? "" : termId.replaceFirst(ICD_NS, ICD_PREFIX).replaceFirst(ICTM_NS, ICTM_PREFIX)) +
			" - " + label +
			" - " + isSynonym +
			" " + isInclusion;
		}
	}
}
