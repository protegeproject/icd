package edu.stanford.bmir.icd.utils;

import edu.stanford.bmir.icd.claml.ICDContentModel;
import edu.stanford.bmir.icd.claml.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ImportTM {
    private static final String ORPHANS_CLASS = "http://who.int/ictm#Orphans";
    private static final String ICTM_ROOT_HIERARCHY_NAME = ".";
    private static final String ICTM_CATEGORY = "http://who.int/ictm#ICTMCategory";
    private static final String ENGLISH = "en";
    private static final String CHINESE = "cn";
    private static final String JAPANESE = "jp";
    private static final String KOREAN = "kr";

    private RDFProperty sourceProperty;
    private RDFProperty codesProperty;
    private RDFProperty termIdProperty;

    private final Map<String, String> duplicateHierarchyValues = new HashMap<String, String>();
    private final Map<String, String> hierarchyValuesToClassNamesMap = new HashMap<String, String>();
    private final Map<String, String> hierarchyNamesToChapterCodes;

    public static void main(String[] args) {
        Project prj = Project.loadProjectFromFile(
                "/home/jacke/Temp/20101201/db_ictm_cm/ictm_umbrella.pprj", new ArrayList());
        File csvFile = new File("/home/jacke/Temp/20101201/TM_Import.csv");

        final OWLModel tmKb = (OWLModel) prj.getKnowledgeBase();
        final ICDContentModel cm = new ICDContentModel(tmKb);

        final Map<String, String> hierarchyNamesToChapterCodes = initializeNamesToChaptersMap();

        final RDFSNamedClass orphans = cm.getICDCategory(ORPHANS_CLASS);
        if (orphans == null) {
            cm.createICDCategory(ORPHANS_CLASS, ICTM_CATEGORY);
        }

        ImportTM importer = new ImportTM(
                tmKb.getRDFProperty("http://who.int/ictm#" + "codes"),
                tmKb.getRDFProperty(ICDContentModelConstants.NS + "source"),
                tmKb.getRDFProperty(ICDContentModelConstants.TERM_ID_PROP),
                hierarchyNamesToChapterCodes);

        importer.importFile(prj, tmKb, cm, csvFile);

    }

    public ImportTM(RDFProperty codesProperty, RDFProperty sourceProperty, RDFProperty termIdProperty, Map<String, String> hierarchyNamesToChapterCodes) {
        this.codesProperty = codesProperty;
        this.sourceProperty = sourceProperty;
        this.termIdProperty = termIdProperty;
        this.hierarchyNamesToChapterCodes = hierarchyNamesToChapterCodes;
    }

    private void importFile(Project prj, OWLModel tmKb, ICDContentModel cm, File file) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-16")));
            try {
                String line = null;

                //skip first 2 lines...
                input.readLine();
                input.readLine();
                while ((line = input.readLine()) != null) {
                    if (line != null) {
                        processLine(cm, line, tmKb);
                    }
                }
                final RDFSNamedClass orphansClass = cm.getICDCategory(ORPHANS_CLASS);
                hierarchyValuesToClassNamesMap.put(ICTM_ROOT_HIERARCHY_NAME, ICTM_CATEGORY);
                final Set<Map.Entry<String, String>> allLeafNodeEntries = new HashSet<Map.Entry<String, String>>(hierarchyValuesToClassNamesMap.entrySet());
                for (Map.Entry<String, String> entry : allLeafNodeEntries) {
                    addParentsToLeafNode(cm, orphansClass, entry.getKey(), entry.getValue());
                }
            } finally {
                if (!duplicateHierarchyValues.isEmpty()) {
                    System.out.println("Duplicate codes detected:");
                }
                for (Map.Entry<String, String> duplicateEntry : duplicateHierarchyValues.entrySet()) {
                    System.out.println("hierarchy value: " + duplicateEntry.getKey() + ", class name: " + duplicateEntry.getValue());
                }
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        prj.save(new ArrayList());
    }

    private void addParentsToLeafNode(ICDContentModel cm, RDFSNamedClass orphansClass, final String currentNodeHierarchyName, final String currentNodeClassName) {
        if (currentNodeHierarchyName.equals(ICTM_ROOT_HIERARCHY_NAME)) {
            return;
        }
        final String parentHierarchyName = getParentHierarchyName(currentNodeHierarchyName);
        String parentClassName = hierarchyValuesToClassNamesMap.get(parentHierarchyName);
        if (parentClassName == null) {
            final RDFSNamedClass parent = createParentClasses(cm, parentHierarchyName);
            parentClassName = parent.getName();
        }
        if (parentClassName != null || parentClassName.trim().length() > 0) {
            final RDFSNamedClass parentClass = cm.getICDCategory(parentClassName);
            final RDFSNamedClass childClass = cm.getICDCategory(currentNodeClassName);
            childClass.addDirectSuperclass(parentClass);
            childClass.removeDirectSuperclass(orphansClass);
        }
    }

    private RDFSNamedClass createParentClasses(final ICDContentModel cm, final String parentHierarchyName) {
        final String parentClassName = hierarchyValuesToClassNamesMap.get(parentHierarchyName);
        if (parentClassName == null) {
            String grandParentHierarchyName = getParentHierarchyName(parentHierarchyName);
            if (grandParentHierarchyName.trim().length() == 0) {
                grandParentHierarchyName = ICTM_ROOT_HIERARCHY_NAME;
            }
            final RDFSNamedClass grandParent = createParentClasses(cm, grandParentHierarchyName);
            final RDFSNamedClass parent = cm.createICDCategory(null, grandParent.getName());
            if (hierarchyNamesToChapterCodes.containsKey(parentHierarchyName)) {
                final String chapterName = hierarchyNamesToChapterCodes.get(parentHierarchyName);
                createTitleTerm(cm, parent, chapterName, ENGLISH);
            }
            parent.setPropertyValue(cm.getSortingLabelProperty(), parentHierarchyName);
            hierarchyValuesToClassNamesMap.put(parentHierarchyName, parent.getName());
            return parent;
        }
        return cm.getICDCategory(parentClassName);
    }

    private void processLine(ICDContentModel cm, String line, OWLModel tmKb) {
        try {
            System.out.println(line);
            final String[] split = line.split("\t");
            //TODO: put the zeroth column into a to-be-determined field...
            final String sortingLabel = split[0] + ICTM_ROOT_HIERARCHY_NAME + split[1] + ICTM_ROOT_HIERARCHY_NAME + split[2];
            final String originalSource = getSafeValue(split, 3);
            final String originalCode = getSafeValue(split, 4);
            final String englishTitleValue = split[5];
            final String chineseTitleValue = getSafeValue(split, 6);
            final String japaneseTitleValue = getSafeValue(split, 7);
            final String koreanTitleValue = getSafeValue(split, 8);
            final String englishDefinitionValue = getSafeValue(split, 9);
            final String chineseDefinitionValue = getSafeValue(split, 10);
            final String hierarchyValue = sortingLabel;

            final RDFSNamedClass cls = cm.createICDCategory(null, ORPHANS_CLASS);
            if (hierarchyValuesToClassNamesMap.containsKey(hierarchyValue)) {
                duplicateHierarchyValues.put(hierarchyValue, hierarchyValuesToClassNamesMap.get(hierarchyValue));
            }
            hierarchyValuesToClassNamesMap.put(hierarchyValue, cls.getName());
            if (cls == null) {
                Log.getLogger().info("***& failed to create class not found");
            } else {
                cls.setPropertyValue(cm.getSortingLabelProperty(), sortingLabel);
                createTitleTerm(cm, cls, englishTitleValue, ENGLISH);
                createTitleTerm(cm, cls, chineseTitleValue, CHINESE);
                createTitleTerm(cm, cls, japaneseTitleValue, JAPANESE);
                createTitleTerm(cm, cls, koreanTitleValue, KOREAN);
                createDefinitionTerm(cm, cls, englishDefinitionValue, ENGLISH, originalSource);
                createDefinitionTerm(cm, cls, chineseDefinitionValue, CHINESE, originalSource);
                // by default we use the chinese language and title - this should be true for IST WPRO, GB95 and GB97
                String codeLanguage = CHINESE;
                String codeLabel = chineseTitleValue;
                if ("AAC".equals(originalSource)) {
                    codeLanguage = ENGLISH;
                    codeLabel = englishTitleValue;
                } else if ("KCDOM3".equals(originalSource)) {
                    codeLanguage = KOREAN;
                    codeLabel = koreanTitleValue;
                } else if ("JSOM".equals(originalSource)) {
                    codeLanguage = JAPANESE;
                    codeLabel = japaneseTitleValue;
                }
                createReferenceTerm(cm, cls, null, codeLabel, codeLanguage, originalCode, originalSource);

            }
        } catch (Exception e) {
            Log.getLogger().log(Level.WARNING, "Error at import", e);
        }
    }

    private String getParentHierarchyName(String entry) {
        if (!entry.contains(ICTM_ROOT_HIERARCHY_NAME)) {
            return "";
        }
        return entry.substring(0, entry.lastIndexOf(ICTM_ROOT_HIERARCHY_NAME));
    }

    private String getSafeValue(final String[] split, final int index) {
        return split.length > index ? split[index] : "";
    }

    private void createTitleTerm(ICDContentModel cm, RDFSNamedClass cls, String titleValue, final String lang) {
        if (titleValue != null && titleValue.trim().length() > 0) {
            RDFResource titleTerm = cm.createTitleTerm();
            cm.fillTerm(titleTerm, null, titleValue, lang);
            cm.addTitleTermToClass(cls, titleTerm);
        }
    }

    private void createDefinitionTerm(ICDContentModel cm, RDFSNamedClass cls, String titleValue, final String lang, final String source) {
        if (titleValue != null && titleValue.trim().length() > 0) {
            RDFResource definitionTerm = cm.createDefinitionTerm();
            cm.fillTerm(definitionTerm, null, titleValue, lang);
            cm.addDefinitionTermToClass(cls, definitionTerm);
            definitionTerm.setPropertyValue(sourceProperty, source);
        }
    }

    private void createReferenceTerm(ICDContentModel cm, RDFSNamedClass cls, final String id, String labelValue, final String lang, final String termId, String ontologyId) {
        if (labelValue != null && labelValue.trim().length() > 0) {
            RDFResource referenceTerm = cm.createReferenceTerm();
            cm.fillTerm(referenceTerm, id, labelValue, lang, ontologyId);
            referenceTerm.setPropertyValue(termIdProperty, termId);
            cls.addPropertyValue(codesProperty, referenceTerm);

        }
    }


    private static Map<String, String> initializeNamesToChaptersMap() {
        final Map<String, String> hierarchyNamesToChapterCodes = new HashMap<String, String>();
        hierarchyNamesToChapterCodes.put("01", "Internal Medicine (TM)");
        hierarchyNamesToChapterCodes.put("02", "External Medicine (TM)");
        hierarchyNamesToChapterCodes.put("03", "Ears, Nose, and Throat");
        hierarchyNamesToChapterCodes.put("04", "Ophthalmology");
        hierarchyNamesToChapterCodes.put("05", "Mental / Behavioral");
        hierarchyNamesToChapterCodes.put("06", "Circulatory");
        hierarchyNamesToChapterCodes.put("07", "Oncology");
        hierarchyNamesToChapterCodes.put("08", "8 Principles");
        hierarchyNamesToChapterCodes.put("09", "Aspect Patterns");
        hierarchyNamesToChapterCodes.put("10", "Meridians");
        hierarchyNamesToChapterCodes.put("11", "Patterns – 1 (wind, cold, blood, qi, etc)");
        hierarchyNamesToChapterCodes.put("12", "Patterns – 2 (dampness / dryness / phlegm / liquid / etc)");
        hierarchyNamesToChapterCodes.put("13", "Patterns – 3 (zang fu / viscera)");
        hierarchyNamesToChapterCodes.put("14", "Patterns – 4 (thus far unclassified)");
        hierarchyNamesToChapterCodes.put("15", "Neuromusculoskeletal");
        hierarchyNamesToChapterCodes.put("16", "Essentials – 1 (qi / blood /yin)");
        hierarchyNamesToChapterCodes.put("17", "Essentials – 2 (fluid / humors / water / phlegm / etc)");
        hierarchyNamesToChapterCodes.put("18", "Obstetrics / Gynecology / Conditions of the Female");
        hierarchyNamesToChapterCodes.put("19", "Conditions of the Male");
        hierarchyNamesToChapterCodes.put("20", "Injuries and External Causes");
        hierarchyNamesToChapterCodes.put("21", "Pediatrics");
        hierarchyNamesToChapterCodes.put("22", "Sangjiao");
        hierarchyNamesToChapterCodes.put("23", "Other");
        hierarchyNamesToChapterCodes.put("24", "Thus Far Unclassifieds");
        return hierarchyNamesToChapterCodes;
    }
}
