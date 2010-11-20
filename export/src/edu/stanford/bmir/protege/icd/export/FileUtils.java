package edu.stanford.bmir.protege.icd.export;

import edu.stanford.bmir.protege.icd.export.ui.ICDExporterPlugin;
import edu.stanford.smi.protege.plugin.PluginUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class FileUtils {
    public static InputStream getInputStream(String location) throws FileNotFoundException {
        File file = new File(location);
        if (!file.exists()) {
            file = new File(PluginUtilities.getInstallationDirectory(ICDExporterPlugin.class.getName()).getPath() + File.separator + location);
            if (!file.exists()) {
                throw new IllegalArgumentException("Could not find file " + file.getAbsolutePath() + " on path.");
            }
        }
        return new FileInputStream(file);
    }
}
