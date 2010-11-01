package edu.stanford.bmir.protege.icd.export.script;

import jxl.Sheet;
import jxl.Workbook;

import java.io.File;

/**
 * Test for the ICDCsvToExcelConverter.
 * <p/>
 * If this test does not work, go and run the ExportScriptWrapperTest on who's output it depends!
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDImporterTest extends ValidateFieldsAddedCorrectlyTest {
    private final String templateLocation = "excel/template.xls";
    private final String sheetName = "Authoring template";
    private CsvToExcelConverter converter = new ICDCsvToExcelConverter();
    private Sheet sheet;

    @Override
    protected String getFieldContents(int fieldPosition, int rowNumber) {
        return sheet.getCell(fieldPosition, rowNumber).getContents();
    }

    @Override
    protected void initializeTest(final String topNode, final String csvFileName, final String outputWorkbookLocation) throws Exception {
        converter.importFile(csvFileName, templateLocation, outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        sheet = outputWorkbook.getSheet(sheetName);
    }
}
