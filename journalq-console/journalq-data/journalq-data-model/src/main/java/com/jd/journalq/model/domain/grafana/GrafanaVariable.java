package com.jd.journalq.model.domain.grafana;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class GrafanaVariable {
    public static final String DEFAULT_GRAFANA_TARGET_DELIMITER = ":";
    private String name;
    private String target;
    @JacksonXmlProperty(localName = "query")
    private GrafanaVariableQuery query;
    @JacksonXmlProperty(localName = "result")
    private GrafanaVariableResult result;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public GrafanaVariableQuery getQuery() {
        return query;
    }

    public void setQuery(GrafanaVariableQuery query) {
        this.query = query;
    }

    public GrafanaVariableResult getResult() {
        return result;
    }

    public void setResult(GrafanaVariableResult result) {
        this.result = result;
    }
}
