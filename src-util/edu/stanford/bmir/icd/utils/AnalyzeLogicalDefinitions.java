package edu.stanford.bmir.icd.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class AnalyzeLogicalDefinitions {

    private static OWLModel owlModel;
    private static ICDContentModel icdContentModel;

    private static final String BROWSERTEXT_LIST_START = "[";
    private static final String BROWSERTEXT_LIST_END = "]";
    private static final String BROWSERTEXT_QOUTE = "\"";
    private static final String BROWSERTEXT_LIST_SEPARATOR = ", ";
    
	public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Argument missing: pprj_file_name. Second argument is optional: top_class");
             return;
         }

         Collection<?> errors = new ArrayList<Object>();
         Project prj = Project.loadProjectFromFile(args[0], errors);

         if (errors != null) {
             ProjectManager.getProjectManager().displayErrors("Errors", errors);
         }

         owlModel = (OWLModel) prj.getKnowledgeBase();

         if (owlModel == null) {
             System.out.println("Failed to load the ICD project from this location: " + args[0]);
             return;
         }

         owlModel = (OWLModel) prj.getKnowledgeBase();
         icdContentModel = new ICDContentModel(owlModel);

         String topClass = ICDContentModelConstants.ICD_CATEGORY_CLASS;
         if (args.length > 1) {
        	 topClass = args[1];
         }
         RDFSNamedClass icdCategory = icdContentModel.getICDCategory(topClass);
         
         analyzeLogicalDefintionsForTree(icdCategory);
	}

	private static void analyzeLogicalDefintionsForTree(RDFSNamedClass icdCategory) {
		Collection<?> subclasses = icdCategory.getSubclasses(true);
		for (Object cls : subclasses) {
			if (cls instanceof OWLNamedClass) {
				String s = getLogicalDefintionsSummaryForClass( (OWLNamedClass)cls );
				System.out.println(s);
			}
			else {
//				Log.getLogger().warning(String.format("Class %s is not of type OWLNamedClass", cls));
			}
		}
	}

	private static String getLogicalDefintionsSummaryForClass(OWLNamedClass cls) {
		String msg = "";
		msg += cls.getURI();
		msg += "\t" + cls.getBrowserText();

		RDFSNamedClass pcSuperclass = icdContentModel.getPrecoordinationSuperclass(cls);
		msg += "\t" + (pcSuperclass == null ? "null" : pcSuperclass.getURI());
		msg += "\t" + (pcSuperclass == null ? "null" : pcSuperclass.getBrowserText());
		
		msg += "\t" + icdContentModel.getPropertiesInPrecoordinationDefinition(cls, true);
		msg += "\t" + icdContentModel.getPropertiesInPrecoordinationDefinition(cls, false);

		msg += "\t" + getBrowserText( getEquivalentPrecoordinationClassExpression(cls, true));
		msg += "\t" + getBrowserText( getNecessaryPrecoordinationClassExpression(cls, true));

		msg += "\t" + getBrowserText( getEquivalentPrecoordinationClassExpression(cls, false));
		msg += "\t" + getBrowserText( getNecessaryPrecoordinationClassExpression(cls, false));

		return msg;
	}

	public static List<OWLIntersectionClass> getEquivalentPrecoordinationClassExpression(RDFSNamedClass cls, boolean superclassRequired) {

    	Collection<?> equivalentClasses = cls.getEquivalentClasses();
    	if (equivalentClasses == null || equivalentClasses.isEmpty()) {
    		return null;
    	}
    	
    	ArrayList<OWLIntersectionClass> res = new ArrayList<OWLIntersectionClass>();
    	for (Iterator<?> it = equivalentClasses.iterator(); it.hasNext(); ) {
    		OWLClass nextEqClass = (OWLClass)it.next();
    		if (isValidPrecoordinationDefinitionClassExpression(nextEqClass, superclassRequired)) {
				res.add((OWLIntersectionClass) nextEqClass);
    		}
    	}
    	
    	return (res.isEmpty() ? null : res);
    }

    public static List<OWLIntersectionClass> getNecessaryPrecoordinationClassExpression(RDFSNamedClass cls, boolean superclassRequired) {
    	Collection<?> superclasses = cls.getSuperclasses(false);
    	if (superclasses == null || superclasses.isEmpty()) {
    		return null;
    	}
    	
    	ArrayList<OWLIntersectionClass> res = new ArrayList<OWLIntersectionClass>();
    	for (Iterator<?> it = superclasses.iterator(); it.hasNext(); ) {
    		OWLClass nextSuperclass = (OWLClass)it.next();
    		if ( (! cls.hasEquivalentClass(nextSuperclass)) &&
    				isValidPrecoordinationDefinitionClassExpression(nextSuperclass, superclassRequired)) {
				res.add((OWLIntersectionClass) nextSuperclass);
    		}
    	}
    	
    	return (res.isEmpty() ? null : res);
    }

	private static boolean isValidPrecoordinationDefinitionClassExpression(OWLClass classExpr, boolean superclassRequired) {
		//TODO We should probably check that the OWLClassIntersection contains a also a OWLNamedClass operand (at least for the equivalent 
    	if (classExpr instanceof OWLIntersectionClass) {
    		if ( ! superclassRequired) {
    			return true;
    		}
    		
    		OWLIntersectionClass intClassExpr = (OWLIntersectionClass) classExpr;
    		Collection<RDFSClass> operands = intClassExpr.getOperands();
    		Iterator<RDFSClass> it = operands.iterator();
    		while (it.hasNext()) {
    			RDFSClass op = it.next();
    			if (op instanceof RDFSNamedClass) {
    				return true;
    			}
    		}
    		return false;
    	}
    	else {
    		return false;
    	}

	}


    private static <C extends OWLClass> String getBrowserText(List<C> clsList) {
    	if (clsList == null) {
    		return null;
    	}
    	if (clsList.size() == 0) {
    		return BROWSERTEXT_LIST_START + BROWSERTEXT_LIST_END;
    	}
    	
    	String res = BROWSERTEXT_LIST_START;
    	for (OWLClass cls : clsList) {
			res += BROWSERTEXT_QOUTE + cls.getBrowserText().trim() + BROWSERTEXT_QOUTE + BROWSERTEXT_LIST_SEPARATOR;
		}
    	res = res.substring(0, res.length() - BROWSERTEXT_LIST_SEPARATOR.length()) + BROWSERTEXT_LIST_END;
    	
    	return res;
	}

	
}
