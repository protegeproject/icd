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

    public void setUp(){
        File file = new File(outputFileName);
        if(file.exists()){
            file.delete();
        }
    }

    public void testConvertsLongDefinition() throws Exception {
        final Project project = Project.loadProjectFromFile("pprj/icd_umbrella.pprj", new ArrayList());
        ExportScriptWrapper unit = new ExportScriptWrapper(project, "resources/export_script.py");
        String topNode = "http://who.int/icd#Class_2554";
        unit.exportToFile(outputFileName, topNode);
        CsvReader reader = new CsvReader(outputFileName, 1);
        reader.nextRow();
         Map<String,String> map = reader.getNamedColumnsAsMap();
        assertEquals(topNode, map.get("Class name (Internal)"));
        assertTrue(map.get("Textual Definition").trim().length() > 0);
        assertTrue(map.get("Detailed Definition").trim().length() == 0);
        topNode = "http://who.int/icd#L24.4";
        unit.exportToFile(outputFileName, topNode);
        reader = new CsvReader(outputFileName, 1);
        reader.nextRow();
        map = reader.getNamedColumnsAsMap();
        assertEquals(topNode, map.get("Class name (Internal)"));
        assertEquals("this value added for unit testing - has it been removed from the ontology?", "my long definition", map.get("Detailed Definition"));
        assertTrue(map.get("Textual Definition").trim().length() == 0);
    }
}