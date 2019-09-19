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
package io.chubao.joyqueue.broker.monitor;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.monitor.config.BrokerMonitorConfig;
import io.chubao.joyqueue.broker.monitor.stat.AppStat;
import io.chubao.joyqueue.broker.monitor.stat.BrokerStat;
import io.chubao.joyqueue.broker.monitor.stat.ConsumerStat;
import io.chubao.joyqueue.broker.monitor.stat.PartitionGroupStat;
import io.chubao.joyqueue.broker.monitor.stat.PartitionStat;
import io.chubao.joyqueue.broker.monitor.stat.ProducerStat;
import io.chubao.joyqueue.broker.monitor.stat.ReplicationStat;
import io.chubao.joyqueue.broker.monitor.stat.TopicStat;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.monitor.Client;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.session.Consumer;
import io.chubao.joyqueue.network.session.Producer;
import io.chubao.joyqueue.nsr.event.RemoveConsumerEvent;
import io.chubao.joyqueue.nsr.event.RemovePartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.RemoveTopicEvent;
import io.chubao.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.network.IpUtil;
import io.chubao.joyqueue.toolkit.service.Service;
import io.chubao.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * BrokerMonitor
 *
 * author: gaohaoxiang
 * date: 2018/11/16
 */
public class BrokerMonitor extends Service implements ConsumerMonitor, ProducerMonitor, ReplicationMonitor, SessionMonitor, EventListener<SessionManager.SessionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BrokerMonitor.class);

    private BrokerMonitorConfig config;
    private SessionManager sessionManager;
    private BrokerStatManager brokerStatManager;
    // 集群管理
    private ClusterManager clusterManager;

    // 统计基础汇总信息
    private BrokerStat brokerStat;

    public BrokerMonitor() {

    }

    public BrokerMonitor(BrokerMonitorConfig config, SessionManager sessionManager, BrokerStatManager brokerStatManager, ClusterManager clusterManager) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.brokerStatManager = brokerStatManager;
        this.clusterManager = clusterManager;
    }

    @Override
    protected void doStart() throws Exception {
        brokerStat = brokerStatManager.getBrokerStat();
        sessionManager.addListener(this);

        clusterManager.addListener(new MonitorMateDataListener());
    }

    @Override
    protected void doStop() {
        sessionManager.removeListener(this);
    }

    @Override
    public void onPutMessage(String topic, String app, int partitionGroup, short partition, long count, long size, double time) {
        if (!config.isEnable()) {
            return;
        }

        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        AppStat appStat = topicStat.getOrCreateAppStat(app);
//            PartitionGroupStat partitionGroupStat = appStat.getOrCreatePartitionGroupStat(partitionGroup);
//            PartitionStat partitionStat = partitionGroupStat.getOrCreatePartitionStat(partition);
        ProducerStat producerStat = appStat.getProducerStat();
        PartitionGroupStat producerPartitionGroupStat = producerStat.getOrCreatePartitionGroupStat(partitionGroup);

//            topicStat.getEnQueueStat().mark(time, size, count);
//            topicStat.getOrCreatePartitionGroupStat(partitionGroup).getEnQueueStat().mark(time, size, count);
//            topicStat.getOrCreatePartitionGroupStat(partitionGroup).getOrCreatePartitionStat(partition).getEnQueueStat().mark(time, size, count);

        producerStat.getEnQueueStat().mark(time, size, count);
        producerPartitionGroupStat.getEnQueueStat().mark(time, size, count);
        producerPartitionGroupStat.getOrCreatePartitionStat(partition).getEnQueueStat().mark(time, size, count);

//            partitionGroupStat.getEnQueueStat().mark(time, size, count);
//            partitionStat.getEnQueueStat().mark(time, size, count);
        brokerStat.getEnQueueStat().mark(time, size, count);
    }

    @Override
    public ConsumerStat getConsumerStat(String topic, String app) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        AppStat appStat = topicStat.getOrCreateAppStat(app);
        return appStat.getConsumerStat();
    }

    @Override
    public void onGetMessage(String topic, String app, int partitionGroup, short partition, long count, long size, double time) {
        if (!config.isEnable()) {
            return;
        }
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        AppStat appStat = topicStat.getOrCreateAppStat(app);
//            PartitionGroupStat partitionGroupStat = appStat.getOrCreatePartitionGroupStat(partitionGroup);
//            PartitionStat partitionStat = partitionGroupStat.getOrCreatePartitionStat(partition);
        ConsumerStat consumerStat = appStat.getConsumerStat();
        PartitionGroupStat consumerPartitionGroupStat = consumerStat.getOrCreatePartitionGroupStat(partitionGroup);

//            topicStat.getDeQueueStat().mark(time, size, count);
//            topicStat.getOrCreatePartitionGroupStat(partitionGroup).getDeQueueStat().mark(time, size, count);
//            topicStat.getOrCreatePartitionGroupStat(partitionGroup).getOrCreatePartitionStat(partition).getDeQueueStat().mark(time, size, count);

        consumerStat.getDeQueueStat().mark(time, size, count);
        consumerPartitionGroupStat.getDeQueueStat().mark(time, size, count);
        consumerPartitionGroupStat.getOrCreatePartitionStat(partition).getDeQueueStat().mark(time, size, count);

//            partitionGroupStat.getDeQueueStat().mark(time, size, count);
//            partitionStat.getDeQueueStat().mark(time, size, count);
        brokerStat.getDeQueueStat().mark(time, size, count);
    }

    @Override
    public void onAckMessage(String topic, String app, int partitionGroup, short partition) {
        if (!config.isEnable()) {
            return;
        }
        brokerStat.getOrCreateTopicStat(topic).
                getOrCreateAppStat(app)
                .getConsumerStat()
                .getOrCreatePartitionGroupStat(partitionGroup).
                getOrCreatePartitionStat(partition).
                lastAckTime(SystemClock.now());
    }

    @Override
    public void onReplicateMessage(String topic, int partitionGroup, long count, long size, double time) {
        if (!config.isEnable()) {
            return;
        }
        ReplicationStat replicationStat = brokerStat.getOrCreateTopicStat(topic).getOrCreatePartitionGroupStat(partitionGroup).getReplicationStat();
        replicationStat.getReplicaStat().mark(time, size, count);
        brokerStat.getReplicationStat().getReplicaStat().mark(time, size, count);
    }

    @Override
    public void onAppendReplicateMessage(String topic, int partitionGroup, long count, long size, double time) {
        if (!config.isEnable()) {
            return;
        }
        ReplicationStat replicationStat = brokerStat.getOrCreateTopicStat(topic).getOrCreatePartitionGroupStat(partitionGroup).getReplicationStat();
        replicationStat.getAppendStat().mark(time, size, count);
        brokerStat.getReplicationStat().getAppendStat().mark(time, size, count);
    }

    @Override
    public void onGetRetry(String topic, String app, long count, double time) {
        if (!config.isEnable()) {
            return;
        }
    }

    @Override
    public void onAddRetry(String topic, String app, long count, double time) {
        if (!config.isEnable()) {
            return;
        }
    }

    @Override
    public void onRetrySuccess(String topic, String app, long count) {
        if (!config.isEnable()) {
            return;
        }
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        AppStat appStat = topicStat.getOrCreateAppStat(app);
        appStat.getConsumerStat().getRetryStat().getSuccess().mark(count);
    }

    @Override
    public void onRetryFailure(String topic, String app, long count) {
        if (!config.isEnable()) {
            return;
        }
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        AppStat appStat = topicStat.getOrCreateAppStat(app);
        appStat.getConsumerStat().getRetryStat().getFailure().mark(count);
    }

    @Override
    public void addProducer(Producer producer) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(producer.getTopic());
        AppStat appStat = topicStat.getOrCreateAppStat(producer.getApp());
        Client client = brokerStat.getConnectionStat().getConnection(producer.getConnectionId());

        if (client == null) {
            return;
        }
        client.setProducerRole(true);
        appStat.getConnectionStat().addConnection(client);
        appStat.getConnectionStat().incrProducer();
        appStat.getProducerStat().getConnectionStat().addConnection(client);
        appStat.getProducerStat().getConnectionStat().incrProducer();

        topicStat.getConnectionStat().addConnection(client);
        topicStat.getConnectionStat().incrProducer();

        brokerStat.getConnectionStat().incrProducer();
    }

    @Override
    public void addConsumer(Consumer consumer) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(consumer.getTopic());
        AppStat appStat = topicStat.getOrCreateAppStat(consumer.getApp());
        Client client = brokerStat.getConnectionStat().getConnection(consumer.getConnectionId());

        if (client == null) {
            return;
        }
        client.setConsumerRole(true);
        appStat.getConnectionStat().addConnection(client);
        appStat.getConnectionStat().incrConsumer();
        appStat.getConsumerStat().getConnectionStat().addConnection(client);
        appStat.getConsumerStat().getConnectionStat().incrConsumer();

        topicStat.getConnectionStat().addConnection(client);
        topicStat.getConnectionStat().incrConsumer();

        brokerStat.getConnectionStat().incrConsumer();
    }

    @Override
    public void removeProducer(Producer producer) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(producer.getTopic());
        AppStat appStat = topicStat.getOrCreateAppStat(producer.getApp());

        if (!appStat.getConnectionStat().removeConnection(producer.getConnectionId())) {
            return;
        }

        appStat.getConnectionStat().removeConnection(producer.getConnectionId());
        appStat.getConnectionStat().decrProducer();
        appStat.getProducerStat().getConnectionStat().removeConnection(producer.getConnectionId());
        appStat.getProducerStat().getConnectionStat().decrProducer();

        topicStat.getConnectionStat().removeConnection(producer.getConnectionId());
        topicStat.getConnectionStat().decrProducer();

        brokerStat.getConnectionStat().decrProducer();
    }

    @Override
    public void removeConsumer(Consumer consumer) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(consumer.getTopic());
        AppStat appStat = topicStat.getOrCreateAppStat(consumer.getApp());

        if (!appStat.getConnectionStat().removeConnection(consumer.getConnectionId())) {
            return;
        }

        appStat.getConnectionStat().removeConnection(consumer.getConnectionId());
        appStat.getConnectionStat().decrConsumer();
        appStat.getConsumerStat().getConnectionStat().removeConnection(consumer.getConnectionId());
        appStat.getConsumerStat().getConnectionStat().decrConsumer();

        topicStat.getConnectionStat().removeConnection(consumer.getConnectionId());
        topicStat.getConnectionStat().decrConsumer();

        brokerStat.getConnectionStat().decrConsumer();
    }

    @Override
    public int getProducer(String topic, String app) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        AppStat appStat = topicStat.getOrCreateAppStat(app);
        return appStat.getProducerStat().getConnectionStat().getProducer();
    }

    @Override
    public int getConsumer(String topic, String app) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        AppStat appStat = topicStat.getOrCreateAppStat(app);
        return appStat.getProducerStat().getConnectionStat().getConsumer();
    }

    @Override
    public void addConnection(Connection connection) {
        InetSocketAddress address = IpUtil.toAddress(connection.getAddress());
        Client client = new Client();
        client.setConnectionId(connection.getId());
        client.setApp(connection.getApp());
        client.setLanguage(connection.getLanguage().name());
        client.setVersion(connection.getVersion());
        client.setSource(connection.getSource());
        client.setRegion(connection.getRegion());
        client.setNamespace(connection.getNamespace());
        client.setCreateTime(connection.getCreateTime());

        if (address != null && address.getAddress() != null) {
            client.setIp(address.getAddress().getHostAddress());
            client.setPort(address.getPort());
        }

        if (!brokerStat.getConnectionStat().addConnection(client)) {
            return;
        }
    }

    @Override
    public void removeConnection(Connection connection) {
        if (!brokerStat.getConnectionStat().removeConnection(connection.getId())) {
            return;
        }

        sessionManager.removeProducer(connection);
        sessionManager.removeConsumer(connection);
    }

    @Override
    public List<Client> getConnections(String topic, String app) {
        TopicStat topicStat = brokerStat.getOrCreateTopicStat(topic);
        AppStat appStat = topicStat.getOrCreateAppStat(app);
        return Lists.newArrayList(appStat.getConnectionStat().getConnectionMap().values());
    }

    @Override
    public void onEvent(SessionManager.SessionEvent event) {
        SessionManager.SessionEventType eventType = event.getType();
        switch (eventType) {
            case AddConnection:
                addConnection(event.getConnection());
                break;
            case RemoveConnection:
                removeConnection(event.getConnection());
            case AddProducer:
                addProducer(event.getProducer());
                break;
            case RemoveProducer:
                removeProducer(event.getProducer());
                break;
            case AddConsumer:
                addConsumer(event.getConsumer());
                break;
            case RemoveConsumer:
                removeConsumer(event.getConsumer());
                break;
        }
    }

    public BrokerStat getBrokerStat() {
        return brokerStat;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * 元数据监听
     */
    class MonitorMateDataListener implements EventListener<MetaEvent> {
        @Override
        public void onEvent(MetaEvent event) {
            switch (event.getEventType()) {
                case REMOVE_CONSUMER:
                    removeConsume(event);
                    break;
                case REMOVE_TOPIC:
                    removeTopic(event);
                    break;
                case REMOVE_PARTITION_GROUP:
                    removePartitionGroup(event);
                    break;
                case UPDATE_PARTITION_GROUP:
                    updatePartitionGroup(event);
                    break;
            }
        }

        private void removeConsume(MetaEvent metaEvent) {
            try {
                logger.info("listen remove_consume_event:[{}]", metaEvent);

                RemoveConsumerEvent removeConsumerEvent = (RemoveConsumerEvent) metaEvent;
                String topic = removeConsumerEvent.getTopic().getFullName();
                String app = removeConsumerEvent.getConsumer().getApp();
                if (brokerStat != null) {
                    TopicStat topicStat = brokerStat.getTopicStats().get(topic);
                    if (topicStat == null) {
                        return;
                    }

                    ConcurrentMap<String, AppStat> appStats = topicStat.getAppStats();
                    AppStat remove = appStats.remove(app);

                    if (remove != null) {
                        logger.info("success to unsubscribe app:[{}] from topic:[{}].", app, topic);
                    } else {
                        logger.info("have no subscribe app:[{}] from topic:[{}].", app, topic);
                    }
                }
            } catch (Throwable th) {
                logger.error("listen remove_consume_event error.", th);
            }
        }

        private void removeTopic(MetaEvent metaEvent) {
            try {
                logger.info("listen remove_topic_event:[{}]", metaEvent);

                RemoveTopicEvent removeTopicEvent = (RemoveTopicEvent) metaEvent;
                String topic = removeTopicEvent.getTopic().getName().getFullName();
                if (brokerStat != null) {
                    TopicStat remove = brokerStat.getTopicStats().remove(topic);
                    if (remove != null) {
                        logger.info("success to remove topic:[{}].", topic);
                    } else {
                        logger.info("have no topic:[{}].", topic);
                    }
                }
            } catch (Throwable th) {
                logger.error("listen remove_topic_event error.", th);
            }
        }

        private void removePartitionGroup(MetaEvent metaEvent) {
            try {
                logger.info("listen remove_partitionGroup_event:[{}]", metaEvent);

                RemovePartitionGroupEvent removePartitionGroupEvent = (RemovePartitionGroupEvent) metaEvent;
                String topic = removePartitionGroupEvent.getTopic().getFullName();
                if (brokerStat != null) {
                    TopicStat topicStat = brokerStat.getTopicStats().get(topic);
                    if (topicStat == null) {
                        return;
                    }

                    ConcurrentMap<Integer, PartitionGroupStat> partitionGroupStatMap = topicStat.getPartitionGroupStatMap();
                    PartitionGroupStat remove = partitionGroupStatMap.remove(removePartitionGroupEvent.getPartitionGroup());
                    if (remove != null) {
                        logger.info("success to remove partitionGroup :[{}] from topic:[{}]", removePartitionGroupEvent.getPartitionGroup(), topic);
                    } else {
                        logger.info("have no partitionGroup:[{}] from topic:[{}].", removePartitionGroupEvent.getPartitionGroup(), topic);
                    }
                }
            } catch (Throwable th) {
                logger.error("listen remove_partitionGroup_event error.", th);
            }
        }

        private void updatePartitionGroup(MetaEvent metaEvent) {
            // 只处理更新里面的删除partition情况
            try {
                logger.info("listen remove_partitionGroup_event:[{}]", metaEvent);

                UpdatePartitionGroupEvent updatePartitionGroupEvent = (UpdatePartitionGroupEvent) metaEvent;
                String topic = updatePartitionGroupEvent.getTopic().getFullName();
                if (brokerStat != null) {
                    TopicStat topicStat = brokerStat.getTopicStats().get(topic);
                    if (topicStat == null) {
                        return;
                    }

                    ConcurrentMap<Integer, PartitionGroupStat> partitionGroupStatMap = topicStat.getPartitionGroupStatMap();
                    PartitionGroupStat partitionGroupStat = partitionGroupStatMap.get(updatePartitionGroupEvent.getNewPartitionGroup().getGroup());
                    if (partitionGroupStat == null) {
                        return;
                    }

                    PartitionGroup partitionGroupByGroup = updatePartitionGroupEvent.getNewPartitionGroup();

                    ConcurrentMap<Short /** partition **/, PartitionStat> partitionStatMap = partitionGroupStat.getPartitionStatMap();
                    partitionStatMap.keySet().stream().forEach(partition -> {
                        if(!partitionGroupByGroup.getPartitions().contains(partition)) {
                            PartitionStat remove = partitionStatMap.remove(partition);
                            if (remove != null) {
                                logger.info("success to remove partition :[{}] from topic:[{}], partitionGroup:[{}]", partitionGroupByGroup, topic, partitionGroupByGroup.getGroup());
                            } else {
                                logger.info("have no partitionGroup:[{}] from topic:[{}], partitionGroup:[{}].", partitionGroupByGroup, topic, partitionGroupByGroup.getGroup());
                            }
                        }
                    });
                }
            } catch (Throwable th) {
                logger.error("listen update_partitionGroup_event error.", th);
            }
        }

    }
}
