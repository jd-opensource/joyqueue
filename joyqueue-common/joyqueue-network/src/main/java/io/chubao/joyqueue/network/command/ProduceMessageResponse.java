package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * ProduceMessageResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageResponse extends JoyQueuePayload {

    private Map<String, ProduceMessageAckData> data;

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_RESPONSE.getCode();
    }

    public void setData(Map<String, ProduceMessageAckData> data) {
        this.data = data;
    }

    public Map<String, ProduceMessageAckData> getData() {
        return data;
    }
}