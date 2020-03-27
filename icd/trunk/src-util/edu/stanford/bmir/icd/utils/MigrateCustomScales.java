package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * Converts references to Custom Scales to references to 
 * class hierarchies value sets (i.e. classes from X Chapter)
 *  
 * @author csnyulas
 *
 */
public class MigrateCustomScales {

	private static final String PPRJ_FILE_URI = "projects/icd/icd_umbrella.pprj";

	private static URI pprjFileUri = new File(PPRJ_FILE_URI).toURI();

	private static final boolean TEST_RUN = false;
	private static final boolean DEBUG = false;

	private static final String SEPARATOR = "\t";
	private static final String QUOTE = "\"";

	private static final int COLUMN_ENTITY_ID = 0;
	private static final int COLUMN_PROPERTY_ID = 1;
	private static final int COLUMN_OLD_SCALE_ID = 2;
	private static final int COLUMN_NEW_VALUE_ID = 3;

	private final static List<String> CustomScaleProperties = ICDContentModelConstants.SCALE_PC_AXES_PROPERTIES_LIST;
	private final static HashMap<String,String> pcAxis2ScalePropertyMap = ICDContentModelConstants.PC_AXIS_PROP_TO_VALUE_SET_PROP;
	private final static HashMap<String,String> scaleProperty2pcAxisMap = createReverseMap(pcAxis2ScalePropertyMap);

	private final static HashMap<String, String> pcAxis2TypeMap = new HashMap<>();
	private final static Map<String, File> property2FilesMap = new LinkedHashMap<>();
	
	
	static {
		pcAxis2TypeMap.put(ICDContentModelConstants.PC_AXIS_HAS_SEVERITY, ICDContentModelConstants.NS + "SeverityReferenceTerm");
		pcAxis2TypeMap.put(ICDContentModelConstants.PC_AXIS_HAS_ALT_SEVERITY1, ICDContentModelConstants.NS + "SeverityReferenceTerm");
		pcAxis2TypeMap.put(ICDContentModelConstants.PC_AXIS_HAS_ALT_SEVERITY2, ICDContentModelConstants.NS + "SeverityReferenceTerm");
		pcAxis2TypeMap.put(ICDContentModelConstants.PC_AXIS_TEMPORALITY_COURSE, ICDContentModelConstants.NS + "CourseReferenceTerm");
		pcAxis2TypeMap.put(ICDContentModelConstants.PC_AXIS_TEMPORALITY_PATTERN_AND_ONSET, ICDContentModelConstants.NS + "TemporalPatternAndOnsetReferenceTerm");
	}

	
	private static HashMap<String, String> createReverseMap(HashMap<String, String> reverseMap) {
		HashMap<String, String> res = new HashMap<String, String>(reverseMap.size());
		for (String key : reverseMap.keySet()) {
			res.put(reverseMap.get(key), key);
		}
		return res;
	}

	/**
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		//read arguments
		if (args.length < 2) {
			printUsage();
			return;
		}
		else {
			pprjFileUri = new File(args[0]).toURI();
			
			for (int i = 1; i < args.length; i++) {
				String arg = args[i];
				if (!arg.matches("-.+=.+")) {
					Log.getLogger().info("Error: Incorrect argument '" + arg +
							"' is not of form -propertyName=csvFile");
					printUsage();
					return;
				}
				String[] argParts = arg.substring(1).split("=",2);

				String propertyName = argParts[0];
				String fileName = argParts[1];
				if (! propertyName.startsWith(ICDContentModelConstants.NS)) {
					propertyName = ICDContentModelConstants.NS + propertyName;
				}
				if (! CustomScaleProperties.contains(propertyName)) {
					Log.getLogger().info("Error: invalid custom scale property name " + argParts[0]);
					printUsage();
					return;
				}
				else {
					property2FilesMap.put(propertyName, new File(fileName));
				}
			}
		}

		SystemUtilities.logSystemInfo();
		Log.getLogger().info("\n===== Started migration of custom scales based on CSV files at " + new Date());
		Log.getLogger().info("=== PPRJ File: " + pprjFileUri);
		Log.getLogger().info("=== CSV files for properties: " + property2FilesMap.toString());

		//open owl model
		OWLModel owlModel = ImportUtils.openOWLModel(pprjFileUri);

		for (String prop : property2FilesMap.keySet()) {
			migrateScaleValues(owlModel, prop, property2FilesMap.get(prop));
		}
		
//		//TODO check whether we want to record changes. If yes, look at:
//		writeChangesToChao(categoryInfoMap, owlModel);

		//finish processing
		Log.getLogger().info("\n===== End migration of custom scales at " + new Date());
	}


	private static void printUsage() {
		Log.getLogger().info("Usage: " + 
		"MigrateCustomScales pprjFile -propertyName1=csvFile1 [-propertyName2=csvFile2 ...]");
	}



	private static void migrateScaleValues(OWLModel owlModel, String propName, File file) {
		Log.getLogger().info("\nMigrating property " + propName + " ...");
		
		Map<String, CategoryInfo> categoryInfoMap = extractCategoryInfoFromCsv(owlModel, file);
//		//print out content to be imported
//		List<CategoryInfo> catInfos = new ArrayList<CategoryInfo>(categoryInfoMap.values());
//		//Collections.sort(catInfos);
//		if (DEBUG) {
//		  for (CategoryInfo catInfo : catInfos) {
//				System.out.println(catInfo);
//		  }
//		}
		
		RDFProperty oldProperty = owlModel.getRDFProperty(pcAxis2ScalePropertyMap.get(propName));
		RDFProperty newProperty = owlModel.getRDFProperty(propName);
		Iterator<RDFResource> itSubjects = (Iterator<RDFResource>) owlModel.listSubjects(oldProperty);
		while (itSubjects.hasNext()) {
			RDFResource subject = itSubjects.next();
			RDFResource scaleValue = (RDFResource) subject.getPropertyValue(oldProperty);
			
			String entityId = subject.getName();
			CategoryInfo catInfo = categoryInfoMap.get(entityId);
			if (catInfo == null) {
				Log.getLogger().log(Level.SEVERE, String.format(
						"Migration information for class %s was not found", entityId));
				continue;
			}
			if ( ! propName.equals(catInfo.getNewProperty()) ) {
				Log.getLogger().log(Level.SEVERE, String.format(
						"Wrong property for class %s. Property in ontology: %s Property in file: %s", 
						entityId, propName, catInfo.getNewProperty()));
				continue;
			}
			if ( ! scaleValue.getName().equals(catInfo.getOldValue()) ) {
				Log.getLogger().log(Level.SEVERE, String.format(
						"Wrong scale value for class %s. Scale individual in ontology: %s Scale individual in file: %s", 
						entityId, scaleValue.getName(), catInfo.getOldValue()));
				continue;
			}
			RDFResource newValue = owlModel.getRDFResource(catInfo.getNewValue());
			
			if (DEBUG) {
				System.out.println(subject + " " + subject.getPropertyValue(oldProperty)) ;
				System.out.println(catInfo);
			}

			replaceValue(owlModel, subject, oldProperty, scaleValue, newProperty, newValue);
		}
		
	}


	private static void replaceValue(OWLModel owlModel, RDFResource subject, RDFProperty oldProperty, RDFResource scaleValue,
			RDFProperty newProperty, RDFResource newValue) {
		Log.getLogger().info(
				String.format("Replacing property value on %s: %s %s  with %s %s",
				subject, oldProperty, scaleValue, newProperty, newValue));
		
		ICDContentModel cm = new ICDContentModel(owlModel);
		
		RDFSNamedClass pcRefClass = owlModel.getRDFSNamedClass(pcAxis2TypeMap.get(newProperty.getName()));

		Log.getLogger().info(
				String.format("Entity publicId: %s, title: %s\n"
						+ "ReferenceTerm: %s Referenced class publicId: %s, title: %s",
				cm.getPublicId((RDFSNamedClass)subject), cm.getTitleLabel((RDFSNamedClass)subject),
				pcRefClass, cm.getPublicId((RDFSNamedClass)newValue), cm.getTitleLabel((RDFSNamedClass)newValue)));
		
		if (! TEST_RUN) {
			cm.addPostCoordinationValueReferenceTermToClass((RDFSNamedClass) subject, newProperty, pcRefClass, newValue);

			//TODO activate the desired on, when we are ready
			subject.removePropertyValue(oldProperty, scaleValue);
			//or
			//scaleValue.delete();
			//or both
		}
	}



	// ------------ Extract Category Info ------------- //


	private static Map<String, CategoryInfo> extractCategoryInfoFromCsv(
			OWLModel owlModel, File csvFile) {

		Log.getLogger().info(String.format(
				"\nReading scale information from CSV file %s ... ", csvFile.getAbsolutePath()));

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
	                    
	                    //activate this if we want to limit the nr. of rows read in TEST mode: 
	                    //if ((!TEST_RUN) || (TEST_RUN && r<10)) //IF TEST ONLY
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
			String id = split[COLUMN_ENTITY_ID];
			String propId = split[COLUMN_PROPERTY_ID];
			String oldValue = split[COLUMN_OLD_SCALE_ID];
			String newValue = split[COLUMN_NEW_VALUE_ID];
			
			CategoryInfo catInfo = new CategoryInfo(id);
			catInfo.setOldProperty(propId);
			catInfo.setNewProperty(scaleProperty2pcAxisMap.get(propId));
			catInfo.setOldValue(oldValue);
			catInfo.setNewValue(newValue);

            res.put(id,catInfo);
            
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at import", e);
        }
    }
    
	private static void removeQuotes(final String[] split) {
		for (int i = 0; i < split.length; i++) {
			if (split[i].startsWith(QUOTE) && split[i].endsWith(QUOTE)) {
				split[i] = split[i].substring(1, split[i].length()-1);
			}
		}
	}



//	//just for reference, as used in ImportPostCoordinationValueSet and ImportIndexTerms
//	
//	// ------------ Write Category Info to OWL Model ------------- //
//
//
//	private static void writeChangesToChao(Map<String, CategoryInfo> categoryInfoMap, OWLModel owlModel) {
//		KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(owlModel);
//		if (chaoKb == null) {
//			Log.getLogger().info("Could not find ChAO for: " + owlModel);
//			return;
//		}
//
//		Log.getLogger().info("Starting writing to ChAO from: " + chaoKb.getProject().getProjectURI() + " on " + new Date());
//
//		ChangesProject.initialize(owlModel.getProject());
//
//		ChangeFactory changeFactory = new ChangeFactory(chaoKb);
//		OntologyComponentFactory ocFactory = new OntologyComponentFactory(chaoKb);
//		PostProcessorManager changes_db = ChangesProject.getPostProcessorManager(owlModel);
//
//		HashMap<String, String> categoryToChangeLog = new HashMap<String, String>();
//		
//		int i = 0;
//		for (String label : categoryInfoMap.keySet()) {
//			CategoryInfo catInfo = categoryInfoMap.get(label);
//
//			if (! catInfo.getChangeMsg().isEmpty()) {
//				String catId = catInfo.getId();
//				String catLog = categoryToChangeLog.get(catId);
//				if (catLog == null) {
//					catLog = "Automatic migration of synonyms, inclusions and exclusions to base index, base inclusions and base exclusions for %catBrText%: ";
//				}
//				catLog += "\n\t" + catInfo.getChangeMsg();
//				categoryToChangeLog.put(catId, catLog);
//			}
//			
//        	i++;
//        	if (i % 1000 == 0) {
//        		System.out.println(i);
//        	}
//		}
//		System.out.println(" The total of " + categoryInfoMap.size() + " term changes were consolidate into single change log entries on " + categoryToChangeLog.size() + " categories ");
//		
//		i = 0;
//		for (String catId : categoryToChangeLog.keySet()) {
//			Cls cls = owlModel.getCls(catId);
//			if (cls == null) {
//				Log.getLogger().warning("Writing to ChAO: Could not find class " + catId);
//			}
//			else {
//				Change change = changeFactory.createComposite_Change(null);
//				ServerChangesUtil.createChangeStd(changes_db, change, cls, categoryToChangeLog.get(catId).replaceFirst("%catBrText%", cls.getBrowserText()));
//				change.setAuthor("WHO");
//			}
//			
//        	i++;
//        	if (i % 1000 == 0) {
//        		System.out.println(i);
//        	}
//		}
//
//	}
//
//
//	static class CategoryInfo implements Comparable<CategoryInfo> {
//		private static final String ICD_NS = "http://who.int/icd#";
//		private static final String ICD_PREFIX = "icd:";
//		private static final String ICTM_NS = "http://who.int/ictm#";
//		private static final String ICTM_PREFIX = "ictm:";
//
//		private String label;
//		private String id;
//		private String termId;
//		private Boolean isSynonym;
//		private Boolean isInclusion;
//		
//		private String changeMsg = "";
//
//		public CategoryInfo(String id, String label) {
//			this.id = id;
//			this.label = label;
//		}
//
//
//		public String getLabel() {
//			return label;
//		}
//		public void setLabel(String label) {
//			this.label = label;
//		}
//		public String getId() {
//			return id;
//		}
//		public void setId(String id) {
//			this.id = id;
//		}
//		public String getTermId() {
//			return termId;
//		}
//		public void setTermId(String termId) {
//			this.termId = termId;
//		}
//		public Boolean getIsSynonym() {
//			return isSynonym;
//		}
//		public void setIsSynonym(Boolean isSynonym) {
//			this.isSynonym = isSynonym;
//		}
//		public Boolean getIsInclusion() {
//			return isInclusion;
//		}
//		public void setIsInclusion(Boolean isInclusion) {
//			this.isInclusion = isInclusion;
//		}
//
//		public String getChangeMsg() {
//			return changeMsg == null ? "" : changeMsg;
//		}
//		public void setChangeMsg(String changeMsg) {
//			this.changeMsg = changeMsg;
//		}
//		public void appendChangeMsg(String changeMsg) {
//			this.changeMsg = getChangeMsg() + changeMsg;
//		}
//
//
//		public int compareTo(CategoryInfo other) {
//			int res = - compare(this.isSynonym, other.isSynonym);
//			if (res == 0) {
//				res = - compare(this.isInclusion, other.isInclusion);
//				if (res == 0) {
//					res = compare(this.termId, other.termId);
//					if (res == 0) {
//						res = this.id.compareTo(other.id);
//						if (res == 0) {
//							res = this.label.compareTo(other.label);
//						}
//					}
//				}
//			}
//			return res;
//		}
//
//		private int compare(Boolean b1, Boolean b2) {
//			return (b1 == null && b2 == null ? 0 :
//				(b1 == null ? -1 :
//					(b2 == null ? 1 :
//						b1.compareTo(b2))));
//		}
//		
//		private int compare(String s1, String s2) {
//			return (s1 == null && s2 == null ? 0 :
//				(s1 == null ? -1 :
//					(s2 == null ? 1 :
//						s1.compareTo(s2))));
//		}
//
//		//--- toString() ---//
//		@Override
//		public String toString() {
//			return id.replaceFirst(ICD_NS, ICD_PREFIX).replaceFirst(ICTM_NS, ICTM_PREFIX) +
//			" - " + (termId == null ? "" : termId.replaceFirst(ICD_NS, ICD_PREFIX).replaceFirst(ICTM_NS, ICTM_PREFIX)) +
//			" - " + label +
//			" - " + isSynonym +
//			" " + isInclusion;
//		}
//	}
	
	private static class CategoryInfo {
		private String id;
		private String oldProperty;
		private String newProperty;
		private String oldValue;
		private String newValue;
		
		public CategoryInfo(String id) {
			this.id = id;
		}
		
		public void setOldProperty(String oldPropertyName) {
			this.oldProperty = oldPropertyName;
		}
		
		public String getOldProperty() {
			return oldProperty;
		}
		
		public void setNewProperty(String newPropertyName) {
			this.newProperty = newPropertyName;
		}
		
		public String getNewProperty() {
			return newProperty;
		}
		
		public void setOldValue(String oldValue) {
			this.oldValue = oldValue;
		}
		
		public String getOldValue() {
			return oldValue;
		}
		
		public void setNewValue(String newValue) {
			this.newValue = newValue;
		}
		
		public String getNewValue() {
			return newValue;
		}

		//--- toString() ---//
		@Override
		public String toString() {
			return replaceNamespaceWithPrefix(id) +
			" - " + replaceNamespaceWithPrefix(oldProperty) +
			" - " + replaceNamespaceWithPrefix(oldValue) +
			" - " + replaceNamespaceWithPrefix(newProperty) +
			" " + replaceNamespaceWithPrefix(newValue);
		}
		
		private String replaceNamespaceWithPrefix(String id) {
			return id.replaceFirst(ICDContentModelConstants.NS, "icd:");
		}
	}
}
