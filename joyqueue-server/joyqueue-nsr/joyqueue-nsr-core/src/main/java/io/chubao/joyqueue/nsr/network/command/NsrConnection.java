package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/3/15
 */
public class NsrConnection extends JoyQueuePayload {
    private Integer brokerId;
    public NsrConnection brokerId(Integer brokerId){
        this.brokerId = brokerId;
        return this;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    @Override
    public int type() {
        return NsrCommandType.CONNECT;
    }
}
