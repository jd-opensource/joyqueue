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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * Grafana config
 * author: chenyanying3
 * email: chenyanying3@jd.com
 * date: 2019/02/29
 */
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
