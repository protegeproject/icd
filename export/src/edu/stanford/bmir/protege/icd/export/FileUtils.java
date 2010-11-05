package edu.stanford.bmir.protege.icd.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class FileUtils {
    public static InputStream getInputStream(String inputWorkbookLocation) throws FileNotFoundException {
        final File file = new File(inputWorkbookLocation);
        if (!file.exists()) {
            return FileUtils.class.getClassLoader().getResourceAsStream(inputWorkbookLocation);
        } else {
            return new FileInputStream(file);
        }
    }
}
