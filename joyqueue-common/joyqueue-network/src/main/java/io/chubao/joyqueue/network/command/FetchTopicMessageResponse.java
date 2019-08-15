package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * FetchTopicMessageResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public class FetchTopicMessageResponse extends JoyQueuePayload {

    private Map<String, FetchTopicMessageAckData> data;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_TOPIC_MESSAGE_RESPONSE.getCode();
    }

    public void setData(Map<String, FetchTopicMessageAckData> data) {
        this.data = data;
    }

    public Map<String, FetchTopicMessageAckData> getData() {
        return data;
    }
}