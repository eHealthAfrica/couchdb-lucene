package com.github.rnewson.couchdb.lucene.output;

import com.github.rnewson.couchdb.lucene.util.ServletUtils;
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

    @Override
    public String getBody(HttpServletRequest req,
                          HttpServletResponse resp,
                          JSONArray docs,
                          final String eTag,
                          final boolean debug)
            throws IOException, JSONException {

        final Object json = docs.length() > 1 ? docs : docs.getJSONObject(0);
        final String callback = req.getParameter("callback");
        final String body;

        resp.setHeader("ETag", eTag);
        resp.setHeader("Cache-Control", "must-revalidate");
        ServletUtils.setResponseContentTypeAndEncoding(req, resp);

        if (callback != null) {
            body = String.format("%s(%s)", callback, json);
        } else {
            if (json instanceof JSONObject) {
                final JSONObject obj = (JSONObject) json;
                body = debug ? obj.toString(2) : obj.toString();
            } else {
                final JSONArray arr = (JSONArray) json;
                body = debug ? arr.toString(2) : arr.toString();
            }
        }

        return body;
    }
}
