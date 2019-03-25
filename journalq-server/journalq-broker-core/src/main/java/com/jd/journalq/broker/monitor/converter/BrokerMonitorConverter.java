package com.jd.journalq.broker.monitor.converter;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.monitor.stat.ConnectionStat;
import com.jd.journalq.broker.monitor.stat.DeQueueStat;
import com.jd.journalq.broker.monitor.stat.EnQueueStat;
import com.jd.journalq.broker.monitor.stat.ReplicationStat;
import com.jd.journalq.monitor.Client;
import com.jd.journalq.monitor.ConnectionMonitorDetailInfo;
import com.jd.journalq.monitor.ConnectionMonitorInfo;
import com.jd.journalq.monitor.DeQueueMonitorInfo;
import com.jd.journalq.monitor.EnQueueMonitorInfo;
import com.jd.journalq.monitor.ReplicationMonitorInfo;

import java.util.List;
import java.util.Map;

/**
 * BrokerMonitorConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public class BrokerMonitorConverter {

    public static ReplicationMonitorInfo convertReplicationMonitorInfo(ReplicationStat replicationStat) {
        ReplicationMonitorInfo replicationMonitorInfo = new ReplicationMonitorInfo();
        replicationMonitorInfo.setTopic(replicationStat.getTopic());
        replicationMonitorInfo.setPartitionGroup(replicationStat.getPartitionGroup());
        replicationMonitorInfo.setReplicaStat(convertEnQueueMonitorInfo(replicationStat.getReplicaStat()));
        replicationMonitorInfo.setAppendStat(convertEnQueueMonitorInfo(replicationStat.getAppendStat()));
        return replicationMonitorInfo;
    }

    public static EnQueueMonitorInfo convertEnQueueMonitorInfo(EnQueueStat enQueueStat) {
        EnQueueMonitorInfo enQueueMonitorInfo = new EnQueueMonitorInfo();
        enQueueMonitorInfo.setCount(enQueueStat.getTotal());
        enQueueMonitorInfo.setOneMinuteRate(enQueueStat.getOneMinuteRate());
        enQueueMonitorInfo.setTp99(enQueueStat.getTp99());
        enQueueMonitorInfo.setTp90(enQueueStat.getTp90());
        enQueueMonitorInfo.setMax(enQueueStat.getMax());
        enQueueMonitorInfo.setMin(enQueueStat.getMin());
        enQueueMonitorInfo.setAvg(enQueueStat.getAvg());
        enQueueMonitorInfo.setSize(enQueueStat.getSize());
        enQueueMonitorInfo.setTotalSize(enQueueStat.getTotalSize());
        return enQueueMonitorInfo;
    }

    public static DeQueueMonitorInfo convertDeQueueMonitorInfo(DeQueueStat deQueueStat) {
        DeQueueMonitorInfo deQueueMonitorInfo = new DeQueueMonitorInfo();
        deQueueMonitorInfo.setCount(deQueueStat.getTotal());
        deQueueMonitorInfo.setOneMinuteRate(deQueueStat.getOneMinuteRate());
        deQueueMonitorInfo.setTp99(deQueueStat.getTp99());
        deQueueMonitorInfo.setTp90(deQueueStat.getTp90());
        deQueueMonitorInfo.setMax(deQueueStat.getMax());
        deQueueMonitorInfo.setMin(deQueueStat.getMin());
        deQueueMonitorInfo.setAvg(deQueueStat.getAvg());
        deQueueMonitorInfo.setSize(deQueueStat.getSize());
        deQueueMonitorInfo.setTotalSize(deQueueStat.getTotalSize());
        return deQueueMonitorInfo;
    }

    public static ConnectionMonitorInfo convertConnectionMonitorInfo(ConnectionStat connectionStat) {
        ConnectionMonitorInfo connectionMonitorInfo = new ConnectionMonitorInfo();
        connectionMonitorInfo.setTotal(connectionStat.getConnection());
        connectionMonitorInfo.setConsumer(connectionStat.getConsumer());
        connectionMonitorInfo.setProducer(connectionStat.getProducer());
        return connectionMonitorInfo;
    }

    public static ConnectionMonitorDetailInfo convertConnectionMonitorDetailInfo(ConnectionStat connectionStat) {
        ConnectionMonitorDetailInfo connectionMonitorDetailInfo = new ConnectionMonitorDetailInfo();
        connectionMonitorDetailInfo.setTotal(connectionStat.getConnection());
        connectionMonitorDetailInfo.setConsumer(connectionStat.getConsumer());
        connectionMonitorDetailInfo.setProducer(connectionStat.getProducer());
        connectionMonitorDetailInfo.setClients(Lists.newArrayListWithCapacity(connectionStat.getConnectionMap().size()));

        for (Map.Entry<String, Client> entry : connectionStat.getConnectionMap().entrySet()) {
            connectionMonitorDetailInfo.getClients().add(entry.getValue());
        }

        return connectionMonitorDetailInfo;
    }

    public static ConnectionMonitorDetailInfo convertConnectionMonitorDetailInfo(ConnectionStat connectionStat, List<Client> clients) {
        ConnectionMonitorDetailInfo connectionMonitorDetailInfo = new ConnectionMonitorDetailInfo();
        connectionMonitorDetailInfo.setTotal(connectionStat.getConnection());
        connectionMonitorDetailInfo.setConsumer(connectionStat.getConsumer());
        connectionMonitorDetailInfo.setProducer(connectionStat.getProducer());
        connectionMonitorDetailInfo.setClients(clients);
        return connectionMonitorDetailInfo;
    }
}