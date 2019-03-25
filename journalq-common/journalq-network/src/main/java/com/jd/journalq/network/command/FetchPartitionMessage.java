package com.jd.journalq.network.command;

import com.google.common.collect.Table;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * FetchPartitionMessage
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchPartitionMessage extends JMQPayload {

    public static final long NONE_INDEX = -1;

    private Table<String, Short, FetchPartitionMessageData> partitions;
    private String app;

    @Override
    public int type() {
        return JMQCommandType.FETCH_PARTITION_MESSAGE.getCode();
    }

    public void setPartitions(Table<String, Short, FetchPartitionMessageData> partitions) {
        this.partitions = partitions;
    }

    public Table<String, Short, FetchPartitionMessageData> getPartitions() {
        return partitions;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }
}