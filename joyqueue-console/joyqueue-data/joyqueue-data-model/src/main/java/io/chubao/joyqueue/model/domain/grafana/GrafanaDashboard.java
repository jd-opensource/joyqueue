package io.chubao.joyqueue.model.domain.grafana;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * Grafana dashboard
 * author: chenyanying3
 * email: chenyanying3@jd.com
 * date: 2019/02/29
 */
public class GrafanaDashboard {
    private String uid;
    private String title;
    private String url;
    @JacksonXmlElementWrapper(localName = "metric_variables")
    @JacksonXmlProperty(localName = "metric_variable")
    private List<GrafanaMetricVariable> metricVariables;
    private String variables;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<GrafanaMetricVariable> getMetricVariables() {
        return metricVariables;
    }

    public void setMetricVariables(List<GrafanaMetricVariable> metricVariables) {
        this.metricVariables = metricVariables;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }
}
