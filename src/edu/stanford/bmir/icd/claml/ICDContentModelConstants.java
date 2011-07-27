package edu.stanford.bmir.icd.claml;

public class ICDContentModelConstants {

    //TODO- what should we do with the default NS?
    public final static String NS = "http://who.int/icd#";

    /*
     * Metaclasses
     */

    public final static String ICD_CAUSAL_MECH_METACLASS = NS + "CausalMechanismAndRiskFactorsSection";
    public final static String ICD_CLINICAL_DESC_METACLASS = NS + "ClinicalDescriptionSection";
    public final static String ICD_DEFINITION_METACLASS = NS + "DefinitionSection";
    public final static String ICD_FUNCTIONAL_IMPACT_METACLASS = NS + "FunctionalImpactSection";
    public final static String ICD_NOTES_METACLASS = NS + "ICD10NotesAndHintsSection";
    public final static String ICD_LINEARIZATION_METACLASS = NS + "LinearizationSection";
    public final static String ICD_SNOMED_METACLASS = NS + "SnomedReferenceSection";
    public final static String ICD_TERM_METACLASS = NS + "TermSection";
    public final static String ICD_DIAGNOSTIC_CRITERIA_METACLASS = NS + "DiagnosticCriteriaSection";
    public static final String ICD_SPECIFIC_CONDITION_METACLASS = NS + "SpecificConditionSection";
    public static final String ICD_EXTERNAL_CAUSE_METACLASS = NS + "ExternalCauseSection";

    /*
     * Classes
     */

    public final static String ICD_CATEGORY_CLASS = NS + "ICDCategory";
    public final static String CLAML_REF_CLASS = NS + "ClamlReference";
    public final static String TERM_CLASS = NS + "Term";
    public final static String TERM_TITLE_CLASS = NS + "TitleTerm";
    @Deprecated
    public final static String TERM_INCLUSION_CLASS = NS + "InclusionTerm";
    @Deprecated
    public final static String TERM_EXCLUSION_CLASS = NS + "ExclusionTerm";
    public final static String TERM_ICD10_NOTES_CLASS = NS + "ICD10NotesTerm";
    public final static String TERM_DEFINITION_CLASS = NS + "DefinitionTerm";
    public final static String TERM_REFERENCE_CLASS = NS + "ReferenceTerm";
    public final static String TERM_SYNONYM_CLASS = NS + "SynonymTerm";
    @Deprecated
    public final static String TERM_INDEX_CLASS = NS + "IndexTerm";
    @Deprecated
    public final static String INDEX_TERM_TYPE_CLASS = NS + "IndexTermType";
    public final static String TERM_NARROWER_CLASS = NS + "NarrowerTerm";
    public final static String TERM_BASE_INDEX_CLASS = NS + "BaseIndexTerm";
    public final static String TERM_BASE_INCLUSION_CLASS = NS + "BaseInclusionTerm";
    public final static String TERM_BASE_EXCLUSION_CLASS = NS + "BaseExclusionTerm";
    public final static String LINEARIZATION_VIEW_CLASS = NS + "LinearizationView";
    public final static String LINEARIZATION_SPECIFICATION_CLASS = NS + "LinearizationSpecification";

    public final static String EXTERNAL_CAUSES_TOP_CLASS = NS + "XX";


    /*
     * Properties
     */

    public final static String ICD_CODE_PROP = NS + "icdCode";
    public final static String ICD_TITLE_PROP = NS + "icdTitle";
    public final static String CLAML_KIND_PROP = NS + "clamlKind";
    public final static String CLAML_USAGE_PROP = NS + "clamlUsage";
    public final static String TEXT_PROP = NS + "text";
    public final static String DEFINITION_PROP = NS + "definition";
    public final static String LONG_DEFINITION_PROP = NS + "longDefinition";
    public final static String PREFILLED_DEFINITION_PROP = NS + "definitionPrefilled";
    public final static String SYNOYM_PROP = NS + "synonym";
    public final static String NARROWER_PROP = NS + "narrower";
    public final static String BASE_INDEX_PROP = NS + "baseIndex";
    public final static String BASE_INCLUSION_PROP = NS + "baseInclusion";
    public final static String INDEX_BASE_INCLUSION_PROP = NS + "indexBaseInclusion";
    public final static String BASE_EXCLUSION_PROP = NS + "baseExclusion";
    public final static String SORTING_LABEL_PROP = NS + "sortingLabel";

    @Deprecated
    public final static String INCLUSION_PROP = NS + "inclusion";
    @Deprecated
    public final static String EXCLUSION_PROP = NS + "exclusion";
    @Deprecated
    public final static String BASE_INDEX_TYPE_PROP = NS + "indexTermType";


    public final static String ID_PROP = NS + "id";
    public final static String LABEL_PROP = NS + "label";
    public final static String LANG_PROP = NS + "language";
    public final static String ONTOLOGYID_PROP = NS + "ontologyId";
    public final static String CLAML_CONTENT_PROP = NS + "clamlContent";
    public final static String CLAML_REFERENCES_PROP = NS + "clamlReferences";
    public final static String ICD_REF_CODE_PROP = NS + "icdRefCode";

    public final static String CODING_HINT_PROP = NS + "codingHint";
    public final static String INTRO_PROP = NS + "introduction";
    public final static String NOTE_PROP = NS + "note";
    public final static String PREFFERED_PROP = NS + "preferred";
    public final static String PREFERRED_LONG_PROP = NS + "preferredLong";

    public final static String URL_PROP = NS + "url";
    public final static String TERM_ID_PROP = NS + "termId";

    public final static String LINEARIZATION_PROP = NS + "linearization";
    public final static String IS_INCLUDED_IN_LINEARIZATION_PROP = NS + "isIncludedInLinearization";
    public final static String LINEARIZATION_PARENT_PROP = NS + "linearizationParent";
    public final static String LINEARIZATION_VIEW_PROP = NS + "linearizationView";
    public final static String LINEARIZATION_SEQUENCE_NO_PROP = NS + "sequenceNumber";
    public final static String LINEARIZATION_SORTING_LABEL_PROP = NS + "linearizationSortingLabel";

    public final static String LINEARIZATION_VIEW_MORBIDITY = NS + "Morbidity";
    public final static String LINEARIZATION_VIEW_MORTALITY = NS + "Mortality";
    public final static String LINEARIZATION_VIEW_PRIMARY_CARE = NS + "PrimaryCare";

    public final static String BIOLOGICAL_SEX_PROP = NS + "biologicalSex";
    public final static String BIOLOGICAL_SEX_NA = NS + "BiologicalSexNotAppSCTerm";

    public final static String ASSIGNED_TAG_PROP = NS + "assignedTAG";
    public final static String ASSIGNED_PRIMARY_TAG_PROP = NS + "assignedPrimaryTAG";
    public final static String ASSIGNED_SECONDARY_TAG_PROP = NS + "assignedSecondaryTAG";
	public final static String DISPLAY_STATUS_PROP = "http://who.int/icd#displayStatus";


    /*
     * Instances
     */

    public final static String INDEX_TYPE_SYNONYM_INST = NS + "Synonym";

    public final static String DISPLAY_STATUS_BLUE = "http://who.int/icd#DS_Blue";
    public final static String DISPLAY_STATUS_GREEN = "http://who.int/icd#DS_Green";
    public final static String DISPLAY_STATUS_YELLOW = "http://who.int/icd#DS_Yellow";
    public final static String DISPLAY_STATUS_RED = "http://who.int/icd#DS_Red";

    /*
     * BioPortal
     */

    public final static String NS_BP = "http://bioportal.bioontology.org#";

    public final static String BP_SHORT_TERM_ID_PROP = NS_BP + "shortTermId";
    public final static String BP_ONTOLOGY_LABEL_PROP = NS_BP + "ontologyLabel";
    public final static String BP_ONTOLOGY_ID_PROP = NS_BP + "ontologyId";

}
