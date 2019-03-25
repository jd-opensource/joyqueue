package com.jd.journalq.common.network.command;

import com.google.common.collect.Table;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * FetchIndexAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndexAck extends JMQPayload {

    private Table<String, Short, FetchIndexAckData> data;

    @Override
    public int type() {
        return JMQCommandType.FETCH_INDEX_ACK.getCode();
    }

    public void setData(Table<String, Short, FetchIndexAckData> data) {
        this.data = data;
    }

    public Table<String, Short, FetchIndexAckData> getData() {
        return data;
    }
}