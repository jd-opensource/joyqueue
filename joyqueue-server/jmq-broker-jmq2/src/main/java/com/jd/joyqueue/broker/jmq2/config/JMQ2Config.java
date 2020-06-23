/**
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
package com.jd.joyqueue.broker.jmq2.config;

import org.joyqueue.toolkit.config.PropertySupplier;

/**
 * JMQ2Config
 * author: gaohaoxiang
 * date: 2019/11/12
 */
public class JMQ2Config {

    private PropertySupplier propertySupplier;

    public JMQ2Config(PropertySupplier propertySupplier) {
        this.propertySupplier = propertySupplier;
    }

    public boolean getClusterBodyCacheEnable() {
        return PropertySupplier.getValue(propertySupplier, JMQ2ConfigKey.CLUSTER_BODY_CACHE_ENABLE);
    }

    public int getClusterBodyCacheExpireTime() {
        return PropertySupplier.getValue(propertySupplier, JMQ2ConfigKey.CLUSTER_BODY_CACHE_EXPIRE_TIME);
    }

    public int getClusterBodyCacheUpdateInterval() {
        return PropertySupplier.getValue(propertySupplier, JMQ2ConfigKey.CLUSTER_BODY_CACHE_UPDATE_INTERVAL);
    }

    public boolean getClusterBodyWithSlave() {
        return PropertySupplier.getValue(propertySupplier, JMQ2ConfigKey.CLUSTER_BODY_WITH_SLAVE);
    }

    public boolean getMessageBusinessIdRewrite(String topic) {
        return (boolean) PropertySupplier.getValue(propertySupplier,
                JMQ2ConfigKey.MESSAGE_BUSINESSID_REWRITE_PREFIX.getName() + topic,
                JMQ2ConfigKey.MESSAGE_BUSINESSID_REWRITE_PREFIX.getType(),
                JMQ2ConfigKey.MESSAGE_BUSINESSID_REWRITE_PREFIX.getValue());
    }
}