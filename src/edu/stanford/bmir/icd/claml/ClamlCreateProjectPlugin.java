package edu.stanford.bmir.icd.claml;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.Application;
import edu.stanford.smi.protege.exception.AmalgamatedLoadException;
import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.AbstractCreateProjectPlugin;
import edu.stanford.smi.protege.plugin.CreateProjectWizard;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.FileField;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.PropertyList;
import edu.stanford.smi.protege.util.Wizard;
import edu.stanford.smi.protege.util.WizardPage;
import edu.stanford.smi.protegex.owl.jena.JenaKnowledgeBaseFactory;
import edu.stanford.smi.protegex.owl.jena.creator.NewOwlProjectCreator;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.util.ImportHelper;
import edu.stanford.smi.protegex.owl.repository.impl.LocalFolderRepository;

public class ClamlCreateProjectPlugin extends AbstractCreateProjectPlugin {

    private static final String PROJECTS_PATH = "/projects";
    private static final URI CLAML_CM_ONTOLOGY_NAME = URI.create("http://who.int/icd/contentModel");

    private File clamlFile;
    private String topCls;
    private String defaultNamespace;

    public ClamlCreateProjectPlugin() {
        super("CLAML Files");
    }

    public boolean canCreateProject(KnowledgeBaseFactory factory, boolean useExistingSources) {
        return factory.getClass().getName().contains(".owl.") && useExistingSources;
    }

    public WizardPage createCreateProjectWizardPage(CreateProjectWizard wizard, boolean useExistingSources) {
        WizardPage page = null;
        if (useExistingSources) {
            page = new ClamlFilesWizardPage(wizard, this);
        }
        return page;
    }

    public void setFile(File file) {
        clamlFile = file;
    }
    
    public void setTopCls(String topCls) {
    	this.topCls = topCls;
    }
    
	public void setDefaultNamespace(String ns) {
		if (defaultNamespace != null) {
			defaultNamespace = defaultNamespace.trim();
		} else {
			defaultNamespace = "";
		}
		this.defaultNamespace = ns;
	}

	
    @Override
    protected Project buildNewProject(KnowledgeBaseFactory factory) {
        Project project = createNewProject(factory);
        if (project != null) {
            try {
                importClamlCM((OWLModel) project.getKnowledgeBase());
                ClamlImport clamlImport = new ClamlImport((OWLModel) project.getKnowledgeBase());
                clamlImport.doImport(clamlFile, topCls, defaultNamespace);
            } catch (Exception ex) {
                Log.getLogger().log(Level.SEVERE, "Exception caught", ex);
                JOptionPane.showMessageDialog(Application.getMainWindow(), "Could not load " + clamlFile.getAbsolutePath() + "\n" + ex, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return project;
    }

    protected void importClamlCM(OWLModel owlModel) throws OntologyLoadException {
        owlModel.getNamespaceManager().setDefaultNamespace(ICDContentModelConstants.NS);
        URI uri = getClamlDir();
        if (uri != null) {
            LocalFolderRepository rep = new LocalFolderRepository(new File(uri));
            owlModel.getRepositoryManager().addProjectRepository(rep);
        }

        ImportHelper ih = new ImportHelper(owlModel);
        ih.addImport(CLAML_CM_ONTOLOGY_NAME);
        ih.importOntologies();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Project createNewProject(KnowledgeBaseFactory factory) {
        Collection errors = new ArrayList();
        NewOwlProjectCreator creator = new NewOwlProjectCreator((JenaKnowledgeBaseFactory) factory);
        creator.setOntologyName("http://test.owl");
        try {
            creator.create(errors);
        } catch (AmalgamatedLoadException ale) {
            errors.addAll(ale.getErrorList());
        } catch (OntologyLoadException ole) {
            errors.add(ole);
        } finally {
            handleErrors(errors);
        }
        return creator.getProject();
    }

    @Override
    protected void initializeSources(PropertyList sources) {
    }

    protected URI getClamlDir() {
        URI clamlURI = null;
        try {
            clamlURI = ClamlCreateProjectPlugin.class.getResource(PROJECTS_PATH).toURI();
        } catch (Throwable e) {
            Log.getLogger().log(Level.WARNING, "Could not find CLAML CM ontology in the CLAML plugin directory.", e);
        }
        return clamlURI;
    }

    class ClamlFilesWizardPage extends WizardPage {
        static final long serialVersionUID = -6533244348208834092L;

        private ClamlCreateProjectPlugin plugin;
        private FileField fileField;
        private JTextField defaultPrefixField;
        private JTextField topClsField;

        ClamlFilesWizardPage(Wizard wizard, ClamlCreateProjectPlugin plugin) {
            super("CLAML files", wizard);
            this.plugin = plugin;
            
            fileField = new FileField("CLAML File", null, ".xml", "CLAML Files");
            defaultPrefixField = ComponentFactory.createTextField();
            topClsField = ComponentFactory.createTextField();
            topClsField.setText("http://www.w3.org/2002/07/owl#Thing");
            
            Box panel = Box.createVerticalBox();
            panel.add(fileField);
            panel.add(new LabeledComponent("Default namespace to add to imported classes (e.g., http://who.int/icf#) - Optional", defaultPrefixField));
            panel.add(new LabeledComponent("Top class name (class to import under) - Optional", topClsField));
            
            setLayout(new BorderLayout());
            add(panel);
            
            setPageComplete(false);
            
            fileField.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    updatePageComplete();
                }
            });
        }

        private void updatePageComplete() {
            File file = fileField.getFilePath();
            setPageComplete(file != null && file.isFile());
        }

        @Override
        public void onFinish() {
            plugin.setFile(fileField.getFilePath());
            plugin.setTopCls(topClsField.getText());
            plugin.setDefaultNamespace(defaultPrefixField.getText());
        }
    }


}
