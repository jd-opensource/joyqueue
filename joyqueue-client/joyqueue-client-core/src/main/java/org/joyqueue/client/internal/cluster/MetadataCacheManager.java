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
package org.joyqueue.client.internal.cluster;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.client.internal.cluster.domain.TopicMetadataHolder;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.time.SystemClock;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * MetadataCacheManager
 *
 * author: gaohaoxiang
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
        TopicMetadataHolder oldTopicMetadataHolder = getTopicMap(app).get(topic);
        TopicMetadata oldTopicMetadata = null;
        if (oldTopicMetadataHolder != null) {
            oldTopicMetadata = oldTopicMetadataHolder.getTopicMetadata();
        }
        getTopicMap(app).put(topic, newTopicMetadataHolder(topic, topicMetadata, oldTopicMetadata));
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

    protected TopicMetadataHolder newTopicMetadataHolder(String topic, TopicMetadata topicMetadata, TopicMetadata oldTopicMetadata) {
        if (topicMetadata.getCode().equals(JoyQueueCode.SUCCESS)) {

            if (oldTopicMetadata != null) {
                topicMetadata.setAttachments(oldTopicMetadata.getAttachments());
                for (BrokerNode broker : topicMetadata.getBrokers()) {
                    BrokerNode oldBrokerNode = oldTopicMetadata.getBroker(broker.getId());
                    if (oldBrokerNode != null) {
                        broker.setAttachments(oldBrokerNode.getAttachments());
                    }
                }
            }

            if (topicMetadata.isAllAvailable()) {
                return new TopicMetadataHolder(topic, topicMetadata, SystemClock.now(), config.getUpdateMetadataInterval(), topicMetadata.getCode());
            } else {
                return new TopicMetadataHolder(topic, topicMetadata, SystemClock.now(), config.getTempMetadataInterval(), topicMetadata.getCode());
            }
        } else {
            return new TopicMetadataHolder(topic, null, SystemClock.now(), config.getTempMetadataInterval(), topicMetadata.getCode());
        }
    }
}