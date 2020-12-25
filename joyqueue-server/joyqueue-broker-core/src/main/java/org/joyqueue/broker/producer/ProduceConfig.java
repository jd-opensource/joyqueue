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

import org.joyqueue.broker.config.BrokerConfig;
import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * Created by chengzhiliang on 2018/10/30.
 */
public class ProduceConfig {
    private PropertySupplier propertySupplier;
    private BrokerConfig brokerConfig;

    public ProduceConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
        this.brokerConfig = new BrokerConfig(propertySupplier);

    }

    public int getTopicQosLevel(String topic) {
        return PropertySupplier.getValue(propertySupplier,
                ProducerConfigKey.TOPIC_QOS_LEVEL_PREFIX.getName() + topic,
                ProducerConfigKey.TOPIC_QOS_LEVEL_PREFIX.getType(),
                ProducerConfigKey.TOPIC_QOS_LEVEL_PREFIX.getValue());
    }

    public int getAppQosLevel(String app) {
        return PropertySupplier.getValue(propertySupplier,
                ProducerConfigKey.APP_QOS_LEVEL_PREFIX.getName() + app,
                ProducerConfigKey.APP_QOS_LEVEL_PREFIX.getType(),
                ProducerConfigKey.APP_QOS_LEVEL_PREFIX.getValue());
    }

    public int getFeedbackTimeout() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.FEEDBACK_TIMEOUT);
    }

    public int getTransactionExpireClearInterval() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.TRANSACTION_CLEAR_INTERVAL);
    }

    public int getTransactionMaxUncomplete() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.TRANSACTION_MAX_UNCOMPLETE);
    }

    public int getTransactionExpireTime() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.TRANSACTION_EXPIRE_TIME);
    }

    public int getFixThreadPoolNThreads() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.FIX_THREAD_POOL_THREADS);
    }

    public int getBrokerQosLevel() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.BROKER_QOS_LEVEL);
    }

    public long getPrintMetricIntervalMs() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.PRINT_METRIC_INTERVAL_MS);
    }

    public int getBusinessIdLength() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.PRODUCE_BUSINESSID_LENGTH);
    }

    public int getBodyLength() {
        return PropertySupplier.getValue(propertySupplier, ProducerConfigKey.PRODUCE_BODY_LENGTH);
    }

    public boolean getLogDetail(String app) {
        return brokerConfig.getLogDetail(app);
    }
}