package com.jd.journalq.common.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * ProduceMessage
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessage extends JMQPayload {

    private String app;
    private Map<String, ProduceMessageData> data;

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE.getCode();
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setData(Map<String, ProduceMessageData> data) {
        this.data = data;
    }

    public Map<String, ProduceMessageData> getData() {
        return data;
    }
}