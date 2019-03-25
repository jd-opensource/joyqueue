package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.List;
import java.util.Map;

/**
 * FetchIndex
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndex extends JMQPayload {

    private Map<String, List<Short>> partitions;
    private String app;

    @Override
    public int type() {
        return JMQCommandType.FETCH_INDEX.getCode();
    }

    public void setPartitions(Map<String, List<Short>> partitions) {
        this.partitions = partitions;
    }

    public Map<String, List<Short>> getPartitions() {
        return partitions;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }
}