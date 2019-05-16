package com.jd.journalq.broker.limit;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.cluster.ClusterManager;

import java.util.concurrent.ConcurrentMap;

/**
 * RateLimiterManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/5/16
 */
// TODO 元数据处理
public class RateLimiterManager {

    private ClusterManager clusterManager;
    private ConcurrentMap<String /** app **/, ConcurrentMap<String /** topic **/, ConcurrentMap<String /** type **/, RateLimiter>>> rateLimiterMapper = Maps.newConcurrentMap();

    public RateLimiterManager(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
    }

    public RateLimiter getRateLimiter(String topic, String app, String type) {
        ConcurrentMap<String, RateLimiter> rateLimiterMapper = getOrCreateTypeRateLimiterMapper(topic, app);
        RateLimiter rateLimiter = rateLimiterMapper.get(type);
        if (rateLimiter != null) {
            return rateLimiter;
        }

        rateLimiter = new RateLimiter(100 * 1000, 1000 * 10);
        rateLimiterMapper.putIfAbsent(type, rateLimiter);
        return rateLimiterMapper.get(type);
    }

    protected ConcurrentMap<String, RateLimiter> getOrCreateTypeRateLimiterMapper(String topic, String app) {
        ConcurrentMap<String, ConcurrentMap<String, RateLimiter>> topicMapper = rateLimiterMapper.get(app);
        if (topicMapper == null) {
            topicMapper = Maps.newConcurrentMap();
            ConcurrentMap<String, ConcurrentMap<String, RateLimiter>> oldAppMapper = rateLimiterMapper.putIfAbsent(app, topicMapper);
            if (oldAppMapper != null) {
                topicMapper = oldAppMapper;
            }
        }

        ConcurrentMap<String, RateLimiter> typeMapper = topicMapper.get(topic);
        if (typeMapper == null) {
            typeMapper = Maps.newConcurrentMap();
            ConcurrentMap<String, RateLimiter> oldTypeMapper = topicMapper.putIfAbsent(topic, typeMapper);
            if (oldTypeMapper != null) {
                typeMapper = oldTypeMapper;
            }
        }

        return typeMapper;
    }
}