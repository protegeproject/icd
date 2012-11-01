package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ImportRareDiseases {
	//file = "/work/protege/projects/icd/content_model/icd_int/icd_mysql/icd_umbrella.pprj"
	//csv = "/work/protege/projects/icd/content_model/icd_int/2010.09.23_Defs_to_import/additionalDefinitions.csv"

	private final static String DEF = "Definition";
	private final static String SYN = "Synonym";
	//private final static String PREFILLED = "prefilled";

	private final static String IMPORT_MODE = DEF;
	private static final String ORPHANET = "orpha";

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Needs 2 params: ICD pprj file and CSV file");
			return;
		}

		String fileName = args[0];
		String csvFile = args[1];

		Project prj = Project.loadProjectFromFile(fileName , new ArrayList());

		OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();
		ICDContentModel cm = new ICDContentModel(owlModel);

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
							String[] split = line.split("\\|");
							if (split.length != 5) {
								System.out.println("Wrong number of element in (expected 5): " + line);
								continue;
							}
							String orphCode = split[0];
							orphCode=removeQuotesIfNecessary(orphCode); //remove quotes

							String orphName = split[1];
							orphName=removeQuotesIfNecessary(orphName); //remove quotes

							String clsName = split[2];
							clsName=removeQuotesIfNecessary(clsName); //remove quotes

							String icdTitle = split[3];
							icdTitle=removeQuotesIfNecessary(icdTitle); //remove quotes

							String icdDefinition = split[4];
							icdDefinition=removeQuotesIfNecessary(icdDefinition); //remove quotes

							RDFSNamedClass cls = owlModel.getRDFSNamedClass(/*ICDContentModelConstants.NS + */clsName);

							if (cls == null) {
								Log.getLogger().info("***& " + clsName + " not found");
							} else {

								try {
									owlModel.beginTransaction("Automatic import of Rare Disease definition", cls.getName());

									RDFResource term = null;
									boolean registerAsAlternative = false;
									if (IMPORT_MODE.equals(DEF)) {
										RDFResource existingDefTerm = (RDFResource) cls.getPropertyValue(cm.getDefinitionProperty());
										if (existingDefTerm != null) {
											String existingDef = (String) existingDefTerm.getPropertyValue(cm.getLabelProperty());
											if (existingDef == null || existingDef.length() == 0) {
												Log.getLogger().warning(" 000 Empty defintion: " + cls + " .Remove it." );
												cls.removePropertyValue(cm.getDefinitionProperty(), existingDefTerm);
												term = cm.createDefinitionTerm();
											} else {
												Log.getLogger().warning(" +++ Existing definition: " + cls);
												if (existingDef.equals(icdDefinition)) {
													term = null;	//we don't need to create a new term
												}
												else {
													term = cm.createExternalDefinitionTerm();
													registerAsAlternative = true;
												}
											}
										} else{
											term = cm.createDefinitionTerm();
										}
									} else if (IMPORT_MODE.equals(SYN)) {
										term = cm.createSynonymTerm();
									}

									if (term != null) {
										cm.fillTerm(term, null, icdDefinition, "en", ORPHANET);

										if (IMPORT_MODE.equals(DEF)) {
											if (registerAsAlternative) {
												cm.addPrefilledDefinitionTermToClass(cls, term);
											}
											else {
												//cm.addPrefilledDefinitionTermToClass(cls, term);
												cm.addDefinitionTermToClass(cls, term);
											}
										} else if (IMPORT_MODE.equals(SYN)) {
											cm.addSynonymTermToClass(cls, term);
										}

									} else {
										Log.getLogger().info("*** Did not create term for " + clsName);
									}
									
									owlModel.commitTransaction();
								} catch (Exception e) {
									Log.getLogger().log(Level.WARNING, "Error at import of " + cls + " " + cls.getBrowserText(), e);
									owlModel.rollbackTransaction();
								}
							}

						} catch (Exception e) {
							Log.getLogger().log(Level.WARNING, "Error at import of " + line, e);
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

	private static String removeQuotesIfNecessary(String str) {
		String ret = str;
		if (str.startsWith("\"") && str.endsWith("\"")) {
			ret = str.substring(1, str.length() - 1);
		}
		return ret.trim();
	}
}
