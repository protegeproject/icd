package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.annotation.api.Comment;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

public class CreateSnomedAnnotations {

    public static void main(String[] args) {
        Project prj = Project.loadProjectFromFile(
                "/work/protege/projects/icd/content_model/icd_int/annotation_ICD.pprj", new ArrayList());
        KnowledgeBase chao = prj.getKnowledgeBase();
        OntologyComponentFactory ocF = new OntologyComponentFactory(chao);
        AnnotationFactory acF = new AnnotationFactory(chao);

        File file = new File("/home/ttania/Desktop/annotation_snomed_mappings.csv");

        try {
            //use buffering, reading one line at a time
            //FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(file));
            try {
                String line = null; //not declared within while loop
                /*
                * readLine is a bit quirky :
                * it returns the content of a line MINUS the newline.
                * it returns null only for the END of the stream.
                * it returns an empty String if two newlines appear in a row.
                */
                while ((line = input.readLine()) != null) {
                    if (line != null) {
                        try {
                            System.out.println(line);
                            String[] split = line.split(";");
                            String entity = split[0];
                            String comment = split[1];
                            Ontology_Component oc = ServerChangesUtil.getOntologyComponent(chao, entity);
                            if (oc == null) {
                                oc = ocF.createOntology_Class(null);
                                oc.setCurrentName(entity);
                            }
                            Comment c = acF.createComment(null);
                            c.setAuthor("SNOMED Mapper");
                            c.setBody(comment);
                            c.setSubject(comment);
                            c.setCreated(DefaultTimestamp.getTimestamp(chao));
                            c.setAnnotates(CollectionUtilities.createCollection(oc));
                        } catch (Exception e) {
                            e.printStackTrace();
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

    }

}
