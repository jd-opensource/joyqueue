/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.model.domain.grafana;

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
