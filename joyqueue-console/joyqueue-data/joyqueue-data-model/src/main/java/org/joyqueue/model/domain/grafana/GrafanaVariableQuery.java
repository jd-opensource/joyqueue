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
 * Grafana variable query model
 * author: chenyanying3
 * email: chenyanying3@jd.com
 * date: 2019/02/29
 */
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
