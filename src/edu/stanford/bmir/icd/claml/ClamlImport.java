package edu.stanford.bmir.icd.claml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ClamlImport {
    private static transient Logger log = Log.getLogger(ClamlImport.class);

    private OWLModel owlModel;
    private ICDContentModel icdContentModel;

    /**
     * This is just an example on how to use the importer programatically
     * @param args - no args needed
     */
    public static void main(String[] args) {
        File file = new File("/tmp/icd10_claml.xml");

        //load into a file that has the empty content model in it
        Project prj = Project.loadProjectFromFile("/tmp/icd_content_model_empty.pprj", new ArrayList());
        OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();

        ClamlImport ci = new ClamlImport(owlModel);
        ci.doImport(file);

        log.info("Started saving of OWL file on " + new Date());
        long t0 = System.currentTimeMillis();

        prj.save(new ArrayList());

        log.info("Finished saving OWL file in " + ((System.currentTimeMillis() - t0) / 1000) + " seconds");

    }

    public ClamlImport(OWLModel owlModel) {
        this.owlModel = owlModel;
        this.icdContentModel = new ICDContentModel(this.owlModel);
    }

    /**
     * Method that does the actual import of the clamlFile given as argument
     * @param clamlFile - the CLAML file to import
     */
    public void doImport(File clamlFile) {
        boolean generateEventsEnabled = owlModel.getGenerateEventsEnabled();
        owlModel.setGenerateEventsEnabled(false);
        try {
            log.info("Started importing of CLAML file: " + clamlFile.getAbsolutePath() + " on " + new Date());
            long t0 = System.currentTimeMillis();

            parse(clamlFile);

            log.info("Finished importing CLAML file in " + ((System.currentTimeMillis() - t0) / 1000) + " seconds");
        } finally {
            owlModel.setGenerateEventsEnabled(generateEventsEnabled);
        }
    }

    private void parse(File file) {
        SAXBuilder builder = new SAXBuilder();
        try {
            Document doc = builder.build(file);
            Element root = doc.getRootElement();
            for (Iterator iterator = root.getChildren(ClamlConstants.CLASS_ELEMENT).iterator(); iterator.hasNext();) {
                Element el = (Element) iterator.next();
                parseElement(el);
            }
        } catch (JDOMException e) {
            log.log(Level.SEVERE, "Error at parsing CLAML file: " + e.getMessage(), e);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Could not open CLAML file: " + file.getAbsolutePath() + " Error message:"
                    + e.getMessage(), e);
        }
    }

    private void parseElement(Element el) {
        RDFSNamedClass cls = parseSubclasses(el);
        parseRubrics(cls, el);
    }

    private RDFSNamedClass parseSubclasses(Element el) {
        RDFSNamedClass cls = null;
        String code = el.getAttributeValue(ClamlConstants.CODE_ATTR);
        List classChildren = el.getChildren(ClamlConstants.SUPERCLASS_ELEMENT);
        if (classChildren.isEmpty()) {
            cls = icdContentModel.createICDCategory(code, (String)null);
            if (log.isLoggable(Level.FINE)) {
                log.fine("Created cls: " + cls.getBrowserText() + " superclass: " + cls.getSuperclasses(false));
            }
        } else {
            for (Iterator iterator = classChildren.iterator(); iterator.hasNext();) {
                Element classChild = (Element) iterator.next(); // TODO: treat
                // multiple
                // parents
                String supercls = classChild.getAttributeValue(ClamlConstants.CODE_ATTR);
                cls = icdContentModel.createICDCategory(code, supercls);
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Created cls: " + cls.getBrowserText() + " superclass: " + cls.getSuperclasses(false));
                }
            }
        }
        if (cls != null) {
            icdContentModel.addClassMetadata(cls, code, el.getAttributeValue(ClamlConstants.KIND_ATTR), el
                    .getAttributeValue(ClamlConstants.USAGE_ATTR));
        }

        return cls;
    }

    private void parseRubrics(RDFSNamedClass cls, Element el) {
        List rubricChildren = el.getChildren(ClamlConstants.RUBRIC_ELEMENT);
        for (Iterator iterator = rubricChildren.iterator(); iterator.hasNext();) {
            Element rubricChild = (Element) iterator.next();
            parseRubric(cls, rubricChild);
        }
    }

    private void parseRubric(RDFSNamedClass cls, Element rubricChild) {
        String id = rubricChild.getAttributeValue(ClamlConstants.ID_ATTR);
        String kind = rubricChild.getAttributeValue(ClamlConstants.KIND_ATTR);
        Element labelElement = rubricChild.getChild(ClamlConstants.LABEL_ELEMENT);
        if (kind.equals(ClamlConstants.RUBRIC_KIND_PREFFERD_ATTR)) {
            parsePreferred(cls, id, labelElement);
            // parsePreferred2(cls, id, labelElement); //preferred is already
            // translated as icdTitle
        } else if (kind.equals(ClamlConstants.RUBRIC_KIND_INCLUSION_ATTR)) {
            parseInclusion(cls, id, labelElement);
        } else if (kind.equals(ClamlConstants.RUBRIC_KIND_EXCLUSION_ATTR)) {
            parseExclusion(cls, id, labelElement);
        } else if (kind.equals(ClamlConstants.RUBRIC_KIND_CODING_HINT_ATTR)) {
            parseCodingHint(cls, id, labelElement);
        } else if (kind.equals(ClamlConstants.RUBRIC_KIND_INTRODUCTION_ATTR)) {
            parseIntroduction(cls, id, labelElement);
        } else if (kind.equals(ClamlConstants.RUBRIC_KIND_NOTE_ATTR)) {
            parseNote(cls, id, labelElement);
        } else if (kind.equals(ClamlConstants.RUBRIC_KIND_PREFFERD_LONG_ATTR)) {
            parsePreferredLong(cls, id, labelElement);
        } else if (kind.equals(ClamlConstants.RUBRIC_KIND_DEFINITION_ATTR)) {
            parseDefinition(cls, id, labelElement);
        }
    }

    private void parseDefinition(RDFSNamedClass cls, String id, Element labelElement) {
        RDFResource term = icdContentModel.createDefinitionTerm();
        parseLabel(cls, term, id, labelElement);
        icdContentModel.addDefinitionTermToClass(cls, term);
    }

    private void parsePreferred(RDFSNamedClass cls, String id, Element labelElement) {
        RDFResource term = icdContentModel.createTitleTerm();
        parseLabel(cls, term, id, labelElement);
        icdContentModel.addTitleTermToClass(cls, term);
        //add also the rdfs:label as the code + title for BioPortal
        icdContentModel.addRdfsLabel(cls);
    }

    private void parseInclusion(RDFSNamedClass cls, String id, Element labelElement) {
        RDFResource term = icdContentModel.createInclusionTerm();
        parseLabel(cls, term, id, labelElement);
        icdContentModel.addInclusionTermToClass(cls, term);
    }

    private void parseExclusion(RDFSNamedClass cls, String id, Element labelElement) {
        RDFResource term = icdContentModel.createExclusionTerm();
        parseLabel(cls, term, id, labelElement);
        icdContentModel.addExclusionTermToClass(cls, term);
    }

    private void parseCodingHint(RDFSNamedClass cls, String id, Element labelElement) {
        RDFResource term = icdContentModel.createICD10NotesTerm();
        parseLabel(cls, term, id, labelElement);
        icdContentModel.addCodingHintToClass(cls, term);
    }

    private void parseIntroduction(RDFSNamedClass cls, String id, Element labelElement) {
        RDFResource term = icdContentModel.createICD10NotesTerm();
        parseLabel(cls, term, id, labelElement);
        icdContentModel.addIntroductionToClass(cls, term);
    }

    private void parseNote(RDFSNamedClass cls, String id, Element labelElement) {
        RDFResource term = icdContentModel.createICD10NotesTerm();
        parseLabel(cls, term, id, labelElement);
        icdContentModel.addNotesToClass(cls, term);
    }

    private void parsePreferred2(RDFSNamedClass cls, String id, Element labelElement) {
        RDFResource term = icdContentModel.createICD10NotesTerm();
        parseLabel(cls, term, id, labelElement);
        icdContentModel.addPreferredToClass(cls, term);
    }

    private void parsePreferredLong(RDFSNamedClass cls, String id, Element labelElement) {
        RDFResource term = icdContentModel.createICD10NotesTerm();
        parseLabel(cls, term, id, labelElement);
        icdContentModel.addPreferredLongToClass(cls, term);
    }

    private void parseLabel(RDFSNamedClass cls, RDFResource term, String id, Element labelElement) {
        String label = labelElement.getTextTrim();
        String lang = labelElement.getAttributeValue(ClamlConstants.XML_LANG, Namespace.XML_NAMESPACE);
        icdContentModel.fillTerm(term, id, label, lang);
        List cl = labelElement.getChildren(ClamlConstants.REFERENCE_ELEMENT);
        for (Iterator iterator = cl.iterator(); iterator.hasNext();) {
            Object next = iterator.next();
            if (next instanceof Element) {
                Element refElement = (Element) next;
                parseRefElement(term, refElement);
            }
        }
        //add rdfs:label to terms for BioPortal
        icdContentModel.addRdfsLabelToTerm(term, label, lang);
    }

    private void parseRefElement(RDFResource term, Element refElement) {
        String code = refElement.getAttributeValue(ClamlConstants.CODE_ATTR);
        String usage = refElement.getAttributeValue(ClamlConstants.USAGE_ATTR);
        String text = refElement.getTextTrim();
        RDFResource ref = icdContentModel.createClamlReference();
        icdContentModel.fillClamlReference(ref, text, usage, code);
        icdContentModel.addClamlRefToTerm(term, ref);
    }

}
