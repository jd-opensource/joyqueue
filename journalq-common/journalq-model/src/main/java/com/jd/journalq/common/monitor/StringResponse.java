package com.jd.journalq.common.monitor;

import java.util.HashMap;
import java.util.Map;

/**
 * StringResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class StringResponse {

    private String body;

    private Map<String, String> headers = new HashMap<>();

    public StringResponse() {

    }

    public StringResponse(String body) {
        this.body = body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }
}