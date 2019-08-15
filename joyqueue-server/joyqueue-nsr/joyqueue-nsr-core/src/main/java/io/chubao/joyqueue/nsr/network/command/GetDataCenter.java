package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/28
 */
public class GetDataCenter extends JoyQueuePayload {
    private String ip;

    public GetDataCenter ip(String ip){
        this.ip = ip;
        return this;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_DATACENTER;
    }

    @Override
    public String toString() {
        return "GetDataCenter{" +
                "ip='" + ip + '\'' +
                '}';
    }
}
