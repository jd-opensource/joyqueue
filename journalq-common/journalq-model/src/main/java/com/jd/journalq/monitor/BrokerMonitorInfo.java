package com.jd.journalq.monitor;

/**
 * broker信息
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/11
 */
public class BrokerMonitorInfo extends BaseMonitorInfo {

    private ConnectionMonitorInfo connection;
    private DeQueueMonitorInfo deQueue;
    private EnQueueMonitorInfo enQueue;
    private ReplicationMonitorInfo replication;
    private StoreMonitorInfo store;
    private NameServerMonitorInfo nameServer;
    private ElectionMonitorInfo election;

    public ConnectionMonitorInfo getConnection() {
        return connection;
    }

    public void setConnection(ConnectionMonitorInfo connection) {
        this.connection = connection;
    }

    public DeQueueMonitorInfo getDeQueue() {
        return deQueue;
    }

    public void setDeQueue(DeQueueMonitorInfo deQueue) {
        this.deQueue = deQueue;
    }

    public EnQueueMonitorInfo getEnQueue() {
        return enQueue;
    }

    public void setEnQueue(EnQueueMonitorInfo enQueue) {
        this.enQueue = enQueue;
    }

    public void setReplication(ReplicationMonitorInfo replication) {
        this.replication = replication;
    }

    public ReplicationMonitorInfo getReplication() {
        return replication;
    }

    public void setStore(StoreMonitorInfo store) {
        this.store = store;
    }

    public StoreMonitorInfo getStore() {
        return store;
    }

    public void setNameServer(NameServerMonitorInfo nameServer) {
        this.nameServer = nameServer;
    }

    public NameServerMonitorInfo getNameServer() {
        return nameServer;
    }

    public void setElection(ElectionMonitorInfo election) {
        this.election = election;
    }

    public ElectionMonitorInfo getElection() {
        return election;
    }
}