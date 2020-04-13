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

import com.codahale.metrics.Snapshot;
import com.google.common.collect.Lists;
import org.joyqueue.broker.monitor.PendingStat;
import org.joyqueue.broker.monitor.stat.*;
import org.joyqueue.model.MonitorRecord;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import org.joyqueue.toolkit.vm.MemoryStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lining11
 * Date: 2019/1/9
 */
public class DefaultConverter implements Converter<BrokerStatExt, List<MonitorRecord>> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConverter.class);
    //broker heap state
    private static final String BROKER_SLICE_HEAP = "broker_slice_heap";
    private static final String BROKER_SLICE_HEAP_INIT = "broker_slice_heap_init";
    private static final String BROKER_SLICE_HEAP_USED = "broker_slice_heap_used";
    private static final String BROKER_SLICE_HEAP_COMMITTED = "broker_slice_heap_committed";
    private static final String BROKER_SLICE_HEAP_MAX = "broker_slice_heap_max";

    private static final String BROKER_SLICE_NON_HEAP = "broker_slice_non_heap";
    private static final String BROKER_SLICE_NON_HEAP_INIT = "broker_slice_non_heap_init";
    private static final String BROKER_SLICE_NON_HEAP_USED = "broker_slice_non_heap_used";
    private static final String BROKER_SLICE_NON_HEAP_COMMITTED = "broker_slice_non_heap_committed";
    private static final String BROKER_SLICE_NON_HEAP_MAX = "broker_slice_non_heap_max";

    // young gc state
    private static final String BROKER_SLICE_YOUNG_GC_TIMES = "broker_slice_young_gc_times";
    private static final String BROKER_SLICE_YOUNG_GC_ELAPSED = "broker_slice_young_gc_elapsed";

    // old gc state
    private static final String BROKER_SLICE_OLD_GC_TIMES = "broker_slice_old_gc_times";
    private static final String BROKER_SLICE_OLD_GC_ELAPSED = "broker_slice_old_gc_elapsed";


    private static final String BROKER_SLICE_DIRECT_BUFFER_SIZE = "broker_slice_direct_buffer_size";


    private static final String BROKER_SLICE_STORAGE = "broker_slice_storage";

    private static final String BROKER_SLICE_ENQUEUE = "broker_slice_enqueue";
    private static final String BROKER_SLICE_ENQUEUE_SIZE = "broker_slice_enqueue_size";
    private static final String BROKER_SLICE_ENQUEUE_TP99 = "broker_slice_enqueue_tp99";
    private static final String BROKER_SLICE_ENQUEUE_TP90 = "broker_slice_enqueue_tp90";

    private static final String BROKER_SLICE_DEQUEUE = "broker_slice_dequeue";
    private static final String BROKER_SLICE_DEQUEUE_SIZE = "broker_slice_dequeue_size";
    private static final String BROKER_SLICE_DEQUEUE_TP90 = "broker_slice_dequeue_tp90";
    private static final String BROKER_CONNECTION = "broker_connection";

    private static final String TOPIC_SLICE_ENQUEUE = "topic_slice_enqueue";
    private static final String TOPIC_SLICE_ENQUEUE_SIZE = "topic_slice_enqueue_size";
    private static final String TOPIC_SLICE_ENQUEUE_TP99 = "topic_slice_enqueue_tp99";
    private static final String TOPIC_SLICE_ENQUEUE_TP90 = "topic_slice_enqueue_tp90";
    private static final String TOPIC_SLICE_ENQUEUE_MAX = "topic_slice_enqueue_max";
    private static final String TOPIC_SLICE_ENQUEUE_MIN = "topic_slice_enqueue_min";
    private static final String TOPIC_SLICE_ENQUEUE_AVG = "topic_slice_enqueue_avg";

    private static final String TOPIC_SLICE_DEQUEUE = "topic_slice_dequeue";
    private static final String TOPIC_SLICE_DEQUEUE_SIZE = "topic_slice_dequeue_size";
    private static final String TOPIC_SLICE_DEQUEUE_TP99 = "topic_slice_dequeue_tp99";
    private static final String TOPIC_SLICE_DEQUEUE_TP90 = "topic_slice_dequeue_tp90";
    private static final String TOPIC_SLICE_DEQUEUE_MAX = "topic_slice_dequeue_max";
    private static final String TOPIC_SLICE_DEQUEUE_MIN = "topic_slice_dequeue_min";
    private static final String TOPIC_SLICE_DEQUEUE_AVG = "topic_slice_dequeue_avg";
    private static final String TOPIC_SLICE_STORAGE_SIZE = "topic_slice_storage_size";

    private static final String APP_SLICE_DEQUEUE = "app_slice_dequeue";
    private static final String APP_SLICE_DEQUEUE_SIZE = "app_slice_dequeue_size";
    private static final String APP_SLICE_DEQUEUE_CONNECTION = "app_slice_dequeue_connection";
    private static final String APP_SLICE_DEQUEUE_TP99 = "app_slice_dequeue_tp99";
    private static final String APP_SLICE_DEQUEUE_TP90 = "app_slice_dequeue_tp90";
    private static final String APP_SLICE_DEQUEUE_MAX = "app_slice_dequeue_max";
    private static final String APP_SLICE_DEQUEUE_MIN = "app_slice_dequeue_min";
    private static final String APP_SLICE_DEQUEUE_AVG = "app_slice_dequeue_avg";

    private static final String APP_SLICE_ENQUEUE = "app_slice_enqueue";
    private static final String APP_SLICE_ENQUEUE_CONNECTION = "app_slice_enqueue_connection";
    private static final String APP_SLICE_ENQUEUE_SIZE = "app_slice_enqueue_size";
    private static final String APP_SLICE_ENQUEUE_TP99 = "app_slice_enqueue_tp99";
    private static final String APP_SLICE_ENQUEUE_TP90 = "app_slice_enqueue_tp90";
    private static final String APP_SLICE_ENQUEUE_MAX = "app_slice_enqueue_max";
    private static final String APP_SLICE_ENQUEUE_MIN = "app_slice_enqueue_min";
    private static final String APP_SLICE_ENQUEUE_AVG = "app_slice_enqueue_avg";

    private static final String PG_SLICE_DEQUEUE = "pg_slice_dequeue";
    private static final String PG_SLICE_DEQUEUE_SIZE = "pg_slice_dequeue_size";
    private static final String PG_SLICE_DEQUEUE_TP99 = "pg_slice_dequeue_tp99";
    private static final String PG_SLICE_DEQUEUE_TP90 = "pg_slice_dequeue_tp90";
    private static final String PG_SLICE_DEQUEUE_MAX = "pg_slice_dequeue_max";
    private static final String PG_SLICE_DEQUEUE_MIN = "pg_slice_dequeue_min";
    private static final String PG_SLICE_DEQUEUE_AVG = "pg_slice_dequeue_avg";

    private static final String PG_REPLICATE = "pg_replicate";
    private static final String PG_REPLICATE_TP99 = "pg_replicate_tp99";
    private static final String PG_REPLICATE_TP90 = "pg_replicate_tp90";
    private static final String PG_REPLICATE_MAX = "pg_replicate_max";
    private static final String PG_REPLICATE_AVG = "pg_replicate_avg";

    private static final String PG_RECEIVE = "pg_receive";
    private static final String PG_RECEIVE_TP99 = "pg_receive_tp99";
    private static final String PG_RECEIVE_TP90 = "pg_receive_tp90";
    private static final String PG_RECEIVE_MAX = "pg_receive_max";
    private static final String PG_RECEIVE_AVG = "pg_receive_avg";

    private static final String PG_SLICE_ENQUEUE = "pg_slice_enqueue";
    private static final String PG_SLICE_ENQUEUE_SIZE = "pg_slice_enqueue_size";
    private static final String PG_SLICE_ENQUEUE_TP99 = "pg_slice_enqueue_tp99";
    private static final String PG_SLICE_ENQUEUE_TP90 = "pg_slice_enqueue_tp90";
    private static final String PG_SLICE_ENQUEUE_MAX = "pg_slice_enqueue_max";
    private static final String PG_SLICE_ENQUEUE_MIN = "pg_slice_enqueue_min";
    private static final String PG_SLICE_ENQUEUE_AVG = "pg_slice_enqueue_avg";

    private static final String PG_SLICE_REPLICA = "pg_slice_replica";
    private static final String PG_SLICE_REPLICA_LOG_MAX_POSITION = "pg_slice_replica_log_max_position";


    // replica state change serials
    private static final String PG_SLICE_REPLICA_STAT = "pg_slice_replica_state";

    private static final String PG_SLICE_ELECTION = "pg_slice_election";
    // term and type
    private static final String PG_SLICE_ELECTION_TERM = "pg_slice_election_term";
    private static final String PG_SLICE_ELECTION_TYPE = "pg_slice_election_type";


    private static final String TOPIC_PENDING = "topic_pending";
    private static final String APP_PENDING = "app_pending";
    private static final String PG_PENDING = "pg_pending";
    private static final String PARTITION_PENDING = "partition_pending";

    private static final String PARTITION_RIGHT = "partition_right";
    private static final String PARTITION_ACK_INDEX = "partition_ack_index";

    private static final String PRODUCE_ARCHIVE_PENDING = "produce_archive_pending";
    private static final String CONSUME_ARCHIVE_PENDING = "consume_archive_pending";
    private static final String TOPIC_PRODUCE_ARCHIVE_PENDING = "topic_produce_archive_pending";

    private boolean broker = true;

    private boolean topic = true;

    private boolean app = true;

    private boolean partitionGroup = true;

    private boolean pending = true;

    private boolean replicate = true;

    private boolean receive = true;

    private boolean archive = true;
    private boolean partitionStat = true;


    @Override
    public List<MonitorRecord> convert(BrokerStatExt brokerStatExt) {

        BrokerStat brokerStat = brokerStatExt.getBrokerStat();
        String brokerId = "";
        if (brokerStatExt.getBrokerId() != null) {
            brokerId = brokerStatExt.getBrokerId().toString();
        }
        Map<String, TopicPendingStat> topicPending = brokerStatExt.getTopicPendingStatMap();
        List<MonitorRecord> result = new ArrayList<>();
        long time = brokerStatExt.getTimeStamp();
        if (time <= 0) {
            time = SystemClock.now() / 1000;
        }
        if (broker) {
            result.addAll(buildBrokerRecord(brokerStat, brokerId, time));
        }

        if (topic) {
            result.addAll(buildTopicRecord(brokerStat, brokerId, time));
        }

        if (app) {
            result.addAll(buildBrokerTopicApp(brokerStat, brokerId, time));
        }

        if (partitionGroup) {
            result.addAll(buildPartitionGroup(brokerStat, brokerId, time));
        }

        if (pending) {
            result.addAll(buildPending(brokerId, time, topicPending));
        }

        if (partitionStat) {
            result.addAll(buildPartitionRecord(brokerId, time, topicPending));
        }

        if (replicate) {
            result.addAll(buildReplicate(brokerId, time, brokerStat));
        }

        if (receive) {
            result.addAll(buildReceive(brokerId, time, brokerStat));
        }

        if (archive) {
            result.addAll(buildArchive(brokerId, time, brokerStatExt));
        }
        return result;
    }

    private Collection<? extends MonitorRecord> buildPartitionRecord(String brokerId, long time, Map<String, TopicPendingStat> topicPending) {
        List<MonitorRecord> records = new ArrayList<>();

        for (String topic :topicPending.keySet()){
            TopicPendingStat tps = topicPending.get(topic);
            Map<String, ConsumerPendingStat> appMap = tps.getPendingStatSubMap();
            for (String app:appMap.keySet()){
                ConsumerPendingStat pendingStat = appMap.get(app);
                Map<Integer, PartitionGroupPendingStat> pgStatMap = pendingStat.getPendingStatSubMap();
                for (Integer pg:pgStatMap.keySet()){
                    PartitionGroupPendingStat pgStat = pgStatMap.get(pg);
                    Map<Short, PartitionStat> partitionMap = pgStat.getPartitionStatHashMap();
                    for (Short partition:partitionMap.keySet()){
                        MonitorRecord record = getPartitionRecord(brokerId,time,topic,app,pg,partition);
                        record.setValue(partitionMap.get(partition).getAckIndex());
                        record.setMetric(PARTITION_ACK_INDEX);
                        fillRecord(record,time);
                        records.add(record);
                        record = getPartitionRecord(brokerId,time,topic,app,pg,partition);
                        fillRecord(record,time);
                        record.setMetric(PARTITION_RIGHT);
                        record.setValue(partitionMap.get(partition).getRight());
                        records.add(record);

                    }
                }
            }

        }

        return records;

    }

    private MonitorRecord getPartitionRecord(String brokerId, long time, String topic, String app, Integer pg, Short partition) {
        MonitorRecord record = new MonitorRecord();
        record.topic(topic);
        record.app(app);
        record.partitionGroup(pg + "");
        record.partition(partition + "");
        record.setTimestamp(time);
        record.brokerId(brokerId);
        return record;
    }

    private List<MonitorRecord> buildArchive(String brokerId, long time, BrokerStatExt brokerStat) {
        List<MonitorRecord> records = new ArrayList<>();
        String ip = IpUtil.getLocalIp();

        try {

            records.add(buildMonitorRecord(CONSUME_ARCHIVE_PENDING, "", "", brokerStat.getArchiveConsumePending(), brokerId, time, ip));
            if (logger.isDebugEnabled()) {
                logger.debug("Archive CONSUME_ARCHIVE_PENDING");
            }
        } catch (Exception e) {
            logger.error("Archive build CONSUME_ARCHIVE_PENDING error!", e);
        }
        try {
            records.add(buildMonitorRecord(PRODUCE_ARCHIVE_PENDING, "", "", brokerStat.getArchiveProducePending(), brokerId, time, ip));
            if (logger.isDebugEnabled()) {
                logger.debug("Archive PRODUCE_ARCHIVE_PENDING");
            }
        } catch (Exception e) {
            logger.error("Archive build PRODUCE_ARCHIVE_PENDING error!", e);
        }

        try {
            Map<String, Long> topicArchivePending = brokerStat.getTopicArchiveProducePending();

            for (String topic : topicArchivePending.keySet()) {
                records.add(buildMonitorRecord(TOPIC_PRODUCE_ARCHIVE_PENDING, topic, "", topicArchivePending.get(topic), brokerId, time, ip));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Archive total size {}", records.size());
            }
        } catch (Exception e) {
            logger.error("Archive buildMonitorRecord error!", e);
        }

        return records;

    }

    private MonitorRecord buildMonitorRecord(String metric, String topic, String app, long value, String brokerId, long time, String ip) {
        MonitorRecord mr = new MonitorRecord();
        mr.setMetric(metric);
        mr.host(ip);
        mr.setValue(value);
        mr.setTimestamp(time);
        mr.brokerId(brokerId);
        mr.topic(topic);
        mr.app(app);

        return mr;
    }

    private List<MonitorRecord> buildReceive(String brokerId, long time, BrokerStat brokerStat) {
        List<MonitorRecord> result = new ArrayList<>();

        ConcurrentMap<String, TopicStat> repStats = brokerStat.getTopicStats();
        for (String topic : repStats.keySet()) {
            ConcurrentMap<Integer, PartitionGroupStat> pgStats = repStats.get(topic).getPartitionGroupStatMap();
            for (Integer pg : pgStats.keySet()) {
                result.addAll(buildReceiveRecord(topic, pg, pgStats.get(pg), brokerId, time));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Receive size : {}", result.size());
        }

        return result;
    }

    private List<MonitorRecord> buildReceiveRecord(String topic, Integer pg, PartitionGroupStat partitionGroupStat, String brokerId, long time) {
        List<MonitorRecord> result = new ArrayList<>();
        MonitorRecord receiveAvg = new MonitorRecord();
        fillRecord(receiveAvg, time);
        receiveAvg.brokerId(brokerId);
        receiveAvg.topic(topic);
        receiveAvg.partitionGroup(pg.toString());
        MonitorRecord receiveTP99;
        MonitorRecord receiveTP90;
        MonitorRecord receiveMax;
        MonitorRecord receive;
        try {
            receiveTP99 = (MonitorRecord) receiveAvg.clone();
            receiveTP90 = (MonitorRecord) receiveAvg.clone();
            receiveMax = (MonitorRecord) receiveAvg.clone();
            receive = (MonitorRecord) receiveAvg.clone();
        } catch (CloneNotSupportedException e) {
            logger.error("build Receive monitor data error!", e);
            return result;
        }

        EnQueueStat repStat = partitionGroupStat.getReplicationStat().getAppendStat();
        receiveAvg.setMetric(PG_RECEIVE_AVG);
        receiveAvg.setValue(repStat.getAvg());
        receiveTP99.setMetric(PG_RECEIVE_TP99);
        receiveTP99.setValue(repStat.getTp99());
        receiveTP90.setMetric(PG_RECEIVE_TP90);
        receiveTP90.setValue(repStat.getTp90());
        receiveMax.setMetric(PG_RECEIVE_MAX);
        receiveMax.setValue(repStat.getMax());
        receive.setMetric(PG_RECEIVE);
        receive.setValue(repStat.getOneMinuteRate());

        result.add(receiveAvg);
        result.add(receiveTP99);
        result.add(receiveTP90);
        result.add(receiveMax);
        result.add(receive);

        return result;
    }

    private List<MonitorRecord> buildReplicate(String brokerId, long time, BrokerStat brokerStat) {

        List<MonitorRecord> result = new ArrayList<>();

        ConcurrentMap<String, TopicStat> repStats = brokerStat.getTopicStats();
        for (String topic : repStats.keySet()) {
            ConcurrentMap<Integer, PartitionGroupStat> pgStats = repStats.get(topic).getPartitionGroupStatMap();
            for (Integer pg : pgStats.keySet()) {
                result.addAll(buildRepRecord(topic, pg, pgStats.get(pg), brokerId, time));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Replicate size : {}", result.size());
        }

        return result;
    }

    private List<MonitorRecord> buildRepRecord(String topic, Integer pg, PartitionGroupStat partitionGroupStat, String brokerId, long time) {
        List<MonitorRecord> result = new ArrayList<>();
        MonitorRecord replicateAvg = new MonitorRecord();
        fillRecord(replicateAvg, time);
        replicateAvg.brokerId(brokerId);
        replicateAvg.topic(topic);
        replicateAvg.partitionGroup(pg.toString());
        MonitorRecord replicateTP99;
        MonitorRecord replicateTP90;
        MonitorRecord replicateMax;
        MonitorRecord replicate;
        try {
            replicateTP99 = (MonitorRecord) replicateAvg.clone();
            replicateTP90 = (MonitorRecord) replicateAvg.clone();
            replicateMax = (MonitorRecord) replicateAvg.clone();
            replicate = (MonitorRecord) replicateAvg.clone();
        } catch (CloneNotSupportedException e) {
            logger.error("build Rep monitor data error!", e);
            return result;
        }

        EnQueueStat repStat = partitionGroupStat.getReplicationStat().getReplicaStat();
        replicateAvg.setMetric(PG_REPLICATE_AVG);
        replicateAvg.setValue(repStat.getAvg());
        replicateTP99.setMetric(PG_REPLICATE_TP99);
        replicateTP99.setValue(repStat.getTp99());
        replicateTP90.setMetric(PG_REPLICATE_TP90);
        replicateTP90.setValue(repStat.getTp90());
        replicateMax.setMetric(PG_REPLICATE_MAX);
        replicateMax.setValue(repStat.getMax());
        replicate.setMetric(PG_REPLICATE);
        replicate.setValue(repStat.getOneMinuteRate());

        result.add(replicateAvg);
        result.add(replicateTP99);
        result.add(replicateTP90);
        result.add(replicateMax);
        result.add(replicate);

        return result;

    }

    private List<MonitorRecord> buildPending(String brokerId, long time, Map topicPending) {

        MonitorRecord record = new MonitorRecord();
        fillRecord(record, time);
        record.brokerId(brokerId);

        String[] pendingMetrics = {TOPIC_PENDING, APP_PENDING, PG_PENDING, PARTITION_PENDING};

        return buildLevelPending(record, topicPending, pendingMetrics, 0);
    }

    private List<MonitorRecord> buildLevelPending(MonitorRecord record, Map pendingMap, String[] pendingMetrics, int i) {

        List<MonitorRecord> result = new ArrayList<>();
        if (pendingMap == null) {
            return result;
        }

        for (Object key : pendingMap.keySet()) {
            MonitorRecord appPending = buildRecord(record, pendingMetrics[i]);
            if (appPending == null) {
                continue;
            }
            appPending.addTag("t" + (i + 3), key.toString());
            Object next = pendingMap.get(key);
            Map aps;
            if (next != null && next instanceof PendingStat) {
                aps = ((PendingStat) next).getPendingStatSubMap();
                result.addAll(buildLevelPending(appPending, aps, pendingMetrics, i + 1));
            } else {
                appPending.setValue(next == null ? 0 : (Long) next);
                result.add(appPending);
                continue;
            }
            if (aps == null) {
                continue;
            }
            Object value = pendingMap.get(key);
            if (value != null && value instanceof PendingStat) {

                PendingStat stat = (PendingStat) value;
                appPending.setValue(stat.getPending());
            } else {
                if (value == null) {
                    appPending.setValue(0);
                } else {
                    appPending.setValue((Double) value);
                }
            }
            result.add(appPending);
        }

        return result;


    }


    private void fillRecord(MonitorRecord record, long time) {
        record.setService("jmq-broker");
        record.setEndpoint("agent.collect");
        record.setCategory("Other");
        record.setProvider("jmq4");
        record.setTimestamp(time);
    }

    private List<MonitorRecord> buildPartitionGroup(BrokerStat brokerStat, String brokerId, long l) {
        ConcurrentMap<String, TopicStat> topics = brokerStat.getTopicStats();

        List<MonitorRecord> records = new ArrayList<>();

        records.addAll(buildBrokerJVMState(brokerStat.getJvmStat().getRecentSnapshot(), brokerId, l));
        for (String tsKey : topics.keySet()) {

            ConcurrentMap<String, AppStat> appStats = topics.get(tsKey).getAppStats();

            for (String app : appStats.keySet()) {
                AppStat appStat = appStats.get(app);
                ConcurrentMap<Integer, PartitionGroupStat> pgs = appStat.getPartitionGroupStatMap();
                for (Integer key : pgs.keySet()) {
                    if (pgs.get(key).getEnQueueStat() != null) {
                        records.addAll(buildEnQueueRecords(pgs.get(key).getEnQueueStat(), brokerId, l, tsKey, app, key));
                    }
                    if (pgs.get(key).getDeQueueStat() != null) {
                        records.addAll(buildDeQueueRecords(pgs.get(key).getDeQueueStat(), brokerId, l, tsKey, app, key));
                    }

                }
            }
            // topic level partition group state
            if (topics.get(tsKey) != null) {
                Map<Integer, PartitionGroupStat> partitionGroupStatMap = topics.get(tsKey).getPartitionGroupStatMap();
                for (PartitionGroupStat partitionGroupStat : partitionGroupStatMap.values()) {
                    records.addAll(buildReplicaLag(partitionGroupStat, brokerId, tsKey, null, partitionGroupStat.getPartitionGroup(), l));
                    records.addAll(buildElectionAndReplicaState(partitionGroupStat, brokerId, tsKey, null, partitionGroupStat.getPartitionGroup(), l));

                }
            }
        }
        return records;
    }


    /**
     * Build partition group replica lag
     *
     * @param partitionGroupStat current partition group stat
     * @param timestampMs        broker state snapshot time in ms
     **/
    public List<MonitorRecord> buildReplicaLag(PartitionGroupStat partitionGroupStat, String brokerId, String topic,
                                               String app, Integer partitionGroup, long timestampMs) {
        long maxLogPosition = partitionGroupStat.getReplicationStat().getMaxLogPosition();
        MonitorRecord replicaRecord = new MonitorRecord();
        fillRecord(replicaRecord, timestampMs);
        replicaRecord.setValue(maxLogPosition);
        // not current broker id
        replicaRecord.brokerId(brokerId);
        replicaRecord.topic(topic);
        replicaRecord.app(app);
        replicaRecord.setMetric(PG_SLICE_REPLICA_LOG_MAX_POSITION);
        replicaRecord.partitionGroup(partitionGroup.toString());
        return Lists.newArrayList(replicaRecord);
    }


    /**
     * Build partition group election and replica state
     * contain term and type
     *
     * @param partitionGroupStat current partition group stat
     * @param timestampMs        broker state snapshot time in ms
     **/
    public List<MonitorRecord> buildElectionAndReplicaState(PartitionGroupStat partitionGroupStat, String brokerId, String topic,
                                                            String app, Integer partitionGroup, long timestampMs) {
        MonitorRecord electionRecord = new MonitorRecord();
        fillRecord(electionRecord, timestampMs);
        // not current broker id
        electionRecord.brokerId(brokerId);
        // PG_SLICE_REPLICA_STAT is compose value
        electionRecord.brokerId(partitionGroup.toString());
        electionRecord.topic(topic);
        electionRecord.app(app);
        electionRecord.partitionGroup(partitionGroup.toString());
        String[] metrics = {PG_SLICE_ELECTION_TERM, PG_SLICE_ELECTION_TYPE, PG_SLICE_REPLICA_STAT};
        List<MonitorRecord> electionAndReplicaRecords = buildEmptyRecords(electionRecord, metrics);
        // compose value
        if (partitionGroupStat.getReplicationStat().getStat().getState() != null
                && partitionGroupStat.getElectionEventStat().getState() != null) {
            // term
            electionAndReplicaRecords.get(0).setValue(partitionGroupStat.getElectionEventStat().getTerm());
            // type
            electionAndReplicaRecords.get(1).setValue(partitionGroupStat.getElectionEventStat().getState().ordinal());
            electionAndReplicaRecords.get(2).setValue(partitionGroupStat.getReplicationStat().getStat().getState().ordinal());
        } else {
            electionAndReplicaRecords.get(0).setValue(0);
            electionAndReplicaRecords.get(1).setValue(0);
            electionAndReplicaRecords.get(2).setValue(0);
        }
        return electionAndReplicaRecords;
    }


    /**
     * Heap,non-heap, direct buffer size
     **/
    public List<MonitorRecord> buildBrokerJVMState(JVMStat jvmStat, String brokerId, long timestampMs) {
        MonitorRecord jvmRecord = new MonitorRecord();
        fillRecord(jvmRecord, timestampMs);
        // not current broker id
        jvmRecord.brokerId(brokerId);
        String[] jvmMetrics = {BROKER_SLICE_HEAP_INIT, BROKER_SLICE_HEAP_USED, BROKER_SLICE_HEAP_COMMITTED,
                BROKER_SLICE_HEAP_MAX, BROKER_SLICE_NON_HEAP_INIT, BROKER_SLICE_NON_HEAP_USED,
                BROKER_SLICE_NON_HEAP_COMMITTED, BROKER_SLICE_NON_HEAP_MAX,
                BROKER_SLICE_DIRECT_BUFFER_SIZE, BROKER_SLICE_YOUNG_GC_TIMES,
                BROKER_SLICE_YOUNG_GC_ELAPSED, BROKER_SLICE_OLD_GC_TIMES, BROKER_SLICE_OLD_GC_ELAPSED};
        // create a record for all metrics
        List<MonitorRecord> jvmMonitorRecords = buildEmptyRecords(jvmRecord, jvmMetrics);
        MemoryStat memoryState = jvmStat.getMemoryStat();
        if (memoryState == null) {
            return Lists.newLinkedList();
        }
        jvmMonitorRecords.get(0).setValue(memoryState.getHeapInit());
        jvmMonitorRecords.get(1).setValue(memoryState.getHeapUsed());
        jvmMonitorRecords.get(2).setValue(memoryState.getHeapCommitted());
        jvmMonitorRecords.get(3).setValue(memoryState.getHeapMax());

        jvmMonitorRecords.get(4).setValue(memoryState.getNonHeapInit());
        jvmMonitorRecords.get(5).setValue(memoryState.getNonHeapUsed());
        jvmMonitorRecords.get(6).setValue(memoryState.getNonHeapCommitted());
        jvmMonitorRecords.get(7).setValue(memoryState.getNonHeapMax());

        jvmMonitorRecords.get(8).setValue(memoryState.getDirectBufferSize());

        // old gc total elapsed time
        jvmMonitorRecords.get(9).setValue(jvmStat.getOldGcTimes().getCount());
        Snapshot oldSnapshot = jvmStat.getOldGcTimes().getSnapshot();
        jvmMonitorRecords.get(10).setValue(oldSnapshot.getMean() * oldSnapshot.size());

        // eden gc count and total elapsed time
        Snapshot edenSnapshot = jvmStat.getEdenGcTimes().getSnapshot();
        jvmMonitorRecords.get(11).setValue(jvmStat.getEdenGcTimes().getCount());
        jvmMonitorRecords.get(12).setValue(edenSnapshot.getMean() * edenSnapshot.size());
        return jvmMonitorRecords;
    }


    private List<MonitorRecord> buildDeQueueRecords(DeQueueStat deQueueStat, String brokerId, long time, String tsKey, String app, Integer key) {

        MonitorRecord deQueue = new MonitorRecord();
        fillRecord(deQueue, time);
        deQueue.brokerId(brokerId);
        deQueue.topic(tsKey);
        deQueue.app(app);
        deQueue.partitionGroup(key.toString());

        String[] metrics = {PG_SLICE_DEQUEUE, PG_SLICE_DEQUEUE_SIZE, PG_SLICE_DEQUEUE_MAX, PG_SLICE_DEQUEUE_AVG, PG_SLICE_DEQUEUE_MIN, PG_SLICE_DEQUEUE_TP99, PG_SLICE_DEQUEUE_TP90};

        List<MonitorRecord> deQueueList = buildEmptyRecords(deQueue, metrics);

        deQueueList.get(0).setValue(deQueueStat.getOneMinuteRate());
        deQueueList.get(1).setValue(deQueueStat.getSize());
        deQueueList.get(2).setValue(deQueueStat.getMax());
        deQueueList.get(3).setValue(deQueueStat.getAvg());
        deQueueList.get(4).setValue(deQueueStat.getMin());
        deQueueList.get(5).setValue(deQueueStat.getTp99());
        deQueueList.get(6).setValue(deQueueStat.getTp90());

        return new ArrayList<>(deQueueList);
    }

    private List<MonitorRecord> buildEnQueueRecords(EnQueueStat enQueueStat, String brokerId, long time, String tsKey, String app, Integer key) {
        List<MonitorRecord> records = new ArrayList<>();

        MonitorRecord enQueue = new MonitorRecord();
        fillRecord(enQueue, time);
        enQueue.brokerId(brokerId);
        enQueue.topic(tsKey);
        enQueue.app(app);
        enQueue.partitionGroup(key.toString());

        String[] metrics = {PG_SLICE_ENQUEUE, PG_SLICE_ENQUEUE_SIZE, PG_SLICE_ENQUEUE_MAX, PG_SLICE_ENQUEUE_AVG, PG_SLICE_ENQUEUE_MIN, PG_SLICE_ENQUEUE_TP99, PG_SLICE_ENQUEUE_TP90};

        List<MonitorRecord> emptyRecords = buildEmptyRecords(enQueue, metrics);
        if (emptyRecords.isEmpty()) {
            return records;
        }

        emptyRecords.get(0).setValue(enQueueStat.getOneMinuteRate());
        emptyRecords.get(1).setValue(enQueueStat.getSize());
        emptyRecords.get(2).setValue(enQueueStat.getMax());
        emptyRecords.get(3).setValue(enQueueStat.getAvg());
        emptyRecords.get(4).setValue(enQueueStat.getMin());
        emptyRecords.get(5).setValue(enQueueStat.getTp99());
        emptyRecords.get(6).setValue(enQueueStat.getTp90());

        records.addAll(emptyRecords);

        return records;
    }

    private List<MonitorRecord> buildBrokerRecord(BrokerStat brokerStat, String brokerId, long time) {


        List<MonitorRecord> records = new ArrayList<>();
        MonitorRecord enQueue = new MonitorRecord();
        fillRecord(enQueue, time);
        enQueue.brokerId(brokerId);

        MonitorRecord enQueueSize;
        MonitorRecord enQueueTp99;
        MonitorRecord enQueueTp90;

        MonitorRecord deQueueRecord;
        MonitorRecord deQueueSize;
        MonitorRecord deQueueTp99;
        MonitorRecord deQueueTp90;
        MonitorRecord storage;

        MonitorRecord connectionsRecord;

        try {
            storage = (MonitorRecord) enQueue.clone();

            deQueueRecord = (MonitorRecord) enQueue.clone();
            deQueueSize = (MonitorRecord) enQueue.clone();
            enQueueSize = (MonitorRecord) enQueue.clone();

            enQueueTp99 = (MonitorRecord) enQueue.clone();
            enQueueTp90 = (MonitorRecord) enQueue.clone();

            deQueueTp99 = (MonitorRecord) enQueue.clone();
            deQueueTp90 = (MonitorRecord) enQueue.clone();

            connectionsRecord = (MonitorRecord) enQueue.clone();
        } catch (CloneNotSupportedException e) {
            logger.error("clone monitor record error!", e);
            return records;
        }

        storage.setMetric(BROKER_SLICE_STORAGE);
        storage.setValue(brokerStat.getStoragePercent());

        enQueue.setMetric(BROKER_SLICE_ENQUEUE);
        enQueue.setValue(brokerStat.getEnQueueStat().getOneMinuteRate());

        enQueueSize.setMetric(BROKER_SLICE_ENQUEUE_SIZE);
        enQueueSize.setValue(brokerStat.getEnQueueStat().getSize());

        deQueueRecord.setMetric(BROKER_SLICE_DEQUEUE);
        deQueueRecord.setValue(brokerStat.getDeQueueStat().getOneMinuteRate());

        deQueueSize.setMetric(BROKER_SLICE_DEQUEUE_SIZE);
        deQueueSize.setValue(brokerStat.getDeQueueStat().getSize());

        connectionsRecord.setValue(brokerStat.getConnectionStat().getConnection());
        connectionsRecord.setMetric(BROKER_CONNECTION);

        enQueueTp99.setMetric(BROKER_SLICE_ENQUEUE_TP99);
        enQueueTp90.setMetric(BROKER_SLICE_ENQUEUE_TP90);
        enQueueTp99.setValue(brokerStat.getEnQueueStat().getTp99());
        enQueueTp90.setValue(brokerStat.getEnQueueStat().getTp90());

        deQueueTp99.setMetric(BROKER_SLICE_ENQUEUE_TP99);
        deQueueTp90.setMetric(BROKER_SLICE_DEQUEUE_TP90);

        deQueueTp99.setValue(brokerStat.getDeQueueStat().getTp99());
        deQueueTp90.setValue(brokerStat.getDeQueueStat().getTp90());

        // 存储占比
        records.add(storage);

        records.add(deQueueRecord);
        records.add(deQueueSize);
        records.add(enQueue);
        records.add(enQueueSize);

        records.add(enQueueTp99);
        records.add(enQueueTp90);
        records.add(deQueueTp99);
        records.add(deQueueTp90);

        return records;
    }

    private List<MonitorRecord> buildTopicRecord(BrokerStat brokerStat, String brokerId, long time) {

        List<MonitorRecord> records = new ArrayList<>();
        ConcurrentMap<String, TopicStat> topics = brokerStat.getTopicStats();

        for (String tsKey : topics.keySet()) {
            MonitorRecord enQueue = new MonitorRecord();
            fillRecord(enQueue, time);
            enQueue.brokerId(brokerId);

            enQueue.topic(tsKey);

            String[] enQueueMetrics = {TOPIC_SLICE_ENQUEUE, TOPIC_SLICE_ENQUEUE_SIZE,
                    TOPIC_SLICE_ENQUEUE_MAX, TOPIC_SLICE_ENQUEUE_AVG,
                    TOPIC_SLICE_ENQUEUE_MIN, TOPIC_SLICE_ENQUEUE_TP99,
                    TOPIC_SLICE_ENQUEUE_TP90};

            List<MonitorRecord> enQueueRecords = buildEmptyRecords(enQueue, enQueueMetrics);

            if (enQueueRecords != null && enQueueRecords.size() == enQueueMetrics.length) {
                enQueueRecords.get(0).setValue(topics.get(tsKey).getEnQueueStat().getOneMinuteRate());
                enQueueRecords.get(1).setValue(topics.get(tsKey).getEnQueueStat().getSize());
                enQueueRecords.get(2).setValue(topics.get(tsKey).getEnQueueStat().getMax());
                enQueueRecords.get(3).setValue(topics.get(tsKey).getEnQueueStat().getAvg());
                enQueueRecords.get(4).setValue(topics.get(tsKey).getEnQueueStat().getMin());
                enQueueRecords.get(5).setValue(topics.get(tsKey).getEnQueueStat().getTp99());
                enQueueRecords.get(6).setValue(topics.get(tsKey).getEnQueueStat().getTp90());
                records.addAll(enQueueRecords);
            }

            String[] deQueueMetrics = {TOPIC_SLICE_DEQUEUE, TOPIC_SLICE_DEQUEUE_SIZE,
                    TOPIC_SLICE_DEQUEUE_MAX, TOPIC_SLICE_DEQUEUE_AVG,
                    TOPIC_SLICE_DEQUEUE_MIN, TOPIC_SLICE_DEQUEUE_TP99,
                    TOPIC_SLICE_DEQUEUE_TP90};
            List<MonitorRecord> deQueueRecords = buildEmptyRecords(enQueue, deQueueMetrics);

            if (deQueueRecords != null && deQueueRecords.size() == deQueueMetrics.length) {
                deQueueRecords.get(0).setValue(topics.get(tsKey).getDeQueueStat().getOneMinuteRate());
                deQueueRecords.get(1).setValue(topics.get(tsKey).getDeQueueStat().getSize());
                deQueueRecords.get(2).setValue(topics.get(tsKey).getDeQueueStat().getMax());
                deQueueRecords.get(3).setValue(topics.get(tsKey).getDeQueueStat().getAvg());
                deQueueRecords.get(4).setValue(topics.get(tsKey).getDeQueueStat().getMin());
                deQueueRecords.get(5).setValue(topics.get(tsKey).getDeQueueStat().getTp99());
                deQueueRecords.get(6).setValue(topics.get(tsKey).getDeQueueStat().getTp90());
                records.addAll(deQueueRecords);
            }

            // topic storage size monitor record
            String[] topicStorageMetric = {TOPIC_SLICE_STORAGE_SIZE};
            List<MonitorRecord> topicStorageRecords = buildEmptyRecords(enQueue, topicStorageMetric);
            if (topicStorageRecords != null && topicStorageRecords.size() > 0) {
                topicStorageRecords.get(0).setValue(topics.get(tsKey).getStoreSize());
                records.addAll(topicStorageRecords);
            }


        }
        return records;
    }

    private List<MonitorRecord> buildBrokerTopicApp(BrokerStat brokerStat, String brokerId, long time) {

        ConcurrentMap<String, TopicStat> topics = brokerStat.getTopicStats();
        logger.debug("start collect app metric ,topicSize: " + topics.size());
        List<MonitorRecord> records = new ArrayList<>();
        for (String tsKey : topics.keySet()) {

            ConcurrentMap<String, AppStat> appStats = topics.get(tsKey).getAppStats();

            if (appStats == null || appStats.isEmpty()) {
                logger.debug("appStats is empty!");
            } else {
                logger.debug("appStatus size:" + appStats.size());
            }
            for (String app : appStats.keySet()) {

                AppStat appStat = appStats.get(app);
                if (appStat.getConsumerStat() != null) {
                    records.addAll(getConsumerRecord(appStat, tsKey, time, brokerId));
                }
                if (appStat.getProducerStat() != null) {
                    records.addAll(getProducerRecord(appStat, tsKey, time, brokerId));
                }
            }
        }

        logger.debug("All app metrics result :" + records.size());
        return records;
    }

    private List<MonitorRecord> getProducerRecord(AppStat appStat, String topic, long time, String brokerId) {

        List<MonitorRecord> records = new ArrayList<>();

        logger.debug("Start collect producer records!");
        String app = appStat.getApp();

        MonitorRecord enQueue = new MonitorRecord();
        fillRecord(enQueue, time);
        enQueue.brokerId(brokerId);
        enQueue.topic(topic);
        enQueue.app(app);

        MonitorRecord connection = buildRecord(enQueue, APP_SLICE_ENQUEUE_CONNECTION);

        if (connection != null) {
            connection.setValue(appStat.getConnectionStat().getConnection());
            records.add(connection);
        }

        String[] metrics = {APP_SLICE_ENQUEUE, APP_SLICE_ENQUEUE_SIZE, APP_SLICE_ENQUEUE_MAX, APP_SLICE_ENQUEUE_AVG, APP_SLICE_ENQUEUE_MIN, APP_SLICE_ENQUEUE_TP99, APP_SLICE_ENQUEUE_TP90};
        List<MonitorRecord> emptyRecords = buildEmptyRecords(enQueue, metrics);

        if (emptyRecords != null && emptyRecords.size() == metrics.length) {
            emptyRecords.get(0).setValue(appStat.getProducerStat().getEnQueueStat().getOneMinuteRate());
            emptyRecords.get(1).setValue(appStat.getProducerStat().getEnQueueStat().getSize());
            emptyRecords.get(2).setValue(appStat.getProducerStat().getEnQueueStat().getMax());
            emptyRecords.get(3).setValue(appStat.getProducerStat().getEnQueueStat().getAvg());
            emptyRecords.get(4).setValue(appStat.getProducerStat().getEnQueueStat().getMin());
            emptyRecords.get(5).setValue(appStat.getProducerStat().getEnQueueStat().getTp99());
            emptyRecords.get(6).setValue(appStat.getProducerStat().getEnQueueStat().getTp90());

            records.addAll(emptyRecords);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Producer records:" + records.size());

        }

        return records;

    }

    private MonitorRecord buildRecord(MonitorRecord enQueue, String appSliceEnqueueConnection) {

        try {
            MonitorRecord result = (MonitorRecord) enQueue.clone();
            HashMap<String, String> map = new HashMap<>();
            for (String key : enQueue.getTags().keySet()) {
                map.put(key, enQueue.getTag(key));
            }
            result.setTags(map);
            result.setMetric(appSliceEnqueueConnection);
            return result;
        } catch (CloneNotSupportedException e) {
            logger.error("Clone MonitorRecord error!", e);
        }

        return null;
    }

    private List<MonitorRecord> buildEmptyRecords(MonitorRecord count, String[] metrics) {

        List<MonitorRecord> result = new ArrayList<>();

        for (String metric : metrics) {

            try {
                MonitorRecord record = (MonitorRecord) count.clone();
                record.setMetric(metric);
                result.add(record);

            } catch (CloneNotSupportedException e) {
                logger.error("clone monitor record error!", e);
            }
        }

        if (result.size() != metrics.length) {
            result.clear();
        }
        return result;

    }

    private List<MonitorRecord> getConsumerRecord(AppStat appStat, String topic, long time, String brokerId) {
        List<MonitorRecord> records = new ArrayList<>();

        if (logger.isDebugEnabled()) {
            logger.debug("Start collect consume records!");
        }
        String app = appStat.getApp();

        MonitorRecord deQueue = new MonitorRecord();
        fillRecord(deQueue, time);

        deQueue.brokerId(brokerId);
        deQueue.topic(topic);
        deQueue.app(app);

        MonitorRecord connection = buildRecord(deQueue, APP_SLICE_DEQUEUE_CONNECTION);

        if (connection != null) {
            connection.setValue(appStat.getConsumerStat().getConnectionStat().getConnection());
            records.add(connection);
        }

        String[] metrics = {APP_SLICE_DEQUEUE, APP_SLICE_DEQUEUE_SIZE, APP_SLICE_DEQUEUE_MAX, APP_SLICE_DEQUEUE_AVG, APP_SLICE_DEQUEUE_MIN, APP_SLICE_DEQUEUE_TP99, APP_SLICE_DEQUEUE_TP90};
        List<MonitorRecord> emptyRecords = buildEmptyRecords(deQueue, metrics);

        if (emptyRecords != null && emptyRecords.size() == 7) {

            emptyRecords.get(0).setValue(appStat.getConsumerStat().getDeQueueStat().getOneMinuteRate());
            emptyRecords.get(1).setValue(appStat.getConsumerStat().getDeQueueStat().getSize());
            emptyRecords.get(2).setValue(appStat.getConsumerStat().getDeQueueStat().getMax());
            emptyRecords.get(3).setValue(appStat.getConsumerStat().getDeQueueStat().getAvg());
            emptyRecords.get(4).setValue(appStat.getConsumerStat().getDeQueueStat().getMin());
            emptyRecords.get(5).setValue(appStat.getConsumerStat().getDeQueueStat().getTp99());
            emptyRecords.get(6).setValue(appStat.getConsumerStat().getDeQueueStat().getTp90());

            records.addAll(emptyRecords);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("consume records:" + records.size());
        }
        return records;
    }

    @Override
    public String type() {
        return "default";
    }
}
