package com.jd.journalq.nsr.model;

import com.jd.journalq.model.Query;

public class NamespaceQuery implements Query {
    /**
     * code
     */
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
