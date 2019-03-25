package com.jd.journalq.common.network.command;

import com.google.common.collect.Table;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * FetchPartitionMessageAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchPartitionMessageAck extends JMQPayload {

    private Table<String, Short, FetchPartitionMessageAckData> data;

    @Override
    public int type() {
        return JMQCommandType.FETCH_PARTITION_MESSAGE_ACK.getCode();
    }

    public Table<String, Short, FetchPartitionMessageAckData> getData() {
        return data;
    }

    public void setData(Table<String, Short, FetchPartitionMessageAckData> data) {
        this.data = data;
    }
}