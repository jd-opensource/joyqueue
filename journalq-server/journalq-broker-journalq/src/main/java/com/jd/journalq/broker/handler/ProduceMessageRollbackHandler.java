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
package com.jd.journalq.broker.handler;

import com.jd.journalq.broker.JournalqCommandHandler;
import com.jd.journalq.broker.converter.CheckResultConverter;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.message.BrokerCommit;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.command.ProduceMessageRollback;
import com.jd.journalq.network.command.ProduceMessageRollbackAck;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessageRollbackHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageRollbackHandler implements JournalqCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageRollbackHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessageRollback produceMessageRollback = (ProduceMessageRollback) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessageRollback.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, produceMessageRollback.getApp());
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessageRollback.getTopic()), produceMessageRollback.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}",
                    transport, produceMessageRollback.getTopic(), produceMessageRollback.getApp(), checkResult.getJournalqCode());
            return new Command(new ProduceMessageRollbackAck(CheckResultConverter.convertCommonCode(checkResult.getJournalqCode())));
        }

        ProduceMessageRollbackAck produceMessageRollbackAck = produceMessageRollback(connection, produceMessageRollback);
        return new Command(produceMessageRollbackAck);
    }

    protected ProduceMessageRollbackAck produceMessageRollback(Connection connection, ProduceMessageRollback produceMessageRollback) {
        Producer producer = new Producer(connection.getId(), produceMessageRollback.getTopic(), produceMessageRollback.getApp(), Producer.ProducerType.JMQ);

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTopic(produceMessageRollback.getTopic());
        brokerCommit.setApp(produceMessageRollback.getApp());
        brokerCommit.setTxId(produceMessageRollback.getTxId());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return new ProduceMessageRollbackAck(JournalqCode.SUCCESS);
        } catch (JournalqException e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageRollback.getTopic(), produceMessageRollback.getApp(), e);
            return new ProduceMessageRollbackAck(JournalqCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageRollback.getTopic(), produceMessageRollback.getApp(), e);
            return new ProduceMessageRollbackAck(JournalqCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JournalqCommandType.PRODUCE_MESSAGE_ROLLBACK.getCode();
    }
}