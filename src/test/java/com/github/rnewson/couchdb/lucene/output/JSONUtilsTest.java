package com.github.rnewson.couchdb.lucene.output;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Test JSONUtils methods
 */
public class JSONUtilsTest {

    @Test
    public void testGetDocs() throws Exception {
        String input = "[{\"rows\":[" +
                "{ \"doc\" : { \"a\" : 1, \"b\" : 1 } }," +
                "{ \"doc\" : { \"a\" : 2, \"b\" : 2 } }," +
                "{ \"doc\" : { \"a\" : 3, \"b\" : 3 } }," +
                "{ \"doc\" : { \"a\" : 4, \"b\" : 4 } }" +
                "]}]";
        String output = "[" +
                "{ \"a\" : 1 }," +
                "{ \"a\" : 2 }," +
                "{ \"a\" : 3 }," +
                "{ \"a\" : 4 }" +
                "]";
        String[] keys = {"a"};

        JSONArray docs = new JSONArray(input);
        String expected = new JSONArray(output).toString();

        String result = JSONUtils.getDocs(docs, keys).toString();

        assertThat(result, is(expected));
    }

    @Test
    public void testFlatDocument() throws JSONException {
        String input = "{ \"a\" : { \"b\" : 1 }," +
                " \"c\" : [ 1, 2.5, 3 ], " +
                "\"d\" : \"string\" }";
        String output = "{ \"a.b\" : 1, " +
                "\"c.0\" : 1, " +
                "\"c.1\" : 2.5, " +
                "\"c.2\" : 3, " +
                "\"d\" : \"string\" }";

        JSONObject doc = new JSONObject(input);
        String expected = new JSONObject(output).toString();
        String result = JSONUtils.flat(doc, null).toString();

        assertThat(result, is(expected));
    }

    @Test
    public void testFlatDocuments() throws JSONException {
        String input = "[{ \"a\" : { \"b\" : 1 }," +
                " \"c\" : [ 1, 2.5, 3 ], " +
                "\"d\" : \"string\" }]";
        String output = "[{ \"a.b\" : 1, " +
                "\"c.0\" : 1, " +
                "\"c.1\" : 2.5, " +
                "\"c.2\" : 3, " +
                "\"d\" : \"string\" }]";

        JSONArray docs = new JSONArray(input);
        String expected = new JSONArray(output).toString();
        String result = JSONUtils.flat(docs, null).toString();

        assertThat(result, is(expected));
    }

    @Test
    public void testFlatDocumentsException() {
        JSONArray input = new JSONArray();
        input.put("a simple string");
        input.put("other simple string");

        Exception expected = null;
        try {
            JSONUtils.flat(input, null);
        } catch (JSONException e) {
            expected = e;
        }

        assertNotNull(expected);
        assertThat(expected.getMessage(), is("Error flattening JSONArray."));
    }

    @Test
    public void testMapDocument() throws JSONException {
        String input = "{ \"a\" : { \"b\" : 1, \"g\": 1 }," +
                " \"c\" : [ 1, 2, 3 ], " +
                "\"d\" : \"string\" }";
        String output = "{ \"a\" : { \"b\" : 1 }, " +
                " \"c\" : [ null, 2 ], " +
                "\"d\" : \"string\" }";
        String[] names = {"a.b", "c.1", "d"};

        JSONObject doc = new JSONObject(input);
        String expected = new JSONObject(output).toString();
        String result = JSONUtils.map(doc, names).toString();

        assertThat(result, is(expected));
    }

    @Test
    public void testGetNestedProperty() throws JSONException {
        String input = "{ \"a\" : { \"b\" : 1}," +
                " \"c\" : [ 1, 2, 3 ], " +
                "\"d\" : \"string\" }";
        JSONObject doc = new JSONObject(input);

        assertEquals(JSONUtils.getNestedProperty(doc, "a.b"), 1);
        assertEquals(JSONUtils.getNestedProperty(doc, "c.2"), 3);
        assertNull(JSONUtils.getNestedProperty(doc, "a.g"));
        assertNull(JSONUtils.getNestedProperty(doc, "c.4"));
    }

    @Test
    public void testSetNestedProperty() throws JSONException {
        String input = "{ \"a\" : { \"b\" : 1}," +
                " \"c\" :  [ 1, 2, 3 ], " +
                "\"d\" : \"string\" }";
        JSONObject doc = new JSONObject(input);

        Integer value = 2;
        JSONUtils.setNestedProperty(doc, "e.f", value);
        JSONUtils.setNestedProperty(doc, "c.0", value);
        JSONUtils.setNestedProperty(doc, "c.6", value);

        assertEquals(JSONUtils.getNestedProperty(doc, "e.f"), value);
        assertEquals(JSONUtils.getNestedProperty(doc, "c.0"), value);
        assertEquals(JSONUtils.getNestedProperty(doc, "c.6"), value);
        assertEquals(JSONUtils.getNestedProperty(doc, "c.3"), JSONObject.NULL);
        assertEquals(JSONUtils.getNestedProperty(doc, "c.4"), JSONObject.NULL);
        assertEquals(JSONUtils.getNestedProperty(doc, "c.5"), JSONObject.NULL);
    }
}
