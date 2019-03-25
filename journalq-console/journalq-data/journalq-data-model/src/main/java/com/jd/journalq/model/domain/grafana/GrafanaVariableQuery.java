package com.jd.journalq.model.domain.grafana;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class GrafanaVariableQuery {
    private String model;
    @JacksonXmlElementWrapper(localName = "parameters")
    @JacksonXmlProperty(localName = "parameter")
    private List<GrafanaVariableParameter> parameters;
    private String bean;
    private String method;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<GrafanaVariableParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<GrafanaVariableParameter> parameters) {
        this.parameters = parameters;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
