package com.jd.journalq.network.command;

import com.google.common.collect.Table;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * FetchIndexResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndexResponse extends JMQPayload {

    private Table<String, Short, FetchIndexAckData> data;

    @Override
    public int type() {
        return JMQCommandType.FETCH_INDEX_RESPONSE.getCode();
    }

    public void setData(Table<String, Short, FetchIndexAckData> data) {
        this.data = data;
    }

    public Table<String, Short, FetchIndexAckData> getData() {
        return data;
    }
}