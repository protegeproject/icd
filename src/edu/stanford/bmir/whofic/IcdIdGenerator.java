package edu.stanford.bmir.whofic;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class IcdIdGenerator {
	
	private final static String DEFAULT_NS_PROP = "default.namespace.new.entities";
	private final static String DEFAULT_NS = "http://who.int/icd#";

	
	private static Map<OWLModel, String> kb2defaultNs = new HashMap<>();
	
	//TODO: not sure if we should synchronize this method, worried about deadlock
	public static String getNextUniqueId(KnowledgeBase kb) {
		String id = IDGenerator.getNextUniqueId();
		if (kb instanceof OWLModel) {
			id = getDefaultNamespace((OWLModel)kb) + id;
		} 
		return id;
	}
	
	private static String getDefaultNamespace(OWLModel owlModel) {
		String defaultNs = kb2defaultNs.get(owlModel);
		if (defaultNs == null) {
			defaultNs = owlModel.getNamespaceManager().getDefaultNamespace();
			if (defaultNs == null) {
				defaultNs = getDefaultNamespaceForNewEntities();
			}
			kb2defaultNs.put(owlModel, defaultNs);
		}
		return defaultNs;
	}
	
	private static String getDefaultNamespaceForNewEntities() {
        return ApplicationProperties.getString(DEFAULT_NS_PROP, DEFAULT_NS);
    }
	
}
