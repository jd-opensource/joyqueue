package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class Register extends JoyQueuePayload {
    private Integer brokerId;
    private String brokerIp;
    private Integer port;

    public Register brokerId(Integer brokerId){
        this.brokerId = brokerId;
        return this;
    }
    public Register brokerIp(String brokerIp){
        this.brokerIp = brokerIp;
        return this;
    }
    public Register port(Integer port){
        this.port = port;
        return this;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public String getBrokerIp() {
        return brokerIp;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public int type() {
        return NsrCommandType.REGISTER;
    }

    @Override
    public String toString() {
        return "Register{" +
                "brokerId=" + brokerId +
                ", brokerIp='" + brokerIp + '\'' +
                ", port=" + port +
                '}';
    }
}
