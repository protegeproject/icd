package edu.stanford.bmir.protege.icd.export.script;

import junit.framework.TestCase;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class JxlCsvToExcelConverterTest extends TestCase {

    public void testSimpleCase() throws BiffException, IOException, WriteException {
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(5, 3, null, 0, 1, 1, new HashSet<Integer>());
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "My Test Sheet";
        unit.convertFile("test/simple-test-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("a", getCellContents(sheet, 6, 0));
        assertEquals("", getCellContents(sheet, 7, 2));
        assertEquals("aa", getCellContents(sheet, 7, 3));
        assertEquals("aaa", getCellContents(sheet, 8, 0));
        assertEquals("eee", getCellContents(sheet, 8, 4));
    }

    public void testHighlightsDuplicateCase() throws BiffException, IOException, WriteException {
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(1, 3, null, 0, 1, 1, new HashSet<Integer>());
        final String outputWorkbookLocation = "output/duplicates-test.xls";
        final String sheetName = "My Test Sheet";
        unit.convertFile("test/duplicates-test-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals(null, getCellColor(sheet, 1, 0));
        assertEquals(Colour.DEFAULT_BACKGROUND, getCellColor(sheet, 4, 1));
        assertEquals(Colour.DEFAULT_BACKGROUND, getCellColor(sheet, 2, 1));
        assertEquals(Colour.CORAL, getCellColor(sheet, 3, 1));
        assertEquals(Colour.CORAL, getCellColor(sheet, 3, 0));
    }

    public void testDoesntHighlightStopFields() throws BiffException, IOException, WriteException {
        final HashSet<Integer> stopFields = new HashSet<Integer>();
        stopFields.add(0);
        stopFields.add(4);
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(1, 3, null, 0, 1, 1, stopFields);
        final String outputWorkbookLocation = "output/doesnt-highlight-duplicates.xls";
        final String sheetName = "My Test Sheet";
        unit.convertFile("test/duplicates-test-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals(null, getCellColor(sheet, 1, 0));
        assertEquals(Colour.DEFAULT_BACKGROUND, getCellColor(sheet, 4, 1));
        assertEquals(Colour.DEFAULT_BACKGROUND, getCellColor(sheet, 2, 1));
        assertEquals(Colour.CORAL, getCellColor(sheet, 3, 1));
        assertEquals("b", getCellContents(sheet, 3, 1));
        assertEquals("x", getCellContents(sheet, 3, 0));
        assertEquals(Colour.DEFAULT_BACKGROUND, getCellColor(sheet, 3, 0));
        assertEquals(null, getCellColor(sheet, 3, 4));
        assertEquals(Colour.CORAL, getCellColor(sheet, 3, 3));
    }

    public void testTimestamp() throws BiffException, IOException, WriteException {
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(5, 3, null, 0, 1, 0, new HashSet<Integer>());
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "My Test Sheet";
        unit.convertFile("test/simple-test-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("Sat, 23 Oct 2010 00:21:49 +0000", getCellContents(sheet, 0, 1));
    }

    public void testMissingColumnNamesInCsvFileCase() throws BiffException, IOException, WriteException {
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(3, 3, null, 0, 1, 0, new HashSet<Integer>());
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "My Missing Columns Sheet";
        unit.convertFile("test/missing-column-names-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("a", getCellContents(sheet, 4, 0));
        assertEquals("", getCellContents(sheet, 5, 2));
        assertEquals("aa", getCellContents(sheet, 5, 3));
        assertEquals("ccc", getCellContents(sheet, 6, 2));
        assertEquals("ddd", getCellContents(sheet, 6, 3));
        assertEquals("aaa", getCellContents(sheet, 6, 0));
        assertEquals("ggg", getCellContents(sheet, 6, 6));
    }

    public void testInsertIntoValidatedCellsCase() throws BiffException, IOException, WriteException {
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(1, 3, null, 0, 1, 0, new HashSet<Integer>());
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "Drop Downs Sheet";
        unit.convertFile("test/validated-cells-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("xxx", getCellContents(sheet, 2, 0));
        assertEquals("abc", getCellContents(sheet, 2, 1));
        assertEquals("x", getCellContents(sheet, 3, 0));
        assertEquals("", getCellContents(sheet, 4, 1));
    }

    public void testSimpleRefinedValuesCase() throws BiffException, IOException, WriteException {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put("0", new HashMap<String, String>());
        map.get("0").put("x", "123");
        map.get("0").put("xxxxx", "xxxx");
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(1, 3, map, 0, 1, 0, new HashSet<Integer>());
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "Drop Downs Sheet";
        unit.convertFile("test/validated-cells-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("123", getCellContents(sheet, 3, 0));
        assertEquals("abc", getCellContents(sheet, 2, 1));
        assertEquals("xxxx", getCellContents(sheet, 4, 0));
        assertEquals("", getCellContents(sheet, 4, 1));
    }

    public void testPipeDelimitedRefinedValuesCase() throws BiffException, IOException, WriteException {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put("0", new HashMap<String, String>());
        map.get("0").put("mapped", "123");
        map.get("0").put("also mapped", "456");
        map.get("0").put("mapped again", "789");
        JxlCsvToExcelConverter unit = new JxlCsvToExcelConverter(1, 3, map, 0, 1, 0, new HashSet<Integer>());
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "Drop Downs Sheet";
        unit.convertFile("test/complex-validated-cells-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("123 || 456", getCellContents(sheet, 2, 0));
        assertEquals("abc", getCellContents(sheet, 2, 1));
        assertEquals("789 || not mapped", getCellContents(sheet, 3, 0));
        assertEquals("", getCellContents(sheet, 4, 1));
    }

    private String getCellContents(final Sheet sheet, int row, int column) {
        Cell cell = sheet.getCell(column, row);
        return cell.getContents();
    }

    private Colour getCellColor(final Sheet sheet, int row, int column) {
        Cell cell = sheet.getCell(column, row);
        return cell.getCellFormat() == null ? null : cell.getCellFormat().getBackgroundColour();
    }


}
