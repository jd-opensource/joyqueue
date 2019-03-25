package com.jd.journalq.broker.kafka.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 速度限制器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/7/2
 */
public class RateLimiter {

    private static final int LIMIT_CACHE_MAX_SIZE = 10240;

    protected static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    private KafkaConfig config;
    private volatile Cache<String /** key **/, AtomicInteger> limitCache;
    private volatile int lastStatTimeWindowSize;

    public RateLimiter(KafkaConfig config) {
        this.config = config;
        this.lastStatTimeWindowSize = config.getRateLimitWindowSize();
        this.limitCache = newLimitCache(lastStatTimeWindowSize);
    }

    public boolean tryAcquire(String key, int type) {
        try {
            Cache<String, AtomicInteger> limitCache = getLimitCache();
            AtomicInteger stat = limitCache.get(key, new Callable() {
                @Override
                public Object call() throws Exception {
                    return new AtomicInteger();
                }
            });

            return stat.incrementAndGet() <= config.getRateLimitTimes(type);
        } catch (Exception e) {
            logger.error("topicMetaRateLimiter tryAcquire exception", e);
            return true;
        }
    }

    protected Cache<String, AtomicInteger> getLimitCache() {
        if (lastStatTimeWindowSize != config.getRateLimitWindowSize()) {
            lastStatTimeWindowSize = config.getRateLimitWindowSize();
            limitCache = null;
        }
        if (limitCache == null) {
            limitCache = newLimitCache(lastStatTimeWindowSize);
        }
        return limitCache;
    }

    protected Cache<String, AtomicInteger> newLimitCache(int expire) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                .maximumSize(LIMIT_CACHE_MAX_SIZE)
                .build();
    }
}