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

    private static Map<String, Map<String, String>> columnValueMapper() {

        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put("RETIRE ??", new HashMap<String, String>());
        map.put("Body system ", new HashMap<String, String>());
        map.get("Body system ").put("Skin and subcutaneous tissue", "Skin System (Integumentary System)");
        map.get("Body system ").put("Skin System (Integumentary System)", "Skin System (Integumentary System)");

        return map;
    }

}
