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


public class ImportEtiology {
    private static Logger log = Log.getLogger(ImportEtiology.class);

    private static final String SEPARATOR = "\t";
    private static final String PREFIX_NEW_TERM = "http://who.int/icd#Etiology_";

    //no. of columns that represent tree levels
    private static final int NO_OF_TREE_COLUMNS = 6;

    private static ICDContentModel cm;
    private static OWLModel owlModel;

    private static List<RDFSNamedClass> metaclasses = new ArrayList<RDFSNamedClass>();
    private static Map<Integer, OWLNamedClass> currentParentForLevel = new HashMap<Integer, OWLNamedClass>();

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
        //FIXME: check if this is the right metaclass
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#InfectiousAgentMetaClass"));
        metaclasses.add(owlModel.getOWLNamedClass("http://who.int/icd#ValueMetaClass"));
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

        int i = NO_OF_TREE_COLUMNS;
        String id = null;

        while (i >= 0 && id == null) {
            i = i - 1;
            id = getValue(cols, i);
        }

        if (id == null) {
            log.warning("Could not find class for line: " + line);
            return;
        }

        String synonym = getValue(cols, NO_OF_TREE_COLUMNS);
        String narrowerTerm = getValue(cols, NO_OF_TREE_COLUMNS + 1);

        OWLNamedClass cls = createClass(id);
        addProperties(cls, id, narrowerTerm, synonym);
        //addSnomedRef(cls, snomedCode, snomedTitle);

        currentParentForLevel.put(i, cls);

        addParent(cls, i, line);

    }

    private static void addParent(OWLNamedClass cls, int level, String line) {
        if (level == 0) {
            cls.addSuperclass(owlModel.getOWLThingClass());
            return;
        }

        OWLNamedClass parent = currentParentForLevel.get(level - 1);
        if (parent == null) {
            log.warning("Could not find parent for "+ cls +" at level " + level +" Line: " + line);
            return;
        }

        cls.addSuperclass(parent);
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

    private static OWLNamedClass createClass(String id) {
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
        RDFResource titleTerm = cm.createTitleTerm();
        titleTerm.addPropertyValue(cm.getLabelProperty(), title);
        cls.addPropertyValue(cm.getIcdTitleProperty(), titleTerm);

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


    //may be used later
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
