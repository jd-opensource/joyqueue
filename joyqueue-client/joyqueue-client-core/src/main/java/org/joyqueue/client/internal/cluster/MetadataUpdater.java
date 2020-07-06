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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joyqueue.client.internal.metadata.MetadataManager;
import org.joyqueue.client.internal.metadata.domain.ClusterMetadata;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * MetadataUpdater
 *
 * author: gaohaoxiang
 * date: 2019/1/2
 */
public class MetadataUpdater extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(MetadataUpdater.class);

    private NameServerConfig config;
    private MetadataManager metadataManager;
    private MetadataCacheManager metadataCacheManager;

    private Set<TopicAndApp> updateFilter = Sets.newConcurrentHashSet();
    private ExecutorService updateThreadPool;

    public static boolean printLog = false;
    public static int printLogInterval = 1000 * 60 * 1;
    private long lastPrintLog;

    public MetadataUpdater(NameServerConfig config, MetadataManager metadataManager, MetadataCacheManager metadataCacheManager) {
        this.config = config;
        this.metadataManager = metadataManager;
        this.metadataCacheManager = metadataCacheManager;
    }

    @Override
    protected void validate() throws Exception {
        updateThreadPool = new ThreadPoolExecutor(config.getUpdateMetadataThread(), config.getUpdateMetadataThread(), 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(config.getUpdateMetadataQueueSize()), new NamedThreadFactory("joyqueue-cluster-updater"));
    }

    @Override
    protected void doStop() {
        if (updateThreadPool != null) {
            updateThreadPool.shutdown();
        }
    }

    public boolean tryUpdateTopicMetadata(final String topic, final String app) {
        final TopicAndApp topicAndApp = new TopicAndApp(topic, app);
        if (!updateFilter.add(topicAndApp)) {
            return false;
        }
        printLog();
        updateThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                updateTopicMetadata(topic, app);
                updateFilter.remove(topicAndApp);
            }
        });
        return true;
    }

    public boolean tryUpdateTopicMetadata(final List<String> topics, final String app) {
        final TopicAndApp topicAndApp = new TopicAndApp(topics, app);
        if (!updateFilter.add(topicAndApp)) {
            return false;
        }
        printLog();
        updateThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                updateTopicMetadata(topics, app);
                updateFilter.remove(topicAndApp);
            }
        });
        return true;
    }

    public TopicMetadata updateTopicMetadata(String topic, String app) {
        logger.debug("update topic metadata, topic: {}, app: {}", topic, app);

        try {
            TopicMetadata topicMetadata = metadataManager.fetchMetadata(topic, app);
            metadataCacheManager.putTopicMetadata(topic, app, topicMetadata);
            if (topicMetadata.getCode().equals(JoyQueueCode.SUCCESS)) {
                return topicMetadata;
            }
            return null;
        } catch (Exception e) {
            logger.error("update topic metadata exception, topic: {}, app: {}", topic, app, e);

            if (metadataCacheManager.getTopicMetadata(topic, app) == null) {
                metadataCacheManager.putTopicMetadata(topic, app, new TopicMetadata(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE));
            }
            return null;
        }
    }

    public Map<String, TopicMetadata> updateTopicMetadata(List<String> topics, String app) {
        logger.debug("update topic metadata, topics: {}, app: {}", topics, app);
        Map<String, TopicMetadata> result = Maps.newHashMap();

        try {
            ClusterMetadata clusterMetadata = metadataManager.fetchMetadata(topics, app);
            for (String topic : topics) {
                TopicMetadata topicMetadata = clusterMetadata.getTopic(topic);
                metadataCacheManager.putTopicMetadata(topic, app, topicMetadata);
                if (topicMetadata.getCode().equals(JoyQueueCode.SUCCESS)) {
                    result.put(topic, topicMetadata);
                }
            }
        } catch (Exception e) {
            logger.error("update topic metadata exception, topics: {}, app: {}", topics, app, e);

            for (String topic : topics) {
                if (metadataCacheManager.getTopicMetadata(topic, app) == null) {
                    metadataCacheManager.putTopicMetadata(topic, app, new TopicMetadata(JoyQueueCode.CN_SERVICE_NOT_AVAILABLE));
                }
            }
        }

        return result;
    }

    protected void printLog() {
        if (!printLog) {
            return;
        }
        if (SystemClock.now() - lastPrintLog > printLogInterval) {
            logger.info("metadata update filter, current size {}", updateFilter.size());
        }
        lastPrintLog = SystemClock.now();
    }
}