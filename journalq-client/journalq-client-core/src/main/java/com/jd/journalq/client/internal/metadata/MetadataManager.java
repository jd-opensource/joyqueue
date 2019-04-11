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
package com.jd.journalq.client.internal.metadata;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.cluster.ClusterClientManager;
import com.jd.journalq.client.internal.metadata.converter.ClusterMetadataConverter;
import com.jd.journalq.client.internal.metadata.domain.ClusterMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.network.command.FetchClusterAck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MetadataManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
        FetchClusterAck fetchClusterAck = clusterClientManager.getOrCreateClient().fetchCluster(topics, app);
        if (logger.isDebugEnabled()) {
            logger.debug("fetch metadata, topics: {}, app: {}, metadata: {}", topics, app, JSON.toJSONString(fetchClusterAck));
        }
        return ClusterMetadataConverter.convert(fetchClusterAck);
    }
}