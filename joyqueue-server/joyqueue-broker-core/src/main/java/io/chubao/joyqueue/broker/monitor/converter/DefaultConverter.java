package io.chubao.joyqueue.broker.monitor.converter;

import io.chubao.joyqueue.broker.monitor.PendingStat;
import io.chubao.joyqueue.broker.monitor.stat.AppStat;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStat;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStatExt;
import io.chubao.joyqueue.broker.monitor.stat.DeQueueStat;
import io.chubao.joyqueue.broker.monitor.stat.EnQueueStat;
import io.chubao.joyqueue.broker.monitor.stat.PartitionGroupStat;
import io.chubao.joyqueue.broker.monitor.stat.TopicPendingStat;
import io.chubao.joyqueue.broker.monitor.stat.TopicStat;
import io.chubao.joyqueue.model.MonitorRecord;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lining11
 * Date: 2019/1/9
 */
public class DefaultConverter implements Converter<BrokerStatExt, List<MonitorRecord>> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConverter.class);

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

    private static final String PG_SLICE_ENQUEUE = "pg_slice_enqueue";
    private static final String PG_SLICE_ENQUEUE_SIZE = "pg_slice_enqueue_size";
    private static final String PG_SLICE_ENQUEUE_TP99 = "pg_slice_enqueue_tp99";
    private static final String PG_SLICE_ENQUEUE_TP90 = "pg_slice_enqueue_tp90";
    private static final String PG_SLICE_ENQUEUE_MAX = "pg_slice_enqueue_max";
    private static final String PG_SLICE_ENQUEUE_MIN = "pg_slice_enqueue_min";
    private static final String PG_SLICE_ENQUEUE_AVG = "pg_slice_enqueue_avg";

    private static final String TOPIC_PENDING = "topic_pending";
    private static final String APP_PENDING = "app_pending";
    private static final String PG_PENDING = "pg_pending";
    private static final String PARTITION_PENDING = "partition_pending";

    private boolean broker = true;

    private boolean topic = true;

    private boolean app = true;

    private boolean partitionGroup = true;

    private boolean pending = true;


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
        }
        return records;
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

        MonitorRecord connectionsRecord;

        try {
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

        }
        return records;
    }

    private List<MonitorRecord> buildBrokerTopicApp(BrokerStat brokerStat, String brokerId, long time) {

        ConcurrentMap<String, TopicStat> topics = brokerStat.getTopicStats();
        logger.info("start collect app metric ,topicSize: " + topics.size());
        List<MonitorRecord> records = new ArrayList<>();
        for (String tsKey : topics.keySet()) {

            ConcurrentMap<String, AppStat> appStats = topics.get(tsKey).getAppStats();

            if (appStats == null || appStats.isEmpty()) {
                logger.info("appStats is empty!");
            } else {
                logger.info("appStatus size:" + appStats.size());
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

        logger.info("All app metrics result :" + records.size());
        return records;
    }

    private List<MonitorRecord> getProducerRecord(AppStat appStat, String topic, long time, String brokerId) {

        List<MonitorRecord> records = new ArrayList<>();

        logger.info("Start collect producer records!");
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
        logger.info("Producer records:" + records.size());

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

        logger.info("Start collect consume records!");
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
        logger.info("consume records:" + records.size());
        return records;
    }

    @Override
    public String type() {
        return "default";
    }
}
