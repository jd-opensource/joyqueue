package com.jd.journalq.client.internal.cluster;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.client.internal.cluster.domain.TopicMetadataHolder;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.toolkit.time.SystemClock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * MetadataCacheManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class MetadataCacheManager {

    private NameServerConfig config;
    private ConcurrentMap<String /** app **/, ConcurrentMap<String /** topic **/, TopicMetadataHolder>> topicMetadataCache = Maps.newConcurrentMap();

    public MetadataCacheManager(NameServerConfig config) {
        this.config = config;
    }

    public List<String> getTopics(String app) {
        return Lists.newArrayList(getTopicMap(app).keySet());
    }

    public TopicMetadataHolder getTopicMetadata(String topic, String app) {
        return getTopicMap(app).get(topic);
    }

    public void putTopicMetadata(String topic, String app, TopicMetadata topicMetadata) {
        getTopicMap(app).put(topic, newTopicMetadataHolder(topic, topicMetadata));
    }

    public void putTopicMetadata(Map<String, TopicMetadata> topicMetadata, String app) {
        ConcurrentMap<String, TopicMetadataHolder> topicMap = getTopicMap(app);
        for (Map.Entry<String, TopicMetadata> entry : topicMetadata.entrySet()) {
            topicMap.put(entry.getKey(), newTopicMetadataHolder(entry.getKey(), entry.getValue()));
        }
    }

    public void setTopicMetadata(Map<String, TopicMetadata> topicMetadata, String app) {
        ConcurrentMap<String, TopicMetadataHolder> newTopicMap = Maps.newConcurrentMap();
        for (Map.Entry<String, TopicMetadata> entry : topicMetadata.entrySet()) {
            newTopicMap.put(entry.getKey(), newTopicMetadataHolder(entry.getKey(), entry.getValue()));
        }
        topicMetadataCache.put(app, newTopicMap);
    }

    protected ConcurrentMap<String, TopicMetadataHolder> getTopicMap(String app) {
        ConcurrentMap<String, TopicMetadataHolder> topicMap = topicMetadataCache.get(app);
        if (topicMap == null) {
            topicMap = Maps.newConcurrentMap();
            ConcurrentMap<String, TopicMetadataHolder> oldTopicMap = topicMetadataCache.putIfAbsent(app, topicMap);
            if (oldTopicMap != null) {
                topicMap = oldTopicMap;
            }
        }
        return topicMap;
    }

    protected TopicMetadataHolder newTopicMetadataHolder(String topic, TopicMetadata topicMetadata) {
        if (topicMetadata.getCode().equals(JMQCode.SUCCESS)) {
            return new TopicMetadataHolder(topic, topicMetadata, SystemClock.now(), config.getUpdateMetadataInterval(), topicMetadata.getCode());
        } else {
            return new TopicMetadataHolder(topic, null, SystemClock.now(), config.getTempMetadataInterval(), topicMetadata.getCode());
        }
    }
}