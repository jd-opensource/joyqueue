package com.jd.journalq.model.domain;

public enum UniqueFields {

    Application("code"),
    Broker("ip", "port"),
    AuditRole("code"),
    AuditFlow("type"),
    Topic("code");

    private String[] fields;

    UniqueFields(String... fields) {
        this.fields = fields;
    }

    public String[] fields() {
        return this.fields;
    }

}
