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
package org.joyqueue.nsr.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * MessengerConfigKey
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public enum MessengerConfigKey implements PropertyDef {

    SESSION_EXPIRE_TIME("nameservice.messenger.session.expire.time", 1000 * 60 * 5, Type.INT),
    SESSION_TIMEOUT("nameservice.messenger.session.timeout", 1000 * 5, Type.INT),
    PUBLISH_ENABLE("nameservice.messenger.publish.enable", true, Type.BOOLEAN),
    PUBLISH_TIMEOUT("nameservice.messenger.publish.timeout", 1000 * 10, Type.INT),
    HANDLER_THREADS("nameservice.messenger.handler.threads", Runtime.getRuntime().availableProcessors(), Type.INT),
    HANDLER_KEEPALIVE("nameservice.messenger.handler.keepalive", 1000 * 60, Type.INT),
    HANDLER_QUEUES("nameservice.messenger.handler.queues", 1024, Type.INT),
    PUBLISH_FORCE("nameservice.messenger.publish.force", true, Type.BOOLEAN),
    PUBLISH_IGNORE_CONNECTION_ERROR("nameservice.messenger.publish.ignore.connection.error", true, Type.BOOLEAN),
    HEARTBEAT_INTERVAL("nameservice.messenger.heartbeat.interval", 1000 * 10, Type.INT),
    HEARTBEAT_TIMEOUT("nameservice.messenger.heartbeat.timeout", 1000, Type.INT),

    ;

    public static final String MESSENGER_SERVER_CONFIG_PREFIX = "nameservice.messenger.server.";
    public static final String MESSENGER_CLIENT_CONFIG_PREFIX = "nameservice.messenger.client.";

    private String name;
    private Object value;
    private PropertyDef.Type type;

    MessengerConfigKey(String name, Object value, PropertyDef.Type type) {
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
