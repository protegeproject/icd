package edu.stanford.bmir.protege.icd.export.script;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
    protected static final int TIMESTAMP_ROW = 0;
    protected static final int TIMESTAMP_COLUMN = 1;
    protected static final int CSV_TITLE_ROW = 1;
    protected static final int EXCEL_TITLE_ROW = 2;
    protected static final int CLASS_FIELD_POSITION = 32;

    public void convertFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName) throws IOException, BiffException, WriteException {
        final HashSet<Integer> fieldsNotToColor = new HashSet<Integer>();
        fieldsNotToColor.add(0);
        fieldsNotToColor.add(1);
        converter = new JxlCsvToExcelConverter(EXCEL_TITLE_ROW, CSV_TITLE_ROW, columnValueMapper(), TIMESTAMP_ROW, TIMESTAMP_COLUMN, CLASS_FIELD_POSITION, fieldsNotToColor);
        converter.convertFile(csvLocation, inputWorkbookLocation, outputWorkbookLocation, sheetName);
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
