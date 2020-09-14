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
package org.joyqueue.toolkit.retry;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 重试策略
 */
public class RetryPolicy implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int EXPIRE_TIME = 1000 * 3600 * 24 * 3;
    public static final int RETRY_DELAY = 1000;
    public static final int MAX_RETRY_DELAY = 0;
    public static final double BACKOFF_MULTIPLIER = 2.0;
    public static final int MAX_RETRYS = 0;
    // 最大指数，初始化计算一次
    private volatile AtomicReference<Integer> maxExponential = new AtomicReference();
    // 最大重试次数
    private Integer maxRetrys;
    // 最大重试间隔
    private Integer maxRetryDelay;
    // 重试间隔
    private Integer retryDelay;
    // 指数增加间隔时间
    private Boolean useExponentialBackOff;
    // 指数系数，必须>=1
    private Double backOffMultiplier;
    // 过期时间（默认3天）
    private Integer expireTime;
    private Integer randomBound;

    public RetryPolicy() {
    }

    public RetryPolicy(Integer retryDelay, Integer maxRetrys) {
        setRetryDelay(retryDelay);
        setMaxRetryDelay(retryDelay);
        setMaxRetrys(maxRetrys);
    }

    public RetryPolicy(Integer retryDelay, Integer maxRetryDelay, Integer maxRetrys, Boolean useExponentialBackOff,
            Double backOffMultiplier, Integer expireTime) {
        setRetryDelay(retryDelay);
        setMaxRetryDelay(maxRetryDelay);
        setMaxRetrys(maxRetrys);
        setUseExponentialBackOff(useExponentialBackOff);
        setBackOffMultiplier(backOffMultiplier);
        setExpireTime(expireTime);
    }

    /**
     * 获取重试时间
     *
     * @param now        当前时间
     * @param retryTimes 当前重试次数，从1开始计数
     * @param startTime  初始化起始时间
     * @return  &lt;=0 过期, &gt;0 下次重试时间
     *
     */
    public long getTime(final long now, final int retryTimes, final long startTime) {
        long time = 0;
        int retrys = retryTimes < 1 ? 1 : retryTimes;
        int maxRetrys = this.maxRetrys == null ? MAX_RETRYS : this.maxRetrys;
        int retryDelay = this.retryDelay == null ? RETRY_DELAY : this.retryDelay;
        int maxRetryDelay = this.maxRetryDelay == null ? MAX_RETRY_DELAY : this.maxRetryDelay;
        boolean useExponentialBackOff = this.useExponentialBackOff == null ? true : this.useExponentialBackOff;
        double backOffMultiplier =
                (this.backOffMultiplier == null || this.backOffMultiplier < 1) ? BACKOFF_MULTIPLIER : this
                        .backOffMultiplier;
        int expireTime = this.expireTime == null ? EXPIRE_TIME : this.expireTime;
        // 判断是否超过最大重试次数
        if (maxRetrys > 0 && retrys > maxRetrys) {
            time = 0;
        } else if (retryDelay <= 0) {
            // 没有时间间隔
            time = now;
        } else {
            long delay = 0;

            // 判断是否使用指数函数
            if (useExponentialBackOff) {
                // 指数
                int exponential = retrys - 1;
                // 底数为1
                if (backOffMultiplier == 1) {
                    delay = retryDelay;
                } else if (maxRetryDelay > 0) {
                    // 获取最大的指数
                    Integer maxExp = maxExponential.get();
                    // 还没用计算过
                    if (maxExp == null) {
                        maxExp = (int) (Math.log(maxRetryDelay) / Math.log(backOffMultiplier));
                        if (!maxExponential.compareAndSet(null, maxExp)) {
                            maxExp = maxExponential.get();
                        }
                    }
                    // 超过了最大指数
                    if (exponential >= maxExp) {
                        delay = maxRetryDelay;
                    } else {
                        delay = Math.round(retryDelay * Math.pow(backOffMultiplier, exponential));
                    }
                } else {
                    delay = Math.round(retryDelay * Math.pow(backOffMultiplier, exponential));
                }
            } else {
                delay = retryDelay;
            }
            if (delay <= 0) {
                time = now;
            } else if (maxRetryDelay > 0 && delay >= maxRetryDelay) {
                time = now + maxRetryDelay;
            } else {
                time = now + delay;
            }
        }
        // 有过期时间设置
        if (expireTime > 0 && time > 0 && time >= (startTime + expireTime)) {
            time = 0;
        }
        return time;
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
        this.maxExponential.set(null);
    }

    public Integer getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(Integer retryDelay) {
        this.retryDelay = retryDelay;
        this.maxExponential.set(null);
    }

    public Boolean getUseExponentialBackOff() {
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
        this.maxExponential.set(null);
    }

    public Integer getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Integer expireTime) {
        this.expireTime = expireTime;
    }

    public void setRandomBound(Integer randomBound) {
        this.randomBound = randomBound;
    }

    public Integer getRandomBound() {
        return randomBound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetryPolicy that = (RetryPolicy) o;
        return Objects.equals(maxRetrys, that.maxRetrys) &&
                Objects.equals(maxRetryDelay, that.maxRetryDelay) &&
                Objects.equals(retryDelay, that.retryDelay) &&
                Objects.equals(useExponentialBackOff, that.useExponentialBackOff) &&
                Objects.equals(backOffMultiplier, that.backOffMultiplier) &&
                Objects.equals(expireTime, that.expireTime) &&
                Objects.equals(randomBound, that.randomBound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxRetrys, maxRetryDelay, retryDelay, useExponentialBackOff, backOffMultiplier, expireTime, randomBound);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RetryPolicy{");
        sb.append("maxRetrys=").append(maxRetrys);
        sb.append(", maxRetryDelay=").append(maxRetryDelay);
        sb.append(", retryDelay=").append(retryDelay);
        sb.append(", useExponentialBackOff=").append(useExponentialBackOff);
        sb.append(", backOffMultiplier=").append(backOffMultiplier);
        sb.append(", expireTime=").append(expireTime);
        sb.append(", randomBound=").append(randomBound);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        // 最大重试次数(无限制)
        private Integer maxRetrys;
        // 最大重试间隔(默认5分钟)
        private Integer maxRetryDelay;
        // 重试间隔
        private Integer retryDelay;
        // 指数增加间隔时间
        private Boolean useExponentialBackOff;
        // 指数系数，必须>=1
        private Double backOffMultiplier;
        // 过期时间（默认3天）
        private Integer expireTime;

        public static Builder build() {
            return new Builder();
        }

        public Builder maxRetrys(Integer maxRetrys) {
            this.maxRetrys = maxRetrys;
            return this;
        }

        public Builder maxRetryDelay(Integer maxRetryDelay) {
            this.maxRetryDelay = maxRetryDelay;
            return this;
        }

        public Builder retryDelay(Integer retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public Builder useExponentialBackOff(Boolean useExponentialBackOff) {
            this.useExponentialBackOff = useExponentialBackOff;
            return this;
        }

        public Builder backOffMultiplier(Double backOffMultiplier) {
            this.backOffMultiplier = backOffMultiplier;
            return this;
        }

        public Builder expireTime(Integer expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        public RetryPolicy create() {
            return new RetryPolicy(retryDelay, maxRetryDelay, maxRetrys, useExponentialBackOff, backOffMultiplier,
                    expireTime);
        }
    }
}