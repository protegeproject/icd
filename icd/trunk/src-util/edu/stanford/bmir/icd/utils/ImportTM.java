package edu.stanford.bmir.icd.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.bmir.icd.claml.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.NamespaceUtil;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ImportTM {

    private static final String ORPHANS_CLASS = "http://who.int/ictm#Orphans";
    private static final String ICTM_ROOT_HIERARCHY_NAME = ".";
    private static final String ICTM_CATEGORY = "http://who.int/ictm#ICTMCategory";
    private static final String ENGLISH = "en";
    private static final String CHINESE = "cn";
    private static final String JAPANESE = "jp";
    private static final String KOREAN = "kr";

    private static OWLModel owlModel;
    private static ICDContentModel cm;
    private static RDFProperty codesProp;
    private static RDFProperty sourceProp;
    private static RDFProperty termIdProp;
    private static RDFProperty typeProp;

    private static Instance diseaseType;
    private static Instance patternType;
    private static RDFSNamedClass orphanCls;
    private static RDFSNamedClass ictmCatCls;

    public static void main(String[] args)  {
        String prjPath = "/work/src/GWT/web-protege/war/projects/ictm/ictm_umbrella_db.pprj";
       //String prjPath = "/work/src/GWT/web-protege/war/projects/ictm/ictm_umbrella.pprj";
        String csvPath = "/work/src/GWT/web-protege/war/projects/ictm/sources/TM Import - Stanford Version.V5.cutforimport.csv";

        Project prj = Project.loadProjectFromFile(prjPath, new ArrayList());
        owlModel = (OWLModel) prj.getKnowledgeBase();

        cm = new ICDContentModel(owlModel);

        codesProp = owlModel.getRDFProperty("http://who.int/ictm#codes");
        typeProp = owlModel.getRDFProperty("http://who.int/ictm#hasType");
        sourceProp = owlModel.getRDFProperty(ICDContentModelConstants.NS + "source");
        termIdProp = owlModel.getRDFProperty(ICDContentModelConstants.TERM_ID_PROP);
        diseaseType = owlModel.getRDFResource("http://who.int/ictm#DiseaseTMType");
        patternType = owlModel.getRDFResource("http://who.int/ictm#PatternTMType");
        ictmCatCls = owlModel.getRDFSNamedClass(ICTM_CATEGORY);

        orphanCls = cm.getICDCategory(ORPHANS_CLASS);
        if (orphanCls == null) {
            orphanCls = cm.createICDCategory(ORPHANS_CLASS, ICTM_CATEGORY);
        }

        try {
        BufferedReader input = new BufferedReader(new FileReader(csvPath));
        input.readLine();

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
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at parsing csv", e);
        }

        createHierarchy();

      // ((JenaOWLModel) owlModel).save(URIUtilities.createURI("/work/src/GWT/web-protege/war/projects/ictm/ictm_umbrella.owl"));
      //  prj.save(new ArrayList());
    }


    private static void processLine(String line) {
        try {
            Log.getLogger().info(line);
            final String[] split = line.split("\t");

            final String name = "TM" + getSafeValue(split, 0) + getSafeValue(split, 1) + getSafeValue(split, 2);
            final String sortingLabel = getSortingLabel(split);
            final String originalSource = getSafeValue(split, 3);
            final String originalCode = getSafeValue(split, 4);
            final String englishTitleValue = getSafeValue(split, 5);
            final String chineseTitleValue = getSafeValue(split, 6);
            final String japaneseTitleValue = getSafeValue(split, 7);
            final String koreanTitleValue = getSafeValue(split, 8);
            final String englishDefinitionValue = getSafeValue(split, 9);
            final String chineseDefinitionValue = getSafeValue(split, 10);
            final String japaneseDefinitionValue = getSafeValue(split, 11);
            final String koreanDefinitionValue = getSafeValue(split, 12);
            final String type = getSafeValue(split, 13);

            final RDFSNamedClass cls = cm.createICDCategory(name, ORPHANS_CLASS);

            cls.setPropertyValue(cm.getSortingLabelProperty(), sortingLabel);
            createTitleTerm(cm, cls, englishTitleValue, ENGLISH);
            createTitleTerm(cm, cls, chineseTitleValue, CHINESE);
            createTitleTerm(cm, cls, japaneseTitleValue, JAPANESE);
            createTitleTerm(cm, cls, koreanTitleValue, KOREAN);
            createDefinitionTerm(cm, cls, englishDefinitionValue, ENGLISH, originalSource);
            createDefinitionTerm(cm, cls, chineseDefinitionValue, CHINESE, originalSource);
            createDefinitionTerm(cm, cls, japaneseDefinitionValue, JAPANESE, originalSource);
            createDefinitionTerm(cm, cls, koreanDefinitionValue, KOREAN, originalSource);

            createType(cls, type);

            // by default we use the chinese language and title - this should be true for IST WPRO, GB95 and GB97
            String codeLanguage = CHINESE;
            String codeLabel = chineseTitleValue;
            if (originalSource.contains("AAC")) {
                codeLanguage = ENGLISH;
                codeLabel = englishTitleValue;
            } else if (originalSource.contains("KCDOM")) {
                codeLanguage = KOREAN;
                codeLabel = koreanTitleValue;
            } else if (originalSource.contains("JSOM") || originalSource.contains("KMPC")) {
                codeLanguage = JAPANESE;
                codeLabel = japaneseTitleValue;
            }
            if (originalSource.length() != 0) {
                createReferenceTerm(cm, cls, null, codeLabel, codeLanguage, originalCode, originalSource);
            }


        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at import", e);
        }

    }

    private static String getSortingLabel(String[] split) {
        String label = getSafeValue(split, 0);
        if ( getSafeValue(split, 1).length() > 0) {
            label = label + ICTM_ROOT_HIERARCHY_NAME + getSafeValue(split, 1);
        }
        if (getSafeValue(split, 2).length() > 0) {
            label = label + ICTM_ROOT_HIERARCHY_NAME + getSafeValue(split, 2);
        }
       return label;
    }

    private static String getSafeValue(final String[] split, final int index) {
        if (index >= split.length) {
            return "";
        }
        String string = split[index];
        if (string == null || string.length() == 0) {
            return "";
        }
        return string.replaceAll("\"", "");
    }

    private static void createTitleTerm(ICDContentModel cm, RDFSNamedClass cls, String titleValue, final String lang) {
        if (titleValue != null && titleValue.trim().length() > 0) {
            RDFResource titleTerm = cm.createTitleTerm();
            cm.fillTerm(titleTerm, null, titleValue, lang);
            cm.addTitleTermToClass(cls, titleTerm);
        }
    }

    private static void createDefinitionTerm(ICDContentModel cm, RDFSNamedClass cls, String titleValue, final String lang, final String source) {
        if (titleValue != null && titleValue.trim().length() > 0) {
            RDFResource definitionTerm = cm.createDefinitionTerm();
            cm.fillTerm(definitionTerm, null, titleValue, lang);
            cm.addDefinitionTermToClass(cls, definitionTerm);
            definitionTerm.setPropertyValue(sourceProp, source);
        }
    }

    private static void createReferenceTerm(ICDContentModel cm, RDFSNamedClass cls, final String id, String labelValue, final String lang, final String termId, String ontologyId) {
        if (labelValue != null && labelValue.trim().length() > 0) {
            RDFResource referenceTerm = cm.createReferenceTerm();
            cm.fillTerm(referenceTerm, id, labelValue, lang, ontologyId);
            referenceTerm.setPropertyValue(termIdProp, termId);
            cls.addPropertyValue(codesProp, referenceTerm);

        }
    }

    private static void createType(RDFSNamedClass cls, String type) {
        if (type == null || type.length() == 0) {
            return;
        }
        if (type.equals("D")) {
            cls.setPropertyValue(typeProp, diseaseType);
        } else if (type.equals("P")) {
            cls.setPropertyValue(typeProp, patternType);
        }
    }

    private static void createHierarchy() {
        Collection<RDFSNamedClass> clses = new ArrayList<RDFSNamedClass>(orphanCls.getSubclasses(false));
        for (RDFSNamedClass cls : clses) {
            RDFSNamedClass parent = getParent(cls);
            if (parent != null) {
                cls.addSuperclass(parent);
                cls.removeSuperclass(orphanCls);
            }
        }

    }

    private static RDFSNamedClass getParent(RDFSNamedClass cls) {
        String name = NamespaceUtil.getLocalName(cls.getName());
        if (name.length() == 4) { //first level 2+2
            return ictmCatCls;
        } else if (name.length() == 6) { //second level 2+2+2
            return cls.getOWLModel().getRDFSNamedClass(name.substring(0, 4));
        } else if (name.length() == 9) {//third level 2+2+2+3 {
            return cls.getOWLModel().getRDFSNamedClass(name.substring(0, 6));
        }
        return null;
    }

}
