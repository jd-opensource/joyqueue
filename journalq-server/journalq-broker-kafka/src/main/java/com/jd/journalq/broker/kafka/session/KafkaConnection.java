package com.jd.journalq.broker.kafka.session;

import com.jd.journalq.common.network.session.Connection;

/**
 * kafka连接
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/7/5
 */
public class KafkaConnection extends Connection {

    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}