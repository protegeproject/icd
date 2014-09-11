package edu.stanford.bmir.icd.utils;

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

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class ImportHistopathology {
    private static Logger log = Log.getLogger(ImportHistopathology.class);

    private static final String SEPARATOR = "\t";
    private static final String PREFIX_NEW_TERM = "http://who.int/icd#Histopathology_";
    private static final String VALUE_SET_PARENT_CLASS_NAME = "http://who.int/icd#Histopathology";
    private static final String TITLE = "Title";
    private static final String SYN = "Synonym";

    private static ICDContentModel cm;
    private static OWLModel owlModel;
    private static OWLNamedClass valueSetTopClass;

    private static List<RDFSNamedClass> metaclasses = new ArrayList<RDFSNamedClass>();
    private static Map<String, String> title2id = new HashMap<String, String>();


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
        metaclasses.add(cm.getExternalReferenceMetaClass());
        //FIXME: check if this is the right metaclass
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#HistopathologyMetaClass"));
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#ValueMetaClass"));

        valueSetTopClass = owlModel.getOWLNamedClass(VALUE_SET_PARENT_CLASS_NAME);
        if (valueSetTopClass == null) {
            valueSetTopClass = owlModel.createOWLNamedSubclass(VALUE_SET_PARENT_CLASS_NAME, (OWLNamedClass)cm.getChapterXClass());
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


        String notid = getValue(cols, 0);
        String parent1 = getValue(cols, 1);
        String parent2 = getValue(cols, 2);
        String type = getValue(cols, 3);
        String value = getValue(cols, 4);
        String icdocode = getValue(cols, 5);


        OWLNamedClass cls = getOrCreateClass(notid);

        if (TITLE.equals(type)) {
            addProperties(cls, value, null, null);
            addICDORef(cls, icdocode, null);
        } else if (SYN.equals(type)) {
            addProperties(cls, null, null, value);
        }

        addParent(cls, parent1, parent2);

    }

    private static void addParent(OWLNamedClass cls, String parent1, String parent2) {

        boolean parent1Exists = title2id.get(parent1) != null;
        OWLNamedClass parent1Cls = getOrCreateClass(parent1);

        boolean parent2Exists = title2id.get(parent2) != null;
        OWLNamedClass parent2Cls = getOrCreateClass(parent2);

        if (parent1Exists == false) {
            addProperties(parent1Cls, parent1, null, null);
            parent1Cls.addSuperclass(valueSetTopClass);
            parent1Cls.removeSuperclass(owlModel.getOWLThingClass());
        }

        if (parent2Exists == false) {
            addProperties(parent2Cls, parent2, null, null);
            parent2Cls.addSuperclass(parent1Cls);
            parent2Cls.removeSuperclass(owlModel.getOWLThingClass());
        }

        if (cls.hasSuperclass(parent2Cls) == false) {
            cls.addSuperclass(parent2Cls);
        }

        cls.removeSuperclass(owlModel.getOWLThingClass());
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


    private static OWLNamedClass getOrCreateClass(String notId) {
        String id = title2id.get(notId);

        OWLNamedClass cls = (id == null) ? null : owlModel.getOWLNamedClass(id);
        if (cls == null) {
            id = PREFIX_NEW_TERM + IDGenerator.getNextUniqueId();
            cls = owlModel.createOWLNamedClass(id);
            addMetaclasses(cls);
            title2id.put(notId, id);
        }
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
    }

    private static void addReferenceScaleValueTerm(String refTermClsName, OWLNamedClass cls) {
        if (refTermClsName == null) {
            return;
        }

        OWLNamedClass refTermCls = owlModel.getOWLNamedClass(refTermClsName);
        if (refTermCls == null) {
            log.warning("Could not find term reference class: " + refTermClsName);
            return;
        }

        RDFResource refTerm = cm.createTerm(refTermCls);
        refTerm.addPropertyValue(cm.getReferencedValueProperty(), cls);

    }

    private static void addICDORef(OWLNamedClass cls, String code, String title) {
        if (code == null) {
            return;
        }

        RDFResource extRef = cm.createExternalReferenceTerm();
        cm.fillTerm(extRef, code, title, "en");
        //TODO: not sure if we should use termId or id, so we use both
        extRef.addPropertyValue(cm.getTermIdProperty(), code);
        extRef.addPropertyValue(cm.getOntologyIdProperty(), "ICD-O");

        cls.addPropertyValue(cm.getExternalReferenceProperty(), extRef);

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
