package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.bmir.icd.claml.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class RetireMuskuloSkeletal {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Needs 2 params: ICD pprj file and TXT file");
            return;
        }

        String fileName = args[0];
        String csvFile = args[1];

        Project prj = Project.loadProjectFromFile(fileName , new ArrayList());

        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();
        ICDContentModel cm = new ICDContentModel(owlModel);

        //create retired cls
        RDFSNamedClass retiredCls = owlModel.getRDFSNamedClass("http://who.int/icd#Retired");
        RDFSNamedClass MRetiredCls = owlModel.getRDFSNamedClass("http://who.int/icd#297_6bc3f235_b24a_493e_a8a3_60cb9fb52dbd");

        //TODO - cols should be split by "\t"
        File file = new File(csvFile);

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                int i = 0;
                while ((line = input.readLine()) != null) {
                    if (line != null) {
                        try {
                            //System.out.println(line);
                            String[] split = line.split("\\t");
                            String clsName = split[0];

                            RDFSNamedClass cls = owlModel.getRDFSNamedClass(ICDContentModelConstants.NS + clsName);
                            if (cls == null) {
                                Log.getLogger().info("***& " + clsName + " not found");
                            } else {
                                if (cls.hasSuperclass(retiredCls) || cls.hasSuperclass(MRetiredCls)) {
                                    Log.getLogger().info("^^^ Already retired: " + cls.getBrowserText() +" " + cls.getName());
                                } else {
                                    i++;
                                    Log.getLogger().info(i + ". " + cls.getBrowserText() + " --- to retire");
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
    }
}
