#SQL scrip to update the iCAT content to work with the new External Causes post-coordination Content Model

#should be none of these
UPDATE icd_umbrella
SET slot = "http://who.int/icd#phaseOfSportOrExerciseActivity"
WHERE slot = "http://who.int/icd#phaseOfActivity";

#should be none of these
UPDATE icd_umbrella
SET slot = "http://who.int/icd#genderOfPerpetrator"
WHERE slot = "http://who.int/icd#sexOfPerpetrator";

#should be none of these
UPDATE icd_umbrella
SET slot = "http://who.int/icd#typeOfArmedConflict"
WHERE slot = "http://who.int/icd#typeOfConflict";


#---- contextOfAssault to contextOfAssaultAndMaltreatment ----#

#should be only 1 of this
UPDATE icd_umbrella
SET slot = "http://who.int/icd#contextOfAssaultAndMaltreatment"
WHERE slot = "http://who.int/icd#contextOfAssault";

#should 1 of this
UPDATE icd_umbrella
SET frame = "http://who.int/icd#ContextOfAssaultAndMaltreatmentReferenceTerm"
WHERE frame = "http://who.int/icd#ContextOfAssaultReferenceTerm";

#should 2 of this
UPDATE icd_umbrella
SET short_value = "http://who.int/icd#ContextOfAssaultAndMaltreatmentReferenceTerm"
WHERE short_value = "http://who.int/icd#ContextOfAssaultReferenceTerm";


#---- modeOfTransport to modeOfTransportOfTheInjuredPerson ----#

#should be only 1 of this
UPDATE icd_umbrella
SET slot = "http://who.int/icd#modeOfTransportOfTheInjuredPerson"
WHERE slot = "http://who.int/icd#modeOfTransport";

#should 1 of this
UPDATE icd_umbrella
SET frame = "http://who.int/icd#ModeOfTransportOfTheInjuredPersonReferenceTerm"
WHERE frame = "http://who.int/icd#ModeOfTransportReferenceTerm";

#should 2 of this
UPDATE icd_umbrella
SET short_value = "http://who.int/icd#ModeOfTransportOfTheInjuredPersonReferenceTerm"
WHERE short_value = "http://who.int/icd#ModeOfTransportReferenceTerm";


#---- move content of placeOfOccurrenceDescriptor to placeOfOccurrence ----#

DROP TABLE IF EXISTS prop_values;
CREATE TEMPORARY TABLE prop_values AS
(	SELECT DISTINCT short_value FROM icd_umbrella
	WHERE slot = "http://who.int/icd#placeOfOccurrenceDescriptor");

#should be many (~233) of these
UPDATE icd_umbrella
SET frame = "http://who.int/icd#PlaceOfOccurrenceReferenceTerm"
WHERE frame = "http://who.int/icd#PlaceOfOccurrenceDescriptorReferenceTerm"
 and 
 short_value in (SELECT short_value FROM prop_values)
;

#should be many (~466) of these
UPDATE icd_umbrella
SET frame = "http://who.int/icd#PlaceOfOccurrenceReferenceTerm"
WHERE short_value = "http://who.int/icd#PlaceOfOccurrenceDescriptorReferenceTerm"
 and 
 frame in (SELECT short_value FROM prop_values)
;
 
DROP TABLE prop_values;

#should be many (~233) of these
UPDATE icd_umbrella
SET slot = "http://who.int/icd#placeOfOccurrence"
WHERE slot = "http://who.int/icd#placeOfOccurrenceDescriptor";


#---- violenceDescriptor to aspectsOfAssaultAndMaltreatment ----#

#should be a few (~21) of these
UPDATE icd_umbrella
SET slot = "http://who.int/icd#aspectsOfAssaultAndMaltreatment"
WHERE slot = "http://who.int/icd#violenceDescriptor";

CREATE TEMPORARY TABLE prop_values AS
(	SELECT DISTINCT short_value FROM icd_umbrella
	WHERE slot = "http://who.int/icd#aspectsOfAssaultAndMaltreatment");

#should be many (~21) of these
UPDATE icd_umbrella
SET frame = "http://who.int/icd#AspectsOfAssaultAndMaltreatmentReferenceTerm"
WHERE frame = "http://who.int/icd#ViolenceDescriptorReferenceTerm"
 and 
 short_value in (SELECT short_value FROM prop_values)
;

#should be many (~42) of these
UPDATE icd_umbrella
SET short_value = "http://who.int/icd#AspectsOfAssaultAndMaltreatmentReferenceTerm"
WHERE short_value = "http://who.int/icd#ViolenceDescriptorReferenceTerm"
 and 
 frame in (SELECT short_value FROM prop_values);
 
DROP TABLE prop_values;
