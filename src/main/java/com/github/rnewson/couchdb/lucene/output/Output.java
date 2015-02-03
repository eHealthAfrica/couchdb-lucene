package com.github.rnewson.couchdb.lucene.output;


import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface Output {

    /**
     * Transform the searched documents into response body
     *
     * @param req,   request
     * @param resp,  response
     * @param docs,  searched JSON documents
     * @return the body response
     * @throws IOException
     * @throws JSONException
     */
    public String getBody(final HttpServletRequest req,
                          final HttpServletResponse resp,
                          final JSONArray docs)
            throws IOException, JSONException;

}
