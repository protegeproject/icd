package edu.stanford.bmir.protegex.icd;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

@Deprecated
public class ICDModel {
	
	private KnowledgeBase kb;
	private Cls code_metaclass;
	private Slot code_slot;
	private Slot url_slot;
	private Slot cd_system_slot;
	private Slot pref_term_slot;
	private Cls standard_term_cls;
	private Slot concept_id_slot;
	private Slot ontology_id_slot;
	
	
	public ICDModel(KnowledgeBase kb) {
		this.kb = kb;
		init();
	}

	private void init() {
		code_metaclass = kb.getCls(ICDConstants.ICD_METACLASS_CODE);
		code_slot = kb.getSlot(ICDConstants.ICD_SLOT_CODE);
		url_slot = kb.getSlot(ICDConstants.ICD_SLOT_URL);
		cd_system_slot = kb.getSlot(ICDConstants.ICD_SLOT_CD_SYSTEM);
		pref_term_slot = kb.getSlot(ICDConstants.ICD_SLOT_PREFERRED_TERM);	
		standard_term_cls = kb.getCls(ICDConstants.ICD_CLS_STANDARD_TERM);
		concept_id_slot = kb.getSlot(ICDConstants.ICD_SLOT_CONCEPT_ID);
		ontology_id_slot = kb.getSlot(ICDConstants.ICD_SLOT_ONTOLOGY_ID);
	}

	public KnowledgeBase getKb() {
		return kb;
	}

	public Cls getCode_metaclass() {
		return code_metaclass;
	}

	public Slot getCode_slot() {
		return code_slot;
	}

	public Slot getUrl_slot() {
		return url_slot;
	}

	public Slot getCd_system_slot() {
		return cd_system_slot;
	}

	public Slot getPref_term_slot() {
		return pref_term_slot;
	}

	public Cls getStandard_term_cls() {
		return standard_term_cls;
	}

	public Slot getConcept_id_slot() {
		return concept_id_slot;
	}

	public Slot getOntology_id_slot() {
		return ontology_id_slot;
	}
	
	
	
}
