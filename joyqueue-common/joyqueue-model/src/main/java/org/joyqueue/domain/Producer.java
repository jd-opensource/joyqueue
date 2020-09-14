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
package org.joyqueue.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 生产者
 *
 * @author lixiaobin6
 * 下午2:41 2018/8/13
 */
public class Producer extends Subscription {

    /**
     * 客户端类型
     */
    protected ClientType clientType;
    /**
     * 发送策略
     */
    protected ProducerPolicy producerPolicy;

    /**
     * 限流策略
     */
    protected ProducerLimitPolicy limitPolicy;

    public Producer() {
        setType(Type.PRODUCTION);
    }

    public String getId() {
        return new StringBuilder(topic.getFullName()).append(".").append(app).toString();
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public ProducerPolicy getProducerPolicy() {
        return producerPolicy;
    }

    public void setProducerPolicy(ProducerPolicy producerPolicy) {
        this.producerPolicy = producerPolicy;
    }

    public void setLimitPolicy(ProducerLimitPolicy limitPolicy) {
        this.limitPolicy = limitPolicy;
    }

    public ProducerLimitPolicy getLimitPolicy() {
        return limitPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Producer)) return false;
        if (!super.equals(o)) return false;

        Producer producer = (Producer) o;
        return clientType == producer.clientType &&
                Objects.equals(producerPolicy, producer.producerPolicy) &&
                Objects.equals(limitPolicy, producer.limitPolicy);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), clientType, producerPolicy, limitPolicy);
    }

    /**
     * 限流策略
     */
    public static class ProducerLimitPolicy implements Serializable {
        private Integer tps;
        private Integer traffic;

        public ProducerLimitPolicy() {

        }

        public ProducerLimitPolicy(Integer tps, Integer traffic) {
            this.tps = tps;
            this.traffic = traffic;
        }

        public void setTps(Integer tps) {
            this.tps = tps;
        }

        public Integer getTps() {
            return tps;
        }

        public void setTraffic(Integer traffic) {
            this.traffic = traffic;
        }

        public Integer getTraffic() {
            return traffic;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProducerLimitPolicy that = (ProducerLimitPolicy) o;
            return Objects.equals(tps, that.tps) &&
                    Objects.equals(traffic, that.traffic);
        }

        @Override
        public int hashCode() {

            return Objects.hash(tps, traffic);
        }
    }

    /**
     * 生产者策略
     */
    //TODO 客户端需要有个自己的
    public static class ProducerPolicy implements Serializable {

        public ProducerPolicy() {
            this.nearby = false;
            this.single = false;
            this.archive = false;
            this.timeOut = 1000 * 2;
        }

        // 就近发送
        private Boolean nearby;
        //单线程发送
        private Boolean single;
        // 是否需要归档,默认归档
        private Boolean archive;
        // 生产者权重 <group,weight>
        private Map<String, Short> weight;
        /**
         * 黑名单
         */
        private Set<String> blackList;
        private Integer timeOut;
        private Integer qosLevel;
        private String region;
        private Map<String, String> params;

        public ProducerPolicy(Boolean nearby, boolean single, Boolean archive, Map<String, Short> weight, Set<String> blackList, Integer timeOut) {
            this.nearby = nearby;
            this.single = single;
            this.archive = archive;
            this.weight = weight;
            this.blackList = blackList;
            this.timeOut = timeOut;
        }

        public Boolean getNearby() {
            return nearby;
        }

        public Map<String, Short> getWeight() {
            return weight;
        }


        public Set<String> getBlackList() {
            return blackList;
        }

        public Boolean isSingle() {
            return single;
        }

        public Boolean getArchive() {
            return archive;
        }

        public Integer getTimeOut() {
            return timeOut;
        }

        public void setNearby(Boolean nearby) {
            this.nearby = nearby;
        }

        public void setSingle(Boolean single) {
            this.single = single;
        }

        public void setArchive(Boolean archive) {
            this.archive = archive;
        }

        public void setWeight(Map<String, Short> weight) {
            this.weight = weight;
        }

        public void setBlackList(Set<String> blackList) {
            this.blackList = blackList;
        }

        public void setTimeOut(Integer timeOut) {
            this.timeOut = timeOut;
        }

        public void setQosLevel(Integer qosLevel) {
            this.qosLevel = qosLevel;
        }

        public Integer getQosLevel() {
            return qosLevel;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getRegion() {
            return region;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public String getParam(String key) {
            if (params == null) {
                return null;
            }
            return params.get(key);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProducerPolicy that = (ProducerPolicy) o;
            return Objects.equals(nearby, that.nearby) &&
                    Objects.equals(single, that.single) &&
                    Objects.equals(archive, that.archive) &&
                    Objects.equals(weight, that.weight) &&
                    Objects.equals(blackList, that.blackList) &&
                    Objects.equals(timeOut, that.timeOut) &&
                    Objects.equals(qosLevel, that.qosLevel) &&
                    Objects.equals(region, that.region) &&
                    Objects.equals(params, that.params);
        }

        @Override
        public int hashCode() {

            return Objects.hash(nearby, single, archive, weight, blackList, timeOut, qosLevel, params);
        }

        public static class Builder {
            // 就近发送
            private Boolean nearby = Boolean.FALSE;
            //单线程发送
            private boolean single = Boolean.FALSE;
            // 是否需要归档,默认归档
            private Boolean archive = Boolean.FALSE;
            // 生产者权重 <group,weight>
            private Map<String, Short> weight;
            /**
             * 黑名单
             */
            private Set<String> blackList;
            // 默认生产超时时间 2秒钟
            private Integer timeOut = 1000 * 2;

            private Integer qosLevel;
            private String region;

            public static Builder build() {
                return new Builder();
            }

            public Builder nearby(Boolean nearby) {
                this.nearby = nearby;
                return this;
            }

            public Builder archive(Boolean archive) {
                this.archive = archive;
                return this;
            }


            public Builder single(Boolean single) {
                this.single = single;
                return this;
            }

            public Builder blackList(String blackList) {
                if (null != blackList && !"".equals(blackList.trim())) {
                    this.blackList = new HashSet<String>(Arrays.asList(blackList.trim().split(",")));
                }
                return this;
            }

            public Builder weight(String weight) {
                if (null != weight && !"".equals(weight.trim())) {
                    this.weight = new HashMap<>();
                    String[] weights = weight.trim().split(",");
                    for (String s : weights) {
                        String[] m = s.split(":");
                        this.weight.put(m[0].trim(), Short.valueOf(m[1].trim()));
                    }
                }
                return this;
            }

            public Builder qosLevel(Integer qosLevel) {
                this.qosLevel = qosLevel;
                return this;
            }

            public Builder region(String region) {
                this.region = region;
                return this;
            }

            public Builder timeout(Integer timeOut) {
                this.timeOut = timeOut;
                return this;
            }

            public ProducerPolicy create() {
                ProducerPolicy producerPolicy = new ProducerPolicy(nearby, single, archive, weight,
                        blackList, timeOut);
                producerPolicy.setQosLevel(qosLevel);
                producerPolicy.setRegion(region);
                return producerPolicy;
            }
        }
    }
}
