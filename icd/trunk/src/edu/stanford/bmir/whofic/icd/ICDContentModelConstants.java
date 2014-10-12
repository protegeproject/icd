package edu.stanford.bmir.whofic.icd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.stanford.bmir.whofic.WHOFICContentModelConstants;

public class ICDContentModelConstants extends WHOFICContentModelConstants {

    //TODO- what should we do with the default NS?
    public final static String NS = "http://who.int/icd#";

    /*
     * Metaclasses
     */

    public final static String ICD_CAUSAL_MECH_METACLASS = NS + "CausalMechanismAndRiskFactorsSection";
    public final static String ICD_CLINICAL_DESC_METACLASS = NS + "ClinicalDescriptionSection";
    public final static String ICD_FUNCTIONAL_IMPACT_METACLASS = NS + "FunctionalImpactSection";
    public final static String ICD_NOTES_METACLASS = NS + "ICD10NotesAndHintsSection";
    public final static String ICD_SNOMED_METACLASS = NS + "SnomedReferenceSection";
    public final static String ICD_DIAGNOSTIC_CRITERIA_METACLASS = NS + "DiagnosticCriteriaSection";
    public static final String ICD_SPECIFIC_CONDITION_METACLASS = NS + "SpecificConditionSection";
    public static final String ICD_EXTERNAL_CAUSE_METACLASS = NS + "ExternalCauseSection";

    /*
     * Classes
     */

    public final static String ICD_CATEGORY_CLASS = NS + "ICDCategory";

    /* Linearizations */
    public final static String LINEARIZATION_ICD_11_VIEW_CLASS = NS + "ICD11LinearizationView";
    public final static String LINEARIZATION_ICD_10_VIEW_CLASS = NS + "ICD10LinearizationView";
    public final static String LINEARIZATION__ICD_10_TABULATION_VIEW_CLASS = NS + "ICD10TabulationListView";

	/* Post-Coordination */

    public final static String EXTERNAL_CAUSES_TOP_CLASS = NS + "XX";

    public final static  String CHAPTER_X_CLASS = NS + "ChapterX";


    /*
     * Properties
     */

    public final static  String IS_TEMPLATE = NS + "isTemplate";


    /* Linearizations */
    public final static String LINEARIZATION_ICD_10_PROP = NS + "icd10Linearization";
    public final static String LINEARIZATION_ICD_10_TABULATION_PROP = NS + "icd10TabulationList";

	/* Post-Coordination Axis Properties */

	public final static String PC_AXIS_SPECIFIC_ANATOMY = NS + "specificAnatomy";
	public final static String PC_AXIS_TOPOLOGY_DISTRIBUTION = NS + "distribution";
	public final static String PC_AXIS_TOPOLOGY_LATERALITY = NS + "laterality";
	public final static String PC_AXIS_TOPOLOGY_REGIONAL = NS + "regional";
	public final static String PC_AXIS_TOPOLOGY_RELATIONAL = NS + "relational";
	public final static String PC_AXIS_BIOLOGICAL_INDICATOR_GENOMIC_AND_CHOMOSOMAL_ANOMALY = NS + "genomicAndChomosomalAnomaly";
	public final static String PC_AXIS_BIOLOGICAL_INDICATOR_SEROTYPE = NS + "serotype";
	public final static String PC_AXIS_CONSCIOUSNESS_MEASURE_DURATION_OF_COMA = NS + "durationOfComa";
	public final static String PC_AXIS_LEVEL_OF_CONSCIOUSNESS = NS + "levelOfConsciousness";
	public final static String PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_GCS_SCORE = NS + "hasGCSScore";
	public final static String PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_GCS_SCORE_HAS_GCS_EYE_SCORE = NS + "hasGCSEyeScore";
	public final static String PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_GCS_SCORE_HAS_GCS_MOTOR_SCORE = NS + "hasGCSMotorScore";
	public final static String PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_GCS_SCORE_HAS_GCS_VERBAL_SCORE = NS + "hasGCSVerbalScore";
	public final static String PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_PUPIL_REACTION_SCORE = NS + "hasPupilReactionScore";
	public final static String PC_AXIS_DIAGNOSIS_CONFIRMED_BY = NS + "diagnosisConfirmedBy";
	public final static String PC_AXIS_ETIOLOGY_CAUSALITY = NS + "causality";
	public final static String PC_AXIS_ETIOLOGY_CHEMICAL_AGENT = NS + "chemicalAgent";
	public final static String PC_AXIS_ETIOLOGY_INFECTIOUS_AGENT = NS + "infectiousAgent";
	public final static String PC_AXIS_ETIOLOGY_MEDICATION = NS + "medication";
	//externalCauseDimension
	public final static String PC_AXIS_HAS_SEVERITY = NS + "hasSeverity";	//replicated in ICDConstants for iCAT client
	public final static String PC_AXIS_HISTOPATHOLOGY = NS + "histopathology";
	public final static String PC_AXIS_INJURY_QUALIFIER_CUSTOM_BURN_SCALE_VALUE = NS + "customBurnScaleValue";
	public final static String PC_AXIS_INJURY_QUALIFIER_CUSTOM_FRACTURE_SUBTYPE = NS + "fractureSubtype";
	public final static String PC_AXIS_INJURY_QUALIFIER_CUSTOM_TYPE_OF_INJURY = NS + "typeOfInjury";
	public final static String PC_AXIS_TEMPORALITY_COURSE = NS + "course";	//replicated in ICDConstants for iCAT client
	public final static String PC_AXIS_TEMPORALITY_PATTERN_AND_ONSET = NS + "temporalPatternAndOnset";	//replicated in ICDConstants for iCAT client
	public final static String PC_AXIS_TEMPORALITY_TIME_IN_LIFE = NS + "timeInLife";
    //TODO continue list

	public final static List<String> PC_AXES_PROPERTIES_LIST = Arrays.asList(
			PC_AXIS_SPECIFIC_ANATOMY, PC_AXIS_TOPOLOGY_DISTRIBUTION, PC_AXIS_TOPOLOGY_LATERALITY,
			PC_AXIS_TOPOLOGY_REGIONAL, PC_AXIS_TOPOLOGY_RELATIONAL, PC_AXIS_BIOLOGICAL_INDICATOR_GENOMIC_AND_CHOMOSOMAL_ANOMALY,
			PC_AXIS_BIOLOGICAL_INDICATOR_SEROTYPE,
			PC_AXIS_CONSCIOUSNESS_MEASURE_DURATION_OF_COMA,
			PC_AXIS_LEVEL_OF_CONSCIOUSNESS, PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_GCS_SCORE,
			PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_GCS_SCORE_HAS_GCS_EYE_SCORE,
			PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_GCS_SCORE_HAS_GCS_MOTOR_SCORE,
			PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_GCS_SCORE_HAS_GCS_VERBAL_SCORE,
			PC_AXIS_DIAGNOSIS_CONFIRMED_BY, PC_AXIS_ETIOLOGY_CAUSALITY,
			PC_AXIS_LEVEL_OF_CONSCIOUSNESS_HAS_PUPIL_REACTION_SCORE,
			PC_AXIS_ETIOLOGY_CHEMICAL_AGENT, PC_AXIS_ETIOLOGY_INFECTIOUS_AGENT, PC_AXIS_ETIOLOGY_MEDICATION,
			//PC_AXIS_EC...
			PC_AXIS_HAS_SEVERITY, PC_AXIS_HISTOPATHOLOGY, PC_AXIS_INJURY_QUALIFIER_CUSTOM_BURN_SCALE_VALUE,
			PC_AXIS_INJURY_QUALIFIER_CUSTOM_FRACTURE_SUBTYPE, PC_AXIS_INJURY_QUALIFIER_CUSTOM_TYPE_OF_INJURY,
			PC_AXIS_TEMPORALITY_COURSE, PC_AXIS_TEMPORALITY_PATTERN_AND_ONSET, PC_AXIS_TEMPORALITY_TIME_IN_LIFE);

	
	public final static String PC_SCALE_SEVERITY = NS + "hasSeverityScale";	//replicated in ICDConstants for iCAT client
	public final static String PC_SCALE_COURSE = NS + "hasCourseScale";	//replicated in ICDConstants for iCAT client
	public final static String PC_SCALE_PATTERN_AND_ONSET = NS + "hasPatternActivityClinicalStatusScale";	//replicated in ICDConstants for iCAT client

	
	@SuppressWarnings("serial")
	public
	static final HashMap<String, String> PC_AXIS_PROP_TO_VALUE_SET_PROP = new HashMap<String, String>(){
		{
			put(PC_AXIS_HAS_SEVERITY, PC_SCALE_SEVERITY);
			put(PC_AXIS_TEMPORALITY_COURSE, PC_SCALE_COURSE);
	    	put(PC_AXIS_TEMPORALITY_PATTERN_AND_ONSET, PC_SCALE_PATTERN_AND_ONSET);
	    }
	
		@Override
		public String get(Object key){
			String res = super.get(key);
			if (res == null) {
				return (String)key;
			}
			return res;
		}
	};


}
