package org.joyqueue.broker.cluster.config;

import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * ClusterConfig
 * author: gaohaoxiang
 * date: 2020/3/25
 */
public class ClusterConfig {

    private PropertySupplier propertySupplier;

    public ClusterConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public int getTopicDynamicMetadataTransportTimeout() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_TRANSPORT_TIMEOUT);
    }

    public int getTopicDynamicMetadataTimeout() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_TIMEOUT);
    }

    public boolean getTopicDynamicMetadataCacheEnable() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_ENABLE);
    }

    public int getTopicDynamicMetadataCacheExpireTime() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_CACHE_EXPIRE_TIME);
    }

    public int getTopicDynamicMetadataMinParallelThreshold() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_MIN_PARALLEL_THRESHOLD);
    }

    public int getTopicDynamicMetadataMaxParallelThreshold() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_MAX_PARALLEL_THRESHOLD);
    }

    public int getTopicDynamicMetadataMaxBatchThreshold() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_MAX_BATCH_THRESHOLD);
    }

    public boolean getTopicDynamicMetadataBatchParallelEnable() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_BATCH_PARALLEL_ENABLE);
    }

    public int getTopicDynamicMetadataBatchTimeout() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_BATCH_TIMEOUT);
    }

    public int getTopicDynamicMetadataBatchMinThreads() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_BATCH_MIN_THREADS);
    }

    public int getTopicDynamicMetadataBatchMaxThreads() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_BATCH_MAX_THREADS);
    }

    public int getTopicDynamicMetadataBatchQueueSize() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_BATCH_THREAD_QUEUE_SIZE);
    }

    public int getTopicDynamicMetadataBatchKeepalive() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_METADATA_BATCH_THREAD_KEEPALIVE);
    }

    public boolean getTopicLocalElectionEnable() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_LOCAL_ELECTION_ENABLE);
    }

    public boolean getTopicDynamicEnable() {
        return PropertySupplier.getValue(propertySupplier, ClusterConfigKey.GET_TOPIC_DYNAMIC_ENABLE);
    }
}
