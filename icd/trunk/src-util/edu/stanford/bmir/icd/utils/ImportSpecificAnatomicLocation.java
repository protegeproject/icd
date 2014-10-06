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

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class ImportSpecificAnatomicLocation {
    private static Logger log = Log.getLogger(ImportSpecificAnatomicLocation.class);

    private static final String SEPARATOR = "\t";
    private static final String PREFIX_NEW_TERM = "http://who.int/icd#SpecificAnatomicLocation_";
    private static final String VALUE_SET_PARENT_CLASS_NAME = "http://who.int/icd#SpecificAnatomicLocation";

    private static ICDContentModel cm;
    private static OWLModel owlModel;
    private static OWLNamedClass valueSetTopClass;

    private static List<RDFSNamedClass> metaclasses = new ArrayList<RDFSNamedClass>();
    private static Map<OWLNamedClass, String> id2parentmap = new HashMap<OWLNamedClass, String>();

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
        metaclasses.add(cm.getSnomedReferenceMetaClass());
        metaclasses.add(cm.getExternalReferenceMetaClass());
        //FIXME: add to CM
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#SpecificAnatomyMetaClass"));
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#ValueMetaClass"));

        valueSetTopClass = owlModel.getOWLNamedClass(VALUE_SET_PARENT_CLASS_NAME);
        if (valueSetTopClass == null) {
            valueSetTopClass = owlModel.createOWLNamedSubclass(VALUE_SET_PARENT_CLASS_NAME, (OWLNamedClass)cm.getChapterXClass());
        }
    }


    private static void importFile(BufferedReader input) {
        importLines(input);
        addParents();

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

        String id = cols[0];
        String parentId = cols[1];
        String title = cols[2];
        String snomedCode = cols.length > 3 ? cols[3] : null;
        String snomedTitle = cols.length > 4 ? cols[4] : null;
        String altTerm = cols.length > 5 ? cols[5] : null;
        String synonym = cols.length > 6 ? cols[6] : null;

        OWLNamedClass cls = createClass(id);
        addProperties(cls, title, altTerm, synonym);
        addSnomedRef(cls, snomedCode, snomedTitle);

        if (parentId != null) {
            id2parentmap.put(cls, PREFIX_NEW_TERM +parentId);
        }
    }

    private static OWLNamedClass createClass(String id) {
        OWLNamedClass cls = owlModel.createOWLNamedClass(PREFIX_NEW_TERM + id);
        addMetaclasses(cls);
        return cls;
    }

    private static void addMetaclasses(OWLNamedClass cls) {
        for (RDFSNamedClass metacls : metaclasses) {
            cls.addProtegeType(metacls);
        }
    }

    private static void addProperties(OWLNamedClass cls, String title, String altName, String synonym) {
        RDFResource titleTerm = cm.createTitleTerm();
        titleTerm.addPropertyValue(cm.getLabelProperty(), title);
        cls.addPropertyValue(cm.getIcdTitleProperty(), titleTerm);

        if (synonym != null && synonym.isEmpty() == false) {
            RDFResource synTerm = cm.createSynonymTerm();
            cm.fillTerm(synTerm, null, synonym, "en");
            cls.addPropertyValue(cm.getSynonymProperty(), synTerm);
        }

        if (altName != null && altName.isEmpty() == false) {
            RDFResource altTerm = cm.createSynonymTerm();
            cm.fillTerm(altTerm, null, altName, "en");
            // TODO: adding the altTerm as synonym for now
            cls.addPropertyValue(cm.getSynonymProperty(), altTerm);
        }
    }


    private static void addSnomedRef(OWLNamedClass cls, String snomedCode, String snomedTitle) {
        if (snomedCode == null) {
            return;
        }

        RDFResource snomedRef = cm.createSnomedReferenceTerm();
        cm.fillTerm(snomedRef, snomedCode, snomedTitle, "en");
        //TODO: not sure if we should use termId or id, so we use both
        snomedRef.addPropertyValue(cm.getTermIdProperty(), snomedCode);

        cls.addPropertyValue(cm.getExternalReferenceProperty(), snomedRef);

    }

    private static void addParents(){
        log.info("Adding parents started at: " + new Date());

        for (OWLNamedClass cls : id2parentmap.keySet()) {
            OWLNamedClass parent = owlModel.getOWLNamedClass(id2parentmap.get(cls));
            if (parent != null) {
                cls.addSuperclass(parent);
                cls.removeSuperclass(owlModel.getOWLThingClass());
            } else {
                log.warning("Could not find parent "+ parent +" for class: " + cls);
            }
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
