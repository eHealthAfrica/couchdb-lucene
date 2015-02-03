package com.github.rnewson.couchdb.lucene.output;

import org.json.JSONArray;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test OutputFormat enum
 */
public class OutputFormatsTest {

    @Test
    public void testJSONFormat() throws Exception {
        JSONArray docs = getFixture();
        OutputFormats format = OutputFormats.JSON;

        String result = format.transformDocs(docs, null);

        // nothing change
        assertThat(result, is(docs.toString()));
    }

    @Test
    public void testJSONFormatWithNames() throws Exception {
        JSONArray docs = getFixture();
        OutputFormats format = OutputFormats.JSON;

        String result = format.transformDocs(docs, null);

        // nothing change
        assertThat(result, is(docs.toString()));
    }

    @Test
    public void testXMLFormat() throws Exception {
        JSONArray docs = getFixture();
        OutputFormats format = OutputFormats.XML;
        String result = format.transformDocs(docs, null);

        String expected = "<docs>" +
                "<doc><baz>1</baz><bar><foo>1</foo></bar></doc>" +
                "<doc><baz>2</baz><bar><foo>2</foo></bar></doc>" +
                "<doc><baz>3</baz><bar><foo>3</foo></bar></doc>" +
                "<doc><baz>4</baz><bar><foo>4</foo></bar></doc>" +
                "</docs>";

        // nothing change
        assertThat(result, is(expected));
    }


    @Test
    public void testCSVFormat() throws Exception {
        JSONArray docs = getFixture();
        OutputFormats format = OutputFormats.CSV;
        String result = format.transformDocs(docs, null);

        String expected = "bar.foo,baz\n" +
                "1,1\n" +
                "2,2\n" +
                "3,3\n" +
                "4,4\n";

        // nothing change
        assertThat(result, is(expected));
    }

    private JSONArray getFixture() throws Exception {
        String input = "[" +
                "{ \"bar\" : { \"foo\" : 1 }, \"baz\" : 1 }," +
                "{ \"bar\" : { \"foo\" : 2 }, \"baz\" : 2 }," +
                "{ \"bar\" : { \"foo\" : 3 }, \"baz\" : 3 }," +
                "{ \"bar\" : { \"foo\" : 4 }, \"baz\" : 4 }" +
                "]";
        return new JSONArray(input);
    }
}
