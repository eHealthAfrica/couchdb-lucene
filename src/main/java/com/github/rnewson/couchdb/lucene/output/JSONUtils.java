package com.github.rnewson.couchdb.lucene.output;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Common methods
 */
public class JSONUtils {

    /**
     * Extracts from the `rows` property in the array the property `doc`
     * <p/>
     * docs:
     * [ { "rows" : [
     * { "doc" : { "a" : 1, "b" : 1 } },
     * { "doc" : { "a" : 2, "b" : 2 } }
     * ] } ]
     * keys:
     * [ "a" ]
     * <p/>
     * Returns:
     * [ { "a" : 1 }, { "a" : 2 } ]
     *
     * @param docs, the searched documents
     * @param keys, the properties to use, if empty all
     * @return the array of `doc` property included in the rows array
     */
    public static JSONArray getDocs(JSONArray docs, String[] keys)
            throws JSONException {

        JSONArray result = new JSONArray();

        for (int i = 0; i < docs.length(); i++) {
            JSONArray rows = docs.getJSONObject(i).getJSONArray("rows");
            if (rows != null) {
                for (int j = 0; j < rows.length(); j++) {
                    JSONObject doc = rows.getJSONObject(j).getJSONObject("doc");
                    if (doc != null) {
                        result.put(map(doc, keys));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Maps document to the given list of properties
     * <p/>
     * doc:
     * { "a" : { "b" : 1, "c" : 2}, "d" : 4 }
     * names:
     * [ "a.b", "d" ]
     * <p/>
     * Returns:
     * { "a" { "b" : 1 }, "d" : 4 }
     *
     * @param doc  the document to be mapped
     * @param keys the list of names for the new document
     * @return the mapped document or the original document if names is empty
     * @throws JSONException
     */
    public static JSONObject map(JSONObject doc, String[] keys)
            throws JSONException {
        if (keys == null || keys.length == 0) {
            // nothing to map
            return doc;
        }

        JSONObject target = new JSONObject();
        mapping(target, doc, keys);
        return target;
    }

    /**
     * Maps documents with the given list of properties
     *
     * @param docs the array of documents
     * @param keys the list of names for the new documents
     * @return the array with mapped documents
     * @throws JSONException
     */
    public static JSONArray map(JSONArray docs, String[] keys)
            throws JSONException {

        if (keys == null || keys.length == 0) {
            // nothing to map
            return docs;
        }

        // create new array with mapped documents
        JSONArray array = new JSONArray();
        for (int i = 0; i < docs.length(); i++) {
            array.put(map(docs.getJSONObject(i), keys));
        }

        return array;
    }

    /**
     * Flat the JSON document (no nested properties)
     * <p/>
     * doc:
     * { "a" : { "b" : 1 }, "c" : [ 1, 2, 3 ], "d" : 4 }
     * keys:
     * null
     * <p/>
     * Returns:
     * { "a.b" : 1, "c.0" : 1, "c.1" : 2, "c.2" : 3, "d" : 4 }
     *
     * @param doc   the document to be flattened
     * @param keys, the properties to use, if empty all
     * @return the flattened document
     * @throws JSONException
     */
    public static JSONObject flat(JSONObject doc, String[] keys)
            throws JSONException {
        JSONObject target = new JSONObject();
        String[] allowedKeys;
        if (keys == null) {
            allowedKeys = new String[0];
        } else {
            allowedKeys = Arrays.copyOf(keys, keys.length);
            Arrays.sort(allowedKeys);
        }
        flatting(target, doc, null, allowedKeys);
        return target;
    }

    /**
     * Flattens the array of JSON documents.
     *
     * @param array the array with the documents
     * @param keys, the properties to use, if empty all
     * @return an array with the flattened documents
     * @throws JSONException if the arrays does not contain ALL JSON objects
     */
    public static JSONArray flat(JSONArray array, String[] keys)
            throws JSONException {
        JSONArray target = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            if (!(array.get(i) instanceof JSONObject)) {
                throw new JSONException("Error flattening JSONArray.");
            }
            target.put(flat(array.getJSONObject(i), keys));
        }
        return target;
    }

    /**
     * Get the value within a document
     * <p/>
     * doc:
     * { "a" : { "b" : 1 } }
     * key:
     * "a.b"
     * <p/>
     * Returns:
     * 1
     * <p/>
     * doc: { "c" : [ 1, 2, 3 ] }
     * key: "c.1"
     * Returns:
     * 2
     *
     * @param doc the document in which search
     * @param key the property to search
     * @return the property value
     * @throws JSONException
     */
    public static Object getNestedProperty(JSONObject doc,
                                           String key)
            throws JSONException {

        return getNested(doc, key);
    }

    /**
     * Set the value within a document
     * <p/>
     * doc: { "a" : { "b" : 1 } }
     * key: "a.b"
     * value: 2
     * <p/>
     * Result:
     * doc: { "a" : { "b" : 2 } }
     * <p/>
     * doc: { "c" : [ 1, 2, 3 ] }
     * key: "c.1"
     * value: 9
     * <p/>
     * Result:
     * doc: { "c" : [ 1, 9, 3 ] }
     *
     * @param doc   the document in which search
     * @param key   the property to search
     * @param value the value to assign
     * @throws JSONException
     */
    public static void setNestedProperty(JSONObject doc,
                                         String key,
                                         Object value)
            throws JSONException {

        setNested(doc, key, value);
    }


    private static void mapping(JSONObject target,
                                JSONObject doc,
                                String[] keys)
            throws JSONException {
        for (String key : keys) {
            setNested(target, key, getNested(doc, key));
        }
    }

    private static void flatting(JSONObject target,
                                 Object obj,
                                 String prefix,
                                 String[] allowedKeys)
            throws JSONException {

        if (obj instanceof JSONObject) {
            JSONObject doc = (JSONObject) obj;

            Iterator<?> keys = doc.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();

                flatting(target,
                        doc.get(key),
                        getKey(prefix, key),
                        allowedKeys);
            }
        } else if (obj instanceof JSONArray) {
            // array
            JSONArray array = (JSONArray) obj;
            for (int i = 0; i < array.length(); i++) {
                flatting(target,
                        array.get(i),
                        getKey(prefix, Integer.toString(i, 10)),
                        allowedKeys);
            }

        } else {
            // already flat
            if (allowedKeys == null || allowedKeys.length == 0
                    || Arrays.binarySearch(allowedKeys, prefix) != -1) {
                target.putOpt(prefix, obj);
            }
        }
    }

    private static String getKey(String prefix, String key) {
        if (prefix == null) {
            return key;
        } else {
            return prefix + "." + key;
        }
    }

    private static Object getNested(Object obj,
                                    String key)
            throws JSONException {

        String[] parts = splitKey(key);

        if (obj instanceof JSONObject) {
            JSONObject doc = (JSONObject) obj;

            if (StringUtils.isNumeric(parts[0])) {
                // this is not an array, something is wrong
                throw new JSONException("getNested: Unexpected int key");
            }

            if (parts[1].equals("")) {
                return doc.opt(key);
            } else {

                // there is no property
                if (!doc.has(parts[0])) {
                    return null;
                }

                return getNested(doc.get(parts[0]), parts[1]);
            }

        } else if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;

            if (!StringUtils.isNumeric(parts[0])) {
                // this is not an integer, something is wrong
                throw new JSONException("getNested: Unexpected non-int key");
            }

            int index = Integer.parseInt(parts[0]);
            if (index < 0 || index > array.length()) {
                return null; // out of range
            }

            if (parts[1].equals("")) {
                return array.get(index);
            } else {
                return getNested(array.get(index), parts[1]);
            }

        } else {
            throw new JSONException("getNested: Unexpected object");
        }
    }


    private static void setNested(Object obj,
                                  String key,
                                  Object value)
            throws JSONException {

        String[] parts = splitKey(key);

        if (obj instanceof JSONObject) {
            JSONObject doc = (JSONObject) obj;

            if (StringUtils.isNumeric(parts[0])) {
                // this is a number, something is wrong
                throw new JSONException("setNested: Unexpected numeric key");
            }

            if (parts[1].equals("")) {
                doc.putOpt(key, value);
            } else {

                // there is no property
                if (!doc.has(parts[0])) {
                    String[] next = splitKey(parts[1]);
                    if (StringUtils.isNumeric(next[0])) {
                        // next part is an array
                        doc.put(parts[0], new JSONArray());
                    } else {
                        doc.put(parts[0], new JSONObject());
                    }
                }

                setNested(doc.get(parts[0]), parts[1], value);
            }

        } else if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;

            if (!StringUtils.isNumeric(parts[0])) {
                // this is not a number, something is wrong
                throw new JSONException("setNested: Unexpected NaN key");
            }

            int index = Integer.parseInt(parts[0]);
            if (index > array.length()) {
                // fill array
                array.put(index, JSONObject.NULL);
            }

            if (parts[1].equals("")) {
                array.put(index, value);
            } else {
                setNested(array.get(index), parts[1], value);
            }

        } else {
            throw new JSONException("setNested: Unexpected object");
        }
    }

    private static String[] splitKey(String key) {

        String[] split = {"", ""};
        int point = key.indexOf('.');

        if (point == -1) {
            split[0] = key;
        } else {
            split[0] = key.substring(0, point);
            split[1] = key.substring(point + 1);
        }

        return split;
    }
}
