package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.List;

/**
 * FetchAssignedPartitionRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class FetchAssignedPartitionRequest extends JMQPayload {

    private List<FetchAssignedPartitionData> data;
    private String app;

    @Override
    public int type() {
        return JMQCommandType.FETCH_ASSIGNED_PARTITION.getCode();
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setData(List<FetchAssignedPartitionData> data) {
        this.data = data;
    }

    public List<FetchAssignedPartitionData> getData() {
        return data;
    }
}