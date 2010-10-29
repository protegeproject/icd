package edu.stanford.bmir.protege.icd.export.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class CsvReader {
    private BufferedReader bufferedReader;
    String[] row;
    String[] titles;
    String timestamp;
    int currentColumn = -1;

    public CsvReader(String csvLocation, int titleRow) {
        File file = new File(csvLocation);
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        initializeTimestamp();
        initializeNames(titleRow);
    }

    private void initializeTimestamp() {
        try {
            timestamp = bufferedReader.readLine();
            if (timestamp.contains("\t")) {
                timestamp = timestamp.split("\t")[1];
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void initializeNames(int titleRow) {
        for (int i = 1; i < titleRow + 1; i++) {
            nextRow();
        }
        titles = row;
    }

    public boolean hasMoreRows() {
        try {
            return bufferedReader.ready();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String nextEntry() {
        currentColumn++;
        return row[currentColumn];
    }

    public String currentEntry() {
        return row[currentColumn];
    }

    public void nextRow() {
        try {
            row = bufferedReader.readLine().split("\t");
            currentColumn = -1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentColumnName() {
        return titles[currentColumn];
    }

    public int getCurrentColumn() {
        return currentColumn;
    }

    public Map<String, String> getNamedColumnsAsMap() {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < titles.length; i++) {
            map.put(titles[i], row[i]);
        }
        return map;
    }

    public boolean hasMoreColumns() {
        return row.length != currentColumn + 1;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
