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
package io.chubao.joyqueue.broker.producer.transaction.handler;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.producer.Produce;
import io.chubao.joyqueue.broker.producer.transaction.command.TransactionRollbackRequest;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.message.BrokerRollback;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.session.Producer;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TransactionRollbackRequestHandler
 *
 * author: gaohaoxiang
 * date: 2019/4/12
 */
public class TransactionRollbackRequestHandler implements CommandHandler, Type {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionRollbackRequestHandler.class);

    private Produce produce;

    public TransactionRollbackRequestHandler(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        TransactionRollbackRequest transactionRollbackRequest = (TransactionRollbackRequest) command.getPayload();
        Producer producer = new Producer(transactionRollbackRequest.getTopic(), transactionRollbackRequest.getTopic(), transactionRollbackRequest.getApp(), Producer.ProducerType.JOYQUEUE);
        int code = JoyQueueCode.SUCCESS.getCode();

        for (String txId : transactionRollbackRequest.getTxIds()) {
            BrokerRollback brokerRollback = new BrokerRollback();
            brokerRollback.setTopic(transactionRollbackRequest.getTopic());
            brokerRollback.setApp(transactionRollbackRequest.getApp());
            brokerRollback.setTxId(txId);

            try {
                produce.putTransactionMessage(producer, brokerRollback);
            } catch (JoyQueueException e) {
                if (e.getCode() == JoyQueueCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
                    logger.error("rollback transaction error, transaction not exists, topic: {}, app: {}, txId: {}", transactionRollbackRequest.getTopic(), transactionRollbackRequest.getApp(), txId);
                } else {
                    logger.error("rollback transaction exception, topic: {}, app: {}, txId: {}", transactionRollbackRequest.getTopic(), transactionRollbackRequest.getApp(), txId, e);
                }
                if (e.getCode() != JoyQueueCode.SUCCESS.getCode()) {
                    code = e.getCode();
                }
            } catch (Exception e) {
                logger.error("rollback transaction exception, topic: {}, app: {}, txId: {}", transactionRollbackRequest.getTopic(), transactionRollbackRequest.getApp(), txId, e);
                code = JoyQueueCode.CN_UNKNOWN_ERROR.getCode();
            }
        }

        return BooleanAck.build(code);
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_ROLLBACK_REQUEST;
    }
}