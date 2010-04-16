package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.Comment;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.server_changes.ChangesProject;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class ImportBRO {

    public static String NS = "http://bioontology.org/ontologies/BiomedicalResourceOntology.owl#";

    public static void main(String[] args) {
        Project prj = Project.loadProjectFromFile(
                "/work/protege/projects/bro/20100212/BRO_v2_7_1_v2.7.1.pprj", new ArrayList());

        OWLModel kb = (OWLModel) prj.getKnowledgeBase();

        KnowledgeBase chao = ChAOKbManager.getChAOKb(kb);
        OntologyComponentFactory ocF = new OntologyComponentFactory(chao);
        AnnotationFactory acF = new AnnotationFactory(chao);

        if (!ChangesProject.isInitialized(prj)) {
            ChangesProject.initialize(prj);
        }

        RDFProperty defProp = kb.getRDFProperty("http://bioontology.org/ontologies/biositemap.owl#definition");

        File file = new File("/home/ttania/Desktop/BRO_Terms_without_Defs.csv");

        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    if (line != null) {
                        try {
                            System.out.println(line);
                            String[] split = line.split("\t");
                            String clsName = unquote(split[0]);
                            String def = unquote(split[1]);
                           // String author = unquote(split[2]);
                          //  String source = unquote(split[3]);

                            RDFSNamedClass cls = kb.getRDFSNamedClass(NS + clsName);
                            if (cls == null) {
                                Log.getLogger().info("***& " + clsName + " not found");
                            } else {
                                 if (def == null || def.length() == 0 || def.equals("?")) {
                                     Log.getLogger().info("***& " + clsName + " no defintions");
                                 } else {
                                     String existingDef = (String) cls.getPropertyValue(defProp);
                                     if (existingDef != null && existingDef.trim().length() == 0) {
                                         cls.setPropertyValue(defProp, def);
                                     } else {
                                         cls.addPropertyValue(defProp, def);
                                     }

                                     Ontology_Component oc = ServerChangesUtil.getOntologyComponentFromChao(chao, NS + clsName);
                                     if (oc == null) {
                                         oc = ocF.createOntology_Class(null);
                                         oc.setCurrentName(NS + clsName);
                                     }
                                     Comment c = acF.createComment(null);
                                     c.setAuthor("Automatic import");
                                     c.setBody("Added new defintion: " + def);
                                     c.setSubject("New defintion");
                                     c.setCreated(DefaultTimestamp.getTimestamp(chao));
                                     c.setAnnotates(CollectionUtilities.createCollection(oc));
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

        prj.save(new ArrayList());
        chao.getProject().save(new ArrayList());

    }

    private static String unquote(String def) {
       if (def == null) {
           return null;
       }
       if (def.startsWith("\"") && def.endsWith("\"")) {
           return def.substring(1, def.length() - 1);
       }
       return def;
    }
}
