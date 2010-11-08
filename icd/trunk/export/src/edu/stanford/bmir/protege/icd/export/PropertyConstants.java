package edu.stanford.bmir.protege.icd.export;

/**
 * @author Jack Elliott <jacke@stanford.edu>
 */
public class PropertyConstants {
    public static final String PYTHON_HOME_PROPERTY = "python.home";

    public static final String JXL_NOWARNINGS_PROPERTY = "jxl.nowarnings";
    public static final String JXL_NOWARNINGS_DEFAULT = Boolean.TRUE.toString();

    public static final String ICD_EXPORT_SCRIPT_FILE_NAME_PROPERTY = "icd.export.script.file.name";
    public static final String ICD_EXPORT_SCRIPT_FILE_NAME_DEFAULT = "export_script.py";

    public static final String ICD_EXPORT_EXCEL_FILE_NAME_PROPERTY = "icd.export.excel.file.name";
    public static final String ICD_EXPORT_EXCEL_FILE_NAME_LOCATION = "template.xls";

    public static final String CSV_FILE_ENCODING_PROPERTY = "csv.file.encoding";
    public static final String CSV_FILE_ENCODING_PROPERTY_DEFAULT = "ISO-8859-1";

    public static final String PYTHON_INTERNAL_TABLES_OPTION_PROPERTY = "python.options.internalTablesImpl";
    public static final String PYTHON_INTERNAL_TABLES_OPTION_DEFAULT = "weak";
}
