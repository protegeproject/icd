package edu.stanford.bmir.protege.web.script;

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
public class JxlImporterTest extends TestCase {
    public void testSimpleCase() throws BiffException, IOException, WriteException {
        Map<String, String> csvToExcelMap = new HashMap<String, String>();
        csvToExcelMap.put("one", "A");
        csvToExcelMap.put("two", "B");
        csvToExcelMap.put("three", "C");
        csvToExcelMap.put("four", "D");
        csvToExcelMap.put("five", "E");
        JxlImporter unit = new JxlImporter(csvToExcelMap, 5, 3, null);
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "My Test Sheet";
        unit.importFile("test/simple-test-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("a", getCellContents(sheet,0,6));
        assertEquals("", getCellContents(sheet,2,7));
        assertEquals("aa", getCellContents(sheet,3,7));
        assertEquals("aaa", getCellContents(sheet,0,8));
        assertEquals("eee", getCellContents(sheet,4,8));
    }

    public void testMissingColumnNamesInCsvFileCase() throws BiffException, IOException, WriteException {
        Map<String, String> csvToExcelMap = new HashMap<String, String>();
        csvToExcelMap.put("one", "A");
        csvToExcelMap.put("two", "B");
        csvToExcelMap.put("five", "E");
        csvToExcelMap.put("six", "F");
        csvToExcelMap.put("seven", "G");
        JxlImporter unit = new JxlImporter(csvToExcelMap, 3, 3, null);
        final String outputWorkbookLocation = "output/simpleTestOutput.xls";
        final String sheetName = "My Missing Columns Sheet";
        unit.importFile("test/missing-column-names-export.csv", "test/TestTemplate.xls", outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("a", getCellContents(sheet, 0,4));
        assertEquals("", getCellContents(sheet, 2, 5));
        assertEquals("aa", getCellContents(sheet, 3,5));
        assertEquals("ccc", getCellContents(sheet, 2,6));
        assertEquals("ddd", getCellContents(sheet, 3,6));
        assertEquals("aaa", getCellContents(sheet, 0,6));
        assertEquals("ggg", getCellContents(sheet, 6,6));
    }

    public void testInsertIntoValidatedCellsCase() throws BiffException, IOException, WriteException {
        Map<String, String> csvToExcelMap = new HashMap<String, String>();
        csvToExcelMap.put("one", "A");
        csvToExcelMap.put("two", "B");
        csvToExcelMap.put("three", "C");
        csvToExcelMap.put("four", "D");
        csvToExcelMap.put("five", "E");
        JxlImporter unit = new JxlImporter(csvToExcelMap, 1, 3, null);
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

    public void testRefinedValuesCase() throws BiffException, IOException, WriteException {
        Map<String, String> csvToExcelMap = new HashMap<String, String>();
        csvToExcelMap.put("one", "A");
        csvToExcelMap.put("two", "B");
        csvToExcelMap.put("three", "C");
        csvToExcelMap.put("four", "D");
        csvToExcelMap.put("five", "E");
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        map.put("A", new HashMap<String, String>());
        map.get("A").put("x", "123");
        map.get("A").put("xxxxx", "xxxx");
        JxlImporter unit = new JxlImporter(csvToExcelMap, 1, 3, map);
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

    private String getCellContents(final Sheet sheet, int column, int row){
        Cell cell = sheet.getCell(column, row);
        return cell.getContents();
    }


}
