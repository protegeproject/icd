package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

public class DeleteRetiredClasses {
	private static transient Logger log = Log.getLogger(DeleteRetiredClasses.class);

	private static final String COL_SEPARATOR = "\t";
	private static final int CLASS_COUNT_FOR_STATUS_NOTIFICATION = 10;
	private static int currClassCount = 0;
	
	public static void main(String[] args) {
        if (args.length != 2) {
            log.info("Expected two arguments: pprj_file_name  file_containing_list_of_classes_to_be_deleted");
            return;
        }

        long startTime = System.currentTimeMillis();
        
        Collection<?> errors = new ArrayList<Object>();
        Project prj = Project.loadProjectFromFile(args[0], errors);

        if (errors != null) {
            ProjectManager.getProjectManager().displayErrors("Errors", errors);
        }

        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();

        List<String> classes = readList(args[1]);
        deleteClasses(owlModel, classes);
        
        long timeElapsed = System.currentTimeMillis() - startTime;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        log.info(String.format("Deleted % d classes. Total time: %d mins %.3f seconds (or %s)", 
        		currClassCount,
        		timeElapsed / (60*1000), timeElapsed / 1000.0,
        		sdf.format(timeElapsed)));
	}

	
	private static List<String> readList(String csvFile) {
		BufferedReader csvReader = null;
		
		try {
			csvReader = new BufferedReader(new FileReader(csvFile));
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		List<String> classesToBeDeleted = new ArrayList<String>();
		
		String row = null;
		try {
			while (( row = csvReader.readLine()) != null) {
				processLine(row, classesToBeDeleted);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "IO Exception at processing FMA row: " + row, e);
		}

		try {
			csvReader.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		return classesToBeDeleted;
	}

	
	private static void processLine(String row, List<String> list) {
		String[] data = row.split(COL_SEPARATOR);
		
		//we ignore anything that is not in the first column
		list.add(data[0]);
	}

	
	private static void deleteClasses(OWLModel owlModel, List<String> classes) {
		
		boolean genEvents = owlModel.setGenerateEventsEnabled(false);
		
		try {
			List<OWLClass> owlClasses = new LinkedList<OWLClass>();
			for (String className : classes) {
				OWLNamedClass owlClass = owlModel.getOWLNamedClass(className);
				if (owlClass == null) {
					log.log(Level.WARNING,className + " not found. Cannot be deleted.");
					//System.out.println(className + " not found. Cannot be deleted.");
				}
				else {
					owlClasses.add(owlClass);
				}
			}
			
			while (! owlClasses.isEmpty()) {
				OWLClass owlClass = owlClasses.get(0);
				deleteClass(owlModel, owlClass, owlClasses);
			}
		}
		finally {
			owlModel.setGenerateEventsEnabled(genEvents);
		}
	}


	@SuppressWarnings("unchecked")
	private static void deleteClass(OWLModel owlModel, OWLClass owlClass, List<OWLClass> owlClasses) {
		owlClasses.remove(owlClass);
		if (owlClass.isDeleted()) {
			return;
		}
		
		Collection<OWLClass> directSubclasses = (Collection<OWLClass>) owlClass.getSubclasses(false);
		
		Iterator<OWLClass> it = directSubclasses.iterator();
		while (it.hasNext()) {
			OWLClass subclass = it.next();
			if (owlClasses.contains(subclass)) {
				deleteClass(owlModel, subclass, owlClasses);
			}
		}
		
		try {
			directSubclasses = (Collection<OWLClass>) owlClass.getSubclasses(false);
			
			if (directSubclasses.isEmpty()) {
				log.info("Deleting class " + owlClass + " (" + owlClass.getBrowserText() + ")");
				owlClass.delete();
			}
			else {
				directSubclasses.stream().forEach((c) -> removeParentRelation(c, owlClass));
			}
	
			if (++currClassCount % CLASS_COUNT_FOR_STATUS_NOTIFICATION == 0) {
				log.info(String.format("Deleted: %d. %d to go.", currClassCount, owlClasses.size()));
			}
		}
		catch (Throwable e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}


	private static void removeParentRelation(OWLClass child, OWLClass parent) {
		child.removeSuperclass(parent);
		log.info("Removing superclass " + parent + " (" + parent.getBrowserText() + ")" +
				" of class " + child + " (" + child.getBrowserText() + ")");
		//System.out.println("Removing superclass " + parent + " (" + parent.getBrowserText() + ")" +
		//		" of class " + child + " (" + child.getBrowserText() + ")");
	}


}
