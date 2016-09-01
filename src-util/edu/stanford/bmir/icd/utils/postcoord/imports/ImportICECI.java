package edu.stanford.bmir.icd.utils.postcoord.imports;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

/**
 * Import script for importing ICECI as a value set to be used as "Dimensions of External Causes".
 * This script was adapted from {@link ImportSpecificAnatomicLocation}
 * @author csnyulas
 *
 */

public class ImportICECI {
    private static Logger log = Log.getLogger(ImportICECI.class);

    private static final String SEPARATOR = "\t";
    private static final String VALUE_SEPARATOR = ",";
    private static final String PREFIX_NEW_TERM = "http://who.int/icd#ICECI_";
    private static final String VALUE_SET_PARENT_CLASS_NAME = "http://who.int/icd#ICECI_ICECI";
    private static final String REFERENCED_CATEGORY_PROPERTY_NAME = "http://who.int/icd#referencedCategory";
    
    private static ICDContentModel cm;
    private static OWLModel owlModel;
    private static OWLNamedClass valueSetTopClass;

    private static List<RDFSNamedClass> metaclasses = new ArrayList<RDFSNamedClass>();
    private static Map<OWLNamedClass, String> id2baseExclusionsMap = new HashMap<OWLNamedClass, String>();
    private static Map<OWLNamedClass, String> id2relevantEntitiesMap = new HashMap<OWLNamedClass, String>();

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

    private static void init() {
        metaclasses.add(cm.getDefinitionMetaClass());
        metaclasses.add(cm.getTermMetaClass());
        metaclasses.add(cm.getLinearizationMetaClass());
        metaclasses.add(cm.getExternalReferenceMetaClass());	//not sure we need this, but other value set classes also have it
        //FIXME: add to CM
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#ICECIMetaClass"));
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#ValueMetaClass"));

        valueSetTopClass = owlModel.getOWLNamedClass(VALUE_SET_PARENT_CLASS_NAME);
        if (valueSetTopClass == null) {
            valueSetTopClass = (OWLNamedClass) cm.createICDCategory(VALUE_SET_PARENT_CLASS_NAME, cm.getChapterXClass().getName());
            addMetaclasses(valueSetTopClass);
        }
    }


    private static void importFile(BufferedReader input) {
        importLines(input);
        addCrossReferences();

        //TODO: optional
        owlModel.getProject().save(new ArrayList());
    }

    private static void importLines(BufferedReader input) {
        String line = null;
        int index = 0;
        try {
            while ((line = input.readLine()) != null) {
            	if (index <= 0) {	//skip the first line containing the column header
            		index++;
            		continue;
            	}
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

        String id = cols[0];
        String parentId = cols[1];
        String title = cols[2];
        String baseExclusions = cols.length > 3 ? cols[3] : null;
        String inclusions = cols.length > 4 ? cols[4] : null;
        String relevantEntities = cols.length > 5 ? cols[5] : null;

        OWLNamedClass cls = createClass(id, parentId);
        addProperties(cls, title, inclusions);

        if (baseExclusions != null) {
            id2baseExclusionsMap.put(cls, baseExclusions);
        }
        if (relevantEntities != null) {
            id2relevantEntitiesMap.put(cls, baseExclusions);
        }
    }

    private static OWLNamedClass createClass(String id, String parentId) {
        OWLNamedClass cls = (OWLNamedClass) cm.createICDCategory(PREFIX_NEW_TERM + id, PREFIX_NEW_TERM + parentId);
        //addMetaclasses(cls);	//metaclasses should be inherited from parent
        return cls;
    }

    private static void addMetaclasses(OWLNamedClass cls) {
        for (RDFSNamedClass metacls : metaclasses) {
            cls.addProtegeType(metacls);
        }
    }

    private static void addProperties(OWLNamedClass cls, String title, String inclusions) {
        RDFResource titleTerm = cm.createTitleTerm();
        titleTerm.addPropertyValue(cm.getLabelProperty(), title);
        cls.addPropertyValue(cm.getIcdTitleProperty(), titleTerm);

        for (String incl : split(inclusions)) {
			RDFResource narrowerTerm = cm.createTerm(cm.getTermNarrowerClass());
			cm.fillTerm(narrowerTerm, null, incl, "en");
			cls.addPropertyValue(cm.getNarrowerProperty(), narrowerTerm);
		}

    }


    private static void addCrossReferences(){
        log.info("Adding cross referneces (for exclusions and related entities) started at: " + new Date());

        OWLObjectProperty referendeCatProp = owlModel.getOWLObjectProperty(REFERENCED_CATEGORY_PROPERTY_NAME);
        for (OWLNamedClass cls : id2baseExclusionsMap.keySet()) {
        	String exclusions = id2baseExclusionsMap.get(cls);
        	for (String exclusionName : split(exclusions)) {
            	exclusionName = PREFIX_NEW_TERM + exclusionName;
                OWLNamedClass excludedCategory = owlModel.getOWLNamedClass(exclusionName);
                if (excludedCategory != null) {
        			RDFResource exclusionTerm = cm.createTerm(cm.getTermBaseExclusionClass());
        			//TODO check this statement
        			exclusionTerm.addPropertyValue(referendeCatProp, excludedCategory);
        			cm.addBaseExclusionTermToClass(cls, exclusionTerm);
                } else {
                    log.warning("Could not find exclusion "+ excludedCategory +" for class: " + cls);
                }
			}
        }
        
        //TODO import relevant entities as well
    }

    
    private static String[] split(String values) {
    	if (values != null && values.isEmpty() == false) {
    		int beginIndex = (values.startsWith("[") ? 1 : 0);
    		int endIndex = (values.endsWith("]") ? values.length()-1 : values.length());
			return values.substring(beginIndex, endIndex).split(VALUE_SEPARATOR);
    	}
    	else {
    		return new String[0];
    	}
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
