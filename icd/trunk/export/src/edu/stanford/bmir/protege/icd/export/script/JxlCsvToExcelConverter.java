package edu.stanford.bmir.protege.icd.export.script;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Does the heavy lifting of reading a csv file of an expected format into a spreadsheet.
 * <p/>
 * Excel title row and the csv title row are passed in.
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class JxlCsvToExcelConverter implements CsvToExcelConverter {
    private static final org.apache.commons.logging.Log logger = LogFactory.getLog(JxlCsvToExcelConverter.class);
    private Map<String, Map<String, String>> columnValuesMap = new HashMap<String, Map<String, String>>();
    private int excelTitleRow;
    private int csvTitleRow;
    private int timestampRow;
    private int timestampColumn;

    /**
     * The constructor for this class.
     *
     * @param excelTitleRow   Note that this is array-based (0 is first number), not an excel-based (1 is first number) parameter
     * @param csvTitleRow     Note that this is an array-based (0 is first number), not an excel-based (1 is first number) parameter.
     * @param columnValuesMap The values used to map from ICD values to 'pretty' spreadsheet values.
     * @param timestampRow
     * @param timestampColumn
     */
    public JxlCsvToExcelConverter(final int excelTitleRow, final int csvTitleRow, Map<String, Map<String, String>> columnValuesMap, int timestampRow, int timestampColumn) {
        this.excelTitleRow = excelTitleRow;
        this.csvTitleRow = csvTitleRow;
        if (columnValuesMap != null) {
            this.columnValuesMap = columnValuesMap;
        }
        this.timestampRow = timestampRow;
        this.timestampColumn = timestampColumn;
    }

    public void importFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName) throws IOException, BiffException, WriteException {
        final File file = new File(inputWorkbookLocation);
        InputStream is = null;
        if (!file.exists()) {
            is = getClass().getResourceAsStream(inputWorkbookLocation);
        } else {
            is = new FileInputStream(file);
        }
        final Workbook inputWorkbook = Workbook.getWorkbook(is);
        final WritableWorkbook outputWorkbook = Workbook.createWorkbook(new File(outputWorkbookLocation), inputWorkbook);
        int excelCurrentRowNumber = excelTitleRow + 1;
        final WritableSheet sheet = outputWorkbook.getSheet(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("Could not find sheet " + sheetName + " in spreadsheet file " + inputWorkbookLocation);
        }
        CsvReader reader = new CsvReader(csvLocation, csvTitleRow);
        writeCell(timestampRow, timestampColumn, sheet, reader.getTimestamp());
        String nextValue = "";
        try {
            while (reader.hasMoreRows()) {
                reader.nextRow();
                while (reader.hasMoreColumns()) {
                    nextValue = reader.nextEntry();
                    // if we don't have a title, just assume that we're writing to the same location.
                    nextValue = mapToExcelValueSet(reader, nextValue, Integer.toString(reader.getCurrentColumn()));
                    writeCell(excelCurrentRowNumber, reader.getCurrentColumn(), sheet, nextValue);
                }
                excelCurrentRowNumber++;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("caught exception when writing excelCurrentRowNumber=" + excelCurrentRowNumber + ", excelColumnNumber=" + reader.getCurrentColumn() + ", csvColumnName=" + reader.getCurrentColumnName() + " with value " + nextValue, e);
        }
        outputWorkbook.write();
        outputWorkbook.close();
    }

    private String mapToExcelValueSet(CsvReader reader, String nextValue, String currentExcelColumnName) {
        StringBuffer sbuff = new StringBuffer();
        if (columnValuesMap.get(currentExcelColumnName) != null) {
            final String[] strings = nextValue.split(" \\|\\| ");
            for (int i = 0; i < strings.length; i++) {
                nextValue = columnValuesMap.get(currentExcelColumnName).get(strings[i]);
                if (nextValue == null) {
                    nextValue = strings[i];
                    if (!nextValue.trim().equals("")) {
                        logger.info("Could not find mapping for value '" + nextValue + "' in data sets for excel column " + currentExcelColumnName);
                    }
                }
                sbuff.append(nextValue);
                if (i + 1 < strings.length) {
                    sbuff.append(" || ");
                }
            }
            nextValue = sbuff.toString();
        }
        return nextValue;
    }

    private void writeCell(int rowNumber, int columnNumber, WritableSheet sheet, String nextValue) throws WriteException {
        if (nextValue == null || nextValue.trim().equals("")) {
            return;
        }

        final WritableCell writableCell = sheet.getWritableCell(columnNumber, rowNumber);
        Label label = new Label(columnNumber, rowNumber, nextValue);
        if (writableCell.getCellFormat() != null) {
            label.setCellFormat(writableCell.getCellFormat());
        }
        sheet.addCell(label);
        if (writableCell.getWritableCellFeatures() != null) {
            label.setCellFeatures(writableCell.getWritableCellFeatures());
        }
    }

}
