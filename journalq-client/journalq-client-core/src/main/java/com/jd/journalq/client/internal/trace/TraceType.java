package com.jd.journalq.client.internal.trace;

/**
 * TraceType
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public enum TraceType {

    PRODUCER_SEND(0),

    CONSUMER_CONSUME(1),

    CONSUMER_FETCH(2),

    ;

    private int type;
    private boolean enable;

    private TraceType(int type) {
        this(type, true);
    }

    private TraceType(int type, boolean enable) {
        this.type = type;
        this.enable = enable;
    }

    public int getType() {
        return type;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}