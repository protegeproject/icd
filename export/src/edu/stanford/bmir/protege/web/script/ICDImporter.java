package edu.stanford.bmir.protege.web.script;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all the logic specific to the ICD Import.
 * <p/>
 * Delegates the actual work for the importation down to the JxlImporter.
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDImporter implements ExcelImporter {
    private ExcelImporter importer;

    public void importFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName) throws IOException, BiffException, WriteException {
        importer = new JxlImporter(2, 1, columnValueMapper(), 0, 1);
        importer.importFile(csvLocation, inputWorkbookLocation, outputWorkbookLocation, sheetName);
    }

    /**
     * This generates the mapping from column number to input value to desired write value.
     * <p/>
     * Note that, due to some late-breaking changes in requirements, this functionality is no longer needed, so here
     * we simply return an empty map.
     *
     * @return
     */
    private static Map<String, Map<String, String>> columnValueMapper() {
        return new HashMap<String, Map<String, String>>();
    }

}
