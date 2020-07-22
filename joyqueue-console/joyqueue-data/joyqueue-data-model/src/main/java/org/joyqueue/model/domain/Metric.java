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
package org.joyqueue.model.domain;

/**
 * Metric
 * Created by chenyanying3 on 19-2-25.
 */
public class Metric extends BaseModel implements Identifier, Cloneable {

    /**
     * metric code, abbr.
     */
    private String code;
    /**
     * metric aliasCode, inner used, unique
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

    /**
     * metric user permission
     */
    private boolean userPermission;

    /**
     * metric type: producer, consumer, broker
     */
    private String category;

    /**
     * collect interval, unit seconds
     */
    private int collectInterval;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getAliasCode() {
        return aliasCode;
    }

    public void setAliasCode(String aliasCode) {
        this.aliasCode = aliasCode;
    }

    @EnumType(MetricType.class)
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

    public boolean isUserPermission() {
        return userPermission;
    }

    public void setUserPermission(boolean userPermission) {
        this.userPermission = userPermission;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCollectInterval() {
        return collectInterval;
    }

    public void setCollectInterval(int collectInterval) {
        this.collectInterval = collectInterval;
    }

    public enum MetricType implements EnumItem{
        OTHERS(0, "others"),
        ATOMIC(1, "atomic"),
        AGGREGATOR(2, "aggregator");

        private int value;
        private String description;

        MetricType(int value, String description) {
            this.value = value;
            this.description = description;
        }

        @Override
        public int value() {
            return this.value;
        }

        @Override
        public String description() {
            return this.description;
        }

        public static MetricType resolve(int value) {
            for (MetricType type : MetricType.values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            return OTHERS;
        }

        public static MetricType resolve(String descOrName) {
            for (MetricType type : MetricType.values()) {
                if (type.description().equals(descOrName) || type.name().equals(descOrName)) {
                    return type;
                }
            }
            return OTHERS;
        }
    }

}
