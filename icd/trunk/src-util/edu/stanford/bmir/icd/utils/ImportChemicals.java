package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class ImportChemicals {
	private static Logger log = Log.getLogger(ImportChemicals.class);

	private static final String SEPARATOR = "\t";
	private static final String PREFIX_NEW_TERM = "http://who.int/icd#Chemicals_";
	private static final String VALUE_SET_PARENT_CLASS_NAME = "http://who.int/icd#Substances";

	//no. of columns that represent tree levels
	private static final int NO_OF_TREE_COLUMNS = 4;

	private static ICDContentModel cm;
	private static OWLModel owlModel;
	private static OWLNamedClass valueSetTopClass;

	private static List<RDFSNamedClass> metaclasses = new ArrayList<RDFSNamedClass>();
	private static Map<Integer, String> parentNameForLevel = new HashMap<Integer, String>();
	private static Map<Integer, OWLNamedClass> parentForLevel = new HashMap<Integer, OWLNamedClass>();
	private static Map<String, OWLNamedClass> synref2Class = new HashMap<String, OWLNamedClass>();
	private static Map<String, OWLNamedClass> name2Class = new HashMap<String, OWLNamedClass>();
	private static Map<OWLNamedClass, Set<String>> class2extRef = new HashMap<OWLNamedClass, Set<String>>();

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Needs 2 params: ICD pprj file and CSV file");
			return;
		}

		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(args[1]));
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Could not find CSV file: " + args[1], e);
			closeReader(input);
			return;
		}

		Collection errors = new ArrayList();
		Project prj = Project.loadProjectFromFile(args[0], errors);

		if (errors != null) {
			ProjectManager.getProjectManager().displayErrors("Errors", errors);
		}

		owlModel = (OWLModel) prj.getKnowledgeBase();
		cm = new ICDContentModel(owlModel);

		if (owlModel == null) {
			log.severe("Abort. Failed to load pprj file");
			closeReader(input);
			return;
		}

		init();

		long t0 = System.currentTimeMillis();
		log.info("Started import at: " + new Date());

		importFile(input);

		log.info("Finished at: " + new Date() + " in " + (System.currentTimeMillis() - t0)/1000 +" seconds.");

	}

	private static void init(){
		initMetaclasses();
		initTopClass();
	}

	private static void initMetaclasses() {
		metaclasses.add(cm.getDefinitionMetaClass());
		metaclasses.add(cm.getTermMetaClass());
		metaclasses.add(cm.getLinearizationMetaClass());
		metaclasses.add(cm.getExternalReferenceMetaClass());
		metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#ChemicalAgentMetaClass"));
		metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#ValueMetaClass"));
	}

	private static void initTopClass(){
		valueSetTopClass = owlModel.getOWLNamedClass(VALUE_SET_PARENT_CLASS_NAME);
		if (valueSetTopClass == null) {
			valueSetTopClass = owlModel.createOWLNamedSubclass(VALUE_SET_PARENT_CLASS_NAME, (OWLNamedClass)cm.getChapterXClass());
			for (RDFSNamedClass metacls : metaclasses) {
				valueSetTopClass.addRDFType(metacls);
			}
		}
	}


	private static void importFile(BufferedReader input) {
		importLines(input);

		//TODO: optional
		owlModel.getProject().save(new ArrayList());
	}

	private static void importLines(BufferedReader input) {
		String line = null;
		int index = 0;
		try {
			while ((line = input.readLine()) != null) {
				if (line != null) {
					try {
						importLine(line);
					} catch (Exception e) {
						Log.getLogger().log(Level.WARNING, "Error at importing line: " + line, e);
					}
				}
				index ++;
				if (index % 100 == 0) {
					log.info("Processed "+ index + " rows at " + new Date());
				}
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}


	private static void importLine(String line) {
		String[] cols = line.split(SEPARATOR);

		for (int i = 0; i < NO_OF_TREE_COLUMNS; i++){
			String name = getValue(cols, i);
			String currentParent = parentNameForLevel.get(i);
			if (currentParent == null || name.equalsIgnoreCase(currentParent) == false) {
				OWLNamedClass cls = name2Class.get(name);
				if (cls == null) {                
					cls = createClass();
					name2Class.put(name, cls);
					addProperties(cls, name, null, null);
				}
				parentForLevel.put(i, cls);
				parentNameForLevel.put(i, name);
				addParent(cls, i, line);
			}
		}

		/*
		 * Assumes that the spreadsheet is ordered by the synref column and then by pref/syn
		 * to ensure that pref always comes before syn for the same term
		 */


		 String label = getValue(cols, NO_OF_TREE_COLUMNS );
		 String synref = getValue(cols, NO_OF_TREE_COLUMNS + 1);

		 boolean isPref = "syn".equalsIgnoreCase(getValue(cols, NO_OF_TREE_COLUMNS + 2)) == false ? true : false;

		 String icd10code = getValue(cols, NO_OF_TREE_COLUMNS + 3);
		 String atccode = getValue(cols, NO_OF_TREE_COLUMNS + 4);
		 String snomedcode = getValue(cols, NO_OF_TREE_COLUMNS + 5);
		 String dermaalergen = getValue(cols, NO_OF_TREE_COLUMNS + 6);

		 OWLNamedClass currentCls = null;

		 if (isPref == false) { // this is a synonym
			 currentCls  = synref2Class.get(synref);
			 if (currentCls == null) {
				 log.warning("Could not find cls for synref: "+ synref +". Line: " + line) ;
				 return;
			 }

			 //this is a synonym of currentCls
			 addProperties(currentCls, null, null, label);

		 } else { //pref is true, this is the first time the class is created
			 currentCls = name2Class.get(label);
			 if (currentCls == null) {
				 currentCls = createClass();
				 name2Class.put(label, currentCls);
				 addProperties(currentCls, label, null, null);
			 }
			 synref2Class.put(synref, currentCls);
		 }

		 addParent(currentCls, NO_OF_TREE_COLUMNS, line);

		 if (isExtRef(currentCls, icd10code) == false) {
			 addExternalReference(currentCls, "ICD-10", icd10code, null);
			 addExtRef(currentCls, icd10code);
		 }
		 if (isExtRef(currentCls, atccode) == false) {
			 addExternalReference(currentCls, "ATC", atccode, null);
			 addExtRef(currentCls, atccode);
		 }
		 if (isExtRef(currentCls, snomedcode) == false) {
			 addExternalReference(currentCls, "SNOMED-CT", snomedcode, null);
			 addExtRef(currentCls,snomedcode);
		 }
		 if (isExtRef(currentCls, dermaalergen) == false) {
			 addExternalReference(currentCls, "Dermaallergen", dermaalergen, null);
			 addExtRef(currentCls, dermaalergen);
		 }
	}

	@SuppressWarnings("deprecation")
	private static void addParent(OWLNamedClass cls, int level, String line) {
		if (level == 0) {
			cls.addSuperclass(valueSetTopClass);
			cls.removeSuperclass(owlModel.getOWLThingClass());
			return;
		}

		OWLNamedClass parent = parentForLevel.get(level - 1);
		if (parent == null) {
			log.warning("Could not find parent for "+ cls +" at level " + level +" Line: " + line);
			return;
		}

		if (cls.equals(parent) == false && cls.hasSuperclass(parent) == false) {
			cls.addSuperclass(parent);
			if (cls.hasSuperclass(owlModel.getOWLThingClass())) {
				cls.removeSuperclass(owlModel.getOWLThingClass());
			}
		}
	}

	private static boolean isExtRef(OWLNamedClass cls, String extRef) {
		Set<String> refs = class2extRef.get(cls);
		if (refs == null) {
			return false;
		}
		return refs.contains(extRef);
	}

	private static void addExtRef(OWLNamedClass cls, String extRef) {
		Set<String> refs = class2extRef.get(cls);
		if (refs == null) {
			refs = new HashSet<String>();
		}
		refs.add(extRef);
		class2extRef.put(cls, refs);
	}

	private static String getValue(String[] cols, int colNo) {
		//colNo is -1 for empty rows
		String text = colNo >=0 && cols.length > colNo ? cols[colNo] : null;

		if (text == null) {
			return null;
		}

		text = text.trim();
		return text.isEmpty() == true ? null : text;
	}

	private static OWLNamedClass createClass() {
		OWLNamedClass cls = owlModel.createOWLNamedClass(PREFIX_NEW_TERM + IDGenerator.getNextUniqueId());
		addMetaclasses(cls);
		return cls;
	}

	private static void addMetaclasses(OWLNamedClass cls) {
		for (RDFSNamedClass metacls : metaclasses) {
			cls.addProtegeType(metacls);
		}
	}

	private static void addProperties(OWLNamedClass cls, String title, String narrowerName, String synonym) {

		if (title != null && title.isEmpty() == false) {
			RDFResource titleTerm = cm.createTitleTerm();
			titleTerm.addPropertyValue(cm.getLabelProperty(), title);
			cls.addPropertyValue(cm.getIcdTitleProperty(), titleTerm);
		}

		if (synonym != null && synonym.isEmpty() == false) {
			RDFResource synTerm = cm.createSynonymTerm();
			cm.fillTerm(synTerm, null, synonym, "en");
			cls.addPropertyValue(cm.getSynonymProperty(), synTerm);
		}

		if (narrowerName != null && narrowerName.isEmpty() == false) {
			RDFResource narrowerTerm = cm.createTerm(cm.getTermNarrowerClass());
			cm.fillTerm(narrowerTerm, null, narrowerName, "en");
			cls.addPropertyValue(cm.getNarrowerProperty(), narrowerTerm);
		}

		//Defintion term should always be created according to Csongor, even if empty
		// if (definitionName != null && definitionName.isEmpty() == false) {
		//RDFResource defTerm = cm.createDefinitionTerm();
		//cm.fillTerm(defTerm, null, null, "en");
		//cls.addPropertyValue(cm.getDefinitionProperty(), defTerm);
		// }
	}

	private static void addReferenceScaleValueTerm(String refTermClsName, OWLNamedClass cls) {
		if (refTermClsName == null) {
			return;
		}

		//TODO: check if reference not added already!!!

		OWLNamedClass refTermCls = owlModel.getOWLNamedClass(refTermClsName);
		if (refTermCls == null) {
			log.warning("Could not find term reference class: " + refTermClsName);
			return;
		}

		RDFResource refTerm = cm.createTerm(refTermCls);
		refTerm.addPropertyValue(cm.getReferencedValueProperty(), cls);

	}

	//may be used later
	private static void addExternalReference(OWLNamedClass cls, String ontologyId, String code, String term) {
		if (code == null) {
			return;
		}

		RDFResource extRefTerm = cm.createExternalReferenceTerm();
		cm.fillTerm(extRefTerm, code, term, "en");

		extRefTerm.addPropertyValue(cm.getOntologyIdProperty(), ontologyId);

		//TODO: not sure if we should use termId or id, so we use both
		extRefTerm.addPropertyValue(cm.getTermIdProperty(), code);

		cls.addPropertyValue(cm.getExternalReferenceProperty(), extRefTerm);
	}


	private static void closeReader(BufferedReader input) {
		try {
			if (input != null) {
				input.close();
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}


}
