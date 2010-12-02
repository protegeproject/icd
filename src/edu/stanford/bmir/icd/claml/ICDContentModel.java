package edu.stanford.bmir.icd.claml;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ICDContentModel {

    private static transient Logger log = Log.getLogger(ICDContentModel.class);

    private final OWLModel owlModel;

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
    private RDFSNamedClass specificConditionMetaClass;
    private RDFSNamedClass externalCauseMetaClass;

    private RDFSNamedClass linearizationViewClass;
    private RDFSNamedClass linearizationSpecificationClass;

    private Collection<RDFSNamedClass> diseaseMetaclasses;
    private Collection<RDFSNamedClass> externalCausesMetaclasses;

    private Collection<RDFResource> linearizationValueSet;

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
    private RDFSNamedClass termDefinitionClass;
    private RDFSNamedClass termReferenceClass;
    private RDFSNamedClass termIndexClass;
    private RDFSNamedClass indexTermTypeClass;

    private RDFSNamedClass externalCausesTopClass;

    /*
     * Properties
     */
    private RDFProperty icdTitleProperty;
    private RDFProperty icdCodeProperty;
    private RDFProperty kindProperty;
    private RDFProperty usageProperty;
    private RDFProperty definitionProperty;
    private RDFProperty longDefinitionProperty;
    private RDFProperty prefilledDefinitionProperty;
    private RDFProperty synonymProperty;
    private RDFProperty baseIndexProperty;
    private RDFProperty sortingLabelProperty;

    private RDFProperty inclusionProperty;
    private RDFProperty exclusionProperty;
    private RDFProperty indexTypeProperty;

    private RDFProperty idProperty;
    private RDFProperty labelProperty;
    private RDFProperty langProperty;
    private RDFProperty ontologyIdProperty;

    private RDFProperty icdRefCodeProperty;
    private RDFProperty clamlRefProperty;
    private RDFProperty textProperty;

    private RDFProperty codingHintProperty;
    private RDFProperty introductionProperty;
    private RDFProperty noteProperty;
    private RDFProperty preferredProperty;
    private RDFProperty preferredLongProperty;

    private RDFProperty linearizationProperty;
    private RDFProperty isIncludedInLinearizationProperty;
    private RDFProperty linearizationParentProperty;
    private RDFProperty linearizationViewProperty;
    private RDFProperty linearizationSequenceNoProperty;
    private RDFProperty linearizationSortingLabelProperty;

    private RDFProperty biologicalSexProperty;

    /*
     * Instances
     */

    private RDFResource indexTypeSynoymInst;

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
            clincalDescriptionMetaClass = owlModel
                    .getRDFSNamedClass(ICDContentModelConstants.ICD_CLINICAL_DESC_METACLASS);
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
            functionalImpactMetaClass = owlModel
                    .getRDFSNamedClass(ICDContentModelConstants.ICD_FUNCTIONAL_IMPACT_METACLASS);
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
            diagnosticCriteriaMetaClass = owlModel
                    .getRDFSNamedClass(ICDContentModelConstants.ICD_DIAGNOSTIC_CRITERIA_METACLASS);
        }
        return diagnosticCriteriaMetaClass;
    }

    public RDFSNamedClass getSpecificConditionMetaClass() {
        if (specificConditionMetaClass == null) {
            specificConditionMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_SPECIFIC_CONDITION_METACLASS);
        }
        return specificConditionMetaClass;
    }

    public RDFSNamedClass getExternalCausenMetaClass() {
        if (externalCauseMetaClass == null) {
            externalCauseMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_EXTERNAL_CAUSE_METACLASS);
        }
        return externalCauseMetaClass;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    public Collection<RDFSNamedClass> getRegularDiseaseMetaclasses() {
        if (diseaseMetaclasses == null) {
            diseaseMetaclasses = new ArrayList<RDFSNamedClass>(getICDCategoryClass().getDirectTypes());
        }
        return diseaseMetaclasses;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    public Collection<RDFSNamedClass> getExternalCauseMetaclasses() {
        if (externalCausesMetaclasses == null) {
            externalCausesMetaclasses = new ArrayList<RDFSNamedClass>(getExternalCausesTopClass().getDirectTypes());
        }
        return externalCausesMetaclasses;
    }

    public Collection<RDFResource> getLinearizationValueSet() {
        if (linearizationValueSet == null) {
            linearizationValueSet = new ArrayList<RDFResource>(getLinearizationViewClass().getInstances(true));
        }
        return linearizationValueSet;
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

    public RDFSNamedClass getTermDefinitionClass() {
        if (termDefinitionClass == null) {
            termDefinitionClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_DEFINITION_CLASS);
        }
        return termDefinitionClass;
    }

    public RDFSNamedClass getTermReferenceClass() {
        if (termReferenceClass == null) {
            termReferenceClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_REFERENCE_CLASS);
        }
        return termReferenceClass;
    }

    public RDFSNamedClass getTermSynonymClass() {
        if (termReferenceClass == null) {
            termReferenceClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_SYNONYM_CLASS);
        }
        return termReferenceClass;
    }

    public RDFSNamedClass getTermIndexClass() {
        if (termIndexClass == null) {
            termIndexClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_INDEX_CLASS);
        }
        return termIndexClass;
    }

    public RDFSNamedClass getTermIndexTypeClass() {
        if (indexTermTypeClass == null) {
            indexTermTypeClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.INDEX_TERM_TYPE_CLASS);
        }
        return indexTermTypeClass;
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

    public RDFSNamedClass getExternalCausesTopClass() {
        if (externalCausesTopClass == null) {
            externalCausesTopClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.EXTERNAL_CAUSES_TOP_CLASS);
        }
        return externalCausesTopClass;
    }

    public RDFSNamedClass getLinearizationViewClass() {
        if (linearizationViewClass == null) {
            linearizationViewClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.LINEARIZATION_VIEW_CLASS);
        }
        return linearizationViewClass;
    }

    public RDFSNamedClass getLinearizationSpecificationClass() {
        if (linearizationSpecificationClass == null) {
            linearizationSpecificationClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.LINEARIZATION_SPECIFICATION_CLASS);
        }
        return linearizationSpecificationClass;
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

    public RDFProperty getDefinitionProperty() {
        if (definitionProperty == null) {
            definitionProperty = owlModel.getRDFProperty(ICDContentModelConstants.DEFINITION_PROP);
        }
        return definitionProperty;
    }

    public RDFProperty getLongDefinitionProperty() {
        if (longDefinitionProperty == null) {
            longDefinitionProperty = owlModel.getRDFProperty(ICDContentModelConstants.LONG_DEFINITION_PROP);
        }
        return longDefinitionProperty;
    }

    public RDFProperty getPrefilledDefinitionProperty() {
        if (prefilledDefinitionProperty == null) {
            prefilledDefinitionProperty = owlModel.getRDFProperty(ICDContentModelConstants.PREFILLED_DEFINITION_PROP);
        }
        return prefilledDefinitionProperty;
    }

    public RDFProperty getSynonymProperty() {
        if (synonymProperty == null) {
            synonymProperty = owlModel.getRDFProperty(ICDContentModelConstants.SYNOYM_PROP);
        }
        return synonymProperty;
    }


    public RDFProperty getBaseIndexProperty() {
        if (baseIndexProperty == null) {
            baseIndexProperty = owlModel.getRDFProperty(ICDContentModelConstants.BASE_INDEX_PROP);
        }
        return baseIndexProperty;
    }


    public RDFProperty getIndexTypeProperty() {
        if (indexTypeProperty == null) {
            indexTypeProperty = owlModel.getRDFProperty(ICDContentModelConstants.BASE_INDEX_TYPE_PROP);
        }
        return indexTypeProperty;
    }

    public RDFProperty getSortingLabelProperty() {
        if (sortingLabelProperty == null) {
            sortingLabelProperty = owlModel.getRDFProperty(ICDContentModelConstants.SORTING_LABEL_PROP);
        }
        return sortingLabelProperty;
    }

    public RDFProperty getLangProperty() {
        if (langProperty == null) {
            langProperty = owlModel.getRDFProperty(ICDContentModelConstants.LANG_PROP);
        }
        return langProperty;
    }

    public RDFProperty getOntologyIdProperty() {
        if (ontologyIdProperty == null) {
            ontologyIdProperty = owlModel.getRDFProperty(ICDContentModelConstants.ONTOLOGYID_PROP);
        }
        return ontologyIdProperty;
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

    public RDFProperty getLinearizationProperty() {
        if (linearizationProperty == null) {
            linearizationProperty = owlModel.getRDFProperty(ICDContentModelConstants.LINEARIZATION_PROP);
        }
        return linearizationProperty;
    }

    public RDFProperty getIsIncludedInLinearizationProperty() {
        if (isIncludedInLinearizationProperty == null) {
            isIncludedInLinearizationProperty = owlModel.getRDFProperty(ICDContentModelConstants.IS_INCLUDED_IN_LINEARIZATION_PROP);
        }
        return isIncludedInLinearizationProperty;
    }

    public RDFProperty getLinearizationParentProperty() {
        if (linearizationParentProperty == null) {
            linearizationParentProperty = owlModel.getRDFProperty(ICDContentModelConstants.LINEARIZATION_PARENT_PROP);
        }
        return linearizationParentProperty;
    }

    public RDFProperty getLinearizationViewProperty() {
        if (linearizationViewProperty == null) {
            linearizationViewProperty = owlModel.getRDFProperty(ICDContentModelConstants.LINEARIZATION_VIEW_PROP);
        }
        return linearizationViewProperty;
    }

    public RDFProperty getLinearizationSequenceNoProperty() {
        if (linearizationSequenceNoProperty == null) {
            linearizationSequenceNoProperty = owlModel.getRDFProperty(ICDContentModelConstants.LINEARIZATION_SEQUENCE_NO_PROP);
        }
        return linearizationSequenceNoProperty;
    }

    public RDFProperty getLinearizationSortingLabelProperty() {
        if (linearizationSortingLabelProperty == null) {
            linearizationSortingLabelProperty = owlModel.getRDFProperty(ICDContentModelConstants.LINEARIZATION_SORTING_LABEL_PROP);
        }
        return linearizationSortingLabelProperty;
    }

    public RDFProperty getBiologicalSexProperty() {
        if (biologicalSexProperty == null) {
            biologicalSexProperty = owlModel.getRDFProperty(ICDContentModelConstants.BIOLOGICAL_SEX_PROP);
        }
        return biologicalSexProperty;
    }

    /*
     * Getters for instances
     */

    public RDFResource getIndexTypeSynonymInst() {
        if (indexTypeSynoymInst == null) {
            indexTypeSynoymInst = owlModel.getRDFResource(ICDContentModelConstants.INDEX_TYPE_SYNONYM_INST);
        }
        return indexTypeSynoymInst;
    }


    /*
     * Create methods
     */

    public RDFSNamedClass createICDCategory(String name, String superclsName) {
        return createICDCategory(name, CollectionUtilities.createCollection(superclsName), true, true); //method is used by the CLAML parser
    }

    public RDFSNamedClass createICDCategory(String name, Collection<String> superclsesName) {
        return createICDCategory(name, superclsesName, false, true);
    }

    /**
     * Creates an ICD Category under the given parents. Default actions:
     * <ul>
     * <li>Add the correct metaclasses (if regular disease, use metaclasses of ICDCategory; if subclass of External causes, use metaclasses of
     * External Causes</li>
     * <li>Create the linearization values: morbidity - is included, and mortality - is not included</li>
     * <li>Set the biologicalSex to NA </li>
     * </ul>
     *
     * @param name                      - name of the new category
     * @param superclsesName            - names of the parents
     * @param createSuperclasses        - true to create parents, if they don't already exist (only the CLAML parser needs to set this to true, all the rest, should use false)
     * @param createICDSpecificEntities
     * @return
     */
    @SuppressWarnings("deprecation")
    public RDFSNamedClass createICDCategory(String name, Collection<String> superclsesName, boolean createSuperclasses, boolean createICDSpecificEntities) {
        if (name == null) {
            name = IDGenerator.getNextUniqueId();
        }
        RDFSNamedClass cls = getICDClass(name, true);

        Collection<RDFSNamedClass> superclses = new ArrayList<RDFSNamedClass>();

        //we could treat also the case when a class has an external cause and another normal disease as parents..

        if (superclsesName == null || superclsesName.size() == 0) {
            superclses.add(getICDCategoryClass());
        } else {
            for (String superclsName : superclsesName) {
                RDFSNamedClass supercls = getICDClass(superclsName, createSuperclasses);
                if (supercls != null) {
                    superclses.add(supercls);
                    //add superclasses
                    if (!cls.getSuperclasses(true).contains(supercls)) {
                        cls.addSuperclass(supercls);
                        if (cls.hasDirectSuperclass(owlModel.getOWLThingClass())) {
                            cls.removeSuperclass(owlModel.getOWLThingClass());
                        }
                        cls.setDirectTypes(supercls.getProtegeTypes());
                    }
                }
            }
        }

        if (createICDSpecificEntities) {

            //add linearization instances
            for (RDFResource linView : getLinearizationValueSet()) {
                RDFResource linSpec = getLinearizationSpecificationClass().createInstance(IDGenerator.getNextUniqueId());
                linSpec.setPropertyValue(getLinearizationViewProperty(), linView);

                cls.addPropertyValue(getLinearizationProperty(), linSpec);
                //default for new properties: morbidity: included; mortality: not included
                if (linView.getName().equals(ICDContentModelConstants.LINEARIZATION_VIEW_MORBIDITY)) {
                    linSpec.setPropertyValue(getIsIncludedInLinearizationProperty(), Boolean.TRUE);
                } else if (linView.getName().equals(ICDContentModelConstants.LINEARIZATION_VIEW_MORTALITY)) {
                    linSpec.setPropertyValue(getIsIncludedInLinearizationProperty(), Boolean.FALSE);
                }
            }

            //set biologicalSex not applicable
            cls.addPropertyValue(getBiologicalSexProperty(), owlModel.getRDFResource(ICDContentModelConstants.BIOLOGICAL_SEX_NA));
        }

        return cls;
    }

    /**
     * It gets or creates and ICDClass. If it creates, it will not add the metaclasses.
     * To create an ICDMetaclass, it is better to use {@link #createICDCategory(String, Collection)}
     *
     * @param name   - name of the class to be retrieved or created
     * @param create - true to create class if it doesn't exit
     * @return - the class
     */
    private RDFSNamedClass getICDClass(String name, boolean create) {
        RDFSNamedClass cls = owlModel.getRDFSNamedClass(name);
        if (cls == null && create) {
            cls = owlModel.createOWLNamedClass(name);
            cls.addSuperclass(owlModel.getOWLThingClass());
        }
        return cls;
    }

    public RDFSNamedClass getICDClass(String name) {
        return owlModel.getRDFSNamedClass(name);
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
        RDFResource term = (RDFResource) owlModel.createInstance(IDGenerator.getNextUniqueId(), CollectionUtilities.createCollection(type));
        return term;
    }

    public void fillTerm(RDFResource term, String id, String label, String lang) {
        fillTerm(term, id, label, lang, null);
    }

    public void fillTerm(RDFResource term, String id, String label, String lang, String ontology) {
        if (id != null) {
            term.addPropertyValue(getIdProperty(), id);
        }
        if (label != null) {
            term.addPropertyValue(getLabelProperty(), label);
        }
        if (lang != null) {
            term.addPropertyValue(getLangProperty(), lang);
        }
        if (ontology != null) {
            term.addPropertyValue(getOntologyIdProperty(), ontology);
        }
    }

    protected void addTermToClass(RDFSNamedClass cls, RDFProperty prop, RDFResource term) {
        cls.addPropertyValue(prop, term);
    }

    public RDFResource createDefinitionTerm() {
        return createTerm(getTermDefinitionClass());
    }

    public RDFResource createReferenceTerm() {
        return createTerm(getTermReferenceClass());
    }

    public RDFResource createSynonymTerm() {
        return createTerm(getTermSynonymClass());
    }

    public RDFResource createTitleTerm() {
        return createTerm(getTermTitleClass());
    }

    public void addTitleTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getIcdTitleProperty(), term);
    }

    public void addDefinitionTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getDefinitionProperty(), term);
    }

    public void addPrefilledDefinitionTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getPrefilledDefinitionProperty(), term);
    }

    public void addSynonymTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getSynonymProperty(), term);
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


    /*
     * Getters
     */

    @SuppressWarnings("unchecked")
    private Collection<RDFSNamedClass> getRDFSNamedClassCollection(Collection someColl) {
        if (someColl == null) {
            return null;
        }
        Set<RDFSNamedClass> coll = new LinkedHashSet<RDFSNamedClass>();
        for (Iterator iterator = someColl.iterator(); iterator.hasNext();) {
            Object cls = iterator.next();
            if (cls instanceof RDFSNamedClass) {
                coll.add((RDFSNamedClass) cls);
            }
        }
        return coll;
    }

    /**
     * Returns a set of all ICD Categories from the entire category tree.
     * This is a very expensive method and should only be used if necessary.
     *
     * @return the closure of all ICD classes in the tree
     */
    public Collection<RDFSNamedClass> getICDCategories() {
        return getRDFSNamedClassCollection(getICDCategoryClass().getSubclasses(true));

    }

    /**
     * Returns the direct children of the ICD class given as argument.
     *
     * @param icdClass
     * @return the childre of the class
     */
    public Collection<RDFSNamedClass> getChildren(RDFSNamedClass icdClass) {
        return getRDFSNamedClassCollection(icdClass.getSubclasses(false));
    }

    /**
     * Returns the ICD Category for the given id.
     * The id corresponds to the Protege class full name
     * (e.g., http://who.int/icd#L42.5)
     *
     * @param id
     * @return
     */
    public RDFSNamedClass getICDCategory(String id) {
        return owlModel.getRDFSNamedClass(id);
    }

    public RDFResource getTerm(RDFSNamedClass icdClass, RDFProperty icdTermProp) {
        return (RDFResource) icdClass.getPropertyValue(icdTermProp);
    }

    public Collection<RDFResource> getTerms(RDFSNamedClass icdClass, RDFProperty icdTermProp) {
        return (Collection<RDFResource>) icdClass.getPropertyValue(icdTermProp);
    }

    public Collection<RDFResource> getLinearizationSpecifications(RDFSNamedClass icdClass) {
        return icdClass.getPropertyValues(getLinearizationProperty());
    }

}
