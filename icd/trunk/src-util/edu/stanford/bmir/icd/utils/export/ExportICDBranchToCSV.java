package edu.stanford.bmir.icd.utils.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ExportICDBranchToCSV {
	private final static Logger log = Log.getLogger(ExportICDBranchToCSV.class);
	
	private static final String COL_SEPARATOR = "\t";
	private static final String VALUE_SEPARATOR = "*";
	private static final String QUOTE = "\"";
	
    private static OWLModel owlModel;
    private static ICDContentModel cm;
    private static RDFSNamedClass topClass;
    private static BufferedWriter csvWriter;
   

    public static void main(String[] args) {
        if (args.length != 3) {
            log.severe("Needs 3 params: (1) ICD pprj file, (2) export CSV file, and (3) top class to export");
            return;
        }

        String fileName = args[0];

        List errors = new ArrayList();
        Project prj = Project.loadProjectFromFile(fileName , errors);
        if (errors.size() > 0) {
            log.severe("There were errors at loading project: " + fileName);
            System.exit(1);
        }

        owlModel = (OWLModel) prj.getKnowledgeBase();
        initICDCM();
        
        topClass = owlModel.getRDFSNamedClass(args[2]);
        if (topClass == null) {
        	log.severe("Could not find top class " + args[2]);
        	System.exit(1);
        }
        
        try {
        	csvWriter = new BufferedWriter(new FileWriter(args[1]));
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not write to CSV file location " + args[1]);
			System.exit(1);
		}
        
        SystemUtilities.logSystemInfo();
		log.info("\n===== Started export to CSV " + new Date());
		log.info("=== PPRJ File: " + args[0]);
		log.info("=== CSV file for output: " + args[1]);
		
		try {
			exportBranch();
			csvWriter.close();
		}
		catch (Throwable t) {
			log.log(Level.SEVERE, t.getMessage(), t);
		}
		
		//finish processing
		log.info("\n===== End export to Excel at " + new Date());
    }
    
    
    private static void initICDCM() {
    	cm = new ICDContentModel(owlModel);
    }
    
	private static void exportBranch() {
		//export top class first
		exportClass(topClass);
		
		//export children of top class
		Collection<RDFSNamedClass> classes = getClassesToExport();
		
		for (RDFSNamedClass owlClass : classes) {
			exportClass(owlClass);
		}
	}

	private static void exportClass(RDFSNamedClass owlClass) {
		String id = owlClass.getName();
		String title = cm.getTitleLabel(owlClass);
		String syns = getExportString(cm.getSynonymLabels(owlClass));
		String superclses = getSuperclsesExportString(owlClass);
		
		try {
        	csvWriter.write(toCsvField(id) + COL_SEPARATOR + toCsvField(title) + COL_SEPARATOR +
        			toCsvField(syns) + COL_SEPARATOR + toCsvField(superclses));
        	csvWriter.newLine();
    	} 
    	catch (IOException ioe) {
			log.severe("Could not export line for: " + owlClass);
		}
	}

	private static String getExportString(Collection<String> values) {
		StringBuffer s = new StringBuffer();
		for (String val : values) {
			s.append(val);
			s.append(VALUE_SEPARATOR);
		}
		//remove last value separator
		if (s.length() > 0) {
			s.delete(s.length()-VALUE_SEPARATOR.length(),s.length());
		}
		return s.toString();
	}

	private static String getSuperclsesExportString(RDFSNamedClass owlClass) {
		StringBuffer s = new StringBuffer();
		for (RDFSNamedClass superCls : (Collection<RDFSNamedClass>) owlClass.getSuperclasses(false)) {
			s.append(superCls.getName());
			s.append(VALUE_SEPARATOR);
		}
		//remove last value separator
		if (s.length() > 0) {
			s.delete(s.length()-VALUE_SEPARATOR.length(),s.length());
		}
		return s.toString();
	}
	
	
	private static Collection<RDFSNamedClass> getClassesToExport() {
		return topClass.getSubclasses(true);
	}

	
	private static String toCsvField(Object o) {
		String res = (o == null ? "" : o.toString());
		if (res.contains("\n") || res.contains(COL_SEPARATOR) || res.contains(VALUE_SEPARATOR) || res.contains(QUOTE)) {
			res = res.replaceAll(QUOTE, QUOTE + QUOTE);
			res = QUOTE + res + QUOTE;
		}
		return res;
	}
    
}
