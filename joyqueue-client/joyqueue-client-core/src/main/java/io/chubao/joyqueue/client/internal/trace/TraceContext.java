package io.chubao.joyqueue.client.internal.trace;

import io.chubao.joyqueue.toolkit.time.SystemClock;

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