package com.jd.journalq.model.domain;

import com.jd.journalq.common.monitor.Client;

public class BrokerClient {

    /**
     * broker ip
     **/
    private String  ip;
    private Client client;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
