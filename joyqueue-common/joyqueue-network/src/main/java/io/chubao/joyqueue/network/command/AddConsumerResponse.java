package io.chubao.joyqueue.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * AddConsumerResponse
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddConsumerResponse extends JoyQueuePayload {

    private Map<String, String> consumerIds;

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_CONSUMER_RESPONSE.getCode();
    }

    public void setConsumerIds(Map<String, String> consumerIds) {
        this.consumerIds = consumerIds;
    }

    public Map<String, String> getConsumerIds() {
        return consumerIds;
    }
}