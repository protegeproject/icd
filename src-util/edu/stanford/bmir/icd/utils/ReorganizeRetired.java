package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ReorganizeRetired {

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
        ICDContentModel cm = new ICDContentModel(owlModel);
        RDFSNamedClass icdDefinitionSectionMetaclass = owlModel.getRDFSNamedClass("http://who.int/icd#DefinitionSection");
        RDFSNamedClass mChapterRetired = owlModel.getRDFSNamedClass("http://who.int/icd#BulkRetire_2011_01_26");
        
        Collection<?> metaclasses = mChapterRetired.getRDFTypes();
        if (! metaclasses.contains(icdDefinitionSectionMetaclass)) {
        	mChapterRetired.addRDFType(icdDefinitionSectionMetaclass);
        	RDFResource titleTerm = cm.createTitleTerm();
        	RDFProperty icdTitleProp = owlModel.getRDFProperty("http://who.int/icd#icdTitle");
			titleTerm.addPropertyValue(icdTitleProp, "BulkRetire_2011-01-26");
        	cm.addTitleTermToClass(mChapterRetired, titleTerm);
        }
        
        parentMap = new HashMap<String, RDFSClass>();
        
        Collection<RDFSClass> retiredMSClasses = mChapterRetired.getSubclasses(false);
        for (RDFSClass retiredClass : retiredMSClasses) {
        	String className = retiredClass.getBrowserText();
        	//if the class' display name starts with Mxx. (where xx are two digits)
        	if (className.matches("^M\\d\\d\\..*") && retiredClass.getSubclassCount() == 0) {
        		String matchedPrefix = className.substring(0, "Mxx.".length());
        		RDFSClass newParent = parentMap.get(matchedPrefix);
        		if (newParent == null) {
        			newParent = cm.createICDCategory("Retired " + matchedPrefix + "XX codes" , mChapterRetired.getName());
        			parentMap.put(matchedPrefix, newParent);
        		}
        		retiredClass.addSuperclass(newParent);
        		retiredClass.removeSuperclass(mChapterRetired);
        	}
        }
        
	}
}
