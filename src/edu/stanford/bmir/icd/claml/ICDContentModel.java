package edu.stanford.bmir.icd.claml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICDContentModel {

    private static transient Logger log = Log.getLogger(ICDContentModel.class);

    private OWLModel owlModel;

    /*
     * Metaclasses
     */
    private RDFSNamedClass causalMechanismMetaClass;
    private RDFSNamedClass clincalDescriptionMetaClass;
    private RDFSNamedClass definitionMetaClass;
    private RDFSNamedClass functionalImpactMetaClass;
    private RDFSNamedClass diagnosticCriteriaMetaClass;
    private RDFSNamedClass notesMetaClass;
    private RDFSNamedClass linearizationMetaClass;
    private RDFSNamedClass snomedReferenceMetaClass;
    private RDFSNamedClass termMetaClass;

    private Collection<RDFSNamedClass> sectionMetaclasses;

    /*
     * Classes
     */
    private RDFSNamedClass icdCategoryClass;
    private RDFSNamedClass clamlReferenceClass;
    private RDFSNamedClass termClass;
    private RDFSNamedClass termTitleClass;
    private RDFSNamedClass termInclusionClass;
    private RDFSNamedClass termExclusionClass;
    private RDFSNamedClass icd10NotesClass;

    /*
     * Properties
     */
    private RDFProperty icdTitleProperty;
    private RDFProperty icdCodeProperty;
    private RDFProperty kindProperty;
    private RDFProperty usageProperty;

    private RDFProperty inclusionProperty;
    private RDFProperty exclusionProperty;

    private RDFProperty idProperty;
    private RDFProperty labelProperty;
    private RDFProperty langProperty;

    private RDFProperty icdRefCodeProperty;
    private RDFProperty clamlRefProperty;
    private RDFProperty textProperty;

    private RDFProperty codingHintProperty;
    private RDFProperty introductionProperty;
    private RDFProperty noteProperty;
    private RDFProperty preferredProperty;
    private RDFProperty preferredLongProperty;

    public ICDContentModel(OWLModel owlModel) {
        this.owlModel = owlModel;
    }

    /*
     * Getters for sections (metaclasses)
     */

    public RDFSNamedClass getCausalMechanismMetaClass() {
        if (causalMechanismMetaClass == null) {
            causalMechanismMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_CAUSAL_MECH_METACLASS);
        }
        return causalMechanismMetaClass;
    }

    public RDFSNamedClass getClinicalDescriptionMetaClass() {
        if (clincalDescriptionMetaClass == null) {
            clincalDescriptionMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_CLINICAL_DESC_METACLASS);
        }
        return clincalDescriptionMetaClass;
    }

    public RDFSNamedClass getDefinitionMetaClass() {
        if (definitionMetaClass == null) {
            definitionMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_DEFINITION_METACLASS);
        }
        return definitionMetaClass;
    }

    public RDFSNamedClass getFunctionalImpactMetaClass() {
        if (functionalImpactMetaClass == null) {
            functionalImpactMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_FUNCTIONAL_IMPACT_METACLASS);
        }
        return functionalImpactMetaClass;
    }

    public RDFSNamedClass getNotesMetaClass() {
        if (notesMetaClass == null) {
            notesMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_NOTES_METACLASS);
        }
        return notesMetaClass;
    }

    public RDFSNamedClass getLinearizationMetaClass() {
        if (linearizationMetaClass == null) {
            linearizationMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_LINEARIZATION_METACLASS);
        }
        return linearizationMetaClass;
    }

    public RDFSNamedClass getSnomedReferenceMetaClass() {
        if (snomedReferenceMetaClass == null) {
            snomedReferenceMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_SNOMED_METACLASS);
        }
        return snomedReferenceMetaClass;
    }

    public RDFSNamedClass getTermMetaClass() {
        if (termMetaClass == null) {
            termMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_TERM_METACLASS);
        }
        return termMetaClass;
    }

    public RDFSNamedClass getDiagnosticCriteriaMetaClass() {
        if (diagnosticCriteriaMetaClass == null) {
            diagnosticCriteriaMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_DIAGNOSTIC_CRITERIA_METACLASS);
        }
        return diagnosticCriteriaMetaClass;
    }

    public Collection<RDFSNamedClass> getSectionMetaclasses() {
        if (sectionMetaclasses == null) {
            sectionMetaclasses = new ArrayList<RDFSNamedClass>();
            sectionMetaclasses.add(owlModel.getOWLNamedClassClass());
            sectionMetaclasses.add(getDefinitionMetaClass());
            sectionMetaclasses.add(getTermMetaClass());
            sectionMetaclasses.add(getClinicalDescriptionMetaClass());
            sectionMetaclasses.add(getCausalMechanismMetaClass());
            sectionMetaclasses.add(getDiagnosticCriteriaMetaClass());
            sectionMetaclasses.add(getFunctionalImpactMetaClass());
            sectionMetaclasses.add(getSnomedReferenceMetaClass());
            sectionMetaclasses.add(getLinearizationMetaClass());
            sectionMetaclasses.add(getNotesMetaClass());
        }
        return sectionMetaclasses;
    }

    /*
     * Getters for classes
     */

    public RDFSNamedClass getICDCategoryClass() {
        if (icdCategoryClass == null) {
            icdCategoryClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_CATEGORY_CLASS);
        }
        return icdCategoryClass;
    }

    public RDFSNamedClass getClamlReferencesClass() {
        if (clamlReferenceClass == null) {
            clamlReferenceClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.CLAML_REF_CLASS);
        }
        return clamlReferenceClass;
    }

    public RDFSNamedClass getTermClass() {
        if (termClass == null) {
            termClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_CLASS);
        }
        return termClass;
    }

    public RDFSNamedClass getTermTitleClass() {
        if (termTitleClass == null) {
            termTitleClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_TITLE_CLASS);
        }
        return termTitleClass;
    }

    public RDFSNamedClass getTermInclusionClass() {
        if (termInclusionClass == null) {
            termInclusionClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_INCLUSION_CLASS);
        }
        return termInclusionClass;
    }

    public RDFSNamedClass getTermExclusionClass() {
        if (termExclusionClass == null) {
            termExclusionClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_EXCLUSION_CLASS);
        }
        return termExclusionClass;
    }

    public RDFSNamedClass getICD10NotesClass() {
        if (icd10NotesClass == null) {
            icd10NotesClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_ICD10_NOTES_CLASS);
        }
        return icd10NotesClass;
    }

    /*
     * Getters for properties
     */

    public RDFProperty getIcdTitleProperty() {
        if (icdTitleProperty == null) {
            icdTitleProperty = owlModel.getRDFProperty(ICDContentModelConstants.ICD_TITLE_PROP);
        }
        return icdTitleProperty;
    }

    public RDFProperty getIcdCodeProperty() {
        if (icdCodeProperty == null) {
            icdCodeProperty = owlModel.getRDFProperty(ICDContentModelConstants.ICD_CODE_PROP);
        }
        return icdCodeProperty;
    }

    public RDFProperty getIdProperty() {
        if (idProperty == null) {
            idProperty = owlModel.getRDFProperty(ICDContentModelConstants.ID_PROP);
        }
        return idProperty;
    }

    public RDFProperty getLabelProperty() {
        if (labelProperty == null) {
            labelProperty = owlModel.getRDFProperty(ICDContentModelConstants.LABEL_PROP);
        }
        return labelProperty;
    }

    public RDFProperty getLangProperty() {
        if (langProperty == null) {
            langProperty = owlModel.getRDFProperty(ICDContentModelConstants.LANG_PROP);
        }
        return langProperty;
    }

    public RDFProperty getKindProperty() {
        if (kindProperty == null) {
            kindProperty = owlModel.getRDFProperty(ICDContentModelConstants.CLAML_KIND_PROP);
        }
        return kindProperty;
    }

    public RDFProperty getUsageProperty() {
        if (usageProperty == null) {
            usageProperty = owlModel.getRDFProperty(ICDContentModelConstants.CLAML_USAGE_PROP);
        }
        return usageProperty;
    }

    public RDFProperty getIcdRefProperty() {
        if (icdRefCodeProperty == null) {
            icdRefCodeProperty = owlModel.getRDFProperty(ICDContentModelConstants.ICD_REF_CODE_PROP);
        }
        return icdRefCodeProperty;
    }

    public RDFProperty getClamlReferencesProperty() {
        if (clamlRefProperty == null) {
            clamlRefProperty = owlModel.getRDFProperty(ICDContentModelConstants.CLAML_REFERENCES_PROP);
        }
        return clamlRefProperty;
    }

    public RDFProperty getTextProperty() {
        if (textProperty == null) {
            textProperty = owlModel.getRDFProperty(ICDContentModelConstants.TEXT_PROP);
        }
        return textProperty;
    }

    public RDFProperty getCodingHintProperty() {
        if (codingHintProperty == null) {
            codingHintProperty = owlModel.getRDFProperty(ICDContentModelConstants.CODING_HINT_PROP);
        }
        return codingHintProperty;
    }

    public RDFProperty getIntroductionProperty() {
        if (introductionProperty == null) {
            introductionProperty = owlModel.getRDFProperty(ICDContentModelConstants.INTRO_PROP);
        }
        return introductionProperty;
    }

    public RDFProperty getNoteProperty() {
        if (noteProperty == null) {
            noteProperty = owlModel.getRDFProperty(ICDContentModelConstants.NOTE_PROP);
        }
        return noteProperty;
    }

    public RDFProperty getPreferredProperty() {
        if (preferredProperty == null) {
            preferredProperty = owlModel.getRDFProperty(ICDContentModelConstants.PREFFERED_PROP);
        }
        return preferredProperty;
    }

    public RDFProperty getPreferredLongProperty() {
        if (preferredLongProperty == null) {
            preferredLongProperty = owlModel.getRDFProperty(ICDContentModelConstants.PREFERRED_LONG_PROP);
        }
        return preferredLongProperty;
    }

    public RDFProperty getInclusionProperty() {
        if (inclusionProperty == null) {
            inclusionProperty = owlModel.getRDFProperty(ICDContentModelConstants.INCLUSION_PROP);
        }
        return inclusionProperty;
    }

    public RDFProperty getExclusionProperty() {
        if (exclusionProperty == null) {
            exclusionProperty = owlModel.getRDFProperty(ICDContentModelConstants.EXCLUSION_PROP);
        }
        return exclusionProperty;
    }

    /*
     * Create methods
     */

    public RDFSNamedClass createICDCategory(String name, String superclsName) {
        //TODO: Handle multiple parents? Right now assume one parent
        RDFSNamedClass supercls = null;
        if (superclsName == null || superclsName.length() == 0) {
            supercls = getICDCategoryClass();
        } else {
            supercls = getICDClass(superclsName, true);
        }
        RDFSNamedClass cls = getICDClass(name, true);
        if (!cls.getSuperclasses(true).contains(supercls)) {
            cls.addSuperclass(supercls);
            cls.removeSuperclass(owlModel.getOWLThingClass());
        }
        return cls;
    }

    @SuppressWarnings("deprecation")
    public RDFSNamedClass getICDClass(String name, boolean create) {
        RDFSNamedClass cls = owlModel.getRDFSNamedClass(name);
        if (cls == null && create) {
            cls = owlModel.createOWLNamedClass(name);
            cls.setDirectTypes(getSectionMetaclasses());
            cls.addSuperclass(owlModel.getOWLThingClass());
        }
        return cls;
    }

    public void addClassMetadata(RDFSNamedClass cls, String code, String kind, String usage) {
        if (code != null) {
            cls.addPropertyValue(getIcdCodeProperty(), code);
        }
        if (kind != null) {
            cls.addPropertyValue(getKindProperty(), kind);
        }
        if (usage != null) {
            cls.addPropertyValue(getUsageProperty(), usage);
        }
    }

    public void addRdfsLabel(RDFSNamedClass cls) {
        try {
            String code = (String) cls.getPropertyValue(getIcdCodeProperty());
            Instance titleInst = (Instance) cls.getPropertyValue(getIcdTitleProperty());
            String title = (String) titleInst.getOwnSlotValue(getLabelProperty());
            cls.addLabel(code + ". " + title, null);
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not set rdfs:label for " + cls);
        }
    }

    /*
     * Terms
     */

    protected RDFResource createTerm(RDFSNamedClass type) {
        RDFResource term = (RDFResource) owlModel.createInstance(null, CollectionUtilities.createCollection(type));
        return term;
    }

    public void fillTerm(RDFResource term, String id, String label, String lang) {
        if (id != null) {
            term.addPropertyValue(getIdProperty(), id);
        }
        if (label != null) {
            term.addPropertyValue(getLabelProperty(), label);
        }
        if (lang != null) {
            term.addPropertyValue(getLangProperty(), lang);
        }
    }

    protected void addTermToClass(RDFSNamedClass cls, RDFProperty prop, RDFResource term) {
        cls.addPropertyValue(prop, term);
    }

    public RDFResource createTitleTerm() {
        return createTerm(getTermTitleClass());
    }

    public void addTitleTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getIcdTitleProperty(), term);
    }

    public RDFResource createInclusionTerm() {
        return createTerm(getTermInclusionClass());
    }

    public void addInclusionTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getInclusionProperty(), term);
    }

    public RDFResource createExclusionTerm() {
        return createTerm(getTermExclusionClass());
    }

    public void addExclusionTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getExclusionProperty(), term);
    }

    public RDFResource createICD10NotesTerm() {
        return createTerm(getICD10NotesClass());
    }

    public void addPreferredToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getPreferredProperty(), term);
    }

    public void addPreferredLongToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getPreferredLongProperty(), term);
    }

    public void addNotesToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getNoteProperty(), term);
    }

    public void addCodingHintToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getCodingHintProperty(), term);
    }

    public void addIntroductionToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getIntroductionProperty(), term);
    }

    public void addRdfsLabelToTerm(RDFResource term, String label, String lang) {
        //term.addLabel(label, lang); //TODO: BP does not handle languages in literals, so ignore for the moment
        term.addLabel(label, null);
    }

    /*
     * Claml References
     */

    @SuppressWarnings("deprecation")
    public RDFResource createClamlReference() {
        return (RDFResource) owlModel.createInstance(null, getClamlReferencesClass());
    }

    public void fillClamlReference(RDFResource clamlRef, String text, String usage, RDFResource ref) {
        if (text != null) {
            clamlRef.addPropertyValue(getTextProperty(), text);
        }
        if (usage != null) {
            clamlRef.addPropertyValue(getUsageProperty(), usage);
        }
        if (ref != null) {
            clamlRef.addPropertyValue(getIcdRefProperty(), ref);
        }
    }

    public void fillClamlReference(RDFResource clamlRef, String text, String usage, String refName) {
        RDFResource ref = null;
        if (refName != null) {
            ref = getICDClass(refName, true);
        }
        fillClamlReference(clamlRef, text, usage, ref);
    }

    public void addClamlRefToTerm(RDFResource term, RDFResource ref) {
        term.addPropertyValue(getClamlReferencesProperty(), ref);
    }

}
