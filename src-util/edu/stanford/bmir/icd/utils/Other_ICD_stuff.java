package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class Other_ICD_stuff {

    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;
    private static RDFProperty termIdProp;
    private static RDFProperty defPrefilledInveProp;

    public static void main(String[] args) {
        Project prj = Project.loadProjectFromFile("/work/protege/projects/icd/content_model/icd_int/icd_mysql/icd_umbrella.pprj", new ArrayList());
        owlModel = (OWLModel) prj.getKnowledgeBase();
        icdContentModel = new ICDContentModel(owlModel);
        termIdProp = owlModel.getRDFProperty("http://who.int/icd#termId");
        defPrefilledInveProp = owlModel.getRDFProperty("http://who.int/icd/umls_definitions#is_prefilled_definition_of");

        //printInvalidClamlRefs();
        //fixClamlRef();
        //printInvalidDefs();
        //findInvalidDefs();
        //fixInvalidDefs();

        testCreateICDClass();
    }


    private static void testCreateICDClass() {
        Collection<String> superClses = CollectionUtilities.createCollection("http://who.int/icd#II");
        superClses.add("http://who.int/icd#V01-X59");

        RDFSNamedClass cls = icdContentModel.createICDCategory(null, superClses);

        System.out.println("Name: " + cls.getName() + " browser text: " + cls.getBrowserText());
        System.out.println("Types: " + cls.getDirectTypes());

        Collection<Slot> slots = cls.getOwnSlots();
        for (Slot slot : slots) {
            System.out.println(slot.getBrowserText() + ": " + cls.getOwnSlotValues(slot));
        }
    }

    private static void printInvalidClamlRefs() {
        long t0 = System.currentTimeMillis();
        int i = 0;
        Collection<RDFSNamedClass> icdCategories = icdContentModel.getICDCategories();
        for (RDFSNamedClass category : icdCategories) {
            if (i % 1000 == 0) {
                System.out.println("Done " + i + " categories");
            }
            printInvalidTerm(category, category.getPropertyValues(icdContentModel.getInclusionProperty()));
            printInvalidTerm(category, category.getPropertyValues(icdContentModel.getExclusionProperty()));
            i++;
        }

        System.out.println("Time: " + ((System.currentTimeMillis() - t0)/1000) + " sec");
    }

    private static void printInvalidTerm(RDFSNamedClass cat, Collection<RDFResource> terms) {
        for (RDFResource term : terms) {
            Collection<RDFResource> clamlRefs = term.getPropertyValues(icdContentModel.getClamlReferencesProperty());
            if (clamlRefs != null) {
                for (Object element : clamlRefs) {
                    Object clamlRef = element;
                    if (clamlRef instanceof String) {
                        System.out.println("Invalid for " + cat.getBrowserText() + ": " + clamlRef);
                    }
                }
            }
        }
    }

    private static void fixClamlRef() {
        int count = 0;
        int fixedCount = 0;

        RDFSNamedClass clamlRefClass = owlModel.getOWLNamedClass("http://who.int/icd#ClamlReference");
        RDFProperty textProp = owlModel.getRDFProperty("http://who.int/icd#text");
        RDFProperty icdRefProp = owlModel.getRDFProperty("http://who.int/icd#icdRefCode");

        Collection<RDFResource> clamlRefs = clamlRefClass.getInstances(true);
        for (RDFResource clamlTerm : clamlRefs) {
            String text = (String) clamlTerm.getPropertyValue(textProp);
            RDFResource ref = (RDFResource) clamlTerm.getPropertyValue(icdRefProp);
            if (text != null && ref == null) {
                Log.getLogger().warning("Missing icd ref: " + clamlTerm.getLocalName() + " text: " + text);
                count++;
                ref = owlModel.getRDFResource(text);
                if (ref != null) {
                    clamlTerm.setPropertyValue(icdRefProp, ref);
                    fixedCount ++;
                    Log.getLogger().info("** Fixed " + text + " -> " + ref.getBrowserText());

                }
            }
        }

        Log.getLogger().info("Found: " + count + " ; fixed: " + fixedCount);
    }


    private static void printInvalidDefs() {
        long t0 = System.currentTimeMillis();
        int i = 0;
        int count = 0;
        Collection<RDFSNamedClass> icdCategories = icdContentModel.getICDCategories();
        for (RDFSNamedClass category : icdCategories) {
            if (i % 1000 == 0) {
                System.out.println("Done " + i + " categories");
            }
            count = count + printInvalidDef(category);
            i++;
        }

        Log.getLogger().info("Time: " + ((System.currentTimeMillis() - t0)/1000) + " sec");
        Log.getLogger().info("Found " + count + " invalid defs");

    }


    private static int printInvalidDef(RDFSNamedClass category) {
        RDFResource defTerm = icdContentModel.getTerm(category, icdContentModel.getDefinitionProperty());
        if (defTerm != null) {
            Object defTermId = defTerm.getPropertyValue(termIdProp);
            Collection<?> isPrefilledOf = defTerm.getPropertyValues(defPrefilledInveProp);
            if (isPrefilledOf != null && isPrefilledOf.size() > 1) {
                Log.getLogger().warning("!!! Multiple is prefilled of " + category.getBrowserText() + " isPrefilledOf: " + isPrefilledOf);
            }
            if (defTermId != null) {
                if (isPrefilledOf != null) {
                	try {
	                    RDFResource inv = (RDFResource) CollectionUtilities.getFirstItem(isPrefilledOf);
	                    if (!inv.equals(category)) {
	                        Log.getLogger().warning("**** Wrong inverses!!! " + category.getBrowserText() + " ? " + inv.getBrowserText());
	                    }
                	} catch (ClassCastException e) {
                        Log.getLogger().warning("**** Wrong type (serious content error)!!! Property value for " + category.getBrowserText() + " and " + defPrefilledInveProp.getBrowserText() + 
                        		" is suppose to be of type RDFResource");
                	}
                }
                Log.getLogger().warning("Invalid def for " + category.getBrowserText() + " defTerm: " + defTerm.getLocalName() + " is Prefilled of: " + isPrefilledOf + "; termId:" + defTermId + " def: " + defTerm.getPropertyValue(icdContentModel.getLabelProperty()));
                return 1;
            }
        }

        return 0;
    }

    private static void findInvalidDefs() {
        int count = 0;

        RDFSNamedClass defClass = owlModel.getOWLNamedClass("http://who.int/icd#DefinitionTerm");
        RDFProperty ontIdProp = owlModel.getRDFProperty("http://who.int/icd#ontologyId");
        RDFProperty invDefOfProp = owlModel.getRDFProperty("http://who.int/icd/umls_definitions#is_prefilled_definition_of");
        RDFProperty termIdProp = owlModel.getRDFProperty("http://who.int/icd#termId");
        RDFProperty defPrefilledProp = owlModel.getRDFProperty("http://who.int/icd#definitionPrefilled");
        RDFProperty defProp = owlModel.getRDFProperty("http://who.int/icd#definition");

        Collection<RDFResource> defTerms = defClass.getInstances(true);
        for (RDFResource defTerm : defTerms) {
            RDFResource invPrefilledDef = (RDFResource) defTerm.getPropertyValue(invDefOfProp); //categ
            if (invPrefilledDef != null) {
                if (!invPrefilledDef.hasPropertyValue(defPrefilledProp, defTerm)) {
                    Log.getLogger().warning("Invalid def term for: " + invPrefilledDef.getBrowserText() + " term id: " + defTerm.getBrowserText());
                    count++;
                    if (!invPrefilledDef.hasPropertyValue(defProp, defTerm)) {
                        Log.getLogger().warning("\t******* Check def for " + invPrefilledDef.getBrowserText());
                    }
                }
            }
        }
        Log.getLogger().info("Invalid defs count: " + count);
    }


    private static void fixInvalidDefs() {
        int count = 0;

        RDFSNamedClass defClass = owlModel.getOWLNamedClass("http://who.int/icd#DefinitionTerm");
        RDFProperty ontIdProp = owlModel.getRDFProperty("http://who.int/icd#ontologyId");
        RDFProperty invDefOfProp = owlModel.getRDFProperty("http://who.int/icd/umls_definitions#is_prefilled_definition_of");
        RDFProperty termIdProp = owlModel.getRDFProperty("http://who.int/icd#termId");
        RDFProperty defPrefilledProp = owlModel.getRDFProperty("http://who.int/icd#definitionPrefilled");
        RDFProperty defProp = owlModel.getRDFProperty("http://who.int/icd#definition");

        Collection<RDFResource> defTerms = defClass.getInstances(true);
        for (RDFResource defTerm : defTerms) {
            RDFResource invPrefilledDef = (RDFResource) defTerm.getPropertyValue(invDefOfProp); //categ
            if (invPrefilledDef != null) {
                if (!invPrefilledDef.hasPropertyValue(defPrefilledProp, defTerm)) {
                    invPrefilledDef.addPropertyValue(defPrefilledProp, defTerm);
                    invPrefilledDef.removePropertyValue(defProp, defTerm);
                    Log.getLogger().info("Fix def term for: " + invPrefilledDef.getBrowserText() + " term id: " + defTerm);
                    count++;
                }
            }
        }
        Log.getLogger().info("Fixed defs count: " + count);
    }

    private static void fixMetaclasses() {
        owlModel.setGenerateEventsEnabled(false);
        RDFSNamedClass icdCatCls = icdContentModel.getICDCategoryClass();

        RDFSNamedClass extCauseCls = icdContentModel.getExternalCausesTopClass();

        Collection<RDFSNamedClass> catMetaclasses = icdContentModel.getRegularDiseaseMetaclasses();

        Collection<RDFSNamedClass> topClses = icdCatCls.getDirectSubclasses();
        topClses.remove(extCauseCls);

        //fix regular disease
        for (RDFSNamedClass topCls : topClses) {
            fixMetaclses(topCls, catMetaclasses);
            Collection<Cls> subclses = topCls.getSubclasses();
            for (Cls subcls :subclses) {
                if (subcls instanceof RDFSNamedClass) {
                    fixMetaclses(subcls, catMetaclasses);
                }
            }
        }

        //fix external causes
        Collection<RDFSNamedClass> extCauseMetaclses = icdContentModel.getExternalCauseMetaclasses();
        Collection<RDFSNamedClass> extClses = extCauseCls.getSubclasses();
        for (RDFSNamedClass cls : extClses) {
            if (cls instanceof RDFSNamedClass) {
                fixMetaclses(cls, extCauseMetaclses);
            }
        }
    }

    private static void fixMetaclses(Cls c, Collection<RDFSNamedClass> metaclasses) {
        for (RDFSNamedClass metacls : metaclasses) {
            if (!c.hasType(metacls)) {
                c.addDirectType(metacls);
            }
        }
        Collection<Cls> extraMetaclases = new ArrayList<Cls>(c.getDirectTypes());
        extraMetaclases.removeAll(metaclasses);

        for (Cls metacls : extraMetaclases) {
            c.removeDirectType(metacls);
        }
    }

}
