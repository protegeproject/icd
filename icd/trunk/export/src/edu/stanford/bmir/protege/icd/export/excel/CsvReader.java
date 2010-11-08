package edu.stanford.bmir.protege.icd.export.excel;

import edu.stanford.bmir.protege.icd.export.PropertyConstants;
import edu.stanford.smi.protege.util.ApplicationProperties;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class CsvReader implements Closeable {
    private BufferedReader bufferedReader;
    public String[] row;
    String[] titles;
    String timestamp;
    int currentColumn = -1;
    int currentRow = 0;

    public CsvReader(String csvLocation, int titleRow) {
        File file = new File(csvLocation);
        if (!file.exists()) {
            throw new IllegalArgumentException("Expected a csv file at location " + csvLocation);
        }
        initializeBufferedReader(file);
        initializeTimestamp();
        initializeNames(titleRow);
    }

    private void initializeBufferedReader(File file) {
        try {
            final String fileEncoding = ApplicationProperties.getApplicationOrSystemProperty(PropertyConstants.CSV_FILE_ENCODING_PROPERTY, PropertyConstants.CSV_FILE_ENCODING_PROPERTY_DEFAULT);
            final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), fileEncoding);
            bufferedReader = new BufferedReader(inputStreamReader);
        } catch (IOException e) {
            throw new RuntimeException("Error intializing CsvReader for " + file.getAbsolutePath(), e);
        }
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
        currentRow = titleRow;
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

    public void nextRow() {
        try {
            row = bufferedReader.readLine().split("\t");
            currentRow = currentRow + 1;
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

    public boolean hasMoreColumns() {
        return row.length != currentColumn + 1;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public String getEntry(int entryNumber) {
        return row[entryNumber];
    }

    public void close() throws IOException {
        bufferedReader.close();
    }
}
