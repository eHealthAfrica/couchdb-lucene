package com.github.rnewson.couchdb.lucene.output;

/**
 * Available output formats
 */
public enum OutputFormats {

    JSON("json"),
    XML("xml"),
    CSV("csv");

    private String value;

    private OutputFormats(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
