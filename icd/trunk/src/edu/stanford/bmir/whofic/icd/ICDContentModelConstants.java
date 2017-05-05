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
    public static final String ICD_ICECI_METACLASS = NS + "ICECIMetaClass";

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
	public final static String PC_AXIS_EC_ACTIVITY_WHEN_INJURED = NS + "activityWhenInjured";
	public final static String PC_AXIS_EC_INTENT = NS + "intent";
	public final static String PC_AXIS_EC_MECHANISM_OF_INJURY = NS + "mechanismOfInjury";
	public final static String PC_AXIS_EC_OBJECT_OR_SUBSTANCE_PRODUCING_INJURY = NS + "objectOrSubstanceProducingInjury";
	public final static String PC_AXIS_EC_PLACE_OF_OCCURRENCE = NS + "placeOfOccurrence";
	public final static String PC_AXIS_EC_SUBSTANCE_USE = NS + "substanceUse";
	public final static String PC_AXIS_EC_OCCUPATIONAL_DESCRIPTOR = NS + "occupationalDescriptor";
	public final static String PC_AXIS_EC_OCCUPATIONAL_DESCRIPTOR_ECONOMIC_ACTIVITY = NS + "economicActivity";
	public final static String PC_AXIS_EC_OCCUPATIONAL_DESCRIPTOR_OCCUPATION = NS + "occupation";
	public final static String PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR = NS + "placeOfOccurrenceDescriptor";
	public final static String PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_INDOOR_OR_OUTDOOR = NS + "indoorOrOutdoor";
	public final static String PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_INSIDE_OR_OUTSIDE_CITY_LIMITS = NS + "insideOrOutsideCityLimits";
	public final static String PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_PART_OF_BUILDING_OR_GROUNDS = NS + "partOfBuildingOrGrounds";
	public final static String PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_RESIDENT_OF_HOME = NS + "residentOfHome";
	public final static String PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_TYPE_OF_HOME = NS + "typeOfHome";
	public final static String PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_TYPE_OF_SCHOOL = NS + "typeOfSchool";
	public final static String PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR = NS + "sportsActivityDescriptor";
	public final static String PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR_ENVIRONMENTAL_COUNTERMEASURES = NS + "environmentalCountermeasures";
	public final static String PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR_PERSONAL_COUNTERMEASURES = NS + "personalCountermeasures";
	public final static String PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR_PHASE_OF_ACTIVITY = NS + "phaseOfActivity";
	public final static String PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR_TYPE_OF_SPORT_OR_EXERCISE_ACTIVITY = NS + "typeOfSportOrExerciseActivity";
	public final static String PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR = NS + "transportEventDescriptor";
	public final static String PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR_COUNTERPART = NS + "counterpart";
	public final static String PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR_MODE_OF_TRANSPORT = NS + "modeOfTransport";
	public final static String PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR_ROLE_OF_THE_INJURED_PERSON = NS + "roleOfTheInjuredPerson";
	public final static String PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR_TYPE_OF_TRANSPORT_INJURY_EVENT = NS + "typeOfTransportInjuryEvent";
	public final static String PC_AXIS_EC_VIOLENCE_DESCRIPTOR = NS + "violenceDescriptor";
	public final static String PC_AXIS_EC_VIOLENCE_DESCRIPTOR_CONTEXT_OF_ASSAULT = NS + "contextOfAssault";
	public final static String PC_AXIS_EC_VIOLENCE_DESCRIPTOR_PERPETRATOR_VICTIM_RELATIONSHIP = NS + "perpetratorVictimRelationship";
	public final static String PC_AXIS_EC_VIOLENCE_DESCRIPTOR_PREVIOUS_SUICIDE_ATTEMPT = NS + "previousSuicideAttempt";
	public final static String PC_AXIS_EC_VIOLENCE_DESCRIPTOR_PROXIMAL_RISK_FACTORS_FOR_INTENTIONAL_SELF_HARM = NS + "proximalRiskFactorsForIntentionalSelfHarm";
	public final static String PC_AXIS_EC_VIOLENCE_DESCRIPTOR_SEX_OF_PERPETRATOR = NS + "sexOfPerpetrator";
	public final static String PC_AXIS_EC_VIOLENCE_DESCRIPTOR_TYPE_OF_CONFLICT = NS + "typeOfConflict";
	public final static String PC_AXIS_EC_VIOLENCE_DESCRIPTOR_TYPE_OF_LEGAL_INTERVENTION = NS + "typeOfLegalIntervention";
	public final static String PC_AXIS_HAS_SEVERITY = NS + "hasSeverity";	//replicated in ICDConstants for iCAT client
	public final static String PC_AXIS_HISTOPATHOLOGY = NS + "histopathology";
	public final static String PC_AXIS_INJURY_QUALIFIER_BURN_QUALIFIER = NS + "burnQualifier";
	public final static String PC_AXIS_INJURY_QUALIFIER_BURN_QUALIFIER_EXTENT_OF_BURN_BY_BODY_SURF = NS + "extentOfBurnByBodySurface";
	public final static String PC_AXIS_INJURY_QUALIFIER_BURN_QUALIFIER_EXTENT_OF_FULL_THICKNESS_BURN_BY_BODY_SURF = NS + "extentOfFullThicknessBurnByBodySurface";
	public final static String PC_AXIS_INJURY_QUALIFIER_BURN_QUALIFIER_OUTCOME_OF_FULL_THICKNESS_BURN = NS + "outcomeOfFullThicknessBurn";
	public final static String PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER = NS + "fractureQualifier";
	public final static String PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER_OPEN_OR_CLOSED = NS + "fractureOpenOrClosed";
	public final static String PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER_FRACTURE_SUBTYPE = NS + "fractureSubtype";
	public final static String PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER_JOINT_INVOLVEMENT_IN_FRACTURE_SUBTYPE = NS + "jointInvolvementInFracture";
	public final static String PC_AXIS_INJURY_QUALIFIER_TYPE_OF_INJURY = NS + "typeOfInjury";
	public final static String PC_AXIS_TEMPORALITY_COURSE = NS + "course";	//replicated in ICDConstants for iCAT client
	public final static String PC_AXIS_TEMPORALITY_PATTERN_AND_ONSET = NS + "temporalPatternAndOnset";	//replicated in ICDConstants for iCAT client
	public final static String PC_AXIS_TEMPORALITY_TIME_IN_LIFE = NS + "timeInLife";
	public final static String PC_AXIS_ASSOCIATED_WITH = NS + "associatedWith";
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
			PC_AXIS_EC_ACTIVITY_WHEN_INJURED,
			PC_AXIS_EC_INTENT,
			PC_AXIS_EC_MECHANISM_OF_INJURY,
			PC_AXIS_EC_OBJECT_OR_SUBSTANCE_PRODUCING_INJURY,
			PC_AXIS_EC_PLACE_OF_OCCURRENCE,
			PC_AXIS_EC_SUBSTANCE_USE,
			PC_AXIS_EC_OCCUPATIONAL_DESCRIPTOR,
			PC_AXIS_EC_OCCUPATIONAL_DESCRIPTOR_ECONOMIC_ACTIVITY,
			PC_AXIS_EC_OCCUPATIONAL_DESCRIPTOR_OCCUPATION,
			PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR,
			PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_INDOOR_OR_OUTDOOR,
			PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_INSIDE_OR_OUTSIDE_CITY_LIMITS,
			PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_PART_OF_BUILDING_OR_GROUNDS,
			PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_RESIDENT_OF_HOME,
			PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_TYPE_OF_HOME,
			PC_AXIS_EC_PLACE_OF_OCCURRENCE_DESCRIPTOR_TYPE_OF_SCHOOL,
			PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR,
			PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR_ENVIRONMENTAL_COUNTERMEASURES,
			PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR_PERSONAL_COUNTERMEASURES,
			PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR_PHASE_OF_ACTIVITY,
			PC_AXIS_EC_SPORTS_ACTIVITY_DESCRIPTOR_TYPE_OF_SPORT_OR_EXERCISE_ACTIVITY,
			PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR,
			PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR_COUNTERPART,
			PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR_MODE_OF_TRANSPORT,
			PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR_ROLE_OF_THE_INJURED_PERSON,
			PC_AXIS_EC_TRANSPORT_EVENT_DESCRIPTOR_TYPE_OF_TRANSPORT_INJURY_EVENT,
			PC_AXIS_EC_VIOLENCE_DESCRIPTOR,
			PC_AXIS_EC_VIOLENCE_DESCRIPTOR_CONTEXT_OF_ASSAULT,
			PC_AXIS_EC_VIOLENCE_DESCRIPTOR_PERPETRATOR_VICTIM_RELATIONSHIP,
			PC_AXIS_EC_VIOLENCE_DESCRIPTOR_PREVIOUS_SUICIDE_ATTEMPT,
			PC_AXIS_EC_VIOLENCE_DESCRIPTOR_PROXIMAL_RISK_FACTORS_FOR_INTENTIONAL_SELF_HARM,
			PC_AXIS_EC_VIOLENCE_DESCRIPTOR_SEX_OF_PERPETRATOR,
			PC_AXIS_EC_VIOLENCE_DESCRIPTOR_TYPE_OF_CONFLICT,
			PC_AXIS_EC_VIOLENCE_DESCRIPTOR_TYPE_OF_LEGAL_INTERVENTION,
			PC_AXIS_HAS_SEVERITY, PC_AXIS_HISTOPATHOLOGY, PC_AXIS_INJURY_QUALIFIER_BURN_QUALIFIER,
			PC_AXIS_INJURY_QUALIFIER_BURN_QUALIFIER_EXTENT_OF_BURN_BY_BODY_SURF,
			PC_AXIS_INJURY_QUALIFIER_BURN_QUALIFIER_EXTENT_OF_FULL_THICKNESS_BURN_BY_BODY_SURF,
			PC_AXIS_INJURY_QUALIFIER_BURN_QUALIFIER_OUTCOME_OF_FULL_THICKNESS_BURN,
			PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER,
			PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER_OPEN_OR_CLOSED,
			PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER_FRACTURE_SUBTYPE,
			PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER_JOINT_INVOLVEMENT_IN_FRACTURE_SUBTYPE,
			PC_AXIS_INJURY_QUALIFIER_TYPE_OF_INJURY,
			PC_AXIS_TEMPORALITY_COURSE, PC_AXIS_TEMPORALITY_PATTERN_AND_ONSET, PC_AXIS_TEMPORALITY_TIME_IN_LIFE,
			PC_AXIS_ASSOCIATED_WITH);

	
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
