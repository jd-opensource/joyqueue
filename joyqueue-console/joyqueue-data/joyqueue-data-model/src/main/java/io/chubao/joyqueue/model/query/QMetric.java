/**
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
package io.chubao.joyqueue.model.query;

import io.chubao.joyqueue.model.QKeyword;

public class QMetric extends QKeyword {

    /**
     * metric code, abbr.
     */
    private String code;
    /**
     * metric value, inner used, unique
     */
    private String aliasCode;
    /**
     * metric name
     */
    private String name;
    /**
     * metric type, atomic or aggregator
     */
    private Integer type;
    /**
     * only for aggregator metric, which describe metric's origin metric code
     */
    private String source;
    /**
     * describe metric aggregate method or others
     */
    private String description;
    /**
     * metric provider
     */
    private String provider;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliasCode() {
        return aliasCode;
    }

    public void setAiasCode(String aliasCode) {
        this.aliasCode = aliasCode;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }


}
