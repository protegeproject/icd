package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAnnotation;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.change.api.ChangeFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.PostProcessorManager;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

/**
 * This script imports definitions for ICD entities 
 * and optionally adds comments to those definitions. <br><br>
 * The script requires 2 inputs: an icd_umbrella Protege project file,
 * and a CSV file where the fields are separated by the '|' character
 * (since definitions can often contain other common separators like comma and semicolon).<br><br>
 * The structure of the CSV file should be as follows:<br>
 * icat_internal_id|definition[|comment]<br>
 * The script takes other optional arguments, such as:<br>
 * <i>-h</i> or <i>--help</i> displays the usage massage
 * <i>-f</i> forced setting of the definitions, i.e. overrides existing values
 * <i>-nt=AnnotationType</i> AnnotationType can be "Comment", "SeeAlso", or any other 
 * valid annotation type in ChAO, such as "Reference" for ICD.
 * 
 * @author csnyulas
 *
 */
public class ImportDefinitions {

	private static final String FORCE_FLAG = "-f";
	private static final String NOTETYPE_OPTION = "-nt=";
	private static final String HELP_OPTION = "-h";
	private static final String HELP_OPTION_LONG = "--help";
	
	private static final String NOTETYPE_COMMENT = "Comment";
	
	private static boolean isForceOptionSet = false;
	private static boolean isHelpOptionSet = false;
	private static String noteType = NOTETYPE_COMMENT;

	//file = "/work/protege/projects/icd/content_model/icd_int/icd_mysql/icd_umbrella.pprj"
    //csv = "/work/protege/projects/icd/content_model/icd_int/2010.09.23_Defs_to_import/additionalDefinitions.csv"


    public static void main(String[] args) {
        if (getNonOptioArgCount(args) < 2) {
            usage();
            return;
        }

        String fileName = getNonOptionArgument(args, 0);
        String csvFile = getNonOptionArgument(args, 1);
        initializeOptionFlags(args);

        if (isHelpOptionSet) {
            usage();
            return;
        }

        Log.getLogger().info("Running import definitions script with the following options:\n" + 
        		formatArguments(args));
        
        Project prj = Project.loadProjectFromFile(fileName , new ArrayList<String>());

        //------ init chao -------
        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();
        ICDContentModel cm = new ICDContentModel(owlModel);

		KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(owlModel);
		if (chaoKb == null) {
			Log.getLogger().info("Could not find ChAO for: " + owlModel);
			return;
		}

		//Log.getLogger().info("Starting writing to ChAO from: " + chaoKb.getProject().getProjectURI() + " on " + new Date());

		ChangesProject.initialize(owlModel.getProject());

		OntologyComponentFactory ocFactory = new OntologyComponentFactory(chaoKb);
		GenericAnnotationFactory annFactory = new GenericAnnotationFactory(chaoKb);
		ChangeFactory changeFactory = new ChangeFactory(chaoKb);
		PostProcessorManager changes_db = ChangesProject.getPostProcessorManager(owlModel);
        //------ end init chao -------

        
        //TODO - cols should be split by "|"
        File file = new File(csvFile);

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    if (line != null) {
                        try {
                            System.out.println(line);
                            String[] split = line.split("\\|", 3);
                            if (split.length < 2) {
                            	csvFormatMessage();
                            	return;
                            }
                            String clsName = removeQuotesIfNecessary(split[0]);
                            String value = removeQuotesIfNecessary(split[1]);
                            String comment = (split.length < 3 ? null : removeQuotesIfNecessary(split[2]));

                            RDFSNamedClass cls = owlModel.getRDFSNamedClass(clsName);
                            if (cls == null) {
                                Log.getLogger().info("***& " + clsName + " not found");
                            } else {
                                RDFResource term = null;
                            	String changeDesc = null;
                                RDFResource existingDefTerm = (RDFResource) cls.getPropertyValue(cm.getDefinitionProperty());
                                if (existingDefTerm != null) {
                                    String existingDef = (String) existingDefTerm.getPropertyValue(cm.getLabelProperty());
                                    if (existingDef == null || existingDef.length() == 0) {
                                        Log.getLogger().warning(" 000 Empty defintion: " + cls + " .Remove it." );
                                        cls.removePropertyValue(cm.getDefinitionProperty(), existingDefTerm);
                                        term = cm.createDefinitionTerm();
                                    } else {
                                        Log.getLogger().warning(" +++ Existing definition: " + cls);
                                        if (isForceOptionSet && !existingDef.equals(value)) {
                                        	Log.getLogger().info("Replacing definition:\n" +
                                        			existingDef +"\nwith\n" + value);
                                        	changeDesc = "Replacing definition. Old value: '" + 
                                        			existingDef + "'. New value: '" + value + "'";
                                        	existingDefTerm.setPropertyValue(cm.getLabelProperty(), value);
                                        	term = existingDefTerm;
                                        }
                                    }
                                } else{
                                    term = cm.createDefinitionTerm();
                                }

                                if (term != null) {
                                	if (term != existingDefTerm) {
	                                    cm.fillTerm(term, null, value, "en", null);
	                                    cm.addDefinitionTermToClass(cls, term);
	                                    changeDesc = "Added new definition: " + value;
                                	}

									addNote(chaoKb, ocFactory, annFactory, changeFactory, changes_db, changeDesc, cls, term, comment);
                                } else {
                                    Log.getLogger().info("*** Did not create term for " + clsName);
                                }
                            }

                        } catch (Exception e) {
                            Log.getLogger().log(Level.WARNING, "Error at import", e);
                        }

                    }
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //prj.save(new ArrayList());

    }


	private static int getNonOptioArgCount(String[] args) {
		int argCnt = args.length;
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				argCnt--;
			}
		}
		return argCnt;
	}

	private static String getNonOptionArgument(String[] args, int index) {
		int currInd = -1;
		for (int i = 0; i < args.length; i++) {
			if ( ! args[i].startsWith("-")) {
				currInd++;
			}
			if (currInd == index) {
				return args[i];
			}
		}
		return null;
	}


	private static void initializeOptionFlags(String[] args) {
		for(int i = 0; i < args.length; i++) {
			if (args[i].equals(FORCE_FLAG)) {
				isForceOptionSet = true;
			}
			if ( args[i].equals(HELP_OPTION) ||
					args[i].equals(HELP_OPTION_LONG) ) {
				isHelpOptionSet = true;
			}
			else if (args[i].startsWith(NOTETYPE_OPTION)) {
				noteType = args[i].substring(NOTETYPE_OPTION.length());
			}
		}
	}

	private static void usage() {
		Log.getLogger().info("Usage: ImportDefinitions [-f] [-nt=Comment|Explanation|SeeAlso|Reference|etc] ICD_pprj_file CSV_file" +
		"\n" +
		"\nNote: If option -f is specified, existing definition will be overwritten.");
	}

	private static String formatArguments(String[] args) {
		String res = "";
		for (String arg : args) {
			res += arg + "\n";
		}
		return res;
	}
    

	private static void csvFormatMessage() {
		Log.getLogger().info("The lines in the CSV file should follow the following format:" +
		"\n" +
		"icat_internal_id|definition[|comment]");
	}

	private static String removeQuotesIfNecessary(String str) {
		String ret = str.trim();
		if (str.startsWith("\"") && str.endsWith("\"")) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret.trim();
	}

	private static void addNote(KnowledgeBase chaoKb, OntologyComponentFactory ocFactory, 
			GenericAnnotationFactory annFactory, ChangeFactory changeFactory,
			PostProcessorManager changes_db, String changeDesc, RDFResource cls, RDFResource term, String comment) {
		if (changeDesc != null) {
	        Ontology_Component oc = ServerChangesUtil.getOntologyComponentFromChao(chaoKb, term.getName());
	        if (oc == null) {
	            oc = ocFactory.createOntology_Individual(null);
	            oc.setCurrentName(term.getName());
	        }

	        Annotation ann = annFactory.createAnnotation(null, noteType);
	        ann.setAuthor("Automatic import");
	        ann.setBody(comment);
	        ann.setSubject(noteType); //Can we set a more appropriate value for the subject?
	        ann.setCreated(DefaultTimestamp.getTimestamp(chaoKb));
	        ann.setAnnotates(CollectionUtilities.createCollection(oc));
	        
			Change change = changeFactory.createComposite_Change(null);
			ServerChangesUtil.createChangeStd(changes_db, change, cls, "Automatic import: " + changeDesc);
			change.setAuthor("WHO");

		}
	}
	
	

	private static class GenericAnnotationFactory extends AnnotationFactory {

	    private KnowledgeBase kb;

	    public GenericAnnotationFactory(KnowledgeBase kb) {
	    	super(kb);
	        this.kb = kb;
	    }

	    public Cls getAnnotationClass(String type) {
	        return kb.getCls(type);
	    }
	
	    public Annotation createAnnotation(String name, String type) {
	        Cls cls = getAnnotationClass(type);
	        if (cls == null) {
	        	Log.getLogger().warning("Can't create note of type '" + type + "'. Comment will be created instead.");
	        	cls = getCommentClass(); // fallback option
	        }
	        Instance inst = cls.createDirectInstance(name);
	        return new DefaultAnnotation(inst);
	    }
	}
}
	