package edu.stanford.bmir.protege.icd.export.script;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all the logic specific to the ICD Conversion from Csv to Excel.
 * <p/>
 * Delegates the actual work for the importation down to the JxlCsvToExcelConverter.
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDCsvToExcelConverter implements CsvToExcelConverter {
    private CsvToExcelConverter converter;

    public void importFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName) throws IOException, BiffException, WriteException {
        converter = new JxlCsvToExcelConverter(2, 1, columnValueMapper(), 0, 1);
        converter.importFile(csvLocation, inputWorkbookLocation, outputWorkbookLocation, sheetName);
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
