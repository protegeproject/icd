package edu.stanford.bmir.protege.icd.export.protege;

import edu.stanford.bmir.protege.icd.export.script.ExportScriptWrapper;
import edu.stanford.bmir.protege.icd.export.script.ICDCsvToExcelConverter;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ExportICDClassesJob extends ProtegeJob {
    private static final long serialVersionUID = 818316286562363577L;
    private static final Logger logger = Log.getLogger(ExportICDClassesJob.class);
    private String outputFileLocation;
    private final String[] topNodes;

    static final String PYTHON_HOME_PROPERTY = "python.home";
    private static final String JXL_NOWARNINGS_PROPERTY = "jxl.nowarnings";
    private static final String ICD_EXPORT_SCRIPT_FILE_NAME_PROPERTY = "icd.export.script.file.name";
    private static final String ICD_EXPORT_EXCEL_FILE_NAME_PROPERTY = "icd.export.excel.file.name";
    private static final String CSV_FILE_EXTENSION = ".csv";

    private static final String EXPORT_SCRIPT_DEFAULT_LOCATION = "export_script.py";
    private static final String TEMPLATE_XLS_FILE_DEFAULT_LOCATION = "/template.xls";

    public ExportICDClassesJob(KnowledgeBase kb, String outputFileLocation, String... topNodes) {
        super(kb);
        this.outputFileLocation = outputFileLocation;
        this.topNodes = topNodes;
    }

    @Override
    public Object run() throws ProtegeException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Executing script on the protege.");
        }
        final File file = PluginUtilities.getInstallationDirectory(ICDExporterPlugin.class.getName());
        if (file != null) {
            setSystemPropertyIfUnset(PYTHON_HOME_PROPERTY, file.getAbsolutePath());
        } else {
            setSystemPropertyIfUnset(PYTHON_HOME_PROPERTY, "");
        }
        setSystemPropertyIfUnset(JXL_NOWARNINGS_PROPERTY, Boolean.TRUE.toString());
        final String csvLocation = outputFileLocation + CSV_FILE_EXTENSION;
        final String scriptFileLocation = ApplicationProperties.getApplicationOrSystemProperty(ICD_EXPORT_SCRIPT_FILE_NAME_PROPERTY, EXPORT_SCRIPT_DEFAULT_LOCATION);
        final String inputWorkbookLocation = ApplicationProperties.getApplicationOrSystemProperty(ICD_EXPORT_EXCEL_FILE_NAME_PROPERTY, TEMPLATE_XLS_FILE_DEFAULT_LOCATION);
        // Only need to synchronize around access to the ExportScriptWrapper, as this is where all the KB access happens.
        synchronized (getKnowledgeBase()) {
            try {
                ExportScriptWrapper wrapper = new ExportScriptWrapper(getKnowledgeBase().getProject(), scriptFileLocation);
                wrapper.exportToFile(csvLocation, topNodes);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Caught when trying to generate a csv file for " + Arrays.asList(topNodes) + " at " + outputFileLocation + " using script file " + scriptFileLocation);
                throw new ProtegeException(e);
            }
        }
        try {
            ICDCsvToExcelConverter csvToExcelConverter = new ICDCsvToExcelConverter();
            csvToExcelConverter.convertFile(csvLocation, inputWorkbookLocation, outputFileLocation, "Authoring template");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Caught when trying to generate an excel file for " + Arrays.asList(topNodes) + " at " + outputFileLocation);
            throw new ProtegeException(e);
        }
        return new File(outputFileLocation).getAbsolutePath();
    }

    private void setSystemPropertyIfUnset(final String propertyName, final String propertyValue) {
        if (System.getProperty(propertyName) == null) {
            System.getProperties().put(propertyName, propertyValue);
        }
    }
}
