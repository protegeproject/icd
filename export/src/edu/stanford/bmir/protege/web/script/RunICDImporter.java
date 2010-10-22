package edu.stanford.bmir.protege.web.script;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class RunICDImporter {
    private static final String csvLocationParameter = "csvLocation";
    private static final String inputWorkbookLocationParameter = "inputWorkbookLocation";
    private static final String outputWorkbookLocationParameter = "outputWorkbookLocation";
    private static final String sheetNameParameter = "sheetName";
    private static final org.apache.commons.logging.Log logger = LogFactory.getLog(RunExportScriptWrapper.class);

    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption(csvLocationParameter, true, "the location of the csv file");
            options.addOption(inputWorkbookLocationParameter, true, "the location of the outputWorkbook file to merge");
            options.addOption(outputWorkbookLocationParameter, true, "the final location of the merged excel file");
            options.addOption(sheetNameParameter, true, "the name of the sheet to use");
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);
            final String csvLocation = cmd.getOptionValue(csvLocationParameter);
            final String inputWorkbookLocation = cmd.getOptionValue(inputWorkbookLocationParameter);
            final String outputWorkbookLocation = cmd.getOptionValue(outputWorkbookLocationParameter);
            final String sheetName = cmd.getOptionValue(sheetNameParameter);
            ExcelImporter importer = new ICDImporter();
            importer.importFile(csvLocation, inputWorkbookLocation, outputWorkbookLocation, sheetName);
        } catch (Exception e) {
            logger.error("error when exporting csv file with arguments " + Arrays.asList(args), e);
        }
    }

}
