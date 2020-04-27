package edu.stanford.bmir.icd.claml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ClamlImport {
	private static transient Logger log = Log.getLogger(ClamlImport.class);

	private OWLModel owlModel;
	private ICDContentModel cm;

	// it is only one, but we need it as a collection
	private Collection<String> topClsColl;

	private Map<RDFResource, String> termToRefCode = new HashMap<RDFResource, String>();
	private Map<RDFSNamedClass, List<String>> cls2superclsesNames = new HashMap<RDFSNamedClass, List<String>>();

	/**
	 * This is just an example on how to use the importer programmatically
	 * 
	 * @param args - no args needed
	 */
	public static void main(String[] args) {
		File file = new File("/tmp/icd10_claml.xml");

		// load into a file that has the empty content model in it
		Project prj = Project.loadProjectFromFile("/tmp/icd_content_model_empty.pprj", new ArrayList());
		OWLModel owlModel = (OWLModel) prj.getKnowledgeBase();

		ClamlImport ci = new ClamlImport(owlModel);
		ci.doImport(file, null);

		log.info("Started saving of OWL file on " + new Date());
		long t0 = System.currentTimeMillis();

		prj.save(new ArrayList());

		log.info("Finished saving OWL file in " + ((System.currentTimeMillis() - t0) / 1000) + " seconds");

	}

	public ClamlImport(OWLModel owlModel) {
		this.owlModel = owlModel;
		this.cm = new ICDContentModel(this.owlModel);
	}

	/**
	 * Method that does the actual import of the clamlFile given as argument
	 * 
	 * @param clamlFile - the CLAML file to import
	 * @param topCls    - top class to import under, null means owl:Thing
	 */
	public void doImport(File clamlFile, String topClsName) {

		this.topClsColl = getTopCls(topClsName);

		boolean generateEventsEnabled = owlModel.getGenerateEventsEnabled();
		owlModel.setGenerateEventsEnabled(false);

		try {
			log.info("Started importing of CLAML file: " + clamlFile.getAbsolutePath() + " on " + new Date());
			long t0 = System.currentTimeMillis();

			parse(clamlFile);
			postprocess();
			cleanup();

			log.info("Finished importing CLAML file in " + ((System.currentTimeMillis() - t0) / 1000) + " seconds");
		} finally {
			owlModel.setGenerateEventsEnabled(generateEventsEnabled);
		}
	}

	private Collection<String> getTopCls(String topClsName) {
		if (topClsName != null) {
			RDFSNamedClass cls = owlModel.getRDFSNamedClass(topClsName);
			if (cls != null) {
				return CollectionUtilities.createCollection(topClsName);
			}
		}
		return CollectionUtilities.createCollection(owlModel.getOWLThingClass().getName());
	}

	private void parse(File file) {
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(file);
			Element root = doc.getRootElement();

			// parse classes
			for (Iterator iterator = root.getChildren(ClamlConstants.CLASS_ELEMENT).iterator(); iterator.hasNext();) {
				Element el = (Element) iterator.next();
				parseElement(el);
			}

			// parse modifiers
			for (Iterator iterator = root.getChildren(ClamlConstants.MODIFIER_ELEMENT).iterator(); iterator.hasNext();) {
				Element el = (Element) iterator.next();
				parseElement(el);
			}

			// parse modifier classes
			for (Iterator iterator = root.getChildren(ClamlConstants.MODIFIER_CLASS_ELEMENT).iterator(); iterator
					.hasNext();) {
				Element el = (Element) iterator.next();
				parseElement(el);
			}

		} catch (JDOMException e) {
			log.log(Level.SEVERE, "Error at parsing CLAML file: " + e.getMessage(), e);
		} catch (IOException e) {
			log.log(Level.SEVERE,
					"Could not open CLAML file: " + file.getAbsolutePath() + " Error message:" + e.getMessage(), e);
		}
	}

	private void parseElement(Element el) {
		RDFSNamedClass cls = parseCls(el);
		parseRubrics(cls, el);
	}

	private RDFSNamedClass parseCls(Element el) {
		RDFSNamedClass cls = null;
		String code = el.getAttributeValue(ClamlConstants.CODE_ATTR);

		// create it under the top cls, fix parents in post-processing
		// this is needed to create the appropriate metaclasses
		cls = cm.createICDCategory(code, topClsColl);
		
		//TODO: this adds the icdCode, but it may not be the right one for all classifications
		cls.addPropertyValue(cm.getIcdCodeProperty(), code);

		List superClsElems = el.getChildren(ClamlConstants.SUPERCLASS_ELEMENT);

		List<String> superclsesNames = new ArrayList<>();

		for (Iterator iterator = superClsElems.iterator(); iterator.hasNext();) {
			Element classChild = (Element) iterator.next();
			String supercls = classChild.getAttributeValue(ClamlConstants.CODE_ATTR);
			superclsesNames.add(supercls);
		}

		cls2superclsesNames.put(cls, superclsesNames);

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
		RDFResource term = cm.createDefinitionTerm();
		parseLabel(cls, term, id, labelElement);
		cm.addDefinitionTermToClass(cls, term);
	}

	private void parsePreferred(RDFSNamedClass cls, String id, Element labelElement) {
		RDFResource term = cm.createTitleTerm();
		parseLabel(cls, term, id, labelElement);
		cm.addTitleTermToClass(cls, term);
		// add also the rdfs:label as the code + title for BioPortal
		cm.addRdfsLabel(cls);
	}

	private void parseInclusion(RDFSNamedClass cls, String id, Element labelElement) {
		RDFResource term = cm.createBaseInclusionTerm();
		parseLabel(cls, term, id, labelElement);
		cm.addBaseInclusionTermToClass(cls, term);
		// needs to be added also to baseIndex because of the current CM.. 
		// TT does not like this
		cm.addBaseIndexTermToClass(cls, term);
	}

	private void parseExclusion(RDFSNamedClass cls, String id, Element labelElement) {
		RDFResource term = cm.createBaseExclusionTerm();
		parseLabel(cls, term, id, labelElement); // make sure here that you also get the references
		cm.addBaseExclusionTermToClass(cls, term);
	}

	private void parseCodingHint(RDFSNamedClass cls, String id, Element labelElement) {
		RDFResource term = cm.createICD10NotesTerm();
		parseLabel(cls, term, id, labelElement);
		cm.addCodingHintToClass(cls, term);
	}

	private void parseIntroduction(RDFSNamedClass cls, String id, Element labelElement) {
		RDFResource term = cm.createICD10NotesTerm();
		parseLabel(cls, term, id, labelElement);
		cm.addIntroductionToClass(cls, term);
	}

	private void parseNote(RDFSNamedClass cls, String id, Element labelElement) {
		RDFResource term = cm.createICD10NotesTerm();
		parseLabel(cls, term, id, labelElement);
		cm.addNotesToClass(cls, term);
	}

	private void parsePreferred2(RDFSNamedClass cls, String id, Element labelElement) {
		RDFResource term = cm.createICD10NotesTerm();
		parseLabel(cls, term, id, labelElement);
		cm.addPreferredToClass(cls, term);
	}

	private void parsePreferredLong(RDFSNamedClass cls, String id, Element labelElement) {
		RDFResource term = cm.createICD10NotesTerm();
		parseLabel(cls, term, id, labelElement);
		cm.addPreferredLongToClass(cls, term);
	}

	private void parseLabel(RDFSNamedClass cls, RDFResource term, String id, Element labelElement) {
		String label = labelElement.getTextTrim();
		label = label.replace("()", "");

		String lang = labelElement.getAttributeValue(ClamlConstants.XML_LANG, Namespace.XML_NAMESPACE);
		cm.fillTerm(term, id, label, lang);
		
		List cl = labelElement.getChildren(ClamlConstants.REFERENCE_ELEMENT);
		for (Iterator iterator = cl.iterator(); iterator.hasNext();) {
			Object next = iterator.next();
			if (next instanceof Element) {
				Element refElement = (Element) next;
				parseRefElement(term, refElement);
			}
		}
		
		// add rdfs:label to terms for BioPortal
		cm.addRdfsLabelToTerm(term, label, lang);
		
	}

	private void parseRefElement(RDFResource term, Element refElement) {
		String code = refElement.getAttributeValue(ClamlConstants.CODE_ATTR);
		String usage = refElement.getAttributeValue(ClamlConstants.USAGE_ATTR);
		String text = refElement.getTextTrim();
		RDFResource ref = cm.createClamlReference();
		cm.fillClamlReference(ref, text, usage, code);
		cm.addClamlRefToTerm(term, ref);

		termToRefCode.put(term, text);
	}

	private void postprocess() {
		addSuperClses();
		addReferencedCategories();
	}

	private void addSuperClses() {
		log.info("Adding superclasses..");

		RDFSNamedClass topCls = owlModel.getRDFSNamedClass(CollectionUtilities.getFirstItem(topClsColl));

		for (RDFSNamedClass cls : cls2superclsesNames.keySet()) {
			List<String> superclsesNames = cls2superclsesNames.get(cls);
			for (String superclsName : superclsesNames) {
				RDFSNamedClass superCls = owlModel.getRDFSNamedClass(superclsName);
				if (superCls == null) {
					log.warning(
							"Could not add superclass to class: " + cls + ". Superclass not found: " + superclsName);
				} else {
					cls.addSuperclass(superCls);
					cls.removeSuperclass(topCls);
				}
			}
		}
	}

	private void addReferencedCategories() {
		log.info("Adding referenced categories..");

		for (RDFResource term : termToRefCode.keySet()) {
			String code = termToRefCode.get(term);

			try {
				RDFSNamedClass refCls = owlModel.getRDFSNamedClass(code);
				if (refCls == null) {
					log.warning("Could not find referenced class: " + code);
				} else {
					term.setPropertyValue(cm.getReferencedCategoryProperty(), refCls);
				}

			} catch (Exception e) {
				log.log(Level.SEVERE, "Could not add reference category: " + code, e);
			}
		}
	}

	private void cleanup() {
		termToRefCode.clear();
	}

}
