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
package org.joyqueue.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * BrokerConfigKeys
 * author: gaohaoxiang
 * date: 2019/12/6
 */
public enum BrokerConfigKey implements PropertyDef {

    FRONTEND_SERVER_PORT("broker.frontend-server.transport.server.port", 50088, Type.INT),
    FRONTEND_SERVER_SHARDED_THREADS("broker.frontend-server.shared.threads", false, Type.BOOLEAN),
    FRONTEND_SERVER_COMMON_THREADS("broker.frontend-server.common.threads", Runtime.getRuntime().availableProcessors() * 4, Type.INT),
    FRONTEND_SERVER_COMMON_THREAD_KEEPALIVE("broker.frontend-server.common.thread.keepalive", 1000 * 60, Type.INT),
    FRONTEND_SERVER_COMMON_THREAD_QUEUE_SIZE("broker.frontend-server.common.thread.queue.size", 10240, Type.INT),
    FRONTEND_SERVER_FETCH_THREADS("broker.frontend-server.fetch.threads", Runtime.getRuntime().availableProcessors() * 4, Type.INT),
    FRONTEND_SERVER_FETCH_THREAD_KEEPALIVE("broker.frontend-server.fetch.thread.keepalive", 1000 * 60, Type.INT),
    FRONTEND_SERVER_FETCH_THREAD_QUEUE_SIZE("broker.frontend-server.fetch.thread.queue.size", 10240, Type.INT),
    FRONTEND_SERVER_PRODUCE_THREADS("broker.frontend-server.produce.threads", Runtime.getRuntime().availableProcessors() * 4, Type.INT),
    FRONTEND_SERVER_PRODUCE_THREAD_KEEPALIVE("broker.frontend-server.produce.thread.keepalive", 1000 * 60, Type.INT),
    FRONTEND_SERVER_PRODUCE_THREAD_QUEUE_SIZE("broker.frontend-server.produce.thread.queue.size", 10240, Type.INT),
    TRACER_TYPE("broker.tracer.type", "default", Type.STRING),
    // 详细日志
    LOG_DETAIL("broker.log.detail", false, Type.BOOLEAN),
    LOG_DETAIL_PREFIX("broker.log.detail.", false, Type.BOOLEAN),
    ;

    private String name;
    private Object value;
    private PropertyDef.Type type;

    BrokerConfigKey(String name, Object value, PropertyDef.Type type) {
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