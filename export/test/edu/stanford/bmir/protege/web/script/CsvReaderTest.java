package edu.stanford.bmir.protege.web.script;

import junit.framework.TestCase;

import java.io.FileNotFoundException;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class CsvReaderTest extends TestCase {

    public void testReadsFirstRow() throws FileNotFoundException {
        CsvReader unit = new CsvReader("test/simple-test-export.csv", 3);
        unit.nextRow();
        assertEquals("a", unit.nextEntry());
        assertEquals("one", unit.getCurrentColumnName());
        assertEquals("b", unit.nextEntry());
        assertEquals("two", unit.getCurrentColumnName());
        assertEquals("c", unit.nextEntry());
        assertEquals("three", unit.getCurrentColumnName());
        assertEquals("d", unit.nextEntry());
        assertEquals("four", unit.getCurrentColumnName());
        assertEquals("e", unit.nextEntry());
        assertEquals("five", unit.getCurrentColumnName());
    }
    
    public void testReadsLastRow() throws FileNotFoundException {
        CsvReader unit = new CsvReader("test/simple-test-export.csv", 3);
        unit.nextRow();
        unit.nextRow();
        unit.nextRow();
        assertEquals("aaa", unit.nextEntry());
        assertEquals("one", unit.getCurrentColumnName());
        assertEquals("bbb", unit.nextEntry());
        assertEquals("two", unit.getCurrentColumnName());
        assertEquals("ccc", unit.nextEntry());
        assertEquals("three", unit.getCurrentColumnName());
        assertEquals("ddd", unit.nextEntry());
        assertEquals("four", unit.getCurrentColumnName());
        assertEquals("eee", unit.nextEntry());
        assertEquals("five", unit.getCurrentColumnName());
    }
}
