package edu.stanford.bmir.icd.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.stanford.bmir.icd.utils.ImportUtils;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.OntologyComponentCache;

/**
 * @author csnyulas
 *
 */
public class ExportFilteredICDContent {

	private static final String PPRJ_FILE_URI = "resources/projects/icd/icd_umbrella.pprj";
	private static final String CSV_FILE_INDEX_TERMS = "resources/xls/export_20121218.csv";

	private static URI pprjFileUri = new File(PPRJ_FILE_URI).toURI();
	private static File csvFile = new File(CSV_FILE_INDEX_TERMS);

	private static final boolean TEST_RUN = false;

	private static final String SEPARATOR = "\t";
	private static final String QUOTE = "\"";

	private static final String MSG_CHANGE_CREATE_CLASS = "Create class with name: ";
	
	
	public static enum Filter {NewICD11CategoryAndHasNoPrimaryTAGAssigned};
	
	private static Set<String> catIdSet = new HashSet<String>();
	private static Map<String, RDFResource> valueId2IndMap = new HashMap<String, RDFResource>();


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//read arguments
		if (args.length < 2) {
			Log.getLogger().info("Usage: " +
			"ExportFilteredICDContent pprjFileName csvOutputFileName");
			return;
		}
		else {
			pprjFileUri = new File(args[0]).toURI();
			csvFile = new File(args[1]);
		}

		SystemUtilities.logSystemInfo();
		Log.getLogger().info("\n===== Started export to CSV " + new Date());
		Log.getLogger().info("=== PPRJ File: " + pprjFileUri);
		Log.getLogger().info("=== CSV file for output: " + csvFile);

		//open owl model
		OWLModel owlModel = ImportUtils.openOWLModel(pprjFileUri);

		//call migration functions
		Map<String, CategoryInfo> categoryInfoMap = extractCategoryInfoFromPprjFiltered(owlModel, pprjFileUri, Filter.NewICD11CategoryAndHasNoPrimaryTAGAssigned);
		//print out content to be imported
		List<CategoryInfo> catInfos = new ArrayList<CategoryInfo>(categoryInfoMap.values());
		Log.getLogger().info("\nSorting...");
		Collections.sort(catInfos);
		for (CategoryInfo catInfo : catInfos) {
			System.out.println(catInfo);
			catIdSet.add(catInfo.getId());
		}
		//write filtered category info to CSV file
		try {
			writeCategoryInfoToCsv(catInfos, csvFile);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		//finish processing
		Log.getLogger().info("\n===== End export to Excel at " + new Date());
	}



	// ------------ Extract Category Info ------------- //


	private static Map<String, CategoryInfo> extractCategoryInfoFromPprjFiltered(
			OWLModel owlModel, URI pprjFileUri, Filter filter) {

		Logger logger = Log.getLogger();
		logger.info("\nExtracting unassigned concepts from model... ");
		Map<String, CategoryInfo> res = new HashMap<String, CategoryInfo>();

		ICDContentModel icdContentModel = new ICDContentModel(owlModel);
		KnowledgeBase changesKB = ChangesProject.getChangesKB(owlModel);

		RDFSNamedClass icdCategoryClass = icdContentModel.getICDCategoryClass();
		
		collectAllChildrenMeetingTheFilter(icdCategoryClass, "", 0, icdContentModel, changesKB, filter, res);

        logger.info("Done!");
		
		return res;
		}



    private static void collectAllChildrenMeetingTheFilter(
			RDFSNamedClass category, String pathPrefix, int crtIndex,
			ICDContentModel icdContentModel, KnowledgeBase changesKB,
			Filter filter, Map<String, CategoryInfo> res) {
    	
    	if (TEST_RUN && crtIndex > 3) {
    		return;
    	}
    	
    	if (category != null) {
    		String categoryId = category.getName();
    		//if we did not already processed this 
    		if (res.get(categoryId) == null) {
	    		CategoryInfo catInfo = new CategoryInfo(categoryId);
	    		catInfo.setLabel(category.getBrowserText());
	    		String path = pathPrefix + (crtIndex < 1000 ? new Integer(crtIndex + 1000).toString().substring(1) : new Integer(crtIndex));
	    		catInfo.setPathIndex(path);
	    		catInfo.setIsValid(passesFilter(category, icdContentModel, changesKB, filter, catInfo));
	    		if (catInfo.getIsValid()) {
	    			res.put(categoryId, catInfo);
	    		}
	    		
	    		Collection<RDFSNamedClass> children = icdContentModel.getChildren(category);
	    		ArrayList<RDFSNamedClass> sortedChildren = new ArrayList<RDFSNamedClass>(children);
	    		Collections.sort(sortedChildren);
	    		int childIndex = 1;
	    		for (RDFSNamedClass child : sortedChildren) {
					collectAllChildrenMeetingTheFilter(child, path + "_", childIndex++, icdContentModel, changesKB, filter, res);
				}
    		}
    	}
	}


	// ------------ Write Category Info to CSV file ------------- //


	private static void writeCategoryInfoToCsv(
			Collection<CategoryInfo> categoryInfo, File csvFile) throws IOException {
		Logger logger = Log.getLogger();
		logger.info("\nWrite category information to model... ");

		BufferedWriter w = new BufferedWriter(new FileWriter(csvFile));

        for (CategoryInfo catInfo : categoryInfo) {
        	if (catInfo.getIsValid()) {
	        	String id = catInfo.getId();
	        	String label = catInfo.getLabel();
	        	String[] labelParts = label.split(" ", 2);
	        	String sortingLabel = removeApostrophes(labelParts[0]);
	        	String title = (labelParts.length > 1 ? removeApostrophes(labelParts[1]) : "");
	        	String author = catInfo.getAuthor();
	        	String primaryTAG = catInfo.getPrimaryTAG();
	        	String directLink = catInfo.getDirectLink();
	        	
	        	try {
		        	w.write(toCsvField(sortingLabel) + SEPARATOR + toCsvField(title) + SEPARATOR +
		        			toCsvField(author) + SEPARATOR + toCsvField(primaryTAG) + SEPARATOR +
		        			toCsvField(directLink) + SEPARATOR + id);
		        	w.newLine();
	        	} 
	        	catch (IOException ioe) {
					logger.severe("Category information could not be written to CSV file: " + id);
					continue;
				}
        	}
		}
        w.close();

        logger.info("Done!");
	}

	
	private static String removeApostrophes(String s) {
		if (s.startsWith("'") && s.endsWith("'")) {
			return s.substring(1, s.length() - 1);
		}
		return s;
	}
	
	private static String toCsvField(Object o) {
		String res = (o == null ? "" : o.toString());
		if (res.contains("\n") || res.contains(SEPARATOR) || res.contains(QUOTE)) {
			res = res.replaceAll(QUOTE, QUOTE + QUOTE);
			res = QUOTE + res + QUOTE;
		}
		return res;
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


	
	/**
	 * Returns true if category passes the filter. Otherwise it returns false.<br>
	 * E.g. If the filter is <code>Filter.NewICD11CategoryAndHasNoPrimaryTAGAssigned<code> this method would return true
	 * if and only if the category was created in ICD-11 (i.e. it has a change attached saying "Create class with name:...")
	 * AND has no value attached to the property primaryTAG.
	 * @param category an ICD category 
	 * @param icdContentModel an instance of {@link ICDContentModel} initialized with an OWLModel
	 * @param chaoKb ChAO KB associated with the OWL model 
	 * @param filter a Filter
	 * @param catInfo a partially filled in {@link CategoryInfo} representing the <code>category</code>, 
	 * 	which may be modified by this call
	 */
	static boolean passesFilter(RDFSNamedClass category, ICDContentModel icdContentModel, KnowledgeBase chaoKb, Filter filter, CategoryInfo catInfo) {
		boolean res = false;
		switch (filter) {
		case NewICD11CategoryAndHasNoPrimaryTAGAssigned:
			Ontology_Component oc = OntologyComponentCache.getOntologyComponent(category);
			if (oc == null) {
				res = false;
			}
			else {
				Collection<Change> changes = oc.getChanges();
				for (Iterator<Change> it = changes.iterator(); it.hasNext();) {
					Change ch = (Change) it.next();
					String context = ch.getContext();
					if (context != null && context.startsWith(MSG_CHANGE_CREATE_CLASS)) {
						res = true;
						catInfo.setAuthor(ch.getAuthor());
						break;
					}
				}
				
				if (res == true) {
					RDFResource primaryTAG = icdContentModel.getAssignedPrimaryTag(category);
					if (primaryTAG == null) {
						res = true;
					}
					else {
						res = false;
						catInfo.setPrimaryTAG(primaryTAG.getBrowserText());
					}
				}
			}
			
			break;
		}
		
		return res;
	}

	static class CategoryInfo implements Comparable<CategoryInfo> {
		private static final String ICD_NS = "http://who.int/icd#";
		private static final String ICD_PREFIX = "icd:";
		private static final String ICD_DIRECT_LINK_PREFIX = "http://icat.stanford.edu/?ontology=ICD&tab=ClassesTab&id=http%3A%2F%2Fwho.int%2Ficd%23";
		private static final String ICTM_NS = "http://who.int/ictm#";
		private static final String ICTM_PREFIX = "ictm:";
		private static final String ICTM_DIRECT_LINK_PREFIX = "http://icat.stanford.edu/?ontology=ICD&tab=ClassesTab&id=http%3A%2F%2Fwho.int%2Fictm%23";

		private String label;
		private String id;
		private String pathIndex;
		private String author;
		private String primaryTAG;
		private Boolean isValid;

		public CategoryInfo(String id) {
			this.id = id;
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
		public String getPathIndex() {
			return pathIndex;
		}
		/**
		 * Suggested path index format would be:<BR>
		 * <B>"000_002_034_001"</B> -
		 * which would stand for the "first" (001) concept at level 3 having the following ancestors: the root (000), the "second" class at level 1 (002), and the "34th" class on level 2 (034).
		 * Order of the concepts at any given level of the hierarchy is to be defined by the caller.
		 * 
		 * @param pathIndex a String representing the path index
		 */
		public void setPathIndex(String pathIndex) {
			this.pathIndex = pathIndex;
		}
		public String getAuthor() {
			return author;
		}
		public void setAuthor(String author) {
			this.author = author;
		}
		public String getPrimaryTAG() {
			return primaryTAG;
		}
		public void setPrimaryTAG(String primaryTAG) {
			this.primaryTAG = primaryTAG;
		}
		public Boolean getIsValid() {
			return isValid;
		}
		public void setIsValid(Boolean isValid) {
			this.isValid = isValid;
		}

		public String getDirectLink() {
			return (id.startsWith(ICD_NS) ? 
						id.replaceFirst(ICD_NS, ICD_DIRECT_LINK_PREFIX) :
						id.replaceFirst(ICTM_NS, ICTM_DIRECT_LINK_PREFIX));
		}

		public int compareTo(CategoryInfo other) {
			int res = compare(this.isValid, other.isValid);
			if (res == 0) {
				res = compare(this.pathIndex, other.pathIndex);
				if (res == 0) {
					res = this.id.compareTo(other.id);
					if (res == 0) {
						res = this.label.compareTo(other.label);
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
			" - " + (pathIndex == null ? "" : pathIndex.replaceFirst(ICD_NS, ICD_PREFIX).replaceFirst(ICTM_NS, ICTM_PREFIX)) +
			" - " + label +
			" - " + isValid;
		}
	}
}
