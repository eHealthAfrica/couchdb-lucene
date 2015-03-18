package com.github.rnewson.couchdb.lucene.output;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Available output formats
 */
public enum OutputFormats {

    JSON("json", "attachment/json"),
    XML("xml", "attachment/xml"),
    CSV("csv", "attachment/csv");

    /**
     * Line separator string
     */
    private final static String EOL = System.getProperty("line.separator");

    /**
     * TAB character
     */
    private final static String TAB = "\t";

    /**
     * Double quote character
     */
    private final static String QUOTES = "\"";

    /**
     * Characters that should be escaped
     */
    private final static String[] QUOTABLE = new String[]{
            "'", "\r", "\n", TAB, QUOTES, EOL
    };

    private String formatType;
    private String contentType;

    private OutputFormats(String type, String contentType) {
        this.formatType = type;
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return this.formatType;
    }

    public String getContentType() {
        return this.contentType;
    }

    /**
     * Transforms the given array of documents in the corresponding format
     *
     * @param docs the array of documents
     * @return the string expression of the formatted documents
     * @throws JSONException
     */
    public String transformDocs(final JSONArray docs)
            throws JSONException {
        return transformDocs(docs, null, null, null);
    }

    /**
     * Transforms the given array of documents in the corresponding format
     * mapped by the list of keys
     *
     * @param docs      the array of documents
     * @param keys      the list of properties to be mapped
     * @param labels    the list of columns for the CSV format
     * @param delimiter the delimiter char for the CSV format
     * @return the string expression of the formatted documents
     * @throws JSONException
     */
    public String transformDocs(final JSONArray docs,
                                String[] keys,
                                String[] labels,
                                String delimiter)
            throws JSONException {

        if (docs == null || docs.length() == 0) {
            return ""; // nothing to export
        }

        switch (this) {
            case CSV:
                return transformCSV(docs, keys, labels, delimiter);
            case XML:
                return "<docs>"
                        + org.json.XML.toString(docs, "doc")
                        + "</docs>";

            case JSON:
            default:
                return docs.toString();
        }
    }

    /**
     * Transforms docs int csv format
     *
     * @param docs      the array of documents
     * @param keys      the list of properties to be mapped
     * @param labels    the list of columns
     * @param delimiter the delimiter char
     * @return the string expression of the formatted documents
     * @throws JSONException
     */
    private String transformCSV(final JSONArray docs,
                                String[] keys,
                                String[] labels,
                                String delimiter)
            throws JSONException {

        StringBuilder csv = new StringBuilder();
        final String sep = getSeparator(delimiter);

        // flatten documents
        JSONArray flattenDocs = JSONUtils.flat(docs, keys);

        // properties names
        if (keys == null || keys.length == 0) {
            // use first document properties
            JSONArray names = flattenDocs.getJSONObject(0).names();
            keys = new String[names.length()];
            for (int i = 0; i < names.length(); i++) {
                keys[i] = names.getString(i);
            }
        }

        // decide first row
        if (labels == null
                || labels.length == 0
                || labels.length != keys.length) {
            // use keys as first row
            labels = keys;
        }

        // headers
        for (String label : labels) {
            csv.append(escape(label, sep)).append(sep);
        }
        csv.append(EOL);

        for (int i = 0; i < flattenDocs.length(); i++) {
            JSONObject doc = flattenDocs.getJSONObject(i);
            for (String key : keys) {
                csv.append(escape(doc.optString(key, ""), sep))
                        .append(sep);
            }
            csv.append(EOL);
        }

        return csv.toString();
    }

    /**
     * Transforms delimiter into valid separator
     *
     * @param delimiter csv delimiter
     * @return one character string
     */
    private String getSeparator(String delimiter) {
        if (delimiter != null
                && delimiter.equalsIgnoreCase("tab")) {
            // TAB character
            return TAB;
        } else if (delimiter != null
                && delimiter.length() == 1
                && !delimiter.equals(QUOTES)) {
            // delimiter is only one character
            // and cannot be double quotes " (escape character)
            return delimiter;
        } else {
            // default value
            return ";";
        }
    }

    /**
     * Escapes string representation
     *
     * @param value string
     * @return quoted value
     */
    private String escape(String value, String separator) {
        if (value == null) {
            return "";
        }

        boolean quotable = value.contains(separator);

        // check problematic characters
        for (String q : QUOTABLE) {
            if (quotable) break;
            quotable = value.contains(q);
        }

        if (!quotable) {
            return value;
        }

        return QUOTES
                + value.replaceAll(QUOTES, QUOTES + QUOTES)
                + QUOTES;
    }

}
