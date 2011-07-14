package edu.stanford.bmir.protegex.icd.diff;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protege.widget.AbstractTabWidget;

public class ICDDiff extends AbstractTabWidget {	
	
	private static final long serialVersionUID = 1092702642237713810L;
    private TreeComponent treePanelLeft;
	private TreeComponent treePanelRight;
	
	private Map<Cls, FrameStatus> left_cls2Status = new HashMap<Cls, FrameStatus>();
	private Map<Cls, FrameStatus> right_cls2Status = new HashMap<Cls, FrameStatus>();

	public void initialize() {
		setLabel("ICD Diff Tab");	
		setLayout(new BorderLayout());
		
		JSplitPane mainPane = ComponentFactory.createLeftRightSplitPane();
		mainPane.setLeftComponent(treePanelLeft = createTreePanel(left_cls2Status));		
		mainPane.setRightComponent(treePanelRight = createTreePanel(right_cls2Status));
		mainPane.setDividerLocation(0.5);
		mainPane.setResizeWeight(0.5);
		add(new JScrollPane(mainPane), BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.add(new JButton(getCompareAction()));
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	
	private TreeComponent createTreePanel(Map<Cls, FrameStatus> cls2Status) {
		TreeComponent treeComponent = new TreeComponent(cls2Status);
		return treeComponent;
	}

	protected Action getCompareAction() {
		return new AbstractAction("Compare") {
			private static final long serialVersionUID = 1058398400197261444L;

            public void actionPerformed(ActionEvent arg0) {
				if (treePanelLeft.getKb() == null || treePanelRight.getKb() == null) {
					ModalDialog.showMessageDialog(ICDDiff.this, "Please load first two models to compare.");
					return;
				}				
				onCompare(treePanelLeft.getKb(), treePanelRight.getKb());
			}			
		};
	}


	protected void onCompare(KnowledgeBase kb_left, KnowledgeBase kb_right) {
		//not the best algorithm...
		//we are cheating and compare by name - should compare by index, or
		//some other configurable slot
		//TODO: algorithm should be optimized
		
		//reinit
		left_cls2Status.clear();
		right_cls2Status.clear();
		
		//traversing left tree
		Collection clses1 = kb_left.getClses();
		for (Iterator iterator = clses1.iterator(); iterator.hasNext();) {
			Cls cls1 = (Cls) iterator.next();
			if (cls1.isSystem()) { continue; }
			
			Cls cls2 = kb_right.getCls(cls1.getName());
			if (cls2 != null) { //found a match
				//test if parents are still the same
				if (equalsSetByName(cls1.getDirectSuperclasses(), cls2.getDirectSuperclasses())) {
					setStatus(cls1, FrameStatus.UNCHANGED, left_cls2Status);
					setStatus(cls2, FrameStatus.UNCHANGED, right_cls2Status);
				} else  {
					setStatus(cls1, FrameStatus.MOVED, left_cls2Status);
					setStatus(cls2, FrameStatus.MOVED, right_cls2Status);
					setParentsStatus(cls1, FrameStatus.CHILDREN_MOVED, left_cls2Status);
					setParentsStatus(cls2, FrameStatus.CHILDREN_MOVED, right_cls2Status);
				}
			} else {
				setStatus(cls1, FrameStatus.DELETED, left_cls2Status);
				setParentsStatus(cls1, FrameStatus.CHILDREN_DELETED, left_cls2Status);
			}			
		}
		
		//traversing right tree
		Collection clses2 = kb_right.getClses();
		for (Iterator iterator = clses2.iterator(); iterator.hasNext();) {
			Cls cls2 = (Cls) iterator.next();
			if (cls2.isSystem()) { continue ; }
			
			Cls cls1 = kb_left.getCls(cls2.getName());
			if (cls1 == null) { //found a match				
				setStatus(cls2, FrameStatus.ADDED, right_cls2Status);
				setParentsStatus(cls2, FrameStatus.CHILDREN_ADDED, right_cls2Status);
			}			
		}
		
		treePanelLeft.revalidate();
		treePanelRight.revalidate();
		treePanelLeft.repaint();
		treePanelRight.repaint();
	}
	
	
	private boolean equalsSetByName(Collection<Cls> set1, Collection<Cls> set2) {
		if (set1.size() != set2.size()) { return false; }
		Set<String> names1 = new HashSet<String>();
		Set<String> names2 = new HashSet<String>();
		for (Frame frame : set1) { names1.add(frame.getName()); }
		for (Frame frame : set2) { names2.add(frame.getName()); }
		return names1.equals(names2);
	}
	
	private void setStatus(Cls cls, FrameStatus status, Map<Cls, FrameStatus> cls2status) {
		FrameStatus existingStatus = cls2status.get(cls);
		if (existingStatus == null || existingStatus == FrameStatus.UNCHANGED) {
			cls2status.put(cls, status);
		}
	}
	
	private void setParentsStatus(Cls cls, FrameStatus status, Map<Cls, FrameStatus> cls2status) {
		//TODO: optimize, do this recursively
		Collection<Cls> directParents = cls.getDirectSuperclasses();
		for (Cls directParent : directParents) {
			if (!directParent.isSystem()) {
				FrameStatus currentStatus = cls2status.get(directParent);
				setStatus(directParent, status, cls2status);
				if (currentStatus != cls2status.get(directParent)) {
					setParentsStatus(directParent, status, cls2status);
				}
			}
		}
	}

}
