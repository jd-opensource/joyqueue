package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetBrokerByRetryType extends JoyQueuePayload {

    private String retryType;
    public GetBrokerByRetryType retryType(String retryType){
        this.retryType = retryType;
        return this;
    }

    public String getRetryType() {
        return retryType;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER_BY_RETRYTYPE;
    }
}
