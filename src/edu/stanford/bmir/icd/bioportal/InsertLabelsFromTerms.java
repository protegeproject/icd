package edu.stanford.bmir.icd.bioportal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ncbo.stanford.bean.concept.ClassBean;
import org.ncbo.stanford.util.BioportalConcept;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.jena.creator.OwlProjectFromUriCreator;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFProperty;

public class InsertLabelsFromTerms {
    private static Logger log = Log.getLogger(InsertLabelsFromTerms.class);
   
    public static String BIOPORTAL_DELAY_PROPERTY="bioportal.call.delay";
    public static String ICD_ONTOLOGY_LOCATION_PROPERTY="labels.to.term.ontology.location";
    public static String REFERENCE_TERM_PROPERTY="labels.to.term.class";
    public static String RECURSIVE_PROPERTY="labels.to.term.recursive";
    public static String TERM_ID_PROPERTY="labels.to.term.id.property";
    
    public static String SNOMED_REST_URL="http://rest.bioontology.org/bioportal/concepts/40403/";
    public static String ICD_NS = "http://who.int/icd#";

    private String referenceTermName;
    private boolean recursive;
    private String termIdPropertyName=ICD_NS + "termId";
    
    private Properties parameters = new Properties();
    private OWLModel owlModel;
    private long delay = 1000;
    
    public InsertLabelsFromTerms() throws FileNotFoundException, IOException {
        parameters.load(new FileInputStream(new File("local.properties")));
        referenceTermName = parameters.getProperty(REFERENCE_TERM_PROPERTY);
        recursive = parameters.getProperty(RECURSIVE_PROPERTY).toLowerCase().equals("true");
        termIdPropertyName = parameters.getProperty(TERM_ID_PROPERTY);
        try {
            delay = Integer.parseInt(parameters.getProperty(BIOPORTAL_DELAY_PROPERTY));
        }
        catch (Throwable t) {
            ;
        }
    }
    
    @SuppressWarnings("unchecked")
    public void loadOntology() throws OntologyLoadException {
        OwlProjectFromUriCreator creator = new OwlProjectFromUriCreator();
        URI u = new File(parameters.getProperty(ICD_ONTOLOGY_LOCATION_PROPERTY)).toURI();
        creator.setOntologyUri(u.toString());
        List errors  = new ArrayList();
        creator.create(errors);
        if (!errors.isEmpty()) {
            for (Object o : errors) {
                if (o instanceof Throwable) {
                    log.log(Level.WARNING, "Exception caught", (Throwable) o);
                }
                else {
                    log.warning(o.toString());
                }
            }
            throw new OntologyLoadException();
        }
        owlModel = creator.getOwlModel();
    }
    
    @SuppressWarnings("unchecked")
    public void saveOntology() throws IOException  {
        List errors  = new ArrayList();
        owlModel.getProject().save(errors);
        if (!errors.isEmpty()) {
            for (Object o : errors) {
                if (o instanceof Throwable) {
                    log.log(Level.WARNING, "Exception caught", (Throwable) o);
                }
                else {
                    log.warning(o.toString());
                }
            }
            throw new IOException();
        }
    }
    
    public void run() throws MalformedURLException {
        OWLNamedClass referenceTerm = owlModel.getOWLNamedClass(referenceTermName);
        OWLDatatypeProperty termId = owlModel.getOWLDatatypeProperty(termIdPropertyName);
        RDFProperty rdfLabel = owlModel.getRDFSLabelProperty();
        int total = referenceTerm.getInstanceCount(true);
        log.info("Examining " + total + " individuals");
        int counter = 0;
        for (Object o : referenceTerm.getInstances(true)) {
            if (++counter % 1000 == 0) {
                log.info("" + counter + " individuals examined");
            }
            if (o instanceof OWLIndividual) {
                OWLIndividual i = (OWLIndividual) o;
                Object v = i.getPropertyValue(termId);
                if (i.getPropertyValue(rdfLabel) == null && 
                        i.getPropertyValue(termId) != null &&
                        v instanceof String) {
                    String value = (String) v;
                    String label = getLabelFromTermId(value);
                    if (label != null) {
                        log.info("Found individual " + label);
                        i.setPropertyValue(rdfLabel, label);
                    }
                }
            }
        }
    }
    
    private String getLabelFromTermId(String termId) throws MalformedURLException {
        ClassBean cb;
        try {
            cb = new BioportalConcept().getConceptProperties(new URL(SNOMED_REST_URL + termId));
        }
        catch (Throwable t) {
            return null;
        }
        if (cb != null) {
            return cb.getLabel();
        }
        else {
            return null;
        }
    }
    
    
    /**
     * @param args
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws OntologyLoadException 
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, OntologyLoadException {
        Logger.getLogger(BioportalConcept.class.getName()).setLevel(Level.SEVERE);
        InsertLabelsFromTerms i = new InsertLabelsFromTerms();
        i.loadOntology();
        i.run();
        i.saveOntology();
    }

}
