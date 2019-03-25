package com.jd.journalq.broker.monitor;

/**
 * 消费监控
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public interface ConsumerMonitor {

    /**
     * 消费消息
     * @param topic
     * @param app
     * @param partitionGroup
     * @param partition
     * @param count
     * @param size
     * @param time
     */
    void onGetMessage(String topic, String app, int partitionGroup, short partition, long count, long size, long time);

    /**
     * 重试消息
     * @param topic
     * @param app
     * @param count
     * @param time
     */
    void onGetRetry(String topic, String app, long count, long time);

    /**
     * 添加重试
     * @param topic
     * @param app
     * @param count
     * @param time
     */
    void onAddRetry(String topic, String app, long count, long time);

    /**
     * 重试成功
     * @param topic
     * @param app
     * @param count
     */
    void onRetrySuccess(String topic, String app, long count);

    /**
     * 重试失败
     * @param topic
     * @param app
     * @param count
     */
    void onRetryFailure(String topic, String app, long count);
}