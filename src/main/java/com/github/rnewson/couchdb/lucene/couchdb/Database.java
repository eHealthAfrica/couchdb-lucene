/*
 * Copyright Robert Newson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rnewson.couchdb.lucene.couchdb;

import com.github.rnewson.couchdb.lucene.Config;
import com.github.rnewson.couchdb.lucene.util.Utils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public final class Database {

    private final HttpClient httpClient;

    private final String url;
    private final String dbName;
    private int size;

    public Database(final HttpClient httpClient, final String aUrl, final String dbName) {
        final String url = aUrl + dbName;
        this.httpClient = httpClient;
        this.url = url.endsWith("/") ? url : url + "/";
        this.dbName = dbName;
        try {
            Config config = new Config();
            this.size = config.getConfiguration()
                    .getInt("lucene.couchdbDocumentSize", 500);
        } catch (ConfigurationException ignore) {
            this.size = 500;
        }
    }

    public boolean create() throws IOException {
        return HttpUtils.put(httpClient, url, null) == 201;
    }

    public boolean delete() throws IOException {
        return HttpUtils.delete(httpClient, url) == 200;
    }

    public List<DesignDocument> getAllDesignDocuments() throws IOException, JSONException {
        final String body = HttpUtils.get(httpClient, String
                .format("%s_all_docs?startkey=%s&endkey=%s&include_docs=true",
                        url, Utils.urlEncode("\"_design\""), Utils
                                .urlEncode("\"_design0\"")));
        final JSONObject json = new JSONObject(body);
        return toDesignDocuments(json);
    }

    public List<DesignDocument> getIndexableDesignDocuments(List blacklist) throws IOException, JSONException {
        final List<DesignDocument> result = new ArrayList<>();
        for (final DesignDocument ddoc : getAllDesignDocuments()) {
            if (!blacklist.contains(dbName + "/" + ddoc.getId())) {
                result.add(ddoc);
            }
        }
        return result;
    }

    public CouchDocument getDocument(final String id) throws IOException, JSONException {
        final String response = HttpUtils.get(httpClient, url
                + Utils.urlEncode(id));
        return new CouchDocument(new JSONObject(response));
    }

    public DesignDocument getDesignDocument(final String id) throws IOException, JSONException {
        final String response = HttpUtils.get(httpClient, url
                + Utils.urlEncode(id));
        return new DesignDocument(new JSONObject(response));
    }

    public List<CouchDocument> getDocuments(final String view,
                                            final String[] ids)
            throws IOException, JSONException {

        if (ids == null || ids.length == 0) {
            return Collections.emptyList();
        }

        final String viewName;
        if (view == null || view.isEmpty()) {
            viewName = "_all_docs?include_docs=true";
        } else {
            viewName = view;
        }

        final List<CouchDocument> docs = new ArrayList<>();

        // send packages of size n
        int limit = ids.length;
        for (int i = 0; i < limit; ) {
            final JSONObject req = new JSONObject();
            final JSONArray keys = new JSONArray();

            for (int p = 0; i < limit && p < size; i++, p++) {
                String id = ids[i];
                keys.put(id);
            }
            req.put("keys", keys);

            final String body = HttpUtils.post(httpClient,
                    url + viewName, req);
            docs.addAll(toDocuments(new JSONObject(body)));
        }

        return docs;
    }

    public DatabaseInfo getInfo() throws IOException, JSONException {
        return new DatabaseInfo(
                new JSONObject(HttpUtils.get(httpClient, url)));
    }

    public UpdateSequence getLastSequence()
            throws IOException, JSONException {
        final JSONObject result = new JSONObject(HttpUtils.get(httpClient, url
                + "_changes?limit=0&descending=true"));
        return UpdateSequence.parseUpdateSequence(result.getString("last_seq"));
    }

    public <T> T handleAttachment(final String doc,
                                  final String att,
                                  final ResponseHandler<T> handler)
            throws IOException {
        final HttpGet get = new HttpGet(url + "/" + Utils.urlEncode(doc) + "/"
                + Utils.urlEncode(att));
        return httpClient.execute(get, handler);
    }

    public HttpUriRequest getChangesRequest(final UpdateSequence since,
                                            final long timeout)
            throws IOException {
        final String uri;
        if (timeout > -1) {
            uri = url + "_changes?feed=continuous&timeout=" + timeout + "&include_docs=true";
        } else {
            uri = url + "_changes?feed=continuous&heartbeat=15000&include_docs=true";
        }
        return new HttpGet(since.appendSince(uri));
    }

    public boolean saveDocument(final String id, final String body)
            throws IOException {
        return HttpUtils.put(httpClient, url + Utils.urlEncode(id), body) == 201;
    }

    public UUID getUuid() throws IOException, JSONException {
        try {
            final CouchDocument local = getDocument("_local/lucene");
            return UUID.fromString(local.asJson().getString("uuid"));
        } catch (final HttpResponseException e) {
            switch (e.getStatusCode()) {
                case HttpStatus.SC_NOT_FOUND:
                    return null;
                default:
                    throw e;
            }
        }
    }

    public void createUuid() throws IOException {
        final UUID uuid = UUID.randomUUID();
        saveDocument("_local/lucene", String.format("{\"uuid\":\"%s\"}", uuid));
    }

    public UUID getOrCreateUuid() throws IOException, JSONException {
        final UUID result = getUuid();
        if (result != null) {
            return result;
        }
        createUuid();
        return getUuid();
    }

    private List<DesignDocument> toDesignDocuments(final JSONObject json) throws JSONException {
        final List<DesignDocument> result = new ArrayList<>();
        for (final JSONObject doc : rows(json)) {
            result.add(new DesignDocument(doc));
        }
        return result;
    }

    private List<CouchDocument> toDocuments(final JSONObject json) throws JSONException {
        final List<CouchDocument> result = new ArrayList<>();
        for (final JSONObject doc : rows(json)) {
            result.add(doc == null ? null : new CouchDocument(doc));
        }
        return result;
    }

    private List<JSONObject> rows(final JSONObject json) throws JSONException {
        final List<JSONObject> result = new ArrayList<>();
        final JSONArray rows = json.optJSONArray("rows");
        if (rows != null) {
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.optJSONObject(i);
                if (row == null) continue;

                if (row.optJSONObject("doc") != null) {
                    result.add(row.optJSONObject("doc"));
                } else if (row.optJSONObject("value") != null) {
                    result.add(row.optJSONObject("value"));
                }
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return 31 + url.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return (that != null)
                && ((this == that)
                || ((that instanceof Database)
                && url.equals(((Database) that).url)));
    }

    @Override
    public String toString() {
        return "Database [url=" + url + "]";
    }

}
