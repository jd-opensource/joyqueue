package io.chubao.joyqueue.model.domain.grafana;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * Grafana metric
 * author: chenyanying3
 * email: chenyanying3@jd.com
 * date: 2019/02/29
 */
public class GrafanaMetric {
    private String name;
    @JacksonXmlElementWrapper(localName = "granularities")
    @JacksonXmlProperty(localName = "granularity")
    private List<GrafanaMetricGranularity> granularities;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GrafanaMetricGranularity> getGranularities() {
        return granularities;
    }

    public void setGranularities(List<GrafanaMetricGranularity> granularities) {
        this.granularities = granularities;
    }
}
