package com.github.rnewson.couchdb.lucene.output;

import org.json.JSONArray;
import org.json.XML;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test OutputFormat enum
 */
public class OutputFormatsTest {

    static {
        System.setProperty("line.separator", "|");
    }

    @Test
    public void testJSONFormat() throws Exception {
        JSONArray docs = getFixture();
        OutputFormats format = OutputFormats.JSON;

        String result = format.transformDocs(docs);

        // nothing change
        assertThat(result, is(docs.toString()));
    }

    @Test
    public void testXMLFormat() throws Exception {
        JSONArray docs = getFixture();
        OutputFormats format = OutputFormats.XML;
        String result = format.transformDocs(docs);

        String output = "<docs>" +
                "<doc><baz>11</baz><bar><foo>1</foo></bar></doc>" +
                "<doc><baz>22</baz><bar><foo>2</foo></bar></doc>" +
                "<doc><baz>33</baz><bar><foo>3</foo></bar></doc>" +
                "<doc><baz>44</baz><bar><foo>4</foo></bar></doc>" +
                "</docs>";
        String expected = XML.toString(XML.toJSONObject(output));

        assertThat(result, is(expected));
    }

    @Test
    public void testCSVFormat() throws Exception {
        JSONArray docs = getFixture();
        OutputFormats format = OutputFormats.CSV;
        String[] keys = {"bar.foo", "baz"};
        String result = format.transformDocs(docs, keys, null, ";");

        String output = "bar.foo;baz;|" +
                "1;11;|" +
                "2;22;|" +
                "3;33;|" +
                "4;44;|";

        assertThat(result, is(output));
    }

    @Test
    public void testCSVFormatWithLabels() throws Exception {
        JSONArray docs = getFixture();
        OutputFormats format = OutputFormats.CSV;
        String[] keys = {"bar.foo", "baz"};
        String[] labels = {"foo", "baz"};
        String result = format.transformDocs(docs, keys, labels, "tab");

        String output = "foo\tbaz\t|" +
                "1\t11\t|" +
                "2\t22\t|" +
                "3\t33\t|" +
                "4\t44\t|";

        assertThat(result, is(output));
    }

    private JSONArray getFixture() throws Exception {
        String input = "[" +
                "{ \"bar\" : { \"foo\" : 1 }, \"baz\" : 11 }," +
                "{ \"bar\" : { \"foo\" : 2 }, \"baz\" : 22 }," +
                "{ \"bar\" : { \"foo\" : 3 }, \"baz\" : 33 }," +
                "{ \"bar\" : { \"foo\" : 4 }, \"baz\" : 44 }" +
                "]";
        return new JSONArray(input);
    }
}
