package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ImportDefs {
    //file = "/work/protege/projects/icd/content_model/icd_int/icd_mysql/icd_umbrella.pprj"
    //csv = "/work/protege/projects/icd/content_model/icd_int/2010.09.23_Defs_to_import/additionalDefinitions.csv"

    private final static String DEF = "Definition";
    private final static String SYN = "Synonym";
    //private final static String PREFILLED = "prefilled";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Needs 2 params: ICD pprj file and CSV file");
            return;
        }

        String fileName = args[0];
        String csvFile = args[1];

        Project prj = Project.loadProjectFromFile(fileName , new ArrayList());

        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();
        ICDContentModel cm = new ICDContentModel(owlModel);

        //TODO - cols should be split by "|"
        File file = new File(csvFile);

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    if (line != null) {
                        try {
                            System.out.println(line);
                            String[] split = line.split("\\|");
                            String clsName = split[0];
                            clsName=clsName.substring(1, clsName.length() - 1); //remove quotes

                            String prop = split[1];
                            prop=prop.substring(1, prop.length() - 1); //remove quotes

                            String value = split[2];
                            value=value.substring(1, value.length() - 1); //remove quotes

                            RDFSNamedClass cls = owlModel.getRDFSNamedClass(ICDContentModelConstants.NS + clsName);
                            if (cls == null) {
                                Log.getLogger().info("***& " + clsName + " not found");
                            } else {
                                RDFResource term = null;
                                if (prop.equals(DEF)) {
                                    RDFResource existingDefTerm = (RDFResource) cls.getPropertyValue(cm.getDefinitionProperty());
                                    if (existingDefTerm != null) {
                                        String existingDef = (String) existingDefTerm.getPropertyValue(cm.getLabelProperty());
                                        if (existingDef == null || existingDef.length() == 0) {
                                            Log.getLogger().warning(" 000 Empty defintion: " + cls + " .Remove it." );
                                            cls.removePropertyValue(cm.getDefinitionProperty(), existingDefTerm);
                                            term = cm.createDefinitionTerm();
                                        } else {
                                            Log.getLogger().warning(" +++ Existing definition: " + cls);
                                        }
                                    } else{
                                        term = cm.createDefinitionTerm();
                                    }
                                } else if (prop.equals(SYN)) {
                                    term = cm.createSynonymTerm();
                                }

                                if (term != null) {
                                    cm.fillTerm(term, null, value, "en", null);
                                    //term.addPropertyValue(cm.getPrefilledDefinitionProperty(), PREFILLED);

                                    if (prop.equals(DEF)) {
                                        //cm.addPrefilledDefinitionTermToClass(cls, term);
                                        cm.addDefinitionTermToClass(cls, term);
                                    } else if (prop.equals(SYN)) {
                                        cm.addSynonymTermToClass(cls, term);
                                    }

                                } else {
                                    Log.getLogger().info("*** Did not create term for " + clsName);
                                }
                            }

                        } catch (Exception e) {
                            Log.getLogger().log(Level.WARNING, "Error at import", e);
                        }

                    }
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //prj.save(new ArrayList());

    }
}
