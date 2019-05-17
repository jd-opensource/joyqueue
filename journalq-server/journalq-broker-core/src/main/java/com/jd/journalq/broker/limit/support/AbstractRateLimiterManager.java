package com.jd.journalq.broker.limit.support;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.limit.RateLimitManager;
import com.jd.journalq.broker.limit.RateLimiter;
import com.jd.journalq.broker.limit.config.LimiterConfig;

import java.util.concurrent.ConcurrentMap;

/**
 * AbstractRateLimiterManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
public abstract class AbstractRateLimiterManager implements RateLimitManager {

    private static final RateLimiter NONE_RATE_LIMITER = new NoneRateLimiter();

    private ConcurrentMap<String /** topic **/, ConcurrentMap<String /** app **/, ConcurrentMap<String /** type **/, RateLimiter>>> rateLimiterMapper = Maps.newConcurrentMap();

    @Override
    public RateLimiter getRateLimiter(String topic, String app, String type) {
        ConcurrentMap<String, RateLimiter> rateLimiterMapper = getOrCreateTypeRateLimiterMapper(topic, app);
        RateLimiter rateLimiter = rateLimiterMapper.get(type);
        if (rateLimiter != null) {
            return rateLimiter;
        }

        rateLimiter = newRateLimiter(topic, app, type);
        RateLimiter oldRateLimiter = rateLimiterMapper.putIfAbsent(type, rateLimiter);
        if (oldRateLimiter != null) {
            rateLimiter = oldRateLimiter;
        }
        return rateLimiter;
    }

    protected RateLimiter newRateLimiter(String topic, String app, String type) {
        LimiterConfig limiterConfig = getLimiterConfig(topic, app, type);
        if (limiterConfig == null) {
            return NONE_RATE_LIMITER;
        }
        return newRateLimiter(topic, app, type, limiterConfig.getTps(), limiterConfig.getTraffic());
    }

    protected RateLimiter newRateLimiter(String topic, String app, String type, int tps, int traffic) {
        return new DefaultRateLimiter(tps, traffic);
    }

    protected ConcurrentMap<String, RateLimiter> getOrCreateTypeRateLimiterMapper(String topic, String app) {
        ConcurrentMap<String, ConcurrentMap<String, RateLimiter>> appMapper = rateLimiterMapper.get(topic);
        if (appMapper == null) {
            appMapper = Maps.newConcurrentMap();
            ConcurrentMap<String, ConcurrentMap<String, RateLimiter>> oldAppMapper = rateLimiterMapper.putIfAbsent(topic, appMapper);
            if (oldAppMapper != null) {
                appMapper = oldAppMapper;
            }
        }

        ConcurrentMap<String, RateLimiter> typeMapper = appMapper.get(app);
        if (typeMapper == null) {
            typeMapper = Maps.newConcurrentMap();
            ConcurrentMap<String, RateLimiter> oldTypeMapper = appMapper.putIfAbsent(app, typeMapper);
            if (oldTypeMapper != null) {
                typeMapper = oldTypeMapper;
            }
        }

        return typeMapper;
    }

    protected void removeAppRateLimiter(String topic, String app) {
        ConcurrentMap<String, ConcurrentMap<String, RateLimiter>> appMapper = rateLimiterMapper.get(topic);
        if (appMapper == null) {
            return;
        }
        appMapper.remove(app);
    }

    protected void removeTopicRateLimiter(String topic) {
        rateLimiterMapper.remove(topic);
    }

    protected abstract LimiterConfig getLimiterConfig(String topic, String app, String type);

}