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
package org.joyqueue.broker.producer;

import org.joyqueue.toolkit.config.PropertyDef;

public enum ProducerConfigKey implements PropertyDef {

    FIX_THREAD_POOL_THREADS("produce.fix.thread.pool.nThreads", 10, Type.INT),
    FEEDBACK_TIMEOUT("produce.feedback.timeout", 1000 * 60 * 1, Type.INT),
    TRANSACTION_EXPIRE_TIME("produce.transaction.expire.time", 1000 * 60 * 60 * 24 * 1, Type.INT),
    TRANSACTION_CLEAR_INTERVAL("produce.transaction.expire.clear.interval", 1000 * 60 * 10, Type.INT),
    TRANSACTION_MAX_UNCOMPLETE("produce.transaction.max.uncomplete", 10240, Type.INT),
    BROKER_QOS_LEVEL("broker.qos.level", -1, Type.INT),
    TOPIC_QOS_LEVEL_PREFIX("produce.topic.qos.level.", -1, Type.INT),
    APP_QOS_LEVEL_PREFIX("produce.app.qos.level.", -1, Type.INT),
    PRINT_METRIC_INTERVAL_MS("print.metric.interval", 0L ,Type.LONG),

    // businessId长度
    PRODUCE_BUSINESSID_LENGTH("produce.businessId.length", 100, PropertyDef.Type.INT),

    // body长度
    PRODUCE_BODY_LENGTH("produce.body.length", 1024 * 1024 * 3, PropertyDef.Type.INT),

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
