package edu.stanford.bmir.protege.icd.export.server;

import edu.stanford.bmir.protege.icd.export.script.ExportScriptWrapper;
import edu.stanford.bmir.protege.icd.export.script.ICDCsvToExcelConverter;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ExportICDClassesJob extends ProtegeJob {
    private static final long serialVersionUID = 818316286562363577L;
    private static final Logger logger = Log.getLogger(ExportICDClassesJob.class);
    private String outputFileLocation;
    private final String topNode;

    public ExportICDClassesJob(KnowledgeBase kb, String topNode, String outputFileLocation) {
        super(kb);
        this.outputFileLocation = outputFileLocation;
        this.topNode = topNode;
    }

    @Override
    public Object run() throws ProtegeException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Executing script on the server.");
        }
        setSystemPropertyIfUnset("python.home", "./plugins/edu.stanford.smi.protegex.icd.export");
        setSystemPropertyIfUnset("jxl.nowarnings", "true");
        final String csvLocation = outputFileLocation + ".csv";
        final String scriptFileLocation = ApplicationProperties.getApplicationOrSystemProperty("icd.export.script.file.name", "export_script.py");
        final String inputWorkbookLocation = ApplicationProperties.getApplicationOrSystemProperty("icd.export.excel.file.name", "/template.xls");
        // Only need to synchronize around access to the ExportScriptWrapper, as this is where all the KB access happens.
        synchronized (getKnowledgeBase()) {
            try {
                ExportScriptWrapper wrapper = new ExportScriptWrapper(getKnowledgeBase().getProject(), scriptFileLocation);
                wrapper.exportToFile(csvLocation, topNode);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Caught when trying to generate a csv file for " + topNode + " at " + outputFileLocation + " using script file " + scriptFileLocation);
                throw new ProtegeException(e);
            }
        }
        try {
            ICDCsvToExcelConverter csvToExcelConverter = new ICDCsvToExcelConverter();
            csvToExcelConverter.importFile(csvLocation, inputWorkbookLocation, outputFileLocation, "Authoring template");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Caught when trying to generate an excel file for " + topNode + " at " + outputFileLocation);
            throw new ProtegeException(e);
        }
        return null;
    }

    private void setSystemPropertyIfUnset(final String propertyName, final String propertyValue) {
        if (System.getProperty(propertyName) == null) {
            System.getProperties().put(propertyName, propertyValue);
        }
    }
}
