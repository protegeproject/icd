package edu.stanford.bmir.protege.icd.export.script;
/**
 * @author Jack Elliott <jacke@stanford.edu>
 */

import edu.stanford.bmir.protege.icd.export.ValidateFieldsAddedCorrectlyTest;
import edu.stanford.bmir.protege.icd.export.excel.CsvReader;
import edu.stanford.smi.protege.model.Project;

import java.io.File;
import java.util.ArrayList;

public class ExportScriptWrapperTest extends ValidateFieldsAddedCorrectlyTest {
    private CsvReader reader;

    @Override
    protected String getFieldContents(final int fieldPosition, int rowNumber) {
        while (reader.getCurrentRow() < rowNumber - 1) {
            reader.nextRow();
        }
        return reader.getEntry(fieldPosition);
    }

    @Override
    protected void initializeTest(final String csvFileName, String excelOutputFileName, String... topNode) throws Exception {
        File file = new File(csvFileName);
        if (file.exists()) {
            file.delete();
        }
        final Project project = Project.loadProjectFromFile("pprj/icd_umbrella.pprj", new ArrayList());
        ExportScriptWrapper unit = new ExportScriptWrapper(project, "resources/export_script.py");
        unit.exportToFile(csvFileName, topNode);
        reader = new CsvReader(csvFileName, 1);
    }


    public void testSubsequentRowsWithSameClassHaveOnlyTitleCodeAndClass() throws Exception {
        String topNode = "http://who.int/icd#Class_2554";
        initializeTest("output/short-definition.csv", "output/short-definition.xsl", topNode, topNode);
        assertEquals("Acral lentiginous malignant melanoma", getFieldContents(TOP_NODE_TITLE_FIELD_POSITION, 3));
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
        assertEquals("Acral lentiginous malignant melanoma", getFieldContents(TOP_NODE_TITLE_FIELD_POSITION, 3));
        assertEquals("", getFieldContents(HASTYPE_LABEL_POSITION, 4));
        assertEquals("", getFieldContents(SORTINGLABEL_POSITION, 4));
        assertEquals("", getFieldContents(ORIGINAL_PARENT_POSITION, 4));
        assertEquals("", getFieldContents(ICDCODE_POSITION, 4));
        assertEquals("", getFieldContents(TEXTUALDEFINITION_POSITION, 4));
        assertEquals("", getFieldContents(DETAILEDDEFINITION_POSITION, 4));
        assertEquals("", getFieldContents(SYNONYM_LABEL_POSITION, 4));
        assertEquals("", getFieldContents(INCLUSION_LABEL_POSITION, 4));
        assertEquals("", getFieldContents(EXCLUSION_LABEL_POSITION, 4));
        assertEquals("", getFieldContents(BODYSYSTEM_LABEL_POSITION, 4));
        assertEquals("", getFieldContents(BODY_PART_LABEL_POSITION, 4));
        assertEquals("", getFieldContents(BODY_PART_SHORT_TERM_ID_POSITION, 4));
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 4));
        validateNeverPopulatedFields(4);
    }


    public void testSubsequentRowsPopulateIcdCode() throws Exception {
        String topNode = "http://who.int/icd#K23.0";
        initializeTest("output/icd-code.csv", "output/icd-code.xsl", topNode, topNode);
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        assertEquals("K23.0", getFieldContents(ICDCODE_POSITION, 3));
        assertEquals("K23.0", getFieldContents(SORTINGLABEL_POSITION, 3));
        assertEquals("", getFieldContents(INCLUSION_LABEL_POSITION, 3));
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 3));
        assertEquals("Tuberculosis of other specified organs || Infectious oesophagitis coded elsewhere", getFieldContents(ORIGINAL_PARENT_POSITION, 3));
        validateNeverPopulatedFields(3);
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 4));
        assertEquals("K23.0", getFieldContents(ICDCODE_POSITION, 4));
        assertEquals("", getFieldContents(SORTINGLABEL_POSITION, 4));
        assertEquals("", getFieldContents(INCLUSION_LABEL_POSITION, 4));
        assertEquals("", getFieldContents(ORIGINAL_PARENT_POSITION, 4));
        assertEquals(topNode, getFieldContents(CLASS_NAME_POSITION, 4));
        validateNeverPopulatedFields(4);
    }

    public void testInfiniteDepthRecursionWillFail() throws Exception {
        String topNode1 = "http://who.int/icd#660_0120d721_21a2_4c15_9bd8_f4e7b2fbba1e";
        try {
            initializeTest("output/duplicate-rows.csv", "output/icd-code.xsl", topNode1);
        } catch (Exception e) {
            assertEquals("AssertionError: Could not export 'http://who.int/icd#154_0120d721_21a2_4c15_9bd8_f4e7b2fbba1e' because the class depth is too great", e.getMessage());
        }
    }
}