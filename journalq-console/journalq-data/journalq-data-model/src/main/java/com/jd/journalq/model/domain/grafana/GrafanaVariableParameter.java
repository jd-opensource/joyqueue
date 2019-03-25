package com.jd.journalq.model.domain.grafana;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class GrafanaVariableParameter {
    private String name;
    @JacksonXmlProperty(localName = "target_index")
    private int targetIndex;
    @JacksonXmlProperty(localName = "arg_index")
    private int argIndex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    public int getArgIndex() {
        return argIndex;
    }

    public void setArgIndex(int argIndex) {
        this.argIndex = argIndex;
    }
}
