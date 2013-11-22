package edu.stanford.bmir.icd.claml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.ui.FrameComparator;
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
        return (List<RDFSNamedClass>) orderedChildrenMap.values();
    }


    @SuppressWarnings("unchecked")
    private boolean computeOrderedChildrenSortedMap(RDFSNamedClass parent, SortedMap<Integer, RDFSNamedClass> orderedChildrenMap) {

        boolean isValidIndex = true;

        List<RDFResource> childrenIndex = ((List<RDFResource>) parent.getPropertyValues(cm.getChildrenOrderProperty()));

        if (childrenIndex == null) {
            orderedChildrenMap = createOrderedChildrenMap(parent);
            return false;
        }

        List<RDFSNamedClass> unorderedChildren = cm.getRDFSNamedClassList(parent.getSubclasses(false));

        //all this pain is because of the no side effect requirement for reads...
        List<RDFSNamedClass> unindexedChildren = new ArrayList<RDFSNamedClass>();

        for (RDFResource childIndex : childrenIndex) {
            Integer index = (Integer) childIndex.getPropertyValue(cm.getOrderedChildIndexProperty());
            RDFSNamedClass child = (RDFSNamedClass) childIndex.getPropertyValue(cm.getOrderedChildProperty());

            //corrupted index, ignore this index instance for now, it will be fixed next time a reindex will occur (e.g., ordering sibling, move, create)
            if (index == null) {
                if (child != null) {
                    unindexedChildren.add(child);
                }
                isValidIndex = false;
            }

            if (unorderedChildren.contains(child)) {
                orderedChildrenMap.put(index, child);
                unorderedChildren.remove(child);
            } else {
                isValidIndex = false;
            }
        }

        if (unorderedChildren.size() > 0) {
            isValidIndex = false;
        }

        //add to the remaining unordered children, the children with corrupted indexes, and sort them
        unorderedChildren.addAll(unindexedChildren);
        Collections.sort(unorderedChildren, new FrameComparator<Frame>());

        int lastIndex = orderedChildrenMap.lastKey();
        //round it to the next million
        lastIndex = (lastIndex + CHILD_INDEX_INCREMENT) / CHILD_INDEX_INCREMENT;

        //add the unordered children to the index
        for (RDFSNamedClass child : unorderedChildren) {
            orderedChildrenMap.put(lastIndex, child);
            lastIndex = lastIndex + CHILD_INDEX_INCREMENT;
        }

        return isValidIndex;
    }

    public boolean reorderSibling(RDFSNamedClass movedCls, RDFSNamedClass targetCls, boolean isBelow,
            RDFSNamedClass parent, String user) {

        SortedMap<Integer, RDFSNamedClass> orderedChildrenMap = new TreeMap<Integer, RDFSNamedClass>();
        boolean isValidIndex = computeOrderedChildrenSortedMap(parent, orderedChildrenMap);

        //TODO: reorder the soft map; if index is valid, reorder in place, only change the value for the int index for the moved
        // class (this will happen in most cases); if index is invalid, reorder the soft list, and then wipe out existing index and
        // write out a completely new index

        //orderedChildrenMap.


        return false;
    }

    private SortedMap<Integer, RDFSNamedClass> createOrderedChildrenMap(RDFSNamedClass parent) {
        List<RDFSNamedClass> unorderedChildren = cm.getRDFSNamedClassList(parent.getSubclasses(false));
        Collections.sort(unorderedChildren, new FrameComparator<Frame>());

        SortedMap<Integer, RDFSNamedClass> orderedChildrenMap = new TreeMap<Integer, RDFSNamedClass>();
        int index = CHILD_INDEX_INCREMENT;
        for (RDFSNamedClass child : unorderedChildren) {
            orderedChildrenMap.put(index, child);
            index = index + CHILD_INDEX_INCREMENT;
        }

        return orderedChildrenMap;
    }


    private boolean isChildrenIndexValid(RDFSNamedClass parent) {
        List<RDFSNamedClass> unorderedChildren = cm.getRDFSNamedClassList(parent.getSubclasses(false));
        List<RDFResource> childrenIndex = (List<RDFResource>) parent.getPropertyValues(cm.getChildrenOrderProperty());
        if (childrenIndex == null) {
            return false;
        }

        for (RDFResource childIndex : childrenIndex) {
            Integer index = (Integer) childIndex.getPropertyValue(cm.getOrderedChildIndexProperty());
            //corrupted index
            if (index == null) {
                return false;
            }
            RDFSNamedClass child = (RDFSNamedClass) childIndex.getPropertyValue(cm.getOrderedChildProperty());
            if (unorderedChildren.contains(child) == false) {
                return false;
            }
        }

        return true;
    }


}
