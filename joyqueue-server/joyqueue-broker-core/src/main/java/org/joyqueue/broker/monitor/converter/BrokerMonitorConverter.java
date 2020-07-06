/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.monitor.converter;

import com.google.common.collect.Lists;
import org.joyqueue.broker.monitor.stat.ConnectionStat;
import org.joyqueue.broker.monitor.stat.DeQueueStat;
import org.joyqueue.broker.monitor.stat.EnQueueStat;
import org.joyqueue.broker.monitor.stat.ReplicationStat;
import org.joyqueue.monitor.Client;
import org.joyqueue.monitor.ConnectionMonitorDetailInfo;
import org.joyqueue.monitor.ConnectionMonitorInfo;
import org.joyqueue.monitor.DeQueueMonitorInfo;
import org.joyqueue.monitor.EnQueueMonitorInfo;
import org.joyqueue.monitor.ReplicationMonitorInfo;

import java.util.List;
import java.util.Map;

/**
 * BrokerMonitorConverter
 *
 * author: gaohaoxiang
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
        enQueueMonitorInfo.setTps(enQueueStat.getTps());
        enQueueMonitorInfo.setTraffic(enQueueStat.getTraffic());
        enQueueMonitorInfo.setTotalTraffic(enQueueStat.getTotalTraffic());
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
        deQueueMonitorInfo.setTps(deQueueStat.getTps());
        deQueueMonitorInfo.setTraffic(deQueueStat.getTraffic());
        deQueueMonitorInfo.setTotalTraffic(deQueueStat.getTotalTraffic());
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