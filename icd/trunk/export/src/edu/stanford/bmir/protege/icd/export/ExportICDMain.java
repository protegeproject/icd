package edu.stanford.bmir.protege.icd.export;

import edu.stanford.smi.protege.model.Project;
import org.apache.bsf.BSFException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ExportICDMain {
    private static final String EXCEL_LOCATION_PARAMETER = "csvOutputLocation";
    private static final String PROJECT_LOCATION_PARAMETER = "projectLocation";
    private static final String TOP_NODE_PARAMETER = "topNode";

    private static final org.apache.commons.logging.Log logger = LogFactory.getLog(ExportICDMain.class);

    public static void main(String[] args) throws BSFException {
        try {

            CommandLine cmd = parseArguments(args);

            final ArrayList errors = new ArrayList();
            final Project project = Project.loadProjectFromFile(cmd.getOptionValue(PROJECT_LOCATION_PARAMETER), errors);
            if (!errors.isEmpty()) {
                for (Object error : errors) {
                    logger.error(error);
                }
                return;
            }

            ExportICDClassesJob job = new ExportICDClassesJob(project.getKnowledgeBase(), cmd.getOptionValue(EXCEL_LOCATION_PARAMETER), cmd.getOptionValue(TOP_NODE_PARAMETER).split(","));
            job.run();

        } catch (Exception e) {
            logger.error("caught when trying to run script in standalone mode with arguments " + Arrays.asList(args), e);
        }

    }

    private static CommandLine parseArguments(String[] args) throws ParseException {
        Options options = new Options();
        OptionBuilder.isRequired(true);

        options.addOption(EXCEL_LOCATION_PARAMETER, true, "the final location of the excel file");
        options.addOption(PROJECT_LOCATION_PARAMETER, true, "the location of the project (as a .pprj file) to use");
        options.addOption(TOP_NODE_PARAMETER, true, "the name of the top node");
        CommandLineParser parser = new PosixParser();
        return parser.parse(options, args);
    }


}
