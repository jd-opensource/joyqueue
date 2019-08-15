package io.chubao.joyqueue.model.domain.grafana;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * Grafana metric variable
 * author: chenyanying3
 * email: chenyanying3@jd.com
 * date: 2019/02/29
 */
public class GrafanaMetricVariable {

    private String name;
    private String target;
    @JacksonXmlElementWrapper(localName = "metrics")
    @JacksonXmlProperty(localName = "metric")
    private List<GrafanaMetric> metrics;

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

    public List<GrafanaMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<GrafanaMetric> metrics) {
        this.metrics = metrics;
    }
}
