package edu.stanford.bmir.icd.utils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLAnonymousClass;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.RDFList;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.triplestore.Tuple;

/**
 * Converts HasValue references to scale values in logical definitions
 * to SomeValueFrom references to classes in hierarchical value sets 
 * (i.e. classes from X Chapter)
 *  
 * @author csnyulas
 *
 */
public class MigrateScalesInLogicalDefinitions {

	private static final String PPRJ_FILE_URI = "projects/icd/icd_umbrella.pprj";

	private static URI pprjFileUri = new File(PPRJ_FILE_URI).toURI();

	private static final boolean TEST_RUN = true;
	private static final boolean DEBUG = false;


	private final static List<String> scaleProperties;

	static {
		scaleProperties = new ArrayList<String>(
				ICDContentModelConstants.FIXED_SCALE_PC_AXES_PROPERTIES_LIST);
		scaleProperties.addAll(ICDContentModelConstants.SCALE_PC_AXES_PROPERTIES_LIST);
		scaleProperties.add(ICDContentModelConstants.PC_AXIS_TEMPORALITY_TIME_IN_LIFE);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		List<String> properties = new ArrayList<>();
		
		//read arguments
		if (args.length < 1) {
			printUsage();
			return;
		}
		else {
			pprjFileUri = new File(args[0]).toURI();
			
			for (int i = 1; i < args.length; i++) {
				String propertyName = args[i];
				if (! propertyName.startsWith(ICDContentModelConstants.NS)) {
					propertyName = ICDContentModelConstants.NS + propertyName;
				}
				if (! scaleProperties.contains(propertyName)) {
					Log.getLogger().info("Error: invalid custom scale property name " + args[i]);
					printUsage();
					return;
				}
				else {
					properties.add(propertyName);
				}
			}
		}
		if (properties.isEmpty()) {
			properties.addAll(scaleProperties);
		}

		SystemUtilities.logSystemInfo();
		Log.getLogger().info("\n===== Started migration of scale property value references in logical definitions " + new Date());
		Log.getLogger().info("=== PPRJ File: " + pprjFileUri);
		Log.getLogger().info("=== Properties: " + properties);

		//open owl model
		OWLModel owlModel = ImportUtils.openOWLModel(pprjFileUri);

		for (String prop : properties) {
			migrateLogicalDefinitions(owlModel, prop);
		}
		
//		//TODO check whether we want to record changes. If yes, look at:
//		writeChangesToChao(categoryInfoMap, owlModel);

		//finish processing
		Log.getLogger().info("\n===== End migration of scale property value references in logical definitions at " + new Date());
	}


	private static void printUsage() {
		Log.getLogger().info("Usage: MigrateScalesInLogicalDefinitions pprjFile\n" +
				"  or \n" +  "       MigrateScalesInLogicalDefinitions pprjFile pcScaleProp1 [pcScaleProperty2 ...]\n" +
				" Note: the first version converts all scale value properties, "
				+ "while the second version only the scale properties listed in the arguments");
	}



	private static void migrateLogicalDefinitions(OWLModel owlModel, String propName) {
		Log.getLogger().info("\nMigrating logical definitions involving property " + propName + " ...");
		
		ICDContentModel cm = new ICDContentModel(owlModel);

		RDFProperty property = owlModel.getRDFProperty(propName);
		Iterator<?> itReferences = owlModel.listReferences(property, 0);
		while (itReferences.hasNext()) {
			Object referenceObj = itReferences.next();
			if (referenceObj instanceof Tuple) {
				Tuple reference = (Tuple) referenceObj;
				RDFResource subject = reference.getSubject();
				RDFResource predicate = reference.getPredicate();
				if ( ! cm.getAllowedPostcoordinationAxisPropertyProperty().equals(predicate) &&
						 ! cm.getRequiredPostcoordinationAxisPropertyProperty().equals(predicate)) {
					if (DEBUG) {
						System.out.println(String.format("Reference: subject %s of type %s; predicate %s; ref. inst %s", 
								subject, subject.getClass(),  predicate, reference));
					}
					if (subject instanceof OWLHasValue) {
						replaceHasValueRestriction(owlModel, (OWLHasValue)subject, property);
					}
					else {
						Log.getLogger().warning(String.format("Unexpected type %s of reference subject %s", 
								subject.getClass(), subject));
					}
				}
			}
			else {
				Log.getLogger().warning(String.format("Unexpected reference type %s of %s", 
						referenceObj.getClass(), referenceObj));
			}
		}
		
	}




	private static void replaceHasValueRestriction(
			OWLModel owlModel, OWLHasValue hasValueRestrInst, RDFProperty property) {
		
		Log.getLogger().info(
				String.format("Replacing HasValue restriction: %s on %s with SomeValuesFrom",
				hasValueRestrInst, property));
		
		ICDContentModel cm = new ICDContentModel(owlModel);
		
		RDFSNamedClass subjectEntity = findSubjectEntity(owlModel, hasValueRestrInst);
		if (subjectEntity == null) {
			Log.getLogger().info("No subject entity found. Ignoring this one.");
			return;
		}
		Log.getLogger().info(String.format("Subject Entity: %s  Title: %s",
				subjectEntity, cm.getTitleLabel(subjectEntity)));
		
		Iterator<?> itReferences = owlModel.listReferences(hasValueRestrInst, 0);
		while (itReferences.hasNext()) {
			Object referenceObj = itReferences.next();
			if (referenceObj instanceof Tuple) {
				Tuple reference = (Tuple) referenceObj;
				RDFResource subject = reference.getSubject();
				RDFResource predicate = reference.getPredicate();
				System.out.println(String.format("Reference to HasValue class expr: "
						+ "subject %s of type %s; precicate %s; ref. inst %s", 
						subject, subject.getClass(),  predicate, reference));
				OWLSomeValuesFrom someValuesFromRestrInst = createSomeValuesFrom(cm, owlModel, hasValueRestrInst);
				if (someValuesFromRestrInst != null) {
					if (DEBUG) {
						System.out.println(someValuesFromRestrInst);
					}
					if (! TEST_RUN) {
						subject.setPropertyValue((RDFProperty)predicate, someValuesFromRestrInst);
						//TODO delete hasValueRestrInst 
					}
					else {
						someValuesFromRestrInst.delete();
					}
				}
			}
			else {
				Log.getLogger().warning(String.format("Unexpected reference type %s of %s", 
						referenceObj.getClass(), referenceObj));
			}

		}
	}


	private static RDFSNamedClass findSubjectEntity(OWLModel owlModel, RDFResource owlRestrInst) {
		RDFSNamedClass res = null;
		Iterator<?> itReferences = owlModel.listReferences(owlRestrInst, 0);
		while (itReferences.hasNext()) {
			Object referenceObj = itReferences.next();
			if (referenceObj instanceof Tuple) {
				Tuple reference = (Tuple) referenceObj;
				RDFResource subject = reference.getSubject();
				if (subject instanceof OWLNamedClass) {
					return (OWLNamedClass) subject;					
				}
				else if (subject instanceof OWLAnonymousClass ||
						subject instanceof RDFList) {
					res = findSubjectEntity(owlModel, subject);
					if (res != null) {
						return res;
					}
				}
				else {
					Log.getLogger().warning(String.format("Unexpected type %s of reference tuple subject %s", 
							subject.getClass(), subject));
				}
			}
			else {
				Log.getLogger().warning(String.format("Unexpected reference type %s of %s", 
						referenceObj.getClass(), referenceObj));
			}
		}
		
		return res;
	}


	private static OWLSomeValuesFrom createSomeValuesFrom(
			ICDContentModel cm, OWLModel owlModel, OWLHasValue hasValueRestrInst) {
		OWLSomeValuesFrom res = null;
		Object value = hasValueRestrInst.getHasValue();
		if (value instanceof OWLIndividual) {
			OWLIndividual valueInd = (OWLIndividual) value;
			Collection<?> types = valueInd.getRDFTypes();
			System.out.println("types of filler " + valueInd + " : " + types);
			RDFResource referencedValueIns = (RDFResource) valueInd.getPropertyValue(cm.getReferencedValueProperty());
			System.out.println("Referenced value: " + referencedValueIns);
			res = owlModel.createOWLSomeValuesFrom(hasValueRestrInst.getOnProperty(), referencedValueIns);
		}
		else {
			Log.getLogger().log(Level.SEVERE, String.format("Wrong type of hasValue filler %s in %s",
					value, hasValueRestrInst));
		}

		return res;
	}


}
