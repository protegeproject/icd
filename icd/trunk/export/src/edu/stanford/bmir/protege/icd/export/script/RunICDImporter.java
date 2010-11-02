package edu.stanford.bmir.protege.icd.export.script;

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
    private static final String CSV_LOCATION_PARAMETER = "csvLocation";
    private static final String INPUT_WORKBOOK_LOCATION_PARAMETER = "inputWorkbookLocation";
    private static final String OUTPUT_WORKBOOK_LOCATION_PARAMETER = "outputWorkbookLocation";
    private static final String SHEET_NAME_PARAMETER = "sheetName";
    private static final org.apache.commons.logging.Log logger = LogFactory.getLog(RunExportScriptWrapper.class);

    public static void main(String[] args) {
        try {
            Options options = new Options();
            options.addOption(CSV_LOCATION_PARAMETER, true, "the location of the csv file");
            options.addOption(INPUT_WORKBOOK_LOCATION_PARAMETER, true, "the location of the outputWorkbook file to merge");
            options.addOption(OUTPUT_WORKBOOK_LOCATION_PARAMETER, true, "the final location of the merged excel file");
            options.addOption(SHEET_NAME_PARAMETER, true, "the name of the sheet to use");
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);
            final String csvLocation = cmd.getOptionValue(CSV_LOCATION_PARAMETER);
            final String inputWorkbookLocation = cmd.getOptionValue(INPUT_WORKBOOK_LOCATION_PARAMETER);
            final String outputWorkbookLocation = cmd.getOptionValue(OUTPUT_WORKBOOK_LOCATION_PARAMETER);
            final String sheetName = cmd.getOptionValue(SHEET_NAME_PARAMETER);
            CsvToExcelConverter converter = new ICDCsvToExcelConverter();
            converter.convertFile(csvLocation, inputWorkbookLocation, outputWorkbookLocation, sheetName);
        } catch (Exception e) {
            logger.error("error when exporting csv file with arguments " + Arrays.asList(args), e);
        }
    }

}
