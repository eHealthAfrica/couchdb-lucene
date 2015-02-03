package com.github.rnewson.couchdb.lucene.output;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Default response
 */
public class DefaultOutputImpl implements Output {

    private String callback;
    private boolean debug;

    public DefaultOutputImpl(String callback, boolean debug) {
        this.callback = callback;
        this.debug = debug;
    }

    @Override
    public String getBody(HttpServletRequest req,
                          HttpServletResponse resp,
                          JSONArray docs)
            throws IOException, JSONException {

        final Object json = docs.length() > 1 ? docs : docs.getJSONObject(0);
        final String body;

        if (this.callback != null) {
            body = String.format("%s(%s)", this.callback, json);
        } else {
            if (json instanceof JSONObject) {
                final JSONObject obj = (JSONObject) json;
                body = this.debug ? obj.toString(2) : obj.toString();
            } else {
                final JSONArray arr = (JSONArray) json;
                body = this.debug ? arr.toString(2) : arr.toString();
            }
        }

        return body;
    }
}
