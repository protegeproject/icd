package edu.stanford.bmir.protege.web.script;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.IOException;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public interface ExcelImporter {
    void importFile(String csvLocation, String inputWorkbookLocation, String outputWorkbookLocation, String sheetName) throws IOException, BiffException, WriteException;

}
