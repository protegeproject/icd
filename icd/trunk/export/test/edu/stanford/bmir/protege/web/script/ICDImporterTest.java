package edu.stanford.bmir.protege.web.script;

import junit.framework.TestCase;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDImporterTest extends TestCase {
    private final String templateLocation = "excel/template.xls";
    private final String sheetName = "Authoring template";
    private ExcelImporter importer = new ICDImporter();
    private static final int TEXTUAL_DEFINITION_COLUMN = 16;
    private static final int DETAILED_DEFINITION_COLUMN = 17;

    public void testTextualDefinitionPopulated() throws BiffException, IOException, WriteException {
        final String outputWorkbookLocation = "output/output.xls";
        importer.importFile("test/sample-textual-definition-populated-file.csv", templateLocation, outputWorkbookLocation, sheetName);
        final Workbook outputWorkbook = Workbook.getWorkbook(new File(outputWorkbookLocation));
        final Sheet sheet = outputWorkbook.getSheet(sheetName);
        assertEquals("Acral lentiginous malignant melanoma is a distinct form of melanoma occurring on palms and soles or in or around the nail apparatus.  It is typically preceded by a slowly progressive in situ phase (acral lentiginous melanoma in situ) before it becomes invasive.   It may, however, present de novo with a rapidly growing invasive tumour. Although it represents a small proportion of melanomas overall (5 to 10%), it accounts for a high proportion of melanomas seen in dark-skinned people.  On the palms and soles it presents typically as an area of irregular macular pigmentation.  If it develops in the nail matrix it may present in the early stages as a longitudinal pigmented band within the nail plate. More aggressive tumours present as ulcerated nodules which, when involving the nail apparatus, can cause destruction of the nail plate.",
                getCellContents(sheet, TEXTUAL_DEFINITION_COLUMN, 3));
        assertTrue(getCellContents(sheet, DETAILED_DEFINITION_COLUMN, 3).trim().equals(""));
    }


    private String getCellContents(final Sheet sheet, int column, int row) {
        Cell cell = sheet.getCell(column, row);
        return cell.getContents();
    }
}
