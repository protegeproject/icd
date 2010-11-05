package edu.stanford.bmir.protege.icd.export.excel;

import edu.stanford.bmir.protege.icd.export.FileUtils;
import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.util.Log;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Does the heavy lifting of reading a csv file of an expected format into a spreadsheet.
 * <p/>
 * All the data defining where to write the data is passed in.
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class JxlCsvToExcelConverter implements CsvToExcelConverter {
    private static final Logger logger = Log.getLogger(JxlCsvToExcelConverter.class);
    private Set<String> duplicates = new HashSet<String>();
    private int excelTitleRow;
    private int csvTitleRow;
    private int timestampRow;
    private int timestampColumn;
    private int classField;
    private Set<Integer> fieldsNotToColor;

    /**
     * The constructor for this class.
     *
     * @param excelTitleRow    Note that this is array-based (0 is first number), not an excel-based (1 is first number) parameter
     * @param csvTitleRow      Note that this is an array-based (0 is first number), not an excel-based (1 is first number) parameter.
     * @param timestampRow     Note that this is an array-based (0 is first number), not an excel-based (1 is first number) parameter.
     * @param timestampColumn  Note that this is an array-based (0 is first number), not an excel-based (1 is first number) parameter.
     * @param identityField    This is the field that we use to identify an entity in the case of duplicates.
     * @param fieldsNotToColor A set of fields that we will not highlight even if duplicates are found.
     */
    public JxlCsvToExcelConverter(final int excelTitleRow, final int csvTitleRow, int timestampRow, int timestampColumn, int identityField, Set<Integer> fieldsNotToColor) {
        this.excelTitleRow = excelTitleRow;
        this.csvTitleRow = csvTitleRow;
        this.timestampRow = timestampRow;
        this.timestampColumn = timestampColumn;
        this.classField = identityField;
        this.fieldsNotToColor = fieldsNotToColor;
    }

    public void convertFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName) {
        InputStream is;
        final Workbook inputWorkbook;
        try {
            is = FileUtils.getInputStream(inputWorkbookLocation);
            inputWorkbook = Workbook.getWorkbook(is);
        } catch (IOException e) {
            throw new ProtegeException("Could not load workbook at " + inputWorkbookLocation, e);
        } catch (BiffException e) {
            throw new ProtegeException("Could not use JExcel with input workbook at " + inputWorkbookLocation, e);
        }

        final WritableWorkbook outputWorkbook = initializeOutputWorkbook(outputWorkbookLocation, inputWorkbook);

        CsvReader reader = new CsvReader(csvLocation, csvTitleRow);
        try {
            int excelCurrentRowNumber = excelTitleRow + 1;
            final WritableSheet sheet = outputWorkbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Could not find sheet " + sheetName + " in spreadsheet file " + inputWorkbookLocation);
            }
            writeCell(timestampRow, timestampColumn, sheet, reader.getTimestamp(), "");

            while (reader.hasMoreRows()) {
                reader.nextRow();
                String className = reader.row[classField];
                while (reader.hasMoreColumns()) {
                    String nextValue = reader.nextEntry();
                    writeCell(excelCurrentRowNumber, reader.getCurrentColumn(), sheet, nextValue, className);
                }
                excelCurrentRowNumber++;
                duplicates.add(className);
            }

            try {
                outputWorkbook.write();
            } catch (IOException e) {
                throw new ProtegeException("Could not write out output workbook at " + outputWorkbookLocation, e);
            }
        } finally {
            inputWorkbook.close();
            try {
                is.close();
                outputWorkbook.close();
                reader.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing input stream", e);
            } catch (WriteException e) {
                logger.log(Level.SEVERE, "Error closing input stream", e);
            }
            duplicates.clear();
        }
    }

    private WritableWorkbook initializeOutputWorkbook(String outputWorkbookLocation, Workbook inputWorkbook) {
        final WritableWorkbook outputWorkbook;
        try {
            outputWorkbook = Workbook.createWorkbook(new File(outputWorkbookLocation), inputWorkbook);
        } catch (IOException e) {
            throw new ProtegeException("Could not create output workbook at " + outputWorkbookLocation, e);
        }
        return outputWorkbook;
    }

    private void writeCell(int rowNumber, int columnNumber, WritableSheet sheet, String nextValue, String className) {
        try {
            if ((nextValue == null || nextValue.trim().equals("")) && (!duplicates.contains(className) || fieldsNotToColor.contains(columnNumber))) {
                return;
            }

            final WritableCell originalCell = sheet.getWritableCell(columnNumber, rowNumber);
            WritableCell label = new Label(columnNumber, rowNumber, nextValue);

            if (originalCell.getCellFormat() != null) {
                label.setCellFormat(originalCell.getCellFormat());
            }
            if (duplicates.contains(className)) {
                WritableCellFormat cellFormat = new WritableCellFormat(label.getCellFormat());
                if (!fieldsNotToColor.contains(columnNumber)) {
                    // WTH - If I attempt to hold this value as a constant or class member variable, then JExcel will fail when writing out the workbook.
                    cellFormat.setBackground(Colour.CORAL);
                }
                label.setCellFormat(cellFormat);
                originalCell.setCellFormat(new WritableCellFormat(cellFormat));
            }
            if (nextValue != null && !nextValue.trim().equals("")) {
                sheet.addCell(label);
            }
        } catch (WriteException e) {
            throw new ProtegeException("Could not write to cell rowNumber=" + rowNumber + ", columnNumber=" + columnNumber + ", sheet=" + sheet + " nextValue=" + nextValue + " className=" + className, e);
        }
    }

}
