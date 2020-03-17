package edu.stanford.bmir.protegex.icd;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Transaction;
import edu.stanford.smi.protege.ui.ClsesPanel;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.widget.ClsesTab;

@Deprecated
public class ICDTab extends ClsesTab {
    private static final long serialVersionUID = -8916256673675254058L;
    private ClsesPanel clsesPanel;
	private Icon categoryIcon;


	@Override
	public void initialize() {
		super.initialize();
		setLabel("ICD Tab");
		categoryIcon = ComponentUtilities.loadImageIcon(ICDTab.class, "resources/icd.png");
		setIcon(categoryIcon);
		adjustButtons();


		final Cls icd_metaclass = getKnowledgeBase().getCls(ICDConstants.ICD_METACLASS);
		getClsTree().setCellRenderer(new FrameRenderer() {
			private static final long serialVersionUID = 6030106662549691960L;

            @Override
			protected void loadCls(Cls cls) {
				super.loadCls(cls);
				if (cls.hasType(icd_metaclass)) {
					setMainIcon(categoryIcon);
				}
			}
		});
	}

	 protected void adjustButtons() {
		 List<Action> actions = new ArrayList<Action>(getLabeledComponent().getHeaderButtonActions());
		 getLabeledComponent().removeAllHeaderButtons();
		 actions.add(2, getCreateClsAction());
		 for (Action action : actions) {
			getLabeledComponent().addHeaderButton(action);
		}

	}

	@Override
	protected ClsesPanel createClsesPanel() {
		 clsesPanel = super.createClsesPanel();
		 return clsesPanel;
	 }


	 protected AllowableAction getCreateClsAction() {
	        return new AllowableAction("Create ICD category", categoryIcon, clsesPanel.getSelectable()) {
				private static final long serialVersionUID = 4823094553153085933L;

                public void actionPerformed(ActionEvent e) {
					 final Collection parents = clsesPanel.getSubclassPane().getSelection();
		                if (!parents.isEmpty()) {
		                    Transaction<Cls> t = new Transaction<Cls>(getKnowledgeBase(), "Create ICD Category (random name)") {
		                        private Cls cls;

		                        @Override
		                        public boolean doOperations() {
		                            cls = getKnowledgeBase().createCls(null, parents);
		                            cls.setDirectTypes(getTypes());
		                            return true;
		                        }

		                        @Override
								public Cls getResult() {
		                            return cls;
		                        }
		                    };
		                    t.execute();
		                    Cls cls = t.getResult();
		                    clsesPanel.getSubclassPane().extendSelection(cls);
		                }

				}
	        };
	    }


	 protected Collection<Cls> getTypes() {
         ArrayList<Cls> types = new ArrayList<Cls>();
         addType(types, ICDConstants.ICD_METACLASS_METADATA);
         addType(types, ICDConstants.ICD_METACLASS_FORMAL_REPR);
         addType(types, ICDConstants.ICD_METACLASS_DIAGNOSTIC_CRITERIA);
         addType(types, ICDConstants.ICD_METACLASS_ETIOLOGY);
         addType(types, ICDConstants.ICD_METACLASS_FUNCTIONAL_IMPACT);
         addType(types, ICDConstants.ICD_METACLASS);
         return types;
	 }

	 protected void addType(Collection<Cls> types, String typeName) {
		 Cls type = getKnowledgeBase().getCls(typeName);
		if (type != null) {
			 types.add(type);
		 }
	 }


	public static boolean isSuitable(Project p, Collection errors) {
		KnowledgeBase kb = p.getKnowledgeBase();
		boolean ok = kb.getCls(ICDConstants.ICD_METACLASS) != null;
		if (!ok) { errors.add("ICD metaclass is missing"); }
		return ok;
	}

}
