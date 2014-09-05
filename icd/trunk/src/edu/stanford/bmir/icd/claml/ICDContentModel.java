package edu.stanford.bmir.icd.claml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLExistentialRestriction;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLRestriction;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

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
    private RDFSNamedClass linearizationHistoricSpecificationClass;

    private RDFSNamedClass postcoordinationAxesSpecificationClass;

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
    private RDFSNamedClass termExternalDefinitionClass;
    private RDFSNamedClass termReferenceClass;
    private RDFSNamedClass termSnomedReferenceClass;
    private RDFSNamedClass termSynonymClass;
    private RDFSNamedClass termIndexClass;
    private RDFSNamedClass indexTermTypeClass;
    private RDFSNamedClass termNarrowerClass;
    private RDFSNamedClass termBaseIndexClass;
    private RDFSNamedClass termBaseInclusionClass;
    private RDFSNamedClass termBaseExclusionClass;

    private RDFSNamedClass externalCausesTopClass;
    private RDFSNamedClass childOrderClass;
    private RDFSNamedClass chapterXClass;

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
    private RDFProperty narrowerProperty;
    private RDFProperty baseIndexProperty;
    private RDFProperty baseInclusionProperty;
    private RDFProperty indexBaseInclusionProperty;
    private RDFProperty subclassBaseInclusionProperty;
    private RDFProperty baseExclusionProperty;
    private RDFProperty sortingLabelProperty;
    private RDFProperty externalReferenceProperty;

    private RDFProperty inclusionProperty;
    private RDFProperty exclusionProperty;
    private RDFProperty indexTypeProperty;

    private RDFProperty idProperty;
    private RDFProperty labelProperty;
    private RDFProperty langProperty;
    private RDFProperty ontologyIdProperty;
    private RDFProperty termIdProperty;

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
    private RDFProperty isGroupingProperty;
    private RDFProperty linearizationParentProperty;
    private RDFProperty linearizationViewProperty;
    private RDFProperty linearizationICD10ViewProperty;
    private RDFProperty linearizationICD10TabulationViewProperty;
    private RDFProperty linearizationSequenceNoProperty;
    private RDFProperty linearizationSortingLabelProperty;

    private RDFProperty suppressOtherSpecifiedResidualsProperty;
    private RDFProperty suppressUnspecifiedResidualsProperty;
    private RDFProperty otherSpecifiedResidualTitleProperty;
    private RDFProperty unspecifiedResidualTitleProperty;

    private RDFProperty biologicalSexProperty;

    private RDFProperty assignedTagProperty;
    private RDFProperty assignedPrimaryTagProperty;
    private RDFProperty assignedSecondaryTagProperty;
    private RDFProperty displayStatusProperty;

    private RDFProperty allowedPostcoordinationAxesProperty;
    private RDFProperty allowedPostcoordinationAxisPropertyProperty;
    private RDFProperty requiredPostcoordinationAxisPropertyProperty;
    private RDFProperty precoordinationSuperclassProperty;

    private RDFProperty isObsoleteProperty;
    private RDFProperty publicIdProperty;

    private RDFProperty childrenOrderProperty;
    private RDFProperty orderedChildIndexProperty;
    private RDFProperty orderedChildProperty;

    private RDFProperty referencedValueProperty;

    /*
     * Instances
     */

    private RDFResource indexTypeSynoymInst;

    private RDFResource displayStatusBlue;
    private RDFResource displayStatusYellow;
    private RDFResource displayStatusRed;

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

    public RDFSNamedClass getTermExternalDefinitionClass() {
        if (termExternalDefinitionClass == null) {
            termExternalDefinitionClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_EXTERNAL_DEFINITION_CLASS);
        }
        return termExternalDefinitionClass;
    }

    public RDFSNamedClass getTermReferenceClass() {
        if (termReferenceClass == null) {
            termReferenceClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_REFERENCE_CLASS);
        }
        return termReferenceClass;
    }

    public RDFSNamedClass  getTermSnomedReferenceClass() {
        if (termSnomedReferenceClass == null) {
            termSnomedReferenceClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_SNOMED_REFERENCE_CLASS);
        }
        return termSnomedReferenceClass;
    }

    public RDFSNamedClass getTermSynonymClass() {
        if (termSynonymClass == null) {
            termSynonymClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_SYNONYM_CLASS);
        }
        return termSynonymClass;
    }

    public RDFSNamedClass getTermNarrowerClass() {
        if (termNarrowerClass == null) {
            termNarrowerClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_NARROWER_CLASS);
        }
        return termNarrowerClass;
    }

    public RDFSNamedClass getTermBaseIndexClass() {
        if (termBaseIndexClass == null) {
            termBaseIndexClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_BASE_INDEX_CLASS);
        }
        return termBaseIndexClass;
    }

    public RDFSNamedClass getTermBaseInclusionClass() {
        if (termBaseInclusionClass == null) {
            termBaseInclusionClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_BASE_INCLUSION_CLASS);
        }
        return termBaseInclusionClass;
    }

    public RDFSNamedClass getTermBaseExclusionClass() {
        if (termBaseExclusionClass == null) {
            termBaseExclusionClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_BASE_EXCLUSION_CLASS);
        }
        return termBaseExclusionClass;
    }


    @Deprecated
    public RDFSNamedClass getTermIndexClass() {
        if (termIndexClass == null) {
            termIndexClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_INDEX_CLASS);
        }
        return termIndexClass;
    }

    @Deprecated
    public RDFSNamedClass getTermIndexTypeClass() {
        if (indexTermTypeClass == null) {
            indexTermTypeClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.INDEX_TERM_TYPE_CLASS);
        }
        return indexTermTypeClass;
    }

    @Deprecated
    public RDFSNamedClass getTermInclusionClass() {
        if (termInclusionClass == null) {
            termInclusionClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.TERM_INCLUSION_CLASS);
        }
        return termInclusionClass;
    }

    @Deprecated
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

    public RDFSNamedClass getLinearizationHistoricSpecificationClass() {
        if (linearizationHistoricSpecificationClass == null) {
            linearizationHistoricSpecificationClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.LINEARIZATION_HISTORIC_SPECIFICATION_CLASS);
        }
        return linearizationHistoricSpecificationClass;
    }

    public RDFSNamedClass getPostcoordinationAxesSpecificationClass() {
        if (postcoordinationAxesSpecificationClass == null) {
            postcoordinationAxesSpecificationClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.POSTCOORDINATION_AXES_SPECIFICATION_CLASS);
        }
        return postcoordinationAxesSpecificationClass;
    }

    public RDFSNamedClass getChildOrderClass() {
        if (childOrderClass == null) {
            childOrderClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.CHILD_ORDER_CLASS);
        }
        return childOrderClass;
    }

    public RDFSNamedClass getChapterXClass() {
        if (chapterXClass == null) {
            chapterXClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.CHAPTER_X_CLASS);
        }
        return chapterXClass;
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

    public RDFProperty getNarrowerProperty() {
        if (narrowerProperty == null) {
            narrowerProperty = owlModel.getRDFProperty(ICDContentModelConstants.NARROWER_PROP);
        }
        return narrowerProperty;
    }

    public RDFProperty getBaseIndexProperty() {
        if (baseIndexProperty == null) {
            baseIndexProperty = owlModel.getRDFProperty(ICDContentModelConstants.BASE_INDEX_PROP);
        }
        return baseIndexProperty;
    }

    public RDFProperty getBaseInclusionProperty() {
        if (baseInclusionProperty == null) {
            baseInclusionProperty = owlModel.getRDFProperty(ICDContentModelConstants.BASE_INCLUSION_PROP);
        }
        return baseInclusionProperty;
    }

    public RDFProperty getIndexBaseInclusionProperty() {
        if (indexBaseInclusionProperty == null) {
            indexBaseInclusionProperty = owlModel.getRDFProperty(ICDContentModelConstants.INDEX_BASE_INCLUSION_PROP);
        }
        return indexBaseInclusionProperty;
    }

    public RDFProperty getSubclassBaseInclusionProperty() {
        if (subclassBaseInclusionProperty == null) {
            subclassBaseInclusionProperty = owlModel.getRDFProperty(ICDContentModelConstants.SUBCLASS_BASE_INCLUSION_PROP);
        }
        return subclassBaseInclusionProperty;
    }

    public RDFProperty getBaseExclusionProperty() {
        if (baseExclusionProperty == null) {
            baseExclusionProperty = owlModel.getRDFProperty(ICDContentModelConstants.BASE_EXCLUSION_PROP);
        }
        return baseExclusionProperty;
    }


    public RDFProperty getExternalReferenceProperty() {
        if (externalReferenceProperty == null) {
            externalReferenceProperty = owlModel.getRDFProperty(ICDContentModelConstants.EXTERNAL_REFERENCE_PROP);
        }
        return externalReferenceProperty;
    }

    @Deprecated
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

    @Deprecated
    public RDFProperty getInclusionProperty() {
        if (inclusionProperty == null) {
            inclusionProperty = owlModel.getRDFProperty(ICDContentModelConstants.INCLUSION_PROP);
        }
        return inclusionProperty;
    }

    @Deprecated
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

    public RDFProperty getLinearizationICD10Property() {
        if (linearizationICD10ViewProperty == null) {
            linearizationICD10ViewProperty = owlModel.getRDFProperty(ICDContentModelConstants.LINEARIZATION_ICD_10_PROP);
        }
        return linearizationICD10ViewProperty;
    }

    public RDFProperty getLinearizationICD10TabulationProperty() {
        if (linearizationICD10TabulationViewProperty == null) {
            linearizationICD10TabulationViewProperty = owlModel.getRDFProperty(ICDContentModelConstants.LINEARIZATION_ICD_10_TABULATION_PROP);
        }
        return linearizationICD10TabulationViewProperty;
    }

    public RDFProperty getIsIncludedInLinearizationProperty() {
        if (isIncludedInLinearizationProperty == null) {
            isIncludedInLinearizationProperty = owlModel.getRDFProperty(ICDContentModelConstants.IS_INCLUDED_IN_LINEARIZATION_PROP);
        }
        return isIncludedInLinearizationProperty;
    }

    public RDFProperty getIsGroupingProperty() {
        if (isGroupingProperty == null) {
            isGroupingProperty = owlModel.getRDFProperty(ICDContentModelConstants.IS_GROUPING_PROP);
        }
        return isGroupingProperty;
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

    public RDFProperty getSuppressOtherSpecifiedResidualsProperty() {
    	if (suppressOtherSpecifiedResidualsProperty == null) {
    		suppressOtherSpecifiedResidualsProperty = owlModel.getRDFProperty(ICDContentModelConstants.SUPPRESS_OTHER_SPECIFIED_RESIDUALS);

    	}
    	return suppressOtherSpecifiedResidualsProperty;
    }

    public RDFProperty getSuppressUnspecifiedResidualsProperty() {
    	if (suppressUnspecifiedResidualsProperty == null) {
    		suppressUnspecifiedResidualsProperty = owlModel.getRDFProperty(ICDContentModelConstants.SUPPRESS_UNSPECIFIED_RESIDUALS);

    	}
    	return suppressUnspecifiedResidualsProperty;
    }

    public RDFProperty getOtherSpecifiedResidualTitleProperty() {
    	if (otherSpecifiedResidualTitleProperty == null) {
    		otherSpecifiedResidualTitleProperty = owlModel.getRDFProperty(ICDContentModelConstants.OTHER_SPECIFIED_RESIDUAL_TITLE);

    	}
    	return otherSpecifiedResidualTitleProperty;
    }

    public RDFProperty getUnspecifiedResidualTitleProperty() {
    	if (unspecifiedResidualTitleProperty == null) {
    		unspecifiedResidualTitleProperty = owlModel.getRDFProperty(ICDContentModelConstants.UNSPECIFIED_RESIDUAL_TITLE);

    	}
    	return unspecifiedResidualTitleProperty;
    }

    public RDFProperty getBiologicalSexProperty() {
        if (biologicalSexProperty == null) {
            biologicalSexProperty = owlModel.getRDFProperty(ICDContentModelConstants.BIOLOGICAL_SEX_PROP);
        }
        return biologicalSexProperty;
    }

    public RDFProperty getAssignedTagProperty() {
        if (assignedTagProperty == null) {
            assignedTagProperty = owlModel.getRDFProperty(ICDContentModelConstants.ASSIGNED_TAG_PROP);
        }
        return assignedTagProperty;
    }

    public RDFProperty getAssignedPrimaryTagProperty() {
        if (assignedPrimaryTagProperty == null) {
            assignedPrimaryTagProperty = owlModel.getRDFProperty(ICDContentModelConstants.ASSIGNED_PRIMARY_TAG_PROP);
        }
        return assignedPrimaryTagProperty;
    }

    public RDFProperty getAssignedSecondaryTagProperty() {
        if (assignedSecondaryTagProperty == null) {
            assignedSecondaryTagProperty = owlModel.getRDFProperty(ICDContentModelConstants.ASSIGNED_SECONDARY_TAG_PROP);
        }
        return assignedSecondaryTagProperty;
    }

    public RDFProperty getDisplayStatusProperty() {
        if (displayStatusProperty == null) {
            displayStatusProperty = owlModel.getRDFProperty(ICDContentModelConstants.DISPLAY_STATUS_PROP);
        }
        return displayStatusProperty;
    }

    public RDFProperty getAllowedPostcoordinationAxesProperty() {
        if (allowedPostcoordinationAxesProperty == null) {
            allowedPostcoordinationAxesProperty = owlModel.getRDFProperty(ICDContentModelConstants.ALLOWED_POSTCOORDINATION_AXES_PROP);
        }
        return allowedPostcoordinationAxesProperty;
    }

    public RDFProperty getAllowedPostcoordinationAxisPropertyProperty() {
        if (allowedPostcoordinationAxisPropertyProperty == null) {
            allowedPostcoordinationAxisPropertyProperty = owlModel.getRDFProperty(ICDContentModelConstants.ALLOWED_POSTCOORDINATION_AXIS_PROPERTY_PROP);
        }
        return allowedPostcoordinationAxisPropertyProperty;
    }

    public RDFProperty getRequiredPostcoordinationAxisPropertyProperty() {
        if (requiredPostcoordinationAxisPropertyProperty == null) {
            requiredPostcoordinationAxisPropertyProperty = owlModel.getRDFProperty(ICDContentModelConstants.REQUIRED_POSTCOORDINATION_AXIS_PROPERTY_PROP);
        }
        return requiredPostcoordinationAxisPropertyProperty;
    }

    public RDFProperty getPrecoordinationSuperclassProperty() {
    	if (precoordinationSuperclassProperty == null) {
    		precoordinationSuperclassProperty = owlModel.getRDFProperty(ICDContentModelConstants.PRECOORDINATION_SUPERCLASS_PROP);
    	}
    	return precoordinationSuperclassProperty;
    }

    public RDFProperty getIsObsoleteProperty() {
        if (isObsoleteProperty == null) {
            isObsoleteProperty = owlModel.getRDFProperty(ICDContentModelConstants.IS_OBSOLETE_PROP);
        }
        return isObsoleteProperty;
    }

    public RDFProperty getPublicIdProperty() {
        if (publicIdProperty == null) {
            publicIdProperty = owlModel.getRDFProperty(ICDContentModelConstants.PUBLIC_ID_PROP);
        }
        return publicIdProperty;
    }


    public RDFProperty getChildrenOrderProperty() {
        if (childrenOrderProperty == null) {
            childrenOrderProperty = owlModel.getRDFProperty(ICDContentModelConstants.CHILDREN_ORDER_PROP);
        }
        return childrenOrderProperty;
    }

    public RDFProperty getOrderedChildIndexProperty() {
        if (orderedChildIndexProperty == null) {
            orderedChildIndexProperty = owlModel.getRDFProperty(ICDContentModelConstants.ORDERED_CHILD_INDEX_PROP);
        }
        return orderedChildIndexProperty;
    }

    public RDFProperty getOrderedChildProperty() {
        if (orderedChildProperty == null) {
            orderedChildProperty = owlModel.getRDFProperty(ICDContentModelConstants.ORDERED_CHILD_PROP);
        }
        return orderedChildProperty;
    }

    public RDFProperty getTermIdProperty() {
        if (termIdProperty == null) {
            termIdProperty = owlModel.getRDFProperty(ICDContentModelConstants.TERM_ID_PROP);
        }
        return termIdProperty;
    }

    public RDFProperty getReferencedValueProperty() {
        if (referencedValueProperty == null) {
            referencedValueProperty = owlModel.getRDFProperty(ICDContentModelConstants.REFERENCED_VALUE_PROP);
        }
        return referencedValueProperty;
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

    public RDFResource getDisplayStatusBlueInst() {
        if (displayStatusBlue == null) {
            displayStatusBlue = owlModel.getRDFResource(ICDContentModelConstants.DISPLAY_STATUS_BLUE);
        }
        return displayStatusBlue;
    }

    public RDFResource getDisplayStatusYellowInst() {
        if (displayStatusYellow == null) {
            displayStatusYellow = owlModel.getRDFResource(ICDContentModelConstants.DISPLAY_STATUS_YELLOW);
        }
        return displayStatusYellow;
    }

    public RDFResource getDisplayStatusRedInst() {
        if (displayStatusRed == null) {
            displayStatusRed = owlModel.getRDFResource(ICDContentModelConstants.DISPLAY_STATUS_RED);
        }
        return displayStatusRed;
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
            createICDSpecificEntities(cls);
        }

        return cls;
    }

    private void createICDSpecificEntities(RDFSNamedClass cls) {
        /*
         * Create the linearization instances for the newly created class. The linearization views are taken from the parents.
         * They are created separately for the ICD-11 linearizzations, the ICD-10 linearizations, and ICD-10 tabulation lists
         */
        createLinearizationSpecifications(cls);

        /*
         * Create the post-coordination instances for the newly created class. The linearization views are taken from the parents.
         * They are created separately for the ICD-11 linearizzations, the ICD-10 linearizations, and ICD-10 tabulation lists
         */
        createPostcoordinationSpecifications(cls);

        //set biologicalSex - default value: N/A (not applicable)
        cls.addPropertyValue(getBiologicalSexProperty(), owlModel.getRDFResource(ICDContentModelConstants.BIOLOGICAL_SEX_NA));
    }


    private void createLinearizationSpecifications(RDFSNamedClass cls) {
        //ICD-11 linearizations
        createLinearizationSpecifications(cls, getLinearizationSpecificationClass(), getLinearizationProperty());
        //ICD-10 linearizations
        createLinearizationSpecifications(cls, getLinearizationHistoricSpecificationClass(), getLinearizationICD10Property());
        //ICD-10 tabulation lists
        createLinearizationSpecifications(cls, getLinearizationHistoricSpecificationClass(), getLinearizationICD10TabulationProperty());
    }

    private void createLinearizationSpecifications(RDFSNamedClass cls, RDFSNamedClass linSpecificationClass, RDFProperty linProp) {
        for (RDFResource linView : getLinearizationViewsFromParents(cls, linProp)) {
            RDFResource linSpec = linSpecificationClass.createInstance(IDGenerator.getNextUniqueId());
            linSpec.setPropertyValue(getLinearizationViewProperty(), linView);
            //set default grouping to FALSE
            linSpec.setPropertyValue(getIsGroupingProperty(), Boolean.FALSE);

            cls.addPropertyValue(linProp, linSpec);

            /* These only apply to the ICD-11 linearizations, but it is easier to make them for all. It won't have any effect on the historic linearization specifications */
            /* set the default for new categories: morbidity - included; mortality - not included */
            if (linView.getName().equals(ICDContentModelConstants.LINEARIZATION_VIEW_MORBIDITY)) {
                linSpec.setPropertyValue(getIsIncludedInLinearizationProperty(), Boolean.TRUE);
            } else if (linView.getName().equals(ICDContentModelConstants.LINEARIZATION_VIEW_MORTALITY)) {
                linSpec.setPropertyValue(getIsIncludedInLinearizationProperty(), Boolean.FALSE);
            }
        }
    }

    private Collection<RDFResource> getLinearizationViewsFromParents(RDFSNamedClass cls, RDFProperty linProp) {
        Collection<RDFResource> linViews = new ArrayList<RDFResource>();

        for (Object parent : cls.getSuperclasses(false)) {
            if (parent instanceof RDFSNamedClass) {
                linViews.addAll(getLinearizationViewsFromCls((RDFSNamedClass) parent, linProp));
            }
        }
        return linViews;
    }

    private Collection<RDFResource> getLinearizationViewsFromCls(RDFSNamedClass parentCls, RDFProperty linProp) {
        Collection<RDFResource> linViews = new ArrayList<RDFResource>();
        Collection<RDFResource> linearizationSpecs = getLinearizationSpecificationsForProp(parentCls, linProp);

        for (RDFResource linearizationSpec : linearizationSpecs) {
            RDFResource linearizationView = (RDFResource) linearizationSpec.getPropertyValue(getLinearizationViewProperty());
            if (linearizationView != null) {
                linViews.add(linearizationView);
            }
        }

        return linViews;
    }

    private Collection<RDFResource> getLinearizationSpecificationsForProp(RDFSNamedClass parentCls, RDFProperty linProp) {
        return parentCls.getPropertyValues(linProp);
    }


    private void createPostcoordinationSpecifications(RDFSNamedClass cls) {
        //allowedPostcoordinationAxes
        createPostcoordinationSpecifications(cls, getPostcoordinationAxesSpecificationClass(), getAllowedPostcoordinationAxesProperty());
    }

    private void createPostcoordinationSpecifications(RDFSNamedClass cls, RDFSNamedClass pcAxesSpecificationClass, RDFProperty pcAxesProp) {
        for (RDFResource linView : getLinearizationViewsFromParents(cls, pcAxesProp)) {
            RDFResource linSpec = pcAxesSpecificationClass.createInstance(IDGenerator.getNextUniqueId());
            linSpec.setPropertyValue(getLinearizationViewProperty(), linView);

            cls.addPropertyValue(pcAxesProp, linSpec);

            /* See if we need to do some default initialization similarly to the linearization specifications */
        }
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

    public RDFResource createTerm(RDFSNamedClass type) {
        RDFResource term = (RDFResource) owlModel.createInstance(IDGenerator.getNextUniqueId(), CollectionUtilities.createCollection(type));
        return term;
    }

    public void fillTerm(RDFResource term, String id, String label, String lang) {
        fillTerm(term, id, label, lang, null);
    }

    public void fillTerm(RDFResource term, String id, String label, String lang, String ontology) {
        if (id != null && id.isEmpty() == false) {
            term.addPropertyValue(getIdProperty(), id);
        }
        if (label != null && label.isEmpty() == false) {
            term.addPropertyValue(getLabelProperty(), label);
        }
        if (lang != null && lang.isEmpty() == false) {
            term.addPropertyValue(getLangProperty(), lang);
        }
        if (ontology != null && ontology.isEmpty() == false) {
            term.addPropertyValue(getOntologyIdProperty(), ontology);
        }
    }

    protected void addTermToClass(RDFSNamedClass cls, RDFProperty prop, RDFResource term) {
        cls.addPropertyValue(prop, term);
    }

    public RDFResource createDefinitionTerm() {
        return createTerm(getTermDefinitionClass());
    }

    public RDFResource createExternalDefinitionTerm() {
        return createTerm(getTermExternalDefinitionClass());
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

    public RDFResource createSnomedReferenceTerm() {
        return createTerm(getTermSnomedReferenceClass());
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

    public void addNarrowerTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getNarrowerProperty(), term);
    }

    public void addBaseInclusionTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getIndexBaseInclusionProperty(), term);
    }

    public void addSubclassInclusionTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getSubclassBaseInclusionProperty(), term);
    }

    public void addBaseExclusionTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getBaseExclusionProperty(), term);
    }

    @Deprecated
    public RDFResource createInclusionTerm() {
        return createTerm(getTermInclusionClass());
    }

    @Deprecated
    public void addInclusionTermToClass(RDFSNamedClass cls, RDFResource term) {
        addTermToClass(cls, getInclusionProperty(), term);
    }

    @Deprecated
    public RDFResource createExclusionTerm() {
        return createTerm(getTermExclusionClass());
    }

    @Deprecated
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


    public RDFResource createOrderedChildIndex(RDFSNamedClass child, int index) {
        RDFResource indexInst = getChildOrderClass().createRDFIndividual(IDGenerator.getNextUniqueId());
        indexInst.setPropertyValue(getOrderedChildProperty(), child);
        indexInst.setPropertyValue(getOrderedChildIndexProperty(), index);
        return indexInst;
    }


    /*
     * Getters
     */

    @SuppressWarnings("unchecked")
    public Collection<RDFSNamedClass> getRDFSNamedClassCollection(Collection someColl) {
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

    @SuppressWarnings("unchecked")
    public  List<RDFSNamedClass> getRDFSNamedClassList(Collection someColl) {
        if (someColl == null) {
            return null;
        }
        List<RDFSNamedClass> coll = new ArrayList<RDFSNamedClass>();
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

    /**
     * Returns the public ID of the class, where the argument it the iCAT ID.
     * @param id - the iCAT ID
     * @return - the public ID of the class; returns null, if the ICD class does not exist, or it has no assigned public ID
     */
    public String getPublicId(String id) {
        return getPublicId(getICDCategory(id));
    }

    /**
     * Returns the public ID of the ICD class.
     * @param icdClass - the ICD class as an RDFSNamedClass (returned from other calls of the API
     * @return - the public ID of the class; returns null, if the ICD class does not have a public ID
     */
    public String getPublicId(RDFSNamedClass icdClass) {
        if (icdClass == null) {
            return null;
        }
        return (String) icdClass.getPropertyValue(getPublicIdProperty());
    }

    /**
     * Returns the ICD entity (ICD category, ICD term, ICD property, etc.) with the given public ID.
     * @param publicId - the public ID of the entity
     * @return -  the ICD entity as a RDFResource; returns null, if an entity with this public ID does not exist;
     * throws a runtime exception if more than one ICD entities have the same public ID (should never happen).
     */
    @SuppressWarnings("unchecked")
    public RDFResource getICDEntityByPublicId(String publicId) {
        Collection<RDFResource> matches = owlModel.getMatchingResources(getPublicIdProperty(), publicId, 1);
        if (matches == null) {
            return null;
        }
        if (matches.size() > 1) { //should never happen
            throw new RuntimeException("More than one ICD classes found with the public ID: " + publicId + ". Classes: " + matches);
        }
        return CollectionUtilities.getFirstItem(matches);
    }

    /**
     * Returns the ICD category with the given public ID.
     * @param publicId
     * @return - the ICD category as a RDFSNamedClass; returns null, if an ICD category with this ID does not exist;
     * throws a runtime exception, if two ICD entities have the same public ID (should never happen), or if the returned entity is not a ICD category.
     */
    public RDFSNamedClass getICDCategoryByPublicId(String publicId) {
        RDFResource resource = getICDEntityByPublicId(publicId);
        if (resource == null) {
            return null;
        }
        if (resource instanceof RDFSNamedClass) {
            return (RDFSNamedClass) resource;
        }
        throw new RuntimeException("The ICD entity with publicId: " + publicId + " is not a ICD category. Entity: " + resource);
    }

    public RDFResource getTerm(RDFSNamedClass icdClass, RDFProperty icdTermProp) {
        return (RDFResource) icdClass.getPropertyValue(icdTermProp);
    }

    @SuppressWarnings("unchecked")
    public Collection<RDFResource> getTerms(RDFSNamedClass icdClass, RDFProperty icdTermProp, boolean includeSubproperties) {
        return icdClass.getPropertyValues(icdTermProp, includeSubproperties);
    }

    @SuppressWarnings("unchecked")
    public Collection<RDFResource> getTerms(RDFSNamedClass icdClass, RDFProperty icdTermProp) {
        return icdClass.getPropertyValues(icdTermProp);
    }

    @SuppressWarnings("unchecked")
    public Collection<RDFResource> getLinearizationSpecifications(RDFSNamedClass icdClass) {
        return icdClass.getPropertyValues(getLinearizationProperty());
    }

    @SuppressWarnings("unchecked")
    public Collection<RDFResource> getLinearizationICD10Specifications(RDFSNamedClass icdClass) {
        return icdClass.getPropertyValues(getLinearizationICD10Property());
    }

    @SuppressWarnings("unchecked")
    public Collection<RDFResource> getLinearizationICD10TabulationSpecifications(RDFSNamedClass icdClass) {
        return icdClass.getPropertyValues(getLinearizationICD10TabulationProperty());
    }

    /*
     * TAG management methods
     */

    /**
     * Returns the  primary and secondary TAGs assigned to this ICD class.
     * It does not include the inherited primary and secondary TAGs.
     *
     * @param icdClass - the ICD class
     * @return a collection of RDFResources (instances of TAG)
     */
    @SuppressWarnings("unchecked")
    public Collection<RDFResource> getAssignedTags(RDFSNamedClass icdClass) {
        return icdClass.getPropertyValues(getAssignedTagProperty());
    }

    /**
     * Returns the primary TAG assigned to this ICD class.
     * @param icdClass - a RDFResource (instance of TAG)
     * @return
     */
    public RDFResource getAssignedPrimaryTag(RDFSNamedClass icdClass) {
        return (RDFResource) icdClass.getPropertyValue(getAssignedPrimaryTagProperty());
    }

    /**
     * Returns the secondary TAGs assigned to this ICD class.
     * It does not include the inherited primary and secondary TAGs.
     *
     * @param icdClass - the ICD class
     * @return a collection of RDFResources (instances of TAG)
     */
    @SuppressWarnings("unchecked")
    public Collection<RDFResource> getAssignedSecondaryTags(RDFSNamedClass icdClass) {
        return icdClass.getPropertyValues(getAssignedSecondaryTagProperty());
    }

    /**
     * Returns a map that contains as a key the TAG instance and value is a list of classes from
     * where the TAG is inherited from.
     * It included also the local primary and secondary TAG.
     *
     * @param icdClass
     * @return map between the TAG instance and a list of classes where it is inherited from
     */
    public Map<RDFResource, List<RDFSNamedClass>> getInvolvedTags(RDFSNamedClass icdClass) {
        Map<Object, List<Instance>> map = ModelUtilities.getPropertyValuesOnAllSuperclasses(icdClass, getAssignedTagProperty());

        Map<RDFResource, List<RDFSNamedClass>> typedMap = new LinkedHashMap<RDFResource, List<RDFSNamedClass>>();

        for (Object value : map.keySet()) {
            typedMap.put((RDFResource) value, new ArrayList<RDFSNamedClass>(getRDFSNamedClassCollection(map.get(value))));
        }

        return typedMap;
    }

    /**
     * Gets the display status as one of the instances defined in {@link ICDContentModelConstants} :
     * <ul>
     * <li>"http://who.int/icd#DS_Blue" (browser text: Blue)
     * <li>"http://who.int/icd#DS_Yellow" (browser text: Yellow)
     * <li>"http://who.int/icd#DS_Red";
     * </ul>
     *
     * @param the ICD category
     * @return one of the DisplayStatus instances
     */
    public RDFResource getDisplayStatus(RDFSNamedClass icdClass) {
        return (RDFResource) icdClass.getPropertyValue(getDisplayStatusProperty());
    }

    /**
     * Retrieves the children of a parent ordered by an index.
     * The index is stored as instances of the ChildOrder class, and accessed through the
     * childrenOrder property. Each such instance has two properties: orderedChild (the class) and
     * orderedChildIndex (an int index for the class under this parent)
     *
     * This operation will take into account that the index might be corrupted and it will not
     * attempt to fix it. This method should not have side effects (no changes should happen
     * in the ontology as a result of this call).
     *
     *
     * @param parent - the parent class
     * @return - an ordered list of the children according to an index
     */
    public List<RDFSNamedClass> getOrderedChildren(RDFSNamedClass parent) {
        return isOrderedSiblingsSupported() == true ?
                new SiblingReordering(this).getOrderedChildren(parent) : getOrderedChildrenNoSiblingSupport(parent);
    }

    @SuppressWarnings("unchecked")
    private List<RDFSNamedClass> getOrderedChildrenNoSiblingSupport(RDFSNamedClass parent) {
        ArrayList<RDFSNamedClass> subclasses = new ArrayList<RDFSNamedClass>(parent.getVisibleDirectSubclasses());
        Collections.sort(subclasses, new FrameComparator<Frame>());
        return subclasses;
    }

    public boolean reorderSibling(RDFSNamedClass movedCls, RDFSNamedClass targetCls, boolean isBelow,
            RDFSNamedClass parent, String user) {
        return new SiblingReordering(this).reorderSibling(movedCls, targetCls, isBelow, parent, user);
    }

    public boolean checkIndexAndRecreate(RDFSNamedClass parent, boolean recreateIndex) {
        return new SiblingReordering(this).checkIndexAndRecreate(parent, recreateIndex);
    }

    public boolean addChildToIndex(RDFSNamedClass parent, RDFSNamedClass cls, boolean isSiblingIndexValid) {
        return new SiblingReordering(this).addChildToParentIndex(parent, cls, isSiblingIndexValid);
    }

    public boolean removeChildFromIndex(RDFSNamedClass parent, RDFSNamedClass cls, boolean isSiblingIndexValid) {
        return new SiblingReordering(this).removeChildFromIndex(parent, cls, isSiblingIndexValid);
    }

    public boolean isOrderedSiblingsSupported() {
        return getChildrenOrderProperty() != null;
    }


    /*
     * Equivalent class definitions
     */

    public RDFSNamedClass getPreecoordinationSuperclass(String clsName) {
    	RDFSNamedClass cls = getICDClass(clsName);
    	return getPreecoordinationSuperclass(cls);
    }

    public RDFSNamedClass getPreecoordinationSuperclass(RDFSNamedClass cls) {
       	RDFProperty precoordSuperclassProp = getPrecoordinationSuperclassProperty();
    	return (RDFSNamedClass) cls.getPropertyValue(precoordSuperclassProp);
    }

    public void setPrecoordinationSuperclass(String clsName, String superclsName) {
    	RDFSNamedClass cls = getICDClass(clsName);
    	RDFSNamedClass supercls = getICDClass(superclsName);

    	RDFProperty precoordSuperclassProp = getPrecoordinationSuperclassProperty();

    	//TODO see if we need transactions or if we need this method at all
    	cls.setPropertyValue(precoordSuperclassProp, supercls);

    	//TODO do we need to do something special here?
    }

    public void removePrecoordinationSuperclass(String clsName) {
    	//TODO

    }


    public OWLIntersectionClass getEquivalentPrecoordinationClassExpression(RDFSNamedClass cls) {
    	RDFSNamedClass precoordSuperclass = getPreecoordinationSuperclass(cls);
    	if (precoordSuperclass == null) {
    		//precoordinationSuperclass is not set, so
    		//there can't be any equivalent class expression that involve that superclass
    		return null;
    	}

    	Collection<?> equivalentClasses = cls.getEquivalentClasses();
    	if (equivalentClasses == null || equivalentClasses.isEmpty()) {
    		return null;
    	}
    	for (Iterator<?> it = equivalentClasses.iterator(); it.hasNext(); ) {
    		OWLClass nextEqClass = (OWLClass)it.next();
    		if (isValidPrecoordinationDefinitionClassExpression(nextEqClass, precoordSuperclass)) {
    			return (OWLIntersectionClass) nextEqClass;
    		}
    	}

    	return null;
    }

    public OWLIntersectionClass getNecessaryPrecoordinationClassExpression(RDFSNamedClass cls) {
    	RDFSNamedClass precoordSuperclass = getPreecoordinationSuperclass(cls);
    	if (precoordSuperclass == null) {
    		//precoordinationSuperclass is not set, so
    		//there can't be any equivalent class expression that involve that superclass
    		return null;
    	}

    	Collection<?> superclasses = cls.getSuperclasses(false);
    	if (superclasses == null || superclasses.isEmpty()) {
    		return null;
    	}
    	for (Iterator<?> it = superclasses.iterator(); it.hasNext(); ) {
    		OWLClass nextSuperclass = (OWLClass)it.next();
    		if ( (! cls.hasEquivalentClass(nextSuperclass)) &&
    				isValidPrecoordinationDefinitionClassExpression(nextSuperclass, precoordSuperclass)) {
    			return (OWLIntersectionClass) nextSuperclass;
    		}
    	}

    	return null;
    }

    /**
     * If this method returns true, then the classExpr can be casted to OWLIntersectionClass.
     *
     * @param classExpr a class expression that is part of a class definition, either as a necessary
     * 		condition or as necessary &amp; sufficient condition
     * @param precoordSuperclass the selected precoordination superclass, which needs to be part of a
     * 		valid precoordination definition class expression.
     * @return
     */
    public boolean isValidPrecoordinationDefinitionClassExpression(OWLClass classExpr, RDFSNamedClass precoordSuperclass) {
    	if (classExpr instanceof OWLIntersectionClass) {
    		OWLIntersectionClass intClassExpr = (OWLIntersectionClass) classExpr;
    		Collection<RDFSClass> operands = intClassExpr.getOperands();
    		Iterator<RDFSClass> it = operands.iterator();
    		while (it.hasNext()) {
    			RDFSClass op = it.next();
    			if (precoordSuperclass.equals(op)) {
    				return true;
    			}
    		}
    		return false;
    	}
    	else {
    		return false;
    	}
    }

    /**
     * Return the names of the properties that are involved in the precoordination definition
     * of the class <code>cls</code>. If the second <code>definitional</code> is true,
     * the method returns the list of properties involved in the equivalent class expression.
     * In case it is false, it returns the properties involves in the necessary conditions.
     * @param cls
     * @param definitional
     * @return
     */
    public Collection<String> getPropertiesInPrecoordinationDefinition(RDFSNamedClass cls, boolean definitional) {
    	OWLIntersectionClass classExpression;
    	if (definitional) {
    		classExpression = getEquivalentPrecoordinationClassExpression(cls);
    	}
    	else {
    		classExpression = getNecessaryPrecoordinationClassExpression(cls);
    	}

    	return getPropertiesFromClassExpression(classExpression);
    }

	public Collection<String> getPropertiesFromClassExpression(
			OWLIntersectionClass classExpression) {
		if (classExpression == null) {
    		return new ArrayList<String>();
    	}

    	ArrayList<String> res = new ArrayList<String>();
    	Collection<RDFSClass> operands = classExpression.getOperands();
    	for (Iterator<RDFSClass> it = operands.iterator(); it.hasNext();) {
    		RDFSClass operand = it.next();
    		if (operand instanceof RDFSNamedClass) {
    			//ignore
    		}
    		else {
    			if (operand instanceof OWLExistentialRestriction) {
    				OWLExistentialRestriction exRestr = (OWLExistentialRestriction) operand;
    				RDFProperty property = exRestr.getOnProperty();
    				res.add(property.getName());
    			}
    		}
    	}
    	return res;
	}

	public Collection<PrecoordinationDefinitionComponent> getPrecoordinationPropertyValues(RDFSNamedClass cls, Collection<String> properties) {
		OWLIntersectionClass eqClassExpression = getEquivalentPrecoordinationClassExpression(cls);
		OWLIntersectionClass necClassExpression = getNecessaryPrecoordinationClassExpression(cls);
		Collection<String> defProps = getPropertiesFromClassExpression(eqClassExpression);
		Collection<String> necProps = getPropertiesFromClassExpression(necClassExpression);

		Collection<PrecoordinationDefinitionComponent> res = new ArrayList<PrecoordinationDefinitionComponent>();
		for (Iterator<String> it = properties.iterator(); it.hasNext();) {
			String property = it.next();
			PrecoordinationDefinitionComponent value;
			if (defProps.contains(property)) {
				value = getPropertyValueFromClassExpression(eqClassExpression, property, true);
			}
			else if (necProps.contains(property)){
				value = getPropertyValueFromClassExpression(necClassExpression, property, false);
			}
			else {
				value = new PrecoordinationDefinitionComponent(property, null, null, false);
			}
			res.add(value);
		}
		return res;
	}

	private PrecoordinationDefinitionComponent getPropertyValueFromClassExpression(
			OWLIntersectionClass classExpression, String property, boolean isDefinitional) {
    	Collection<RDFSClass> operands = classExpression.getOperands();
		for (Iterator<RDFSClass> it = operands.iterator(); it.hasNext();) {
    		RDFSClass operand = it.next();
    		if (operand instanceof RDFSNamedClass) {
    			//ignore
    		}
    		else {
    			if (operand instanceof OWLHasValue) {
    				OWLHasValue exRestr = (OWLHasValue) operand;
    				if (exRestr.getOnProperty().getName().equals(property)) {
    					return new PrecoordinationDefinitionComponent(property,
    							((RDFResource)exRestr.getHasValue()).getName(), ValueType.INSTANCE, isDefinitional);
    				}
    			}
    			if (operand instanceof OWLSomeValuesFrom) {
    				OWLSomeValuesFrom exRestr = (OWLSomeValuesFrom) operand;
    				if (exRestr.getOnProperty().getName().equals(property)) {
    					return new PrecoordinationDefinitionComponent(property,
    							exRestr.getSomeValuesFrom().getName(), ValueType.CLS, isDefinitional);
    				}
    			}
    		}
    	}
		return null;
	}

	public class PrecoordinationDefinitionComponent {
		private String property;
		private String value;
		private ValueType valueType;
		private boolean isDefinitional;

		public PrecoordinationDefinitionComponent(String property,
				String value, ValueType valueType, boolean isDefinitional) {
			this.property = property;
			this.value = value;
			this.valueType = valueType;
			this.isDefinitional = isDefinitional;
		}

		public String getProperty() {
			return property;
		}

		public String getValue() {
			return value;
		}

		public boolean isDefinitional() {
			return isDefinitional;
		}

		public ValueType getValueType() {
			return valueType;
		}


		@Override
		public String toString() {
			return "PrecoordinationClassExpressionData(" +
					property + ", " +
					value + ", " +
					valueType + ", " +
					isDefinitional + ")";
		}

	}

	public boolean setPrecoordinationDefinitionPropertyValue(
			RDFSNamedClass cls, String property,
			String oldValue, String newValue) {
		OWLIntersectionClass eqClassExpression = getEquivalentPrecoordinationClassExpression(cls);
		OWLIntersectionClass necClassExpression = getNecessaryPrecoordinationClassExpression(cls);
		Collection<String> defProps = getPropertiesFromClassExpression(eqClassExpression);
		Collection<String> necProps = getPropertiesFromClassExpression(necClassExpression);

		if (eqClassExpression == null && necClassExpression == null && newValue != null) {
			necClassExpression = createPrecoordinationClassExpressionDraft(cls, false);
			addPropertyRestrictionToClassExpression(necClassExpression, property, newValue);
			return true;
		}

		if (newValue == null) {
			boolean changed = false;
			if (defProps.contains(property)) {
				if (defProps.size() == 1) {	//this is the only (i.e. the last) property in this class expression
					removeEquivalentClass(cls, eqClassExpression);
				}
				else {
					removePropertyRestrictionFromClassExpression(eqClassExpression, property);
				}
				changed = true;
			}
			if (necProps.contains(property)) {
				if (necProps.size() == 1) {	//this is the only (i.e. the last) property in this class expression
					((OWLNamedClass)cls).removeSuperclass(necClassExpression);
				}
				else {
					removePropertyRestrictionFromClassExpression(necClassExpression, property);
				}
				changed = true;
			}
			return changed;
		}

		// here we want to add or replace an old value with a (non-null) new value
		if (oldValue == null) {
			if (necProps.contains(property)) {
				//this must be an error
				Log.getLogger().warning("Possible error while changing value of property " + property + " in precoordination definition." +
						" Although oldValue is null, " + property + " appears in necessary condition: " + necClassExpression.getBrowserText());
				removePropertyRestrictionFromClassExpression(necClassExpression, property);
			}
			if (defProps.contains(property)) {
				//this must be an error
				Log.getLogger().warning("Possible error while changing value of property " + property + " in precoordination definition." +
						" Although oldValue is null, " + property + " appears in necessary & sufficient condition: " + eqClassExpression.getBrowserText());
				removePropertyRestrictionFromClassExpression(eqClassExpression, property);
			}
			//TODO see if there are cases when we need to add it to the eq. class expr.
			if (necClassExpression == null) {
				necClassExpression = createPrecoordinationClassExpressionDraft(cls, false);
			}
			addPropertyRestrictionToClassExpression(necClassExpression, property, newValue);
			return true;
		}
		else {
			if (necProps.contains(property)) {
				OWLRestriction restr = getPropertyRestrictionFromClassExpression(necClassExpression, property);
				if (restr instanceof OWLHasValue) {
					((OWLHasValue)restr).setHasValue(owlModel.getRDFResource(newValue));
				}
				else if (restr instanceof OWLSomeValuesFrom) {
					((OWLSomeValuesFrom)restr).setSomeValuesFrom(owlModel.getRDFResource(newValue));
				}
				else {
					//this must be an error
					Log.getLogger().severe("Error while changing value of property " + property + " in precoordination definition." +
						" Old value was not set as neither a hasValue nor a someValueFrom restriction");
					//remove old restriction & create a new restriction of the appropriate type
					removePropertyRestrictionFromClassExpression(necClassExpression, property);
					addPropertyRestrictionToClassExpression(necClassExpression, property, newValue);
				}
				return true;
			}
			else if (defProps.contains(property)) {
				OWLRestriction restr = getPropertyRestrictionFromClassExpression(eqClassExpression, property);
				if (restr instanceof OWLHasValue) {
					((OWLHasValue)restr).setHasValue(owlModel.getRDFResource(newValue));
				}
				else if (restr instanceof OWLSomeValuesFrom) {
					((OWLSomeValuesFrom)restr).setSomeValuesFrom(owlModel.getRDFResource(newValue));
				}
				else {
					//this must be an error
					Log.getLogger().severe("Error while changing value of property " + property + " in precoordination definition." +
						" Old value was not set as neither a hasValue nor a someValueFrom restriction");
					//remove old restriction & create a new restriction of the appropriate type
					removePropertyRestrictionFromClassExpression(eqClassExpression, property);
					addPropertyRestrictionToClassExpression(eqClassExpression, property, newValue);
				}
				return true;
			}
			else {
				//this must be an error
				Log.getLogger().warning("Possible error while changing value of property " + property + " in precoordination definition." +
						" Although oldValue is not null, " + property + " does not appears in necessary or necessary & sufficient conditions: ");
				return false;
			}
		}
	}

	private OWLIntersectionClass createPrecoordinationClassExpressionDraft(RDFSNamedClass cls, boolean equivalentClass) {
		OWLIntersectionClass precoordClassExpression;
		precoordClassExpression = owlModel.createOWLIntersectionClass();
		precoordClassExpression.addOperand(getPreecoordinationSuperclass(cls));
		if (equivalentClass) {
			((OWLNamedClass)cls).addEquivalentClass(precoordClassExpression);
		}
		else {
			cls.addSuperclass(precoordClassExpression);
		}
		return precoordClassExpression;
	}

	private void addPropertyRestrictionToClassExpression(
			OWLIntersectionClass classExpression, String property, String newValue) {
		//TODO Do we need additional information to know whether we should create a hasValue or
		//or a someValueOf property restriction
		RDFResource value = owlModel.getRDFResource(newValue);
		if (value instanceof OWLClass) {
			OWLSomeValuesFrom someValuesFromRestr = owlModel.createOWLSomeValuesFrom();
			someValuesFromRestr.setOnProperty(owlModel.getOWLProperty(property));
			someValuesFromRestr.setSomeValuesFrom(value);
			classExpression.addOperand(someValuesFromRestr);
		}
		else {
			OWLHasValue hasValueRestr = owlModel.createOWLHasValue();
			hasValueRestr.setOnProperty(owlModel.getOWLProperty(property));
			hasValueRestr.setHasValue(value);
			classExpression.addOperand(hasValueRestr);
		}
	}

	public boolean changeIsDefinitionalFlag(RDFSNamedClass cls,
			String property, boolean isDefinitionalFlag) {
		OWLIntersectionClass eqClassExpression = getEquivalentPrecoordinationClassExpression(cls);
		OWLIntersectionClass necClassExpression = getNecessaryPrecoordinationClassExpression(cls);
		Collection<String> defProps = getPropertiesFromClassExpression(eqClassExpression);
		Collection<String> necProps = getPropertiesFromClassExpression(necClassExpression);

		if (isDefinitionalFlag) {
			if (necProps.contains(property)) {
				OWLRestriction restr = removePropertyRestrictionFromClassExpression(necClassExpression, property);
				if (eqClassExpression == null) {
					eqClassExpression = createPrecoordinationClassExpressionDraft(cls, true);
					//((OWLNamedClass)cls).addEquivalentClass(eqClassExpression);
				}
				eqClassExpression.addOperand(restr);

				if (necProps.size() == 1) {
					((OWLNamedClass)cls).removeSuperclass(necClassExpression);
				}
				//TODO
			}
			else {
				return false;
			}
		}
		else {
			if (defProps.contains(property)) {
				OWLRestriction restr = removePropertyRestrictionFromClassExpression(eqClassExpression, property);
				if (necClassExpression == null) {
					necClassExpression = createPrecoordinationClassExpressionDraft(cls, false);
					//((OWLNamedClass)cls).addSuperclass(necClassExpression);
				}
				necClassExpression.addOperand(restr);

				if (defProps.size() == 1) {
					removeEquivalentClass(cls, eqClassExpression);
				}
			}
			else {
				return false;
			}
		}
		// TODO Decide what should be returned
		return true;
	}

	private void removeEquivalentClass(RDFSNamedClass cls,
			OWLIntersectionClass eqClassExpression) {
		Collection<OWLNamedClass> namedSuperclasses = new ArrayList<OWLNamedClass>();
		Collection directSuperclasses = cls.getSuperclasses(false);
		for (RDFSClass eqClassExpComp : eqClassExpression.getOperands()) {
			if (eqClassExpComp instanceof OWLNamedClass &&
					directSuperclasses.contains(eqClassExpComp)) {
				namedSuperclasses.add((OWLNamedClass) eqClassExpComp);
			}
		}

		boolean generateEvents = owlModel.getGenerateEventsEnabled();
		try {
			owlModel.setGenerateEventsEnabled(false);

			((OWLNamedClass)cls).removeEquivalentClass(eqClassExpression);

			//this is necessary because removeEquivalentClass removes also the named superclass
			for (OWLNamedClass superclass : namedSuperclasses) {
				cls.addSuperclass(superclass);
			}
		}
		catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "removeEquivalentClass operation failed", e);
		}
		finally {
			owlModel.setGenerateEventsEnabled(generateEvents);
		}
	}

	private OWLRestriction getPropertyRestrictionFromClassExpression(
			OWLIntersectionClass classExpression, String property) {
		Collection<RDFSClass> operands = classExpression.getOperands();
		for (RDFSClass operand : operands) {
			if (operand instanceof OWLRestriction) {
				OWLRestriction restr = (OWLRestriction) operand;
				if (restr.getOnProperty().getName().equals(property)) {
					return restr;
				}
			}
		}
		return null;
	}

	private OWLRestriction removePropertyRestrictionFromClassExpression(
			OWLIntersectionClass classExpression, String property) {
		OWLRestriction restr = getPropertyRestrictionFromClassExpression(classExpression, property);
		if (restr == null) {
			return null;
		}

		//OWLRestriction cloneRestr = (OWLRestriction) restr.createClone();  //very slow
		OWLRestriction cloneRestr = cloneOWLRestriction(restr);
		classExpression.removeOperand(restr);
		if (classExpression.getOperands().size() == 1) {
			//TODO see if we need to remove existing class expression
		}
		return cloneRestr;
	}

	private OWLRestriction cloneOWLRestriction(OWLRestriction restr) {
		if (restr instanceof OWLSomeValuesFrom) {
			OWLSomeValuesFrom someRestr = (OWLSomeValuesFrom) restr;
			OWLSomeValuesFrom clone = owlModel.createOWLSomeValuesFrom(someRestr.getOnProperty(), someRestr.getSomeValuesFrom());
			return clone;
		}
		if (restr instanceof OWLHasValue) {
			OWLHasValue valueRestr = (OWLHasValue) restr;
			OWLHasValue clone = owlModel.createOWLHasValue(valueRestr.getOnProperty(), valueRestr.getHasValue());
			return clone;
		}
		//we don't deal with other type of restrictions
		return null;
	}
}
