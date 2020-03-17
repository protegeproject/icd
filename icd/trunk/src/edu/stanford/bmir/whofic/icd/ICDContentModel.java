package edu.stanford.bmir.whofic.icd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.IcdIdGenerator;
import edu.stanford.bmir.whofic.KBUtil;
import edu.stanford.bmir.whofic.WHOFICContentModel;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICDContentModel extends WHOFICContentModel {

    private static transient Logger log = Log.getLogger(ICDContentModel.class);

    private final OWLModel owlModel;

    /*
     * Metaclasses
     */
    private RDFSNamedClass causalMechanismMetaClass;
    private RDFSNamedClass clincalDescriptionMetaClass;
    private RDFSNamedClass functionalImpactMetaClass;
    private RDFSNamedClass diagnosticCriteriaMetaClass;
    private RDFSNamedClass notesMetaClass;
    private RDFSNamedClass snomedReferenceMetaClass;
    private RDFSNamedClass specificConditionMetaClass;
    private RDFSNamedClass externalCauseMetaClass;
    private RDFSNamedClass iceciMetaClass;

    private Collection<RDFSNamedClass> diseaseMetaclasses;
    private Collection<RDFSNamedClass> externalCausesMetaclasses;


    /*
     * Classes
     */
    private RDFSNamedClass icdCategoryClass;

    private RDFSNamedClass externalCausesTopClass;

    /*
     * Properties
     */
    private RDFProperty isTemplateProperty;
    private RDFProperty linearizationICD10ViewProperty;
    private RDFProperty linearizationICD10TabulationViewProperty;

    /*
     * Instances
     */



    public ICDContentModel(OWLModel owlModel) {
    	super(owlModel);
        this.owlModel = owlModel;
    }

    /*
     * Getters for sections (metaclasses)
     */

    public RDFSNamedClass getCausalMechanismMetaClass() {
        if (causalMechanismMetaClass == null) {
            causalMechanismMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_CAUSAL_MECH_METACLASS);
        }
        return causalMechanismMetaClass;
    }

    public RDFSNamedClass getClinicalDescriptionMetaClass() {
        if (clincalDescriptionMetaClass == null) {
            clincalDescriptionMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_CLINICAL_DESC_METACLASS);
        }
        return clincalDescriptionMetaClass;
    }

    public RDFSNamedClass getFunctionalImpactMetaClass() {
        if (functionalImpactMetaClass == null) {
            functionalImpactMetaClass = owlModel
                    .getRDFSNamedClass(ICDContentModelConstants.ICD_FUNCTIONAL_IMPACT_METACLASS);
        }
        return functionalImpactMetaClass;
    }

    public RDFSNamedClass getNotesMetaClass() {
        if (notesMetaClass == null) {
            notesMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_NOTES_METACLASS);
        }
        return notesMetaClass;
    }

    public RDFSNamedClass getSnomedReferenceMetaClass() {
        if (snomedReferenceMetaClass == null) {
            snomedReferenceMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_SNOMED_METACLASS);
        }
        return snomedReferenceMetaClass;
    }

    public RDFSNamedClass getDiagnosticCriteriaMetaClass() {
        if (diagnosticCriteriaMetaClass == null) {
            diagnosticCriteriaMetaClass = owlModel
                    .getRDFSNamedClass(ICDContentModelConstants.ICD_DIAGNOSTIC_CRITERIA_METACLASS);
        }
        return diagnosticCriteriaMetaClass;
    }

    public RDFSNamedClass getSpecificConditionMetaClass() {
        if (specificConditionMetaClass == null) {
            specificConditionMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_SPECIFIC_CONDITION_METACLASS);
        }
        return specificConditionMetaClass;
    }

    public RDFSNamedClass getExternalCauseMetaClass() {
        if (externalCauseMetaClass == null) {
            externalCauseMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_EXTERNAL_CAUSE_METACLASS);
        }
        return externalCauseMetaClass;
    }

    public RDFSNamedClass getICECIMetaClass() {
        if (iceciMetaClass == null) {
        	iceciMetaClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_ICECI_METACLASS);
        }
        return iceciMetaClass;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    public Collection<RDFSNamedClass> getExternalCauseMetaclasses() {
        if (externalCausesMetaclasses == null) {
            externalCausesMetaclasses = new ArrayList<RDFSNamedClass>(getExternalCausesTopClass().getDirectTypes());
        }
        return externalCausesMetaclasses;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    public Collection<RDFSNamedClass> getRegularDiseaseMetaclasses() {
        if (diseaseMetaclasses == null) {
            diseaseMetaclasses = new ArrayList<RDFSNamedClass>(getICDCategoryClass().getDirectTypes());
        }
        return diseaseMetaclasses;
    }

    /*
     * Getters for classes
     */

    public RDFSNamedClass getICDCategoryClass() {
        if (icdCategoryClass == null) {
            icdCategoryClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.ICD_CATEGORY_CLASS);
        }
        return icdCategoryClass;
    }

    public RDFSNamedClass getExternalCausesTopClass() {
        if (externalCausesTopClass == null) {
            externalCausesTopClass = owlModel.getRDFSNamedClass(ICDContentModelConstants.EXTERNAL_CAUSES_TOP_CLASS);
        }
        return externalCausesTopClass;
    }



    /*
     * Getters for properties
     */

    public RDFProperty getIsTemplateProperty() {
        if (isTemplateProperty == null) {
        	isTemplateProperty = owlModel.getRDFProperty(ICDContentModelConstants.IS_TEMPLATE);
        }
        return isTemplateProperty;
    }

    public RDFProperty getLinearizationICD10Property() {
        if (linearizationICD10ViewProperty == null) {
            linearizationICD10ViewProperty = owlModel.getRDFProperty(ICDContentModelConstants.LINEARIZATION_ICD_10_PROP);
        }
        return linearizationICD10ViewProperty;
    }

    public RDFProperty getLinearizationICD10TabulationProperty() {
        if (linearizationICD10TabulationViewProperty == null) {
            linearizationICD10TabulationViewProperty = owlModel.getRDFProperty(ICDContentModelConstants.LINEARIZATION_ICD_10_TABULATION_PROP);
        }
        return linearizationICD10TabulationViewProperty;
    }

    public List<String> getPostcoordinationAxesPropertyList() {
    	return ICDContentModelConstants.PC_AXES_PROPERTIES_LIST;
    }


    /*
     * Create methods
     */

    /**
     * This method is deprecated, as it does not behave as expected.
     * 
     * Use {@link #createICDCategory(String, Collection) instead}.
     * 
     * This method will also create the superclasses of this class, 
     * but it will not add the right metaclasses to the superclasses.
     * 
     * @param name of ICD class to be created; if null, one will be generated.
     * @param superclsName - the superclass of the class to be created. The superclass will be created, if it does not exist.
     * @return
     */
    @Deprecated
    public RDFSNamedClass createICDCategory(String name, String superclsName) {
        return createICDCategory(name, CollectionUtilities.createCollection(superclsName), true, true); //method is used by the CLAML parser
    }

    /**
     * Creates an ICD Category under the given parents. Default actions:
     * <ul>
     * <li>Add the correct metaclasses (if regular disease, use metaclasses of ICDCategory; if subclass of External causes, use metaclasses of
     * External Causes</li>
     * <li>Create the linearization values: morbidity - is included, and mortality - is not included</li>
     * <li>Set the biologicalSex to NA </li>
     * </ul>
     *
     * @param name                      - name of the new category
     * @param superclsesName            - names of the parents
     * @param createICDSpecificEntities
     * @return
     */
    public RDFSNamedClass createICDCategory(String name, Collection<String> superclsesName) {
        return createICDCategory(name, superclsesName, false, true);
    }

    /**
     * Creates an ICD Category under the given parents. Default actions:
     * <ul>
     * <li>Add the correct metaclasses (if regular disease, use metaclasses of ICDCategory; if subclass of External causes, use metaclasses of
     * External Causes</li>
     * <li>Create the linearization values: morbidity - is included, and mortality - is not included</li>
     * <li>Set the biologicalSex to NA </li>
     * </ul>
     *
     * This method is deprecated, as it does not behave as expected, if the createSuperClasses flag is set to true.
     * If it will create also the superclasses, they will not have the right metaclasses set.
     * 
     * Use instead {@link #createICDCategory(String, Collection)}.
     *
     * @param name                      - name of the new category
     * @param superclsesName            - names of the parents
     * @param createSuperclasses        - true to create parents, 
     * 										if they don't already exist (only the CLAML parser needs to set this to true, 
     * 										all the rest, should use false)
     * 									WARNING: If it creates the superclasses, it will not add the right metaclasses.
     * 
     * @param createICDSpecificEntities
     * @return
     */
    @Deprecated
    public RDFSNamedClass createICDCategory(String name, Collection<String> superclsesName, boolean createSuperclasses, boolean createICDSpecificEntities) {
        if (name == null) {
            name = IcdIdGenerator.getNextUniqueId(owlModel);
        }
        RDFSNamedClass cls = getICDClass(name, true);

        Collection<RDFSNamedClass> superclses = new ArrayList<RDFSNamedClass>();

        //we could treat also the case when a class has an external cause and another normal disease as parents..

        if (superclsesName == null || superclsesName.size() == 0) {
            superclses.add(getICDCategoryClass());
        } else {
            for (String superclsName : superclsesName) {
                RDFSNamedClass supercls = getICDClass(superclsName, createSuperclasses);
                if (supercls != null) {
                    superclses.add(supercls);
                    //add superclasses
                    if (!cls.getSuperclasses(true).contains(supercls)) {
                        cls.addSuperclass(supercls);
                        if (cls.hasDirectSuperclass(owlModel.getOWLThingClass())) {
                            cls.removeSuperclass(owlModel.getOWLThingClass());
                        }
                        cls.setDirectTypes(supercls.getProtegeTypes());
                    }
                }
            }
        }

        if (createICDSpecificEntities) {
            createICDSpecificEntities(cls);
        }

        return cls;
    }

    protected void createICDSpecificEntities(RDFSNamedClass cls) {
        /*
         * Create the linearization instances for the newly created class. The linearization views are taken from the parents.
         * They are created separately for the ICD-11 linearizzations, the ICD-10 linearizations, and ICD-10 tabulation lists
         */
        createLinearizationSpecifications(cls);

        /*
         * Create the post-coordination instances for the newly created class. The linearization views are taken from the parents.
         * They are created separately for the ICD-11 linearizzations, the ICD-10 linearizations, and ICD-10 tabulation lists
         */
        createPostcoordinationSpecifications(cls);

        //set biologicalSex - default value: N/A (not applicable)
        cls.addPropertyValue(getBiologicalSexProperty(), owlModel.getRDFResource(ICDContentModelConstants.BIOLOGICAL_SEX_NA));
    }


    protected void createLinearizationSpecifications(RDFSNamedClass cls) {
        //ICD-11 linearizations
        createLinearizationSpecifications(cls, getLinearizationSpecificationClass(), getLinearizationProperty());
        //ICD-10 linearizations
        createLinearizationSpecifications(cls, getLinearizationHistoricSpecificationClass(), getLinearizationICD10Property());
        //ICD-10 tabulation lists
        createLinearizationSpecifications(cls, getLinearizationHistoricSpecificationClass(), getLinearizationICD10TabulationProperty());
    }

    protected void createLinearizationSpecifications(RDFSNamedClass cls, RDFSNamedClass linSpecificationClass, RDFProperty linProp) {
        for (RDFResource linView : getLinearizationViewsFromParents(cls, linProp)) {
            RDFResource linSpec = linSpecificationClass.createInstance(IcdIdGenerator.getNextUniqueId(owlModel));
            linSpec.setPropertyValue(getLinearizationViewProperty(), linView);
            //set default grouping to FALSE
            linSpec.setPropertyValue(getIsGroupingProperty(), Boolean.FALSE);
            linSpec.setPropertyValue(getIsAuxiliaryAxisChildProperty(), Boolean.FALSE);

            cls.addPropertyValue(linProp, linSpec);

            /* These only apply to the ICD-11 linearizations, but it is easier to make them for all. It won't have any effect on the historic linearization specifications */
            /* set the default for new categories: morbidity - included; mortality - not included */
            if (linView.getName().equals(ICDContentModelConstants.LINEARIZATION_VIEW_MORBIDITY)) {
                linSpec.setPropertyValue(getIsIncludedInLinearizationProperty(), Boolean.TRUE);
            } else if (linView.getName().equals(ICDContentModelConstants.LINEARIZATION_VIEW_MORTALITY)) {
                linSpec.setPropertyValue(getIsIncludedInLinearizationProperty(), Boolean.FALSE);
            }
        }
    }

    /**
     * It gets or creates and ICDClass. If it creates, it will not add the metaclasses.
     * To create an ICDMetaclass, it is better to use {@link #createICDCategory(String, Collection)}
     *
     * This method is deprecated, as it does not behave as expected. If the class will be
     * created, the correct metaclasses will not be added. Use instead {@link #createICDCategory(String, Collection)}.
     *
     * @param name   - name of the class to be retrieved or created
     * @param create - true to create class if it doesn't exit
     * @return - the class
     */
    @Deprecated
    private RDFSNamedClass getICDClass(String name, boolean create) {
        RDFSNamedClass cls = getICDClass(name);
        if (cls == null && create) {
            cls = owlModel.createOWLNamedClass(name);
            cls.addSuperclass(owlModel.getOWLThingClass());
        }
        return cls;
    }

    public RDFSNamedClass getICDClass(String name) {
        return KBUtil.getRDFSNamedClass(owlModel, name);
    }

    /**
     * Returns a set of all ICD Categories from the entire category tree.
     * This is a very expensive method and should only be used if necessary.
     *
     * @return the closure of all ICD classes in the tree
     */
    public Collection<RDFSNamedClass> getICDCategories() {
        return getRDFSNamedClassCollection(getICDCategoryClass().getSubclasses(true));

    }

    
    /*
     * Getters
     */

    @SuppressWarnings("unchecked")
    public Collection<RDFResource> getLinearizationICD10Specifications(RDFSNamedClass icdClass) {
        return icdClass.getPropertyValues(getLinearizationICD10Property());
    }

    @SuppressWarnings("unchecked")
    public Collection<RDFResource> getLinearizationICD10TabulationSpecifications(RDFSNamedClass icdClass) {
        return icdClass.getPropertyValues(getLinearizationICD10TabulationProperty());
    }


	public static boolean isFixedScalePCProp(String propName) {
		return ICDContentModelConstants.FIXED_SCALE_PC_AXES_PROPERTIES_LIST.contains(propName);
	}
	
	public static boolean isScalePCProp(String propName) {
		return ICDContentModelConstants.SCALE_PC_AXES_PROPERTIES_LIST.contains(propName);
	}
	
	public static boolean isHierarchicalPCProp(String propName) {
		return ICDContentModelConstants.HIERARCHICAL_PC_AXES_PROPERTIES_LIST.contains(propName);
	}
	
	public static boolean isLogicalDefinitionWithHasValueRestriction(String propName) {
		return isFixedScalePCProp(propName) || isScalePCProp(propName);
	}


}
