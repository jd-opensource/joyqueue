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

import org.joyqueue.broker.kafka.KafkaCommandHandler;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.command.AddOffsetsToTxnRequest;
import org.joyqueue.broker.kafka.command.AddOffsetsToTxnResponse;
import org.joyqueue.broker.kafka.coordinator.transaction.TransactionCoordinator;
import org.joyqueue.broker.kafka.coordinator.transaction.exception.TransactionException;
import org.joyqueue.broker.kafka.helper.KafkaClientHelper;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AddOffsetsToTxnRequestHandler
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class AddOffsetsToTxnRequestHandler implements KafkaCommandHandler, Type, KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(AddOffsetsToTxnRequestHandler.class);

    private TransactionCoordinator transactionCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.transactionCoordinator = kafkaContext.getTransactionCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        AddOffsetsToTxnRequest addOffsetsToTxnRequest = (AddOffsetsToTxnRequest) command.getPayload();
        String clientId = KafkaClientHelper.parseClient(addOffsetsToTxnRequest.getClientId());
        String groupId = clientId;
        AddOffsetsToTxnResponse response = null;

        try {
            boolean isSuccess = transactionCoordinator.handleAddOffsetsToTxn(clientId, addOffsetsToTxnRequest.getTransactionId(),
                    groupId, addOffsetsToTxnRequest.getProducerId(), addOffsetsToTxnRequest.getProducerEpoch());
            response = new AddOffsetsToTxnResponse(KafkaErrorCode.NONE.getCode());
        } catch (TransactionException e) {
            logger.warn("add offsets to txn exception, code: {}, message: {}, request: {}", e.getCode(), e.getMessage(), addOffsetsToTxnRequest);
            response = new AddOffsetsToTxnResponse((short) e.getCode());
        } catch (Exception e) {
            logger.error("add offsets to txn exception, request: {}", addOffsetsToTxnRequest, e);
            response = new AddOffsetsToTxnResponse(KafkaErrorCode.exceptionFor(e));
        }

        return new Command(response);
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_OFFSETS_TO_TXN.getCode();
    }
}