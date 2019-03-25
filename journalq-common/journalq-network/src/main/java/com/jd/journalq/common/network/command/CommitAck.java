package com.jd.journalq.common.network.command;

import com.google.common.collect.Table;
import com.jd.journalq.common.network.transport.command.JMQPayload;

import java.util.List;

/**
 * CommitAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAck extends JMQPayload {

    private Table<String, Short, List<CommitAckData>> data;
    private String app;

    @Override
    public int type() {
        return JMQCommandType.COMMIT_ACK.getCode();
    }

    public void setData(Table<String, Short, List<CommitAckData>> data) {
        this.data = data;
    }

    public Table<String, Short, List<CommitAckData>> getData() {
        return data;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }
}