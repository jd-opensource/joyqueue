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
package org.joyqueue.broker.kafka.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.kafka.KafkaCommandHandler;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.command.TxnOffsetCommitRequest;
import org.joyqueue.broker.kafka.command.TxnOffsetCommitResponse;
import org.joyqueue.broker.kafka.coordinator.transaction.TransactionCoordinator;
import org.joyqueue.broker.kafka.coordinator.transaction.exception.TransactionException;
import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.joyqueue.broker.kafka.model.OffsetAndMetadata;
import org.joyqueue.broker.kafka.model.PartitionMetadataAndError;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * TxnOffsetCommitRequestHandler
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
@org.joyqueue.network.protocol.annotation.ProduceHandler
public class TxnOffsetCommitRequestHandler implements KafkaCommandHandler, Type, KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(TxnOffsetCommitRequestHandler.class);

    private TransactionCoordinator transactionCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.transactionCoordinator = kafkaContext.getTransactionCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        TxnOffsetCommitRequest txnOffsetCommitRequest = (TxnOffsetCommitRequest) command.getPayload();
        String clientId = KafkaClientHelper.parseClient(txnOffsetCommitRequest.getClientId());
        String groupId = clientId;
        TxnOffsetCommitResponse response = null;

        try {
            Map<String, List<PartitionMetadataAndError>> errors = transactionCoordinator.handleCommitOffset(clientId, txnOffsetCommitRequest.getTransactionId(), groupId,
                    txnOffsetCommitRequest.getProducerId(), txnOffsetCommitRequest.getProducerEpoch(), txnOffsetCommitRequest.getPartitions());
            response = new TxnOffsetCommitResponse(errors);
        } catch (TransactionException e) {
            logger.warn("commit offset to txn exception, code: {}, message: {}, request: {}", e.getCode(), e.getMessage(), txnOffsetCommitRequest);
            response = new TxnOffsetCommitResponse(buildPartitionError(e.getCode(), txnOffsetCommitRequest.getPartitions()));
        } catch (Exception e) {
            logger.error("commit offset to txn exception, request: {}", txnOffsetCommitRequest, e);
            response = new TxnOffsetCommitResponse(buildPartitionError(KafkaErrorCode.exceptionFor(e), txnOffsetCommitRequest.getPartitions()));
        }
        return new Command(response);
    }

    protected Map<String, List<PartitionMetadataAndError>> buildPartitionError(int code, Map<String, List<OffsetAndMetadata>> partitions) {
        Map<String, List<PartitionMetadataAndError>> result = Maps.newHashMapWithExpectedSize(partitions.size());
        for (Map.Entry<String, List<OffsetAndMetadata>> entry : partitions.entrySet()) {
            List<PartitionMetadataAndError> partitionMetadataAndErrors = Lists.newArrayListWithCapacity(entry.getValue().size());
            for (OffsetAndMetadata partition : entry.getValue()) {
                partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition.getPartition(), (short) code));
            }
            result.put(entry.getKey(), partitionMetadataAndErrors);
        }
        return result;
    }

    @Override
    public int type() {
        return KafkaCommandType.TXN_OFFSET_COMMIT.getCode();
    }
}