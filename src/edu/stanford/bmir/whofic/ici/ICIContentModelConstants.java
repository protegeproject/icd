package edu.stanford.bmir.whofic.ici;

import java.util.Arrays;
import java.util.List;

import edu.stanford.bmir.whofic.WHOFICContentModelConstants;

public class ICIContentModelConstants extends WHOFICContentModelConstants {

    //TODO- what should we do with the default NS?
    public final static String NS = "http://who.int/ici/contentModel#";


    /*
     * Classes
     */

    public final static String ICD_CATEGORY_CLASS = NS + "ICICategory";

    /* Linearizations */

	/* Post-Coordination */

    public final static  String CHAPTER_X_CLASS = NS + "ChapterX";


    /*
     * Properties
     */


    /* Linearizations */

	/* Post-Coordination Axis Properties */

	public final static String PC_AXIS_ACTION = NS + "hasAction";
	public final static String PC_AXIS_APPROACH = NS + "hasApproach";
	public final static String PC_AXIS_ASSOCIATED_PROCEDURE = NS + "hasAssociatedProcedure";
	public final static String PC_AXIS_BODY_SYSTEM = NS + "hasBodySystem";
	public final static String PC_AXIS_COMPLEXITY = NS + "hasComplexity";
	public final static String PC_AXIS_COMPONENT = NS + "hasComponent";
	public final static String PC_AXIS_ENTITY_ADDED_IN_PROCEDURE = NS + "hasEntityAddedInProcedure";
	public final static String PC_AXIS_ENTITY_REMOVED_IN_PROCEDURE = NS + "hasEntityRemovedInProcedure";
	public final static String PC_AXIS_ENTITY_USED_IN_PROCEDURE = NS + "hasEntityUsedInProcedure";
	public final static String PC_AXIS_EXTENT = NS + "hasExtent";
	public final static String PC_AXIS_INDICATION = NS + "hasIndication";
	public final static String PC_AXIS_FINDING_INDICATION = NS + "hasFindingIndication";
	public final static String PC_AXIS_PATHOLOGY = NS + "hasPathology";
	public final static String PC_AXIS_LOCALE = NS + "hasLocale";
	public final static String PC_AXIS_PART = NS + "hasPart";
	public final static String PC_AXIS_PANEL_ELEMENT = NS + "hasPanelElement";
	public final static String PC_AXIS_PATIENT_STATUS = NS + "hasPatientStatus";
	public final static String PC_AXIS_RECIPIENT_TYPE = NS + "hasRecipientType";
	public final static String PC_AXIS_TARGET = NS + "hasTarget";
	public final static String PC_AXIS_TARGET_ACTIVITY_PARTICIPATION = NS + "hasActivityParticipationTarget";
	public final static String PC_AXIS_TARGET_ANATOMIC = NS + "hasAnatomicTarget";
	public final static String PC_AXIS_TARGET_BEHAVIOR = NS + "hasBehaviorTarget";
	public final static String PC_AXIS_TARGET_ENVIRONMENTAL_FACTOR = NS + "hasEnvironmentalFactorTarget";
	public final static String PC_AXIS_TARGET_FUNCTION = NS + "hasFunctionTarget";
    //TODO continue list
//	public final static String[] PC_AXES_PROPERTIES = {PC_AXIS_ACTION, PC_AXIS_APPROACH,
//		PC_AXIS_ASSOCIATED_PROCEDURE, PC_AXIS_BODY_SYSTEM, PC_AXIS_COMPLEXITY};
	public final static List<String> PC_AXES_PROPERTIES_LIST = Arrays.asList(
			PC_AXIS_ACTION, PC_AXIS_APPROACH, PC_AXIS_ASSOCIATED_PROCEDURE,
			PC_AXIS_BODY_SYSTEM, PC_AXIS_COMPLEXITY, PC_AXIS_COMPONENT,
			PC_AXIS_ENTITY_ADDED_IN_PROCEDURE,
			PC_AXIS_ENTITY_REMOVED_IN_PROCEDURE,
			PC_AXIS_ENTITY_USED_IN_PROCEDURE,
			PC_AXIS_EXTENT,
			PC_AXIS_INDICATION, PC_AXIS_FINDING_INDICATION,
			PC_AXIS_PATHOLOGY,
			PC_AXIS_PART, PC_AXIS_PANEL_ELEMENT,
			PC_AXIS_LOCALE,
			PC_AXIS_PATIENT_STATUS, PC_AXIS_RECIPIENT_TYPE, PC_AXIS_TARGET,
			PC_AXIS_TARGET_ACTIVITY_PARTICIPATION, PC_AXIS_TARGET_ANATOMIC, PC_AXIS_TARGET_BEHAVIOR,
			PC_AXIS_TARGET_ENVIRONMENTAL_FACTOR, PC_AXIS_TARGET_FUNCTION);

//	public final static String PC_SCALE_SEVERITY = NS + "hasSeverityScale";
//	public final static String PC_SCALE_COURSE = NS + "hasCourseScale";
//	public final static String PC_SCALE_PATTERN_AND_ONSET = NS + "hasPatternActivityClinicalStatusScale";


}
