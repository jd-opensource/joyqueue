package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * ProduceMessageRequest
 *
 * author: gaohaoxiang
 * date: 2018/12/18
 */
public class ProduceMessageRequest extends JoyQueuePayload {

    private String app;
    private Map<String, ProduceMessageData> data;

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_REQUEST.getCode();
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