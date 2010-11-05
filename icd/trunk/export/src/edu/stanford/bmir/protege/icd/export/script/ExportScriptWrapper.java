package edu.stanford.bmir.protege.icd.export.script;

import edu.stanford.bmir.protege.icd.export.FileUtils;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.engines.jython.JythonEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps the actual script that we use for ICD export.
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ExportScriptWrapper {
    private Project project;
    private String scriptFileLocation;
    private static final String ASSERTION_ERROR_PARTIAL_MESSAGE = "AssertionError: Could not export";
    private static final Logger logger = Log.getLogger(ExportScriptWrapper.class);

    static {
        BSFManager.registerScriptingEngine("python", JythonEngine.class.getName(), new String[]{"py"});
    }

    public ExportScriptWrapper(Project project, String scriptFileLocation) {
        this.project = project;
        this.scriptFileLocation = scriptFileLocation;
    }

    public void exportToFile(String outputCsvFile, String... topNodes) {
        File file = new File(outputCsvFile);
        final File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                throw new ProtegeException("Could not create directory " + parentFile);
            }
        }
        BSFManager manager = new BSFManager();
        try {
            manager.declareBean("kb", project.getKnowledgeBase(), project.getKnowledgeBase().getClass());
            final String langFromFilename = BSFManager.getLangFromFilename(scriptFileLocation);
            final String script = loadScript(scriptFileLocation);
            manager.exec(langFromFilename, scriptFileLocation, 1, 1, script);
            final String quotedStrings = generateTopNodesAsQuotedStrings(topNodes);
            final String fragment = MessageFormat.format("topNodes = [{1}]\n" +
                    "startICDExport(topNodes, \"{0}\");\n", outputCsvFile, quotedStrings);
            manager.exec(langFromFilename, scriptFileLocation, 1, 1, fragment);
        } catch (BSFException e) {
            // following is to handle the 'ugly' exceptions we see from BSF. we really just want to get the Assertion Failures
            final String message = e.getMessage();
            if (message != null && message.contains(ASSERTION_ERROR_PARTIAL_MESSAGE)) {
                final int i = message.lastIndexOf(ASSERTION_ERROR_PARTIAL_MESSAGE);
                throw new ProtegeException(message.substring(i).trim());
            }
            throw new ProtegeException("Error when using script at " + scriptFileLocation + " to export nodes " + Arrays.asList(topNodes) + " for project " + project.getName(), e);
        } finally {
            manager.terminate();
        }
    }

    private String generateTopNodesAsQuotedStrings(String[] topNodes) {
        StringBuffer topNodesAsString = new StringBuffer();
        for (int i = 0; i < topNodes.length; i++) {
            if (i > 0) {
                topNodesAsString.append(",");
            }
            topNodesAsString.append("\"");
            topNodesAsString.append(topNodes[i]);
            topNodesAsString.append("\"");
        }
        return topNodesAsString.toString();
    }

    private String loadScript(String fileName) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(FileUtils.getInputStream(fileName)));

            StringBuffer stringBuffer = new StringBuffer();
            while (bufferedReader.ready()) {
                stringBuffer.append(bufferedReader.readLine());
                stringBuffer.append("\n");
            }
            return stringBuffer.toString();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not load script from file " + fileName);
        } catch (IOException e) {
            throw new ProtegeException("Error when reading script from file " + fileName, e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Caught error when trying to close BufferedReader for file " + fileName, e);
            }
        }
    }
}
