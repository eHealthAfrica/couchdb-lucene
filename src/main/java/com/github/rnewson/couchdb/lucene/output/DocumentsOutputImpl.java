package com.github.rnewson.couchdb.lucene.output;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JSON response
 * <p>
 * Transforms the JSON documents in other JSON documents
 */
public class DocumentsOutputImpl implements Output {

    private static Logger logger =
            Logger.getLogger(DocumentsOutputImpl.class.getName());

    private OutputFormats format;
    private String callback;
    private boolean debug;
    private String[] keys;
    private String labels;
    private String delimiter;
    private JSONParser parser;

    public DocumentsOutputImpl(String callback,
                               boolean debug,
                               OutputFormats format,
                               String[] keys,
                               String labels,
                               String delimiter,
                               String parserClass) {
        this.callback = callback;
        this.debug = debug;
        this.format = format;
        this.keys = keys;
        this.labels = labels;
        this.delimiter = delimiter;
        this.parser = new JSONParser();

        if (parserClass != null && parserClass.length() > 0) {
            // load parser class
            try {
                Class clazz = Class.forName(parserClass);
                this.parser = (JSONParser) clazz.newInstance();
            } catch (Exception e) {
                // something went wrong, ignore parser
            }
        }

        logger.debug("Output: " + this.toString());
    }

    @Override
    public String getBody(HttpServletRequest req,
                          HttpServletResponse resp,
                          JSONArray docs)
            throws IOException, JSONException {

        JSONArray json = JSONUtils.getDocs(docs, this.keys, this.parser);

        if (this.callback != null) {
            return String.format("%s(%s)", this.callback, json);
        } else {
            resp.setContentType(this.format.getContentType());
            return this.debug ?
                    json.toString(2) :
                    this.format.transformDocs(json, this.keys,
                            this.labels, this.delimiter);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(" - Callback: ")
                .append(this.callback)
                .append(" - Debug: ")
                .append(this.debug)
                .append(" - Format: ")
                .append(this.format)
                .append(" - Delimiter: ")
                .append(this.delimiter)
                .append(" - Parser: ")
                .append(this.parser.getClass().getSimpleName())
                .toString();
    }
}
