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
package org.joyqueue.broker.protocol.config;

import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * JoyQueueConfig
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class JoyQueueConfig {
    protected PropertySupplier propertySupplier;

    public static void main(String[] args) {

    }

    public JoyQueueConfig() {

    }

    public JoyQueueConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public int getProduceMaxTimeout() {
        return PropertySupplier.getValue(propertySupplier, JoyQueueConfigKey.PRODUCE_MAX_TIMEOUT);
    }

    public String getCoordinatorPartitionAssignType() {
        return PropertySupplier.getValue(propertySupplier, JoyQueueConfigKey.COORDINATOR_PARTITION_ASSIGN_TYPE);
    }

    public int getCoordinatorPartitionAssignMinConnections() {
        return PropertySupplier.getValue(propertySupplier, JoyQueueConfigKey.COORDINATOR_PARTITION_ASSIGN_MIN_CONNECTIONS);
    }

    public int getCoordinatorPartitionAssignTimeoutOverflow() {
        return PropertySupplier.getValue(propertySupplier, JoyQueueConfigKey.COORDINATOR_PARTITION_ASSIGN_TIMEOUT_OVERFLOW);
    }
}