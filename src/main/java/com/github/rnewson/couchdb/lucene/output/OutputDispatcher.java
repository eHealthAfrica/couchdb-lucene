package com.github.rnewson.couchdb.lucene.output;

import javax.servlet.http.HttpServletRequest;

/**
 * Selects the Output class.
 */
public class OutputDispatcher {

    private static final String OUTPUT_PARAM = "output_format";
    private static final String KEYS_PARAM = "export_keys";
    private static final String CSV_PARAM = "csv_labels";
    private static final String DELIMITER_PARAM = "csv_delimiter";
    private static final String PARSER_PARAM = "output_parser";

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
                    String[] keys = null;
                    if (keysParam != null && keysParam.trim().length() > 0) {
                        keys = keysParam.split(",");
                    }
                    String labels = req.getParameter(CSV_PARAM);
                    String delimiter = req.getParameter(DELIMITER_PARAM);
                    if (delimiter == null || delimiter.isEmpty()) {
                        delimiter = ","; // default value
                    }
                    String parser = req.getParameter(PARSER_PARAM);

                    // returns Formatted Documents Output
                    return new DocumentsOutputImpl(callback, debug,
                            format, keys, labels, delimiter, parser);
                }
            }
        }

        // default output response
        return new DefaultOutputImpl(callback, debug);
    }

}
