package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class RetireResiduals {

	static final String CLASS_RETIRED = "http://who.int/icd#Retired";
	static final String CLASS_TO_BE_RETIRED_CHAPTER = "http://who.int/icd#231575_52a94b8b_65cc_439a_b8e5_57058c37b3ab";
	static final String CLASS_BULK_RETIRED = "http://who.int/icd#BulkRetire_2013_05_01";
	
	//static final int IDX_PUBLIC_ID = 0;
	static final int IDX_ID = 1;
	static final int IDX_TITLE = 2;
	static final int IDX_IS_ICD_10 = 3;
	static final int IDX_MIGR_CONT_TO_PARENT = 4;
	//static final int IDX_PARENT_PUBLIC_ID = 5;
	static final int IDX_PARENT_ID = 6;
	
	static final String IS_IN_ICD10 = "IsInICD10";


    private static class RetirementInfo {
    	String clsId;
    	String title;
    	boolean isICD10;
    	boolean moveContentToParent;
    	String parentId;
    	
    	RDFSNamedClass cls;
    	RDFSNamedClass parent;
    	
    	public String toString() {
    		return clsId + " '" + title + "' " + 
    				isICD10 + " " + moveContentToParent + 
    				(parentId == null ? "" : " " + parentId); 
    	}
    }

    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Needs 2 params: ICD pprj file and TXT file");
            return;
        }

        String fileName = args[0];
        String csvFile = args[1];

        Project prj = Project.loadProjectFromFile(fileName , new ArrayList());

        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();
        ICDContentModel cm = new ICDContentModel(owlModel);

        //create retired cls
        OWLNamedClass retiredCls = owlModel.getOWLNamedClass(CLASS_RETIRED);
        OWLNamedClass toBeRetiredCls = owlModel.getOWLNamedClass(CLASS_TO_BE_RETIRED_CHAPTER);

        RDFSNamedClass bulkRetiredCls = owlModel.getRDFSNamedClass(CLASS_BULK_RETIRED);
        if (bulkRetiredCls == null) {
            bulkRetiredCls= owlModel.createOWLNamedSubclass(CLASS_BULK_RETIRED, retiredCls);
        }
        //NOTE: In case WHO will request to move also the ICD10 residual classes under the 
        //      "to be retired" class (instead of only making them obsolete), we should   
        //      repeat the above in order to create a "bulk retired ICD10 residuals" subclass
        //      of toBeRetired.

        RDFProperty isObsoleteProp = cm.getIsObsoleteProperty();
        
        //TODO - cols should be split by "\t"
        File file = new File(csvFile);

        Set<RetirementInfo> clses = new HashSet<RetirementInfo>();

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                int i = 0;
                while ((line = input.readLine()) != null) {
                    if (line != null) {
                        try {
                            //System.out.println(line);
                            String[] split = line.split("\\t");
                            
                            RetirementInfo retInfo = new RetirementInfo();
                            retInfo.clsId = split[IDX_ID];
                            retInfo.title = split[IDX_TITLE];
                            retInfo.isICD10 = IS_IN_ICD10.equals( split[IDX_IS_ICD_10] );
                            retInfo.moveContentToParent = "TRUE".equals( split[IDX_MIGR_CONT_TO_PARENT].toUpperCase() );

                            String clsName = retInfo.clsId;
                            RDFSNamedClass cls = owlModel.getRDFSNamedClass(clsName);
                            if (cls == null) {
                                Log.getLogger().info("***& " + clsName + " not found");
                            } else {
                            	Object isObsoleteVal = cls.getPropertyValue(isObsoleteProp);
                            	boolean isObsolete = (isObsoleteVal != null && ((Boolean)isObsoleteVal).booleanValue());
                                if (cls.hasSuperclass(bulkRetiredCls) || 
                                		cls.hasSuperclass(retiredCls) || cls.hasSuperclass(toBeRetiredCls) ||
                                		isObsolete) {
                                    Log.getLogger().info("^^^ Already retired: " + cls.getBrowserText() +" " + cls.getName() + 
                                    		(isObsolete ? "(obsolete flag set)" : ""));
                                } else {
                                    i++;
                                    Log.getLogger().info(i + ". " + cls.getBrowserText() + " (" + clsName + ") --- to retire");
                                    retInfo.cls = cls;
//                                    if current title term used in browser text is not equal with retInfo.title {
//                                    	Log.getLogger().info("*** WARNING: The title of the class " + clsName + 
//                                    			" has changed from " + retInfo.title + " to " + cls.getBrowserText());
//                                    }
                                    if (retInfo.moveContentToParent) {
                                        retInfo.parentId = split[IDX_PARENT_ID];
                                        
                                    	if (retInfo.parentId != null) {
                                    		RDFSNamedClass parentCls = owlModel.getRDFSNamedClass(retInfo.parentId);
                                    		if (parentCls == null) {
                                    			Log.getLogger().info("***& Parent class " + retInfo.parentId + " not found. Content will not be migrated for " + clsName);
                                    		}
                                    		else {
                                    			retInfo.parent = parentCls;
                                    		}
                                    	}
                                    	else {
                                    		Log.getLogger().info("*** WARNING: Parent name is missing for " + clsName);
                                    	}
                                    }
                                    clses.add(retInfo);
                                }
                            }
                        } catch (Exception e) {
                            Log.getLogger().log(Level.WARNING, "Error at import", e);
                        }
                    }
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Log.getLogger().info(clses.size() + " classes to retire.");

        int i = 0;
        for (RetirementInfo retInfo : clses) {
        	RDFSNamedClass cls = retInfo.cls;
            try {
                i++;
                Log.getLogger().info(i + ". Retiring: " + retInfo.clsId + "" + cls.getBrowserText());
                if (retInfo.isICD10) {
                	cls.setPropertyValue(isObsoleteProp, true);
                }
                else {
                    Collection<RDFSNamedClass> superclses = cls.getSuperclasses(false);
	                cls.addSuperclass(bulkRetiredCls);
	                for (RDFSNamedClass supercls : superclses) {
	                    cls.removeSuperclass(supercls);
	                }
	                cls.setDeprecated(true);
                }
                if (retInfo.moveContentToParent && retInfo.parent != null) {
                	migrateContent(cls, retInfo.parent, cm);
                }
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error at retiring " + cls, e);
            }
        }

        Log.getLogger().info("Retired: " + i + " classes.");
    }


    /**
     * get all terms (base Index, base Exclusion) of fromCls
     * move all the property value assertions, which
     * currently link those terms to fromCls, to toCls
     * 
     * @param fromCls
     * @param toCls
     * @param cm
     */
	@SuppressWarnings("rawtypes")
    static private void migrateContent(RDFSNamedClass fromCls, RDFSNamedClass toCls,
    		ICDContentModel cm) {
		Log.getLogger().info(" --- Migrating content to " + toCls.getBrowserText() + " (" + toCls.getName() +") ---");
		//get properties
    	RDFProperty baseExclusionProperty = cm.getBaseExclusionProperty();
    	
    	RDFProperty baseInclusionProperty = cm.getBaseInclusionProperty();
    	RDFProperty indexBaseInclusionProperty = cm.getIndexBaseInclusionProperty();
    	RDFProperty subclassBaseInclusionProperty = cm.getSubclassBaseInclusionProperty();
    	
    	RDFProperty baseIndexProperty = cm.getBaseIndexProperty();
    	RDFProperty narrowerProperty = cm.getNarrowerProperty();
    	RDFProperty synonymProperty = cm.getSynonymProperty();
    	
    	//get property values
    	Collection baseExclusions = fromCls.getPropertyValues(baseExclusionProperty, false);
    	
    	Collection baseInclusions = fromCls.getPropertyValues(baseInclusionProperty, false);
    	Collection indexBaseInclusions = fromCls.getPropertyValues(indexBaseInclusionProperty, false);
    	Collection subclassBaseInclusions = fromCls.getPropertyValues(subclassBaseInclusionProperty, false);
    	
    	Collection baseIndex = fromCls.getPropertyValues(baseIndexProperty, false);
    	Collection narrower = fromCls.getPropertyValues(narrowerProperty, false);
    	Collection synonym = fromCls.getPropertyValues(synonymProperty, false);
    	
    	//move property values
    	for (Object value : baseExclusions) {
    		Log.getLogger().info(" Moving baseExclusion: " + ((RDFResource)value).getBrowserText());
			toCls.addPropertyValue(baseExclusionProperty, value);
			fromCls.removePropertyValue(baseExclusionProperty, value);
		}
    	for (Object value : baseInclusions) {
    		Log.getLogger().info(" Moving baseInclusion: " + ((RDFResource)value).getBrowserText());
			toCls.addPropertyValue(baseInclusionProperty, value);
			fromCls.removePropertyValue(baseInclusionProperty, value);
		}
    	for (Object value : indexBaseInclusions) {
    		Log.getLogger().info(" Moving indexBaseInclusion: " + ((RDFResource)value).getBrowserText());
			toCls.addPropertyValue(indexBaseInclusionProperty, value);
			fromCls.removePropertyValue(indexBaseInclusionProperty, value);
		}
    	for (Object value : subclassBaseInclusions) {
    		Log.getLogger().info(" Moving subclassBaseInclusion: " + ((RDFResource)value).getBrowserText());
			toCls.addPropertyValue(subclassBaseInclusionProperty, value);
			fromCls.removePropertyValue(subclassBaseInclusionProperty, value);
		}
    	for (Object value : baseIndex) {
    		Log.getLogger().info(" Moving baseIndex: " + ((RDFResource)value).getBrowserText());
			toCls.addPropertyValue(baseIndexProperty, value);
			fromCls.removePropertyValue(baseIndexProperty, value);
		}
    	for (Object value : narrower) {
    		Log.getLogger().info(" Moving narrower: " + ((RDFResource)value).getBrowserText());
			toCls.addPropertyValue(narrowerProperty, value);
			fromCls.removePropertyValue(narrowerProperty, value);
		}
    	for (Object value : synonym) {
    		Log.getLogger().info(" Moving synonym: " + ((RDFResource)value).getBrowserText());
			toCls.addPropertyValue(synonymProperty, value);
			fromCls.removePropertyValue(synonymProperty, value);
		}
    }
}
