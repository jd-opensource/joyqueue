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
package com.jd.journalq.broker.protocol.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.protocol.converter.CheckResultConverter;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.message.BrokerCommit;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.command.ProduceMessageRollbackRequest;
import com.jd.journalq.network.command.ProduceMessageRollbackResponse;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessageRollbackRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageRollbackRequestHandler implements JournalqCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageRollbackRequestHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessageRollbackRequest produceMessageRollbackRequest = (ProduceMessageRollbackRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessageRollbackRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, produceMessageRollbackRequest.getApp());
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessageRollbackRequest.getTopic()), produceMessageRollbackRequest.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport,
                    produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), checkResult.getJournalqCode());
            return new Command(new ProduceMessageRollbackResponse(CheckResultConverter.convertCommonCode(checkResult.getJournalqCode())));
        }

        ProduceMessageRollbackResponse produceMessageRollbackResponse = produceMessageRollback(connection, produceMessageRollbackRequest);
        return new Command(produceMessageRollbackResponse);
    }

    protected ProduceMessageRollbackResponse produceMessageRollback(Connection connection, ProduceMessageRollbackRequest produceMessageRollbackRequest) {
        Producer producer = new Producer(connection.getId(), produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), Producer.ProducerType.JMQ);

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTopic(produceMessageRollbackRequest.getTopic());
        brokerCommit.setApp(produceMessageRollbackRequest.getApp());
        brokerCommit.setTxId(produceMessageRollbackRequest.getTxId());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return new ProduceMessageRollbackResponse(JournalqCode.SUCCESS);
        } catch (JournalqException e) {
            logger.error("produceMessage rollback exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), e);
            return new ProduceMessageRollbackResponse(JournalqCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage rollback exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), e);
            return new ProduceMessageRollbackResponse(JournalqCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JournalqCommandType.PRODUCE_MESSAGE_ROLLBACK_REQUEST.getCode();
    }
}