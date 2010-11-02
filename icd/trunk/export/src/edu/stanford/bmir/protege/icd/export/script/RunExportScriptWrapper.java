package edu.stanford.bmir.protege.icd.export.script;

import edu.stanford.smi.protege.model.Project;
import org.apache.bsf.BSFException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class RunExportScriptWrapper {
    private static final String CSV_LOCATION_PARAMETER = "csvOutputLocation";
    private static final String PROJECT_LOCATION_PARAMETER = "projectLocation";
    private static final String SCRIPT_LOCATION_PARAMETER = "scriptLocation";
    private static final String TOP_NODE_PARAMETER = "topNode";

    private static final org.apache.commons.logging.Log logger = LogFactory.getLog(RunExportScriptWrapper.class);

    public static void main(String[] args) throws BSFException {
        try {

            Options options = new Options();
            OptionBuilder.isRequired(true);

            options.addOption(CSV_LOCATION_PARAMETER, true, "the final location of the csv file");
            options.addOption(PROJECT_LOCATION_PARAMETER, true, "the location of the project (as a .pprj file) to use");
            options.addOption(SCRIPT_LOCATION_PARAMETER, true, "the script location ");
            options.addOption(TOP_NODE_PARAMETER, true, "the name of the top node");
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse(options, args);

            final ArrayList errors = new ArrayList();
            final Project project = Project.loadProjectFromFile(cmd.getOptionValue(PROJECT_LOCATION_PARAMETER), errors);
            if (!errors.isEmpty()) {
                for (Object error : errors) {
                    logger.error(error);
                }
                return;
            }

            ExportScriptWrapper wrapper = new ExportScriptWrapper(project, cmd.getOptionValue(SCRIPT_LOCATION_PARAMETER));
            wrapper.exportToFile(cmd.getOptionValue(CSV_LOCATION_PARAMETER), cmd.getOptionValue(TOP_NODE_PARAMETER));

        } catch (Exception e) {
            logger.error("caught when trying to run script in standalone mode with arguments " + Arrays.asList(args), e);
        }

    }


}
