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
package org.joyqueue.broker.kafka.config;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.toolkit.config.PropertyDef;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaConfig
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class KafkaConfig {
    protected static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    private PropertySupplier propertySupplier;

    public KafkaConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public boolean getProduceDelayEnable() {
        return getConfig(KafkaConfigKey.PRODUCE_DELAY_ENABLE);
    }

    public int getProduceDelay() {
        return getConfig(KafkaConfigKey.PRODUCE_DELAY);
    }

    public boolean getLogDetail(String app) {
        return (boolean) getConfig(KafkaConfigKey.LOG_DETAIL)
                || (boolean) PropertySupplier.getValue(propertySupplier,
                KafkaConfigKey.LOG_DETAIL_PREFIX.getName() + app,
                KafkaConfigKey.LOG_DETAIL_PREFIX.getType(),
                KafkaConfigKey.LOG_DETAIL_PREFIX.getValue());
    }

    public int getProduceTimeout() {
        return getConfig(KafkaConfigKey.PRODUCE_TIMEOUT);
    }

    public boolean getMetadataDelayEnable() {
        return getConfig(KafkaConfigKey.METADATA_DELAY_ENABLE);
    }

    public int getMetadataDelay() {
        return getConfig(KafkaConfigKey.METADATA_DELAY);
    }

    public boolean getFetchDelay() {
        return getConfig(KafkaConfigKey.FETCH_DELAY);
    }

    public int getOffsetSyncTimeout() {
        return getConfig(KafkaConfigKey.OFFSET_SYNC_TIMEOUT);
    }

    public int getTransactionSyncTimeout() {
        return getConfig(KafkaConfigKey.TRANSACTION_SYNC_TIMEOUT);
    }

    public int getTransactionTimeout() {
        return getConfig(KafkaConfigKey.TRANSACTION_TIMEOUT);
    }

    public int getTransactionLogRetries() {
        return getConfig(KafkaConfigKey.TRANSACTION_LOG_RETRIES);
    }

    public int getTransactionLogInterval() {
        return getConfig(KafkaConfigKey.TRANSACTION_LOG_INTERVAL);
    }

    public int getTransactionProducerSequenceExpire() {
        return getConfig(KafkaConfigKey.TRANSACTION_PRODUCER_SEQUENCE_EXPIRE);
    }

    public String getTransactionLogApp() {
        return getConfig(KafkaConfigKey.TRANSACTION_LOG_APP);
    }

    public int getTransactionLogScanSize() {
        return getConfig(KafkaConfigKey.TRANSACTION_LOG_SCAN_SIZE);
    }

    public QosLevel getTransactionLogWriteQosLevel() {
        return QosLevel.valueOf((int) getConfig(KafkaConfigKey.TRANSACTION_LOG_WRITE_QOSLEVEL));
    }

    public int getSessionMaxTimeout() {
        return getConfig(KafkaConfigKey.SESSION_MIN_TIMEOUT);
    }

    public int getSessionMinTimeout() {
        return getConfig(KafkaConfigKey.SESSION_MAX_TIMEOUT);
    }

    public int getRebalanceInitialDelay() {
        return getConfig(KafkaConfigKey.REBALANCE_INITIAL_DELAY);
    }

    public int getRebalanceTimeout() {
        return getConfig(KafkaConfigKey.REBALANCE_TIMEOUT);
    }

    protected <T> T getConfig(String key, PropertyDef.Type type, Object defaultValue) {
        return PropertySupplier.getValue(this.propertySupplier, key, type, defaultValue);
    }

    protected <T> T getConfig(PropertyDef key) {
        return PropertySupplier.getValue(this.propertySupplier, key);
    }
}