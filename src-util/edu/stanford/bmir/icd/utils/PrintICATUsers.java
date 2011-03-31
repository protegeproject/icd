package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.logging.Level;

import edu.stanford.smi.protege.util.Log;

public class PrintICATUsers {

    public static void main(String[] args) {

        String csvPath = "/work/src/icd/icatusers/icat_metaproject_db_exported_20110309.csv";

        try {
            BufferedReader input = new BufferedReader(new FileReader(csvPath));
            input.readLine();

            String line = null;
            while ((line = input.readLine()) != null) {
                if (line != null) {
                    try {
                        while (!line.endsWith("\"") && !line.endsWith("\t")) {
                            line = line + input.readLine();
                        }
                        processLine(line);
                    } catch (Exception e) {
                        Log.getLogger().log(Level.WARNING, " Could not read line: " + line, e);
                    }
                }
            }
            } catch (Exception e) {
                Log.getLogger().log(Level.WARNING, "Error at parsing csv", e);
            }
    }


    private static void processLine(String line) {
        final String[] split = line.split("\t");

        String name = getSafeValue(split, 0);
        String email = getSafeValue(split, 1);

        String date1_str = getSafeValue(split, 2);
        Date date1 = null;
        if (date1_str != null && date1_str.length() > 0) {
            date1 = new Date(Long.parseLong(date1_str));
        }

        Log.getLogger().info(name + "\t" + email + "\t" + (date1 == null ? "" : date1) );

    }

    private static String getSafeValue(final String[] split, final int index) {
        if (index >= split.length) {
            return "";
        }
        String string = split[index];
        if (string == null || string.length() == 0) {
            return "";
        }
        return string.replaceAll("\"", "");
    }

}
