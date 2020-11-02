package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

//TODO: Uncomment method to delete existing display status!!!! It's commented only for testing

public class SetReleaseStatus {

    private static OWLModel owlModel;
    private static ICDContentModel cm;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Needs 2 params: ICD pprj file and CSV file with released entities");
            return;
        }

        String fileName = args[0];

        List errors = new ArrayList();
        Project prj = Project.loadProjectFromFile(fileName , errors);
        if (errors.size() > 0) {
            System.out.println("There were errors at loading project: " + fileName);
            System.exit(1);
        }

        owlModel = (OWLModel) prj.getKnowledgeBase();
        cm = new ICDContentModel(owlModel);

        //removeDisplayStatus();
        setReleasedStatus(args[1]);
    }

    private static void removeDisplayStatus() {
    	Log.getLogger().info("Starting removing display status ...");
    	
    	//doing it only for ICD, the others don't have display status set
		Collection<RDFSNamedClass> clses = cm.getICDCategories();
		for (RDFSNamedClass cls : clses) {
			cm.setDisplayStatus(cls, null);
		}
	}

	private static void setReleasedStatus(String relEntitiesFileName) throws IOException {
		Log.getLogger().info("Starting setting release status ...");
		
    	BufferedReader csvReader = null;
		
		csvReader = new BufferedReader(new FileReader(relEntitiesFileName));
		
		int lineCount = 0;
		
		String row = null;
		try {
			while (( row = csvReader.readLine()) != null) {
				setReleased(row);
				if (lineCount % 1000 == 0) {
					Log.getLogger().info("Processed " + lineCount + " lines");
				}
				lineCount ++;
			}
		} catch (IOException e) {
			Log.getLogger().log(Level.SEVERE, "IO Exception at processing row: " + row, e);
		}
		csvReader.close();
    }

	private static void setReleased(String entityId) {
		RDFSNamedClass cls = owlModel.getRDFSNamedClass(entityId);
		if (cls == null) {
			Log.getLogger().warning("Could not find entity: " + entityId);
			return;
		}
		
		//cm.setDisplayStatus(cls, null);
		cm.setReleased(cls, true);
	}

}
