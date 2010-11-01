package edu.stanford.bmir.protege.icd.export.server;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.ExportPlugin;
import edu.stanford.smi.protege.util.ComponentFactory;

import javax.swing.*;
import java.io.File;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDExporterPlugin implements ExportPlugin {
    private static final String EXTENSION = ".xls";

    public String getName() {
        return "ICDExporterPlugin";
    }

    public void handleExportRequest(Project project) {
        File file = promptForExcelFile(project);

        new ExportICDClassesJob(project.getKnowledgeBase(), "", file.getAbsolutePath()).execute();

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
