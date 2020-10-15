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

import org.joyqueue.model.domain.nsr.BaseNsrModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Map;

public class ConsumerConfig extends BaseNsrModel {
    /**
     * related consumer id
     */
    private String consumerId;

    /**
     * 开启就近机房消费
     **/
    private Boolean nearBy = false;

    /**
     * 是否暂停消费
     **/
    private Boolean paused = false;

    /**
     * 是否需要归档,默认不归档
     **/
    private Boolean archive = false;

    /**
     * 是否启用重试服务，默认开启
     **/
    private Boolean retry = true;

    /**
     * 延迟时间,最大延迟1小时
     */
    @Max(3600000)
    private Integer delay = 0;

    /**
     * 偏移量管理类型
     */
    private OffsetMode offsetMode = OffsetMode.SERVER;

    /**
     * 应答超时时间，默认2min
     */
    @Min(0)
    private Integer ackTimeout = 120000;

    /**
     * 批量大小,默认10
     **/
    @Min(0)
    @Max(127)
    private Integer batchSize = 10;

    /**
     * 最大重试次数(无限制)
     **/
    @Min(0)
    private Integer maxRetrys = 0;

    /**
     * 最大重试间隔(默认5分钟)
     **/
    @Min(0)
    private Integer maxRetryDelay = 0;

    /**
     * 重试间隔
     */
    @Min(0)
    private Integer retryDelay = 0;

    private String region;

    /**
     * 指数增加间隔时间
     **/
    private Boolean useExponentialBackOff = true;

    /**
     * 指数系数
     */
    @Min(0)
    private Double backOffMultiplier = 0D;

    /**
     * 过期时间（默认3天）
     **/
    @Min(0)
    private Integer expireTime = 0;

    /**
     * 单队列并行度
     **/
    private Integer concurrent = 1;

    /**
     * 黑名单
     */
    private String blackList;

    private String filters;

    private Integer limitTps = 0;

    private Integer limitTraffic = 0;

    private Map<String, String> params;


    public Boolean isNearBy() {
        return nearBy;
    }

    public void setNearBy(Boolean nearBy) {
        this.nearBy = nearBy;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public Boolean isPaused() {
        return paused;
    }

    public void setPaused(Boolean paused) {
        this.paused = paused;
    }

    public Boolean isArchive() {
        return archive;
    }

    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    public Boolean isRetry() {
        return retry;
    }

    public void setRetry(Boolean retry) {
        this.retry = retry;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getAckTimeout() {
        return ackTimeout;
    }

    public void setAckTimeout(Integer ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getMaxRetrys() {
        return maxRetrys;
    }

    public void setMaxRetrys(Integer maxRetrys) {
        this.maxRetrys = maxRetrys;
    }

    public Integer getMaxRetryDelay() {
        return maxRetryDelay;
    }

    public void setMaxRetryDelay(Integer maxRetryDelay) {
        this.maxRetryDelay = maxRetryDelay;
    }

    public Integer getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Integer retryDelay) {
        this.retryDelay = retryDelay;
    }

    public Boolean isUseExponentialBackOff() {
        return useExponentialBackOff;
    }

    public void setUseExponentialBackOff(Boolean useExponentialBackOff) {
        this.useExponentialBackOff = useExponentialBackOff;
    }

    public Double getBackOffMultiplier() {
        return backOffMultiplier;
    }

    public void setBackOffMultiplier(Double backOffMultiplier) {
        this.backOffMultiplier = backOffMultiplier;
    }

    public Integer getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Integer expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(Integer concurrent) {
        this.concurrent = concurrent;
    }

    public String getBlackList() {
        return blackList;
    }

    public void setBlackList(String blackList) {
        this.blackList = blackList;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public void setLimitTps(Integer limitTps) {
        this.limitTps = limitTps;
    }

    public Integer getLimitTps() {
        return limitTps;
    }

    public void setLimitTraffic(Integer limitTraffic) {
        this.limitTraffic = limitTraffic;
    }

    public Integer getLimitTraffic() {
        return limitTraffic;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public OffsetMode getOffsetMode() {
        return offsetMode;
    }

    public void setOffsetMode(OffsetMode offsetMode) {
        this.offsetMode = offsetMode;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
