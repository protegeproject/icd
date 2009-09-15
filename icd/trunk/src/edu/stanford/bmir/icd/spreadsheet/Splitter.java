package edu.stanford.bmir.icd.spreadsheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import jxl.Cell;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.biff.EmptyCell;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Splitter {
    public final static String ORIGINAL_PROPERTY="spreadsheet.original";
    public final static String NO_COMMENTS_PROPERTY="spreadsheet.property.value";
    public final static String COMMENTS_ONLY_PROPERTY="spreadsheet.annotations";
    
    public final static int COLUMNS = 4;
    public final static int COMMENT_COLUMN = 1;
    public final static String COMMENT_CONTENTS = "Comment";
    
    private Properties parameters = new Properties();
    
    public Splitter() throws FileNotFoundException, IOException {
        parameters.load(new FileInputStream(new File("local.properties")));
    }
    
    public void run() throws BiffException, IOException, RowsExceededException, WriteException {
        Workbook workbook = Workbook.getWorkbook(new File(parameters.getProperty(ORIGINAL_PROPERTY)));
        Sheet input = workbook.getSheet(0);
        writeWithoutComments(input);
    }
    
    private void writeWithoutComments(Sheet input) throws IOException, RowsExceededException, WriteException {
        WritableWorkbook propertyValueWorkBook = Workbook.createWorkbook(new File(parameters.getProperty(NO_COMMENTS_PROPERTY)));
        WritableSheet propertyValueSheet = propertyValueWorkBook.createSheet("ICD Class/Property Values", 0);
        
        WritableWorkbook annotationsWorkBook = Workbook.createWorkbook(new File(parameters.getProperty(COMMENTS_ONLY_PROPERTY)));
        WritableSheet annotationsSheet = annotationsWorkBook.createSheet("ICD Annotations", 0);
           
        int propertyValueRow = 0;
        int annotationsRow = 0;
        for (int i = 0; i < input.getRows(); i++) {
            Cell commentCell =  input.getCell(COMMENT_COLUMN, i);
            
            String clsName =  ((LabelCell) input.getCell(0, i)).getContents();
            clsName = "icd:" + clsName;
            
            if (!commentCell.getContents().equals(COMMENT_CONTENTS)) {
                propertyValueSheet.addCell(new Label(0, propertyValueRow, clsName));

                String spreadSheetPropertyName = ((LabelCell) input.getCell(1, i)).getContents();
                String propertyRangeName = "icd:" + spreadSheetPropertyName + "Term";
                String propertyName = "icd:" + spreadSheetPropertyName.substring(0, 1).toLowerCase() + spreadSheetPropertyName.substring(1);
                
                propertyValueSheet.addCell(new Label(1, propertyValueRow, propertyName));
                propertyValueSheet.addCell(new Label(2, propertyValueRow, propertyRangeName));
                
                for (int j = 2; j < COLUMNS; j++) {
                    propertyValueSheet.addCell(copyCell(j+1, propertyValueRow, input.getCell(j, i)));
                }
                propertyValueRow++;
            }
            else {
                annotationsSheet.addCell(new Label(0, annotationsRow, clsName));

                for (int j = 1; j < COLUMNS; j++) {
                    annotationsSheet.addCell(copyCell(j, annotationsRow, input.getCell(j, i)));
                }
                annotationsRow++;
            }
        }
        
        propertyValueWorkBook.write();
        propertyValueWorkBook.close();
        
        annotationsWorkBook.write();
        annotationsWorkBook.close();
    }
    
    private WritableCell copyCell(int col, int row, Cell cell) {
        if (cell instanceof LabelCell) {
            return new Label(col, row, ((LabelCell) cell).getString());
        }
        else if (cell instanceof NumberCell) {
            return new jxl.write.Number(col, row, ((NumberCell) cell).getValue());
        }
        else if (cell instanceof EmptyCell) {
            return new EmptyCell(col, row);
        }
        else {
            throw new UnsupportedOperationException("Not implemented yet.");
        }
    }
    
    
    /**
     * @param args
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws BiffException 
     * @throws WriteException 
     * @throws RowsExceededException 
     */
    public static void main(String[] args) throws BiffException, FileNotFoundException, IOException, RowsExceededException, WriteException {
        new Splitter().run();
    }

}
