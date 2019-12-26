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
import org.joyqueue.client.internal.metadata.MetadataManager;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.NameServerConfigChecker;
import com.google.common.base.Preconditions;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * ClusterManager
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class ClusterManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

    private NameServerConfig config;
    private ClusterClientManager clusterClientManager;

    private MetadataManager metadataManager;
    private MetadataCacheManager metadataCacheManager;
    private MetadataUpdater metadataUpdater;

    public ClusterManager(NameServerConfig config, ClusterClientManager clusterClientManager) {
        NameServerConfigChecker.check(config);
        Preconditions.checkArgument(clusterClientManager != null, "clusterClientManager can not be null");

        this.config = config;
        this.clusterClientManager = clusterClientManager;
    }

    public List<TopicMetadata> fetchTopicMetadataList(List<String> topics, String app) {
        Map<String, TopicMetadata> topicMetadataMap = fetchTopicMetadata(topics, app);
        List<TopicMetadata> result = Lists.newArrayListWithCapacity(topicMetadataMap.size());

        for (Map.Entry<String, TopicMetadata> entry : topicMetadataMap.entrySet()) {
            TopicMetadata topicMetadata = entry.getValue();
            if (topicMetadata != null) {
                result.add(topicMetadata);
            }
        }

        return result;
    }

    public Map<String, TopicMetadata> fetchTopicMetadata(List<String> topics, String app) {
        Map<String, TopicMetadata> result = Maps.newLinkedHashMap();
        List<String> needUpdateTopics = null;

        for (String topic : topics) {
            TopicMetadataHolder topicMetadataHolder = metadataCacheManager.getTopicMetadata(topic, app);
            if (topicMetadataHolder != null) {
                if (topicMetadataHolder.isExpired()) {
                    metadataUpdater.tryUpdateTopicMetadata(topic, app);
                }
                result.put(topic, topicMetadataHolder.getTopicMetadata());
            } else {
                if (needUpdateTopics == null) {
                    needUpdateTopics = Lists.newLinkedList();
                }
                needUpdateTopics.add(topic);
            }
        }

        if (CollectionUtils.isNotEmpty(needUpdateTopics)) {
            Map<String, TopicMetadata> topicMetadataMap = metadataUpdater.updateTopicMetadata(topics, app);
            result.putAll(topicMetadataMap);
        }

        return result;
    }

    public TopicMetadata fetchTopicMetadata(String topic, String app) {
        TopicMetadataHolder topicMetadataHolder = metadataCacheManager.getTopicMetadata(topic, app);
        if (topicMetadataHolder == null) {
            return metadataUpdater.updateTopicMetadata(topic, app);
        }
        if (topicMetadataHolder.isExpired()) {
            metadataUpdater.tryUpdateTopicMetadata(topic, app);
        }
        return topicMetadataHolder.getTopicMetadata();
    }

    public boolean tryUpdateTopicMetadata(String topic, String app) {
        TopicMetadataHolder topicMetadataHolder = metadataCacheManager.getTopicMetadata(topic, app);
        if (topicMetadataHolder != null && !topicMetadataHolder.isExpired(config.getTempMetadataInterval())) {
            return false;
        }
        return metadataUpdater.tryUpdateTopicMetadata(topic, app);
    }

    public TopicMetadata updateTopicMetadata(String topic, String app) {
        TopicMetadataHolder topicMetadataHolder = metadataCacheManager.getTopicMetadata(topic, app);
        if (topicMetadataHolder != null && !topicMetadataHolder.isExpired(config.getTempMetadataInterval())) {
            return topicMetadataHolder.getTopicMetadata();
        }
        return metadataUpdater.updateTopicMetadata(topic, app);
    }

    public TopicMetadata forceUpdateTopicMetadata(String topic, String app) {
        return metadataUpdater.updateTopicMetadata(topic, app);
    }

    public Map<String, TopicMetadata> forceUpdateTopicMetadata(List<String> topics, String app) {
        return metadataUpdater.updateTopicMetadata(topics, app);
    }

    @Override
    protected void validate() throws Exception {
        metadataCacheManager = new MetadataCacheManager(config);
        metadataManager = new MetadataManager(clusterClientManager);
        metadataUpdater = new MetadataUpdater(config, metadataManager, metadataCacheManager);
    }

    @Override
    protected void doStart() throws Exception {
        metadataUpdater.start();
//        logger.info("clusterManager is started");
    }

    @Override
    protected void doStop() {
        if (metadataUpdater != null) {
            metadataUpdater.stop();
        }
//        logger.info("clusterManager is stopped");
    }
}