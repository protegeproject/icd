package edu.stanford.bmir.whofic;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLProperty;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class KBUtil {

	/**
	 * This is a workaround for the problem that
	 * many iCAT ids are not absolute URIs, and the owlModel.getResource(name) 
	 * will try to expand these names, and will fail.
	 * 
	 * This method will return the RDFResource, even if the provided name is 
	 * the long or short id, and whether in the database we have stored
	 * long or short ids.
	 * 
	 * @param owlModel - the OWL Model
	 * @param name - the short of long name of the resource to be retrieved
	 * @return
	 */
	public static RDFResource getRDFResource(OWLModel owlModel, String name) {
		RDFResource res = owlModel.getRDFResource(name);
		
		if(res != null) { //this is the good case
			return res;
		}
		
		//this is the fallback for non-absolute URIs (short ids)
		@SuppressWarnings("deprecation")
		Frame frame = owlModel.getFrame(name);
		
		return frame != null && frame instanceof RDFResource ? 
				(RDFResource) frame : null;
	}

	public static RDFSNamedClass getRDFSNamedClass(OWLModel owlModel, String name) {
		RDFResource res = getRDFResource(owlModel, name);
		return res != null && res instanceof RDFSNamedClass ? 
				(RDFSNamedClass) res : null;
	}

	public static OWLNamedClass getOWLNamedClass(OWLModel owlModel, String name) {
		RDFResource res = getRDFResource(owlModel, name);
		return res != null && res instanceof OWLNamedClass ? 
				(OWLNamedClass) res : null;
	}

	public static RDFSClass getRDFSClass(OWLModel owlModel, String name) {
		RDFResource res = getRDFResource(owlModel, name);
		return res != null && res instanceof RDFSClass ? 
				(RDFSClass) res : null;
	}

	public static RDFProperty getRDFProperty(OWLModel owlModel, String name) {
		RDFResource res = getRDFResource(owlModel, name);
		return res != null && res instanceof RDFProperty ? 
				(RDFProperty) res : null;
	}

	public static OWLProperty getOWLProperty(OWLModel owlModel, String name) {
		RDFResource res = getRDFResource(owlModel, name);
		return res != null && res instanceof OWLProperty ? 
				(OWLProperty) res : null;
	}

	public static RDFIndividual getRDFIndividual(OWLModel owlModel, String name) {
		RDFResource res = getRDFResource(owlModel, name);
		return res != null && res instanceof RDFIndividual ? 
				(RDFIndividual) res : null;
	}

	public static OWLIndividual getOWLIndividual(OWLModel owlModel, String name) {
		RDFResource res = getRDFResource(owlModel, name);
		return res != null && res instanceof OWLIndividual ? 
				(OWLIndividual) res : null;
	}

	public static Cls getCls(KnowledgeBase kb, String name) {
		if (kb instanceof OWLModel) {
			return getRDFSClass((OWLModel)kb, name);
		}
		
		return kb.getCls(name);
	}

	public static Slot getSlot(KnowledgeBase kb, String name) {
		if (kb instanceof OWLModel) {
			return getRDFProperty((OWLModel)kb, name);
		}
		
		return kb.getSlot(name);
	}

	public static Instance getInstance(KnowledgeBase kb, String name) {
		if (kb instanceof OWLModel) {
			return getRDFResource((OWLModel)kb, name);
		}
		
		return kb.getInstance(name);
	}
	
}
