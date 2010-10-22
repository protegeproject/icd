package edu.stanford.bmir.protege.web.script;

import edu.stanford.smi.protege.model.Project;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Wraps the actual script that we use for ICD export.
 * 
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ExportScriptWrapper {
    Project project;
    String scriptFileLocation;

    public ExportScriptWrapper(Project project, String scriptFileLocation) {
        this.project = project;
        this.scriptFileLocation = scriptFileLocation;
    }

    public void exportToFile(String outputCsvFile, String topNode) throws BSFException, IOException {
        BSFManager.registerScriptingEngine("python", "org.apache.bsf.engines.jython.JythonEngine", new String[]{"py"});
        BSFManager manager = new BSFManager();
        manager.declareBean("kb", project.getKnowledgeBase(), project.getKnowledgeBase().getClass());
        final String langFromFilename = BSFManager.getLangFromFilename(scriptFileLocation);
        final String script = loadScript(scriptFileLocation);
        manager.exec(langFromFilename, scriptFileLocation, 1, 1, script);
        final String fragment = MessageFormat.format("topNodes = [\"{1}\"]\n" +
                "startICDExport(topNodes, \"{0}\");\n", outputCsvFile, topNode);
        manager.exec(langFromFilename, scriptFileLocation, 1,1, fragment);
    }

    private String loadScript(String fileName) throws IOException {
        File file = new File(fileName);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuffer stringBuffer = new StringBuffer();
        while(bufferedReader.ready()){
            stringBuffer.append(bufferedReader.readLine());
            stringBuffer.append("\n");
        }
        return stringBuffer.toString();
    }
}
