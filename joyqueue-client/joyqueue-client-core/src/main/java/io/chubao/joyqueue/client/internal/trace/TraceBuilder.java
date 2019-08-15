package io.chubao.joyqueue.client.internal.trace;

import io.chubao.joyqueue.client.internal.trace.support.TraceWrapper;
import io.chubao.joyqueue.toolkit.time.SystemClock;

/**
 * TraceBuilder
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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