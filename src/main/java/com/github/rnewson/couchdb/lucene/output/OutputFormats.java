package com.github.rnewson.couchdb.lucene.output;

import org.json.CDL;
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
     * mapped by the list of keys
     *
     * @param docs the array of documents
     * @param keys the list of properties to be mapped
     * @return the string expression of the formatted documents
     * @throws JSONException
     */
    public String transformDocs(final JSONArray docs,
                                final String[] keys)
            throws JSONException {

        switch (this) {
            case XML:
                return "<docs>" + org.json.XML.toString(docs, "doc") + "</docs>";
            case CSV:
                return CDL.toString(JSONUtils.flat(docs, keys));
            case JSON:
            default:
                return docs.toString();
        }
    }
}
