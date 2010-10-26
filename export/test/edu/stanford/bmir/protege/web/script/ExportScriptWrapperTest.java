package edu.stanford.bmir.protege.web.script;
/**
 * @author Jack Elliott <jacke@stanford.edu>
 */

import edu.stanford.smi.protege.model.Project;
import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ExportScriptWrapperTest extends TestCase {
    private final String outputFileName = "output/myunit.csv";

    public void setUp() {
        File file = new File(outputFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public void testConvertsShortDefinitionGreaterThan100WordsIntoDetailedDefinition() throws Exception {
        final Project project = Project.loadProjectFromFile("pprj/icd_umbrella.pprj", new ArrayList());
        ExportScriptWrapper unit = new ExportScriptWrapper(project, "resources/export_script.py");
        String topNode = "http://who.int/icd#Class_2554";
        unit.exportToFile(outputFileName, topNode);
        CsvReader reader = new CsvReader(outputFileName, 1);
        reader.nextRow();
        Map<String, String> map = reader.getNamedColumnsAsMap();
        assertEquals(topNode, map.get("Class name (Internal)"));
        assertTrue(map.get("Detailed Definition").trim().length() > 100);
        assertTrue(map.get("Textual Definition").trim().length() == 0);
    }
}