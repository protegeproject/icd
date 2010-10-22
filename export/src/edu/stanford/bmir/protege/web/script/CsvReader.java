package edu.stanford.bmir.protege.web.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class CsvReader {
    private BufferedReader bufferedReader;
    String[] row;
    String[] titles;
    int currentColumn = -1;

    public CsvReader(String csvLocation, int titleRow) {
         File file = new File(csvLocation);
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        initializeNames(titleRow);
    }

    private void initializeNames(int titleRow){
         for (int i = 0; i < titleRow + 1; i ++){
             nextRow();
         }
         titles = row;
    }

    public boolean hasMoreRows(){
        try {
            return bufferedReader.ready();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String nextEntry(){
        currentColumn ++;
        return row[currentColumn ];
    }

    public String currentEntry(){
        return row[currentColumn ];
    }

    public void nextRow() {
        try {
            row = bufferedReader.readLine().split("\t");
            currentColumn = -1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentColumnName(){
        return titles[currentColumn];
    }

    public int getCurrentColumn(){
        return currentColumn;
    }

    public boolean hasMoreColumns(){
        return row.length != currentColumn + 1;
    }

}
