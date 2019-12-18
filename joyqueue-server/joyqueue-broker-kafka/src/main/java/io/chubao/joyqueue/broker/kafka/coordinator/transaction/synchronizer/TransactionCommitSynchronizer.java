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
package io.chubao.joyqueue.broker.kafka.coordinator.transaction.synchronizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.coordinator.session.CoordinatorSession;
import io.chubao.joyqueue.broker.coordinator.session.CoordinatorSessionManager;
import io.chubao.joyqueue.broker.index.command.ConsumeIndexStoreRequest;
import io.chubao.joyqueue.broker.index.command.ConsumeIndexStoreResponse;
import io.chubao.joyqueue.broker.index.model.IndexAndMetadata;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionIdManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionOffset;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.helper.TransactionHelper;
import io.chubao.joyqueue.broker.producer.transaction.command.TransactionCommitRequest;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandCallback;
import io.chubao.joyqueue.network.transport.command.JoyQueueCommand;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TransactionCommitSynchronizer
 *
 * author: gaohaoxiang
 * date: 2019/4/18
 */
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

    public boolean commitPrepare(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepareList) throws Exception {
        Map<Broker, List<TransactionPrepare>> brokerPrepareMap = TransactionHelper.splitPrepareByBroker(prepareList);
        CountDownLatch latch = new CountDownLatch(brokerPrepareMap.size());
        boolean[] result = {true};

        for (Map.Entry<Broker, List<TransactionPrepare>> entry : brokerPrepareMap.entrySet()) {
            Broker broker = entry.getKey();
            List<TransactionPrepare> brokerPrepareList = entry.getValue();
            TransactionPrepare brokerPrepare = brokerPrepareList.get(0);
            List<String> txIds = Lists.newLinkedList();

            for (TransactionPrepare prepare : brokerPrepareList) {
                String txId = transactionIdManager.generateId(prepare.getTopic(), prepare.getPartition(), prepare.getApp(),
                        prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch());
                txIds.add(txId);
            }

            CoordinatorSession session = sessionManager.getOrCreateSession(broker);
            TransactionCommitRequest transactionCommitRequest = new TransactionCommitRequest(brokerPrepare.getTopic(), brokerPrepare.getApp(), txIds);
            session.async(new JoyQueueCommand(transactionCommitRequest), new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    if (response.getHeader().getStatus() != JoyQueueCode.SUCCESS.getCode() &&
                            response.getHeader().getStatus() != JoyQueueCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
                        logger.error("commit transaction error, broker: {}, request: {}", broker, transactionCommitRequest);
                        result[0] = false;
                    }
                    latch.countDown();
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    logger.error("commit transaction error, broker: {}, request: {}", broker, transactionCommitRequest, cause);
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

    public boolean commitOffsets(TransactionMetadata transactionMetadata, Set<TransactionOffset> offsets) throws Exception {
        Map<Broker, List<TransactionOffset>> brokerOffsetMap = splitOffsetsByBroker(offsets);
        CountDownLatch latch = new CountDownLatch(brokerOffsetMap.size());
        boolean[] result = {true};

        for (Map.Entry<Broker, List<TransactionOffset>> entry : brokerOffsetMap.entrySet()) {
            Broker broker = entry.getKey();
            Map<String, Map<Integer, IndexAndMetadata>> saveOffsetParam = buildSaveOffsetParam(entry.getValue());

            try {
                CoordinatorSession session = sessionManager.getOrCreateSession(broker);
                ConsumeIndexStoreRequest indexStoreRequest = new ConsumeIndexStoreRequest(transactionMetadata.getApp(), saveOffsetParam);
                Command request = new JoyQueueCommand(indexStoreRequest);

                session.async(request, new CommandCallback() {
                    @Override
                    public void onSuccess(Command request, Command response) {
                        ConsumeIndexStoreResponse payload = (ConsumeIndexStoreResponse) response.getPayload();
                        for (Map.Entry<String, Map<Integer, Short>> topicEntry : payload.getIndexStoreStatus().entrySet()) {
                            String topic = topicEntry.getKey();
                            for (Map.Entry<Integer, Short> partitionEntry : topicEntry.getValue().entrySet()) {
                                if (partitionEntry.getValue() != JoyQueueCode.SUCCESS.getCode()) {
                                    logger.error("commit transaction offset error, broker: {}, topic: {}, partition: {}, code: {}",
                                            broker, topic, partitionEntry.getKey(), JoyQueueCode.valueOf(partitionEntry.getValue()));
                                }
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onException(Command request, Throwable cause) {
                        logger.error("commit transaction offset failed, async transport exception, broker: {}, topic: {}, group: {}",
                                broker, saveOffsetParam, transactionMetadata.getApp(), cause);
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

    protected Map<String, Map<Integer, IndexAndMetadata>> buildSaveOffsetParam(List<TransactionOffset> offsets) {
        Map<String, Map<Integer, IndexAndMetadata>> result = Maps.newHashMap();
        for (TransactionOffset offset : offsets) {
            String topic = offset.getTopic();
            Map<Integer, IndexAndMetadata> partitionMetadataMap = result.get(topic);
            if (partitionMetadataMap == null) {
                partitionMetadataMap = Maps.newHashMap();
                result.put(topic, partitionMetadataMap);
            }
            IndexAndMetadata indexAndMetadata = new IndexAndMetadata(offset.getOffset(), null);
            partitionMetadataMap.put((int) offset.getPartition(), indexAndMetadata);
        }
        return result;
    }

    protected Map<Broker, List<TransactionOffset>> splitOffsetsByBroker(Set<TransactionOffset> offsets) {
        Map<Broker, List<TransactionOffset>> result = Maps.newHashMap();

        for (TransactionOffset offset : offsets) {
            TopicConfig topic = nameService.getTopicConfig(TopicName.parse(offset.getTopic()));
            if (topic == null) {
                continue;
            }
            PartitionGroup partitionGroup = topic.fetchPartitionGroupByPartition(offset.getPartition());
            if (partitionGroup == null) {
                continue;
            }
            Integer leader = partitionGroup.getLeader();
            if (leader == null || leader <= 0) {
                continue;
            }
            Broker broker = nameService.getBroker(leader);
            if (broker == null) {
                continue;
            }
            List<TransactionOffset> brokerOffsets = result.get(broker);
            if (brokerOffsets == null) {
                brokerOffsets = Lists.newLinkedList();
                result.put(broker, brokerOffsets);
            }
            brokerOffsets.add(offset);
        }
        return result;
    }
}