package org.joyqueue.broker.cluster.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * ClusterConfigKey
 * author: gaohaoxiang
 * date: 2020/3/25
 */
public enum ClusterConfigKey implements PropertyDef {

    GET_TOPIC_DYNAMIC_METADATA_TRANSPORT_TIMEOUT("cluster.topic.dynamic.metadata.transport.timeout", 500 * 1, Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_TIMEOUT("cluster.topic.dynamic.metadata.timeout", 1000 * 1, Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_CACHE_ENABLE("cluster.topic.dynamic.metadata.cache.enable", true, Type.BOOLEAN),
    GET_TOPIC_DYNAMIC_METADATA_CACHE_EXPIRE_TIME("cluster.topic.dynamic.metadata.cache.expire.time", 1000 * 10, Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_MIN_PARALLEL_THRESHOLD("cluster.topic.dynamic.metadata.min.parallel.threshold", 6, Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_MAX_PARALLEL_THRESHOLD("cluster.topic.dynamic.metadata.max.parallel.threshold", 15, Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_MAX_BATCH_THRESHOLD("cluster.topic.dynamic.metadata.max.batch.threshold", 50, Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_BATCH_PARALLEL_ENABLE("cluster.topic.dynamic.metadata.batch.parallel.enable", true, Type.BOOLEAN),
    GET_TOPIC_DYNAMIC_METADATA_BATCH_TIMEOUT("cluster.topic.dynamic.metadata.batch.timeout", 1000 * 2, Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_BATCH_MIN_THREADS("cluster.topic.dynamic.metadata.batch.min.threads", Runtime.getRuntime().availableProcessors(), Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_BATCH_MAX_THREADS("cluster.topic.dynamic.metadata.batch.max.threads", Runtime.getRuntime().availableProcessors() * 4, Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_BATCH_THREAD_QUEUE_SIZE("cluster.topic.dynamic.metadata.batch.thread.queue.size", 128, Type.INT),
    GET_TOPIC_DYNAMIC_METADATA_BATCH_THREAD_KEEPALIVE("cluster.topic.dynamic.metadata.batch.thread.keepalive", 1000 * 60, Type.INT),
    GET_TOPIC_LOCAL_ELECTION_ENABLE("cluster.topic.local.election.enable", true, Type.BOOLEAN),
    GET_TOPIC_DYNAMIC_ENABLE("cluster.topic.dynamic.enable", true, Type.BOOLEAN),

    ;

    public static final String TRANSPORT_KEY_PREFIX = "cluster.";

    private String name;
    private Object value;
    private PropertyDef.Type type;

    ClusterConfigKey(String name, Object value, PropertyDef.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public java.lang.String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }
}