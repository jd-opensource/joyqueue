package io.chubao.joyqueue.client.internal.metadata;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.client.internal.cluster.ClusterClientManager;
import io.chubao.joyqueue.client.internal.metadata.converter.ClusterMetadataConverter;
import io.chubao.joyqueue.client.internal.metadata.domain.ClusterMetadata;
import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.network.command.FetchClusterResponse;
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