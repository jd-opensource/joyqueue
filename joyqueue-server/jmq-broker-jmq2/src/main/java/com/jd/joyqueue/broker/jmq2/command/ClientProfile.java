package com.jd.joyqueue.broker.jmq2.command;

import com.jd.joyqueue.broker.jmq2.network.JMQ2Payload;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * 客户端性能
 */
public class ClientProfile extends JMQ2Payload {
    private List<ClientTPStat> clientStats;

    public ClientProfile clientStat(List<ClientTPStat> clientStat) {
        setClientStats(clientStat);
        return this;
    }

    public List<ClientTPStat> getClientStats() {
        return clientStats;
    }

    public void setClientStats(List<ClientTPStat> clientStats) {
        this.clientStats = clientStats;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkArgument(clientStats != null, "client stat can not be null.");
    }

    @Override
    public int type() {
        return JMQ2CommandType.CLIENT_PROFILE.getCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientProfile{");
        sb.append("clientStats=").append(clientStats);
        sb.append('}');
        return sb.toString();
    }
}
