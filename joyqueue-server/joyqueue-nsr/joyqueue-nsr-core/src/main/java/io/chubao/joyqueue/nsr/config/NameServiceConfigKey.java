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
package io.chubao.joyqueue.nsr.config;

import io.chubao.joyqueue.toolkit.config.PropertyDef;

public enum NameServiceConfigKey implements PropertyDef {

    NAMESERVER_ADDRESS("nameservice.serverAddress", "127.0.0.1:50092", Type.STRING),
    NAMESERVER_ALL_METADATA_CACHE_ENABLE("nameservice.allmetadata.cache.enable", true, Type.BOOLEAN),
    NAMESERVER_ALL_METADATA_CACHE_EXPIRE_TIME("nameservice.allmetadata.cache.expire.time", 1000 * 1, Type.INT),
    NAMESERVER_COMPENSATION_DELAY("nameservice.compensation.delay", 1000 * 60 * 1, Type.INT),
    NAMESERVER_COMPENSATION_INTERVAL("nameservice.compensation.interval", 1000 * 30, Type.INT),
    NAMESERVER_COMPENSATION_TOPIC_ENABLE("nameservice.compensation.topic.enable", true, Type.BOOLEAN),
    NAMESERVER_COMPENSATION_BROKER_ENABLE("nameservice.compensation.broker.enable", true, Type.BOOLEAN),
    NAMESERVER_COMPENSATION_PRODUCER_ENABLE("nameservice.compensation.producer.enable", true, Type.BOOLEAN),
    NAMESERVER_COMPENSATION_CONSUMER_ENABLE("nameservice.compensation.consumer.enable", true, Type.BOOLEAN),
    NAMESERVER_COMPENSATION_DATACENTER_ENABLE("nameservice.compensation.datacenter.enable", true, Type.BOOLEAN),
    NAMESERVER_COMPENSATION_CONFIG_ENABLE("nameservice.compensation.config.enable", true, Type.BOOLEAN),
    NAMESERVER_COMPENSATION_CACHE_ENABLE("nameservice.compensation.cache.enable", false, Type.BOOLEAN),
    NAMESERVER_MESSENGER_TYPE("nameservice.messenger.type", "default", Type.STRING),
    NAMESERVER_MESSENGER_IGNITE_ENABLE("nameservice.messenger.ignite.enable", false, Type.BOOLEAN), // TODO 临时配置，是否启用ignite推送
    NAMESERVER_THIN_TRANSPORT_TIMEOUT("nameservice.thin.transport.timeout", 1000 * 10, Type.INT),
    NAMESERVER_THIN_TRANSPORT_TOPIC_TIMEOUT("nameservice.thin.transport.topic.timeout", 1000 * 1, Type.INT),
    NAMESERVER_THIN_CACHE_ENABLE("nameservice.thin.cache.enable", false, Type.BOOLEAN),
    NAMESERVER_THIN_CACHE_EXPIRE_TIME("nameservice.thin.cache.expire.time", 1000 * 1, Type.INT),

    ;

    public static final String NAMESERVICE_KEY_PREFIX ="nameservice.";

    private String name;
    private Object value;
    private PropertyDef.Type type;

    NameServiceConfigKey(String name, Object value, PropertyDef.Type type) {
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
