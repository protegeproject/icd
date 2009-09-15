package edu.stanford.bmir.icd.spreadsheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import jxl.CellFeatures;
import jxl.CellType;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Splitter {
    public final static String ORIGINAL_PROPERTY="spreadsheet.original";
    public final static String NO_COMMENTS_PROPERTY="spreadsheet.no.comments";
    public final static String COMMENTS_ONLY_PROPERTY="spreadsheet.comments.only";
    
    public final static int COLUMNS = 4;
    public final static int COMMENT_COLUMN = 2;
    public final static String COMMENT_CONTENTS = "Comment";
    
    private Properties parameters = new Properties();
    
    public Splitter() throws FileNotFoundException, IOException {
        parameters.load(new FileInputStream(new File("local.properties")));
    }
    
    public void run() throws BiffException, IOException {
        Workbook workbook = Workbook.getWorkbook(new File(parameters.getProperty(ORIGINAL_PROPERTY)));
        Sheet input = workbook.getSheet(0);
        writeWithoutComments(input);
    }
    
    private void writeWithoutComments(Sheet input) throws IOException, RowsExceededException, WriteException {
        WritableWorkbook workbook = Workbook.createWorkbook(new File(parameters.getProperty(NO_COMMENTS_PROPERTY)));
        WritableSheet sheet = workbook.getSheet(0);
        sheet.setName("ICD Class/Property Values");
        int outputRow = 0;
        for (int i = 0; i < input.getRows(); i++) {
            LabelCell commentCell = (LabelCell) input.getCell(COMMENT_COLUMN, i);
            if (!commentCell.getContents().equals(COMMENT_CONTENTS)) {
                outputRow++;
                
                String clsName =  ((LabelCell) input.getCell(0, i)).getContents();
                clsName = "icd:" + clsName;
                sheet.addCell(new Label(0, outputRow, clsName));
                
                String propertyName = ((LabelCell) input.getCell(1, i)).getContents();
                propertyName = "icd:" + propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
                sheet.addCell(new Label(1, outputRow, propertyName));
                
                

            }
        }
    }
    
    
    /**
     * @param args
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws BiffException 
     */
    public static void main(String[] args) throws BiffException, FileNotFoundException, IOException {
        new Splitter().run();
    }

}
