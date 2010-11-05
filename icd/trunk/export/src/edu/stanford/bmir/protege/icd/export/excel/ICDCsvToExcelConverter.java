package edu.stanford.bmir.protege.icd.export.excel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds all the logic specific to the ICD Conversion from Csv to Excel.
 * <p/>
 * Delegates the actual work for the importation down to the JxlCsvToExcelConverter.
 * <p/>
 * This allows us to code the 'data' part of the ICD Export here, rather than cluttering up the JxlCsvToExcelConverter.
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDCsvToExcelConverter implements CsvToExcelConverter {
    protected static final int TIMESTAMP_ROW = 0;
    protected static final int TIMESTAMP_COLUMN = 1;
    protected static final int CSV_TITLE_ROW = 1;
    protected static final int EXCEL_TITLE_ROW = 2;
    protected static final int CLASS_FIELD_POSITION = 32;
    protected static final Set<Integer> FIELDS_NOT_TO_COLOR = new HashSet<Integer>(Arrays.asList(0, 1));

    public void convertFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName) {
        CsvToExcelConverter converter = new JxlCsvToExcelConverter(EXCEL_TITLE_ROW, CSV_TITLE_ROW, TIMESTAMP_ROW, TIMESTAMP_COLUMN, CLASS_FIELD_POSITION, FIELDS_NOT_TO_COLOR);
        converter.convertFile(csvLocation, inputWorkbookLocation, outputWorkbookLocation, sheetName);
    }

}
