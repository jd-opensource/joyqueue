package io.chubao.joyqueue.broker.producer;

import io.chubao.joyqueue.toolkit.config.PropertyDef;

public enum ProducerConfigKey implements PropertyDef {

    FIX_THREAD_POOL_THREADS("produce.fix.thread.pool.nThreads", 10, Type.INT),
    FEEDBACK_TIMEOUT("produce.feedback.timeout", 1000 * 60 * 1, Type.INT),
    TRANSACTION_EXPIRE_TIME("produce.transaction.expire.time", 1000 * 60 * 60 * 24 * 1, Type.INT),
    TRANSACTION_CLEAR_INTERVAL("produce.transaction.expire.clear.interval", 1000 * 60 * 10, Type.INT),
    TRANSACTION_MAX_UNCOMPLETE("produce.transaction.max.uncomplete", 10240, Type.INT),
    BROKER_QOS_LEVEL("broker.qos.level", -1, Type.INT),
    BROKER_QOS_LEVEL_PREFIX("broker.qos.level.", -1, Type.INT),
    PRINT_METRIC_INTERVAL_MS("print.metric.interval", 0L ,Type.LONG),

    // businessId长度
    PRODUCE_BUSINESSID_LENGTH("produce.businessId.length", 100, PropertyDef.Type.INT),

    // body长度
    PRODUCE_BODY_LENGTH("produce.body.length", 1024 * 1024 * 5, PropertyDef.Type.INT),

    ;


    private String name;
    private Object value;
    private PropertyDef.Type type;

    ProducerConfigKey(String name, Object value, PropertyDef.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public String getName() {
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
