package com.jd.journalq.broker.monitor.stat;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.monitor.metrics.Metrics;
import com.jd.journalq.monitor.Client;

import java.util.concurrent.ConcurrentMap;

/**
 * connectionStat
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/11
 */
public class ConnectionStat {

    private Metrics consumer = new Metrics();
    private Metrics producer = new Metrics();
    private ConcurrentMap<String /** connectionId **/, Client> connectionMap = Maps.newConcurrentMap();

    public void incrConsumer() {
        this.incrConsumer(1);
    }

    public void incrConsumer(int count) {
        this.consumer.mark(count);
    }

    public void decrConsumer() {
        this.decrConsumer(1);
    }

    public void decrConsumer(int count) {
        this.consumer.mark(-count);
    }

    public void incrProducer() {
        this.incrProducer(1);
    }

    public void incrProducer(int count) {
        this.producer.mark(count);
    }

    public void decrProducer() {
        this.decrProducer(1);
    }

    public void decrProducer(int count) {
        this.producer.mark(-count);
    }

    public int getConsumer() {
        return (int) this.consumer.getCount();
    }

    public int getProducer() {
        return (int) this.producer.getCount();
    }

    public int getConnection() {
        return this.connectionMap.size();
    }

    public Client getConnection(String connectionId) {
        return connectionMap.get(connectionId);
    }

    public boolean addConnection(Client client) {
        return connectionMap.putIfAbsent(client.getConnectionId(), client) == null;
    }

    public boolean removeConnection(String connectionId) {
        return connectionMap.remove(connectionId) != null;
    }

    public ConcurrentMap<String, Client> getConnectionMap() {
        return connectionMap;
    }
}