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
package org.joyqueue.broker.mqtt.config;

import org.joyqueue.toolkit.config.PropertyDef;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author majun8
 */
public class MqttConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttConfig.class);

    private PropertySupplier propertySupplier;

    public MqttConfig(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public String getConnectionThreadPoolName() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_CONNECTION);
    }

    public String getPingThreadPoolName() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_PING);
    }

    public String getSubscriptionThreadPoolName() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_SUBSCRIPTION);
    }

    public String getPublishThreadPoolName() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_PUBLISH);
    }

    public int getConnectionThreadPoolSize() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_CONNECTION_THREAD);
    }

    public int getPingThreadPoolSize() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_PING_THREAD);
    }

    public int getSubscriptionThreadPoolSize() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_SUBSCRIPTION_THREAD);
    }

    public int getPublishThreadPoolSize() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_PUBLISH_THREAD);
    }

    public int getConnectionThreadPoolQueueSize() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_CONNECTION_QUEUESIZE);
    }

    public int getPingThreadPoolQueueSize() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_PING_QUEUESIZE);
    }

    public int getSubscriptionThreadPoolQueueSize() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_SUBSCRIPTION_QUEUESIZE);
    }

    public int getPublishThreadPoolQueueSize() {
        return getConfig(MqttConfigKey.EXECUTOR_SERVICE_PUBLISH_QUEUESIZE);
    }

    public int getMaxPayloadSize() {
        return getConfig(MqttConfigKey.MAX_PAYLOAD_SIZE);
    }

    protected <T> T getConfig(PropertyDef key) {
        return PropertySupplier.getValue(this.propertySupplier, key);
    }
}
