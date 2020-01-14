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
package org.joyqueue.client.internal.metadata;

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.metadata.converter.ClusterMetadataConverter;
import org.joyqueue.client.internal.metadata.domain.ClusterMetadata;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.network.command.FetchClusterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MetadataManager
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class MetadataManager {

    protected static final Logger logger = LoggerFactory.getLogger(MetadataManager.class);

    private ClusterClientManager clusterClientManager;

    public MetadataManager(ClusterClientManager clusterClientManager) {
        this.clusterClientManager = clusterClientManager;
    }

    public TopicMetadata fetchMetadata(String topic, String app) {
        ClusterMetadata clusterMetadata = fetchMetadata(Lists.newArrayList(topic), app);
        return clusterMetadata.getTopic(topic);
    }

    public ClusterMetadata fetchMetadata(List<String> topics, String app) {
        FetchClusterResponse fetchClusterResponse = clusterClientManager.getOrCreateClient().fetchCluster(topics, app);
        if (logger.isDebugEnabled()) {
            logger.debug("fetch metadata, topics: {}, app: {}, metadata: {}", topics, app, fetchClusterResponse);
        }
        return ClusterMetadataConverter.convert(fetchClusterResponse);
    }
}