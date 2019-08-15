package io.chubao.joyqueue.nsr.model;

import io.chubao.joyqueue.model.Query;

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
