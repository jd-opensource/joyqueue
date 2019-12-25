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

import java.util.Map;
import java.util.Set;

public class ConsumerPolicy {

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

    public ConsumerPolicy() {

    }

    public ConsumerPolicy(Boolean nearby, Boolean paused, Boolean archive, Boolean retry, Boolean seq, Integer ackTimeout,
                          Short batchSize, Integer concurrent, Integer delay, Set<String> blackList, Integer errTimes,
                          Integer maxPartitionNum, Integer readRetryProbability, Map<String, String> filters) {
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

    public Boolean getNearby() {
        return nearby;
    }

    public void setNearby(Boolean nearby) {
        this.nearby = nearby;
    }

    public Boolean getPaused() {
        return paused;
    }

    public void setPaused(Boolean paused) {
        this.paused = paused;
    }

    public Boolean getArchive() {
        return archive;
    }

    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    public Boolean getRetry() {
        return retry;
    }

    public void setRetry(Boolean retry) {
        this.retry = retry;
    }

    public Boolean getSeq() {
        return seq;
    }

    public void setSeq(Boolean seq) {
        this.seq = seq;
    }

    public Integer getAckTimeout() {
        return ackTimeout;
    }

    public void setAckTimeout(Integer ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public Short getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Short batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(Integer concurrent) {
        this.concurrent = concurrent;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Set<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(Set<String> blackList) {
        this.blackList = blackList;
    }

    public Integer getErrTimes() {
        return errTimes;
    }

    public void setErrTimes(Integer errTimes) {
        this.errTimes = errTimes;
    }

    public Integer getMaxPartitionNum() {
        return maxPartitionNum;
    }

    public void setMaxPartitionNum(Integer maxPartitionNum) {
        this.maxPartitionNum = maxPartitionNum;
    }

    public Integer getReadRetryProbability() {
        return readRetryProbability;
    }

    public void setReadRetryProbability(Integer readRetryProbability) {
        this.readRetryProbability = readRetryProbability;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "ConsumerPolicy{" +
                "nearby=" + nearby +
                ", paused=" + paused +
                ", archive=" + archive +
                ", retry=" + retry +
                ", seq=" + seq +
                ", ackTimeout=" + ackTimeout +
                ", batchSize=" + batchSize +
                ", concurrent=" + concurrent +
                ", delay=" + delay +
                ", blackList=" + blackList +
                ", errTimes=" + errTimes +
                ", maxPartitionNum=" + maxPartitionNum +
                ", readRetryProbability=" + readRetryProbability +
                ", filters=" + filters +
                '}';
    }
}
