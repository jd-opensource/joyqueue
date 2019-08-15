package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class HasSubscribeAck extends JoyQueuePayload {

    private boolean have;

    public HasSubscribeAck have(boolean have){
        this.have = have;
        return this;
    }

    public boolean isHave() {
        return have;
    }

    @Override
    public int type() {
        return NsrCommandType.HAS_SUBSCRIBE_ACK;
    }
}
