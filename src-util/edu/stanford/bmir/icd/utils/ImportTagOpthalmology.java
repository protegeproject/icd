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

public class ImportTagOpthalmology {
    private final static String PREFILLED = "prefilled";

    public static void main(String[] args) {
        Project prj = Project.loadProjectFromFile(
                "/work/protege/projects/icd/content_model/icd_int/icd_mysql/icd_umbrella.pprj", new ArrayList());

        OWLModel icd = (OWLModel) prj.getKnowledgeBase();
        ICDContentModel cm = new ICDContentModel(icd);

        File file = new File("/home/ttania/Desktop/Link to icd/2009.11.23Import_to_prod/2009.11.24/opt2Processed.txt");

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    if (line != null) {
                        try {
                            System.out.println(line);
                            if (line != null && line.length() > 0) {
                                String[] split = line.split("\\@");
                                String clsName = split[0];
                                String parentName = split[1];
                                String title = split[2];

                                RDFSNamedClass cls = icd.getRDFSNamedClass(ICDContentModelConstants.NS + clsName);
                                if (cls != null) {
                                    Log.getLogger().info("***! " + clsName + " already exists");
                                } else {
                                    RDFSNamedClass parent = icd.getRDFSNamedClass(ICDContentModelConstants.NS + parentName);
                                    if (parent == null) {
                                        Log.getLogger().info("***!- Parent: " + parentName + " does not exist for " + clsName);
                                    } else {
                                        cls = cm.createICDCategory(clsName, parentName);

                                        RDFResource term = cm.createTitleTerm();
                                        cm.fillTerm(term, null, title, "en");
                                        cm.addTitleTermToClass(cls, term);
                                        cls.addPropertyValue(cm.getIcdCodeProperty(), clsName);

                                    }
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
