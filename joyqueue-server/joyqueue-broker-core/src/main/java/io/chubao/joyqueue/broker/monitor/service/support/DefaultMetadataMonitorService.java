package io.chubao.joyqueue.broker.monitor.service.support;

import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.response.BooleanResponse;
import io.chubao.joyqueue.broker.monitor.service.MetadataMonitorService;

/**
 * DefaultMetadataMonitorService
 *
 * author: gaohaoxiang
 * date: 2019/2/11
 */
public class DefaultMetadataMonitorService implements MetadataMonitorService {

    private ClusterManager clusterManager;

    public DefaultMetadataMonitorService(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    @Override
    public TopicConfig getTopicMetadata(String topic, boolean isCluster) {
        TopicName topicName = TopicName.parse(topic);
        if (isCluster) {
            return clusterManager.getTopicConfig(topicName);
        } else {
            return clusterManager.getNameService().getTopicConfig(topicName);
        }
    }

    @Override
    public BooleanResponse getReadableResult(String topic, String app, String address) {
        TopicName topicName = TopicName.parse(topic);
        return clusterManager.checkReadable(topicName, app, address);
    }

    @Override
    public BooleanResponse getWritableResult(String topic, String app, String address) {
        TopicName topicName = TopicName.parse(topic);
        return clusterManager.checkWritable(topicName, app, address);
    }
}