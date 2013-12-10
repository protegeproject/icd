package edu.stanford.bmir.icd.claml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.util.Log;
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
 * These index information can potentially become corrupted.
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
        isValidIndex = backedChildrenInIndex.retainAll(realChildren) && isValidIndex; //keep in the index only the children that are real

        //likely never true, if we assume that index corruption rarely happens
        if (backedChildrenInIndex.size() != realChildren.size()) {
            isValidIndex = false;

            //keep real children that are not in the index
            realChildren.removeAll(backedChildrenInIndex);
            addChildrenToVirtualIndex(realChildren, orderedChildrenMap);
        }

        if (isValidIndex == false) {
            Log.getLogger().warning("Invalid sibling index for class: " + parent.getName() + " Browser text: " + parent.getBrowserText());
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
        //round it to the next million
        lastIndex = ((lastIndex - 1) / CHILD_INDEX_INCREMENT + 1) * CHILD_INDEX_INCREMENT; //FIXME: check for out of range

        //add the unordered children to the index
        for (RDFSNamedClass child : childrenToAdd) {
            orderedChildrenMap.put(lastIndex, child);
            lastIndex = lastIndex + CHILD_INDEX_INCREMENT;
            //FIXME: check for negative numbers!!!!
        }
    }

    public boolean reorderSibling(RDFSNamedClass movedCls, RDFSNamedClass targetCls, boolean isBelow,
            RDFSNamedClass parent, String user) {

        SortedMap<Integer, RDFSNamedClass> orderedChildrenMap = new TreeMap<Integer, RDFSNamedClass>();
        IndexState indexState = computeOrderedChildrenSortedMap(parent, orderedChildrenMap);

        if (indexState == IndexState.VALID) { //write only one; hopefully most cases

        } else { //invalid or new - write out the entire index

        }

        return indexState == IndexState.VALID ? true : false; //FIXME: not sure what to return
    }


    private void reoderSiblingValidIndex(RDFSNamedClass movedCls, RDFSNamedClass targetCls, boolean isBelow,
            RDFSNamedClass parent, String user) {

    }

    private void reoderSiblingInvalidIndex(RDFSNamedClass movedCls, RDFSNamedClass targetCls, boolean isBelow,
            RDFSNamedClass parent, String user) {

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


}

enum IndexState {
    VALID, // index is in a valid state
    INVALID,  // index is invalid
    NEW; // index does not exist in the ontology
}
