package com.jd.journalq.broker.polling;

import com.jd.journalq.common.network.session.Consumer;
import com.jd.journalq.toolkit.time.SystemClock;

/**
 * Created by chengzhiliang on 2018/9/5.
 */
public class LongPolling {
    private Consumer consumer;
    private int count;
    private int ackTimeout;
    private long longPollingTimeout;
    // 长轮询回调
    private LongPollingCallback longPollingCallback;
    // 过期时间
    private long expire;

    public LongPolling(Consumer consumer, int count, int ackTimeout, long longPollingTimeout, LongPollingCallback longPollingCallback) {
        this.consumer = consumer;
        this.count = count;
        this.ackTimeout = ackTimeout;
        this.longPollingTimeout = longPollingTimeout;
        this.longPollingCallback = longPollingCallback;
        this.expire = SystemClock.now() + longPollingTimeout;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public void setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public long getLongPollingTimeout() {
        return longPollingTimeout;
    }

    public void setLongPollingTimeout(long longPollingTimeout) {
        this.longPollingTimeout = longPollingTimeout;
    }

    public LongPollingCallback getLongPollingCallback() {
        return longPollingCallback;
    }

    public void setLongPollingCallback(LongPollingCallback longPollingCallback) {
        this.longPollingCallback = longPollingCallback;
    }

    public long getExpire() {
        return expire;
    }
}
