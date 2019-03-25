package com.jd.journalq.broker.monitor;

/**
 * ReplicationMonitor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/16
 */
public interface ReplicationMonitor {

    /**
     * 复制消息
     * @param topic
     * @param partitionGroup
     * @param count
     * @param size
     * @param time
     */
    void onReplicateMessage(String topic, int partitionGroup, long count, long size, long time);

    /**
     * 写入复制消息
     * @param topic
     * @param partitionGroup
     * @param count
     * @param size
     * @param time
     */
    void onAppendReplicateMessage(String topic, int partitionGroup, long count, long size, long time);
}