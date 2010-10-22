package edu.stanford.bmir.protege.web.script;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * The class that performs our error checking, against externally supplied data.
 *
 * This is done because it allows us to gather all faults together into a single place, whereas scanning simply ends up
 * picking the errors one by one. Not very useful when you have lots of unexpected columns or unmapped data!
 *
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class ErrorChecker {
    private Set<String> missingExpectedCsvColumnTitles;
    private Set<String> missingActualCsvColumnTitles;
    private Set<String> actualCsvColumnTitlesSet;
    private Set<String> missingExpectedExcelColumnTitles;
    private Set<String> missingActualExcelColumnTitles;
    private Set<String> actualExcelColumnTitles;
    private int extraUnnamedCsvColumns;


    public ErrorChecker(String[] actualCsvColumnTitles, Set<String> actualExcelColumnTitles, Set<String> expectedCsvColumnTitles, Set<String> expectedExcelColumnTitles, int expectedUnnamedCsvColumns) {
        this.actualExcelColumnTitles = new TreeSet<String>(actualExcelColumnTitles);
        actualCsvColumnTitlesSet = new TreeSet<String>(Arrays.asList(actualCsvColumnTitles));
        actualCsvColumnTitlesSet.remove("");
        checkErrors(actualCsvColumnTitles, expectedCsvColumnTitles, expectedExcelColumnTitles,actualExcelColumnTitles,expectedUnnamedCsvColumns);
    }

    private void checkErrors(String[] actualCsvColumnTitles,
                             Set<String> expectedCsvColumnTitles,
                             Set<String> expectedExcelColumnTitles,
                             Set<String> actualExcelColumnTitles,
                             int expectedUnnamedCsvColumns
    ) {

        missingExpectedCsvColumnTitles = getDisjunctionOfA(expectedCsvColumnTitles, actualCsvColumnTitlesSet);
        missingActualCsvColumnTitles = getDisjunctionOfA(actualCsvColumnTitlesSet, expectedCsvColumnTitles);
        missingExpectedExcelColumnTitles = getDisjunctionOfA(expectedExcelColumnTitles, actualExcelColumnTitles);
        missingActualExcelColumnTitles = getDisjunctionOfA(actualExcelColumnTitles, expectedExcelColumnTitles);
        int actualUnnamedCsvColumns = 0;
        for (String actualCsvColumnTitle : actualCsvColumnTitles) {
            if (actualCsvColumnTitle == null || actualCsvColumnTitle.trim().equals("")){
                actualUnnamedCsvColumns ++;
            }
        }
        extraUnnamedCsvColumns = actualUnnamedCsvColumns - expectedUnnamedCsvColumns;
    }

    private Set<String> getDisjunctionOfA(Set<String> setA, Set<String> setB) {
        Set<String> disjunction = new HashSet<String>(setA);
        disjunction.removeAll(setB);
        return disjunction;
    }

    public String generateReport(){
        return MessageFormat.format("Error report:\n" +
                (missingExpectedCsvColumnTitles.isEmpty()? "" :"\t error: expected column(s) {0} in csv file, but could not find them. \n" +
                        "\t\t actual column names were {5}" )+
                (missingExpectedExcelColumnTitles.isEmpty()? "" :"\t error: expected column(s) {1} in excel file, but could not find them.\n"+
                        "\t\t actual column names were {6}" ) +
                (missingActualCsvColumnTitles.isEmpty()? "" :"\t warning: found extra unexpected column(s) {2} in csv file.\n") +
                (missingActualExcelColumnTitles.isEmpty()? "" :"\t warning: found extra unexpected column(s) {3} in excel file.\n") +
                (extraUnnamedCsvColumns > 0 ? "\t warning: found {4} extra unnamed column(s) in csv file.\n":"")+
                (extraUnnamedCsvColumns < 0 ? "\t warning: expected {4} blank column(s) that were not in the csv file.\n":""),
                missingExpectedCsvColumnTitles,
                missingExpectedExcelColumnTitles,
                missingActualCsvColumnTitles,
                missingActualExcelColumnTitles,
                Math.abs(extraUnnamedCsvColumns),
                actualCsvColumnTitlesSet,
                actualExcelColumnTitles
                );
    }

    /**
     * Errors are cases where it would be unsafe to proceed, as the metadata is fundamentally mismatched.
     * @return
     */
    public int numberOfErrors(){
        return missingExpectedCsvColumnTitles.size() + missingExpectedExcelColumnTitles.size();
    }

    /**
     * Warnings are cases where we can proceed safely. There are interesting conditions to report, but while the data may
     * not be as complete as we might like, there will be nothing fundamentally missing from the output. 
     * @return
     */
    public int numberOfWarnings(){
        return missingActualCsvColumnTitles.size() + missingActualExcelColumnTitles.size() + Math.abs(extraUnnamedCsvColumns);
    }
}
