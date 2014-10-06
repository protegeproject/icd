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
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ImportIND {

    private final static String IND = "International Nomenclature of Diseases (IND)";
    private final static String DEF = "Definition";
    private final static String SYN = "Synonym";
    private final static String PREFILLED = "prefilled";

    public static void main(String[] args) {
        Project prj = Project.loadProjectFromFile(
                "/work/protege/projects/icd/content_model/icd_int/ind_definitions.pprj", new ArrayList());

        OWLModel indDefKb = (OWLModel) prj.getKnowledgeBase();
        ICDContentModel cm = new ICDContentModel(indDefKb);

        RDFProperty defType = indDefKb.getRDFProperty("http://who.int/icd#definitionType");

        //TODO - cols should be split by ";"
        File file = new File("/work/protege/projects/icd/content_model/icd_int/ind.csv");

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
                            String prop = split[1];
                            String value = split[2];

                            RDFSNamedClass cls = indDefKb.getRDFSNamedClass(ICDContentModelConstants.NS + clsName);
                            if (cls == null) {
                                Log.getLogger().info("***& " + clsName + " not found");
                            } else {
                                RDFResource term = null;
                                if (prop.equals(DEF)) {
                                    term = cm.createDefinitionTerm();
                                } else if (prop.equals(SYN)) {
                                    term = cm.createSynonymTerm();
                                }

                                if (term != null) {
                                    cm.fillTerm(term, null, value, "en", IND);
                                    term.addPropertyValue(cm.getPrefilledDefinitionProperty(), PREFILLED);

                                    if (prop.equals(DEF)) {
                                        cm.addPrefilledDefinitionTermToClass(cls, term);
                                    } else if (prop.equals(SYN)) {
                                        cm.addSynonymTermToClass(cls, term);
                                    }

                                } else {
                                    Log.getLogger().info("*** Could not create term for " + clsName);
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

        prj.save(new ArrayList());

    }
}
