package edu.stanford.bmir.protege.icd.export.script;

import edu.stanford.smi.protege.model.Project;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;

/**
 * Wraps the actual script that we use for ICD export.
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ExportScriptWrapper {
    Project project;
    String scriptFileLocation;
    protected static final String JYTHON_ENGINE_CLASSNAME = "org.apache.bsf.engines.jython.JythonEngine";

    static {
        BSFManager.registerScriptingEngine("python", JYTHON_ENGINE_CLASSNAME, new String[]{"py"});
    }

    public ExportScriptWrapper(Project project, String scriptFileLocation) {
        this.project = project;
        this.scriptFileLocation = scriptFileLocation;
    }

    public void exportToFile(String outputCsvFile, String... topNodes) throws BSFException, IOException {
        if (topNodes == null || topNodes.length == 0) {
            throw new IllegalArgumentException("Must supply at least one top node.");
        }
        File file = new File(outputCsvFile);
        final File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        BSFManager manager = new BSFManager();
        try {
            manager.declareBean("kb", project.getKnowledgeBase(), project.getKnowledgeBase().getClass());
            final String langFromFilename = BSFManager.getLangFromFilename(scriptFileLocation);
            final String script = loadScript(scriptFileLocation);
            manager.exec(langFromFilename, scriptFileLocation, 1, 1, script);
            StringBuffer topNodesAsString = new StringBuffer();
            for (int i = 0; i < topNodes.length; i++) {
                if (i > 0) {
                    topNodesAsString.append(",");
                }
                topNodesAsString.append("\"" + topNodes[i] + "\"");
            }
            final String fragment = MessageFormat.format("topNodes = [{1}]\n" +
                    "startICDExport(topNodes, \"{0}\");\n", outputCsvFile, topNodesAsString.toString());
            manager.exec(langFromFilename, scriptFileLocation, 1, 1, fragment);
        } finally {
            manager.terminate();
        }
    }

    private String loadScript(String fileName) throws IOException {
        File file = new File(fileName);
        Reader reader = null;
        if (!file.exists()) {
            reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName));
        } else {
            reader = new FileReader(file);
        }
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuffer stringBuffer = new StringBuffer();
        while (bufferedReader.ready()) {
            stringBuffer.append(bufferedReader.readLine());
            stringBuffer.append("\n");
        }
        return stringBuffer.toString();
    }
}
