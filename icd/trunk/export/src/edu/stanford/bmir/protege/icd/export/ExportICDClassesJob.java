package edu.stanford.bmir.protege.icd.export;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.protege.icd.export.excel.ICDCsvToExcelConverter;
import edu.stanford.bmir.protege.icd.export.script.ExportScriptWrapper;
import edu.stanford.bmir.protege.icd.export.ui.ICDExporterPlugin;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ExportICDClassesJob extends ProtegeJob {
    private static final long serialVersionUID = 818316286562363577L;
    private static final Logger logger = Log.getLogger(ExportICDClassesJob.class);
    private String outputFileLocation;
    private final String[] topNodes;

    private static final String CSV_FILE_EXTENSION = ".csv";
    protected static final String EXCEL_FILE_EXTENSION = ".xls";

    public ExportICDClassesJob(KnowledgeBase kb, String outputFileLocation, String... topNodes) {
        super(kb);
        this.outputFileLocation = outputFileLocation;
        this.topNodes = topNodes;
    }

    @Override
    public Object run() throws ProtegeException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Beginning ExportICDClassesJob...");
        }
        performValidation();

        initializePython();

        final String csvLocation = getCsvLocation(outputFileLocation);

        generateCsvFile(csvLocation);

        generateExcelFile(csvLocation, outputFileLocation);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finishing ExportICDClassesJob.");
        }
        return new File(outputFileLocation).getAbsolutePath();
    }

    private void performValidation() {
        try {
            if (outputFileLocation == null || !outputFileLocation.endsWith(EXCEL_FILE_EXTENSION)) {
                throw new IllegalArgumentException("The output file location must be set to a non-null value and must have a " + EXCEL_FILE_EXTENSION + " extension.");
            }
            if (topNodes == null || topNodes.length == 0) {
                throw new IllegalArgumentException("Must supply at least one top node.");
            }
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Caught when validating arguments", e);
            throw e;
        }
    }

    private void initializePython() {
        if (ApplicationProperties.getApplicationOrSystemProperty(PropertyConstants.PYTHON_HOME_PROPERTY) == null) {
            final File file = PluginUtilities.getInstallationDirectory(ICDExporterPlugin.class.getName());
            if (file == null || !file.exists()) {
                throw new IllegalArgumentException("Please set " + PropertyConstants.PYTHON_HOME_PROPERTY + " as a system or application property pointing to your Jython install directory or install the ICD Export functionality as a ui plugin.");
            }
            System.setProperty(PropertyConstants.PYTHON_HOME_PROPERTY, file.getPath());
        }
        setSystemPropertyIfUnset(PropertyConstants.PYTHON_INTERNAL_TABLES_OPTION_PROPERTY, PropertyConstants.PYTHON_INTERNAL_TABLES_OPTION_DEFAULT);
    }

    private String getCsvLocation(String outputFileLocation) {
        return outputFileLocation.substring(0, this.outputFileLocation.length() - EXCEL_FILE_EXTENSION.length()) + CSV_FILE_EXTENSION;
    }

    private void generateCsvFile(final String csvLocation) {
        final String scriptFileLocation = ApplicationProperties.getApplicationOrSystemProperty(PropertyConstants.ICD_EXPORT_SCRIPT_FILE_NAME_PROPERTY, PropertyConstants.ICD_EXPORT_SCRIPT_FILE_NAME_DEFAULT);
        try {
            // Only need to synchronize around access to the ExportScriptWrapper, as this is where all the KB access happens.
            synchronized (getKnowledgeBase()) {
                ExportScriptWrapper wrapper = new ExportScriptWrapper(getKnowledgeBase().getProject(), scriptFileLocation);
                wrapper.exportToFile(csvLocation, topNodes);
            }
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Caught when trying to generate a csv file for " + Arrays.asList(topNodes) + " at "
                    + csvLocation + " using script file " + scriptFileLocation, e);
            throw e;
        }
    }

    private void generateExcelFile(String csvLocation, final String outputFileLocation) {
        try {
            final String inputWorkbookLocation = ApplicationProperties.getApplicationOrSystemProperty(PropertyConstants.ICD_EXPORT_EXCEL_FILE_NAME_PROPERTY, PropertyConstants.ICD_EXPORT_EXCEL_FILE_NAME_LOCATION);
            setSystemPropertyIfUnset(PropertyConstants.JXL_NOWARNINGS_PROPERTY, PropertyConstants.JXL_NOWARNINGS_DEFAULT);
            ICDCsvToExcelConverter csvToExcelConverter = new ICDCsvToExcelConverter();
            csvToExcelConverter.convertFile(csvLocation, inputWorkbookLocation, outputFileLocation, "Authoring template");
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Caught when trying to generate an excel file for " + Arrays.asList(topNodes) + " at " + outputFileLocation, e);
            throw e;
        }
    }

    private void setSystemPropertyIfUnset(final String propertyName, final String propertyValue) {
        if (System.getProperty(propertyName) == null) {
            System.getProperties().put(propertyName, propertyValue);
        }
    }
}
