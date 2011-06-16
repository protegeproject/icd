package edu.stanford.bmir.icd.claml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;

public class KBUtilToBeRemoved {

    /* ******************************************************************** *
     * TODO BEFORE THE NEXT PROTEGE RELEASE MOVE THE FOLLOWING 4 METHODS INTO
     *      THE ModelUtilities CLASS!!!!!!!!!!!
     *      AND
     *      ACTIVATE THE ModelUtilities_Test JUNIT TEST!!!!!!!
     * ******************************************************************** */

    /**
     * Returns all own slot values for the slot <code>slot</code> for every superclass of a class.
     *
     * @param cls - a class
     * @param slot - a slot
     *
     * @return a map containing all own slot values as keys, and each value is mapped to a list
     *      of classes which contained the value as their own slot value
     */
    public static Map<Object, List<Instance>> getPropertyValuesOnAllSuperclasses(Cls cls, Slot slot) {
        KnowledgeBase kb = cls.getKnowledgeBase();
        Cls rootCls = kb.getRootCls();
        Slot parentSlot = kb.getSystemFrames().getDirectSuperclassesSlot();

        return getPropertyValuesOnPropertyClosureToRoot(cls, parentSlot, rootCls, slot);
    }

    /**
     * Returns all own slot values for the slot <code>slot</code> for every instance
     * in any of the paths between an instance (<code>resource</code>) and
     * a root instances (<code>rootResource</code>) following the relationships
     * defined by the <code>parentSlot</code> slot.
     *
     * @param resource - a resource
     * @param parentSlot - the slot that is used to traverse the instance graph to the <code>rootResource</code>
     * @param rootResource - the resource that is the
     * @param slot - a slot
     *
     * @return a map containing all own slot values as keys, and each value is mapped to a list
     *      of instances which contained the value as their own slot value
     */
    public static Map<Object, List<Instance>> getPropertyValuesOnPropertyClosureToRoot(
            Instance resource, Slot parentSlot, Instance rootResource, Slot slot) {
        Map<Object, List<Instance>> result = new HashMap<Object, List<Instance>>();

        Collection<List<Instance>> propertyClosureToRoot = getPropertyClosureToRoot(resource, parentSlot, rootResource);
        Set<Instance> allNodes = new HashSet<Instance>();
        for (List<Instance> path : propertyClosureToRoot) {
            allNodes.addAll(path);
        }

        for (Instance node : allNodes) {
            Collection<?> values = node.getOwnSlotValues(slot);
            for (Object value : values) {
                if (value != null) {
                    List<Instance> nodeList = result.get(value);
                    if (nodeList == null) {
                        nodeList = new ArrayList<Instance>();
                        result.put(value, nodeList);
                    }
                    nodeList.add(node);
                }
            }
        }

        return result;
    }

    /**
     * Computes all paths from an instance to the "root instance" node by navigating on a
     * given slot.
     *
     * @param resource - an instance
     * @param parentSlot - slot to navigate on towards a "root node"
     * @param rootResource - an instance considered as the root of the navigation tree,
     *              necessary to stop the navigation.
     *
     * @return a collection of the paths from the resource to the root resource
     */
    public static Collection<List<Instance>> getPropertyClosureToRoot(Instance resource, Slot parentSlot, Instance rootResource) {
        Collection<List<Instance>> results = new ArrayList<List<Instance>>();
        if (resource.equals(rootResource)) {
            results.add(Collections.singletonList(rootResource));
            return results;
        }
        getPropertyClosureToRoot(resource, parentSlot, rootResource, new LinkedList<Instance>(), results);
        return results;
    }

    private static void getPropertyClosureToRoot(Instance resource, Slot parentSlot, Instance rootResource,
            List<Instance> path, Collection<List<Instance>> pathLists) {
        path.add(0, resource);

        Collection<?> parents = resource.getOwnSlotValues(parentSlot);

        for (Object parentObject : parents) {
            if (parentObject instanceof Instance) {
                Instance parentResource = (Instance) parentObject;
                if (parentResource.equals(rootResource)) {
                    List<Instance> copyPathList = new ArrayList<Instance>(path);
                    copyPathList.add(0, parentResource);
                    pathLists.add(copyPathList);
                } else if (!path.contains(parentResource)) {
                    //if (ModelUtilities.isVisibleInGUI(parentResource)) { //TODO: do we want this?
                    List<Instance> copyPath = new ArrayList<Instance>(path);
                    getPropertyClosureToRoot(parentResource, parentSlot, rootResource, copyPath, pathLists);
                    //}
                }
            }
        }
    }

    /* ******************************************************************** *
     * END OF TODO
     * ******************************************************************** */

}
