package edu.stanford.bmir.protege.icd.export.excel;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public interface CsvToExcelConverter {
    void convertFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName);
}
