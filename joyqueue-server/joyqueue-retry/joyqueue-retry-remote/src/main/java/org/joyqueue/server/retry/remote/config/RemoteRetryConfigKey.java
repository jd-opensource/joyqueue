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
package org.joyqueue.server.retry.remote.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * @author liyue25
 * Date: 2019-07-05
 */
public enum  RemoteRetryConfigKey implements PropertyDef {

    REMOTE_RETRY_LIMIT_THREADS("retry.remote.retry.limit.thread", 10, Type.INT),
    REMOTE_RETRY_UPDATE_INTERVAL("retry.remote.retry.update.interval", 60000L, Type.LONG),
    REMOTE_RETRY_TRANSPORT_TIMEOUT("retry.remote.retry.transport.timeout", 1000 * 1, Type.INT),
    REMOTE_RETRY_THREADS("retry.remote.retry.threads", Runtime.getRuntime().availableProcessors() * 4, Type.INT),
    REMOTE_RETRY_THREAD_QUEUE_SIZE("retry.remote.retry.thread.queue.size", 1024, Type.INT),
    REMOTE_RETRY_THREAD_KEEPALIVE("retry.remote.retry.thread.keepalive", 1000 * 60, Type.INT),

    ;


    private String name;
    private Object value;
    private PropertyDef.Type type;

    RemoteRetryConfigKey(String name, Object value, PropertyDef.Type type) {
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
