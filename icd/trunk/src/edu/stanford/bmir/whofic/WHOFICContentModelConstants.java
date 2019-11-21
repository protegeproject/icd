package edu.stanford.bmir.whofic;


public class WHOFICContentModelConstants {

    //TODO- what should we do with the default NS?
    //public final static String NS = "http://who.int/who-fic#";
    public final static String NS = "http://who.int/icd#";

    /*
     * Metaclasses
     */

    public final static String ICD_DEFINITION_METACLASS = NS + "DefinitionSection";
    public final static String ICD_LINEARIZATION_METACLASS = NS + "LinearizationSection";
    public final static String ICD_EXTERNAL_REFERENCE_METACLASS = NS + "ExternalReferenceSection";
    public final static String ICD_TERM_METACLASS = NS + "TermSection";

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
    public final static String TERM_EXTERNAL_DEFINITION_CLASS = NS + "ExternalDefinitionTerm";
    public final static String TERM_REFERENCE_CLASS = NS + "ReferenceTerm";
    public final static String TERM_SNOMED_REFERENCE_CLASS = NS + "SnomedReferenceTerm";
    public final static String TERM_EXTERNAL_REFERENCE_CLASS = NS + "ExternalReferenceTerm";
    public final static String TERM_SYNONYM_CLASS = NS + "SynonymTerm";
    @Deprecated
    public final static String TERM_INDEX_CLASS = NS + "IndexTerm";
    @Deprecated
    public final static String INDEX_TERM_TYPE_CLASS = NS + "IndexTermType";
    public final static String TERM_NARROWER_CLASS = NS + "NarrowerTerm";
    public final static String TERM_BASE_INDEX_CLASS = NS + "BaseIndexTerm";
    public final static String TERM_BASE_INCLUSION_CLASS = NS + "BaseInclusionTerm";
    public final static String TERM_BASE_EXCLUSION_CLASS = NS + "BaseExclusionTerm";
    public final static String TERM_CODING_NOTE_CLASS = NS + "CodingNoteTerm";

    /* Linearizations */
    public final static String LINEARIZATION_VIEW_CLASS = NS + "LinearizationView";
    public final static String LINEARIZATION_SPECIFICATION_CLASS = NS + "LinearizationSpecification";
    public final static String LINEARIZATION_HISTORIC_SPECIFICATION_CLASS = NS + "HistoricLinearizationSpecification";

	/* Post-Coordination */
    public final static String POSTCOORDINATION_AXES_SPECIFICATION_CLASS = NS + "PostcoordinationAxesSpecification";
    public final static String POSTCOORDINATION_SCALE_TERM_CLASS = NS + "PostcoordinationScaleTerm";
    public final static String POSTCOORDINATION_VALUE_REFERENCE_TERM_CLASS = NS + "PostcoordinationValueReferenceTerm";

    public final static String CHILD_ORDER_CLASS = NS +"ChildOrder";

    public final static  String CHAPTER_X_CLASS = NS + "ChapterX";


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
    public final static String SUBCLASS_BASE_INCLUSION_PROP = NS + "subclassBaseInclusion";
    public final static String BASE_EXCLUSION_PROP = NS + "baseExclusion";
    public final static String SORTING_LABEL_PROP = NS + "sortingLabel";
    public final static String EXTERNAL_REFERENCE_PROP = NS + "externalReference";

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
    public final static String PUBLIC_ID_PROP = NS+ "publicId";

    /* Linearizations */
    public final static String LINEARIZATION_PROP = NS + "linearization";
    public final static String LINEARIZATION_HISTORIC_PROP = NS + "historicLinearization";

    public final static String IS_INCLUDED_IN_LINEARIZATION_PROP = NS + "isIncludedInLinearization";
    public final static String IS_GROUPING_PROP = NS + "isGrouping";
    public final static String IS_AUXILIARY_AXIS_CHILD_PROP = NS + "isAuxiliaryAxisChild";
    public final static String CODING_NOTE_PROP = NS + "codingNote";
    public final static String LINEARIZATION_PARENT_PROP = NS + "linearizationParent";
    public final static String LINEARIZATION_VIEW_PROP = NS + "linearizationView";
    public final static String LINEARIZATION_SEQUENCE_NO_PROP = NS + "sequenceNumber";
    public final static String LINEARIZATION_SORTING_LABEL_PROP = NS + "linearizationSortingLabel";

    public final static String LINEARIZATION_VIEW_MORBIDITY = NS + "Morbidity";
    public final static String LINEARIZATION_VIEW_MORTALITY = NS + "Mortality";
    public final static String LINEARIZATION_VIEW_PRIMARY_CARE = NS + "PrimaryCare";
    public final static String LINEARIZATION_VIEW_OPHTHALMOLOGY = NS + "Specialty_Adaptation_Ophthalmology";

    public final static String SUPPRESS_OTHER_SPECIFIED_RESIDUALS = NS + "suppressOtherSpecifiedResiduals";
    public final static String SUPPRESS_UNSPECIFIED_RESIDUALS = NS + "suppressUnspecifiedResiduals";
    public final static String OTHER_SPECIFIED_RESIDUAL_TITLE = NS + "otherSpecifiedResidualTitle";
    public final static String UNSPECIFIED_RESIDUAL_TITLE = NS + "unspecifiedResidualTitle";

    public final static String BIOLOGICAL_SEX_PROP = NS + "biologicalSex";
    public final static String BIOLOGICAL_SEX_NA = NS + "BiologicalSexNotAppSCTerm";

    public final static String ASSIGNED_TAG_PROP = NS + "assignedTAG";
    public final static String ASSIGNED_PRIMARY_TAG_PROP = NS + "assignedPrimaryTAG";
    public final static String ASSIGNED_SECONDARY_TAG_PROP = NS + "assignedSecondaryTAG";
	public final static String DISPLAY_STATUS_PROP = NS + "displayStatus";

	/* Post-Coordination */
	public final static String ALLOWED_POSTCOORDINATION_AXES_PROP = NS + "allowedPostcoordinationAxes";
	public final static String ALLOWED_POSTCOORDINATION_AXIS_PROPERTY_PROP = NS + "allowedPostcoordinationAxisProperty";
	public final static String REQUIRED_POSTCOORDINATION_AXIS_PROPERTY_PROP = NS + "requiredPostcoordinationAxisProperty";
	public final static String PRECOORDINATION_SUPERCLASS_PROP = NS + "precoordinationSuperclass";
	public final static String HAS_SCALE_VALUE_PROP = NS + "hasScaleValue";

	public final static String REFERENCED_VALUE_PROP = NS + "referencedValue";


	public final static String IS_OBSOLETE_PROP = NS + "isObsolete";
	public final static String IS_DEPRECATED_PROP = NS + "isDeprecated";

	/* Children Ordering based on the parent */
	public final static String CHILDREN_ORDER_PROP = NS + "childrenOrder";
	public final static String ORDERED_CHILD_INDEX_PROP = NS + "orderedChildIndex";
	public final static String ORDERED_CHILD_PROP = NS + "orderedChild";

	/* Post-Coordination Axis Properties */


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
