package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfigByBroker extends JoyQueuePayload {
    private int brokerId;
    public GetTopicConfigByBroker brokerId(int brokerId){
        this.brokerId = brokerId;
        return this;
    }

    public int getBrokerId() {
        return brokerId;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIGS_BY_BROKER;
    }

    @Override
    public String toString() {
        return "GetTopicConfigByBroker{" +
                "brokerId=" + brokerId +
                '}';
    }
}
