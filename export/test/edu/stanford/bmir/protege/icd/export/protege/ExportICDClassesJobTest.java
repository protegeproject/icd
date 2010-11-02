package edu.stanford.bmir.protege.icd.export.protege;
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
        System.setProperty(ExportICDClassesJob.PYTHON_HOME_PROPERTY, "lib/");
        final Project project = Project.loadProjectFromFile("pprj/icd_umbrella.pprj", new ArrayList());
        ExportICDClassesJob unit = new ExportICDClassesJob(project.getKnowledgeBase(), "output/serverside-job.xls", "http://who.int/icd#Class_2635");
        unit.run();
    }

    public void uncommentMeIfYouWantToTestTheServertestExecuteOnServer() throws Exception {
        RemoteServer server = (RemoteServer) Naming.lookup("//localhost/" + Server.getBoundName());
        RemoteSession session = server.openSession("Guest", SystemUtilities.getMachineIpAddress(), "guest");
        RemoteServerProject serverProject = server.openProject("ICD", session);
        final Project project = RemoteClientProject.createProject(server, serverProject, session, false);
        ExportICDClassesJob unit = new ExportICDClassesJob(project.getKnowledgeBase(), "output/serverside-job.xls", "http://who.int/icd#Class_2635");
        unit.execute();
    }

    public void uncommentMeToExportTheseChapterstestExportChapters12And2() throws Exception {
        RemoteServer server = (RemoteServer) Naming.lookup("//localhost/" + Server.getBoundName());
        RemoteSession session = server.openSession("Guest", SystemUtilities.getMachineIpAddress(), "guest");
        RemoteServerProject serverProject = server.openProject("ICD", session);
        final Project project = RemoteClientProject.createProject(server, serverProject, session, false);
        ExportICDClassesJob unit = new ExportICDClassesJob(project.getKnowledgeBase(), "output/II.xls", "http://who.int/icd#II");
        String fileName = (String) unit.execute();
        System.out.println("fileName = " + fileName);
        unit = new ExportICDClassesJob(project.getKnowledgeBase(), "output/XII.xls", "http://who.int/icd#XII");
        fileName = (String) unit.execute();
        System.out.println("fileName = " + fileName);
    }
}