package edu.stanford.bmir.protege.icd.export.script;

import junit.framework.TestCase;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class JxlCsvToExcelConverterTest extends TestCase {
    public void testSimpleCase() throws BiffException, IOException, WriteException {
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(5, 3, null, 0, 1);
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "My Test Sheet";
        unit.importFile("test/simple-test-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("a", getCellContents(sheet, 0, 6));
        assertEquals("", getCellContents(sheet, 2, 7));
        assertEquals("aa", getCellContents(sheet, 3, 7));
        assertEquals("aaa", getCellContents(sheet, 0, 8));
        assertEquals("eee", getCellContents(sheet, 4, 8));
    }

    public void testTimestamp() throws BiffException, IOException, WriteException {
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(5, 3, null, 0, 1);
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "My Test Sheet";
        unit.importFile("test/simple-test-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("Sat, 23 Oct 2010 00:21:49 +0000", getCellContents(sheet, 1, 0));
    }

    public void testMissingColumnNamesInCsvFileCase() throws BiffException, IOException, WriteException {
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(3, 3, null, 0, 1);
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "My Missing Columns Sheet";
        unit.importFile("test/missing-column-names-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("a", getCellContents(sheet, 0, 4));
        assertEquals("", getCellContents(sheet, 2, 5));
        assertEquals("aa", getCellContents(sheet, 3, 5));
        assertEquals("ccc", getCellContents(sheet, 2, 6));
        assertEquals("ddd", getCellContents(sheet, 3, 6));
        assertEquals("aaa", getCellContents(sheet, 0, 6));
        assertEquals("ggg", getCellContents(sheet, 6, 6));
    }

    public void testInsertIntoValidatedCellsCase() throws BiffException, IOException, WriteException {
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(1, 3, null, 0, 1);
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "Drop Downs Sheet";
        unit.importFile("test/validated-cells-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("xxx", getCellContents(sheet, 0, 2));
        assertEquals("abc", getCellContents(sheet, 1, 2));
        assertEquals("x", getCellContents(sheet, 0, 3));
        assertEquals("", getCellContents(sheet, 1, 4));
    }

    public void testSimpleRefinedValuesCase() throws BiffException, IOException, WriteException {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put("0", new HashMap<String, String>());
        map.get("0").put("x", "123");
        map.get("0").put("xxxxx", "xxxx");
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(1, 3, map, 0, 1);
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "Drop Downs Sheet";
        unit.importFile("test/validated-cells-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("123", getCellContents(sheet, 0, 3));
        assertEquals("abc", getCellContents(sheet, 1, 2));
        assertEquals("xxxx", getCellContents(sheet, 0, 4));
        assertEquals("", getCellContents(sheet, 1, 4));
    }

    public void testPipeDelimitedRefinedValuesCase() throws BiffException, IOException, WriteException {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put("0", new HashMap<String, String>());
        map.get("0").put("mapped", "123");
        map.get("0").put("also mapped", "456");
        map.get("0").put("mapped again", "789");
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(1, 3, map, 0, 1);
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "Drop Downs Sheet";
        unit.importFile("test/complex-validated-cells-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("123 || 456", getCellContents(sheet, 0, 2));
        assertEquals("abc", getCellContents(sheet, 1, 2));
        assertEquals("789 || not mapped", getCellContents(sheet, 0, 3));
        assertEquals("", getCellContents(sheet, 1, 4));
    }

    private String getCellContents(final Sheet sheet, int column, int row) {
        Cell cell = sheet.getCell(column, row);
        return cell.getContents();
    }


}
