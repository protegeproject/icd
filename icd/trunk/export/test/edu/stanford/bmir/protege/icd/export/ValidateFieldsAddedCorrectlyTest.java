package edu.stanford.bmir.protege.icd.export;

import junit.framework.TestCase;

/**
 * Code that both the ICDCsvToExcelConverterTest and the ExportScriptWrapperTest share.
 * <p/>
 * This allows us to check the output of one and then the output of the other (both should be identical to each other).
 * <p/>
 * Note that when the ICDCsvToExcelConverterTest fails, we need to run the ExportScriptWrapperTest
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public abstract class ValidateFieldsAddedCorrectlyTest extends TestCase {
    protected static final int TOP_NODE_TITLE_FIELD_POSITION = 1;
    protected static final int RETIRED_POSITION = 0;
    protected static final int HASTYPE_LABEL_POSITION = 12;
    protected static final int SORTINGLABEL_POSITION = 13;
    protected static final int ORIGINAL_PARENT_POSITION = 14;
    protected static final int ICDCODE_POSITION = 15;
    protected static final int TEXTUALDEFINITION_POSITION = 16;
    protected static final int DETAILEDDEFINITION_POSITION = 17;
    protected static final int SYNONYM_LABEL_POSITION = 18;
    protected static final int INCLUSION_LABEL_POSITION = 19;
    protected static final int EXCLUSION_LABEL_POSITION = 20;
    protected static final int LATERALITY_POSITION = 21;
    protected static final int BODYSYSTEM_LABEL_POSITION = 22;
    protected static final int BODY_PART_LABEL_POSITION = 23;
    protected static final int BODY_PART_SHORT_TERM_ID_POSITION = 24;
    protected static final int MORPHOLOGICALLYABNORMALSTRUCTURE_LABEL_POSITION = 25;
    protected static final int MORPHOLOGICALLYABNORMALSTRUCTURE_SHORTTERMID_POSITION = 26;
    protected static final int TEMPORALPROPERTIES_LABEL_POSITION = 27;
    protected static final int TEMPORALPROPERTIES_TERMID_POSITION = 28;
    protected static final int SEVERITY_LEVEL_POSITION = 29;
    protected static final int SEVERITY_TERMID_POSITION = 30;
    protected static final int AUTHORINGNOTE_POSITION = 31;
    protected static final int CLASS_NAME_POSITION = 32;


    public void testConvertsShortDefinitionGreaterThan100WordsIntoDetailedDefinition() throws Exception {
        String topNode = "http://who.int/icd#Class_2554";
        initializeTest("output/short-definition.csv", "output/short-definition.xsl", topNode);
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
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        validateNeverPopulatedFields(3);
    }

    public void testTypeAndSynonym() throws Exception {
        String topNode = "http://who.int/icd#Class_2635";
        initializeTest("output/type-and-synonym.csv", "output/type-and-synonym.xsl", topNode);

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
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        validateNeverPopulatedFields(3);
    }

    public void testIcdCodeAndOriginalParent() throws Exception {
        String topNode = "http://who.int/icd#K23.0";
        initializeTest("output/icd-code.csv", "output/icd-code.xsl", topNode);
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        assertEquals("K23.0", getFieldContents(ICDCODE_POSITION, 3));
        assertEquals("K23.0", getFieldContents(SORTINGLABEL_POSITION, 3));
        assertEquals("", getFieldContents(INCLUSION_LABEL_POSITION, 3));
        assertEquals("Tuberculosis of other specified organs || Infectious oesophagitis coded elsewhere", getFieldContents(ORIGINAL_PARENT_POSITION, 3));
        validateNeverPopulatedFields(3);
    }

    public void testDoesNotPopulateTemporalColumns() throws Exception {
        String topNode = "http://who.int/icd#O20.9";
        initializeTest("output/temporal-columns.csv", "output/temporal-columns.xls", topNode);
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        validateNeverPopulatedFields(3);
    }

    public void validateNeverPopulatedFields(final int rowNumber) {
        assertEquals("", getFieldContents(RETIRED_POSITION, rowNumber));
        assertEquals("", getFieldContents(LATERALITY_POSITION, rowNumber));
        assertEquals("", getFieldContents(SEVERITY_LEVEL_POSITION, rowNumber));
        assertEquals("", getFieldContents(SEVERITY_TERMID_POSITION, rowNumber));
        assertEquals("", getFieldContents(AUTHORINGNOTE_POSITION, rowNumber));
        // These are always empty, according to ST:
        assertEquals("", getFieldContents(TEMPORALPROPERTIES_LABEL_POSITION, rowNumber));
        assertEquals("", getFieldContents(TEMPORALPROPERTIES_TERMID_POSITION, rowNumber));
        assertEquals("", getFieldContents(MORPHOLOGICALLYABNORMALSTRUCTURE_LABEL_POSITION, 3));
        assertEquals("", getFieldContents(MORPHOLOGICALLYABNORMALSTRUCTURE_SHORTTERMID_POSITION, 3));
    }

    protected abstract String getFieldContents(int fieldPosition, int rowNumber);

    protected abstract void initializeTest(String csvFileName, String excelOutputFileName, String... topNode) throws Exception;
}
