package edu.stanford.bmir.icd.bioportal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
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

    public static final String BIOPORTAL_DELAY_PROPERTY="bioportal.call.delay";
    public static final String ICD_ONTOLOGY_LOCATION_PROPERTY="labels.from.term.ontology.location";
    public static final String LABEL_PROPERTY="labels.from.term.label.property";
    public static final String REFERENCE_TERM_PROPERTY="labels.from.term.class";
    public static final String RECURSIVE_PROPERTY="labels.from.term.recursive";
    public static final String SNOMED_REST_URL="labels.from.term.rest.url.prefix";
    public static final String TERM_ID_PROPERTY="labels.from.term.id.property";


    public static String ICD_NS = "http://who.int/icd#";

    private String  labelPropertyName;
    private boolean recursive;
    private String  referenceTermName;
    private String  restUrlPrefix;
    private String  termIdPropertyName=ICD_NS + "termId";

    private Properties parameters = new Properties();
    private OWLModel owlModel;

    public InsertLabelsFromTerms() throws FileNotFoundException, IOException {
        parameters.load(new FileInputStream(new File("local.properties")));
        labelPropertyName = parameters.getProperty(LABEL_PROPERTY);
        referenceTermName = parameters.getProperty(REFERENCE_TERM_PROPERTY);
        restUrlPrefix = parameters.getProperty(SNOMED_REST_URL);
        recursive = parameters.getProperty(RECURSIVE_PROPERTY).toLowerCase().equals("true");
        termIdPropertyName = parameters.getProperty(TERM_ID_PROPERTY);
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
        OWLNamedClass referenceTermCls = owlModel.getOWLNamedClass(referenceTermName);
        OWLDatatypeProperty termIdProperty = owlModel.getOWLDatatypeProperty(termIdPropertyName);
        RDFProperty labelProperty = owlModel.getRDFProperty(labelPropertyName);

        int total = referenceTermCls.getInstanceCount(true);
        int found = 0;
        int counter = 0;
        log.info("Examining " + total + " individuals");
        for (Object o : referenceTermCls.getInstances(true)) {
            if (++counter % 1000 == 0) {
                log.info("" + counter + " individuals examined");
            }
            if (o instanceof OWLIndividual) {
                OWLIndividual i = (OWLIndividual) o;
                Object v = i.getPropertyValue(termIdProperty);

                if (i.getPropertyValue(labelProperty) == null &&
                        i.getPropertyValue(termIdProperty) != null &&
                        v instanceof String) {
                    String value = (String) v;

                    String label = getLabelFromTermId(value);
                    if (label != null) {
                        if (++found % 100 == 0) {
                            log.info("Added label property for " + found + " individuals");
                        }
                        i.setPropertyValue(labelProperty, label);
                    }
                }
            }
        }
        log.info("Added Property level for " + found  + "individual");
    }

    private String getLabelFromTermId(String termId) throws MalformedURLException {
        try {
            Long.parseLong(termId);
        }
        catch (NumberFormatException nfe) {
            return null;
        }

        ClassBean cb = null;
        try {
            //does not compile
            //cb = new BioportalConcept().getConceptProperties(new URL(restUrlPrefix + termId));
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
