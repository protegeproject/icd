package edu.stanford.bmir.protege.web.script;

import junit.framework.TestCase;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.IOException;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ICDImporterTest extends TestCase {
    public void testBasicCase() throws BiffException, IOException, WriteException {
        ExcelImporter importer = new ICDImporter();
        importer.importFile("test/test-export.csv", "excel/template.xls", "output/output.xls", "Authoring template");

    }
}
