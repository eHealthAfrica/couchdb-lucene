package com.github.rnewson.couchdb.lucene.output;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Available output formats
 */
public enum OutputFormats {

    JSON("json", "application/json"),
    XML("xml", "application/xml"),
    CSV("csv", "text/plain");

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
                                final String[] keys,
                                final String labels,
                                final String delimiter)
            throws JSONException {

        if (docs == null || docs.length() == 0) {
            return ""; // nothing to export
        }

        switch (this) {
            case CSV:
                StringBuilder csv = new StringBuilder();
                String join = ",";
                if (delimiter != null && delimiter.length() == 1) {
                    join = delimiter;
                } else if (delimiter != null && delimiter.equals("tab")) {
                    join = "\t";
                }

                // flatten documents
                JSONArray flattenDocs = JSONUtils.flat(docs, keys);
                // properties names
                JSONArray names;
                if (keys != null && keys.length > 0) {
                    // use provided list
                    names = new JSONArray(keys);
                } else {
                    // use first document properties
                    names = flattenDocs.getJSONObject(0).names();
                }

                // decide first row
                if (labels != null && labels.trim().length() > 0) {
                    // use labels as first row
                    csv.append(labels);
                } else {
                    // use properties names as first row
                    csv.append(names.join(join));
                }
                csv.append("\n");

                for (int i = 0; i < flattenDocs.length(); i++) {
                    csv.append(flattenDocs
                            .getJSONObject(i)
                            .toJSONArray(names)
                            .join(join));
                    csv.append("\n");
                }

                return csv.toString();
            case XML:
                return "<docs>" + org.json.XML.toString(docs, "doc") + "</docs>";

            case JSON:
            default:
                return docs.toString();
        }
    }
}