package edu.stanford.bmir.protege.icd.export.script;

import edu.stanford.smi.protege.util.ApplicationProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    int currentRow = 0;
    protected static final String DEFAULT_FILE_ENCODING = "ISO-8859-1";

    public CsvReader(String csvLocation, int titleRow) {
        File file = new File(csvLocation);
        try {
            final String fileEncoding = ApplicationProperties.getApplicationOrSystemProperty("csv.file.encoding", DEFAULT_FILE_ENCODING);
            final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), fileEncoding);
            bufferedReader = new BufferedReader(inputStreamReader);
        } catch (IOException e) {
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

    public String currentEntry() {
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

    public int getCurrentRow() {
        return currentRow;
    }

    public void close() throws IOException {
        bufferedReader.close();
    }
}
