package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
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
        OWLNamedClass retiredCls = owlModel.getOWLNamedClass("http://who.int/icd#Retired");
        OWLNamedClass MLocalRetiredCls = owlModel.getOWLNamedClass("http://who.int/icd#297_6bc3f235_b24a_493e_a8a3_60cb9fb52dbd");

        RDFSNamedClass mChapterRetired = owlModel.getRDFSNamedClass("http://who.int/icd#BulkRetire_2011_01_26");
        if (mChapterRetired == null) {
            mChapterRetired= owlModel.createOWLNamedSubclass("http://who.int/icd#BulkRetire_2011_01_26", MLocalRetiredCls);
        }

        //TODO - cols should be split by "\t"
        File file = new File(csvFile);

        Set<RDFSNamedClass> clses = new HashSet<RDFSNamedClass>();

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

                            RDFSNamedClass cls = owlModel.getRDFSNamedClass(clsName);
                            if (cls == null) {
                                Log.getLogger().info("***& " + clsName + " not found");
                            } else {
                                if (cls.hasSuperclass(retiredCls) || cls.hasSuperclass(MLocalRetiredCls)) {
                                    Log.getLogger().info("^^^ Already retired: " + cls.getBrowserText() +" " + cls.getName());
                                } else {
                                    i++;
                                    Log.getLogger().info(i + ". " + cls.getBrowserText() + " --- to retire");
                                    clses.add(cls);
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

        Log.getLogger().info(clses.size() + " classes to retire.");

        Set<RDFSNamedClass> topClses = getTopLevelClses(clses);
        Log.getLogger().info(topClses.size() + " top level classes: " + topClses);

        int i = 0;
        for (RDFSNamedClass cls : topClses) {
            try {
                i++;
                Log.getLogger().info(i + ". Retiring: " + cls.getBrowserText());
                Collection<RDFSNamedClass> superclses = cls.getSuperclasses(false);
                cls.addSuperclass(mChapterRetired);
                for (RDFSNamedClass supercls : superclses) {
                    cls.removeSuperclass(supercls);
                }
                cls.setDeprecated(true);
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error at retiring " + cls, e);
            }
        }

        Log.getLogger().info("Retired: " + i + " classes.");
    }


    private static Set<RDFSNamedClass> getTopLevelClses(Set<RDFSNamedClass> clses) {
        //less code, more inefficient
        Set<RDFSNamedClass> toRemove = new HashSet<RDFSNamedClass>();
        for (RDFSNamedClass cls : clses) {
            toRemove.addAll(cls.getSubclasses(true));
        }
        clses.removeAll(toRemove);
        return clses;
    }

}
