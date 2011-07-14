package edu.stanford.bmir.protegex.icd.diff;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ClsesPanel;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ExtensionFilter;
import edu.stanford.smi.protege.util.FileField;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ModalDialog;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class TreeComponent extends JPanel {
	
	private static final long serialVersionUID = -7471727828011399326L;

	private KnowledgeBase kb;
	
	private LabeledComponent bogusPanel;
	private FileField fileField;

	private ClsesPanel clsesPanel;

	private Map<Cls, FrameStatus> cls2Status;

	public TreeComponent(Map<Cls, FrameStatus> cls2Status) {
		this.cls2Status = cls2Status;
		buildUI();
	}

	private void buildUI() {
		setLayout(new BorderLayout());
		
		java.util.List<String> extensions = (java.util.List<String>) Arrays.asList((new String[]{"pprj", "owl" , "rdfs", "rdf"}));
		fileField = new FileField("Select file", null, new ExtensionFilter(extensions.iterator(), "Files"));
		JPanel filePanel = new JPanel(new BorderLayout());
		filePanel.add(fileField, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		JButton button = new JButton(getLoadAction());
		button.setAlignmentY(Component.BOTTOM_ALIGNMENT);		
		buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(button);		
		filePanel.add(buttonPanel, BorderLayout.EAST);
		add(filePanel, BorderLayout.NORTH);
		bogusPanel = new LabeledComponent(null, new Panel(), true);
		add(bogusPanel, BorderLayout.CENTER);	
	}
	
	
	protected JComponent createTreeComponent() {
		if (kb == null) { return null;}
		Collection rootClasses = CollectionUtilities.createCollection(kb.getRootCls());
		makeSystemClassesHidden(kb);		
		clsesPanel = new ClsesPanel(kb.getProject());	
		clsesPanel.setRenderer(new ChangedFrameRenderer(cls2Status));
		return clsesPanel;	
	}
	
	
	@SuppressWarnings({"deprecation" })
	private void makeSystemClassesHidden(KnowledgeBase kb) {
		Collection systemFrames = new HashSet(kb.getSystemFrames().getFrames());
				
		for (Iterator iterator = systemFrames.iterator(); iterator.hasNext();) {
			Frame frame = (Frame) iterator.next();
			frame.setVisible(false);
		}
		kb.getRootCls().setVisible(true);
		kb.getProject().setDisplayHiddenClasses(false);		
	}
	
	
	
	protected Action getLoadAction() {
		return new AbstractAction("Load") {
			private static final long serialVersionUID = 8541247235917469361L;

            public void actionPerformed(ActionEvent arg0) {
				if (fileField.getPath() == null) {
					ModalDialog.showMessageDialog(TreeComponent.this, "Please select a file first, and then click Load");
					return;
				}
				kb = loadKnowledgeBase(fileField.getFilePath().toURI());
				if (kb == null) {
					ModalDialog.showMessageDialog(TreeComponent.this, "Could not load file. Check console for details.");
					return;
				}
				JComponent treeComp = createTreeComponent();
				if (treeComp == null) {	return;	}
				bogusPanel.setCenterComponent(treeComp);
				//bogusPanel.add(treeComp);
				bogusPanel.revalidate();
				bogusPanel.repaint();				
			}			
		};
	}
	
	
	private KnowledgeBase loadKnowledgeBase(URI uri) {
		if (kb != null) {
			try {
				kb.getProject().dispose();
			} catch (Exception e) {
				Log.getLogger().log(Level.WARNING, "Errors at disposing " + kb, e);
			}
		}
		kb = null;
		String uriStr = uri.toString();
		if (uriStr.endsWith(".owl") || uriStr.endsWith(".rdf") || uriStr.endsWith(".rdfs")) {
			return loadOwlModel(uri);
		} else if (uriStr.endsWith(".pprj")) {
			return loadProject(uri);
		} 
		return null;
	}
	
	private KnowledgeBase loadProject(URI uri) {
		ArrayList errors = new ArrayList();
		Project project = null;
		try {
			project = Project.loadProjectFromURI(uri, errors);	
		} catch (Exception e) {
			Log.getLogger().log(Level.SEVERE, "Errors at loading " + uri, e);
		}
		
		if (errors.size() > 0) {
			ProjectManager.getProjectManager().displayErrors("Errors at loading project " + uri, errors);
		}
		
		return project == null ? null : project.getKnowledgeBase();
	}

	private OWLModel loadOwlModel(URI uri) {
		OWLModel owlModel = null;
		try {
			owlModel = ProtegeOWL.createJenaOWLModelFromURI(uri.toURL().toString());
		} catch (Exception e) {
			Log.getLogger().log(Level.WARNING, "Error at loading ontology from: " + uri, e);
		}
		
		return owlModel;
	}
	
	public KnowledgeBase getKb() {
		return kb;
	}
	
	public ClsesPanel getClsesPanel() {
		return clsesPanel;
	}
	
}
