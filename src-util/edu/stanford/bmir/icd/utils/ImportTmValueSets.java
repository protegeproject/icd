package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.util.logging.Level;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.OWLUtil;

public class ImportTmValueSets {

    //private static final String NS = "http://who.int/ictm/constitution#";
    //private static final String ONT_NAME = "http://who.int/ictm/constitution";

    //private static final String NS = "http://who.int/ictm/otherFactors#";
    //private static final String ONT_NAME = "http://who.int/ictm/otherFactors";

    private static final String NS = "http://who.int/ictm/signsAndSymptoms#";
    private static final String ONT_NAME = "http://who.int/ictm/signsAndSymptoms";


    private static final String ICTM_NS = "http://who.int/ictm#";
    private static final String SEP = " / ";

    private static final String ENGLISH = "en";
    private static final String CHINESE = "cn";
    private static final String JAPANESE = "jp";
    private static final String KOREAN = "kr";

    private static JenaOWLModel owlModel;

    private static RDFProperty defEnProp;
    private static RDFProperty defCnProp;
    private static RDFProperty defJpProp;
    private static RDFProperty defKrProp;

    private static RDFProperty titleEnProp;
    private static RDFProperty titleCnProp;
    private static RDFProperty titleJpProp;
    private static RDFProperty titleKrProp;

    private static RDFProperty origSourceProp;
    private static RDFProperty origCodeProp;

    private static int startId;
    private static int counter = 0;

    public static void main(String[] args) throws Exception {
        //String csvPath = "/work/src/GWT/web-protege/war/projects/ictm/sources/TM_Constitution.csv";
        //String filePath = "/work/src/GWT/web-protege/war/projects/ictm/value_sets/TM_Constitution.owl";

        //String csvPath = "/work/src/GWT/web-protege/war/projects/ictm/sources/TM_OtherFactors.csv";
        //String filePath = "/work/src/GWT/web-protege/war/projects/ictm/value_sets/TM_OtherFactors.owl";

        String csvPath = "/work/src/GWT/web-protege/war/projects/ictm/sources/TM_SignsAndSymptoms.csv";
        String filePath = "/work/src/GWT/web-protege/war/projects/ictm/value_sets/TM_SignsAndSymptoms.owl";

        startId = getRandomIdStart();

        createOntology();

        BufferedReader input = new BufferedReader(new FileReader(csvPath));
        input.readLine(); //skip first line

        String line = null;
        while ((line = input.readLine()) != null) {
            if (line != null) {
                try {
                    while (!line.endsWith("\"") && !line.endsWith("\t")) {
                        line = line + input.readLine();
                    }
                    processLine(line);
                } catch (Exception e) {
                    Log.getLogger().log(Level.WARNING, " Could not read line: " + line, e);
                }
            }
        }

      owlModel.save(URIUtilities.createURI(filePath));
    }


    private static void createOntology() throws OntologyLoadException {
        owlModel = ProtegeOWL.createJenaOWLModel();
        OWLUtil.renameOntology(owlModel, owlModel.getDefaultOWLOntology(), ONT_NAME);
        owlModel.getNamespaceManager().setDefaultNamespace(NS);

        owlModel.getNamespaceManager().setPrefix(ICTM_NS, "ictm");

        defEnProp = owlModel.createAnnotationProperty(ICTM_NS + "definitionEnglish");
        defEnProp.addLabel("Definition English", null);
        defCnProp = owlModel.createAnnotationProperty(ICTM_NS + "definitionChinese");
        defCnProp.addLabel("Definition Chinese", null);
        defJpProp = owlModel.createAnnotationProperty(ICTM_NS + "definitionJapanese");
        defJpProp.addLabel("Definition Japanese", null);
        defKrProp = owlModel.createAnnotationProperty(ICTM_NS + "definitionKorean");
        defKrProp.addLabel("Definition Korean", null);

        titleEnProp = owlModel.createAnnotationProperty(ICTM_NS + "titleEnglish");
        titleEnProp.addLabel("Title English", null);
        titleCnProp = owlModel.createAnnotationProperty(ICTM_NS + "titleChinese");
        titleCnProp.addLabel("Title Chinese", null);
        titleJpProp = owlModel.createAnnotationProperty(ICTM_NS + "titleJapanese");
        titleJpProp.addLabel("Title Japanese", null);
        titleKrProp = owlModel.createAnnotationProperty(ICTM_NS + "titleKorean");
        titleKrProp.addLabel("Title Korean", null);

        origSourceProp = owlModel.createAnnotationProperty(ICTM_NS + "originalProperty");
        origSourceProp.addLabel("Original Source", null);
        origCodeProp = owlModel.createAnnotationProperty(ICTM_NS + "originalCode");
        origCodeProp.addLabel("Original Code", null);
    }

    private static int getRandomIdStart() {
        Random randomGen = new Random();
        int range = (9999999 - 1000000)/2;
        int fraction = (int) (range * randomGen.nextDouble());
        return (100000 + fraction);
    }

    private static void processLine(String line) {
        try {
            Log.getLogger().info(line);
            final String[] split = line.split("\t");

            final String originalSource = getSafeValue(split, 0);
            final String originalCode = getSafeValue(split, 1);

            final String englishTitleValue = getSafeValue(split, 2);
            final String chineseTitleValue = getSafeValue(split, 3);
            final String japaneseTitleValue = getSafeValue(split, 4);
            final String koreanTitleValue = getSafeValue(split, 5);

            final String englishDefinitionValue = getSafeValue(split, 6);
            final String chineseDefinitionValue = getSafeValue(split, 7);
            final String japaneseDefinitionValue = getSafeValue(split, 8);
            final String koreanDefinitionValue = getSafeValue(split, 9);

            final String name = getName();

            RDFSNamedClass cls = owlModel.createOWLNamedClass(name);

            String rdfsLabel = getRDFSLabel(englishTitleValue, chineseTitleValue, japaneseTitleValue, koreanTitleValue);
            cls.setPropertyValue(owlModel.getRDFSLabelProperty(), rdfsLabel);

            setValue(cls, titleEnProp, englishTitleValue, ENGLISH);
            setValue(cls, titleCnProp, chineseTitleValue, CHINESE);
            setValue(cls, titleJpProp, japaneseTitleValue, JAPANESE);
            setValue(cls, titleKrProp, koreanTitleValue, KOREAN);

            setValue(cls, defEnProp, englishDefinitionValue, ENGLISH);
            setValue(cls, defCnProp, chineseDefinitionValue, CHINESE);
            setValue(cls, defJpProp, japaneseDefinitionValue, JAPANESE);
            setValue(cls, defKrProp, koreanDefinitionValue, KOREAN);

            setValue(cls, origSourceProp, originalSource, null);
            setValue(cls, origCodeProp, originalCode, null);

        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at import", e);
        }

    }


    private static void setValue(RDFSNamedClass cls, RDFProperty prop, String val, String lang) {
        if (val == null || val.length() == 0) {
            return;
        }
        if (lang != null) {
            RDFSLiteral lit = owlModel.createRDFSLiteral(val, lang);
            cls.setPropertyValue(prop, lit);
        } else {
            cls.setPropertyValue(prop, val);
        }
    }


    private static String getSafeValue(final String[] split, final int index) {
        if (index >= split.length) {
            return "";
        }
        String string = split[index];
        if (string == null || string.length() == 0) {
            return "";
        }
        string = string.trim();
        return string.replaceAll("\"", "");
    }

    private static String getRDFSLabel(String en, String cn, String jp, String kr) {
        String label = en;
        if (cn != null && cn.length() > 0) {
            label = label + SEP + cn;
        }
        if (jp != null && jp.length() > 0) {
            label = label + SEP + jp;
        }
        if (kr != null && kr.length() > 0) {
            label = label + SEP + kr;
        }
        return label;
    }

    private static String getName() {
        counter = counter + 1;
        return  NS + "TM" + Integer.toString(startId + counter);
    }

}
