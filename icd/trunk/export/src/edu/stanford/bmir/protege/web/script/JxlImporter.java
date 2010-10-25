package edu.stanford.bmir.protege.web.script;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Does the actual heavily lifting of reading a csv file of an expected format into a spreadsheet.
 *
 * Column names are specified in the matrix passed in, as are the excel title row and the csv title row.
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class JxlImporter implements ExcelImporter{
    private static final org.apache.commons.logging.Log logger = LogFactory.getLog(JxlImporter.class);
    private Map<String, String> csvColumnNamesToExcelColumnNames;
    private Map<String, Map<String, String>> columnValuesMap = new HashMap<String, Map<String, String>>();
    private int excelTitleRow;
    private int csvTitleRow;
    private Map<String, Integer> excelTitleToColumnNumberMap;

    /**
     * The constructor for this class.
     *
     * @param csvColumnNamesToExcelColumnNames The map from csv columns to excel columns.
     * @param excelTitleRow  Note that this is array-based (0 is first number), not an excel-based (1 is first number) parameter
     * @param csvTitleRow    Note that this is an array-based (0 is first number), not an excel-based (1 is first number) parameter.
     * @param columnValuesMap The values used to map from ICD values to 'pretty' spreadsheet values. 
     * First Map is Excel column name to map, second map is from input value to 'pretty' spreadsheet value.
     */
    public JxlImporter(final Map<String, String> csvColumnNamesToExcelColumnNames, final int excelTitleRow, final int csvTitleRow, Map<String, Map<String, String>> columnValuesMap) {
        this.csvColumnNamesToExcelColumnNames = csvColumnNamesToExcelColumnNames;
        this.excelTitleRow = excelTitleRow;
        this.csvTitleRow = csvTitleRow;
        if (columnValuesMap != null){
            this.columnValuesMap = columnValuesMap;
        }
    }

    public void importFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName) throws IOException, BiffException, WriteException {
        final Workbook inputWorkbook = Workbook.getWorkbook(new File(inputWorkbookLocation));
        final WritableWorkbook outputWorkbook = Workbook.createWorkbook(new File(outputWorkbookLocation), inputWorkbook);
        int excelCurrentRowNumber = excelTitleRow + 1;
        final WritableSheet sheet = outputWorkbook.getSheet(sheetName);
        if (sheet == null){
            throw new IllegalArgumentException("Could not find sheet " + sheetName + " in spreadsheet file " + inputWorkbookLocation);
        }
        excelTitleToColumnNumberMap = mapExcelColumnNamesToColumnNumbers(sheet, excelTitleRow);
        CsvReader reader = new CsvReader(csvLocation, csvTitleRow);
        String nextValue = "";
        ErrorChecker checker = new ErrorChecker(reader.titles, excelTitleToColumnNumberMap.keySet(), csvColumnNamesToExcelColumnNames.keySet(), new HashSet<String>(csvColumnNamesToExcelColumnNames.values()), 9);
        if (checker.numberOfErrors()>0){
            throw new IllegalArgumentException("Can't proceed with data export due to data inconsistencies " + checker.generateReport());
        }
        if (checker.numberOfWarnings() > 0){
            logger.info(checker.generateReport());
        }
        try {
            while (reader.hasMoreRows()) {
                reader.nextRow();
                while (reader.hasMoreColumns()) {
                    nextValue = reader.nextEntry();
                    // if we don't have a title, just assume that we're writing to the same location.
                    String currentExcelColumnName = csvColumnNamesToExcelColumnNames.get(reader.getCurrentColumnName());
                    nextValue = mapToExcelValueSet(reader, nextValue, currentExcelColumnName);
                    if (reader.getCurrentColumnName() == null || reader.getCurrentColumnName().trim().equals("") ) {
                        writeCell(excelCurrentRowNumber, reader.getCurrentColumn(), sheet, nextValue);
                    } else {
                        final String xlColumnName = csvColumnNamesToExcelColumnNames.get(reader.getCurrentColumnName());
                        final Integer xlColumnNumber = excelTitleToColumnNumberMap.get(xlColumnName);
                        if (xlColumnNumber != null) {
                            writeCell(excelCurrentRowNumber, xlColumnNumber, sheet, nextValue);
                        }
                    }
                }
                excelCurrentRowNumber++;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("caught exception when writing exelCurrentRowNumber=" + excelCurrentRowNumber + ", csvColumnName=" + reader.getCurrentColumnName() +", excelColumnName="+csvColumnNamesToExcelColumnNames.get(reader.getCurrentColumnName())+ " with value " + nextValue, e);
        }
        outputWorkbook.write();
        outputWorkbook.close();
    }

    private String mapToExcelValueSet(CsvReader reader, String nextValue, String currentExcelColumnName) {
        if (columnValuesMap.get(currentExcelColumnName) != null) {
            nextValue = columnValuesMap.get(currentExcelColumnName).get(nextValue);
            if (nextValue == null){
                nextValue = reader.currentEntry();
                if (!nextValue.trim().equals(""))
                logger.info("Could not find mapping for value '" + nextValue + "' in data sets for excel column " + currentExcelColumnName);
            }
        }
        return nextValue;
    }

    private void writeCell(int rowNumber, int columnNumber, WritableSheet sheet, String nextValue) throws WriteException {
        if (nextValue == null || nextValue.trim().equals("")){
            return ;
        }

        final WritableCell writableCell = sheet.getWritableCell(columnNumber, rowNumber);
        Label label = new Label(columnNumber, rowNumber, nextValue);
        if (writableCell.getCellFormat()!=null){
            label.setCellFormat(writableCell.getCellFormat());
        }
        sheet.addCell(label);
        if (writableCell.getWritableCellFeatures() != null) {
            label.setCellFeatures(writableCell.getWritableCellFeatures());
        }
    }

    public Map<String, Integer> mapExcelColumnNamesToColumnNumbers(final WritableSheet sheet, Integer titleRow){
        Map<String,Integer> titleToColumnMap = new HashMap<String, Integer>();
        for (int columnNumber = 0; columnNumber < 50; columnNumber ++){
        final WritableCell writableCell = sheet.getWritableCell(columnNumber, titleRow);
            titleToColumnMap.put(writableCell.getContents(), columnNumber);
        }
        return titleToColumnMap;
    }

    public Integer getColumnNumber(String name){
        if (excelTitleToColumnNumberMap == null ){
            throw new IllegalStateException("columns not yet mapped");
        }
        return excelTitleToColumnNumberMap.get(name);
    }

}
