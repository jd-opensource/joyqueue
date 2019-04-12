package com.jd.journalq.network.session;

import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.lang3.StringUtils;

import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 *
 * @author lining11
 * Date: 2018/8/23
 */
public class TransactionId {

    private String topic;
    private String app;
    private String txId;
    private String queryId;
    private int storeId;
    private long timeout;
    private long startTime;
    private String producerId;
    private byte source;

    private AtomicLong lastQueryTimestamp = new AtomicLong();

    public TransactionId(String txId) {
        if (txId == null || txId.isEmpty()) {
            throw new IllegalArgumentException("transactionId can not be empty");
        }
        String[] parts = new String[]{null, null, null, null, null, null, null};
        int index = 0;
        StringTokenizer tokenizer = new StringTokenizer(txId, "-");
        while (tokenizer.hasMoreTokens()) {
            parts[index++] = tokenizer.nextToken();
            if (index >= parts.length) {
                break;
            }
        }
        if (index < parts.length) {
            throw new IllegalArgumentException("transactionId is invalid.");
        }
        setup(new ProducerId(parts), Integer.parseInt(parts[parts.length - 1]));
    }

    /**
     * 初始化
     *
     * @param producerId 生产者ID
     * @param sequence   序号
     */
    protected void setup(final ProducerId producerId, final int sequence) {
        if (producerId == null || sequence < 0) {
            throw new IllegalArgumentException("producerId must not be null");
        }

        this.producerId = producerId.getProducerId();
        this.storeId = sequence;
        // 在构造函数中创建，防止延迟加载并发问题
        this.txId = producerId.getProducerId() + "-" + sequence;
    }

    public TransactionId(String topic, String app, String txId, String queryId, int storeId, byte source, long timeout, long startTime) {
        this.topic = topic;
        this.app = app;
        this.txId = txId;
        this.queryId = queryId;
        this.storeId = storeId;
        this.source = source;
        this.timeout = timeout;
        this.startTime = startTime;
    }

    public boolean isFeedback() {
        return StringUtils.isNotBlank(queryId);
    }

    public boolean isTimeout() {
        return (SystemClock.now() > (startTime + timeout));
    }

    public boolean isExpired(long timeout) {
        return (SystemClock.now() > (startTime + timeout));
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public String getTxId() {
        return txId;
    }

    public String getQueryId() {
        return queryId;
    }

    public int getStoreId() {
        return storeId;
    }

    public long getTimeout() {
        return timeout;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getProducerId() {
        return producerId;
    }

    public long getLastQueryTimestamp() {
        return lastQueryTimestamp.get();
    }

    public boolean setLastQueryTimestamp(long oldLastQueryTimestamp, long lastQueryTimestamp) {
        return this.lastQueryTimestamp.compareAndSet(oldLastQueryTimestamp, lastQueryTimestamp);
    }

    public void setSource(byte source) {
        this.source = source;
    }

    public byte getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "TransactionId{" +
                "topic='" + topic + '\'' +
                ", app='" + app + '\'' +
                ", txId='" + txId + '\'' +
                ", queryId='" + queryId + '\'' +
                ", storeId=" + storeId +
                ", source=" + source +
                '}';
    }
}