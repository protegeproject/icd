package edu.stanford.bmir.protege.web.script;
/**
 * @author Jack Elliott <jacke@stanford.edu>
 */

import edu.stanford.smi.protege.model.Project;

import java.io.File;
import java.util.ArrayList;

public class ExportScriptWrapperTest extends ValuesTest {
    private CsvReader reader;

    @Override
    protected String getFieldContents(final int fieldPosition, int rowNumber) {
        return reader.row[fieldPosition];
    }

    @Override
    protected void initializeTest(String topNode, final String csvFileName, String excelOutputFileName) throws Exception {
        File file = new File(csvFileName);
        if (file.exists()) {
            file.delete();
        }
        final Project project = Project.loadProjectFromFile("pprj/icd_umbrella.pprj", new ArrayList());
        ExportScriptWrapper unit = new ExportScriptWrapper(project, "resources/export_script.py");
        unit.exportToFile(csvFileName, topNode);
        reader = new CsvReader(csvFileName, 1);
        reader.nextRow();
    }


}