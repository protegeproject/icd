package edu.stanford.bmir.protege.web.script;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all the logic specific to the ICD Import.
 *
 * Delegates the actual work for the importation down to the JxlImporter.
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDImporter implements ExcelImporter {
    private ExcelImporter importer;

    public void importFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName) throws IOException, BiffException, WriteException {
        importer = new JxlImporter(csvRowNameToColumnNameMap(), 2, 1, columnValueMapper());
        importer.importFile(csvLocation, inputWorkbookLocation, outputWorkbookLocation, sheetName);
    }

    private static Map<String, String> csvRowNameToColumnNameMap() {

        Map<String, String> map = new HashMap<String, String>();

        map.put("Fully specified title", "Fully specified name");
        map.put("Sort label", "Sorting label");
        map.put("Type", "Concept type");
        map.put("Original Parent", "Original parents");
        map.put("ICD code", "ICD-10 code");
        map.put("Textual Definition", "Definition (plus citation in square brackets [ ] at end)");
        //TODO: change this value to map from the newly populated 
        map.put("Detailed Definition", "Detailed Definition");
        map.put("Synonyms", "Synonyms (exact equivalents)");
        map.put("Legacy Inclusion Terms(For information only)", "Current inclusions (from iCAT) ");
        map.put("Legacy Exclusion Terms (For information only)", "Current Exclusions (from iCAT)");
        map.put("Does laterality apply?", "Laterality applicable?");
        map.put("Body System", "Body system ");
        map.put("Body Part", "Body Part code");
        map.put("Body Part SNOMEDCT Code", "Body Part (SNOMED CT)");
        map.put("Histopathology", "Histopathology code");
        map.put("Histopathology SNOMEDCT Code", "Histopathology (SNOMED CT)");
        map.put("Temporal Modifier", "Temporal modifier (Y/N)?");
        map.put("Temporal Modifier Definition", "Temporal modifier definition");
        map.put("Severity Term", "Severity qualifier (Y/N)?");
        map.put("Severity Term Definition", "Severity qualifier definition");
        map.put("Authoring Note to be imported into iCAT", "Note for import into iCAT");
        map.put("Class name (Internal)", "Class name (internal)");
        map.put("Retired", "RETIRE ??");
        map.put("ICD category", "Concept title");

        return map;
    }

    private static Map<String, Map<String, String>> columnValueMapper() {

        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put("RETIRE ??", new HashMap<String, String>());
        map.put("Body system ", new HashMap<String, String>());
        map.get("Body system ").put("Skin and subcutaneous tissue", "Skin System (Integumentary System)");
        map.get("Body system ").put("Skin System (Integumentary System)", "Skin System (Integumentary System)");

        return map;
    }

    public Integer getColumnNumber(String name){
        if (importer == null ){
            throw new IllegalStateException("columns not yet mapped");
        }
        return importer.getColumnNumber(name);
    }

}
