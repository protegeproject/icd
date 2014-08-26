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
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class ImportSpecificAnatomyLocation {
    private static Logger log = Log.getLogger(ImportSpecificAnatomyLocation.class);

    private static final String SEPARATOR = "\t";
    private static final String PREFIX_NEW_TERM = "http://who.int/icd#SpecificAnatomyLocation_";

    private static ICDContentModel cm;
    private static OWLModel owlModel;

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
        //FIXME: add to CM
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#SpecificAnatomyMetaClass"));
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#ValueMetaClass"));
    }

    private static void importFile(BufferedReader input) {
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
        String snomedCode = cols[3];
        String snomedTitle = cols[4];
        String altTerm = cols[5];
        String synonym = cols[6];

        OWLNamedClass cls = createClass(id);
        addProperties(cls, title, altTerm, synonym);
        addSnomedRef(cls, snomedCode, snomedTitle);

    }

    private static OWLNamedClass createClass(String id) {
        OWLNamedClass cls = owlModel.createOWLNamedClass(PREFIX_NEW_TERM + id);
        addMetaclasses(cls);
        return cls;
    }

    private static void addMetaclasses(OWLNamedClass cls) {
        for (OWLNamedClass metacls : metaclasses) {
            cls.addProtegeType(metacls);
        }
    }

    private static void addProperties(OWLNamedClass cls, String title, String altName, String synonym) {
        RDFResource titleTerm = cm.createTitleTerm();
        titleTerm.addPropertyValue(cm.getLabelProperty(), title);
        cls.addPropertyValue(cm.getIcdTitleProperty(), titleTerm);

        RDFResource synTerm = cm.createSynonymTerm();
        synTerm.addPropertyValue(cm.getLabelProperty(), synonym);
        synTerm.addPropertyValue(cm.getLangProperty(), "en");
        cls.addPropertyValue(cm.getSynonymProperty(), synTerm);

        RDFResource altTerm = cm.createSynonymTerm();
        altTerm.addPropertyValue(cm.getLabelProperty(), altName);
        altTerm.addPropertyValue(cm.getLangProperty(), "en");
        // TODO: adding the altTerm as synonym for now
        cls.addPropertyValue(cm.getSynonymProperty(), altTerm);
    }


    private static void addSnomedRef(OWLNamedClass cls, String snomedCode, String snomedTitle) {
        RDFResource snomedRef = cm.getS

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
