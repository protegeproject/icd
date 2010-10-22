package edu.stanford.bmir.protege.web.script;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ErrorCheckerTest extends TestCase {
    public void testActualCsvMissingColumn(){
        Set<String> expectedCsv = new HashSet<String>();
        expectedCsv.addAll(Arrays.asList("a", "b", "c","d", "e"));
        String[] actualCsv = new String[]{"a", "b", "d", "", ""};
        Set<String> expectedExcel = new HashSet<String>();
        expectedExcel.addAll(Arrays.asList("x", "y", "z"));
        Set<String> actualExcel = new HashSet<String>();
        actualExcel.addAll(Arrays.asList("x", "y", "z"));

        ErrorChecker unit = new ErrorChecker(actualCsv, actualExcel, expectedCsv, expectedExcel, 2);
        assertEquals(0, unit.numberOfWarnings());
        assertEquals(2, unit.numberOfErrors());
        assertTrue(unit.generateReport(), unit.generateReport().trim().endsWith("error: expected column(s) [e, c] in csv file, but could not find them. \n" +
                "\t\t actual column names were [a, b, d]"));
    }

    public void testExtraActualCsvColumn(){
        Set<String> expectedCsv = new HashSet<String>();
        expectedCsv.addAll(Arrays.asList("a", "b", "c","d", "e"));
        String[] actualCsv = new String[]{"a", "b", "c", "d","e", "f","g", "", ""};
        Set<String> expectedExcel = new HashSet<String>();
        expectedExcel.addAll(Arrays.asList("x", "y", "z"));
        Set<String> actualExcel = new HashSet<String>();
        actualExcel.addAll(Arrays.asList("x", "y", "z"));

        ErrorChecker unit = new ErrorChecker(actualCsv, actualExcel, expectedCsv, expectedExcel, 2);
        assertEquals(2, unit.numberOfWarnings());
        assertEquals(0, unit.numberOfErrors());
        assertTrue(unit.generateReport().trim().endsWith("warning: found extra unexpected column(s) [f, g] in csv file."));
    }

    public void testActualExcelMissingColumns(){
        Set<String> expectedCsv = new HashSet<String>();
        expectedCsv.addAll(Arrays.asList("a", "b", "c","d", "e"));
        String[] actualCsv = new String[]{"a", "b", "c", "d","e", "", ""};
        Set<String> expectedExcel = new HashSet<String>();
        expectedExcel.addAll(Arrays.asList("x", "y", "z", "aa", "bb"));
        Set<String> actualExcel = new HashSet<String>();
        actualExcel.addAll(Arrays.asList("x", "y", "z"));

        ErrorChecker unit = new ErrorChecker(actualCsv, actualExcel, expectedCsv, expectedExcel, 2);
        assertEquals(0, unit.numberOfWarnings());
        assertEquals(2, unit.numberOfErrors());
        assertTrue(unit.generateReport(), unit.generateReport().trim().endsWith("error: expected column(s) [aa, bb] in excel file, but could not find them.\n" +
                "\t\t actual column names were [x, y, z]"));
    }

    public void testExtraActualExcelColumns(){
        Set<String> expectedCsv = new HashSet<String>();
        expectedCsv.addAll(Arrays.asList("a", "b", "c","d", "e"));
        String[] actualCsv = new String[]{"a", "b", "c", "d","e", "", ""};
        Set<String> expectedExcel = new HashSet<String>();
        expectedExcel.addAll(Arrays.asList("x", "y", "z"));
        Set<String> actualExcel = new HashSet<String>();
        actualExcel.addAll(Arrays.asList("x", "y", "z", "aa", "bb"));

        ErrorChecker unit = new ErrorChecker(actualCsv, actualExcel, expectedCsv, expectedExcel, 2);
        assertEquals(2, unit.numberOfWarnings());
        assertEquals(0, unit.numberOfErrors());
        assertTrue(unit.generateReport().trim().endsWith("warning: found extra unexpected column(s) [aa, bb] in excel file."));
    }

    public void testExtraUntitledColumns(){
        Set<String> expectedCsv = new HashSet<String>();
        expectedCsv.addAll(Arrays.asList("a", "b", "c","d", "e"));
        String[] actualCsv = new String[]{"a", "b", "c", "d","e", "", "", "",""};
        Set<String> expectedExcel = new HashSet<String>();
        expectedExcel.addAll(Arrays.asList("x", "y", "z"));
        Set<String> actualExcel = new HashSet<String>();
        actualExcel.addAll(Arrays.asList("x", "y", "z"));

        ErrorChecker unit = new ErrorChecker(actualCsv, actualExcel, expectedCsv, expectedExcel, 2);
        assertEquals(2, unit.numberOfWarnings());
        assertEquals(0, unit.numberOfErrors());
        assertTrue(unit.generateReport(), unit.generateReport().trim().endsWith("warning: found 2 extra unnamed column(s) in csv file."));
    }

    public void testFewerUntitledColumns(){
        Set<String> expectedCsv = new HashSet<String>();
        expectedCsv.addAll(Arrays.asList("a", "b", "c","d", "e"));
        String[] actualCsv = new String[]{"a", "b", "c", "d","e"};
        Set<String> expectedExcel = new HashSet<String>();
        expectedExcel.addAll(Arrays.asList("x", "y", "z"));
        Set<String> actualExcel = new HashSet<String>();
        actualExcel.addAll(Arrays.asList("x", "y", "z"));

        ErrorChecker unit = new ErrorChecker(actualCsv, actualExcel, expectedCsv, expectedExcel, 2);
        assertEquals(2, unit.numberOfWarnings());
        assertEquals(0, unit.numberOfErrors());
        assertTrue(unit.generateReport().trim().endsWith("warning: expected 2 blank column(s) that were not in the csv file."));
    }

}
