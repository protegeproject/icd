package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;

public class ChaoUtils {

    private static KnowledgeBase chaoKb;
    private static Cls compositeChangeCls;
    private static Cls changeCls;
    private static Slot timestampSlot;
    private static Slot partOfCompositeChangeSlot;

    public static void main(String[] args) {
        Project prj = Project.loadProjectFromFile("/work/protege/projects/icd/content_model/icd_int/icd_mysql/protege2/annotation_ICD.pprj", new ArrayList());

        chaoKb = prj.getKnowledgeBase();
        changeCls = chaoKb.getCls("Change");
        compositeChangeCls = chaoKb.getCls("Composite_Change");
        partOfCompositeChangeSlot = chaoKb.getSlot("partOfCompositeChange");
        timestampSlot = chaoKb.getSlot("timestamp");

        deleteSimpleChanges();

        //copy();
    }


    private static void copy() {
        Project source = Project.loadProjectFromFile("/work/protege/projects/copy/source.pprj", new ArrayList());
        Project target = Project.loadProjectFromFile("/work/protege/projects/copy/target.pprj", new ArrayList());

        Instance s_inst = source.getKnowledgeBase().getInstance("source_Class0");
        s_inst.copy(target.getKnowledgeBase(), null, true);

        target.save(new ArrayList());
    }

    private static void deleteSimpleChanges() {
        chaoKb.setGenerateEventsEnabled(false);
        System.out.println("Started delete simple changes on: " + new Date());
        Collection<Cls> changeSubclses = new ArrayList(changeCls.getSubclasses());
        changeSubclses.remove(compositeChangeCls);
        for (Cls cls : changeSubclses) {
            System.out.println("=== Cls: " + cls.getBrowserText() + " instance count: " + cls.getDirectInstanceCount() + " on " + new Date());
            int i = 0;
            Collection<Instance> directInstances = cls.getDirectInstances();
            for (Instance instance : directInstances) {
                if (i % 1000 == 0) {
                    System.out.println("\tDeleted " + i + " instances\t" + new Date());
                }
                if (instance.getOwnSlotValue(partOfCompositeChangeSlot) != null) {
                    deleteChangeInstance(instance);
                    i++;
                }
            }
            System.out.println(" ^^^ Finished cls " + cls.getBrowserText() + " deleted: " + i + " instances on " + new Date());
        }
    }


    private static void deleteChangeInstance(Instance inst) {
        Instance timestamp = (Instance) inst.getOwnSlotValue(timestampSlot);

        inst.delete();
        timestamp.delete();
    }

}
