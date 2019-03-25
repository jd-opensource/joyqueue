package com.jd.journalq.model.domain.grafana;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "Configuration")
public class GrafanaConfig {
    public static final String DEFAULT_GRAFANA_CONFIG_DELIMITER = ",";
    private String url;
    @JacksonXmlElementWrapper(localName = "dashboards")
    @JacksonXmlProperty(localName = "dashboard")
    private List<GrafanaDashboard> dashboards;
    @JacksonXmlElementWrapper(localName = "variables")
    @JacksonXmlProperty(localName = "variable")
    private List<GrafanaVariable> variables;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<GrafanaDashboard> getDashboards() {
        return dashboards;
    }

    public void setDashboards(List<GrafanaDashboard> dashboards) {
        this.dashboards = dashboards;
    }

    public List<GrafanaVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<GrafanaVariable> variables) {
        this.variables = variables;
    }
}
