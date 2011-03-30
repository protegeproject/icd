package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ReorganizeRetiredMuskuloSkeletal {

	static Map<String, RDFSClass> parentMap;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Needs 1 param: ICD pprj file");
            return;
        }

        String fileName = args[0];

        Project prj = Project.loadProjectFromFile(fileName , new ArrayList());

        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();
        reorganizeBulkRetiredMuskuloSkeletal(owlModel);
	}

	public static void reorganizeBulkRetiredMuskuloSkeletal(OWLModel owlModel) {
    	String prefixMatchString = "M\\d\\d\\.";
        String newParentNamePrefix = "Retired ";
		String newParentNameSuffix = "XX codes";
    	
        ICDContentModel cm = new ICDContentModel(owlModel);
        RDFSNamedClass icdDefinitionSectionMetaclass = owlModel.getRDFSNamedClass("http://who.int/icd#DefinitionSection");
        RDFSNamedClass mChapterRetired = owlModel.getRDFSNamedClass("http://who.int/icd#BulkRetire_2011_01_26");
        
        Collection<?> metaclasses = mChapterRetired.getRDFTypes();
        if (! metaclasses.contains(icdDefinitionSectionMetaclass)) {
        	mChapterRetired.addRDFType(icdDefinitionSectionMetaclass);
        	RDFResource titleTerm = cm.createTitleTerm();
			titleTerm.addPropertyValue(cm.getLabelProperty(), "BulkRetire_2011-01-26");
			titleTerm.addPropertyValue(cm.getLangProperty(), "en");
        	cm.addTitleTermToClass(mChapterRetired, titleTerm);
        }
        
        parentMap = new HashMap<String, RDFSClass>();
        
        Collection<RDFSClass> retiredMSClasses = mChapterRetired.getSubclasses(false);
        //initialize parentMap
		for (RDFSClass retiredClass : retiredMSClasses) {
        	String className = retiredClass.getBrowserText();
        	if (className.matches(newParentNamePrefix + prefixMatchString + newParentNameSuffix)) {
        		String matchedPrefix = className.replaceFirst(newParentNamePrefix + "(" + prefixMatchString + ")" + newParentNameSuffix, "$1");
        		parentMap.put(matchedPrefix, retiredClass);
        	}
        }
        //reorganize subclasses
        for (RDFSClass retiredClass : retiredMSClasses) {
        	String className = retiredClass.getBrowserText();
        	String msg = className;
        	//if the class' display name starts with Mxx. (where xx are two digits)
        	if (className.matches("^\\s*" + prefixMatchString + ".*") && retiredClass.getSubclassCount() == 0) {
        		//String matchedPrefix = className.substring(0, "Mxx.".length());
        		String matchedPrefix = className.replaceFirst("^\\s*(" + prefixMatchString + ").*", "$1");
        		msg += "/matched: " + matchedPrefix;
        		RDFSClass newParent = parentMap.get(matchedPrefix);
        		if (newParent == null) {
        			newParent = cm.createICDCategory(null , mChapterRetired.getName());
        			String newParentName = newParentNamePrefix + matchedPrefix + newParentNameSuffix;
        			RDFResource titleTerm = cm.createTitleTerm();
        			titleTerm.addPropertyValue(cm.getLabelProperty(), newParentName);
        			titleTerm.addPropertyValue(cm.getLangProperty(), "en");
					cm.addTitleTermToClass((RDFSNamedClass)newParent, titleTerm);
        			parentMap.put(matchedPrefix, newParent);
        		}
        		msg += "/new parent: " + newParent.getBrowserText();
        		retiredClass.addSuperclass(newParent);
        		retiredClass.removeSuperclass(mChapterRetired);
        	}
        	System.out.println(msg);
        	Log.getLogger().info(msg);
        }
        
	}
}
