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


import org.joyqueue.toolkit.retry.RetryPolicy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 消费订阅
 *
 * @author lixiaobin6
 * 下午3:23 2018/7/31
 */
public class Consumer extends Subscription {

    /**
     * 客户端类型
     */
    protected ClientType clientType;

    protected TopicType topicType = TopicType.TOPIC;

    /**
     * 重试策略
     */
    protected RetryPolicy retryPolicy;

    /**
     * 消费策略
     */
    protected ConsumerPolicy consumerPolicy;

    /**
     * 限流策略
     */
    protected ConsumerLimitPolicy limitPolicy;

    public Consumer() {
        setType(Type.CONSUMPTION);
    }

    public String getId() {
        return new StringBuilder(30).append(topic.getFullName()).append(".").append(app).toString();
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public TopicType getTopicType() {
        return topicType;
    }

    public void setTopicType(TopicType topicType) {
        this.topicType = topicType;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public ConsumerPolicy getConsumerPolicy() {
        return consumerPolicy;
    }

    public void setConsumerPolicy(ConsumerPolicy consumerPolicy) {
        this.consumerPolicy = consumerPolicy;
    }

    public void setLimitPolicy(ConsumerLimitPolicy limitPolicy) {
        this.limitPolicy = limitPolicy;
    }

    public ConsumerLimitPolicy getLimitPolicy() {
        return limitPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Consumer)) return false;
        if (!super.equals(o)) return false;

        Consumer consumer = (Consumer) o;
        return clientType == consumer.clientType &&
                topicType == consumer.topicType &&
                Objects.equals(retryPolicy, consumer.retryPolicy) &&
                Objects.equals(consumerPolicy, consumer.consumerPolicy) &&
                Objects.equals(limitPolicy, consumer.limitPolicy);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), clientType, topicType, retryPolicy, consumerPolicy, limitPolicy);
    }

    /**
     * 限流策略
     */
    public static class ConsumerLimitPolicy implements Serializable {
        private Integer tps;
        private Integer traffic;

        public ConsumerLimitPolicy() {

        }

        public ConsumerLimitPolicy(Integer tps, Integer traffic) {
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
            ConsumerLimitPolicy that = (ConsumerLimitPolicy) o;
            return Objects.equals(tps, that.tps) &&
                    Objects.equals(traffic, that.traffic);
        }

        @Override
        public int hashCode() {

            return Objects.hash(tps, traffic);
        }
    }

    /**
     * 消费策略
     */
    public static class ConsumerPolicy implements Serializable {
        // 就近发送
        private Boolean nearby;
        // 是否暂停消费
        private Boolean paused;
        // 是否需要归档,默认归档
        private Boolean archive;
        // 是否需要重试，默认重试
        private Boolean retry;
        // 顺序消费
        @Deprecated
        private Boolean seq;
        // 应答超时时间
        private Integer ackTimeout = 12000;
        // 批量大小
        private Short batchSize;
        //并行消费预取数量
        private Integer concurrent;
        //延迟消费
        private Integer delay = 0;
        //黑名单
        private Set<String> blackList;
        //出错次数
        private Integer errTimes;
        //一个连接最多占用的分区数
        private Integer maxPartitionNum;
        //读取到重试队列比例(0~100)
        private Integer readRetryProbability;
        //过滤规则
        private Map</*类型*/String, /*内容*/String> filters;
        private String region;
        private Map<String, String> params;

        public ConsumerPolicy() {
        }

        public ConsumerPolicy(Boolean nearby, Boolean paused, Boolean archive, Boolean retry, Boolean seq,
                              Integer ackTimeout, Short batchSize, Integer concurrent,
                              Integer delay, Set<String> blackList, Integer errTimes, Integer maxPartitionNum, Integer readRetryProbability,Map<String,String> filters) {
            this.nearby = nearby;
            this.paused = paused;
            this.archive = archive;
            this.retry = retry;
            this.seq = seq;
            this.ackTimeout = ackTimeout;
            this.batchSize = batchSize;
            this.concurrent = concurrent;
            this.delay = delay;
            this.blackList = blackList;
            this.errTimes = errTimes;
            this.maxPartitionNum = maxPartitionNum;
            this.readRetryProbability = readRetryProbability;
            this.filters = filters;
        }

        public void setAckTimeout(Integer ackTimeout) {
            this.ackTimeout = ackTimeout;
        }

        public void setBatchSize(Short batchSize) {
            this.batchSize = batchSize;
        }

        public Boolean getNearby() {
            return nearby;
        }


        public Boolean getPaused() {
            return paused;
        }


        public Boolean getArchive() {
            return archive;
        }


        public Boolean getRetry() {
            return retry;
        }


        public Boolean getSeq() {
            return seq;
        }

        public Integer getAckTimeout() {
            return ackTimeout;
        }


        public Short getBatchSize() {
            return batchSize;
        }

        public Boolean isConcurrent() {
            return concurrent > 1;
        }

        public Integer getConcurrent() {
            return concurrent;
        }

        public Integer getConcurrentPrefetchSize() {
            return concurrent * batchSize;
        }

        public Integer getDelay() {
            return delay;
        }

        public Set<String> getBlackList() {
            return blackList;
        }

        public Integer getErrTimes() {
            return errTimes;
        }

        public Integer getMaxPartitionNum() {
            return maxPartitionNum;
        }

        public void setNearby(Boolean nearby) {
            this.nearby = nearby;
        }

        public void setPaused(Boolean paused) {
            this.paused = paused;
        }

        public void setArchive(Boolean archive) {
            this.archive = archive;
        }

        public void setRetry(Boolean retry) {
            this.retry = retry;
        }

        public void setSeq(Boolean seq) {
            this.seq = seq;
        }

        public void setConcurrent(Integer concurrent) {
            this.concurrent = concurrent;
        }

        public void setMaxPartitionNum(Integer maxPartitionNum) {
            this.maxPartitionNum = maxPartitionNum;
        }

        public void setReadRetryProbability(Integer readRetryProbability) {
            this.readRetryProbability = readRetryProbability;
        }

        public void setDelay(Integer delay) {
            this.delay = delay;
        }

        public void setErrTimes(Integer errTimes) {
            this.errTimes = errTimes;
        }

        public void setBlackList(Set<String> blackList) {
            this.blackList = blackList;
        }

        public Integer getReadRetryProbability() {
            return readRetryProbability;
        }

        public void setFilters(Map<String, String> filters) {
            this.filters = filters;
        }

        public Map<String, String> getFilters() {
            return filters;
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
            ConsumerPolicy that = (ConsumerPolicy) o;
            return Objects.equals(nearby, that.nearby) &&
                    Objects.equals(paused, that.paused) &&
                    Objects.equals(archive, that.archive) &&
                    Objects.equals(retry, that.retry) &&
                    Objects.equals(seq, that.seq) &&
                    Objects.equals(ackTimeout, that.ackTimeout) &&
                    Objects.equals(batchSize, that.batchSize) &&
                    Objects.equals(concurrent, that.concurrent) &&
                    Objects.equals(delay, that.delay) &&
                    Objects.equals(blackList, that.blackList) &&
                    Objects.equals(errTimes, that.errTimes) &&
                    Objects.equals(maxPartitionNum, that.maxPartitionNum) &&
                    Objects.equals(readRetryProbability, that.readRetryProbability) &&
                    Objects.equals(filters, that.filters) &&
                    Objects.equals(region, that.region) &&
                    Objects.equals(params, that.params);
        }

        @Override
        public int hashCode() {

            return Objects.hash(nearby, paused, archive, retry, seq, ackTimeout, batchSize, concurrent, delay, blackList, errTimes, maxPartitionNum, readRetryProbability, filters, params);
        }

        public static class Builder {
            // 就近发送
            private Boolean nearby = false;
            // 是否暂停消费
            private Boolean paused = false;
            // 是否需要归档,默认归档
            private Boolean archive = false;
            // 是否需要重试，默认重试
            private Boolean retry = true;
            // 顺序消费
            private Boolean seq = false;
            // 应答超时时间
            private Integer ackTimeout = 1000 * 60 * 5;
            // 批量大小
            private Short batchSize = 10;
            //并行消费
            private Integer concurrent = 1;
            //延迟消费
            private Integer delay = 0;
            //出错次数
            private Integer errTimes = 3;
            //一个连接最多占用的分区数
            private Integer maxPartitionNum = Integer.MAX_VALUE;
            //黑名单
            private Set<String> blackList;
            //读取到重试队列比例(0~100)
            private Integer retryReadProbability = 20;
            //消息过滤规则
            private Map</*类型*/String, /*规则*/String> filters = new HashMap<>();

            private String region;

            private Map<String, String> params;


            public static Builder build() {
                return new Builder();
            }

            public Builder region(String region) {
                this.region = region;
                return this;
            }

            public Builder nearby(Boolean nearby) {
                this.nearby = nearby;
                return this;
            }

            public Builder paused(Boolean paused) {
                this.paused = paused;
                return this;
            }

            public Builder archive(Boolean archive) {
                this.archive = archive;
                return this;
            }

            public Builder retry(Boolean retry) {
                this.retry = retry;
                return this;
            }

            public Builder seq(Boolean seq) {
                this.seq = seq;
                return this;
            }

            public Builder ackTimeout(Integer ackTimeout) {
                this.ackTimeout = ackTimeout;
                return this;
            }

            public Builder batchSize(Short batchSize) {
                this.batchSize = batchSize;
                return this;
            }

            public Builder concurrent(Integer concurrent) {
                this.concurrent = concurrent;
                return this;
            }

            public Builder delay(Integer delay) {
                this.delay = delay;
                return this;
            }

            public Builder errTimes(Integer errTimes) {
                this.errTimes = errTimes;
                return this;
            }

            public Builder maxPartitionNum(Integer maxPartitionNum) {
                this.maxPartitionNum = maxPartitionNum;
                return this;
            }

            public Builder blackList(String blackList) {
                if (null != blackList && !"".equals(blackList.trim())) {
                    this.blackList = new HashSet<String>(Arrays.asList(blackList.trim().split(",")));
                }
                return this;
            }


            public Builder retryReadProbability(Integer retryReadProbability) {
                this.retryReadProbability = retryReadProbability;
                return this;
            }
            public Builder filters(String filtersStr) {
                if (filtersStr != null && !"".equals(filtersStr.trim())) {
                    String[] filterArray=filtersStr.split(";");
                    Map<String,String> map = new HashMap<>();
                    for (String filter:filterArray) {
                        map.put(filter.split(":")[0],filter.split(":")[1]);
                    }
                    this.filters=map;
                }
                return this;
            }

            public Builder filters(Map<String, String> filters) {
                this.filters = filters;
                return this;
            }

            public Builder params(Map<String, String> params) {
                this.params = params;
                return this;
            }

            public ConsumerPolicy create() {
                ConsumerPolicy consumerPolicy = new ConsumerPolicy(nearby, paused, archive, retry, seq, ackTimeout, batchSize, concurrent, delay,
                        blackList, errTimes, maxPartitionNum, retryReadProbability, filters);
                consumerPolicy.setRegion(region);
                if (this.params!=null && this.params.size() > 0) {
                    consumerPolicy.setParams(this.params);
                }
                return consumerPolicy;
            }
        }

    }
}
