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

import org.joyqueue.client.internal.trace.support.TraceWrapper;
import org.joyqueue.toolkit.time.SystemClock;

/**
 * TraceBuilder
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class TraceBuilder {

    private String topic;
    private String app;
    private String namespace;
    private TraceType type;
    private long startTime;

    public static TraceBuilder newInstance() {
        return new TraceBuilder();
    }

    public TraceBuilder topic(String topic) {
        this.topic = topic;
        return this;
    }

    public TraceBuilder app(String app) {
        this.app = app;
        return this;
    }

    public TraceBuilder type(TraceType type) {
        this.type = type;
        return this;
    }

    public TraceBuilder startTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public TraceBuilder namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public TraceContext context() {
        if (startTime == 0) {
            startTime = SystemClock.now();
        }
        return new TraceContext(topic, app, namespace, type, startTime);
    }

    public TraceCaller begin(String topic, String app, TraceType type) {
        TraceContext context = new TraceContext(topic, app, namespace, type, SystemClock.now());
        return TraceWrapper.getInstance().begin(context);
    }

    public TraceCaller begin() {
        TraceContext context = context();
        return TraceWrapper.getInstance().begin(context);
    }
}