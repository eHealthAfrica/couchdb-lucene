package com.github.rnewson.couchdb.lucene.output;

import javax.servlet.http.HttpServletRequest;

/**
 * Selects the Output class.
 */
public class OutputDispatcher {

    private static final String OUTPUT_PARAM = "o";
    private static final String KEYS_PARAM = "k";

    public static Output getOutput(final HttpServletRequest req) {

        // check the output parameter
        String output = req.getParameter(OUTPUT_PARAM);
        boolean includeDocs = Boolean.parseBoolean(
                req.getParameter("include_docs"));
        final String callback = req.getParameter("callback");
        final boolean debug = Boolean.parseBoolean(req.getParameter("debug"));

        // keys
        String keysParam = req.getParameter(KEYS_PARAM);
        if (keysParam == null) keysParam = "";
        String[] keys = keysParam.split(",");

        if (includeDocs && output != null) {
            // returns only formatted documents
            for (OutputFormats format : OutputFormats.values()) {
                if (output.equals(format.toString())) {
                    // returns Formatted Documents Output
                    return new DocumentsOutputImpl(callback, debug, format, keys);
                }
            }
        }

        // default output response
        return new DefaultOutputImpl(callback, debug);
    }

}
