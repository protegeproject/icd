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

    /*
     * Classes
     */

    public final static String ICD_CATEGORY_CLASS = NS + "ICDCategory";
    public final static String CLAML_REF_CLASS = NS + "ClamlReference";
    public final static String TERM_CLASS = NS + "Term";
    public final static String TERM_TITLE_CLASS = NS + "TitleTerm";
    public final static String TERM_INCLUSION_CLASS = NS + "InclusionTerm";
    public final static String TERM_EXCLUSION_CLASS = NS + "ExclusionTerm";
    public final static String TERM_ICD10_NOTES_CLASS = NS + "ICD10NotesTerm";

    /*
     * Properties
     */

    public final static String ICD_CODE_PROP = NS + "icdCode";
    public final static String ICD_TITLE_PROP = NS + "icdTitle";
    public final static String CLAML_KIND_PROP = NS + "clamlKind";
    public final static String CLAML_USAGE_PROP = NS + "clamlUsage";
    public final static String TEXT_PROP = NS + "text";

    public final static String INCLUSION_PROP = NS + "inclusion";
    public final static String EXCLUSION_PROP = NS + "exclusion";

    public final static String ID_PROP = NS + "id";
    public final static String LABEL_PROP = NS + "label";
    public final static String LANG_PROP = NS + "language";
    public final static String CLAML_CONTENT_PROP = NS + "clamlContent";
    public final static String CLAML_REFERENCES_PROP = NS + "clamlReferences";
    public final static String ICD_REF_CODE_PROP = NS + "icdRefCode";

    public final static String CODING_HINT_PROP = NS + "codingHint";
    public final static String INTRO_PROP = NS + "introduction";
    public final static String NOTE_PROP = NS + "note";
    public final static String PREFFERED_PROP = NS + "preferred";
    public final static String PREFERRED_LONG_PROP = NS + "preferredLong";

    public final static String URL_PROP = NS + "url";

    public final static String MORBIDITY_PARENT_PROP = NS + "morbidityParent";

    /*
     * BioPortal 
     */

    public final static String NS_BP = "http://bioportal.bioontology.org#";

    public final static String BP_SHORT_TERM_ID_PROP = NS_BP + "shortTermId";
    public final static String BP_ONTOLOGY_LABEL_PROP = NS_BP + "ontologyLabel";
    public final static String BP_ONTOLOGY_ID_PROP = NS_BP + "ontologyId";
}
