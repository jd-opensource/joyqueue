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
package com.jd.joyqueue.model.domain;

import com.jd.joyqueue.model.domain.nsr.BaseNsrModel;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class ConsumerConfig extends BaseNsrModel {
    /**
     * related consumer id
     */
    private String consumerId;

    /**
     * 开启就近机房消费
     **/
    private boolean nearBy;

    /**
     * 是否暂停消费
     **/
    private boolean paused;

    /**
     * 是否需要归档,默认不归档
     **/
    private boolean archive;

    /**
     * 是否启用重试服务，默认开启
     **/
    private boolean retry = true;

    /**
     * 延迟时间,最大延迟1小时
     */
    @Max(3600000)
    private int delay = 0;

    /**
     * 偏移量管理类型
     */
    private OffsetMode offsetMode = OffsetMode.SERVER;

    /**
     * 应答超时时间，默认2min
     */
    @Min(0)
    private int ackTimeout = 120000;

    /**
     * 批量大小,默认10
     **/
    @Min(0)
    @Max(127)
    private int batchSize = 10;

    /**
     * 最大重试次数(无限制)
     **/
    @Min(0)
    private int maxRetrys;

    /**
     * 最大重试间隔(默认5分钟)
     **/
    @Min(0)
    private int maxRetryDelay;

    /**
     * 重试间隔
     */
    @Min(0)
    private int retryDelay;

    /**
     * 指数增加间隔时间
     **/
    private boolean useExponentialBackOff = true;

    /**
     * 指数系数
     */
    @Min(0)
    private double backOffMultiplier;

    /**
     * 过期时间（默认3天）
     **/
    @Min(0)
    private int expireTime;

    /**
     * 单队列并行度
     **/
    private int concurrent = 1;

    /**
     * 黑名单
     */
    private String blackList;

    private String filters;

    private int limitTps;

    private int limitTraffic;


    public boolean isNearBy() {
        return nearBy;
    }

    public void setNearBy(boolean nearBy) {
        this.nearBy = nearBy;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public void setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxRetrys() {
        return maxRetrys;
    }

    public void setMaxRetrys(int maxRetrys) {
        this.maxRetrys = maxRetrys;
    }

    public int getMaxRetryDelay() {
        return maxRetryDelay;
    }

    public void setMaxRetryDelay(int maxRetryDelay) {
        this.maxRetryDelay = maxRetryDelay;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }

    public boolean isUseExponentialBackOff() {
        return useExponentialBackOff;
    }

    public void setUseExponentialBackOff(boolean useExponentialBackOff) {
        this.useExponentialBackOff = useExponentialBackOff;
    }

    public double getBackOffMultiplier() {
        return backOffMultiplier;
    }

    public void setBackOffMultiplier(double backOffMultiplier) {
        this.backOffMultiplier = backOffMultiplier;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public int getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(int concurrent) {
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

    public void setLimitTps(int limitTps) {
        this.limitTps = limitTps;
    }

    public int getLimitTps() {
        return limitTps;
    }

    public void setLimitTraffic(int limitTraffic) {
        this.limitTraffic = limitTraffic;
    }

    public int getLimitTraffic() {
        return limitTraffic;
    }

    public OffsetMode getOffsetMode() {
        return offsetMode;
    }

    public void setOffsetMode(OffsetMode offsetMode) {
        this.offsetMode = offsetMode;
    }

}
