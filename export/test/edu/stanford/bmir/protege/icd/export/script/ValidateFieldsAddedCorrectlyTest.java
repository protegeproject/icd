package edu.stanford.bmir.protege.icd.export.script;

import junit.framework.TestCase;

/**
 * Code that both the ICDImporterTest and the ExportScriptWrapperTest share.
 * <p/>
 * This allows us to check the output of one and then the output of the other (both should be identical to each other).
 * <p/>
 * Note that when the ICDImporterTest fails, we need to run the ExportScriptWrapperTest
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public abstract class ValidateFieldsAddedCorrectlyTest extends TestCase {
    protected static int RETIRED_POSITION = 0;
    protected static int HASTYPE_LABEL_POSITION = 12;
    protected static int SORTINGLABEL_POSITION = 13;
    protected static int ORIGINAL_PARENT_POSITION = 14;
    protected static int ICDCODE_POSITION = 15;
    protected static int TEXTUALDEFINITION_POSITION = 16;
    protected static int DETAILEDDEFINITION_POSITION = 17;
    protected static int SYNONYM_LABEL_POSITION = 18;
    protected static int INCLUSION_LABEL_POSITION = 19;
    protected static int EXCLUSION_LABEL_POSITION = 20;
    protected static int LATERALITY_POSITION = 21;
    protected static int BODYSYSTEM_LABEL_POSITION = 22;
    protected static int BODY_PART_LABEL_POSITION = 23;
    protected static int BODY_PART_SHORT_TERM_ID_POSITION = 24;
    protected static int MORPHOLOGICALLYABNORMALSTRUCTURE_LABEL_POSITION = 25;
    protected static int MORPHOLOGICALLYABNORMALSTRUCTURE_SHORTTERMID_POSITION = 26;
    protected static int TEMPORALPROPERTIES_LABEL_POSITION = 27;
    protected static int TEMPORALPROPERTIES_TERMID_POSITION = 28;
    protected static int SEVERITY_LEVEL_POSITION = 29;
    protected static int SEVERITY_TERMID_POSITION = 30;
    protected static int AUTHORINGNOTE_POSITION = 31;
    protected static int CLASS_NAME_POSITION = 32;


    public void testConvertsShortDefinitionGreaterThan100WordsIntoDetailedDefinition() throws Exception {
        String topNode = "http://who.int/icd#Class_2554";
        initializeTest(topNode, "output/short-definition.csv", "output/short-definition.xsl");
        assertEquals("", getFieldContents(HASTYPE_LABEL_POSITION, 3));
        assertEquals("C43a.3", getFieldContents(SORTINGLABEL_POSITION, 3));
        assertEquals("", getFieldContents(ORIGINAL_PARENT_POSITION, 3));
        assertEquals("", getFieldContents(ICDCODE_POSITION, 3));
        assertTrue(getFieldContents(TEXTUALDEFINITION_POSITION, 3).trim().length() == 0);
        assertTrue(getFieldContents(DETAILEDDEFINITION_POSITION, 3).length() > 100);
        assertEquals("", getFieldContents(SYNONYM_LABEL_POSITION, 3));
        assertEquals("Invasive malignant melanoma of the nail apparatus", getFieldContents(INCLUSION_LABEL_POSITION, 3));
        assertEquals("Acral lentiginous melanoma in situ", getFieldContents(EXCLUSION_LABEL_POSITION, 3));
        assertEquals("Skin", getFieldContents(BODYSYSTEM_LABEL_POSITION, 3));
        assertEquals("Skin", getFieldContents(BODY_PART_LABEL_POSITION, 3));
        assertEquals("181469002", getFieldContents(BODY_PART_SHORT_TERM_ID_POSITION, 3));
        assertEquals("", getFieldContents(MORPHOLOGICALLYABNORMALSTRUCTURE_LABEL_POSITION, 3));
        assertEquals("", getFieldContents(MORPHOLOGICALLYABNORMALSTRUCTURE_SHORTTERMID_POSITION, 3));
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        validateNeverPopulatedFields();
    }

    public void testTypeAndSynonym() throws Exception {
        String topNode = "http://who.int/icd#Class_2635";
        initializeTest(topNode, "output/type-and-synonym.csv", "output/type-and-synonym.xsl");

        assertEquals("Disease_Type", getFieldContents(HASTYPE_LABEL_POSITION, 3));
        assertEquals("C44a.0", getFieldContents(SORTINGLABEL_POSITION, 3));
        assertEquals("", getFieldContents(ORIGINAL_PARENT_POSITION, 3));
        assertEquals("", getFieldContents(ICDCODE_POSITION, 3));
        assertTrue(getFieldContents(TEXTUALDEFINITION_POSITION, 3).trim().length() == 0);
        assertTrue(getFieldContents(DETAILEDDEFINITION_POSITION, 3).length() > 100);
        assertEquals("Basal cell epithelioma || Rodent ulcer", getFieldContents(SYNONYM_LABEL_POSITION, 3));
        assertEquals("BCC", getFieldContents(INCLUSION_LABEL_POSITION, 3));
        assertEquals("", getFieldContents(EXCLUSION_LABEL_POSITION, 3));
        assertEquals("", getFieldContents(BODYSYSTEM_LABEL_POSITION, 3));
        assertEquals("", getFieldContents(BODY_PART_LABEL_POSITION, 3));
        assertEquals("", getFieldContents(BODY_PART_SHORT_TERM_ID_POSITION, 3));
        assertEquals("", getFieldContents(MORPHOLOGICALLYABNORMALSTRUCTURE_LABEL_POSITION, 3));
        assertEquals("", getFieldContents(MORPHOLOGICALLYABNORMALSTRUCTURE_SHORTTERMID_POSITION, 3));
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        validateNeverPopulatedFields();
    }

    public void testIcdCode() throws Exception {
        String topNode = "http://who.int/icd#K23.0";
        initializeTest(topNode, "output/icd-code.csv", "output/icd-code.xsl");
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        assertEquals("K23.0", getFieldContents(ICDCODE_POSITION, 3));
        assertEquals("K23.0", getFieldContents(SORTINGLABEL_POSITION, 3));
        assertEquals("", getFieldContents(INCLUSION_LABEL_POSITION, 3));
        assertEquals("Tuberculosis of other specified organs || Infectious oesophagitis coded elsewhere", getFieldContents(ORIGINAL_PARENT_POSITION, 3));
        validateNeverPopulatedFields();
    }

    public void testDoesNotPopulateTemporalColumns() throws Exception {
        String topNode = "http://who.int/icd#O20.9";
        initializeTest(topNode, "output/temporal-columns.csv", "output/temporal-columns.xls");

        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        validateNeverPopulatedFields();
    }

    public void validateNeverPopulatedFields() {
        assertEquals("", getFieldContents(RETIRED_POSITION, 3));
        assertEquals("", getFieldContents(LATERALITY_POSITION, 3));
        assertEquals("", getFieldContents(TEMPORALPROPERTIES_LABEL_POSITION, 3));
        assertEquals("", getFieldContents(TEMPORALPROPERTIES_TERMID_POSITION, 3));
        assertEquals("", getFieldContents(SEVERITY_LEVEL_POSITION, 3));
        assertEquals("", getFieldContents(SEVERITY_TERMID_POSITION, 3));
        assertEquals("", getFieldContents(AUTHORINGNOTE_POSITION, 3));
    }

    protected abstract String getFieldContents(int fieldPosition, int rowNumber);

    protected abstract void initializeTest(String topNode, String csvFileName, String excelOutputFileName) throws Exception;
}
