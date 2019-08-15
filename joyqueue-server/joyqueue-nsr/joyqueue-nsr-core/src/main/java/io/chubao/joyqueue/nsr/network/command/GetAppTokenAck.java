package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.AppToken;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/13
 */
public class GetAppTokenAck extends JoyQueuePayload {
    private AppToken appToken;

    public GetAppTokenAck appToken(AppToken appToken){
        this.appToken = appToken;
        return this;
    }

    public AppToken getAppToken() {
        return appToken;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_APP_TOKEN_ACK;
    }

    @Override
    public String toString() {
        return "GetAppTokenAck{" +
                "appToken=" + appToken +
                '}';
    }
}
