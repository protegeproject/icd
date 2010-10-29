package edu.stanford.bmir.protege.web.script;

import jxl.Sheet;
import jxl.Workbook;

import java.io.File;

/**
 * Test for the ICDImporter.
 * <p/>
 * If this test does not work, go and run the ExportScriptWrapperTest on who's output it depends!
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDImporterTest extends ValuesTest {
    private final String templateLocation = "excel/template.xls";
    private final String sheetName = "Authoring template";
    private ExcelImporter importer = new ICDImporter();
    private Sheet sheet;

    @Override
    protected String getFieldContents(int fieldPosition, int rowNumber) {
        return sheet.getCell(fieldPosition, rowNumber).getContents();
    }

    @Override
    protected void initializeTest(final String topNode, final String csvFileName, final String outputWorkbookLocation) throws Exception {
        importer.importFile(csvFileName, templateLocation, outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        sheet = outputWorkbook.getSheet(sheetName);
    }
}
