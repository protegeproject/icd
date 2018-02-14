package edu.stanford.bmir.whofic.ici;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import edu.stanford.bmir.whofic.WHOFICContentModel;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.IDGenerator;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ICIContentModel extends WHOFICContentModel {

    private static transient Logger log = Log.getLogger(ICIContentModel.class);

    private final OWLModel owlModel;

    /*
     * Metaclasses
     */

    private Collection<RDFSNamedClass> diseaseMetaclasses;

    /*
     * Classes
     */
    private RDFSNamedClass icdCategoryClass;


    /*
     * Properties
     */

    
    /*
     * Instances
     */

    private RDFResource indexTypeSynoymInst;

    private RDFResource displayStatusBlue;
    private RDFResource displayStatusYellow;
    private RDFResource displayStatusRed;

    public ICIContentModel(OWLModel owlModel) {
    	super(owlModel);
        this.owlModel = owlModel;
    }

    /*
     * Getters for sections (metaclasses)
     */


//TODO see if we need to access special metaclasses
//    @SuppressWarnings({"deprecation", "unchecked"})
//    public Collection<RDFSNamedClass> getRegularDiseaseMetaclasses() {
//        if (diseaseMetaclasses == null) {
//            diseaseMetaclasses = new ArrayList<RDFSNamedClass>(getICDCategoryClass().getDirectTypes());
//        }
//        return diseaseMetaclasses;
//    }


    /*
     * Getters for classes
     */

    public RDFSNamedClass getICICategoryClass() {
        if (icdCategoryClass == null) {
            icdCategoryClass = owlModel.getRDFSNamedClass(ICIContentModelConstants.ICD_CATEGORY_CLASS);
        }
        return icdCategoryClass;
    }


    /*
     * Getters for properties
     */


    public List<String> getPostcoordinationAxesPropertyList() {
    	return ICIContentModelConstants.PC_AXES_PROPERTIES_LIST;
    }



    /*
     * Create methods
     */

    public RDFSNamedClass createICDCategory(String name, String superclsName) {
        return createICICategory(name, CollectionUtilities.createCollection(superclsName), true, true); //method is used by the CLAML parser
    }

    public RDFSNamedClass createICDCategory(String name, Collection<String> superclsesName) {
        return createICICategory(name, superclsesName, false, true);
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
     * @param createSuperclasses        - true to create parents, if they don't already exist (only the CLAML parser needs to set this to true, all the rest, should use false)
     * @param createICDSpecificEntities
     * @return
     */
    @SuppressWarnings("deprecation")
    public RDFSNamedClass createICICategory(String name, Collection<String> superclsesName, boolean createSuperclasses, boolean createICDSpecificEntities) {
        if (name == null) {
            name = IDGenerator.getNextUniqueId();
        }
        RDFSNamedClass cls = getICDClass(name, true);

        Collection<RDFSNamedClass> superclses = new ArrayList<RDFSNamedClass>();

        //we could treat also the case when a class has an external cause and another normal disease as parents..

        if (superclsesName == null || superclsesName.size() == 0) {
            superclses.add(getICICategoryClass());
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

    private void createICDSpecificEntities(RDFSNamedClass cls) {
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
        cls.addPropertyValue(getBiologicalSexProperty(), owlModel.getRDFResource(ICIContentModelConstants.BIOLOGICAL_SEX_NA));
    }


    private void createLinearizationSpecifications(RDFSNamedClass cls) {
        //ICHI linearizations
        createLinearizationSpecifications(cls, getLinearizationSpecificationClass(), getLinearizationProperty());
        //CPT linearizations???
    }

    private void createLinearizationSpecifications(RDFSNamedClass cls, RDFSNamedClass linSpecificationClass, RDFProperty linProp) {
        for (RDFResource linView : getLinearizationViewsFromParents(cls, linProp)) {
            RDFResource linSpec = linSpecificationClass.createInstance(IDGenerator.getNextUniqueId());
            linSpec.setPropertyValue(getLinearizationViewProperty(), linView);
            //set default grouping to FALSE
            linSpec.setPropertyValue(getIsGroupingProperty(), Boolean.FALSE);
            linSpec.setPropertyValue(getIsAuxiliaryAxisChildProperty(), Boolean.FALSE);

            cls.addPropertyValue(linProp, linSpec);

            /* These only apply to the ICD-11 linearizations, but it is easier to make them for all. It won't have any effect on the historic linearization specifications */
            /* set the default for new categories: morbidity - included; mortality - not included */
            if (linView.getName().equals(ICIContentModelConstants.LINEARIZATION_VIEW_MORBIDITY)) {
                linSpec.setPropertyValue(getIsIncludedInLinearizationProperty(), Boolean.TRUE);
            } else if (linView.getName().equals(ICIContentModelConstants.LINEARIZATION_VIEW_MORTALITY)) {
                linSpec.setPropertyValue(getIsIncludedInLinearizationProperty(), Boolean.FALSE);
            }
        }
    }

    /**
     * It gets or creates and ICDClass. If it creates, it will not add the metaclasses.
     * To create an ICDMetaclass, it is better to use {@link #createICDCategory(String, Collection)}
     *
     * @param name   - name of the class to be retrieved or created
     * @param create - true to create class if it doesn't exit
     * @return - the class
     */
    private RDFSNamedClass getICDClass(String name, boolean create) {
        RDFSNamedClass cls = owlModel.getRDFSNamedClass(name);
        if (cls == null && create) {
            cls = owlModel.createOWLNamedClass(name);
            cls.addSuperclass(owlModel.getOWLThingClass());
        }
        return cls;
    }


    /*
     * Terms
     */


    /*
     * Getters
     */

    /**
     * Returns a set of all ICI Categories from the entire category tree.
     * This is a very expensive method and should only be used if necessary.
     *
     * @return the closure of all ICI classes in the tree
     */
    public Collection<RDFSNamedClass> getICICategories() {
        return getRDFSNamedClassCollection(getICICategoryClass().getSubclasses(true));

    }


    /*
     * TAG management methods
     */


    /*
     * Equivalent class definitions
     */


}
