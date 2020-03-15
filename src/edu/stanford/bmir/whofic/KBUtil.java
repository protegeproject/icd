package edu.stanford.bmir.whofic;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class KBUtil {

	// This method will try to get the class with the full URI using the 
	// Protege OWL API, and if it fails, it will try to get it through the 
	// Frames API with the provided name (probably the short id)
	public static RDFSNamedClass getRDFSNamedClass(OWLModel owlModel, String clsName) {
		RDFSNamedClass cls = owlModel.getRDFSNamedClass(clsName);
		
		if (cls == null) { //maybe short name is used
			Cls framesCls = ((KnowledgeBase)owlModel).getCls(clsName);
			if (framesCls != null && framesCls instanceof RDFSNamedClass) {
				cls = (RDFSNamedClass) framesCls;
			}
		}
		
		return cls;
	}
	
	
	
}
