package edu.stanford.bmir.protege.icd.export.server;
/**
 * @author Jack Elliott <jacke@stanford.edu>
 */

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteServerProject;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.SystemUtilities;
import junit.framework.TestCase;

import java.rmi.Naming;
import java.util.ArrayList;

public class ExportICDClassesJobTest extends TestCase {
    public void testBasicCase() throws Exception {
        final Project project = Project.loadProjectFromFile("pprj/icd_umbrella.pprj", new ArrayList());
        ExportICDClassesJob unit = new ExportICDClassesJob(project.getKnowledgeBase(), "http://who.int/icd#Class_2635", "output/serverside-job.xls");
        unit.run();
    }

    public void uncommentMeIfYouWantToTestTheServertestExecuteOnServer() throws Exception {
        RemoteServer server = (RemoteServer) Naming.lookup("//localhost/" + Server.getBoundName());
        RemoteSession session = server.openSession("Guest", SystemUtilities.getMachineIpAddress(), "guest");
        RemoteServerProject serverProject = server.openProject("ICD", session);
        final Project project = RemoteClientProject.createProject(server, serverProject, session, false);
        ExportICDClassesJob unit = new ExportICDClassesJob(project.getKnowledgeBase(), "http://who.int/icd#Class_2635", "output/serverside-job.xls");
        unit.execute();
    }
}