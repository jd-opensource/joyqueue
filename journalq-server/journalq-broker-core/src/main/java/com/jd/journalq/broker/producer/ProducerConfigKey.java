package com.jd.journalq.broker.producer;

import com.jd.journalq.toolkit.config.PropertyDef;

public enum ProducerConfigKey implements PropertyDef {

    FIX_THREAD_POOL_THREADS("produce.fix.thread.pool.nThreads", 10, Type.INT),
    FEEDBACK_TIMEOUT("produce.feedback.timeout", 1000 * 60 * 1, Type.INT),
    TRANSACTION_EXPIRE_TIME("produce.transaction.expire.time", 1000 * 60 * 60 * 24 * 7, Type.INT),
    TRANSACTION_CLEAR_INTERVAL("produce.transaction.expire.clear.interval", 1000 * 60 * 10, Type.INT),
    BROKER_QOS_LEVEL("broker.qos.level", -1, Type.INT);

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
