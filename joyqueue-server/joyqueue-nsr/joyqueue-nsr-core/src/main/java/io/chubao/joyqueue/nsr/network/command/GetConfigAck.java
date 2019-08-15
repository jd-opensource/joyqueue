package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConfigAck extends JoyQueuePayload {
    private String value;
    public GetConfigAck value(String value){
        this.value = value;
        return this;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONFIG_ACK;
    }
}
