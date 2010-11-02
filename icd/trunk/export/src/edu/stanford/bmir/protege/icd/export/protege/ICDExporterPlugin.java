package edu.stanford.bmir.protege.icd.export.protege;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.ExportPlugin;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ModalDialog;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDExporterPlugin implements ExportPlugin {
    private static final String EXTENSION = ".xls";
    private static final String TOP_LEVEL_ICD_CLASS_PROPERTY = "top.level.icd.class";

    public String getName() {
        return "ICD Excel Export";
    }

    public void handleExportRequest(final Project project) {
        String topLevelICDClass = "http://who.int/icd#ICDCategory";
        topLevelICDClass = ApplicationProperties.getApplicationOrSystemProperty(TOP_LEVEL_ICD_CLASS_PROPERTY, topLevelICDClass);
        final List rootClasses = Arrays.asList(project.getKnowledgeBase().getCls(topLevelICDClass));
        final JComponent mainPanel = ProjectManager.getProjectManager().getMainPanel();
        final Cls clsToExport = DisplayUtilities.pickCls(mainPanel, project.getKnowledgeBase(), rootClasses);
        final File file = promptForExcelFile(project);

        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                String prettyClassName = clsToExport.getBrowserText();
                prettyClassName = prettyClassName == null ? clsToExport.getName() : prettyClassName;
                try {
                    new ExportICDClassesJob(project.getKnowledgeBase(), file.getAbsolutePath(), clsToExport.getName()).execute();
                    ModalDialog.showMessageDialog(mainPanel, "Successfully exported branch " + prettyClassName + " to file " + file.getAbsolutePath());
                    return null;
                } catch (ProtegeException e) {
                    ModalDialog.showMessageDialog(mainPanel, "Could not export " + prettyClassName + " to file " + file.getAbsolutePath() + "\nProblem was " + e.getMessage());
                    throw e;
                }
            }
        };
        worker.execute();

    }

    public void dispose() {
        // do nothing
    }

    private File promptForExcelFile(Project project) {
        String name = project.getName();
        String proposedName = new File(name + EXTENSION).getPath();
        JFileChooser chooser = ComponentFactory.createFileChooser(proposedName, EXTENSION);
        File file = null;
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
        }
        return file;
    }
}
