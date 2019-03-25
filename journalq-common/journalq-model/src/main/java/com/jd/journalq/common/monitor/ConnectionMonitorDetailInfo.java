package com.jd.journalq.common.monitor;

import java.util.List;

/**
 * 连接明细
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public class ConnectionMonitorDetailInfo extends BaseMonitorInfo {

    private int consumer;
    private int producer;
    private int total;
    private List<Client> clients;

    public int getConsumer() {
        return consumer;
    }

    public void setConsumer(int consumer) {
        this.consumer = consumer;
    }

    public int getProducer() {
        return producer;
    }

    public void setProducer(int producer) {
        this.producer = producer;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }
}