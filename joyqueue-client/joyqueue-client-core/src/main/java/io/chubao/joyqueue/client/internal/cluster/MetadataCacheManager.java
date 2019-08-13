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
package io.chubao.joyqueue.client.internal.cluster;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.client.internal.cluster.domain.TopicMetadataHolder;
import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.toolkit.time.SystemClock;

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
        if (topicMetadata.getCode().equals(JoyQueueCode.SUCCESS)) {
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