package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.coordinator.session.CoordinatorSession;
import com.jd.journalq.broker.coordinator.session.CoordinatorSessionManager;
import com.jd.journalq.broker.index.command.ConsumeIndexStoreRequest;
import com.jd.journalq.broker.index.model.IndexAndMetadata;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionOffset;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.kafka.coordinator.transaction.helper.TransactionHelper;
import com.jd.journalq.broker.producer.transaction.command.TransactionCommitRequest;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.command.Direction;
import com.jd.journalq.network.transport.command.JMQCommand;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TransactionCommitSynchronizer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/18
 */
// TODO 补充日志
public class TransactionCommitSynchronizer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCommitSynchronizer.class);

    private KafkaConfig config;
    private CoordinatorSessionManager sessionManager;
    private TransactionIdManager transactionIdManager;
    private NameService nameService;

    public TransactionCommitSynchronizer(KafkaConfig config, CoordinatorSessionManager sessionManager, TransactionIdManager transactionIdManager, NameService nameService) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.transactionIdManager = transactionIdManager;
        this.nameService = nameService;
    }

    public boolean commit(TransactionMetadata transactionMetadata, List<TransactionPrepare> prepareList, Map<String, List<TransactionOffset>> offsets) throws Exception {
        return commitPrepare(transactionMetadata, prepareList) && commitOffsets(transactionMetadata, offsets);
    }

    public boolean commitPrepare(TransactionMetadata transactionMetadata, List<TransactionPrepare> prepareList) throws Exception {
        if (CollectionUtils.isEmpty(prepareList)) {
            return true;
        }

        prepareList = TransactionHelper.filterPrepareByBroker(prepareList);
        CountDownLatch latch = new CountDownLatch(prepareList.size());
        boolean[] result = {true};

        for (TransactionPrepare prepare : prepareList) {
            CoordinatorSession session = sessionManager.getOrCreateSession(prepare.getBrokerId(), prepare.getBrokerHost(), prepare.getBrokerPort());
            String txId = transactionIdManager.generateId(prepare.getTopic(), prepare.getApp(), prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch());
            TransactionCommitRequest transactionCommitRequest = new TransactionCommitRequest(prepare.getTopic(), prepare.getApp(), txId);
            session.async(new JMQCommand(transactionCommitRequest), new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    if (response.getHeader().getStatus() != JMQCode.SUCCESS.getCode() &&
                            response.getHeader().getStatus() != JMQCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
                        result[0] = false;
                    }
                    latch.countDown();
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    result[0] = false;
                    latch.countDown();
                }
            });
        }

        if (!latch.await(config.getTransactionSyncTimeout(), TimeUnit.MILLISECONDS)) {
            logger.error("commit transaction timeout, metadata: {}, prepare: {}", transactionMetadata, prepareList);
            return false;
        }

        return result[0];
    }

    public boolean commitOffsets(TransactionMetadata transactionMetadata, Map<String, List<TransactionOffset>> offsets) throws Exception {
        if (MapUtils.isEmpty(offsets)) {
            return true;
        }

        Map<Broker, Map<String, List<TransactionOffset>>> brokerMap = splitOffsetsByBroker(offsets);
        CountDownLatch latch = new CountDownLatch(brokerMap.size());
        boolean[] result = {true};

        for (Map.Entry<Broker, Map<String, List<TransactionOffset>>> entry : brokerMap.entrySet()) {
            Broker broker = entry.getKey();
            Map<String, Map<Integer, IndexAndMetadata>> saveOffsetParam = buildSaveOffsetParam(entry.getValue());

            try {
                CoordinatorSession session = sessionManager.getOrCreateSession(broker);
                ConsumeIndexStoreRequest indexStoreRequest = new ConsumeIndexStoreRequest(transactionMetadata.getApp(), saveOffsetParam);
                JMQHeader header = new JMQHeader(Direction.REQUEST, CommandType.CONSUME_INDEX_STORE_REQUEST);
                Command request = new Command(header, indexStoreRequest);

                session.async(request, new CommandCallback() {
                    @Override
                    public void onSuccess(Command request, Command response) {
//                        ConsumeIndexStoreResponse payload = (ConsumeIndexStoreResponse) response.getPayload();
//                        for (Map.Entry<String, Map<Integer, Short>> topicEntry : payload.getIndexStoreStatus().entrySet()) {
//                            String topic = topicEntry.getKey();
//                            for (Map.Entry<Integer, Short> partitionEntry : topicEntry.getValue().entrySet()) {
//                            }
//                        }
                        latch.countDown();
                    }

                    @Override
                    public void onException(Command request, Throwable cause) {
                        logger.error("sync offset failed, async transport exception, topic: {}, group: {}, leader: {id: {}, ip: {}, port: {}}",
                                saveOffsetParam, transactionMetadata.getApp(), broker.getId(), broker.getIp(), broker.getBackEndPort(), cause);
                        result[0] = false;
                        latch.countDown();
                    }
                });
            } catch (Throwable t) {
                logger.error("sync offset failed, async transport exception, topic: {}, group: {}, leader: {id: {}, ip: {}, port: {}}",
                        saveOffsetParam, transactionMetadata.getApp(), broker.getId(), broker.getIp(), broker.getBackEndPort(), t);
                latch.countDown();
            }
        }

        if (!latch.await(config.getTransactionSyncTimeout(), TimeUnit.MILLISECONDS)) {
            logger.error("commit transaction timeout, metadata: {}, offsets: {}", transactionMetadata, offsets);
            return false;
        }

        return result[0];
    }

    protected Map<String, Map<Integer, IndexAndMetadata>> buildSaveOffsetParam(Map<String, List<TransactionOffset>> offsets) {
        Map<String, Map<Integer, IndexAndMetadata>> result = Maps.newHashMap();
        for (Map.Entry<String, List<TransactionOffset>> entry : offsets.entrySet()) {
            String topic = entry.getKey();
            for (TransactionOffset offset : entry.getValue()) {
                Map<Integer, IndexAndMetadata> partitionMetadataMap = result.get(topic);
                if (partitionMetadataMap == null) {
                    partitionMetadataMap = Maps.newHashMap();
                    result.put(topic, partitionMetadataMap);
                }
                IndexAndMetadata indexAndMetadata = new IndexAndMetadata(offset.getOffset(), null);
                partitionMetadataMap.put((int) offset.getPartition(), indexAndMetadata);
            }
        }
        return result;
    }

    protected Map<Broker, Map<String, List<TransactionOffset>>> splitOffsetsByBroker(Map<String, List<TransactionOffset>> offsets) {
        Map<Broker, Map<String, List<TransactionOffset>>> result = Maps.newHashMap();

        for (Map.Entry<String, List<TransactionOffset>> entry : offsets.entrySet()) {
            TopicConfig topic = nameService.getTopicConfig(TopicName.parse(entry.getKey()));
            if (topic == null) {
                continue;
            }
            for (TransactionOffset offset : entry.getValue()) {
                PartitionGroup partitionGroup = topic.fetchPartitionGroupByPartition(offset.getPartition());
                if (partitionGroup == null) {
                    continue;
                }
                Broker broker = partitionGroup.getLeaderBroker();
                if (broker == null) {
                    continue;
                }
                Map<String, List<TransactionOffset>> topicOffsets = result.get(broker);
                if (topicOffsets == null) {
                    topicOffsets = Maps.newHashMap();
                    result.put(broker, topicOffsets);
                }
                List<TransactionOffset> partitionOffsets = topicOffsets.get(entry.getKey());
                if (partitionOffsets == null) {
                    partitionOffsets = Lists.newArrayList();
                    topicOffsets.put(entry.getKey(), partitionOffsets);
                }

                partitionOffsets.add(offset);
            }
        }
        return result;
    }
}