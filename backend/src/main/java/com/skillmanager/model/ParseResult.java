package com.skillmanager.model;

import java.util.Map;

public class ParseResult {
    private Map<String, String> headers;
    private String contentWithoutHeader;

    public ParseResult(Map<String, String> headers, String contentWithoutHeader) {
        this.headers = headers;
        this.contentWithoutHeader = contentWithoutHeader;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getContentWithoutHeader() {
        return contentWithoutHeader;
    }
}
