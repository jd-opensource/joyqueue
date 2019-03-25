package com.jd.journalq.client.internal.producer.feedback.config;

/**
 * TxFeedbackConfig
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public class TxFeedbackConfig {

    private String app;
    private long timeout = 1000 * 10;
    private long longPollTimeout = 1000 * 3;

    private int fetchInterval = 1000 * 5;
    private int fetchSize = 1;

    public TxFeedbackConfig copy() {
        TxFeedbackConfig txFeedbackConfig = new TxFeedbackConfig();
        txFeedbackConfig.setApp(app);
        txFeedbackConfig.setTimeout(timeout);
        txFeedbackConfig.setLongPollTimeout(longPollTimeout);
        txFeedbackConfig.setFetchInterval(fetchInterval);
        txFeedbackConfig.setFetchSize(fetchSize);
        return txFeedbackConfig;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setLongPollTimeout(long longPollTimeout) {
        this.longPollTimeout = longPollTimeout;
    }

    public long getLongPollTimeout() {
        return longPollTimeout;
    }

    public int getFetchInterval() {
        return fetchInterval;
    }

    public void setFetchInterval(int fetchInterval) {
        this.fetchInterval = fetchInterval;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
}