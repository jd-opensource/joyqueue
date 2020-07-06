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
package org.joyqueue.client.internal.trace;

import org.joyqueue.toolkit.time.SystemClock;

/**
 * TraceContext
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class TraceContext {

    private String topic;
    private String app;
    private String namespace;
    private TraceType type;
    private long startTime;

    public TraceContext(String topic, String app, String namespace, TraceType type) {
        this(topic, app, namespace, type, SystemClock.now());
    }

    public TraceContext(String topic, String app, String namespace, TraceType type, long startTime) {
        this.topic = topic;
        this.app = app;
        this.namespace = namespace;
        this.type = type;
        this.startTime = startTime;
    }

    public long getInterval() {
        return SystemClock.now() - startTime;
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public String getNamespace() {
        return namespace;
    }

    public TraceType getType() {
        return type;
    }

    public long getStartTime() {
        return startTime;
    }
}