package com.github.rnewson.couchdb.lucene.output;

import javax.servlet.http.HttpServletRequest;

/**
 * Selects the Output class.
 */
public class OutputDispatcher {

    private static final String OUTPUT_PARAM = "output_format";
    private static final String KEYS_PARAM = "export_keys";

    public static Output getOutput(final HttpServletRequest req) {

        // check the output parameter
        String output = req.getParameter(OUTPUT_PARAM);
        boolean includeDocs = Boolean.parseBoolean(
                req.getParameter("include_docs"));
        final String callback = req.getParameter("callback");
        final boolean debug = Boolean.parseBoolean(req.getParameter("debug"));

        if (includeDocs && output != null) {
            // returns only formatted documents
            for (OutputFormats format : OutputFormats.values()) {
                if (output.equals(format.toString())) {
                    // keys
                    String keysParam = req.getParameter(KEYS_PARAM);
                    if (keysParam == null) keysParam = "";
                    String[] keys = keysParam.split(",");

                    // returns Formatted Documents Output
                    return new DocumentsOutputImpl(callback, debug, format, keys);
                }
            }
        }

        // default output response
        return new DefaultOutputImpl(callback, debug);
    }

}
