package edu.stanford.bmir.icd.claml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


/**
 * This class provides methods to retrieve the sorted children of a class.
 * The ordering information is kept in an index per parent,
 * as instances of the class ChildOrder, which has two properties:
 * orderedChild (which points to the child) and orderedChildIndex
 * that keeps the index of the ordered child for this particular parent.
 *
 * These instances contain redundant information (the children of a class)
 * and need to be updated for operations that affect the children and/or parent
 * (e.g. create class, move in hierarchy, add new parent, reorder siblings in the UI, etc.)
 * This index information can potentially become corrupted.
 *
 * The requirement is that a read operation, e.g, {@link #getOrderedChildren(RDFSNamedClass)}
 * will not modify the ontology, hence the index will also not be changed. This means that in case of an index corruption
 * we need to work around it and only fix the index when a set or reordering operation happens.
 *
 *
 * @author ttania
 *
 */
public class SiblingReordering {

    private static transient Logger log = Log.getLogger(SiblingReordering.class);

    private static int CHILD_INDEX_INCREMENT = 1000000;

    private ICDContentModel cm;


    public SiblingReordering(ICDContentModel cm) {
        this.cm = cm;
    }


    public List<RDFSNamedClass> getOrderedChildren(RDFSNamedClass parent) {
        SortedMap<Integer, RDFSNamedClass> orderedChildrenMap = new TreeMap<Integer, RDFSNamedClass>();
        computeOrderedChildrenSortedMap(parent, orderedChildrenMap);
        return new ArrayList<RDFSNamedClass>(orderedChildrenMap.values());
    }


    public boolean checkIndexAndRecreate(RDFSNamedClass parent, boolean recreateIndex) {
        SortedMap<Integer, RDFSNamedClass> orderedChildrenMap = new TreeMap<Integer, RDFSNamedClass>();
        IndexState state = computeOrderedChildrenSortedMap(parent, orderedChildrenMap);

        if (state == IndexState.VALID) {
            return true;
        }

        if (recreateIndex == true) {
            recreateIndex(parent, orderedChildrenMap);
        }
        return false;
    }



    /**
     * Writes out completely the index for parent using the sorted map.
     * Removes the entire old index.
     * This method write to the KB.
     *
     * @param parent
     * @param orderedChildrenMap
     */
    private void recreateIndex(RDFSNamedClass parent, SortedMap<Integer, RDFSNamedClass> orderedChildrenMap) {

        //TT: commenting out only until we run scripts
        //log.warning("Writing to the KB a new index for parent: " + parent);

        OWLModel owlModel = parent.getOWLModel();
        boolean eventsEnabled = owlModel.setGenerateEventsEnabled(false);

        try {
            parent.setPropertyValue(cm.getChildrenOrderProperty(), null);

            RDFProperty childrenOrderProp = cm.getChildrenOrderProperty();

            int index = CHILD_INDEX_INCREMENT;
            for (RDFSNamedClass child : orderedChildrenMap.values()) {
                RDFResource indexInst = cm.createOrderedChildIndex(child, index);
                parent.addPropertyValue(childrenOrderProp, indexInst);
                index = index + CHILD_INDEX_INCREMENT;
            }

        } catch (Exception e) {
            log.log(Level.WARNING,"There was a problem at writing child order index for parent: " + parent,  e);
            throw new RuntimeException("Problem at writing child order index for parent: " + parent, e);
        } finally {
            owlModel.setGenerateEventsEnabled(eventsEnabled);
        }
    }


    /**
     * This method has to assume that the index is somehow corrupted,
     * but it is not allowed to fix the index (because reads should not trigger
     * writes). So, it has to work around this issue, and the method gets
     * complicated.
     *
     * <p>
     * Types of index corruption:
     * <ul>
     *  <li>null -> child</li>
     *  <li>index -> null</li>
     *  <li>index -> child that is not a child</li>
     *  <li>index1 = index2</li>
     *  <li>missing index for an existing child</li>
     *  </ul>
     *  </p>
     *
     *  <p>
     *  If index is found to be invalid, the method will try to fix it in place,
     *  i.e, add the bad index entries at the end of the index.
     *  </p>
     *
     * @param parent
     * @param orderedChildrenMap - will be filled by this call
     * @return
     */
    @SuppressWarnings("unchecked")
    private IndexState computeOrderedChildrenSortedMap(RDFSNamedClass parent, SortedMap<Integer, RDFSNamedClass> orderedChildrenMap) {

        boolean isValidIndex = true;

        List<RDFResource> childrenIndex = ((List<RDFResource>) parent.getPropertyValues(cm.getChildrenOrderProperty()));

        if (childrenIndex == null || childrenIndex.size() == 0) {
            createOrderedChildrenMap(parent, orderedChildrenMap);
            // no need to log this, it is the initial case
            return IndexState.NEW;
        }

        isValidIndex = fillOrderedMap(childrenIndex, orderedChildrenMap);

        // the real subclasses that should also show up in the index
        List<RDFSNamedClass> realChildren = cm.getRDFSNamedClassList(parent.getSubclasses(false));

        // the values are backed by the map, any operation on this will affect the map
        Collection<RDFSNamedClass> backedChildrenInIndex = orderedChildrenMap.values();
        isValidIndex = !backedChildrenInIndex.retainAll(realChildren) && isValidIndex; //keep in the index only the children that are real

        //likely never true, if we assume that index corruption rarely happens
        if (backedChildrenInIndex.size() != realChildren.size()) {
            isValidIndex = false;

            //keep real children that are not in the index
            realChildren.removeAll(backedChildrenInIndex);
            addChildrenToVirtualIndex(realChildren, orderedChildrenMap);
        }

        if (isValidIndex == false) {
            if (log.getLevel().equals(Level.FINE)) {
                log.fine("Invalid sibling index for class: " + parent.getName() + " Browser text: " + parent.getBrowserText());
            }
        }

        return isValidIndex == true ? IndexState.VALID : IndexState.INVALID;
    }

    private boolean fillOrderedMap(List<RDFResource> childrenIndex, SortedMap<Integer, RDFSNamedClass> orderedChildrenMap) {
        boolean isValidIndex = true;

        //fill sorted map with whatever is in the index, even corrupted stuff
        for (RDFResource childIndexInstance : childrenIndex) {
            Integer index = (Integer) childIndexInstance.getPropertyValue(cm.getOrderedChildIndexProperty());
            RDFSNamedClass child = (RDFSNamedClass) childIndexInstance.getPropertyValue(cm.getOrderedChildProperty());

            if (index != null) { //avoid a NPE
                orderedChildrenMap.put(index, child);
            } else {
                isValidIndex = false;
            }
        }

        return isValidIndex;
    }

    private void addChildrenToVirtualIndex(List<RDFSNamedClass> childrenToAdd, SortedMap<Integer, RDFSNamedClass> orderedChildrenMap) {
        Collections.sort(childrenToAdd, new FrameComparator<Frame>());

        int lastIndex = orderedChildrenMap.lastKey();

        //check for out of range for adding the new children
        if (lastIndex + childrenToAdd.size() * CHILD_INDEX_INCREMENT < 0) {
            //redo all the virtual index for the entire map

            ArrayList<RDFSNamedClass> childrenInIndex = new ArrayList<RDFSNamedClass>(orderedChildrenMap.values());
            childrenInIndex.addAll(childrenToAdd);

            orderedChildrenMap.clear();

            int index = CHILD_INDEX_INCREMENT;
            for (RDFSNamedClass child : childrenInIndex) {
                orderedChildrenMap.put(index, child);
            }

            return;
        }

        // if not out of range:
        //round it to the next million
        lastIndex = ((lastIndex - 1) / CHILD_INDEX_INCREMENT + 1) * CHILD_INDEX_INCREMENT;

        //add the unordered children to the index
        for (RDFSNamedClass child : childrenToAdd) {
            lastIndex = lastIndex + CHILD_INDEX_INCREMENT;
            orderedChildrenMap.put(lastIndex, child);
        }
    }

    // callers of this method should embed it in a transaction
    public boolean reorderSibling(RDFSNamedClass movedCls, RDFSNamedClass targetCls, boolean isBelow,
            RDFSNamedClass parent, String user) {

        SortedMap<Integer, RDFSNamedClass> orderedChildrenMap = new TreeMap<Integer, RDFSNamedClass>();
        IndexState indexState = computeOrderedChildrenSortedMap(parent, orderedChildrenMap);

        int positionToInsert = getPositionToInsert(orderedChildrenMap, movedCls, targetCls, isBelow, parent);

        if (indexState == IndexState.VALID && positionToInsert > -1) { //write only one; hopefully most cases
            reorderSiblingValidIndex(movedCls, positionToInsert, parent);
        } else { //invalid or new - write out the entire index
           reorderSiblingInvalidIndex(orderedChildrenMap, movedCls, targetCls, isBelow, parent);
        }

        return true; //FIXME: return if operation succeeded
    }


    private void reorderSiblingValidIndex(RDFSNamedClass movedCls, int positionToInsert, RDFSNamedClass parent) {

        // System.out.println("Changing index for child: " + movedCls.getBrowserText());

        List<RDFResource> childrenIndex = ((List<RDFResource>) parent.getPropertyValues(cm.getChildrenOrderProperty()));

        for (RDFResource childIndex : childrenIndex) {
            RDFResource child = (RDFResource) childIndex.getPropertyValue(cm.getOrderedChildProperty());
            if (movedCls.equals(child)) {
                childIndex.setPropertyValue(cm.getOrderedChildIndexProperty(), positionToInsert);
                return;
            }
        }

        log.warning("Something went wrong with the changing the order index for child " + movedCls + " of parent " + parent);
    }

    //FIXME: disable instance creation events..; when many instance creation events are generated, the UI
    //gets very slow at processing them. It is not clear that we need to disable events, as the index will be
    //pre-populated and we don't expect too many creation events, except when something goes very wrong with
    //the index. Fix later, if needed
    private void reorderSiblingInvalidIndex(SortedMap<Integer, RDFSNamedClass> orderedChildrenMap, RDFSNamedClass movedCls,
            RDFSNamedClass targetCls, boolean isBelow, RDFSNamedClass parent) {

        // System.out.println("Recreating new child index for: " + parent.getBrowserText());

        List<RDFSNamedClass> orderedChildren = new ArrayList<RDFSNamedClass>(orderedChildrenMap.values());
        orderedChildren.remove(movedCls);

        int targetIndex = orderedChildren.indexOf(targetCls);
        if (targetIndex == -1) {
            log.warning("Problem at creating a new children index for " + parent +". Target class: " + targetCls + " does not exist in virtual index.");
            orderedChildren.add(movedCls);
        } else {
            if (isBelow == true) { // insert below
                if (targetIndex == orderedChildren.size() - 1) {
                    orderedChildren.add(movedCls);
                } else {
                    orderedChildren.add(targetIndex + 1, movedCls);
                }
            } else { // insert above
                orderedChildren.add(targetIndex, movedCls);
            }
        }

        // write new index
        recreateIndex(parent, orderedChildrenMap);

    }


    private int getPositionToInsert(SortedMap<Integer, RDFSNamedClass> orderedChildrenMap, RDFSNamedClass movedCls,
            RDFSNamedClass targetCls, boolean isBelow, RDFSNamedClass parent) {

        int childBefore = 0;
        int childAfter = -1; //this means there is an error

        for (Iterator<Integer> iterator = orderedChildrenMap.keySet().iterator(); iterator.hasNext();) {
            int index = iterator.next();
            if (orderedChildrenMap.get(index).equals(targetCls)) {

                if (isBelow == true) { // insert below
                    childBefore = index;
                    if (iterator.hasNext()) {
                        childAfter = iterator.next();
                        int pos = (childBefore + childAfter) / 2;
                        return pos == childBefore ? -1 : pos;
                    } else { //insert after last one in list
                        int pos = childBefore + CHILD_INDEX_INCREMENT;
                        return pos < 0 ? -1 : pos;
                    }
                }
                   else { // insert above - it happens with the first child, if inserting above it, otherwise the below event is triggered
                    //there might be other cases, but we'll not treat until necessary
                       int pos = (index + childBefore) / 2;
                       return pos == childBefore ? -1 : pos;
                }
            }
            childBefore = index;
        }
        return -1;
    }

    private void createOrderedChildrenMap(RDFSNamedClass parent, SortedMap<Integer, RDFSNamedClass> orderedChildrenMap) {
        List<RDFSNamedClass> unorderedChildren = cm.getRDFSNamedClassList(parent.getSubclasses(false));
        Collections.sort(unorderedChildren, new FrameComparator<Frame>());

        int index = CHILD_INDEX_INCREMENT;
        for (RDFSNamedClass child : unorderedChildren) {
            orderedChildrenMap.put(index, child);
            index = index + CHILD_INDEX_INCREMENT;
        }
    }


    /**
     * Adds the child to the index of this parent at the end of the list. If isSiblingIndex == true, it
     * will not recompute the index, it will assume it is valid, and it will only add the new value
     * at the end.
     * @param parent
     * @param cls
     * @param isSiblingIndexValid - usually coming from a previous computation. If true, it will force
     * a recreation of the index
     * @return - true if the index is not recreated, or false - otherwise
     */
    public boolean addChildToParentIndex(RDFSNamedClass parent, RDFSNamedClass cls, boolean isSiblingIndexValid) {
        SortedMap<Integer, RDFSNamedClass> orderedChildrenMap = new TreeMap<Integer, RDFSNamedClass>();
        computeOrderedChildrenSortedMap(parent, orderedChildrenMap);

        if (isSiblingIndexValid == false) {
            //TODO - there is a small problem here: if the index did not exist, the new class will not be added last, but by
            //alphabetical order and the UI will show the child as the last.
            recreateIndex(parent, orderedChildrenMap);
            return false;
        }

        // The last index is the child added the last (the child to be added).
        // No other children have been added meanwhile, because this runs in a transaction.
        // So, it is pretty safe to assume that the index of the last child is the last index.
        // TT: documented this, in case there will be bugs later
         int index = orderedChildrenMap.lastKey();

        // index is valid, add the child at the end
        ICDContentModel cm = new ICDContentModel(parent.getOWLModel());
        RDFResource inst = cm.createOrderedChildIndex(cls, index);
        parent.addPropertyValue(cm.getChildrenOrderProperty(), inst);

        return true;
    }


    public boolean removeChildFromIndex(RDFSNamedClass parent, RDFSNamedClass cls, boolean isSiblingIndexValid) {
        SortedMap<Integer, RDFSNamedClass> orderedChildrenMap = new TreeMap<Integer, RDFSNamedClass>();
        computeOrderedChildrenSortedMap(parent, orderedChildrenMap);

        if (isSiblingIndexValid == false) {
            recreateIndex(parent, orderedChildrenMap);
            return false;
        }

        RDFProperty orderedChildProp = cm.getOrderedChildProperty();
        RDFResource indexInstToRemove = null;

        List<RDFResource> childrenIndex = ((List<RDFResource>) parent.getPropertyValues(cm.getChildrenOrderProperty()));
        for (RDFResource index : childrenIndex) {
            if (cls.equals(index.getPropertyValue(orderedChildProp))) {
                indexInstToRemove = index;
                break;
            }
        }

        if (indexInstToRemove != null) {
            parent.removePropertyValue(orderedChildProp, indexInstToRemove);
            indexInstToRemove.delete();
        }

        return true;
    }

}
