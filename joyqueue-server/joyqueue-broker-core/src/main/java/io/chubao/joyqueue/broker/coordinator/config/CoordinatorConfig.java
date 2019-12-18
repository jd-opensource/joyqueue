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
package io.chubao.joyqueue.broker.coordinator.config;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CoordinatorConfig
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class CoordinatorConfig {

    protected static final Logger logger = LoggerFactory.getLogger(CoordinatorConfig.class);

    private PropertySupplier propertySupplier;

    public CoordinatorConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public TopicName getGroupTopic() {
        return TopicName.parse(PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.GROUP_TOPIC_CODE));
    }

    public short getGroupTopicPartitions() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.GROUP_TOPIC_PARTITIONS);
    }

    public int getGroupExpireTime() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.GROUP_EXPIRE_TIME);
    }

    public TopicName getTransactionTopic() {
        return TopicName.parse(PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TRANSACTION_TOPIC_CODE));
    }

    public short getTransactionTopicPartitions() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TRANSACTION_TOPIC_PARTITIONS);
    }

    public String getTransactionLogApp() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TRANSACTION_LOG_APP);
    }

    public int getTransactionExpireTime() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TRANSACTION_EXPIRE_TIME);
    }

    public int getTransactionMaxNum() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.TRANSACTION_MAX_NUM);
    }

    public int getSessionTimeout() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.SESSION_SYNC_TIMEOUT);
    }

    public int getSessionExpireTime() {
        return PropertySupplier.getValue(propertySupplier, CoordinatorConfigKey.SESSION_EXPIRE_TIME);
    }
}