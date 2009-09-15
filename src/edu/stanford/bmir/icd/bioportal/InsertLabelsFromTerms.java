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
    
    public static String ICD_ONTOLOGY_LOCATION_PROPERTY="latels.to.term.ontology.location";
    public static String SNOMED_REST_URL="http://rest.bioontology.org/bioportal/concepts/40403/";
    public static String ICD_NS = "http://who.int/icd#";
    public static String REFERENCE_TERM=ICD_NS + "ReferenceTerm";
    public static String TERM_ID_PROPERTY=ICD_NS + "termId";
    
    private Properties parameters = new Properties();
    private OWLModel owlModel;
    
    public InsertLabelsFromTerms() throws FileNotFoundException, IOException {
        parameters.load(new FileInputStream(new File("local.properties")));  
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
    
    public void run() throws MalformedURLException {
        OWLNamedClass referenceTerm = owlModel.getOWLNamedClass(REFERENCE_TERM);
        OWLDatatypeProperty termId = owlModel.getOWLDatatypeProperty(TERM_ID_PROPERTY);
        RDFProperty rdfLabel = owlModel.getRDFSLabelProperty();
        int total = referenceTerm.getInstanceCount(true);
        log.info("Examining " + total + "individuals");
        for (Object o : referenceTerm.getInstances(true)) {
            if (o instanceof OWLIndividual) {
                OWLIndividual i = (OWLIndividual) o;
                Object v = i.getPropertyValue(termId);
                if (i.getPropertyValue(rdfLabel) == null && 
                        i.getPropertyValue(termId) != null &&
                        v instanceof String) {
                    String value = (String) v;
                    String label = getLabelFromTermId(value);
                    if (label != null) {
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
        InsertLabelsFromTerms i = new InsertLabelsFromTerms();
        i.loadOntology();
        i.run();
    }

}
