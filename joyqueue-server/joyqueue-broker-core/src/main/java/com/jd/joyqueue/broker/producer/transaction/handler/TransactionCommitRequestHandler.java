/**
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
package com.jd.joyqueue.broker.producer.transaction.handler;

import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.producer.Produce;
import com.jd.joyqueue.broker.producer.transaction.command.TransactionCommitRequest;
import com.jd.joyqueue.exception.JoyQueueCode;
import com.jd.joyqueue.exception.JoyQueueException;
import com.jd.joyqueue.message.BrokerCommit;
import com.jd.joyqueue.network.command.BooleanAck;
import com.jd.joyqueue.network.command.CommandType;
import com.jd.joyqueue.network.session.Producer;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Type;
import com.jd.joyqueue.network.transport.command.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TransactionCommitRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionCommitRequestHandler implements CommandHandler, Type {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCommitRequestHandler.class);

    private Produce produce;

    public TransactionCommitRequestHandler(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        TransactionCommitRequest transactionCommitRequest = (TransactionCommitRequest) command.getPayload();
        Producer producer = new Producer(transactionCommitRequest.getTopic(), transactionCommitRequest.getTopic(), transactionCommitRequest.getApp(), Producer.ProducerType.JOYQUEUE);
        int code = JoyQueueCode.SUCCESS.getCode();

        for (String txId : transactionCommitRequest.getTxIds()) {
            BrokerCommit brokerCommit = new BrokerCommit();
            brokerCommit.setTopic(transactionCommitRequest.getTopic());
            brokerCommit.setApp(transactionCommitRequest.getApp());
            brokerCommit.setTxId(txId);

            try {
                produce.putTransactionMessage(producer, brokerCommit);
            } catch (JoyQueueException e) {
                if (e.getCode() == JoyQueueCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
                    logger.error("commit transaction error, transaction not exists, topic: {}, app: {}, txId: {}", transactionCommitRequest.getTopic(), transactionCommitRequest.getApp(), txId);
                } else {
                    logger.error("commit transaction exception, topic: {}, app: {}, txId: {}", transactionCommitRequest.getTopic(), transactionCommitRequest.getApp(), txId, e);
                }
                if (e.getCode() != JoyQueueCode.SUCCESS.getCode()) {
                    code = e.getCode();
                }
            } catch (Exception e) {
                logger.error("commit transaction exception, topic: {}, app: {}, txId: {}", transactionCommitRequest.getTopic(), transactionCommitRequest.getApp(), txId, e);
                code = JoyQueueCode.CN_UNKNOWN_ERROR.getCode();
            }
        }

        return BooleanAck.build(code);
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_COMMIT_REQUEST;
    }
}