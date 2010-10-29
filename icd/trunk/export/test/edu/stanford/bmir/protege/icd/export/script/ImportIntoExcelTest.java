package edu.stanford.bmir.protege.icd.export.script;

import junit.framework.TestCase;
import jxl.demo.ReadWrite;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ImportIntoExcelTest extends TestCase {
    private final String scratchOutputFile = "output/xxx.xls";

    public void setUp(){
        File file = new File(scratchOutputFile);
        if (file.exists()){
            file.delete();
        }
    }

    public void testOurTemplateFile() throws BiffException, IOException, WriteException {
        ReadWrite readWrite = new ReadWrite("excel/template.xls", scratchOutputFile);
        readWrite.readWrite();
    }
}
